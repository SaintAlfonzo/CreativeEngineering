package com.SaintAlfonzo.solraspberrypie.client;

import com.SaintAlfonzo.solraspberrypie.RpConfig;
import com.SaintAlfonzo.solraspberrypie.SolRaspberryPie;
import com.SaintAlfonzo.solraspberrypie.logic.Drinks;
import com.SaintAlfonzo.solraspberrypie.data.MealLog;
import com.SaintAlfonzo.solraspberrypie.ladder.Ladder;
import com.SaintAlfonzo.solraspberrypie.ladder.Rung;
import com.SaintAlfonzo.solraspberrypie.logic.DietEngine;
import com.SaintAlfonzo.solraspberrypie.logic.SlotActions;
import com.SaintAlfonzo.solraspberrypie.mixin.client.FoodBookScreenAccessor;
import com.SaintAlfonzo.solraspberrypie.net.S2CState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tarinoita.solsweetpotato.client.gui.BenefitsPage;
import com.tarinoita.solsweetpotato.client.gui.FoodBookScreen;
import com.tarinoita.solsweetpotato.client.gui.FoodListPage;
import com.tarinoita.solsweetpotato.client.gui.elements.UIElement;
import com.tarinoita.solsweetpotato.client.gui.elements.UIImage;
import com.tarinoita.solsweetpotato.tracking.FoodInstance;
import com.tarinoita.solsweetpotato.tracking.FoodList;
import com.SaintAlfonzo.solraspberrypie.logic.FoodWindow;
import net.minecraftforge.fml.ModList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Raspberry Pie's pages in Apple Pie's Diet Log (6.3, 12.4, plan_diet_books.md phase 1): the Diet Ladder (an
 * even-spaced bar over a window of five milestones with an icon per rung, then the key) and the Milestones list; the
 * last meals grid and the diversity food icon on Apple Pie's Overview page. Apple Pie's own benefit and food history
 * pages are removed. The whole book is drawn at
 * {@link #bookScale} (G5b); each page type's content at its own position and scale ({@link Layout}, client config
 * {@code [dietLog.pages]}).
 */
public final class DietLogPages {
	static final int HEALTH = 0xFFD03030, ATTRIBUTE = 0xFF4060C0, SLOT = 0xFF20A0A0, FLAT = 0xFFC09020, UNLOCK = 0xFF9040C0, GREY = 0xFFA0A0A0;
	private static final int ROWS_PER_PAGE = 11;
	private static final int ICON = 9;
	/** Bar window: the last milestone reached and the next four, or the first five (plan_diet_books.md 1.7). */
	private static final int WINDOW = 5;
	private static final int MEAL_COLS = 8;
	/** Apple Pie's book page size, and how far the Diet Bonuses button reaches below the screen center. */
	public static final int BOOK_W = 186, BOOK_H = 192, BUTTON_BOTTOM = 118;

	/**
	 * The scale the book is drawn at (G5b): the configured {@code bookScale}, shrunk to fit the window with a 4 px
	 * margin, but never below 100% (or below the configured value when that is under 1.0). The width includes
	 * {@link #extraWidth}.
	 */
	public static float bookScale(int screenWidth, int screenHeight) {
		float want = RpConfig.CLIENT_SPEC.isLoaded() ? RpConfig.CLIENT.bookScale.get().floatValue() : 1.4F;
		if (want <= 1F) return want;
		float fit = Math.min((screenWidth / 2F - 4F) / ((BOOK_W + extraWidth()) / 2F), (screenHeight / 2F - 4F) / BUTTON_BOTTOM);
		return Math.min(want, Math.max(1F, fit));
	}

	/** Pixels the book background is stretched by (client config {@code [dietLog] extraWidth}). */
	public static int extraWidth() {
		return RpConfig.CLIENT_SPEC.isLoaded() ? RpConfig.CLIENT.extraWidth.get() : 0;
	}

	// ---- page layouts ----------------------------------------------------------------------------------------------

	/** A page type's content offset (book pixels) and scale (relative to the book). */
	public record Layout(int x, int y, float scale) {
		public static Layout diversity() {
			return of(RpConfig.CLIENT_SPEC.isLoaded() ? RpConfig.CLIENT.diversityPage : null, 0, 1F);
		}

		static Layout meals() {
			return of(RpConfig.CLIENT_SPEC.isLoaded() ? RpConfig.CLIENT.mealsPage : null, 0, 0.85F);
		}

		static Layout ladder() {
			return of(RpConfig.CLIENT_SPEC.isLoaded() ? RpConfig.CLIENT.ladderPage : null, 10, 0.85F);
		}

		static Layout milestones() {
			return of(RpConfig.CLIENT_SPEC.isLoaded() ? RpConfig.CLIENT.milestonesPage : null, 10, 0.85F);
		}

		private static Layout of(RpConfig.PageLayout c, int y, float scale) {
			return c == null ? new Layout(0, y, scale) : new Layout(c.x.get(), c.y.get(), c.scale.get().floatValue());
		}

		/** The page frame laid out at {@code 1 / scale} size, so content drawn at the scale fills the page. */
		Rectangle virtual(Rectangle f) {
			return new Rectangle(f.x, f.y, Math.round(f.width / scale), Math.round(f.height / scale));
		}

		/** Maps a rectangle laid out in the virtual frame to its screen position. */
		Rectangle mapped(Rectangle page, int x, int y, int w, int h) {
			return new Rectangle(page.x + this.x + Math.round((x - page.x) * scale), page.y + this.y + Math.round((y - page.y) * scale),
					Math.max(1, Math.round(w * scale)), Math.max(1, Math.round(h * scale)));
		}

		/** Starts drawing the page's content: offset, then scaled around the page's top left corner. */
		void begin(Rectangle page) {
			begin(page.x, page.y);
		}

		/** Same around a given pivot (Apple Pie's diversity page scales around its top center, its content is centered). */
		public void begin(int pivotX, int pivotY) {
			PoseStack mv = RenderSystem.getModelViewStack();
			mv.pushPose();
			mv.translate(pivotX + x, pivotY + y, 0);
			mv.scale(scale, scale, 1);
			mv.translate(-pivotX, -pivotY, 0);
			RenderSystem.applyModelViewMatrix();
		}

		/** A screen point mapped back into the content's coordinates (for hovers under {@link #begin(int, int)}). */
		public int unmapX(int sx, int pivotX) {
			return (int) Math.floor(pivotX + (sx - pivotX - x) / scale);
		}

		public int unmapY(int sy, int pivotY) {
			return (int) Math.floor(pivotY + (sy - pivotY - y) / scale);
		}

		public static void end() {
			RenderSystem.getModelViewStack().popPose();
			RenderSystem.applyModelViewMatrix();
		}
	}

	/**
	 * Rebuilds the page list after Apple Pie's initPages: diversity (with the last meals grid, {@link #mealsGrid}), Diet
	 * Ladder, Milestones (plan_owner_notes_1119.md ruling 8: the meals pages are gone).
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void insert(FoodBookScreen screen) {
		FoodBookScreenAccessor acc = (FoodBookScreenAccessor) (Object) screen;
		List pages = acc.solrp$pages();
		if (pages.isEmpty()) return;
		Rectangle frame = ((UIElement) pages.get(0)).frame;
		pages.removeIf(p -> p instanceof BenefitsPage || p instanceof FoodListPage);

		List<UIElement> ours = new ArrayList<>();
		if (ClientState.ready()) {
			Layout ladder = Layout.ladder(), list = Layout.milestones();
			ours.add(new ProgressPage(ladder.virtual(frame), ladder));
			List<Rung> rungs = ClientState.ladder.rungs;
			for (int i = 0; i < rungs.size(); i += ROWS_PER_PAGE) {
				ours.add(new ListPage(list.virtual(frame), list, rungs.subList(i, Math.min(rungs.size(), i + ROWS_PER_PAGE))));
			}
		}
		pages.addAll(Math.min(1, pages.size()), ours);
	}

	private static List<ResourceLocation> mealLog() {
		var player = Minecraft.getInstance().player;
		if (player == null) return List.of();
		try {
			return MealLog.of(FoodList.get(player));
		} catch (RuntimeException e) {
			return List.of();
		}
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

	/** Hover and list text of a rung. A slot rung names only its slot (plan_diet_books.md ruling 14, no effect). */
	static Component describe(Rung r) {
		return switch (r.kind) {
			case HEALTH -> SlotActions.hearts(r.amount);
			case ATTRIBUTE -> SlotActions.attribute(r.id, r.amount);
			case SLOT -> Component.translatable("solraspberrypie.slot", r.slotIndex + 1);
			case FLAT -> Component.translatable("solraspberrypie.preview.flat", DietEngine.effectName(r.id, r.level));
			case UNLOCK -> Component.translatable("solraspberrypie.dietlog.unlock", DietEngine.effectName(r.id, r.level));
		};
	}

	/** The gate line: "N more unique food(s) to activate diet" (plan_diet_books.md 1.9), or Apple Pie's count wording. */
	public static Component gateText(S2CState s) {
		int n = s.minFoods - s.foodsEaten;
		if (!s.uniqueGate) return Component.translatable("solraspberrypie.screen.gate", n);
		return Component.translatable(n == 1 ? "solraspberrypie.screen.gate_unique_one" : "solraspberrypie.screen.gate_unique", n);
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

	/** How long a locked one-time unlock shows the lock, then the effect (plan_diet_books.md ruling 5). */
	private static final long ALTERNATE_MS = 1000;

	private static boolean effectLoaded(ResourceLocation id) {
		return id != null && ForgeRegistries.MOB_EFFECTS.containsKey(id);
	}

	/**
	 * The rung's icon at {@code (x, y)}, {@link #ICON} pixels, on the bar and the Milestones list (plan_diet_books.md
	 * 1.8). A slot rung is the slot item; a locked one-time unlock alternates the lock item and the effect sprite each
	 * second in a gray frame; an unlocked one shows the effect sprite grayed in the unlock color's frame, whatever the
	 * current diversity. Every other rung: kind-colored frame, grayed when not reached.
	 */
	static void drawIcon(PoseStack pose, Rung r, int x, int y, boolean got) {
		if (r.kind == Rung.Kind.UNLOCK && effectLoaded(r.id)) {
			if (got) {
				Icons.effect(pose, r.id, x, y, ICON);
				Icons.dimInner(pose, x, y, ICON);
				Icons.frame(pose, x, y, ICON, UNLOCK);
				return;
			}
			if ((System.currentTimeMillis() / ALTERNATE_MS) % 2 == 1) Icons.effect(pose, r.id, x, y, ICON);
			else kindIcon(pose, RpConfig.CLIENT.unlockIcon.get(), r, x, y);
			Icons.frame(pose, x, y, ICON, GREY);
			Icons.dim(pose, x, y, ICON);
			return;
		}
		drawKindIcon(pose, r, x, y);
		Icons.frame(pose, x, y, ICON, got ? color(r) : GREY);
		if (!got) Icons.dim(pose, x, y, ICON);
	}

	/** The kind's plain icon, as the key draws it. */
	private static void drawKindIcon(PoseStack pose, Rung r, int x, int y) {
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

	/**
	 * The bar's window over the ladder (plan_diet_books.md 1.7): up to five distinct milestone values, evenly spaced.
	 * With a milestone reached, the window starts at the last one reached (at the top of the ladder, the last five);
	 * with none, it shows the first five after a 0 origin at the left end. The fill moves proportionally between
	 * neighboring points.
	 */
	record Window(List<Double> values, boolean origin, boolean before, boolean after) {
		static Window of(List<Rung> rungs, double diversity) {
			TreeSet<Double> t = new TreeSet<>();
			for (Rung r : rungs) t.add(r.threshold);
			List<Double> ts = new ArrayList<>(t);
			if (ts.isEmpty()) return new Window(List.of(), true, false, false);
			int last = -1;
			for (int i = 0; i < ts.size(); i++) if (ts.get(i) <= diversity) last = i;
			int start = Math.max(0, last), end = Math.min(ts.size(), start + WINDOW);
			start = Math.max(0, Math.min(start, end - WINDOW));
			boolean origin = last < 0 || end - start == 1;
			return new Window(ts.subList(start, end), origin, start > 0, end < ts.size());
		}

		/** Screen x of point {@code i} (0 = first milestone in the window). */
		int pointX(int i, int x0, int x1) {
			int n = values.size();
			if (n == 0) return x0;
			return origin ? x0 + Math.round((x1 - x0) * (i + 1) / (float) n) : x0 + Math.round((x1 - x0) * i / (float) (n - 1));
		}

		double lo() {
			return origin || values.isEmpty() ? 0 : values.get(0);
		}

		double hi() {
			return values.isEmpty() ? 0 : values.get(values.size() - 1);
		}

		/** Screen x of a value: piecewise linear between the points (and the origin). */
		int x(double v, int x0, int x1) {
			if (values.isEmpty()) return x0;
			double prevV = origin ? 0 : values.get(0);
			int prevX = origin ? x0 : pointX(0, x0, x1);
			if (v <= prevV) return prevX;
			for (int i = origin ? 0 : 1; i < values.size(); i++) {
				double nv = values.get(i);
				int nx = pointX(i, x0, x1);
				if (v <= nv) return prevX + (int) Math.round((nx - prevX) * (v - prevV) / Math.max(1e-9, nv - prevV));
				prevV = nv;
				prevX = nx;
			}
			return prevX;
		}

		boolean contains(double v) {
			return v >= lo() - 1e-9 && v <= hi() + 1e-9;
		}

		int indexOf(double threshold) {
			for (int i = 0; i < values.size(); i++) if (Math.abs(values.get(i) - threshold) < 1e-9) return i;
			return -1;
		}
	}

	private static final class ProgressPage extends UIElement {
		private static final int BAR_DY = 66, LEGEND_DY = 86;
		private final Layout layout;
		private final Window window;
		private final List<Rung> shown = new ArrayList<>();
		private final List<int[]> spots = new ArrayList<>();

		ProgressPage(Rectangle frame, Layout layout) {
			super(frame);
			this.layout = layout;
			Ladder l = ClientState.ladder;
			var s = ClientState.state;
			List<Rung> bar = barRungs(l);
			window = Window.of(bar, s.diversity);
			int x0 = frame.x + 10, x1 = frame.x + frame.width - 10;
			int barY = frame.y + BAR_DY;
			Map<Integer, Integer> stack = new HashMap<>();
			for (Rung r : bar) {
				int idx = window.indexOf(r.threshold);
				if (idx < 0) continue;
				int x = window.pointX(idx, x0, x1);
				int n = stack.merge(x, 1, Integer::sum) - 1;
				int ix = x - ICON / 2, iy = barY - 3 - ICON - n * (ICON + 2);
				shown.add(r);
				spots.add(new int[]{ix, iy});
				children.add(new Hover(layout.mapped(frame, ix, iy, ICON, ICON), Icons.num(r.threshold) + ": " + describe(r).getString()));
			}
			int ly = frame.y + LEGEND_DY;
			for (int i = 0; i < LEGEND_NAMES.length; i++) {
				children.add(new Hover(layout.mapped(frame, frame.x + 8, ly + i * 11, ICON, ICON),
						Component.translatable("solraspberrypie.dietlog.legend." + LEGEND_NAMES[i]).getString()));
			}
		}

		@Override
		protected void render(PoseStack pose) {
			layout.begin(frame);
			var s = ClientState.state;
			fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.ladder"), frame.x + 6, frame.y + 4, 0x000000);
			fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.now", Icons.diversity(s.diversity)), frame.x + 6, frame.y + 16, 0x333333);
			fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.max", Icons.diversity(s.maxDiversity)), frame.x + 6, frame.y + 26, 0x333333);

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
			if (window.after()) fontRenderer.draw(pose, "...", x1 - fontRenderer.width("...") / 2, barY + 16, 0x777777);
			if (window.origin()) fontRenderer.draw(pose, "0", x0, barY + 7, 0x777777);
			for (int i = 0; i < window.values().size(); i++) {
				String label = Icons.num(window.values().get(i));
				int px = window.pointX(i, x0, x1);
				int lx = Math.min(x1 - fontRenderer.width(label), Math.max(x0, px - fontRenderer.width(label) / 2));
				fontRenderer.draw(pose, label, lx, barY + 7, 0x777777);
			}

			for (int i = 0; i < shown.size(); i++) {
				Rung r = shown.get(i);
				drawIcon(pose, r, spots.get(i)[0], spots.get(i)[1], reached(r));
			}

			int ly = frame.y + LEGEND_DY;
			for (int i = 0; i < LEGEND_NAMES.length; i++) {
				drawKindIcon(pose, LEGEND[i], frame.x + 8, ly + i * 11);
				Icons.frame(pose, frame.x + 8, ly + i * 11, ICON, color(LEGEND[i]));
				fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.legend." + LEGEND_NAMES[i]), frame.x + 22, ly + i * 11 + 1, 0x333333);
			}
			if (s.foodsEaten < s.minFoods) {
				fontRenderer.draw(pose, gateText(s), frame.x + 6, ly + LEGEND_NAMES.length * 11 + 2, 0x8A4A00);
			}
			Layout.end();
			super.render(pose);
		}
	}

	private static Component styled(String text, boolean struck) {
		MutableComponent c = Component.literal(text);
		return struck ? c.withStyle(ChatFormatting.STRIKETHROUGH) : c;
	}

	/** The Diet Ladder bar leaves out reached one-time unlocks (plan_owner_notes_1119.md ruling 11). */
	static List<Rung> barRungs(Ladder l) {
		List<Rung> out = new ArrayList<>();
		for (Rung r : l.rungs) if (!(r.kind == Rung.Kind.UNLOCK && reached(r))) out.add(r);
		return out;
	}

	private static final class ListPage extends UIElement {
		private final Layout layout;
		private final List<Rung> rungs;

		ListPage(Rectangle frame, Layout layout, List<Rung> rungs) {
			super(frame);
			this.layout = layout;
			this.rungs = rungs;
		}

		@Override
		protected void render(PoseStack pose) {
			layout.begin(frame);
			fontRenderer.draw(pose, Component.translatable("solraspberrypie.dietlog.milestones"), frame.x + 6, frame.y + 4, 0x000000);
			int y = frame.y + 18;
			for (Rung r : rungs) {
				boolean got = reached(r);
				drawIcon(pose, r, frame.x + 7, y, got);
				// A reached one-time unlock is struck through; its color follows the current diversity
				// (plan_owner_notes_1119.md ruling 6). Every other row: black reached, gray not.
				boolean struck = r.kind == Rung.Kind.UNLOCK && got;
				boolean dark = struck ? r.threshold <= ClientState.state.diversity : got;
				String text = fontRenderer.plainSubstrByWidth(describe(r).getString(), frame.width - 46);
				fontRenderer.draw(pose, styled(Icons.num(r.threshold), struck), frame.x + 20, y + 1, dark ? 0x1E6B2A : 0x777777);
				fontRenderer.draw(pose, styled(text, struck), frame.x + 40, y + 1, dark ? 0x000000 : 0x777777);
				y += 12;
			}
			Layout.end();
			super.render(pose);
		}
	}

	// ---- Overview page: last meals grid and the diversity food icon (plan_owner_notes_1119.md rulings 7 to 10, 12) --

	private static final int MEAL_ROWS = 4, GRID_STEP = 16, GRID_ICON = 15, GRID_TITLE = 11;

	/** A meal on the grid: the item and its age in meals (0 = newest), which is Apple Pie's memory count for it. */
	private record Meal(Item item, ResourceLocation id, int age) {}

	/**
	 * The last meals grid on Apple Pie's Overview page, in place of its top spacer (DiversityPageMixin): a "Last 32
	 * Meals" title centered over an 8 x 4 icon grid (plan_diet_books_r2.md A1, A2), newest at the top left, drawn at the
	 * {@code [dietLog.pages.meals]} offset and scale. A meal shows only while its food is in the decay window, so foods
	 * a Change or Drop aged out leave the grid (ruling 9). Hover: "N. Name", the food's diversity contribution only when
	 * it differs from this meal's, then this meal's (struck through when the food's is shown).
	 */
	public static UIElement mealsGrid() {
		long t0 = System.nanoTime();
		try {
			return new MealsGrid(Layout.meals());
		} finally {
			OpenTiming.grid = System.nanoTime() - t0;
		}
	}

	/** The meals shown: the log, newest first, without meals whose food is out of Apple Pie's decay window. */
	private static List<Meal> shownMeals(Map<FoodInstance, Integer> memory, int end) {
		List<ResourceLocation> log = mealLog();
		List<Meal> out = new ArrayList<>();
		for (int i = 0; i < log.size() && out.size() < MEAL_COLS * MEAL_ROWS; i++) {
			Item item = ForgeRegistries.ITEMS.getValue(log.get(i));
			if (item == null || item == Items.AIR) continue;
			if (memory != null) {
				Integer k = memory.get(new FoodInstance(item));
				if (k == null || k >= end) continue;
			}
			out.add(new Meal(item, log.get(i), i));
		}
		return out;
	}

	private static Map<FoodInstance, Integer> clientMemory() {
		var player = Minecraft.getInstance().player;
		if (player == null) return null;
		try {
			return FoodWindow.copy(FoodList.get(player));
		} catch (RuntimeException e) {
			return null;
		}
	}

	private static double contribution(Item item, int age) {
		try {
			return FoodList.calculateDiversityContribution(new FoodInstance(item), age);
		} catch (RuntimeException e) {
			return 0;
		}
	}

	private static final class MealsGrid extends UIElement {
		private final Layout layout;
		private final List<Meal> meals;
		private final List<UIElement> cells = new ArrayList<>();

		MealsGrid(Layout layout) {
			super(new Rectangle(0, 0, Math.round(MEAL_COLS * GRID_STEP * layout.scale()),
					Math.round((GRID_TITLE + MEAL_ROWS * GRID_STEP) * layout.scale())));
			this.layout = layout;
			Map<FoodInstance, Integer> memory = clientMemory();
			int end = MealLog.length();
			meals = shownMeals(memory, end);
			for (int n = 0; n < meals.size(); n++) {
				Meal m = meals.get(n);
				ItemStack stack = new ItemStack(m.item());
				double mine = contribution(m.item(), m.age());
				Integer k = memory == null ? null : memory.get(new FoodInstance(m.item()));
				double total = k == null ? mine : contribution(m.item(), k);
				// plan_diet_books_r2.md A2: the food total only when it differs from this meal, then this meal
				// (struck through when the total is there).
				boolean differs = !Icons.diversity(mine).equals(Icons.diversity(total));
				List<Component> lines = new ArrayList<>();
				lines.add(Component.translatable("solraspberrypie.dietlog.meal", n + 1, stack.getHoverName()));
				if (differs) lines.add(Component.translatable("solraspberrypie.dietlog.meal_total", Icons.diversity(total)).withStyle(ChatFormatting.GRAY));
				MutableComponent self = Component.translatable("solraspberrypie.dietlog.meal_this", Icons.diversity(mine)).withStyle(ChatFormatting.GRAY);
				lines.add(differs ? self.withStyle(ChatFormatting.STRIKETHROUGH) : self);
				UIElement cell = new MealCell(stack, lines);
				cells.add(cell);
				children.add(cell);
			}
		}

		@Override
		protected void render(PoseStack pose) {
			float s = layout.scale();
			int px = frame.x + layout.x(), py = frame.y + layout.y();
			for (int n = 0; n < cells.size(); n++) {
				int lx = (n % MEAL_COLS) * GRID_STEP, ly = GRID_TITLE + (n / MEAL_COLS) * GRID_STEP;
				cells.get(n).frame = new Rectangle(px + Math.round(lx * s), py + Math.round(ly * s),
						Math.max(1, Math.round(GRID_ICON * s)), Math.max(1, Math.round(GRID_ICON * s)));
			}
			layout.begin(frame.x, frame.y);
			// Labels centered over the grid (plan_diet_books_r2.md A2).
			int gridW = MEAL_COLS * GRID_STEP - (GRID_STEP - GRID_ICON);
			Component title = Component.translatable("solraspberrypie.dietlog.meals", MealLog.length());
			fontRenderer.draw(pose, title, frame.x + (gridW - fontRenderer.width(title)) / 2F, frame.y, 0x000000);
			if (meals.isEmpty()) {
				Component none = Component.translatable("solraspberrypie.dietlog.meals_none");
				fontRenderer.draw(pose, none, frame.x + (gridW - fontRenderer.width(none)) / 2F, frame.y + GRID_TITLE + 2, 0x777777);
			}
			for (int n = 0; n < meals.size(); n++) {
				int x = frame.x + (n % MEAL_COLS) * GRID_STEP, y = frame.y + GRID_TITLE + (n / MEAL_COLS) * GRID_STEP;
				Icons.item(new ItemStack(meals.get(n).item()), x, y, GRID_ICON);
			}
			Layout.end();
			super.render(pose);
		}
	}

	/** A meal's hover target: an item tooltip with the contribution lines. Draws nothing itself. */
	private static final class MealCell extends UIElement {
		private final ItemStack stack;
		private final List<Component> lines;

		MealCell(ItemStack stack, List<Component> lines) {
			super(new Rectangle(0, 0, 1, 1));
			this.stack = stack;
			this.lines = lines;
		}

		@Override
		protected boolean hasTooltip() {
			return true;
		}

		@Override
		protected void renderTooltip(PoseStack pose, int mouseX, int mouseY) {
			renderTooltip(pose, stack, lines, mouseX, mouseY);
		}

		@Override
		protected void render(PoseStack pose) {}
	}

	/**
	 * The icon left of "Current Food Diversity" (plan_diet_books_r2.md A7): the average base diversity value (Apple Pie
	 * complexity) of the meals in the diet window, repeats counted, and the eaten food (Spice of Life: Carrot's list)
	 * that is not a drink whose base value is closest to it; on a tie the one eaten most recently. With no meal in the
	 * window, nothing eaten or no Carrot: one of the {@code [dietLog] diversityIconFallback} items, stable while the
	 * diversity (to 0.1) is unchanged. Returns null when nothing fits (Apple Pie's own image stays).
	 */
	public static UIImage diversityIcon(UIImage.Image fallback) {
		long t0 = System.nanoTime();
		try {
			Item item = closestEaten();
			if (item == null) item = fallbackItem(currentDiversity());
			return item == null ? null : new FoodIcon(fallback, new ItemStack(item));
		} finally {
			OpenTiming.icon = System.nanoTime() - t0;
		}
	}

	private static Item closestEaten() {
		if (!ModList.get().isLoaded("solcarrot")) return null;
		Map<FoodInstance, Integer> memory = clientMemory();
		List<Meal> window = shownMeals(memory, MealLog.length());
		if (window.isEmpty()) return null;
		Map<Item, Double> base = new HashMap<>();
		double sum = 0;
		for (Meal m : window) sum += base.computeIfAbsent(m.item(), DietLogPages::complexity);
		double avg = sum / window.size();
		Set<Item> eaten;
		try {
			eaten = CarrotEaten.items();
		} catch (RuntimeException | LinkageError e) {
			return null;
		}
		Item best = null;
		double bestGap = Double.MAX_VALUE;
		int bestAge = Integer.MAX_VALUE;
		String bestId = "";
		for (Item item : eaten) {
			if (Drinks.isDrink(item)) continue;
			double c = base.computeIfAbsent(item, DietLogPages::complexity);
			if (c < 0) continue;
			double gap = Math.abs(c - avg);
			Integer k = memory == null ? null : memory.get(new FoodInstance(item));
			int age = k == null ? Integer.MAX_VALUE : k;
			String id = String.valueOf(ForgeRegistries.ITEMS.getKey(item));
			boolean better = gap < bestGap - 1e-9
					|| (Math.abs(gap - bestGap) <= 1e-9 && (age < bestAge || (age == bestAge && id.compareTo(bestId) < 0)));
			if (better) {
				best = item;
				bestGap = gap;
				bestAge = age;
				bestId = id;
			}
		}
		return best;
	}

	private static double currentDiversity() {
		var player = Minecraft.getInstance().player;
		try {
			if (player != null) return FoodList.get(player).foodDiversity();
		} catch (RuntimeException ignored) {
		}
		return ClientState.state == null ? 0 : ClientState.state.diversity;
	}

	private static double complexity(Item item) {
		try {
			return FoodList.getComplexity(new FoodInstance(item));
		} catch (RuntimeException e) {
			return -1;
		}
	}

	/** One loaded item of the configured fallback list, picked by the diversity (to 0.1). */
	private static Item fallbackItem(double div) {
		if (!RpConfig.CLIENT_SPEC.isLoaded()) return null;
		List<Item> items = new ArrayList<>();
		for (String id : RpConfig.CLIENT.diversityIconFallback.get()) {
			ResourceLocation rl = ResourceLocation.tryParse(id.trim());
			if (rl != null && ForgeRegistries.ITEMS.containsKey(rl)) items.add(ForgeRegistries.ITEMS.getValue(rl));
		}
		if (items.isEmpty()) return null;
		int seed = (int) Math.floor(div * 10);
		return items.get(Math.floorMod(seed * 0x9E3779B1, items.size()));
	}

	/** Spice of Life: Carrot's eaten foods; only loaded with Carrot present. */
	private static final class CarrotEaten {
		static Set<Item> items() {
			var player = Minecraft.getInstance().player;
			Set<Item> out = new HashSet<>();
			if (player == null) return out;
			for (com.cazsius.solcarrot.tracking.FoodInstance f : com.cazsius.solcarrot.tracking.FoodList.get(player).getEatenFoods()) {
				if (f.item != null) out.add(f.item);
			}
			return out;
		}
	}

	/**
	 * Timing of one Diet Log open (plan_diet_books_r2.md A4): the page build, its diversity icon and meals grid, and
	 * the first frame. One line per open in latest.log, so the owner's log shows what freezes.
	 */
	public static final class OpenTiming {
		static long icon, grid;
		private static long pagesStart, pagesTook;
		private static boolean pendingFrame;

		public static void pagesBegin() {
			icon = 0;
			grid = 0;
			pagesStart = System.nanoTime();
		}

		public static void pagesEnd() {
			pagesTook = System.nanoTime() - pagesStart;
			pendingFrame = true;
		}

		public static boolean frameWanted() {
			return pendingFrame;
		}

		public static void frameEnd(long took) {
			if (!pendingFrame) return;
			pendingFrame = false;
			SolRaspberryPie.LOG.info("Diet Log open: pages {} ms (diversity icon {} ms, meals grid {} ms), first frame {} ms",
					ms(pagesTook), ms(icon), ms(grid), ms(took));
		}

		private static String ms(long nanos) {
			return String.format(Locale.ROOT, "%.1f", nanos / 1e6);
		}
	}

	/** Apple Pie's stat icon slot showing a food item (16 px), hover names it. */
	private static final class FoodIcon extends UIImage {
		private final ItemStack stack;

		FoodIcon(UIImage.Image image, ItemStack stack) {
			super(image);
			this.stack = stack;
			setWidth(16);
			setHeight(16);
			tooltip = stack.getHoverName().getString();
		}

		@Override
		protected void render(PoseStack pose) {
			Icons.item(stack, frame.x, frame.y, 16);
		}
	}

	private DietLogPages() {}
}
