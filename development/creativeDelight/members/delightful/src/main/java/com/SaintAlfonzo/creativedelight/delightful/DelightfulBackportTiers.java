package com.SaintAlfonzo.creativedelight.delightful;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;

/** The three backported knives' tiers, 3.8.1's DelightfulTiers values (pack tuning, if any, goes through tuneTools). */
public enum DelightfulBackportTiers implements Tier {
	ALUMINUM(2, 220, 6.0F, 2.0F, 22, "forge:ingots/aluminum"),
	CERTUS_QUARTZ(Tiers.IRON.getLevel(), Tiers.IRON.getUses(), Tiers.IRON.getSpeed(), Tiers.IRON.getAttackDamageBonus(),
			Tiers.IRON.getEnchantmentValue(), "forge:gems/certus_quartz"),
	FLUIX(Tiers.IRON.getLevel(), Tiers.IRON.getUses() * 3, Tiers.IRON.getSpeed() * 1.2F, Tiers.IRON.getAttackDamageBonus() * 1.2F,
			Tiers.IRON.getEnchantmentValue(), "forge:gems/fluix");

	private final int level;
	private final int uses;
	private final float speed;
	private final float damage;
	private final int enchantmentValue;
	private final LazyLoadedValue<Ingredient> repair;

	DelightfulBackportTiers(int level, int uses, float speed, float damage, int enchantmentValue, String repairTag) {
		this.level = level;
		this.uses = uses;
		this.speed = speed;
		this.damage = damage;
		this.enchantmentValue = enchantmentValue;
		this.repair = new LazyLoadedValue<>(() -> Ingredient.of(ItemTags.create(new ResourceLocation(repairTag))));
	}

	@Override public int getLevel() { return level; }
	@Override public int getUses() { return uses; }
	@Override public float getSpeed() { return speed; }
	@Override public float getAttackDamageBonus() { return damage; }
	@Override public int getEnchantmentValue() { return enchantmentValue; }
	@Override public Ingredient getRepairIngredient() { return repair.get(); }
}
