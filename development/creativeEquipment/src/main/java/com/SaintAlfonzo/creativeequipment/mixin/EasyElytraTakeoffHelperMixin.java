package com.SaintAlfonzo.creativeequipment.mixin;

import com.SaintAlfonzo.creativeequipment.util.ElytraChestplates;
import com.natamus.easyelytratakeoff.forge.services.ForgeElytraEventHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Easy Elytra Takeoff decides whether to run its firework-rocket takeoff with
 * {@code ForgeElytraEventHelper.isWearingAnElytra(player)}, which only recognises a
 * literal {@code instanceof ElytraItem} in an armour slot (plus a Curios "flight modifier"
 * attribute). DragonLoot's {@code upgraded_dragon_chestplate} glides via its own
 * {@code LivingEntity#updateFallFlying} / {@code Player#tryToStartFallFlying} mixins — it is
 * a plain {@code ArmorItem}, not an {@code ElytraItem} — so EET never fires for it and you
 * can only take off by jump-in-air, not with a rocket.
 *
 * <p>This widens the check: any armour-slot stack that Forge already considers elytra-capable
 * ({@code ItemStack#canElytraFly}) OR that is in {@code #creativeequipment:elytra_chestplates}
 * (populated in KubeJS, reload-able) counts as "wearing an elytra". Once this returns true
 * EET force-sets the fall-flying flag for a few ticks and DragonLoot's own tick mixin keeps
 * the glide going.
 *
 * <p>{@code ForgeElytraEventHelper} and its method are unobfuscated; this project ships no
 * Mixin refmap, so {@code remap = false}. The body's {@code getArmorSlots} / {@code is} are
 * Mojmap and mapped back by the reobf step.
 */
@Mixin(value = ForgeElytraEventHelper.class, remap = false)
public class EasyElytraTakeoffHelperMixin {

    @Inject(method = "isWearingAnElytra", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$acceptModdedElytra(Player player, CallbackInfoReturnable<Boolean> cir) {
        for (ItemStack stack : player.getArmorSlots()) {
            if (stack.isEmpty()) continue;
            if (stack.is(ElytraChestplates.TAG) || stack.canElytraFly(player)) {
                cir.setReturnValue(Boolean.TRUE);
                return;
            }
        }
    }
}
