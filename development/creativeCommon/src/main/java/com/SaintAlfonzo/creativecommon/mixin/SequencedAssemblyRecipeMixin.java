package com.SaintAlfonzo.creativecommon.mixin;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Create's {@code create:sequenced_assembly} recipe (used by {@code NCH.echoAssembly} /
 * {@code felsicAssembly} / {@code dragonAssembly} for the netherite-&gt;echo-&gt;felsic-&gt;dragon
 * gear progression) has no NBT passthrough at all: {@code advance(ItemStack)} builds every
 * intermediate "incomplete" stack from the recipe's own transitional-item template
 * ({@code getTransitionalItem().copy()}), and the final step's {@code rollResult()} builds the
 * finished item from the recipe's result-pool template — neither ever looks at the actual stack
 * being fed through. So today, enchanting or naming a netherite tool/armor piece before running
 * it through echo/felsic/dragon assembly silently drops that NBT at the very first step.
 *
 * <p>{@code advance(ItemStack input)}'s {@code input} argument is, however, the real evolving
 * stack (the original enchanted/named gear piece on step 0, whatever step 0 returned on step 1,
 * etc. — confirmed via {@code ProcessingRecipe.rollResults()}, which substitutes
 * {@code forcedResult.get()} — i.e. this method's return value — directly as the actual output
 * item). So we can carry the original piece's identity NBT along for the ride: stash it in a
 * {@code CEStash} tag on every intermediate stack, then splice it onto the real final item.
 *
 * <p>Scoped by content, not recipe id: it only ever fires when the input actually carries one of
 * {@link #GEAR_NBT_KEYS}, which in this pack only enchanted/named tools and armor do. Every other
 * {@code createSequencedAssembly} recipe (ingots, redstone components, food, ...) is unaffected.
 *
 * <p>{@code advance} is Create's own method (unobfuscated at runtime), hence {@code remap = false}
 * — see [[patchermod-coremod]].
 */
@Mixin(value = SequencedAssemblyRecipe.class, remap = false)
public class SequencedAssemblyRecipeMixin {

    @Unique
    private static final String[] GEAR_NBT_KEYS = {
            "Enchantments", "StoredEnchantments", "display", "RepairCost", "Unbreakable", "AttributeModifiers"
    };

    @Inject(method = "advance", at = @At("RETURN"))
    private void creativecommon$preserveGearNbt(ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (result.isEmpty()) return;

        CompoundTag stash = creativecommon$extractGearStash(input);
        if (stash == null) return;

        CompoundTag resultTag = result.getOrCreateTag();
        if (resultTag.contains("SequencedAssembly")) {
            // Still mid-sequence: carry the stash forward on the incomplete item.
            resultTag.put("CEStash", stash);
        } else {
            // This is the real, finished item - splice the original piece's identity back on.
            for (String key : stash.getAllKeys()) {
                resultTag.put(key, stash.get(key).copy());
            }
        }
    }

    @Unique
    private static CompoundTag creativecommon$extractGearStash(ItemStack input) {
        if (!input.hasTag()) return null;
        CompoundTag tag = input.getTag();

        // Already mid-sequence: forward whatever was stashed on step 0 (may be absent/empty).
        if (tag.contains("CEStash")) {
            return tag.getCompound("CEStash").copy();
        }
        if (tag.contains("SequencedAssembly")) {
            return null;
        }

        // Step 0: input is the real gear piece the player fed in - lift its identity NBT.
        CompoundTag stash = new CompoundTag();
        for (String key : GEAR_NBT_KEYS) {
            if (tag.contains(key)) {
                stash.put(key, tag.get(key).copy());
            }
        }
        return stash.isEmpty() ? null : stash;
    }
}
