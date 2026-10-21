package net.satisfy.vinery.core.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.satisfy.vinery.core.registry.ArmorSetRegistry;
import net.satisfy.vinery.core.util.PortUtil;
import net.satisfy.vinery.forge.VineryConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Winemaker set bonus: the bone meal isn't used up. Port: the {@code [winemaker_set]} settings (alcohol.md B4.7).
 * A refunded use returns PASS, as upstream; the pack's bonemeal charm only refunds CONSUME, so one use is never
 * refunded twice (I7).
 */
@Mixin(BoneMealItem.class)
public abstract class BoneMealItemMixin {
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    @Inject(method = "useOn", at = @At("RETURN"), cancellable = true)
    private void vinery$winemakerSet(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir) {
        if (cir.getReturnValue() != InteractionResult.CONSUME || !(context.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Player player = context.getPlayer();
        if (player == null || !VineryConfig.SET_ENABLED.get() || !ArmorSetRegistry.hasWinemakerSet(player)) {
            return;
        }
        if (level.random.nextDouble() >= VineryConfig.SET_KEEP_CHANCE.get()) {
            return;
        }
        cir.setReturnValue(InteractionResult.PASS);
        ItemStack heldItem = context.getItemInHand();
        if (!player.getAbilities().instabuild) {
            heldItem.grow(1);
        }

        int amount = VineryConfig.SET_DAMAGE_AMOUNT.get();
        VineryConfig.DamageMode mode = VineryConfig.SET_DAMAGE_MODE.get();
        if (mode == VineryConfig.DamageMode.NONE || amount <= 0 || level.random.nextDouble() >= VineryConfig.SET_DAMAGE_CHANCE.get()) {
            return;
        }
        if (mode == VineryConfig.DamageMode.ONE_PIECE) {
            EquipmentSlot slot = ARMOR[level.random.nextInt(ARMOR.length)];
            PortUtil.hurt(player.getItemBySlot(slot), amount, player, slot);
            return;
        }
        for (EquipmentSlot slot : ARMOR) {
            ItemStack armorPiece = player.getItemBySlot(slot);
            if (!armorPiece.isEmpty()) {
                PortUtil.hurt(armorPiece, amount, player, slot);
            }
        }
    }
}
