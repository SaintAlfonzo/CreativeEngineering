package com.SaintAlfonzo.more_mooblooms.compat;

import com.SaintAlfonzo.more_mooblooms.MMConfig;
import com.SaintAlfonzo.more_mooblooms.MoreMooblooms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.village.WandererTradesEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The buzzier_bees content set (O14, feature_writeup/mooblooms.md 4.11): only active when Buzzier Bees is
 * installed. Removes its moobloom, buttercup and the Sunny effect itself (disableSunny), from outside: Buzzier Bees is under the
 * Abnormals License, so this mod contains nothing of it, doesn't mixin into it and doesn't compile against it.
 * Everything is by registry id. One config switch per change (MMConfig.BuzzierBees).
 *
 * Data-side changes are sub-packs of {@code packs/buzzier_bees/<switch>/}, added only when their switch is on
 * ({@link #dataSwitches}, MoreMooblooms.addPacks). Code-side changes are here and in mixin/ItemHiddenMixin,
 * mixin/TagLoaderStripMixin and mixin/MobSunBurnMixin.
 */
public final class BuzzierBeesSet {

    public static final String MOD_ID = "buzzier_bees";

    private static final ResourceLocation MOOBLOOM = new ResourceLocation(MOD_ID, "moobloom");
    private static final ResourceLocation MOOBLOOM_SPAWN_EGG = new ResourceLocation(MOD_ID, "moobloom_spawn_egg");
    private static final ResourceLocation BUTTERCUP = new ResourceLocation(MOD_ID, "buttercup");
    private static final ResourceLocation POTTED_BUTTERCUP = new ResourceLocation(MOD_ID, "potted_buttercup");
    public static final ResourceLocation SUNNY = new ResourceLocation(MOD_ID, "sunny");

    /** Sub-pack folder under packs/buzzier_bees/ -> the switch that turns it on. */
    public record DataSwitch(String folder, ForgeConfigSpec.BooleanValue value) {}

    public static List<DataSwitch> dataSwitches() {
        return List.of(
                new DataSwitch("disable_moobloom_spawns", MMConfig.BuzzierBees.DISABLE_MOOBLOOM_SPAWNS),
                new DataSwitch("restore_cow_spawns", MMConfig.BuzzierBees.RESTORE_COW_SPAWNS),
                new DataSwitch("empty_hostiles_tag", MMConfig.BuzzierBees.EMPTY_HOSTILES_TAG),
                new DataSwitch("replace_two_by_two", MMConfig.BuzzierBees.REPLACE_TWO_BY_TWO),
                new DataSwitch("remove_sunny_from_all_effects", MMConfig.BuzzierBees.REMOVE_SUNNY_FROM_ALL_EFFECTS),
                new DataSwitch("remove_buttercup_worldgen", MMConfig.BuzzierBees.REMOVE_BUTTERCUP_WORLDGEN),
                new DataSwitch("remove_buttercup_recipes", MMConfig.BuzzierBees.REMOVE_BUTTERCUP_RECIPES));
    }

    private static final AtomicInteger removedMooblooms = new AtomicInteger();

    public static void register(IEventBus modBus) {
        modBus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(BuzzierBeesSet::removeCompost));
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, BuzzierBeesSet::onEntityJoin);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, BuzzierBeesSet::onWandererTrades);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, BuzzierBeesSet::onEffectApplicable);
        MinecraftForge.EVENT_BUS.addListener(BuzzierBeesSet::stripSunnyOnJoin);
        // Again at server start: anything that re-registered the compost value after setup loses it again.
        MinecraftForge.EVENT_BUS.addListener((ServerAboutToStartEvent e) -> removeCompost());
    }

    // ---- creative / JEI hiding ----

    public static boolean isHidden(Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id == null || !id.getNamespace().equals(MOD_ID)) return false;
        return (id.equals(BUTTERCUP) && MMConfig.on(MMConfig.BuzzierBees.HIDE_BUTTERCUP))
                || (id.equals(MOOBLOOM_SPAWN_EGG) && MMConfig.on(MMConfig.BuzzierBees.HIDE_SPAWN_EGG));
    }

    /** Hidden items, for the JEI plugin. */
    public static List<Item> hiddenItems() {
        return ForgeRegistries.ITEMS.getValues().stream().filter(BuzzierBeesSet::isHidden).toList();
    }

    // ---- tags ----

    /** Element ids TagLoaderStripMixin takes out of every tag in this tag directory. */
    public static Set<ResourceLocation> tagRemovals(String directory) {
        if (!MMConfig.on(MMConfig.BuzzierBees.REMOVE_BUTTERCUP_TAGS)) return Set.of();
        return switch (directory) {
            case "tags/items" -> Set.of(BUTTERCUP);
            case "tags/blocks" -> Set.of(BUTTERCUP, POTTED_BUTTERCUP);
            default -> Set.of();
        };
    }

    public static void logStripped(String directory, Set<ResourceLocation> removed, int tags) {
        MoreMooblooms.LOGGER.info("[more_mooblooms] removed {} from {} {} tags", removed, tags, directory);
    }

    // ---- Buzzier Bees mooblooms (D25: they shouldn't be in the game at all) ----

    private static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) return;
        if (!MOOBLOOM.equals(ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType()))) return;
        if (!MMConfig.on(MMConfig.BuzzierBees.REMOVE_MOOBLOOMS)) return;
        // Cancelling keeps it out of the world: a new one is never added, and one loaded from disk isn't added
        // to its chunk, so it is gone the next time the chunk saves.
        event.setCanceled(true);
        int n = removedMooblooms.incrementAndGet();
        if (n <= 5 || Integer.bitCount(n) == 1) {
            MoreMooblooms.LOGGER.info("[more_mooblooms] removed a Buzzier Bees moobloom at {} ({} so far)",
                    event.getEntity().blockPosition(), n);
        }
    }

    // ---- Sunny off (R4, 4.11) ----

    @Nullable
    private static MobEffect sunny() {
        return ForgeRegistries.MOB_EFFECTS.getValue(SUNNY);
    }

    private static boolean isSunny(MobEffect effect) {
        return SUNNY.equals(ForgeRegistries.MOB_EFFECTS.getKey(effect));
    }

    /** Nothing can apply Sunny: potions, stews, mobs and {@code /effect give} ("Unable to apply"). */
    private static void onEffectApplicable(MobEffectEvent.Applicable event) {
        if (MMConfig.on(MMConfig.BuzzierBees.DISABLE_SUNNY) && isSunny(event.getEffectInstance().getEffect())) {
            event.setResult(Event.Result.DENY);
        }
    }

    /** Sunny given before the switch was on: players at login, mobs loaded from disk. */
    private static void stripSunnyOnJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof LivingEntity living)) return;
        if (!MMConfig.on(MMConfig.BuzzierBees.DISABLE_SUNNY)) return;
        MobEffect effect = sunny();
        if (effect != null && living.hasEffect(effect)) {
            living.removeEffect(effect);
            MoreMooblooms.LOGGER.info("[more_mooblooms] removed Sunny from {} ({})", living.getName().getString(), living.getUUID());
        }
    }

    /** For mixin/MobSunBurnMixin: true when Buzzier Bees' Sunny burn check should be skipped for this mob. */
    public static boolean blocksSunnyBurn(Mob mob) {
        if (!MMConfig.on(MMConfig.BuzzierBees.DISABLE_SUNNY)) return false;
        MobEffect effect = sunny();
        return effect != null && mob.hasEffect(effect);
    }

    // ---- buttercup sources outside data ----

    private static void onWandererTrades(WandererTradesEvent event) {
        if (!MMConfig.on(MMConfig.BuzzierBees.REMOVE_BUTTERCUP_TRADE)) return;
        int removed = removeButtercupListings(event.getGenericTrades()) + removeButtercupListings(event.getRareTrades());
        MoreMooblooms.LOGGER.info("[more_mooblooms] removed {} buttercup Wandering Trader listing(s)", removed);
    }

    private static int removeButtercupListings(List<VillagerTrades.ItemListing> listings) {
        int removed = 0;
        RandomSource random = RandomSource.create(0);
        for (Iterator<VillagerTrades.ItemListing> it = listings.iterator(); it.hasNext(); ) {
            VillagerTrades.ItemListing listing = it.next();
            MerchantOffer offer;
            try {
                offer = listing.getOffer(null, random);
            } catch (RuntimeException e) {
                continue; // a listing that needs a real trader: not a plain buttercup sale
            }
            if (offer != null && BUTTERCUP.equals(ForgeRegistries.ITEMS.getKey(offer.getResult().getItem()))) {
                it.remove();
                removed++;
            }
        }
        return removed;
    }

    private static void removeCompost() {
        if (!MMConfig.on(MMConfig.BuzzierBees.REMOVE_BUTTERCUP_COMPOST)) return;
        Item buttercup = ForgeRegistries.ITEMS.getValue(BUTTERCUP);
        if (buttercup == null) return;
        ComposterBlock.COMPOSTABLES.removeFloat((ItemLike) buttercup);
    }

    private BuzzierBeesSet() {}
}
