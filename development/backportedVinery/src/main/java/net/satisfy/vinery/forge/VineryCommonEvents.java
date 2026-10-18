package net.satisfy.vinery.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.VineryToolActions;
import net.satisfy.vinery.core.command.VineryCommands;
import net.satisfy.vinery.core.command.WineCommands;
import net.satisfy.vinery.core.event.EventHandler;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.core.wine.VintageClock;

import java.util.Map;
import java.util.Set;

/** Forge-bus events of the port. */
public final class VineryCommonEvents {
    /** D3 / W9: 1.3.10's improved effects are gone; active instances become the vanilla effect. */
    private static final Map<String, MobEffect> IMPROVED_EFFECTS = Map.of(
            "improved_speed", MobEffects.MOVEMENT_SPEED,
            "improved_jump_boost", MobEffects.JUMP,
            "improved_strength", MobEffects.DAMAGE_BOOST,
            "improved_instant_health", MobEffects.HEAL,
            "improved_regeneration", MobEffects.REGENERATION,
            "improved_fire_resistance", MobEffects.FIRE_RESISTANCE,
            "improved_water_breathing", MobEffects.WATER_BREATHING,
            "improved_night_vision", MobEffects.NIGHT_VISION,
            "improved_haste", MobEffects.DIG_SPEED,
            "improved_absorbtion", MobEffects.ABSORPTION);

    private VineryCommonEvents() {
    }

    public static void register(IEventBus bus) {
        bus.addListener((TickEvent.ServerTickEvent event) -> {
            if (event.phase == TickEvent.Phase.END) {
                VintageClock.tick(event.getServer());
            }
        });
        bus.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                VintageClock.sync(player);
            }
        });
        bus.addListener(VineryVillagers::onVillagerTrades);
        bus.addListener(VineryToolActions::onToolModification);
        bus.addListener(VineryCommonEvents::onFuel);
        bus.addListener(VineryCommonEvents::onAttack);
        bus.addListener(VineryCommonEvents::onMissingMappings);
        bus.addListener((RegisterCommandsEvent event) -> {
            WineCommands.register(event.getDispatcher());
            VineryCommands.register(event.getDispatcher());
        });
    }

    private static void onAttack(AttackEntityEvent event) {
        if (EventHandler.onAttack(event.getEntity(), event.getEntity().level, event.getTarget())) {
            event.setCanceled(true);
        }
    }

    /** Upstream's FuelRegistry entries (1000 ticks). */
    private static void onFuel(FurnaceFuelBurnTimeEvent event) {
        Item item = event.getItemStack().getItem();
        if (item == ObjectRegistry.DARK_CHERRY_FENCE.get().asItem() || item == ObjectRegistry.DARK_CHERRY_FENCE_GATE.get().asItem()
                || item == ObjectRegistry.STACKABLE_LOG.get().asItem() || item == ObjectRegistry.FERMENTATION_BARREL.get().asItem()) {
            event.setBurnTime(1000);
        }
    }

    private static void onMissingMappings(MissingMappingsEvent event) {
        for (MissingMappingsEvent.Mapping<MobEffect> mapping : event.getMappings(ForgeRegistries.Keys.MOB_EFFECTS, Vinery.MOD_ID)) {
            MobEffect replacement = IMPROVED_EFFECTS.get(mapping.getKey().getPath());
            if (replacement != null) {
                mapping.remap(replacement);
            }
        }
        // W10: 1.3.10's seat entity; whoever sat stands up
        for (MissingMappingsEvent.Mapping<?> mapping : event.getMappings(ForgeRegistries.Keys.ENTITY_TYPES, Vinery.MOD_ID)) {
            if (Set.of("chair").contains(mapping.getKey().getPath())) {
                mapping.ignore();
            }
        }
    }

    @SuppressWarnings("unused")
    private static ResourceLocation id(String path) {
        return Vinery.identifier(path);
    }
}
