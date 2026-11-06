package com.SaintAlfonzo.creativebrewing.mixin.immersiveengineering;

import com.SaintAlfonzo.creativebrewing.CreativeBrewing;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.crafting.PotionRecipeGenerators;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Route IE-Mixer potion brewing entirely through Create's {@code create:potion} fluid, so the
 * IE Mixer's potion recipes are the Create potion-mixing recipes (same {@code from + reagent ->
 * to}, on the IE multiblock). Works in CREATIVEENGINEERING$AMOUNT mB batches (1500 mB, pack
 * design — IE stock and Create Automatic Brewing are 1000). {@code PotionHelperMixin}
 * additionally un-skips the thick / mundane base potions so brews from those bases reach
 * this class.
 *
 * <p>Stock IE only generates <b>regular</b> potion&nbsp;-&gt;&nbsp;regular potion mixer recipes
 * ({@code PotionHelper.applyToAllPotionRecipes} walks {@code POTION_MIXES} + the
 * {@code Items.POTION}-output brewing recipes only — it never touches {@code CONTAINER_MIXES}
 * or the splash / lingering forms). Create's {@code PotionMixingRecipes} does all three bottle
 * types plus the container conversions. Two injects here close that gap:
 *
 * <ul>
 *   <li>{@link #creativeengineering$skipNonPotionBase} — cancels any recipe whose input potion
 *       is {@code EMPTY}. IE's brewing-recipe loop feeds Alex's Mobs' bottle-based bases
 *       (lava&nbsp;vision from a lava bottle, poison&nbsp;resistance from a poison bottle) in as
 *       {@code from == EMPTY}, which this class would otherwise turn into an unobtainable
 *       {@code create:potion{Potion:"minecraft:empty"}} input. Explicit datapack recipes cover
 *       the ones the pack wants craftable.</li>
 *   <li>{@link #creativeengineering$splashLingeringParity} — after the vanilla list is built,
 *       append the SPLASH + LINGERING variant of every {@code POTION_MIXES} step and the
 *       per-potion container conversions ({@code REGULAR + gunpowder -> SPLASH},
 *       {@code SPLASH + dragon's breath -> LINGERING}), all as {@code create:potion} NBT fluids
 *       at 1500 mB. This is what finally gives the dragon's-breath <em>item</em> a use.</li>
 * </ul>
 */
@Mixin(value = PotionRecipeGenerators.class, remap = false)
public class PotionRecipeGeneratorsMixin {

    private static final Logger CREATIVEENGINEERING$LOG = LogUtils.getLogger();

    private static final ResourceLocation CREATIVEENGINEERING$CREATE_POTION =
            new ResourceLocation("create", "potion");

    /** IE Mixer potion working amount. Pack design: 1500 mB per op (IE stock / Create: 1000). */
    private static final int CREATIVEENGINEERING$AMOUNT = 1500;

    /**
     * Lingering brews (any recipe that OUTPUTS a lingering-potion fluid) run at 1000 mB instead —
     * so one dragon's-breath bottle converts a bucket of splash potion, and Create / IE stay in
     * step for lingering. Regular + splash keep the bigger 1500 mB batch.
     */
    private static final int CREATIVEENGINEERING$LINGERING_AMOUNT = 1000;

    /** IE's own generated potion recipes use this energy/tick; keep parity. */
    private static final int CREATIVEENGINEERING$ENERGY = 6400;

    @Redirect(
            method = "registerPotionRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lblusunrize/immersiveengineering/common/fluids/PotionFluid;"
                           + "getFluidStackForType(Lnet/minecraft/world/item/alchemy/Potion;I)"
                           + "Lnet/minecraftforge/fluids/FluidStack;"
            )
    )
    private static FluidStack creativeengineering$mixerOutputCreatePotion(Potion potion, int amount) {
        return PotionFluidHandler.getFluidFromPotion(potion, PotionFluid.BottleType.REGULAR,
                CREATIVEENGINEERING$AMOUNT);
    }

    @Redirect(
            method = "registerPotionRecipe",
            at = @At(
                    value = "INVOKE",
                    target = "Lblusunrize/immersiveengineering/common/crafting/PotionHelper;"
                           + "getFluidTagForType(Lnet/minecraft/world/item/alchemy/Potion;I)"
                           + "Lblusunrize/immersiveengineering/api/crafting/FluidTagInput;"
            )
    )
    private static FluidTagInput creativeengineering$mixerInputCreatePotion(Potion from, int amount) {
        int amt = CREATIVEENGINEERING$AMOUNT;
        // Awkward base step: water bottle equivalent -> plain water fluid, unchanged.
        if (from == Potions.WATER) {
            return new FluidTagInput(FluidTags.WATER, amt);
        }
        // Everything else: consume create:potion with the same NBT shape as the output.
        FluidStack in = PotionFluidHandler.getFluidFromPotion(from, PotionFluid.BottleType.REGULAR, amt);
        return new FluidTagInput(
                Either.right(List.of(CREATIVEENGINEERING$CREATE_POTION)), amt, in.getTag());
    }

    /**
     * IE's brewing-recipe loop feeds Alex's Mobs' bottle-based bases in with {@code from == EMPTY}
     * (a lava bottle / poison bottle isn't a potion). Drop those — an explicit datapack recipe
     * (or, for poison resistance, a re-based {@code strong_poison} brewing mix) covers them.
     */
    @Inject(method = "registerPotionRecipe", at = @At("HEAD"), cancellable = true)
    private static void creativeengineering$skipNonPotionBase(Potion to, Potion from,
            IngredientWithSize reagent, Map<Potion, List<MixerRecipe>> recipes, CallbackInfo ci) {
        if (from == Potions.EMPTY) {
            ci.cancel();
        }
    }

    /**
     * Mirror Create's Automatic-Brewing recipe set onto the IE Mixer for the SPLASH and
     * LINGERING bottle types (IE stock only generates the REGULAR ones).
     */
    @Inject(method = "initPotionRecipes", at = @At("RETURN"), cancellable = true)
    private static void creativeengineering$splashLingeringParity(
            CallbackInfoReturnable<List<MixerRecipe>> cir) {

        List<MixerRecipe> out = new ArrayList<>(cir.getReturnValue());
        int before = out.size();

        // Phase B parity: every potion -> potion step, in SPLASH and LINGERING form.
        for (PotionBrewing.Mix<Potion> m :
                PotionRecipeGeneratorsMixin.<Potion>creativeengineering$mixes("f_43494_", "POTION_MIXES")) {
            Potion from = m.from.value();
            Potion to = m.to.value();
            if (from == Potions.WATER || to == Potions.WATER
                    || from == Potions.EMPTY || to == Potions.EMPTY) {
                continue;
            }
            IngredientWithSize[] reagent = { new IngredientWithSize(m.ingredient) };
            creativeengineering$addMixer(out, from, to,
                    PotionFluid.BottleType.SPLASH, PotionFluid.BottleType.SPLASH, reagent);
            creativeengineering$addMixer(out, from, to,
                    PotionFluid.BottleType.LINGERING, PotionFluid.BottleType.LINGERING, reagent);
        }

        // Phase C parity: container conversions (gunpowder / dragon's breath) per potion.
        for (PotionBrewing.Mix<Item> c :
                PotionRecipeGeneratorsMixin.<Item>creativeengineering$mixes("f_43495_", "CONTAINER_MIXES")) {
            PotionFluid.BottleType fromType = PotionFluidHandler.bottleTypeFromItem(c.from.value());
            PotionFluid.BottleType toType = PotionFluidHandler.bottleTypeFromItem(c.to.value());
            if (fromType == null || toType == null || fromType == toType) {
                continue;
            }
            IngredientWithSize[] reagent = { new IngredientWithSize(c.ingredient) };
            for (Potion p : ForgeRegistries.POTIONS) {
                if (p == Potions.EMPTY || p == Potions.WATER) {
                    continue;
                }
                creativeengineering$addMixer(out, p, p, fromType, toType, reagent);
            }
        }

        // Alex's Mobs Lava Vision base — its real brew is a non-potion lava bottle, so neither
        // mixer can generate it. Add the lava-fluid version here (Create side is a KubeJS recipe).
        creativeengineering$addLavaVision(out);

        cir.setReturnValue(out);
        CREATIVEENGINEERING$LOG.info(
                "[CE] IE Mixer: +{} splash/lingering potion recipes (parity with Create Automatic Brewing)",
                out.size() - before);
    }

    private static void creativeengineering$addLavaVision(List<MixerRecipe> out) {
        Potion lavaVision = ForgeRegistries.POTIONS.getValue(
                new ResourceLocation("alexsmobs", "lava_vision"));
        Item tooth = ForgeRegistries.ITEMS.getValue(
                new ResourceLocation("alexsmobs", "bone_serpent_tooth"));
        if (lavaVision == null || tooth == null || tooth == Items.AIR) {
            return;
        }
        FluidStack outStack = PotionFluidHandler.getFluidFromPotion(
                lavaVision, PotionFluid.BottleType.REGULAR, CREATIVEENGINEERING$AMOUNT);
        FluidTagInput lavaIn = new FluidTagInput(FluidTags.LAVA, CREATIVEENGINEERING$AMOUNT);
        IngredientWithSize[] reagent = { new IngredientWithSize(Ingredient.of(tooth)) };
        out.add(new MixerRecipe(
                new ResourceLocation(CreativeBrewing.NS, "mixer_potion_parity/lava_vision_base"),
                outStack, lavaIn, reagent, CREATIVEENGINEERING$ENERGY));
    }

    private static void creativeengineering$addMixer(List<MixerRecipe> out, Potion from, Potion to,
            PotionFluid.BottleType fromType, PotionFluid.BottleType toType,
            IngredientWithSize[] reagents) {

        // The smaller batch is only for the conversion INTO lingering (splash + dragon's breath —
        // one dragon's-breath bottle converts a bucket). Modifying an already-lingering potion
        // (lengthen / amplify / any POTION_MIXES step) stays at the full pack amount.
        int amt = (toType == PotionFluid.BottleType.LINGERING
                && fromType != PotionFluid.BottleType.LINGERING)
                ? CREATIVEENGINEERING$LINGERING_AMOUNT
                : CREATIVEENGINEERING$AMOUNT;
        FluidStack outStack = PotionFluidHandler.getFluidFromPotion(to, toType, amt);
        FluidStack inStack = PotionFluidHandler.getFluidFromPotion(from, fromType, amt);
        CompoundTag inTag = inStack.getTag();

        FluidTagInput in = new FluidTagInput(
                Either.right(List.of(CREATIVEENGINEERING$CREATE_POTION)), amt, inTag);

        ResourceLocation id = new ResourceLocation(CreativeBrewing.NS,
                "mixer_potion_parity/" + creativeengineering$slug(from, fromType)
                        + "_to_" + creativeengineering$slug(to, toType) + "_" + out.size());

        out.add(new MixerRecipe(id, outStack, in, reagents, CREATIVEENGINEERING$ENERGY));
    }

    /**
     * Read one of {@link PotionBrewing}'s private static mix lists reflectively. SRG name first
     * (this pack only ever runs reobfuscated — same reason the {@code @Redirect} targets in this
     * file are SRG), Mojmap name as a dev-run fallback.
     */
    @SuppressWarnings("unchecked")
    private static <T> List<PotionBrewing.Mix<T>> creativeengineering$mixes(String srg, String mojmap) {
        for (String name : new String[] { srg, mojmap }) {
            try {
                Field f = PotionBrewing.class.getDeclaredField(name);
                f.setAccessible(true);
                return (List<PotionBrewing.Mix<T>>) f.get(null);
            } catch (ReflectiveOperationException ignored) {
                // try the next name
            }
        }
        CREATIVEENGINEERING$LOG.error(
                "[CE] PotionBrewing mix list {}/{} not found - IE Mixer splash/lingering parity disabled",
                srg, mojmap);
        return List.of();
    }

    private static String creativeengineering$slug(Potion p, PotionFluid.BottleType bt) {
        ResourceLocation rl = ForgeRegistries.POTIONS.getKey(p);
        String base = rl == null ? "unknown" : rl.getNamespace() + "_" + rl.getPath();
        return (base + "_" + bt.name().toLowerCase(Locale.ROOT)).replaceAll("[^a-z0-9_./-]", "_");
    }
}
