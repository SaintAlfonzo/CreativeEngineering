package com.SaintAlfonzo.solraspberrypie.client;

import com.SaintAlfonzo.solraspberrypie.RpConfig;
import com.SaintAlfonzo.solraspberrypie.data.MealLog;
import com.SaintAlfonzo.solraspberrypie.logic.Drinks;
import com.mojang.datafixers.util.Either;
import com.tarinoita.solsweetpotato.SOLSweetPotatoConfig;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Food tooltips (plan_diet_books_r2.md A9, A11). Raspberry Pie owns the order of the food rows, as the last
 * GatherComponents listener: the item name, AppleSkin's hunger and saturation row, the diversity line
 * ("Diversity: 1.40", plus "(contributes 0.85)" while the food is in the diet window), Quark's saturation word
 * ("Hearty Meal"), then everything else. Quark's own shank row is off by its config ("Food Tooltips" = false, "Show
 * Saturation" = true). AppleSkin's saturation shares the shank row unless {@code [tooltips] saturationOwnLine}
 * (AppleSkinTooltipRendererMixin). Apple Pie's own "Food Diversity: X" / "Diversity Contribution: X" line at the
 * bottom is removed (the diversity line covers it); its "Last eaten" line stays.
 */
public final class FoodTooltips {
	private static final String APPLESKIN = "squeek.appleskin.client.TooltipOverlayHandler$FoodTooltip";

	public static void gather(RenderTooltipEvent.GatherComponents e) {
		List<Either<FormattedText, TooltipComponent>> list = e.getTooltipElements();
		if (list.isEmpty()) return;
		dropApplePieDiversity(list);
		Component diversity = diversityLine(e.getItemStack());
		Either<FormattedText, TooltipComponent> apple = null, word = null;
		for (int i = list.size() - 1; i >= 1; i--) {
			Either<FormattedText, TooltipComponent> el = list.get(i);
			if (apple == null && el.right().isPresent() && APPLESKIN.equals(el.right().get().getClass().getName())) {
				apple = list.remove(i);
			} else if (word == null && el.left().isPresent() && isQuarkWord(el.left().get())) {
				word = list.remove(i);
			}
		}
		if (apple == null && word == null && diversity == null) return;
		int at = afterTitle(list);
		if (apple != null) list.add(Math.min(at++, list.size()), apple);
		if (diversity != null) list.add(Math.min(at++, list.size()), Either.left(diversity));
		if (word != null) list.add(Math.min(at, list.size()), word);
	}

	private static final String TITLE_BREAK = "$TitleBreakComponent";

	/**
	 * Where the food rows go: right after the name, or after Iceberg's title break when it is already in the title
	 * area (Legendary Tooltips: name, or model + name + spacer, then the break). Iceberg's handler and ours are both
	 * LOWEST, so which runs first follows listener registration and changes between launches. Rows placed above the
	 * break become title lines; Legendary Tooltips then draws an empty zero-size frame for the whole tooltip.
	 */
	private static int afterTitle(List<Either<FormattedText, TooltipComponent>> list) {
		for (int i = 1; i < Math.min(list.size(), 4); i++) {
			var right = list.get(i).right();
			if (right.isPresent() && right.get().getClass().getName().endsWith(TITLE_BREAK)) return i + 1;
		}
		return 1;
	}

	private static final String AP_LABELS ="gui.solapplepie.food_book.queue.tooltip.";

	/** Apple Pie's TooltipHandler line: its label (uneaten or contribution) followed by the value. */
	private static void dropApplePieDiversity(List<Either<FormattedText, TooltipComponent>> list) {
		String uneaten = I18n.get(AP_LABELS + "uneaten_contribution_label");
		String contribution = I18n.get(AP_LABELS + "contribution_label");
		for (int i = list.size() - 1; i >= 1; i--) {
			var left = list.get(i).left();
			if (left.isEmpty()) continue;
			String text = left.get().getString();
			if (text.startsWith(uneaten) || text.startsWith(contribution)) list.remove(i);
		}
	}

	private static boolean isQuarkWord(FormattedText text) {
		if (!(text instanceof Component c) || !(c.getContents() instanceof TranslatableContents t)) return false;
		String key = t.getKey();
		return key.startsWith("quark.misc.saturation") || key.startsWith("quark.misc.bad_saturation");
	}

	/** "Diversity: X" for a food Apple Pie counts, "(contributes Y)" added while it is in the diet window. */
	static Component diversityLine(ItemStack stack) {
		if (stack.isEmpty()) return null;
		Item item = stack.getItem();
		if (!item.isEdible() || Drinks.isOutOfDiet(item)) return null;
		try {
			if (!SOLSweetPotatoConfig.shouldCount(item)) return null;
			FoodInstance food = new FoodInstance(item);
			double base = FoodList.getComplexity(food);
			if (base < 0) return null;
			var player = Minecraft.getInstance().player;
			int k = player == null ? -1 : FoodList.get(player).getLastEaten(item);
			if (k >= 0 && k < MealLog.length()) {
				double contributes = FoodList.calculateDiversityContribution(food, k);
				return Component.translatable("solraspberrypie.tooltip.diversity_contributes", Icons.diversity(base), Icons.diversity(contributes))
						.withStyle(ChatFormatting.GRAY);
			}
			return Component.translatable("solraspberrypie.tooltip.diversity", Icons.diversity(base)).withStyle(ChatFormatting.GRAY);
		} catch (RuntimeException ex) {
			return null;
		}
	}

	/**
	 * A gray description line under the name of Apple Pie's Diet Log and Carrot Edition's Food Dictionary,
	 * like the Liquid Diet Log's own line. Lang keys {@code solraspberrypie.tooltip.book.<namespace>}.
	 */
	public static void bookLine(ItemTooltipEvent e) {
		var id = ForgeRegistries.ITEMS.getKey(e.getItemStack().getItem());
		if (id == null || !"food_book".equals(id.getPath())) return;
		if (!"solapplepie".equals(id.getNamespace()) && !"solcarrot".equals(id.getNamespace())) return;
		List<Component> list = e.getToolTip();
		list.add(Math.min(1, list.size()), Component.translatable("solraspberrypie.tooltip.book." + id.getNamespace()).withStyle(ChatFormatting.GRAY));
	}

	public static boolean saturationOwnLine() {
		return RpConfig.CLIENT_SPEC.isLoaded() && RpConfig.CLIENT.saturationOwnLine.get();
	}

	private FoodTooltips() {}
}
