// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/block/MetronomeBlock.kt. Translated to Java
// for Forge 1.19.2:
// - useWithoutItem (1.21, main hand only) -> use() answering the main hand; PASS for the off hand.
// - Properties.of() -> Properties.of(Material.STONE, MaterialColor.NONE); no block codec; getDrops takes LootContext.Builder
//   (block entity read with getOptionalParameter).
// - metronomeEnergy (overworld attachment) -> MetronomeData.get() (overworld SavedData).
// - setting the owner on first use marks the block entity changed (upstream leaves it to the next save).
package earth.terrarium.tempad.common.block;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.common.data.MetronomeData;
import earth.terrarium.tempad.common.registries.ModBlocks;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MetronomeBlock extends BaseEntityBlock {
    public MetronomeBlock() {
        super(Properties.of(Material.STONE, MaterialColor.NONE).strength(3.0f, 1200f));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlocks.metronomeBe.get(), (l, p, s, block) -> block.tick());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof MetronomeBe blockEntity && !level.isClientSide && blockEntity.bootTime == 0) {
            MetronomeData energy = MetronomeData.get();
            if (energy != null && blockEntity.owner != null && blockEntity.owner.getId() != null) {
                blockEntity.initialChronons = energy.remove(blockEntity.owner.getId(), GlobalPos.of(level.dimension(), pos));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof MetronomeBe blockEntity && blockEntity.bootTime == 0 && !level.isClientSide) {
            MetronomeData energy = MetronomeData.get();
            if (energy != null && blockEntity.owner != null && blockEntity.owner.getId() != null) {
                energy.add(blockEntity.owner.getId(), GlobalPos.of(level.dimension(), pos), blockEntity.initialChronons);
                blockEntity.initialChronons = 0;
            }
        }
        super.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level.getBlockEntity(pos) instanceof MetronomeBe blockEntity && player instanceof ServerPlayer opener) {
            if (blockEntity.bootTime == 0) {
                if (blockEntity.owner == null) {
                    blockEntity.owner = player.getGameProfile();
                    blockEntity.setChanged();
                } else if (blockEntity.locked && !Objects.equals(blockEntity.owner.getId(), player.getGameProfile().getId())) {
                    player.displayClientMessage(Component.translatable("error.tempad.block_locked", getName())
                            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(Tempad.ORANGE.getValue() & 0xFFFFFF))), true);
                    return InteractionResult.FAIL;
                }
                blockEntity.openMenu(opener);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder params) {
        ItemStack stack = new ItemStack(ModItems.metronome.get());
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof MetronomeBe blockEntity) {
            ModComponents.setChrononContent(stack, blockEntity.initialChronons);
        }
        List<ItemStack> drops = new ArrayList<>();
        drops.add(stack);
        return drops;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MetronomeBe(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
