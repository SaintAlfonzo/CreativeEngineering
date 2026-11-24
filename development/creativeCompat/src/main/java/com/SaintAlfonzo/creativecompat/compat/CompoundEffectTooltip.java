package com.SaintAlfonzo.creativecompat.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

import com.SaintAlfonzo.creativecommon.effect.CompoundEffects;
import com.SaintAlfonzo.creativecompat.CreativeCompat;
import com.mojang.datafixers.util.Pair;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Item tooltips: the parts of every compound effect the item gives ({@link CompoundEffects}) go under "When Applied:".
 * Effects come from the potion NBT (potions, tipped arrows, potion fluids in bottles), the food properties (Farmer's
 * Delight style foods and drinks, Fruits Delight jellies) and the sources other mods register with
 * {@link #addEffectSource} (a wine table). Client only, after every other tooltip handler.
 */
@Mod.EventBusSubscriber(modid = CreativeCompat.MOD_ID, value = Dist.CLIENT)
public final class CompoundEffectTooltip {

	/** Extra effect sources other mods register ({@link #addEffectSource}). */
	private static final List<Function<Item, List<MobEffectInstance>>> SOURCES = new CopyOnWriteArrayList<>();

	private CompoundEffectTooltip() {}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		List<MobEffectInstance> effects = new ArrayList<>(PotionUtils.getMobEffects(stack));
		if (stack.isEdible()) {
			FoodProperties food = stack.getItem().getFoodProperties(stack, event.getEntity());
			if (food != null) {
				for (Pair<MobEffectInstance, Float> pair : food.getEffects()) {
					if (pair.getFirst() != null) effects.add(pair.getFirst());
				}
			}
		}
		effects.addAll(extraEffects(stack.getItem()));
		if (effects.isEmpty()) return;

		boolean compound = false;
		for (MobEffectInstance instance : effects) {
			if (!CompoundEffects.parts(instance.getEffect()).isEmpty()) {
				compound = true;
				break;
			}
		}
		if (!compound) return;

		List<Component> tooltip = event.getToolTip();
		int fallback = 1;
		for (int i = 0; i < tooltip.size(); i++) {
			String line = tooltip.get(i).getString();
			for (MobEffectInstance instance : effects) {
				if (line.contains(instance.getEffect().getDisplayName().getString())) fallback = i + 1;
			}
		}
		CompoundEffects.insert(tooltip, effects, fallback);
	}

	/**
	 * Adds a source of effects an item gives that are in neither its potion NBT nor its food properties (a wine table,
	 * say). Sources are asked on the client for every tooltip; return an empty list for other items.
	 */
	public static void addEffectSource(Function<Item, List<MobEffectInstance>> source) {
		SOURCES.add(source);
	}

	private static List<MobEffectInstance> extraEffects(Item item) {
		List<MobEffectInstance> out = new ArrayList<>();
		for (Function<Item, List<MobEffectInstance>> source : SOURCES) {
			try {
				out.addAll(source.apply(item));
			} catch (RuntimeException e) {
				// a broken source drops only its own lines
			}
		}
		return out;
	}
}
