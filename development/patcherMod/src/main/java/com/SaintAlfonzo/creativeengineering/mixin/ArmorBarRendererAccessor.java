package com.SaintAlfonzo.creativeengineering.mixin;

import com.mojang.datafixers.util.Pair;
import com.redlimerl.detailab.api.render.CustomArmorBar;
import com.redlimerl.detailab.render.ArmorBarRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

/**
 * Exposes Detail Armor Bar's private {@code ArmorBarRenderer#getArmorPoints(Player)} - the exact
 * (ItemStack, CustomArmorBar) list its own row-0 render loop reads from (attribute-modifier bonus
 * points, slot order and all) - so {@code compat.ArmorOverflowRenderer} can draw extra overflow
 * rows using the identical ordering/count instead of re-deriving that algorithm independently and
 * risking drift from detailab's own version.
 *
 * <p>{@code ArmorBarRenderer} is an unobfuscated mod class (detailab ships no SRG mappings of its
 * own to remap against), hence {@code remap = false} - matches every other mixin in this project
 * targeting an optional third-party mod's class directly. If detailab isn't installed, Mixin just
 * skips applying this (target class absent) rather than failing.
 */
@Mixin(value = ArmorBarRenderer.class, remap = false)
public interface ArmorBarRendererAccessor {
    @Invoker("getArmorPoints")
    static List<Pair<ItemStack, CustomArmorBar>> creativeengineering$getArmorPoints(Player player) {
        throw new AssertionError();
    }
}
