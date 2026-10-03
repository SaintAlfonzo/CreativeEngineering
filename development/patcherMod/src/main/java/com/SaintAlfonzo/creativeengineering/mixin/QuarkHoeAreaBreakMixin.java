package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.crops.HoeHarvestArea;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.quark.base.handler.MiscUtil;
import vazkii.quark.content.tweaks.module.HoeHarvestingModule;

/**
 * Quark's hoe area break (break a plant with a hoe, the plants around it break too) over the
 * per-hoe {@link HoeHarvestArea} instead of Quark's centred radius. Takes over where Quark asks for
 * the range, so Quark's own checks (a hoe, a plant at the broken block) have already passed; the
 * loop body is Quark 3.4-418's, unchanged.
 */
@Mixin(value = HoeHarvestingModule.class, remap = false)
public abstract class QuarkHoeAreaBreakMixin {

    @Shadow
    private boolean canHarvest(Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        throw new AssertionError();
    }

    @Inject(method = "onBlockBroken", at = @At(value = "INVOKE",
            target = "Lvazkii/quark/content/tweaks/module/HoeHarvestingModule;getRange(Lnet/minecraft/world/item/ItemStack;)I"),
            cancellable = true, require = 1, remap = false)
    private void creativeengineering$sizedArea(BreakEvent event, CallbackInfo ci) {
        Level level = (Level) event.getLevel();
        Player player = event.getPlayer();
        BlockPos basePos = event.getPos();
        ItemStack stack = player.getMainHandItem();
        HoeHarvestArea.Area area = HoeHarvestArea.of(player, stack);

        boolean brokeNonInstant = false;
        for (int x = area.minX(); x <= area.maxX(); x++) {
            for (int z = area.minZ(); z <= area.maxZ(); z++) {
                if (x == 0 && z == 0) continue;
                BlockPos pos = basePos.offset(x, 0, z);
                BlockState state = level.getBlockState(pos);
                if (!canHarvest(player, level, pos, state)) continue;
                Block block = state.getBlock();
                if (state.getDestroySpeed(level, pos) != 0.0F) {
                    brokeNonInstant = true;
                }
                if (block.canHarvestBlock(state, level, pos, player)) {
                    block.playerDestroy(level, player, pos, state, level.getBlockEntity(pos), stack);
                }
                level.destroyBlock(pos, false);
                level.levelEvent(2001, pos, Block.getId(state));
            }
        }
        if (brokeNonInstant) {
            MiscUtil.damageStack(player, InteractionHand.MAIN_HAND, stack, 1);
        }
        ci.cancel();
    }
}
