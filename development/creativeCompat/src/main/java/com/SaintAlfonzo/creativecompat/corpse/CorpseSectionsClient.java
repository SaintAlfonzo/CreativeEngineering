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
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;

import java.util.Map;
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
 * </ul>
 */
public final class CorpseSectionsClient {
	private static final ResourceLocation CORPSE_GUI = new ResourceLocation("corpse", "textures/gui/inventory_corpse.png");
	private static final ResourceLocation CURIO_GUI = new ResourceLocation("curios", "textures/gui/inventory.png");

	private record Parsed(CompoundTag tag, ItemStack[] stacks, boolean[] skin) {}

	private static final Map<CorpseEntity, Parsed> COSMETIC = new WeakHashMap<>();

	private CorpseSectionsClient() {}

	public static void register() {
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
		for (int i = 0; i < armor.size() && i < CorpseSections.COSMETIC; i++) {
			ItemStack want = real(corpse, i);
			if (p != null && p.skin()[i]) want = ItemStack.EMPTY;
			else if (p != null && !p.stacks()[i].isEmpty()) want = p.stacks()[i];
			armor.set(i, want);
		}
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
