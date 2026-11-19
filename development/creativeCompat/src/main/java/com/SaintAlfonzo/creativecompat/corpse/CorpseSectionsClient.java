package com.SaintAlfonzo.creativecompat.corpse;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.maxhenkel.corpse.Main;
import de.maxhenkel.corpse.entities.CorpseEntity;
import de.maxhenkel.corpse.entities.DummyPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Client half of the corpse sections (feature_writeup/plan_corpses.md 3.6 items 5 and 6):
 * <ul>
 * <li>{@link #drawBackground}: slot frames for the 4 cosmetic slots and the moved offhand (blitted from the corpse
 * texture's own slot frame, no new texture) and the curio panel left of the window in Curios' own panel art, one
 * column of up to 12 slots, a second column further left past 12.</li>
 * <li>Tooltips: a stack in a corpse section names its curio slot type, or "Cosmetic armor".</li>
 * <li>The corpse's player model wears the corpse's own cosmetic armor. Cosmetic Armor Reworked swaps armor at
 * {@code RenderPlayerEvent.Pre} (HIGH) by player UUID, and the Corpse mod's dummy player carries the dead player's
 * UUID, so the corpse showed that player's current cosmetic armor. The NORMAL listener here writes the corpse's synced
 * cosmetic stacks (else its real armor) over that swap; Cosmetic Armor's own Post (LOW) restore still runs.</li>
 * <li>Round 2: the same listener fills the dummy player's client curios handler from the synced curio list
 * ({@link CorpseCuriosClient}), so Curios' own render layer and Elytra Slot's layer draw them as on the player. The
 * skeleton gets the cosmetic armor through {@link #withCosmetics} and its curios through {@link CorpseSkeletonLayer},
 * added here to the vanilla skeleton renderer (and by CorpseRender to its wither look).</li>
 * </ul>
 */
public final class CorpseSectionsClient {
	private static final ResourceLocation CORPSE_GUI = new ResourceLocation("corpse", "textures/gui/inventory_corpse.png");
	private static final ResourceLocation CURIO_GUI = new ResourceLocation("curios", "textures/gui/inventory.png");

	private record Parsed(CompoundTag tag, ItemStack[] stacks, boolean[] skin) {}

	private static final Map<CorpseEntity, Parsed> COSMETIC = new WeakHashMap<>();

	private CorpseSectionsClient() {}

	public static void register(IEventBus modBus) {
		modBus.addListener(EventPriority.NORMAL, false, EntityRenderersEvent.AddLayers.class, CorpseSectionsClient::onAddLayers);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderPlayerEvent.Pre.class, CorpseSectionsClient::onRenderPre);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RenderPlayerEvent.Post.class, CorpseSectionsClient::onRenderPost);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, ItemTooltipEvent.class, CorpseSectionsClient::onTooltip);
	}

	// ---- screen ----

	public static void drawBackground(PoseStack pose, int left, int top, AbstractContainerMenu menu) {
		int curios = 0;
		boolean any = false;
		for (Slot slot : menu.slots) {
			if (slot instanceof CorpseSections.SectionSlot s) {
				any = true;
				if (s.isCurio() && s.isActive()) curios++;
			}
		}
		if (!any) return;
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, CORPSE_GUI);
		// the texture's top-left armor slot frame (slot at 8, 18) copied to the cosmetic slots and the offhand
		for (int k = 0; k < CorpseSections.COSMETIC; k++) {
			frame(pose, left + CorpseSections.COSMETIC_X + k * 18 - 1, top + CorpseSections.TOP_Y - 1);
		}
		frame(pose, left + CorpseSections.OFFHAND_X - 1, top + CorpseSections.TOP_Y - 1);
		if (curios == 0) return;
		RenderSystem.setShaderTexture(0, CURIO_GUI);
		for (int col = 0; col * CorpseSections.COLUMN < curios; col++) {
			int rows = Math.min(CorpseSections.COLUMN, curios - col * CorpseSections.COLUMN);
			int px = left + CorpseSections.CURIO_X - 8 - col * CorpseSections.COLUMN_STEP;
			int py = top + CorpseSections.TOP_Y - 8;
			GuiComponent.blit(pose, px, py, 0, 0, 27, 7, 256, 256);
			for (int r = 0; r < rows; r++) GuiComponent.blit(pose, px, py + 7 + r * 18, 0, 7, 27, 18, 256, 256);
			GuiComponent.blit(pose, px, py + 7 + rows * 18, 0, 151, 27, 7, 256, 256);
		}
	}

	private static void frame(PoseStack pose, int x, int y) {
		GuiComponent.blit(pose, x, y, 7, 17, 18, 18, 256, 256);
	}

	// ---- tooltips ----

	private static void onTooltip(ItemTooltipEvent event) {
		ItemStack s = event.getItemStack();
		if (CorpseSections.isCurio(s)) {
			CompoundTag t = s.getTag().getCompound(CorpseSections.CURIO);
			Component type = Component.translatable("curios.identifier." + t.getString("Slot"));
			event.getToolTip().add(Component.translatable(t.getBoolean("Cosmetic")
					? "tooltip.creativecompat.corpse.curio_cosmetic" : "tooltip.creativecompat.corpse.curio", type)
					.withStyle(ChatFormatting.GRAY));
		} else if (CorpseSections.isCosmetic(s)) {
			event.getToolTip().add(Component.translatable("tooltip.creativecompat.corpse.cosmetic").withStyle(ChatFormatting.GRAY));
		}
	}

	// ---- model ----

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void onAddLayers(EntityRenderersEvent.AddLayers event) {
		LivingEntityRenderer renderer = event.getRenderer(EntityType.SKELETON);
		if (renderer instanceof SkeletonRenderer skeleton) {
			skeleton.addLayer(new CorpseSkeletonLayer<>(skeleton, event.getEntityModels()));
		}
	}

	/** One curio worn by the corpse, from the synced list. */
	public record Worn(String slot, int index, boolean cosmetic, boolean hidden, ItemStack stack) {}

	private record WornCache(CompoundTag tag, List<Worn> all, List<Worn> shown) {}

	private static final Map<CorpseEntity, WornCache> WORN = new WeakHashMap<>();

	private static WornCache wornCache(CorpseEntity corpse) {
		CompoundTag tag = sectionTag(corpse);
		if (tag == null) return new WornCache(null, List.of(), List.of());
		WornCache c = WORN.get(corpse);
		if (c != null && c.tag() == tag) return c;
		List<Worn> all = new ArrayList<>();
		ListTag list = tag.getList("Curios", Tag.TAG_COMPOUND);
		for (int i = 0; i < list.size(); i++) {
			CompoundTag e = list.getCompound(i);
			ItemStack stack = ItemStack.of(e.getCompound("Item"));
			if (stack.isEmpty()) continue;
			all.add(new Worn(e.getString("Slot"), e.getInt("Index"), e.getBoolean("Cosmetic"), e.getBoolean("Hidden"), stack));
		}
		// what the model shows per slot index, as Curios' own layer: the cosmetic stack over the real one, nothing when
		// the player had that index's render toggled off
		Map<String, Worn> shown = new LinkedHashMap<>();
		Set<String> hidden = new HashSet<>();
		for (Worn w : all) {
			String key = w.slot() + "#" + w.index();
			if (w.hidden()) hidden.add(key);
			Worn had = shown.get(key);
			if (had == null || (w.cosmetic() && !had.cosmetic())) shown.put(key, w);
		}
		shown.keySet().removeAll(hidden);
		c = new WornCache(tag, List.copyOf(all), List.copyOf(shown.values()));
		WORN.put(corpse, c);
		return c;
	}

	/** Every curio stack the corpse holds, cosmetic and hidden ones included (fills the dummy player's handler). */
	public static List<Worn> worn(CorpseEntity corpse) {
		return wornCache(corpse).all();
	}

	/** The curios the corpse model shows: one per slot index, cosmetic first, render-toggled ones left out. */
	public static List<Worn> shownCurios(CorpseEntity corpse) {
		return wornCache(corpse).shown();
	}

	/** The synced section tag object (identity changes when the server resyncs it). */
	static CompoundTag sectionTag(CorpseEntity corpse) {
		return corpse instanceof CorpseState state ? state.creativecompat$sections() : null;
	}

	/** The equipment the skeleton wears: the real list with each armor slot shown as the corpse's cosmetic armor. */
	public static NonNullList<ItemStack> withCosmetics(CorpseEntity corpse, NonNullList<ItemStack> equipment) {
		Parsed p = parsed(corpse);
		if (p == null) return equipment;
		NonNullList<ItemStack> out = NonNullList.withSize(equipment.size(), ItemStack.EMPTY);
		for (int i = 0; i < equipment.size(); i++) out.set(i, equipment.get(i));
		for (int i = 0; i < CorpseSections.COSMETIC; i++) {
			int ordinal = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i).ordinal();
			if (ordinal < out.size()) out.set(ordinal, shown(corpse, p, i));
		}
		return out;
	}

	/** What armor slot {@code i} (0 feet to 3 head) shows: the cosmetic stack, nothing when skin-hidden, else the real one. */
	private static ItemStack shown(CorpseEntity corpse, Parsed p, int i) {
		if (p != null && p.skin()[i]) return ItemStack.EMPTY;
		if (p != null && !p.stacks()[i].isEmpty()) return p.stacks()[i];
		return real(corpse, i);
	}

	private static Parsed parsed(CorpseEntity corpse) {
		if (!(corpse instanceof CorpseState state)) return null;
		CompoundTag tag = state.creativecompat$sections();
		Parsed p = COSMETIC.get(corpse);
		if (p != null && p.tag() == tag) return p;
		ItemStack[] stacks = new ItemStack[CorpseSections.COSMETIC];
		boolean[] skin = new boolean[CorpseSections.COSMETIC];
		ListTag list = tag.getList("Cosmetic", Tag.TAG_COMPOUND);
		for (int i = 0; i < CorpseSections.COSMETIC; i++) {
			CompoundTag c = i < list.size() ? list.getCompound(i) : new CompoundTag();
			stacks[i] = c.contains("id") ? ItemStack.of(c) : ItemStack.EMPTY;
			skin[i] = c.getBoolean("Skin");
		}
		p = new Parsed(tag, stacks, skin);
		COSMETIC.put(corpse, p);
		return p;
	}

	private static void onRenderPre(RenderPlayerEvent.Pre event) {
		if (!(event.getEntity() instanceof DummyPlayer dummy) || !Main.SERVER_CONFIG.renderEquipment.get()) return;
		CorpseEntity corpse = CorpseRender.renderingFor(dummy);
		if (corpse == null) return;
		Parsed p = parsed(corpse);
		NonNullList<ItemStack> armor = dummy.getInventory().armor;
		for (int i = 0; i < armor.size() && i < CorpseSections.COSMETIC; i++) armor.set(i, shown(corpse, p, i));
		if (CorpseSections.CURIOS) CorpseCuriosClient.fill(dummy, corpse);
	}

	private static void onRenderPost(RenderPlayerEvent.Post event) {
		if (!(event.getEntity() instanceof DummyPlayer dummy) || !Main.SERVER_CONFIG.renderEquipment.get()) return;
		CorpseEntity corpse = CorpseRender.renderingFor(dummy);
		if (corpse == null) return;
		NonNullList<ItemStack> armor = dummy.getInventory().armor;
		for (int i = 0; i < armor.size() && i < CorpseSections.COSMETIC; i++) armor.set(i, real(corpse, i));
	}

	/** The real armor the corpse model wears in armor slot {@code i} (0 feet to 3 head). */
	private static ItemStack real(CorpseEntity corpse, int i) {
		NonNullList<ItemStack> eq = corpse.getEquipment();
		int ordinal = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i).ordinal();
		return ordinal < eq.size() ? eq.get(ordinal) : ItemStack.EMPTY;
	}
}
