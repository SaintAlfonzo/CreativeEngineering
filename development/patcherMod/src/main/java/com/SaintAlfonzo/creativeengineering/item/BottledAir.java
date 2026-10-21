package com.SaintAlfonzo.creativeengineering.item;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Bottled Air port (feature_writeup/feature_porting.md 2.1, jar retired 2026-10-20). Right-clicking a glass bottle while
 * in water refills {@code airPerBottle} air (150 of 300); vanilla then fills the bottle with water as usual. With a
 * fire source ({@code #creativeengineering:bottled_air/fire_sources}: campfires, fire charge, lava bucket, magma
 * block) in the other hand, one water bottle is taken back out of the inventory and "evaporated": the glass bottle
 * comes back unless it breaks ({@code bottleBreakChance}). Drinking a water bottle while under water is blocked
 * ({@code noDrinkingUnderwater}), so the bottle can't be an endless air supply. Server and client both run the event;
 * only the server changes anything.
 */
public final class BottledAir {
    public static final TagKey<Item> FIRE_SOURCES =
            ItemTags.create(new ResourceLocation(CreativeEngineering.MODID, "bottled_air/fire_sources"));

    private BottledAir() {}

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        CEConfig.BottledAir cfg = CEConfig.BOTTLED_AIR;
        if (!cfg.enabled.get()) return;
        Player player = event.getEntity();
        if (!player.isInWater()) return;
        ItemStack stack = event.getItemStack();
        if (isWaterBottle(stack)) {
            if (cfg.noDrinkingUnderwater.get() && player.isUnderWater()) event.setCanceled(true);
            return;
        }
        if (!stack.is(Items.GLASS_BOTTLE) || player.level.isClientSide) return;
        int max = player.getMaxAirSupply();
        int air = player.getAirSupply();
        if (air >= max) return;
        player.setAirSupply(Math.min(max, air + cfg.airPerBottle.get()));
        InteractionHand other = event.getHand() == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (!cfg.fireEvaporates.get() || !player.getItemInHand(other).is(FIRE_SOURCES)) return;
        Inventory inv = player.getInventory();
        for (int slot = Inventory.INVENTORY_SIZE - 1; slot > 0; slot--) {
            ItemStack held = inv.getItem(slot);
            if (isWaterBottle(held)) {
                held.shrink(1);
                if (player.getRandom().nextDouble() > cfg.bottleBreakChance.get()) {
                    ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                    if (!inv.add(bottle)) player.drop(bottle, false);
                }
                break;
            }
        }
    }

    private static boolean isWaterBottle(ItemStack stack) {
        return stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER;
    }
}
