package com.SaintAlfonzo.creativeloot.corpse;

import com.SaintAlfonzo.creativecompat.corpse.CorpseState;
import de.maxhenkel.corpse.entities.CorpseEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

/**
 * Corpse mod hooks for ancient corpses (feature_writeup/plan_corpses.md 3.2, rulings 5, 21, 22). Loaded only with
 * the Corpse mod ({@link AncientCorpseEvents#registerCorpseListeners}).
 * <ul>
 * <li>First open: the table in {@link AncientCorpseFeature#LOOT_KEY} is rolled as chest loot (so the LootJS layer
 * applies) into the corpse's main inventory, overflow into its additional items; the key and Compat's sealed mark are
 * cleared, so whoever opens first takes it and the corpse then follows the empty rules.</li>
 * <li>Remains ({@link AncientCorpseFeature#REMAINS_KEY}): a player's hit drops {@code remains.drops} at the corpse and
 * removes it.
 * No other corpse can be broken.</li>
 * </ul>
 */
public final class CorpseHooks {
	private CorpseHooks() {}

	static void onInteract(PlayerInteractEvent.EntityInteract event) {
		if (!(event.getTarget() instanceof CorpseEntity corpse) || !(corpse.level instanceof ServerLevel level)) return;
		CompoundTag data = corpse.getPersistentData();
		if (!data.contains(AncientCorpseFeature.LOOT_KEY)) return;
		String table = data.getString(AncientCorpseFeature.LOOT_KEY);
		data.remove(AncientCorpseFeature.LOOT_KEY);
		data.remove(CorpseState.SEALED);
		List<ItemStack> items = roll(level, corpse, event.getEntity(), table);
		NonNullList<ItemStack> main = corpse.getDeath().getMainInventory();
		for (ItemStack stack : items) {
			if (stack.isEmpty()) continue;
			int free = -1;
			for (int i = 0; i < main.size(); i++) {
				if (main.get(i).isEmpty()) {
					free = i;
					break;
				}
			}
			if (free >= 0) main.set(free, stack);
			else corpse.getDeath().getAdditionalItems().add(stack);
		}
	}

	static void onAttack(AttackEntityEvent event) {
		if (!(event.getTarget() instanceof CorpseEntity corpse) || !(corpse.level instanceof ServerLevel level)) return;
		CompoundTag data = corpse.getPersistentData();
		if (!data.getBoolean(AncientCorpseFeature.REMAINS_KEY)) return;
		event.setCanceled(true);
		if (corpse.isRemoved()) return;
		for (AncientCorpseConfig.Drop d : AncientCorpseConfig.get().remains.drops()) {
			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(d.item()));
			if (item == null || item == Items.AIR) continue;
			if (d.chance() < 1.0 && level.random.nextDouble() >= d.chance()) continue;
			int count = d.min() + level.random.nextInt(Math.max(1, d.max() - d.min() + 1));
			if (count > 0) Containers.dropItemStack(level, corpse.getX(), corpse.getY() + 0.25, corpse.getZ(), new ItemStack(item, count));
		}
		level.playSound(null, corpse.getX(), corpse.getY(), corpse.getZ(), SoundEvents.SKELETON_DEATH, SoundSource.BLOCKS, 1.0F, 0.8F);
		corpse.discard();
	}

	private static List<ItemStack> roll(ServerLevel level, CorpseEntity corpse, Player player, String id) {
		LootTable table = level.getServer().getLootTables().get(new ResourceLocation(id));
		LootContext ctx = new LootContext.Builder(level)
				.withParameter(LootContextParams.ORIGIN, corpse.position())
				.withParameter(LootContextParams.THIS_ENTITY, player)
				.withLuck(player.getLuck())
				.withRandom(level.random)
				.create(LootContextParamSets.CHEST);
		return table.getRandomItems(ctx);
	}
}
