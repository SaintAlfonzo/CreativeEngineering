package com.SaintAlfonzo.solraspberrypie.client;

import com.SaintAlfonzo.solraspberrypie.data.SlotChoice;
import com.SaintAlfonzo.solraspberrypie.ladder.Ladder;
import com.SaintAlfonzo.solraspberrypie.ladder.Rung;
import com.SaintAlfonzo.solraspberrypie.logic.DietEngine;
import com.SaintAlfonzo.solraspberrypie.net.C2SAction;
import com.SaintAlfonzo.solraspberrypie.net.RpNetwork;
import com.SaintAlfonzo.solraspberrypie.net.S2CPreview;
import com.SaintAlfonzo.solraspberrypie.net.S2CState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The effect screen (11.2): flat bonuses (read only), then the slots side by side with a progress bar under them (G5c). Active slots: fill (free, lands in the
 * lowest empty active slot), level change (free), change effect and drop (paid, through the confirm panel). Grayed
 * slots show their stored choice and are read only. A toggle effect (3.3) has an on/off switch instead of a level change:
 * a click on its flat icon, or the slot's On/Off button. Hover text carries the ladder's tagline. While paused, every
 * action is disabled and PAUSED shows.
 */
public class EffectScreen extends Screen {
	private static final int MIN_W = 276, CELL_MAX = 92, CELL_H = 90, BAR_H = 5;

	private enum Mode { SLOTS, PICK_FILL, PICK_CHANGE, CONFIRM }

	private Mode mode = Mode.SLOTS;
	private int pickSlot = -1;
	private S2CPreview preview;
	private int left, top, height0, panelW = MIN_W, cell = CELL_MAX;
	private final List<Runnable> hoverTips = new ArrayList<>();
	/** Buttons with a hover tooltip (Change and Drop: how many foods age out, plan_diet_books.md 1.10). */
	private final List<TipButton> tipButtons = new ArrayList<>();
	/** The flat icons' left edges this frame, in {@code S2CState.flat} order (clicks switch toggle effects). */
	private final List<Integer> flatXs = new ArrayList<>();
	private final List<ResourceLocation> flatIds = new ArrayList<>();

	public EffectScreen() {
		super(Component.translatable("solraspberrypie.screen.title"));
	}

	public static void open() {
		Minecraft.getInstance().setScreen(new EffectScreen());
	}

	public void refresh() {
		if (mode == Mode.CONFIRM) return; // the panel waits for the server's reply or the player's choice
		rebuild();
	}

	public void showPreview(S2CPreview p) {
		preview = p;
		mode = Mode.CONFIRM;
		rebuild();
	}

	@Override
	protected void init() {
		rebuild();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void rebuild() {
		clearWidgets();
		tipButtons.clear();
		if (!ClientState.ready()) return;
		Ladder ladder = ClientState.ladder;
		S2CState s = ClientState.state;
		int n = Math.max(1, ladder.slotCount());
		panelW = Math.max(MIN_W, Math.min(n * CELL_MAX + 16, width - 16));
		cell = (panelW - 16) / n;
		int pickRows = (pickable(ladder, s).size() + 10) / 11;
		height0 = Math.max(Math.max(210, 66 + CELL_H + 6 + BAR_H + 22 + 30), 66 + 14 + pickRows * 23 + 34);
		left = (width - panelW) / 2;
		top = Math.max(8, (height - height0) / 2);
		switch (mode) {
			case SLOTS -> buildSlots(ladder, s);
			case PICK_FILL, PICK_CHANGE -> buildPicker(ladder, s);
			case CONFIRM -> buildConfirm(s);
		}
		addRenderableWidget(new Button(left + panelW - 64, top + height0 - 26, 56, 20, Component.translatable("gui.done"), b -> onClose()));
	}

	private int slotsTop() {
		return top + 66;
	}

	/** Left edge of slot {@code i}'s cell: the slots sit side by side (G5c). */
	private int cellX(int i) {
		return left + 8 + i * cell;
	}

	private void buildSlots(Ladder ladder, S2CState s) {
		boolean locked = s.paused;
		for (int i = 0; i < ladder.slotCount(); i++) {
			int y = slotsTop();
			boolean active = i < s.activeSlots;
			SlotChoice c = i < s.slots.size() ? s.slots.get(i) : null;
			if (!active || locked) continue;
			final int slot = i;
			int cx = cellX(i) + cell / 2;
			if (c == null) {
				int bw = Math.min(64, cell - 8);
				addRenderableWidget(new Button(cx - bw / 2, y + 48, bw, 18, Component.translatable("solraspberrypie.screen.choose"), b -> {
					mode = Mode.PICK_FILL;
					pickSlot = slot;
					rebuild();
				}));
				continue;
			}
			if (ladder.isToggle(c.effect())) {
				boolean on = !s.off.contains(c.effect());
				addRenderableWidget(new Button(cx - 19, y + 48, 38, 18, Component.translatable(on ? "solraspberrypie.screen.on" : "solraspberrypie.screen.off"),
						b -> send(C2SAction.Op.TOGGLE, slot, c.effect(), c.level())));
			} else {
				int max = Math.max(ladder.unlockedLevel(c.effect(), s.maxDiversity), c.level());
				Button minus = new Button(cx - 19, y + 48, 18, 18, Component.literal("-"), b -> send(C2SAction.Op.LEVEL, slot, c.effect(), c.level() - 1));
				minus.active = c.level() > 1;
				Button plus = new Button(cx + 1, y + 48, 18, 18, Component.literal("+"), b -> send(C2SAction.Op.LEVEL, slot, c.effect(), c.level() + 1));
				plus.active = c.level() < max && c.level() < ladder.unlockedLevel(c.effect(), s.maxDiversity);
				addRenderableWidget(minus);
				addRenderableWidget(plus);
			}
			int bw = Math.min(44, (cell - 8) / 2);
			tipButtons.add(addRenderableWidget(new TipButton(cx - bw - 1, y + 68, bw, 18, Component.translatable("solraspberrypie.screen.change"), b -> {
				mode = Mode.PICK_CHANGE;
				pickSlot = slot;
				rebuild();
			}, () -> coveredSlot(slot) ? List.of(Component.translatable("solraspberrypie.screen.free").withStyle(ChatFormatting.GREEN))
					: ageTip(ClientState.state.changeAge))));
			tipButtons.add(addRenderableWidget(new TipButton(cx + 1, y + 68, bw, 18, Component.translatable("solraspberrypie.screen.drop"),
					b -> send(C2SAction.Op.PREVIEW_DROP, slot, null, 0), () -> ageTip(ClientState.state.dropAge))));
		}
	}

	private List<ResourceLocation> pickable(Ladder ladder, S2CState s) {
		List<ResourceLocation> out = new ArrayList<>();
		for (ResourceLocation id : ladder.pool.keySet()) {
			if (ladder.unlockedLevel(id, s.maxDiversity) < 1 || s.flat.containsKey(id) || ladder.replacedBy(id, s.maxDiversity) != null) continue;
			boolean used = false;
			for (SlotChoice c : s.slots) if (c != null && c.effect().equals(id)) used = true;
			if (!used && ForgeRegistries.MOB_EFFECTS.containsKey(id)) out.add(id);
		}
		return out;
	}

	private int gridX(int i) {
		return left + 12 + (i % 11) * 23;
	}

	private int gridY(int i) {
		return slotsTop() + 14 + (i / 11) * 23;
	}

	private void buildPicker(Ladder ladder, S2CState s) {
		List<ResourceLocation> ids = pickable(ladder, s);
		for (int i = 0; i < ids.size(); i++) {
			ResourceLocation id = ids.get(i);
			int lvl = ladder.unlockedLevel(id, s.maxDiversity);
			Button b = new Button(gridX(i), gridY(i), 22, 22, Component.empty(), btn -> {
				if (mode == Mode.PICK_FILL) {
					send(C2SAction.Op.FILL, pickSlot, id, lvl);
					mode = Mode.SLOTS;
				} else {
					send(C2SAction.Op.PREVIEW_CHANGE, pickSlot, id, lvl);
				}
			});
			addRenderableWidget(b);
		}
		addRenderableWidget(new Button(left + 8, top + height0 - 26, 60, 20, Component.translatable("gui.cancel"), b -> {
			mode = Mode.SLOTS;
			rebuild();
		}));
	}

	private void buildConfirm(S2CState s) {
		Button ok = new Button(left + 8, top + height0 - 26, 70, 20, Component.translatable("solraspberrypie.screen.confirm"), b -> {
			C2SAction.Op op = preview.op == C2SAction.Op.PREVIEW_CHANGE ? C2SAction.Op.CONFIRM_CHANGE : C2SAction.Op.CONFIRM_DROP;
			RpNetwork.toServer(new C2SAction(op, preview.slot, preview.effect, preview.level, preview.stamp));
			preview = null;
			mode = Mode.SLOTS;
			rebuild();
		});
		ok.active = !s.paused;
		addRenderableWidget(ok);
		addRenderableWidget(new Button(left + 82, top + height0 - 26, 60, 20, Component.translatable("gui.cancel"), b -> {
			preview = null;
			mode = Mode.SLOTS;
			rebuild();
		}));
	}

	private void send(C2SAction.Op op, int slot, ResourceLocation effect, int level) {
		RpNetwork.toServer(new C2SAction(op, slot, effect, level, ClientState.state.stamp));
	}

	// ---- drawing ---------------------------------------------------------------------------------------------------

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partial) {
		renderBackground(pose);
		hoverTips.clear();
		if (!ClientState.ready()) {
			drawCenteredString(pose, font, Component.translatable("solraspberrypie.screen.waiting"), width / 2, height / 2, 0xFFFFFF);
			super.render(pose, mouseX, mouseY, partial);
			return;
		}
		Ladder ladder = ClientState.ladder;
		S2CState s = ClientState.state;
		fill(pose, left - 1, top - 1, left + panelW + 1, top + height0 + 1, 0xFF6B6B6B);
		fill(pose, left, top, left + panelW, top + height0, 0xFF202020);
		drawString(pose, font, title, left + 8, top + 8, 0xFFFFFF);
		Component div = Component.translatable("solraspberrypie.screen.diversity", Icons.diversity(s.diversity), Icons.diversity(s.maxDiversity));
		drawString(pose, font, div, left + 8, top + 20, 0xAAAAAA);
		if (s.foodsEaten < s.minFoods) {
			drawString(pose, font, DietLogPages.gateText(s), left + 8, top + 31, 0xFFAA55);
		}
		// Flat bonuses (read only).
		Component flatLabel = Component.translatable("solraspberrypie.screen.flat");
		drawString(pose, font, flatLabel, left + 8, top + 44, 0xFFD27F);
		int fx = left + 8 + font.width(flatLabel) + 14;
		if (s.flat.isEmpty()) drawString(pose, font, Component.translatable("solraspberrypie.screen.none"), fx, top + 44, 0x777777);
		flatXs.clear();
		flatIds.clear();
		for (Map.Entry<ResourceLocation, Integer> e : s.flat.entrySet()) {
			ResourceLocation id = e.getKey();
			boolean toggle = ladder.isToggle(id), off = s.off.contains(id);
			fill(pose, fx - 1, top + 39, fx + 19, top + 59, off ? 0xFF4A3D1E : 0xFF8A6D1E);
			if (off) RenderSystem.setShaderColor(0.35F, 0.35F, 0.35F, 1F);
			Icons.effect(pose, id, fx, top + 40);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			if (toggle) fill(pose, fx + 1, top + 57, fx + 17, top + 58, off ? 0xFFCC4444 : 0xFF44CC44);
			flatXs.add(fx);
			flatIds.add(id);
			if (in(mouseX, mouseY, fx, top + 40, 18, 18)) {
				List<Component> tip = new ArrayList<>();
				tip.add(DietEngine.effectName(id, e.getValue()));
				addTagline(tip, ladder, id, e.getValue());
				tip.add(Component.translatable("solraspberrypie.screen.flat_tip").withStyle(ChatFormatting.GOLD));
				if (toggle) addToggleState(tip, off, !s.paused && mode == Mode.SLOTS);
				hoverTips.add(() -> renderComponentTooltip(pose, tip, mouseX, mouseY));
			}
			fx += 22;
		}
		switch (mode) {
			case SLOTS -> drawSlots(pose, ladder, s, mouseX, mouseY);
			case PICK_FILL, PICK_CHANGE -> drawPicker(pose, ladder, s, mouseX, mouseY);
			case CONFIRM -> drawConfirm(pose, mouseX, mouseY);
		}
		super.render(pose, mouseX, mouseY, partial);
		for (TipButton b : tipButtons) {
			if (b.visible && b.isMouseOver(mouseX, mouseY)) hoverTips.add(() -> renderComponentTooltip(pose, b.tip.get(), mouseX, mouseY));
		}
		for (Runnable r : hoverTips) r.run();
	}

	private void drawSlots(PoseStack pose, Ladder ladder, S2CState s, int mouseX, int mouseY) {
		if (s.paused) {
			drawString(pose, font, Component.translatable("solraspberrypie.paused").withStyle(ChatFormatting.BOLD), left + panelW - 60, top + 8, 0xFF5555);
		}
		if (ladder.slotCount() == 0) {
			drawString(pose, font, Component.translatable("solraspberrypie.screen.no_slots"), left + 8, slotsTop() + 4, 0x777777);
			return;
		}
		int y = slotsTop();
		for (int i = 0; i < ladder.slotCount(); i++) {
			int x = cellX(i), cx = x + cell / 2;
			boolean active = i < s.activeSlots;
			SlotChoice c = i < s.slots.size() ? s.slots.get(i) : null;
			fill(pose, x + 1, y, x + cell - 1, y + CELL_H, active ? 0xFF2E3A2E : 0xFF2A2A2A);
			drawCenteredString(pose, font, Component.translatable("solraspberrypie.screen.slot", i + 1), cx, y + 3, active ? 0x88AA88 : 0x666666);
			fill(pose, cx - 10, y + 13, cx + 10, y + 33, 0xFF111111);
			boolean off = c != null && s.off.contains(c.effect());
			// food-tuning #49, #50: covered by a permanent buff or replaced by an unlocked effect: grayed, change is free.
			boolean covered = c != null && active && ladder.covered(c.effect(), s.flat.keySet(), s.maxDiversity);
			if (c != null) {
				if (!active || s.paused || covered) RenderSystem.setShaderColor(0.4F, 0.4F, 0.4F, 1F);
				else if (off) RenderSystem.setShaderColor(0.35F, 0.35F, 0.35F, 1F);
				Icons.effect(pose, c.effect(), cx - 9, y + 14);
				RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			}
			Component name = c != null ? DietEngine.effectName(c.effect(), c.level()) : Component.translatable("solraspberrypie.screen.empty");
			int color = !active || covered ? 0x777777 : off ? 0xAAAAAA : 0xFFFFFF;
			String shown = font.plainSubstrByWidth(name.getString(), cell - 6);
			drawString(pose, font, shown, cx - font.width(shown) / 2, y + 37, color);
			if (in(mouseX, mouseY, x + 1, y, cell - 2, 46)) {
				Rung r = ladder.slots.get(i);
				ResourceLocation by = covered ? ladder.replacedBy(c.effect(), s.maxDiversity) : null;
				Component sub = active ? (covered ? (by != null && !s.flat.containsKey(c.effect())
						? Component.translatable("solraspberrypie.screen.replaced", DietEngine.effectName(by, 0))
						: Component.translatable("solraspberrypie.screen.covered"))
						: Component.translatable("solraspberrypie.widget.slot", i + 1, Icons.num(r.threshold)))
						: Component.translatable("solraspberrypie.screen.greyed", i + 1, Icons.num(r.threshold));
				List<Component> tip = new ArrayList<>();
				tip.add(name);
				if (c != null) addTagline(tip, ladder, c.effect(), c.level());
				tip.add(sub.copy().withStyle(covered ? ChatFormatting.YELLOW : active ? ChatFormatting.GREEN : ChatFormatting.GRAY));
				if (covered) tip.add(Component.translatable("solraspberrypie.screen.change_free").withStyle(ChatFormatting.GREEN));
				if (c != null && ladder.isToggle(c.effect())) addToggleState(tip, off, false);
				hoverTips.add(() -> renderComponentTooltip(pose, tip, mouseX, mouseY));
			}
		}
		drawSlotBar(pose, ladder, s, y + CELL_H + 6);
	}

	/**
	 * The slot progress bar (G5c), replacing each slot's "needs diversity" line: it runs under the whole row of slots,
	 * one segment per slot, with a tick and the slot's diversity at the right end of each segment. The green fill
	 * follows the current diversity piece by piece and is full at the last slot's diversity.
	 */
	private void drawSlotBar(PoseStack pose, Ladder ladder, S2CState s, int barY) {
		int n = ladder.slotCount();
		int x0 = cellX(0) + 1, x1 = cellX(n - 1) + cell - 1;
		fill(pose, x0, barY, x1, barY + BAR_H, 0xFF555555);
		int filled = x0;
		double prev = 0;
		for (int i = 0; i < n; i++) {
			double t = ladder.slots.get(i).threshold;
			int a = i == 0 ? x0 : cellX(i), b = i == n - 1 ? x1 : cellX(i + 1);
			if (s.diversity >= t) filled = b;
			else if (s.diversity > prev && t > prev) filled = Math.max(filled, a + (int) Math.round((b - a) * (s.diversity - prev) / (t - prev)));
			prev = Math.max(prev, t);
		}
		if (filled > x0) fill(pose, x0, barY, filled, barY + BAR_H, 0xFF3FA34D);
		drawString(pose, font, "0", x0, barY + BAR_H + 3, 0x888888);
		for (int i = 0; i < n; i++) {
			double t = ladder.slots.get(i).threshold;
			int tx = i == n - 1 ? x1 - 1 : cellX(i + 1);
			boolean got = s.diversity >= t;
			fill(pose, tx, barY - 2, tx + 1, barY + BAR_H + 2, got ? 0xFFFFFFFF : 0xFFAAAAAA);
			String label = Icons.num(t);
			int lx = Math.min(x1 - font.width(label), Math.max(x0, tx - font.width(label) / 2));
			drawString(pose, font, label, lx, barY + BAR_H + 3, got ? 0x88CC88 : 0x888888);
		}
	}

	private void drawPicker(PoseStack pose, Ladder ladder, S2CState s, int mouseX, int mouseY) {
		Component head = Component.translatable(mode == Mode.PICK_FILL ? "solraspberrypie.screen.pick_fill" : "solraspberrypie.screen.pick_change", pickSlot + 1);
		drawString(pose, font, head, left + 8, slotsTop(), 0xFFFFFF);
		List<ResourceLocation> ids = pickable(ladder, s);
		if (ids.isEmpty()) drawString(pose, font, Component.translatable("solraspberrypie.screen.none_unlocked"), left + 12, slotsTop() + 18, 0x777777);
		// Icons are drawn after the buttons (super.render), so queue them as overlays.
		for (int i = 0; i < ids.size(); i++) {
			ResourceLocation id = ids.get(i);
			int x = gridX(i), y = gridY(i);
			int lvl = ladder.unlockedLevel(id, s.maxDiversity);
			hoverTips.add(0, () -> Icons.effect(pose, id, x + 2, y + 2));
			if (in(mouseX, mouseY, x, y, 22, 22)) {
				List<Component> tip = new ArrayList<>();
				tip.add(DietEngine.effectName(id, lvl));
				addTagline(tip, ladder, id, lvl);
				if (mode == Mode.PICK_CHANGE && !coveredSlot(pickSlot)) tip.add(Component.translatable("solraspberrypie.screen.change_cost").withStyle(ChatFormatting.RED));
				else tip.add(Component.translatable("solraspberrypie.screen.free").withStyle(ChatFormatting.GREEN));
				hoverTips.add(() -> renderComponentTooltip(pose, tip, mouseX, mouseY));
			}
		}
	}

	private void drawConfirm(PoseStack pose, int mouseX, int mouseY) {
		if (preview == null) return;
		int y = slotsTop();
		Component head = preview.op == C2SAction.Op.PREVIEW_CHANGE
				? Component.translatable("solraspberrypie.confirm.change", preview.slot + 1, DietEngine.effectName(preview.effect, preview.level))
				: Component.translatable("solraspberrypie.confirm.drop", preview.slot + 1);
		drawString(pose, font, head, left + 8, y, 0xFFFFFF);
		y += 12;
		if (preview.stale) {
			drawString(pose, font, Component.translatable("solraspberrypie.confirm.stale"), left + 8, y, 0xFFFF55);
			y += 12;
		}
		drawString(pose, font, Component.translatable("solraspberrypie.confirm.diversity", Icons.diversity(preview.before), Icons.diversity(preview.after)), left + 8, y, 0xAAAAAA);
		y += 12;
		drawString(pose, font, Component.translatable("solraspberrypie.confirm.aged", preview.aged.size()), left + 8, y, 0xAAAAAA);
		y += 10;
		int x = left + 8;
		for (ResourceLocation id : preview.aged) {
			Item item = ForgeRegistries.ITEMS.getValue(id);
			if (item == null) continue;
			ItemStack stack = new ItemStack(item);
			itemRenderer.renderAndDecorateItem(stack, x, y);
			if (in(mouseX, mouseY, x, y, 16, 16)) hoverTips.add(() -> renderTooltip(pose, stack, mouseX, mouseY));
			x += 17;
			if (x > left + panelW - 24) {
				x = left + 8;
				y += 17;
			}
		}
		y += 20;
		if (preview.slotGreyedAfter) {
			drawString(pose, font, Component.translatable("solraspberrypie.confirm.greyed"), left + 8, y, 0xFFFF55);
			y += 12;
		}
		drawString(pose, font, Component.translatable(preview.lines.isEmpty() ? "solraspberrypie.confirm.nothing_lost" : "solraspberrypie.confirm.lost"), left + 8, y, 0xFFFFFF);
		y += 11;
		for (int i = 0; i < preview.lines.size() && y < top + height0 - 30; i++) {
			drawString(pose, font, preview.lines.get(i), left + 14, y, preview.yellow.get(i) ? 0xFFFF55 : 0xFF5555);
			y += 10;
		}
	}

	/** A click on a toggle effect's flat icon switches it (3.3); free, like the slot's On/Off button. */
	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == 0 && mode == Mode.SLOTS && ClientState.ready() && !ClientState.state.paused) {
			for (int i = 0; i < flatXs.size(); i++) {
				ResourceLocation id = flatIds.get(i);
				if (ClientState.ladder.isToggle(id) && in((int) mx, (int) my, flatXs.get(i), top + 40, 18, 18)) {
					Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
							net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1F));
					send(C2SAction.Op.TOGGLE, -1, id, 0);
					return true;
				}
			}
		}
		return super.mouseClicked(mx, my, button);
	}

	/** The ladder's hover tagline for an effect at a level (3.3), gray, when it has one. */
	public static void addTagline(List<Component> tip, Ladder ladder, ResourceLocation id, int level) {
		String key = ladder.tagline(id, level);
		if (key != null) tip.add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
	}

	/** "Switched on" / "Switched off" for a toggle effect, and the click hint where a click switches it. */
	public static void addToggleState(List<Component> tip, boolean off, boolean clickable) {
		tip.add(Component.translatable(off ? "solraspberrypie.toggle.off" : "solraspberrypie.toggle.on")
				.withStyle(off ? ChatFormatting.RED : ChatFormatting.GREEN));
		if (clickable) tip.add(Component.translatable("solraspberrypie.toggle.click").withStyle(ChatFormatting.DARK_GRAY));
	}

	/** Whether an active slot's choice is covered (food-tuning #49, #50): changing it ages out no foods. */
	private static boolean coveredSlot(int slot) {
		S2CState s = ClientState.state;
		if (s == null || ClientState.ladder == null || slot < 0 || slot >= s.activeSlots || slot >= s.slots.size()) return false;
		SlotChoice c = s.slots.get(slot);
		return c != null && ClientState.ladder.covered(c.effect(), s.flat.keySet(), s.maxDiversity);
	}

	private static boolean in(int mx, int my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	/** "N foods age out" (singular for one). */
	private static List<Component> ageTip(int n) {
		return List.of(Component.translatable(n == 1 ? "solraspberrypie.screen.age_out_one" : "solraspberrypie.screen.age_out", n).withStyle(ChatFormatting.RED));
	}

	/**
	 * A button whose label is drawn smaller than vanilla's (plan_diet_books.md 1.10: the button keeps its size, the
	 * text is {@link #LABEL_SCALE}), with a hover tooltip.
	 */
	private static final class TipButton extends Button {
		private static final float LABEL_SCALE = 0.85F;
		final Supplier<List<Component>> tip;

		TipButton(int x, int y, int w, int h, Component label, OnPress press, Supplier<List<Component>> tip) {
			super(x, y, w, h, label, press);
			this.tip = tip;
		}

		@Override
		public void renderButton(PoseStack pose, int mouseX, int mouseY, float partial) {
			Component label = getMessage();
			setMessage(Component.empty());
			super.renderButton(pose, mouseX, mouseY, partial);
			setMessage(label);
			int color = (active ? 0xFFFFFF : 0xA0A0A0) | (Mth.ceil(alpha * 255F) << 24);
			pose.pushPose();
			pose.translate(x + width / 2F, y + (height - 8 * LABEL_SCALE) / 2F, 0);
			pose.scale(LABEL_SCALE, LABEL_SCALE, 1F);
			drawCenteredString(pose, Minecraft.getInstance().font, label, 0, 0, color);
			pose.popPose();
		}
	}
}
