package com.SaintAlfonzo.solraspberrypie.client;

import com.SaintAlfonzo.solraspberrypie.RpConfig;
import com.SaintAlfonzo.solraspberrypie.ladder.Ladder;
import com.SaintAlfonzo.solraspberrypie.ladder.Rung;
import com.SaintAlfonzo.solraspberrypie.logic.DietEngine;
import com.SaintAlfonzo.solraspberrypie.logic.SlotActions;
import com.SaintAlfonzo.solraspberrypie.mixin.client.FoodBookScreenAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tarinoita.solsweetpotato.client.gui.BenefitsPage;
import com.tarinoita.solsweetpotato.client.gui.FoodBookScreen;
import com.tarinoita.solsweetpotato.client.gui.elements.UIElement;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Raspberry Pie's pages in Apple Pie's Diet Log (6.3, 12.4): a progress bar over a window of the ladder (the last
 * milestone reached, a short stretch before it and the next few milestones) with an icon per rung (reached in color,
 * unreached grayed, hover for the value), then the milestone list. Inserted after the diversity page; Apple Pie's own
 * benefit pages are removed. The whole book is drawn at {@link #bookScale} (G5b).
 */
public final class DietLogPages {
	static final int HEALTH = 0xFFD03030, ATTRIBUTE = 0xFF4060C0, SLOT = 0xFF20A0A0, FLAT = 0xFFC09020, UNLOCK = 0xFF9040C0, GREY = 0xFFA0A0A0;
	private static final int ROWS_PER_PAGE = 11;
	private static final int ICON = 9;
	/** Apple Pie's book page size, and how far the Diet Bonuses button reaches below the screen center. */
	public static final int BOOK_W = 186, BOOK_H = 192, BUTTON_BOTTOM = 118;

	/**
	 * The scale the book is drawn at (G5b): the configured {@code bookScale}, shrunk to fit the window with a 4 px
	 * margin, but never below 100% (or below the configured value when that is under 1.0).
	 */
	public static float bookScale(int screenWidth, int screenHeight) {
		float want = RpConfig.CLIENT_SPEC.isLoaded() ? RpConfig.CLIENT.bookScale.get().floatValue() : 1.25F;
		if (want <= 1F) return want;
		float fit = Math.min((screenWidth / 2F - 4F) / (BOOK_W / 2F), (screenHeight / 2F - 4F) / BUTTON_BOTTOM);
		return Math.min(want, Math.max(1F, fit));
	}

	/** Rebuilds the page list after Apple Pie's initPages. */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void insert(FoodBookScreen screen) {
		FoodBookScreenAccessor acc = (FoodBookScreenAccessor) (Object) screen;
		List pages = acc.solrp$pages();
		if (pages.isEmpty()) return;
		Rectangle frame = ((UIElement) pages.get(0)).frame;
		pages.removeIf(p -> p instanceof BenefitsPage);

		if (!ClientState.ready()) return;
		List<UIElement> ours = new ArrayList<>();
		ours.add(new ProgressPage(new Rectangle(frame)));
		List<Rung> rungs = ClientState.ladder.rungs;
		for (int i = 0; i < rungs.size(); i += ROWS_PER_PAGE) {
			ours.add(new ListPage(new Rectangle(frame), rungs.subList(i, Math.min(rungs.size(), i + ROWS_PER_PAGE))));
		}
		pages.addAll(Math.min(1, pages.size()), ours);
	}

	// ---- rungs -----------------------------------------------------------------------------------------------------

	static boolean reached(Rung r) {
		var s = ClientState.state;
		boolean gate = s.foodsEaten >= s.minFoods;
		return r.kind == Rung.Kind.UNLOCK ? r.threshold <= s.maxDiversity : gate && r.threshold <= s.diversity;
	}

	static int color(Rung r) {
		return switch (r.kind) {
			case HEALTH -> HEALTH;
			case ATTRIBUTE -> ATTRIBUTE;
			case SLOT -> SLOT;
			case FLAT -> FLAT;
			case UNLOCK -> UNLOCK;
		};
	}

	static Component describe(Rung r) {
		return switch (r.kind) {
			case HEALTH -> SlotActions.hearts(r.amount);
			case ATTRIBUTE -> SlotActions.attribute(r.id, r.amount);
			case SLOT -> Component.translatable("solraspberrypie.slot", r.slotIndex + 1);
			case FLAT -> Component.translatable("solraspberrypie.preview.flat", DietEngine.effectName(r.id, r.level));
			case UNLOCK -> Component.translatable("solraspberrypie.dietlog.unlock", DietEngine.effectName(r.id, r.level));
		};
	}

	private static ItemStack attributeItem(ResourceLocation id) {
		String path = id == null ? "" : id.getPath();
		if (path.endsWith("armor_toughness")) return new ItemStack(Items.NETHERITE_CHESTPLATE);
		if (path.endsWith("knockback_resistance")) return new ItemStack(Items.SHIELD);
		if (path.endsWith("movement_speed")) return new ItemStack(Items.FEATHER);
		if (path.endsWith("attack_damage")) return new ItemStack(Items.IRON_SWORD);
		if (path.endsWith("attack_speed")) return new ItemStack(Items.GOLDEN_SWORD);
		if (path.endsWith("luck")) return new ItemStack(Items.RABBIT_FOOT);
		return ItemStack.EMPTY;
	}

	/**
	 * The kind's item icon from the client config (G5d: lock, diamond shard, metal frame, with vanilla fallbacks). When
	 * none of the configured items is loaded, the old icon: the effect sprite, or for a slot an empty box.
	 */
	private static void kindIcon(PoseStack pose, List<? extends String> ids, Rung r, int x, int y) {
		ItemStack stack = RpConfig.CLIENT_SPEC.isLoaded() ? Icons.configured(ids) : ItemStack.EMPTY;
		if (!stack.isEmpty()) Icons.item(stack, x, y, ICON);
		else if (r.kind == Rung.Kind.SLOT) GuiComponent.fill(pose, x, y, x + ICON, y + ICON, 0x40000000);
		else Icons.effect(pose, r.id, x, y, ICON);
	}

	/** The rung's icon at {@code (x, y)}, {@link #ICON} pixels: kind-colored frame, grayed when not reached. */
	static void drawIcon(PoseStack pose, Rung r, int x, int y, boolean got) {
		switch (r.kind) {
			case HEALTH -> Icons.heart(pose, x, y);
			case ATTRIBUTE -> {
				ItemStack stack = attributeItem(r.id);
				if (stack.isEmpty()) Icons.armor(pose, x, y);
				else Icons.item(stack, x, y, ICON);
			}
			case SLOT -> kindIcon(pose, RpConfig.CLIENT.slotIcon.get(), r, x, y);
			case FLAT -> kindIcon(pose, RpConfig.CLIENT.flatIcon.get(), r, x, y);
			case UNLOCK -> kindIcon(pose, RpConfig.CLIENT.unlockIcon.get(), r, x, y);
		}
		Icons.frame(pose, x, y, ICON, got ? color(r) : GREY);
		if (!got) Icons.dim(pose, x, y, ICON);
	}

	/** The key (legend) rows: a sample rung per row and its name (G5d adds knockback resistance with the shield). */
	private static final Rung[] LEGEND = {
			new Rung(Rung.Kind.HEALTH, 0, null, 2, 0),
			new Rung(Rung.Kind.ATTRIBUTE, 0, new ResourceLocation("minecraft", "generic.armor_toughness"), 1, 0),
			new Rung(Rung.Kind.ATTRIBUTE, 0, new ResourceLocation("minecraft", "generic.knockback_resistance"), 1, 0),
			new Rung(Rung.Kind.SLOT, 0, null, 0, 0),
			new Rung(Rung.Kind.FLAT, 0, new ResourceLocation("minecraft", "speed"), 0, 1),
			new Rung(Rung.Kind.UNLOCK, 0, new ResourceLocation("minecraft", "haste"), 0, 1)};
	private static final String[] LEGEND_NAMES = {"health", "toughness", "knockback", "slot", "flat", "unlock"};

	/** A hover target over an icon: Apple Pie's UIElement shows its tooltip string. Draws nothing itself. */
	private static final class Hover extends UIElement {
		Hover(Rectangle frame, String tooltip) {
			super(frame);
			this.tooltip = tooltip;
		}

		@Override
		protected void render(PoseStack pose) {}
	}

	/** The bar's window over the ladder: values from {@code lo} to {@code hi}. */
	record Window(double lo, double hi, boolean before, boolean after) {
		static Window of(Ladder l, double diversity, int ahead) {
			TreeSet<Double> t = new TreeSet<>();
			for (Rung r : l.rungs) t.add(r.threshold);
			List<Double> ts = new ArrayList<>(t);
			if (ts.isEmpty()) return new Window(0, Math.max(1, diversity), false, false);
			int last = -1;
			for (int i = 0; i < ts.size(); i++) if (ts.get(i) <= diversity) last = i;
			double lo;
			if (last < 0) {
				lo = 0;
			} else {
				double prev = last > 0 ? ts.get(last - 1) : 0;
				lo = Math.max(0, ts.get(last) - (ts.get(last) - prev) * 0.35);
			}
			int far = Math.min(ts.size() - 1, last + ahead);
			double hi = ts.get(far);
			if (hi <= lo || far == last) hi = Math.max(hi, diversity) + Math.max(1, (hi - lo) * 0.15);
			return new Window(lo, hi, lo > 0, far < ts.size() - 1);
		}

		int x(double v, int x0, int x1) {
			return x0 + (int) Math.round((x1 - x0) * Math.max(0, Math.min(1, (v - lo) / (hi - lo))));
		}

		boolean contains(double v) {
			return v >= lo - 1e-9 && v <= hi + 1e-9;
		}
	}

	private static final class ProgressPage extends UIElement {
		private static final int BAR_DY = 66, LEGEND_DY = 86;
		private final Window window;
		private final List<Rung> shown = new ArrayList<>();
		private final List<int[]> spots = new ArrayList<>();

		ProgressPage(Rectangle frame) {
			super(frame);
			Ladder l = ClientState.ladder;
			var s = ClientState.state;
			window = Window.of(l, s.diversity, RpConfig.CLIENT_SPEC.isLoaded() ? RpConfig.CLIENT.progressAhead.get() : 3);
			int x0 = frame.x + 10, x1 = frame.x + frame.width - 10;
			int barY = frame.y + BAR_DY;
			Map<Integer, Integer> stack = new HashMap<>();
			for (Rung r : l.rungs) {
				if (!window.contains(r.threshold)) continue;
				int x = window.x(r.threshold, x0, x1);
				int n = stack.merge(x, 1, Integer::sum) - 1;
				int ix = x - ICON / 2, iy = barY - 3 - ICON - n * (ICON + 2);
				shown.add(r);
				spots.add(new int[]{ix, iy});
				children.add(new Hover(new Rectangle(ix, iy, ICON, ICON), Icons.num(r.threshold) + ": " + describe(r).getString()));
			}
			int ly = frame.y + LEGEND_DY;
			for (int i = 0; i < LEGEND_NAMES.length; i++) {
				children.add(new Hover(new Rectangle(frame.x + 8, ly + i * 11, ICON, ICON),
						Component.translatable("solraspberrypie.dietlog.legend." + LEGEND_NAMES[i]).getString()));
			}
		}

		@Override
		protected void render(PoseStack pose) {
			var s = ClientState.state;
			fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.ladder"), frame.x + 6, frame.y + 4, 0x000000);
			fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.now", Icons.num(Math.round(s.diversity * 10) / 10.0)), frame.x + 6, frame.y + 16, 0x333333);
			fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.max", Icons.num(Math.round(s.maxDiversity * 10) / 10.0)), frame.x + 6, frame.y + 26, 0x333333);

			int x0 = frame.x + 10, x1 = frame.x + frame.width - 10, barY = frame.y + BAR_DY;
			GuiComponent.fill(pose, x0, barY, x1, barY + 4, 0xFF8A8A8A);
			int filled = window.x(s.diversity, x0, x1);
			GuiComponent.fill(pose, x0, barY, filled, barY + 4, 0xFF3FA34D);
			if (window.contains(s.maxDiversity)) {
				int maxX = window.x(s.maxDiversity, x0, x1);
				GuiComponent.fill(pose, maxX, barY - 1, maxX + 1, barY + 5, 0xFF7A3FA3);
			} else if (s.maxDiversity > window.hi()) {
				fontRenderer.draw(pose, ">", x1 + 2, barY - 2, 0x7A3FA3);
			}
			if (window.before()) fontRenderer.draw(pose, "<", x0 - 7, barY - 2, 0x777777);
			fontRenderer.draw(pose, Icons.num(Math.round(window.lo() * 10) / 10.0), x0, barY + 7, 0x777777);
			String hi = Icons.num(Math.round(window.hi() * 10) / 10.0) + (window.after() ? " ..." : "");
			fontRenderer.draw(pose, hi, x1 - fontRenderer.width(hi), barY + 7, 0x777777);

			for (int i = 0; i < shown.size(); i++) {
				Rung r = shown.get(i);
				drawIcon(pose, r, spots.get(i)[0], spots.get(i)[1], reached(r));
			}

			int ly = frame.y + LEGEND_DY;
			for (int i = 0; i < LEGEND_NAMES.length; i++) {
				drawIcon(pose, LEGEND[i], frame.x + 8, ly + i * 11, true);
				fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.legend." + LEGEND_NAMES[i]), frame.x + 22, ly + i * 11 + 1, 0x333333);
			}
			if (s.foodsEaten < s.minFoods) {
				fontRenderer.draw(pose, Component.translatable(s.uniqueGate ? "solraspberrypie.screen.gate_unique" : "solraspberrypie.screen.gate",
						s.minFoods - s.foodsEaten), frame.x + 6, ly + LEGEND_NAMES.length * 11 + 2, 0x8A4A00);
			}
			super.render(pose);
		}
	}

	private static final class ListPage extends UIElement {
		private final List<Rung> rungs;

		ListPage(Rectangle frame, List<Rung> rungs) {
			super(frame);
			this.rungs = rungs;
		}

		@Override
		protected void render(PoseStack pose) {
			fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.milestones"), frame.x + 6, frame.y + 4, 0x000000);
			int y = frame.y + 18;
			for (Rung r : rungs) {
				boolean got = reached(r);
				drawIcon(pose, r, frame.x + 7, y, got);
				fontRenderer.draw(pose, Icons.num(r.threshold), frame.x + 20, y + 1, got ? 0x1E6B2A : 0x777777);
				String text = fontRenderer.plainSubstrByWidth(describe(r).getString(), frame.width - 46);
				fontRenderer.draw(pose, text, frame.x + 40, y + 1, got ? 0x000000 : 0x777777);
				y += 12;
			}
			super.render(pose);
		}
	}

	private DietLogPages() {}
}
