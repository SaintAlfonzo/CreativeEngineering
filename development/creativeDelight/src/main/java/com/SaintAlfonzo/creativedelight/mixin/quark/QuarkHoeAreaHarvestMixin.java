package com.SaintAlfonzo.creativedelight.mixin.quark;

import com.SaintAlfonzo.creativedelight.crops.CropParity;
import com.SaintAlfonzo.creativedelight.crops.HoeHarvestArea;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.quark.content.tweaks.module.HoeHarvestingModule;
import vazkii.quark.content.tweaks.module.SimpleHarvestModule;

/**
 * Quark's hoe right-click (harvest and replant every ripe crop, click every berry bush) over the
 * per-hoe {@link HoeHarvestArea} instead of Quark's centred radius. Takes over where Quark asks for
 * the range, after its claim, till and "is there a crop here" checks; only for hoes, so the empty
 * hand path stays Quark's. The loop and the durability cost are Quark 3.4-418's, unchanged. Runs on
 * both sides like Quark's, and both compute the same area from the player's look.
 *
 * <p>Climbing own-pick crops (farming/crops #29): Quark reaches the clicked height and one above. A column of
 * {@link CropParity#OWN_PICK_CROPS} (tomato vines on ropes and sticks) goes on upward, each level clicked, while
 * the block and the one below it are own-pick crops, at most {@link HoeHarvestArea#MAX_CLIMB} levels above the
 * second (8 levels in all from the clicked one). Area
 * hoes only (a 1x1 hoe leaves own-pick crops to the clicked block's own use).
 */
@Mixin(value = SimpleHarvestModule.class, remap = false)
public abstract class QuarkHoeAreaHarvestMixin {

    @Shadow
    private static boolean handle(Player player, InteractionHand hand, BlockPos pos, boolean doRightClick, boolean isHoe) {
        throw new AssertionError();
    }

    @Inject(method = "click", at = @At(value = "INVOKE",
            target = "Lvazkii/quark/content/tweaks/module/HoeHarvestingModule;getRange(Lnet/minecraft/world/item/ItemStack;)I"),
            cancellable = true, require = 1, remap = false)
    private static void creativeengineering$sizedArea(Player player, InteractionHand hand, BlockPos pos, BlockHitResult pick,
                                                     CallbackInfoReturnable<Boolean> cir) {
        ItemStack inHand = player.getItemInHand(hand);
        if (!HoeHarvestingModule.isHoe(inHand)) return;
        HoeHarvestArea.Area area = HoeHarvestArea.of(player, inHand);
        boolean doRightClick = area.size() > 1;

        boolean hasHarvested = false;
        for (int x = area.minX(); x <= area.maxX(); x++) {
            for (int z = area.minZ(); z <= area.maxZ(); z++) {
                BlockPos shiftPos = pos.offset(x, 0, z);
                boolean atDone = handle(player, hand, shiftPos, doRightClick, true);
                if (atDone || handle(player, hand, shiftPos.above(), doRightClick, true)) {
                    hasHarvested = true;
                }
                // Quark skips the level above once the clicked level acted; a climbing column continues from there.
                if (doRightClick && (atDone ? climb(player, hand, shiftPos.above(), HoeHarvestArea.MAX_CLIMB + 1)
                        : climb(player, hand, shiftPos.above(2), HoeHarvestArea.MAX_CLIMB))) {
                    hasHarvested = true;
                }
            }
        }
        if (hasHarvested && !player.level.isClientSide && SimpleHarvestModule.harvestingCostsDurability) {
            inHand.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        }
        cir.setReturnValue(hasHarvested);
    }

    /** Clicks up to {@code levels} own-pick crops upward from {@code start} while they stack on own-pick crops. */
    private static boolean climb(Player player, InteractionHand hand, BlockPos start, int levels) {
        boolean any = false;
        BlockPos p = start;
        for (int i = 0; i < levels; i++, p = p.above()) {
            if (!CropParity.isOwnPickCrop(player.level.getBlockState(p))
                    || !CropParity.isOwnPickCrop(player.level.getBlockState(p.below()))) {
                break;
            }
            if (handle(player, hand, p, true, true)) any = true;
        }
        return any;
    }
}
