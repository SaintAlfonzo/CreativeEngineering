package com.SaintAlfonzo.creativebrewing.alcohol;

import com.SaintAlfonzo.creativebrewing.CBConfig;
import com.SaintAlfonzo.creativebrewing.CreativeBrewing;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Quark's food tooltip ends with a saturation word ("Light Snack", or "Disgusting Snack" when a food effect is
 * harmful): one of six buckets of saturation x nutrition x 2, so no food value can give a word of its own. Every
 * alcoholic drink (alcohol.md 11: hunger 1, saturation 0, hidden Intoxication) lands in the lowest bad bucket with
 * rotten flesh and raw chicken. This listener runs after Quark's and replaces that word with "Intoxicating Drink" on
 * any food whose effects hold Brewin' and Chewin's Intoxication. Client only; {@code [alcohol] quarkLine}.
 */
@Mod.EventBusSubscriber(modid = CreativeBrewing.MOD_ID, value = Dist.CLIENT)
public final class IntoxicatingDrinkTooltip {
    private static final ResourceLocation INTOXICATION = new ResourceLocation("brewinandchewin", "intoxication");
    private static final String QUARK_KEY = "quark.misc.saturation";
    private static final String QUARK_BAD_KEY = "quark.misc.bad_saturation";
    private static MobEffect intoxication;
    private static boolean looked;

    private IntoxicatingDrinkTooltip() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGather(RenderTooltipEvent.GatherComponents event) {
        if (!CBConfig.ALCOHOL.quarkLine.get()) return;
        ItemStack stack = event.getItemStack();
        if (!stack.isEdible()) return;
        FoodProperties food = stack.getItem().getFoodProperties();
        if (food == null || !intoxicating(food)) return;
        List<Either<FormattedText, TooltipComponent>> elements = event.getTooltipElements();
        for (int i = 0; i < elements.size(); i++) {
            Either<FormattedText, TooltipComponent> element = elements.get(i);
            if (element.left().isEmpty()) continue;
            if (!isQuarkSaturation(element.left().get())) continue;
            elements.set(i, Either.left(Component.translatable("tooltip.creativebrewing.intoxicating_drink")
                    .withStyle(ChatFormatting.GRAY)));
            return;
        }
    }

    static boolean isQuarkSaturation(FormattedText text) {
        if (!(text instanceof Component component)) return false;
        if (!(component.getContents() instanceof TranslatableContents translatable)) return false;
        String key = translatable.getKey();
        return key.startsWith(QUARK_KEY) || key.startsWith(QUARK_BAD_KEY);
    }

    /** True when one of the food's effects is Intoxication (null-safe: the effect may be missing without Brewin'). */
    static boolean intoxicating(FoodProperties food) {
        if (!looked) {
            intoxication = ForgeRegistries.MOB_EFFECTS.getValue(INTOXICATION);
            looked = true;
        }
        if (intoxication == null) return false;
        for (Pair<MobEffectInstance, Float> pair : food.getEffects()) {
            MobEffectInstance instance = pair.getFirst();
            if (instance != null && instance.getEffect() == intoxication) return true;
        }
        return false;
    }
}
