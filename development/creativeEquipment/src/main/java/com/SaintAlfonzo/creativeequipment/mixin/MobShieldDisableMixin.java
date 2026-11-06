package com.SaintAlfonzo.creativeequipment.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.SaintAlfonzo.creativeequipment.shield.ShieldConfig;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.ToolActions;

/**
 * A mob's axe disabling a player's shield ({@code Mob#maybeDisableShield}, {@code m_21424_}: vindicators,
 * piglin brutes, any mob holding an axe). Vanilla's version, bytecode-checked on the Forge 43.3.9 client
 * jar, differs from the player-axe path ({@code Player#disableShield}, see
 * {@link ShieldDisableCooldownMixin}) in three ways, all replaced here:
 * <ul>
 *   <li>it only fires when the player holds exactly {@code Items.SHIELD}, so tier shields (and IE's /
 *       Alex's Mobs' shields) were immune to mob axes - now any item that can {@code SHIELD_BLOCK};</li>
 *   <li>it puts a hardcoded 100-tick cooldown on {@code Items.SHIELD} - now
 *       {@code [shields].disableCooldownTicks} on the shield actually raised;</li>
 *   <li>it never lowered the shield - now it calls {@code stopUsingItem}, like the player path.</li>
 * </ul>
 * The weapon test uses Forge's {@code canDisableShield} hook (true for every {@code AxeItem}) instead of
 * vanilla's {@code instanceof AxeItem}. The chance is vanilla's: 25% + 5% per Efficiency level on the mob.
 *
 * <p>Vanilla-class target, SRG name with {@code remap = false}, same idiom as
 * {@link LivingEntityShieldMixin}. Any error falls back to vanilla's own method.
 */
@Mixin(Mob.class)
public abstract class MobShieldDisableMixin {

    @Inject(method = "m_21424_", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$disableAnyShield(Player player, ItemStack weapon, ItemStack shield, CallbackInfo ci) {
        try {
            Mob self = (Mob) (Object) this;
            if (!weapon.isEmpty() && !shield.isEmpty()
                    && shield.canPerformAction(ToolActions.SHIELD_BLOCK)
                    && weapon.canDisableShield(shield, player, self)) {
                float chance = 0.25F + EnchantmentHelper.getBlockEfficiency(self) * 0.05F;
                if (self.getRandom().nextFloat() < chance) {
                    player.getCooldowns().addCooldown(shield.getItem(), ShieldConfig.disableCooldownTicks());
                    player.stopUsingItem();
                    self.level.broadcastEntityEvent(player, (byte) 30);
                }
            }
            ci.cancel();
        } catch (Throwable ignored) {
            // fall through to vanilla's own method
        }
    }
}
