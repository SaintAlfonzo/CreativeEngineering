package com.SaintAlfonzo.creativeequipment.mixin;

import com.SaintAlfonzo.creativeequipment.util.ElytraChestplates;
import com.natamus.easyelytratakeoff_common_forge.events.ElytraEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Easy Elytra Takeoff's {@code ElytraEvent.onFirework} gates firework-rocket takeoff on an
 * {@code inAir} check: it scans the 3x3 block layer two blocks below the player's feet
 * ({@code player.blockPosition().below().below()}) and, if every block there is vanilla
 * {@code Blocks.AIR}, treats the player as airborne and returns {@code PASS} — no takeoff.
 *
 * <p>Consequences with DragonLoot's {@code upgraded_dragon_chestplate} (which is not an
 * {@code ElytraItem} and only auto-deploys on jump-in-air): rocket-takeoff works while
 * standing on thick terrain or at the apex of a jump (a solid block enters the scanned
 * layer), but fails while standing on a single glass block over air, or while falling
 * without jumping. The player reads this as "the rocket only works sometimes".
 *
 * <p>This forces {@code inAir} to {@code false} whenever the player wears a
 * {@code #creativeequipment:elytra_chestplates} item, so those chestplates can rocket-takeoff
 * from anywhere — matching their own jump-in-air glide, which has no such ground requirement.
 * Vanilla elytra are tag-gated out and keep EET's stock behaviour.
 *
 * <p>Implementation notes: local slot 8 is {@code inAir} in the shipped
 * {@code easyelytratakeoff-1.19.2-4.2} bytecode, with a single load — the {@code if (inAir)}
 * test immediately after the scan loop. {@code remap = false}: this project ships no Mixin
 * refmap and {@code ElytraEvent} is an unobfuscated mod class (its {@code onFirework} descriptor
 * is spelled with SRG-mapped MC types, reobf'd back by the build). Requires
 * {@code easyelytratakeoff} at runtime, like {@link EasyElytraTakeoffHelperMixin}.
 */
@Mixin(value = ElytraEvent.class, remap = false)
public class EasyElytraTakeoffGroundCheckMixin {

    @ModifyVariable(method = "onFirework", at = @At("LOAD"), index = 8, remap = false)
    private static boolean creativeengineering$allowAirborneTakeoff(boolean inAir, Player player, Level world, InteractionHand hand) {
        if (inAir && ElytraChestplates.isWorn(player)) {
            return false;
        }
        return inAir;
    }
}
