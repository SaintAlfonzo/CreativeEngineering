package com.SaintAlfonzo.creativecompat.corpse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Curio origin in corpses (feature_writeup/plan_corpses.md 3.6 items 1 and 4, ruling 32). Curios drops a dead
 * player's curios at NORMAL priority of {@link LivingDropsEvent} and the Corpse mod collects every unmatched drop into
 * its Additional Items at LOWEST, so the slot is lost. A HIGHEST listener snapshots each curio stack with its slot id,
 * index and cosmetic flag; a LOW listener tags the matching drops {@link CorpseSections#CURIO} {Slot, Index,
 * Cosmetic, Hidden (render toggled off)}. Transfer Items puts each tagged curio back into that exact slot when it is empty (before the Corpse mod
 * and Corpse Curios Compat run; mixin/CorpseTransferMessageMixin); a curio whose slot is taken is left to them.
 * Only this class touches the Curios API; callers check {@link CorpseSections#CURIOS} first, so the class is never
 * loaded without Curios.
 */
public final class CorpseCurios {
	private record Entry(String slot, int index, boolean cosmetic, boolean hidden, ItemStack stack) {}

	private static final Map<LivingEntity, List<Entry>> SNAPSHOTS = new WeakHashMap<>();

	private CorpseCurios() {}

	public static void register() {
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, LivingDropsEvent.class, CorpseCurios::snapshot);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, false, LivingDropsEvent.class, CorpseCurios::tag);
	}

	private static void snapshot(LivingDropsEvent event) {
		if (!(event.getEntity() instanceof Player player) || player.level.isClientSide) return;
		List<Entry> out = new ArrayList<>();
		CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
			for (Map.Entry<String, ICurioStacksHandler> e : handler.getCurios().entrySet()) {
				add(out, e.getKey(), false, e.getValue().getStacks(), e.getValue());
				add(out, e.getKey(), true, e.getValue().getCosmeticStacks(), e.getValue());
			}
		});
		if (!out.isEmpty()) SNAPSHOTS.put(player, out);
	}

	private static void add(List<Entry> out, String slot, boolean cosmetic, IDynamicStackHandler stacks, ICurioStacksHandler type) {
		for (int i = 0; i < stacks.getSlots(); i++) {
			ItemStack s = stacks.getStackInSlot(i);
			// the player's render toggle for this slot index (Curios' eye button), so the corpse model hides it too
			boolean hidden = i < type.getRenders().size() && !type.getRenders().get(i);
			if (!s.isEmpty()) out.add(new Entry(slot, i, cosmetic, hidden, s.copy()));
		}
	}

	private static void tag(LivingDropsEvent event) {
		List<Entry> entries = SNAPSHOTS.remove(event.getEntity());
		if (entries == null) return;
		List<ItemEntity> drops = new ArrayList<>(event.getDrops());
		boolean[] used = new boolean[drops.size()];
		for (Entry e : entries) {
			for (int i = 0; i < drops.size(); i++) {
				ItemStack d = drops.get(i).getItem();
				if (used[i] || CorpseSections.isCurio(d) || !ItemStack.matches(d, e.stack())) continue;
				used[i] = true;
				CompoundTag t = new CompoundTag();
				t.putString("Slot", e.slot());
				t.putInt("Index", e.index());
				t.putBoolean("Cosmetic", e.cosmetic());
				if (e.hidden()) t.putBoolean("Hidden", true);
				d.getOrCreateTag().put(CorpseSections.CURIO, t);
				break;
			}
		}
	}

	/** Transfer Items, first step: each tagged curio in the corpse goes back to its own slot when that slot is empty. */
	public static void returnToSlots(ServerPlayer player, List<ItemStack> additional) {
		ICuriosItemHandler handler = CuriosApi.getCuriosHelper().getCuriosHandler(player).resolve().orElse(null);
		if (handler == null) return;
		Map<String, ICurioStacksHandler> curios = handler.getCurios();
		for (int i = 0; i < additional.size(); i++) {
			ItemStack s = additional.get(i);
			if (s.isEmpty() || !CorpseSections.isCurio(s)) continue;
			CompoundTag t = s.getTag().getCompound(CorpseSections.CURIO);
			ICurioStacksHandler type = curios.get(t.getString("Slot"));
			if (type == null) continue;
			IDynamicStackHandler stacks = t.getBoolean("Cosmetic") ? type.getCosmeticStacks() : type.getStacks();
			int index = t.getInt("Index");
			if (index < 0 || index >= stacks.getSlots() || !stacks.getStackInSlot(index).isEmpty()) continue;
			stacks.setStackInSlot(index, CorpseSections.strip(s.copy()));
			additional.set(i, ItemStack.EMPTY);
		}
	}

	/** Transfer Items, last step: no section tag stays on a stack that went into a curio slot. */
	public static void sweep(Player player) {
		CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
			for (ICurioStacksHandler type : handler.getCurios().values()) {
				strip(type.getStacks());
				strip(type.getCosmeticStacks());
			}
		});
	}

	private static void strip(IDynamicStackHandler stacks) {
		for (int i = 0; i < stacks.getSlots(); i++) {
			ItemStack s = stacks.getStackInSlot(i);
			if (!s.isEmpty() && (CorpseSections.isCurio(s) || CorpseSections.isCosmetic(s))) {
				stacks.setStackInSlot(i, CorpseSections.strip(s.copy()));
			}
		}
	}
}
