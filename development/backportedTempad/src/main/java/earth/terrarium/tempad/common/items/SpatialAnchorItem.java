// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/SpatialAnchorItem.kt (items
// tempad:timedoor_marker and tempad:chronomark). Translated to Java for Forge 1.19.2: the properties are passed in (ModItems
// adds the creative tab); the CUSTOM_NAME component -> the stack's custom hover name; block entity attachments ->
// AbstractMarkerBe fields.
// Fix (TODO gameplay/tnt-and-portal #9 item 4): upstream's onPlace registers the fresh block entity under a new random
// anchor id before this copies the stack's anchor_id onto it; the registration now moves to the stored id.
package earth.terrarium.tempad.common.items;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.common.block.timedoor_marker.AbstractMarkerBe;
import earth.terrarium.tempad.common.location_handlers.AnchorPointsData;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class SpatialAnchorItem extends BlockItem {
    public SpatialAnchorItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        if (level.getBlockEntity(pos) instanceof AbstractMarkerBe blockEntity) {
            Color color = ModComponents.getColor(stack);
            if (color != null) blockEntity.setColor(color);
            if (stack.hasCustomHoverName()) blockEntity.setPosName(stack.getHoverName());
            UUID anchorId = ModComponents.getAnchorId(stack);
            if (anchorId != null && !anchorId.equals(blockEntity.id)) adoptStoredId(level, pos, blockEntity, anchorId);
            blockEntity.angle = (int) blockEntity.getFacingAngle();
            blockEntity.yOffset = state.getValue(BlockStateProperties.UP) ? 0.5f : -2f;
            if (player != null) blockEntity.owner = player.getGameProfile();
            blockEntity.setChanged();
            // A creative ctrl-pick copy carries the block entity NBT (BlockEntityTag, anchor_id included since #11): the
            // vanilla load below would hand the copy the original's anchor id. The id chosen above (stored or fresh, and
            // registered in AnchorPointsData) stays.
            UUID chosen = blockEntity.id;
            boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
            if (!Objects.equals(chosen, blockEntity.id)) {
                blockEntity.id = chosen;
                blockEntity.setChanged();
            }
            return result;
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    /**
     * Fix (TODO gameplay/tnt-and-portal #9 item 4): the block's onPlace has already registered the new block entity
     * under a fresh random id. Move that registration to the stack's stored id, so cards written from the marker before
     * it was picked up still find it and no stale entry stays behind. A stored id that another standing marker still
     * holds (a copied stack) is left alone; the new marker keeps its fresh id.
     */
    private static void adoptStoredId(Level level, BlockPos pos, AbstractMarkerBe blockEntity, UUID anchorId) {
        if (level.isClientSide) {
            blockEntity.id = anchorId;
            return;
        }
        AnchorPointsData points = AnchorPointsData.get();
        if (points == null) {
            blockEntity.id = anchorId;
            return;
        }
        GlobalPos here = GlobalPos.of(level.dimension(), pos);
        GlobalPos holder = points.anchors.get(anchorId);
        if (holder != null && !holder.equals(here)) {
            AbstractMarkerBe other = AnchorPointsData.getBlockEntity(holder);
            if (other != null && anchorId.equals(other.id)) return;
        }
        if (blockEntity.id != null) points.remove(blockEntity.id);
        blockEntity.id = anchorId;
        points.add(blockEntity);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        GameProfile owner = ModComponents.getOwner(context.getItemInHand());
        if (owner != null) {
            Player player = context.getPlayer();
            if (!Objects.equals(owner.getId(), player == null ? null : player.getGameProfile().getId())) {
                if (player != null) {
                    player.displayClientMessage(Component.translatable("block.tempad.marker.owner_mismatch.place")
                            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Tempad.ORANGE.getValue() & 0xFFFFFF))), true);
                }
                return InteractionResult.FAIL;
            }
        }
        return super.place(context);
    }
}
