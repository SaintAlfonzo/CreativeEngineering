package com.SaintAlfonzo.creativedelight.food;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * One line per flavor under the food's name ({@link Flavors}): "Garlic: Speed (0:10)". Names are lang keys
 * {@code flavor.<namespace>.<path>}.
 */
public final class FlavorTooltip {

	private FlavorTooltip() {}

	@SubscribeEvent
	public static void onTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		List<ResourceLocation> ids = Flavors.of(stack);
		if (ids.isEmpty()) return;
		int at = Math.min(1, event.getToolTip().size());
		for (ResourceLocation id : ids) {
			event.getToolTip().add(at++, line(id));
		}
		for (Flavors.FlavorPair pair : Flavors.pairsOn(stack)) {
			event.getToolTip().add(at++, Component.translatable("creativedelight.flavor.pair", name(pair.a()), name(pair.b()),
					Component.translatable("creativedelight.flavor.saturation", number(pair.saturation())))
					.withStyle(ChatFormatting.GRAY));
		}
	}

	/** A flavor's display name ({@link Flavors#SALTED}: Salt). */
	private static MutableComponent name(ResourceLocation id) {
		if (Flavors.SALTED.equals(id)) {
			return Component.translatable("creativedelight.flavor.salted").withStyle(ChatFormatting.GOLD);
		}
		Flavors.Definition def = Flavors.get(id);
		String key = "flavor." + id.getNamespace() + "." + id.getPath();
		return (I18n.exists(key) || def == null || def.name().isEmpty() ? Component.translatable(key) : Component.literal(def.name()))
				.withStyle(ChatFormatting.GOLD);
	}

	/** The line a food flavored with {@code id} shows; also the flavor item's own line (Barbeque's Delight SeasoningItem, by reflection). */
	public static Component line(ResourceLocation id) {
		Flavors.Definition def = Flavors.get(id);
		// The lang key comes from Creative Delight's flavor_lang.js; without it (TODO food/seasoning #16) the line
		// showed the raw key, so the script's name stands in
		String key = "flavor." + id.getNamespace() + "." + id.getPath();
		MutableComponent name = (I18n.exists(key) || def == null || def.name().isEmpty()
				? Component.translatable(key) : Component.literal(def.name()))
				.withStyle(ChatFormatting.GOLD);
		if (def == null) {
			return name;
		}
		MutableComponent bonus = Component.empty();
		boolean first = true;
		if (def.nutrition() > 0) {
			first = append(bonus, first, Component.translatable("creativedelight.flavor.nutrition", def.nutrition()));
		}
		if (def.saturation() > 0) {
			first = append(bonus, first, Component.translatable("creativedelight.flavor.saturation", number(def.saturation())));
		}
		if (def.heal() > 0) {
			float hearts = def.heal() / 2f;
			first = append(bonus, first, hearts == 1f
					? Component.translatable("creativedelight.flavor.heal_one")
					: Component.translatable("creativedelight.flavor.heal", number(hearts)));
		}
		Flavors.Kinds k = def.kinds();
		if (k.fast()) {
			first = append(bonus, first, Component.translatable("creativedelight.flavor.fast"));
		}
		if (k.alwaysEdible()) {
			first = append(bonus, first, Component.translatable("creativedelight.flavor.always_edible"));
		}
		if (k.damage() > 0) {
			float hearts = k.damage() / 2f;
			String dmgKey = "fire".equals(k.damageType()) ? "creativedelight.flavor.damage" : "creativedelight.flavor.hurt";
			first = append(bonus, first, hearts == 1f
					? Component.translatable(dmgKey + "_one")
					: Component.translatable(dmgKey, number(hearts)));
		}
		Flavors.Transform t = k.transform();
		if (t.chance() != 1f) {
			first = append(bonus, first, Component.translatable("creativedelight.flavor.effect_chance", number(t.chance())));
		}
		if (t.duration() != 1f) {
			first = append(bonus, first, t.duration() < 1f && t.duration() > 0f
					? Component.translatable("creativedelight.flavor.effect_duration_div", number(1f / t.duration()))
					: Component.translatable("creativedelight.flavor.effect_duration", number(t.duration())));
		}
		if (t.amplifier() != 0) {
			first = append(bonus, first, Component.translatable("creativedelight.flavor.effect_level", t.amplifier()));
		}
		for (Flavors.Effect e : def.effects()) {
			MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(e.effect());
			if (effect == null) continue;
			MutableComponent text = effect.getDisplayName().copy();
			if (e.amplifier() > 0) {
				text.append(" ").append(Component.translatable("potion.potency." + e.amplifier()));
			}
			if (!effect.isInstantenous()) {
				text.append(" (" + MobEffectUtil.formatDuration(new MobEffectInstance(effect, e.duration()), 1f) + ")");
			}
			first = append(bonus, first, text);
		}
		if (first) {
			return name;
		}
		return Component.translatable("creativedelight.flavor.line", name, bonus.withStyle(ChatFormatting.GRAY))
				.withStyle(ChatFormatting.GRAY);
	}

	private static boolean append(MutableComponent to, boolean first, Component part) {
		if (!first) to.append(", ");
		to.append(part);
		return false;
	}

	private static String number(float v) {
		return v == Math.floor(v) ? String.valueOf((int) v) : String.valueOf(v);
	}
}
