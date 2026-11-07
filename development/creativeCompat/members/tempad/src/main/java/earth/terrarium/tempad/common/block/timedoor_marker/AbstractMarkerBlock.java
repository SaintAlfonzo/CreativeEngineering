// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/block/timedoor_marker/AbstractMarkerBlock.kt.
// Translated to Java for Forge 1.19.2:
// - useItemOn + useWithoutItem (1.21) -> use(): the held-item branch first, then (main hand only, when it passed) the
//   empty-hand branch; ItemInteractionResult.sidedSuccess -> InteractionResult.sidedSuccess; PASS_TO_DEFAULT -> PASS.
// - Properties.of() -> Properties.of(Material.STONE, MaterialColor.NONE); no block codec; getDrops takes
//   LootContext.Builder (block entity read with getOptionalParameter).
// - NeoForge Tags.Items.DYES_* -> Forge Tags.Items.DYES_*; Resourceful ConstantColors -> the same RGB values (alpha 0, as
//   Resourceful's named colours); the CUSTOM_NAME component -> setHoverName.
// - TooltipFlag.hasShiftDown() (1.21) -> Screen.hasShiftDown() on the client (1.19.2 flags carry no shift state).
// - anchorPoints (overworld attachment) -> AnchorPointsData (overworld SavedData).
// - Setting the owner on first use marks the block entity changed (attachment write).
// Kept upstream quirks: the card branch overwrites the whole held card stack; a missing anchor id clears the card's
// target (upstream would throw on `id!!`); dyes are not consumed.
package earth.terrarium.tempad.common.block.timedoor_marker;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.common.location_handlers.AnchorPointsData;
import earth.terrarium.tempad.common.location_handlers.AnchorPointsHandler;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModItems;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public abstract class AbstractMarkerBlock extends BaseEntityBlock {
    public static final VoxelShape UP_SHAPE = Shapes.or(
            box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            box(3.0, 3.0, 3.0, 13.0, 4.0, 13.0)
    );

    public static final VoxelShape DOWN_SHAPE = Shapes.or(
            box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0),
            box(3.0, 12.0, 3.0, 13.0, 13.0, 13.0)
    );

    // Resourceful Lib ConstantColors (CSS named colours, alpha 0).
    private static final Color RED = new Color(0xFF0000);
    private static final Color GREEN = new Color(0x008000);
    private static final Color BLUE = new Color(0x0000FF);
    private static final Color YELLOW = new Color(0xFFFF00);
    private static final Color BLACK = new Color(0x000000);
    private static final Color BROWN = new Color(0xA52A2A);
    private static final Color CYAN = new Color(0x00FFFF);
    private static final Color GRAY = new Color(0x808080);
    private static final Color POWDERBLUE = new Color(0xB0E0E6);
    private static final Color LIGHTGRAY = new Color(0xD3D3D3);
    private static final Color LIME = new Color(0x00FF00);
    private static final Color MAGENTA = new Color(0xFF00FF);
    private static final Color PINK = new Color(0xFFC0CB);
    private static final Color PURPLE = new Color(0x800080);
    private static final Color WHITE = new Color(0xFFFFFF);

    protected AbstractMarkerBlock() {
        super(Properties.of(Material.STONE, MaterialColor.NONE).strength(3.0f, 6.0f));
        this.registerDefaultState(stateDefinition.any()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
                .setValue(BlockStateProperties.UP, true));
    }

    static Style orange() {
        return Style.EMPTY.withColor(TextColor.fromRgb(Tempad.ORANGE.getValue() & 0xFFFFFF));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltips, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltips, flag);
        GameProfile owner = ModComponents.getOwner(stack);
        if (owner != null) {
            tooltips.add(Component.translatable("item.tempad.location_card.created_by",
                    Component.literal(owner.getName()).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GRAY));
        }
        UUID anchorId = ModComponents.getAnchorId(stack);
        if (anchorId != null) {
            if (shiftDown()) {
                tooltips.add(Component.translatable("item.tempad.location_card.id", anchorId.toString()).withStyle(ChatFormatting.DARK_GRAY));
            } else {
                tooltips.add(Component.translatable("misc.tempad.shift_key_info",
                        Component.keybind("key.keyboard.left.shift").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    private static boolean shiftDown() {
        Boolean down = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> Screen::hasShiftDown);
        return down != null && down;
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult withItem = useItemOn(player.getItemInHand(hand), state, level, pos, player);
        if (withItem != InteractionResult.PASS || hand != InteractionHand.MAIN_HAND) return withItem;
        return useWithoutItem(level, pos, player);
    }

    /** Upstream {@code useItemOn}; PASS = PASS_TO_DEFAULT_BLOCK_INTERACTION. */
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
        if (stack.is(ModItems.locationCard.get())) {
            if (level.isClientSide) return InteractionResult.sidedSuccess(true);
            AbstractMarkerBe blockEntity = (AbstractMarkerBe) level.getBlockEntity(pos);
            UUID id = blockEntity == null ? null : blockEntity.id;
            ModComponents.setPortalTarget(stack, id == null ? null : new AnchorPointsHandler(player.getGameProfile()).getSerializable(id));
            return InteractionResult.sidedSuccess(false);
        }

        Color color;
        if (stack.is(Tags.Items.DYES_RED)) color = RED;
        else if (stack.is(Tags.Items.DYES_GREEN)) color = GREEN;
        else if (stack.is(Tags.Items.DYES_BLUE)) color = BLUE;
        else if (stack.is(Tags.Items.DYES_YELLOW)) color = YELLOW;
        else if (stack.is(Tags.Items.DYES_BLACK)) color = BLACK;
        else if (stack.is(Tags.Items.DYES_BROWN)) color = BROWN;
        else if (stack.is(Tags.Items.DYES_CYAN)) color = CYAN;
        else if (stack.is(Tags.Items.DYES_GRAY)) color = GRAY;
        else if (stack.is(Tags.Items.DYES_LIGHT_BLUE)) color = POWDERBLUE;
        else if (stack.is(Tags.Items.DYES_LIGHT_GRAY)) color = LIGHTGRAY;
        else if (stack.is(Tags.Items.DYES_LIME)) color = LIME;
        else if (stack.is(Tags.Items.DYES_MAGENTA)) color = MAGENTA;
        else if (stack.is(Tags.Items.DYES_ORANGE)) color = Tempad.ORANGE;
        else if (stack.is(Tags.Items.DYES_PINK)) color = PINK;
        else if (stack.is(Tags.Items.DYES_PURPLE)) color = PURPLE;
        else if (stack.is(Tags.Items.DYES_WHITE)) color = WHITE;
        else return InteractionResult.PASS;

        if (level instanceof ServerLevel && level.getBlockEntity(pos) instanceof AbstractMarkerBe marker) {
            marker.setColor(color);
            level.setBlockAndUpdate(pos, state);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Upstream {@code useWithoutItem}. */
    protected InteractionResult useWithoutItem(Level level, BlockPos pos, Player player) {
        if (level.isClientSide) return InteractionResult.sidedSuccess(true);
        if (level.getBlockEntity(pos) instanceof AbstractMarkerBe marker) {
            if (marker.owner == null) {
                marker.owner = player.getGameProfile();
                marker.setChanged();
            } else if (marker.locked && !Objects.equals(marker.owner.getId(), player.getGameProfile().getId())) {
                player.displayClientMessage(Component.translatable("error.tempad.block_locked", getName()).withStyle(orange()), true);
                return InteractionResult.FAIL;
            }
            marker.openScreen(player);
        }
        return InteractionResult.sidedSuccess(false);
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder params) {
        List<ItemStack> result = super.getDrops(state, params);
        for (ItemStack item : result) {
            if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof AbstractMarkerBe marker) {
                ModComponents.setColor(item, marker.getColor());
                item.setHoverName(marker.getPosName());
                ModComponents.setAnchorId(item, marker.id);
                ModComponents.setOwner(item, marker.owner);
            }
        }
        return result;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        if (!level.isClientSide) {
            AnchorPointsData points = AnchorPointsData.get();
            if (level.getBlockEntity(pos) instanceof AbstractMarkerBe marker && points != null) points.add(marker);
        }
        super.onPlace(state, level, pos, oldState, moved);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (level instanceof ServerLevel) {
            AnchorPointsData points = AnchorPointsData.get();
            if (level.getBlockEntity(pos) instanceof AbstractMarkerBe marker && marker.id != null && points != null) points.remove(marker.id);
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING).add(BlockStateProperties.UP);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite())
                .setValue(BlockStateProperties.UP, context.getNearestLookingVerticalDirection() == Direction.DOWN);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(BlockStateProperties.UP) ? UP_SHAPE : DOWN_SHAPE;
    }
}
