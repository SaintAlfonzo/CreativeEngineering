package com.SaintAlfonzo.creativeengineering.compat;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.fluids.transfer.EmptyingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Potion-style effect lines for drink fluids (Central Kitchen's ciders, teas and the like). A drink fluid has
 * no effects of its own; its effects are those of the item it empties from. The map is read from Create's
 * emptying recipes on the client (item with food effects -> fluid without NBT) and rebuilt whenever recipes
 * sync. A fluid two items empty into with different effects is left out rather than guessed.
 *
 * <p>Lines match vanilla's potion tooltip ({@code PotionUtils.addPotionTooltip}): "Absorption II (0:30)",
 * coloured by the effect's category. Used by {@code JeiFluidHelperTooltipMixin}, so every JEI fluid tooltip
 * (ingredient list, Create, IE and the other categories) shows them under the fluid's name. Create potion
 * fluids are skipped: Create's own categories already list their effects.
 */
@Mod.EventBusSubscriber(modid = CreativeEngineering.MODID, value = Dist.CLIENT)
public final class DrinkFluidEffects {

    private DrinkFluidEffects() {}

    private static Map<Fluid, List<MobEffectInstance>> byFluid;

    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        byFluid = null;
    }

    public static List<Component> lines(FluidStack fs) {
        if (fs.isEmpty() || (fs.hasTag() && fs.getTag().contains("Potion"))) return List.of();
        Map<Fluid, List<MobEffectInstance>> map = byFluid;
        if (map == null) {
            map = build();
            if (map == null) return List.of();
            byFluid = map;
        }
        List<MobEffectInstance> effects = map.get(fs.getFluid());
        if (effects == null) return List.of();
        List<Component> out = new ArrayList<>();
        for (MobEffectInstance inst : effects) {
            MutableComponent c = Component.translatable(inst.getDescriptionId());
            if (inst.getAmplifier() > 0) {
                c = Component.translatable("potion.withAmplifier", c,
                        Component.translatable("potion.potency." + inst.getAmplifier()));
            }
            if (inst.getDuration() > 20) {
                c = Component.translatable("potion.withDuration", c, MobEffectUtil.formatDuration(inst, 1.0F));
            }
            out.add(c.withStyle(inst.getEffect().getCategory().getTooltipFormatting()));
        }
        return out;
    }

    private static Map<Fluid, List<MobEffectInstance>> build() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null) return null;
        Map<Fluid, List<MobEffectInstance>> map = new HashMap<>();
        Set<Fluid> ambiguous = new HashSet<>();
        for (Recipe<?> r : mc.getConnection().getRecipeManager().getRecipes()) {
            if (r.getType() != AllRecipeTypes.EMPTYING.getType() || !(r instanceof EmptyingRecipe er)) continue;
            if (er.getIngredients().isEmpty() || er.getFluidResults().isEmpty()) continue;
            FluidStack out = er.getFluidResults().get(0);
            if (out.isEmpty() || out.hasTag()) continue;
            for (ItemStack stack : er.getIngredients().get(0).getItems()) {
                FoodProperties food = stack.getFoodProperties(null);
                if (food == null || food.getEffects().isEmpty()) continue;
                List<MobEffectInstance> effects = new ArrayList<>();
                for (Pair<MobEffectInstance, Float> p : food.getEffects()) {
                    if (p.getFirst() != null) effects.add(p.getFirst());
                }
                List<MobEffectInstance> prev = map.putIfAbsent(out.getFluid(), effects);
                if (prev != null && !prev.equals(effects)) ambiguous.add(out.getFluid());
            }
        }
        ambiguous.forEach(map::remove);
        return map;
    }
}
