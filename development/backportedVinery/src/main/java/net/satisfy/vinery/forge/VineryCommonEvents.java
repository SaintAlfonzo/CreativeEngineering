package net.satisfy.vinery.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.block.WineBoxBlock;
import net.satisfy.vinery.core.VineryToolActions;
import net.satisfy.vinery.core.command.VineryCommands;
import net.satisfy.vinery.core.command.WineCommands;
import net.satisfy.vinery.core.event.EventHandler;
import net.satisfy.vinery.core.registry.ObjectRegistry;
import net.satisfy.vinery.core.wine.VintageClock;

/** Forge-bus events of the port. */
public final class VineryCommonEvents {
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
        bus.addListener(WinemakerSpawner::onServerStarted);
        bus.addListener(WinemakerSpawner::onLevelTick);
        bus.addListener(VineryToolActions::onToolModification);
        bus.addListener(VineryCommonEvents::onFuel);
        bus.addListener(VineryCommonEvents::onAttack);
        bus.addListener(VineryCommonEvents::onRightClickBlock);
        bus.addListener(net.satisfy.vinery.forge.upstream.VillageHouses::onServerAboutToStart);
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

    /** alcohol/vineryport #18: a sneaking player with something in hand still reaches the wine box (WineBoxBlock#useItemOn). */
    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity().isShiftKeyDown() && event.getLevel().getBlockState(event.getPos()).getBlock() instanceof WineBoxBlock) {
            event.setUseBlock(Event.Result.ALLOW);
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

    @SuppressWarnings("unused")
    private static ResourceLocation id(String path) {
        return Vinery.identifier(path);
    }
}
