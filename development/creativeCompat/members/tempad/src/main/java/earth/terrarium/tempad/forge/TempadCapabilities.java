// Port glue (ours, GPLv3), TP-D5: the capability registrations of Tempad 3.0.4 (MIT, Terrarium; Tempad.kt
// RegisterCapabilitiesEvent block, and the player attachments of ModAttachments.kt) on Forge 1.19.2.
// Items: AttachCapabilitiesEvent<ItemStack> with one provider per item, built from the same table upstream fills;
// every query builds a fresh handler over the stack's NBT, as upstream's providers do. Players: TempadPlayerData,
// saved with the player and copied on respawn and on the End-exit clone (pinned_location and travel_history are
// copyOnDeath upstream).
package earth.terrarium.tempad.forge;

import earth.terrarium.tempad.api.player_access.DefaultAccess;
import earth.terrarium.tempad.api.player_access.PlayerAccess;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.api.tva_device.UpgradeHandler;
import earth.terrarium.tempad.api.tva_device.impl.InfiniteChrononHandler;
import earth.terrarium.tempad.api.tva_device.impl.ItemChrononHandler;
import earth.terrarium.tempad.api.tva_device.impl.ItemUpgradeHandler;
import earth.terrarium.tempad.api.tva_device.impl.RudimentaryUpgradeHandler;
import earth.terrarium.tempad.api.tva_device.impl.TempadChrononHandler;
import earth.terrarium.tempad.common.config.CommonConfigCache;
import earth.terrarium.tempad.common.data.TempadPlayerData;
import earth.terrarium.tempad.common.items.ScreeningDeviceAccess;
import earth.terrarium.tempad.common.items.WalletInventory;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class TempadCapabilities {
    private static final ResourceLocation ITEM_KEY = new ResourceLocation("tempad", "device");

    private static final Map<Item, Function<ItemStack, ChrononHandler>> CHRONON_ITEMS = new HashMap<>();
    private static final Map<Item, Function<ItemStack, UpgradeHandler>> UPGRADE_ITEMS = new HashMap<>();
    private static final Map<Item, Function<ItemStack, PlayerAccess>> ACCESS_ITEMS = new HashMap<>();
    private static final Map<Item, Function<ItemStack, IItemHandler>> ITEM_ITEMS = new HashMap<>();

    private TempadCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ChrononHandler.class);
        event.register(UpgradeHandler.class);
        event.register(PlayerAccess.class);
        event.register(TempadPlayerData.class);
    }

    private static volatile boolean filled;

    /** The upstream table (phase 1 items), built on the first stack created after the items are registered. */
    private static synchronized void fillTables() {
        if (filled || !ModItems.tempad.isPresent()) return;
        CHRONON_ITEMS.put(ModItems.tempad.get(), stack ->
                TempadChrononHandler.create(stack, CommonConfigCache.Tempad.capacity(), CommonConfigCache.TimeTwister.capacity()));
        CHRONON_ITEMS.put(ModItems.chrononCell.get(), stack -> ItemChrononHandler.create(stack, CommonConfigCache.ChrononCell.capacity()));
        CHRONON_ITEMS.put(ModItems.chrononBattery.get(), stack -> ItemChrononHandler.create(stack, CommonConfigCache.Battery.capacity()));
        CHRONON_ITEMS.put(ModItems.chronometer.get(), stack -> {
            ItemChrononHandler handler = ItemChrononHandler.create(stack, CommonConfigCache.Chronometer.capacity());
            if (handler != null) handler.canInsert = false;
            return handler;
        });
        CHRONON_ITEMS.put(ModItems.chrononGenerator.get(), stack -> {
            ItemChrononHandler handler = ItemChrononHandler.create(stack, CommonConfigCache.ChrononGenerator.capacity());
            if (handler != null) handler.canInsert = false;
            return handler;
        });
        CHRONON_ITEMS.put(ModItems.timedoorProjector.get(), stack -> {
            ItemChrononHandler handler = ItemChrononHandler.create(stack, CommonConfigCache.RudimentaryTempad.capacity());
            if (handler != null) handler.canExtract = false;
            return handler;
        });
        CHRONON_ITEMS.put(ModItems.timeTwister.get(), stack -> {
            ItemChrononHandler handler = ItemChrononHandler.create(stack, CommonConfigCache.TimeTwister.capacity());
            if (handler != null) handler.canExtract = false;
            return handler;
        });
        CHRONON_ITEMS.put(ModItems.metronome.get(), stack -> ItemChrononHandler.create(stack, CommonConfigCache.Metronome.capacity()));
        CHRONON_ITEMS.put(ModItems.creativeChronometer.get(), stack -> InfiniteChrononHandler.INSTANCE);
        UPGRADE_ITEMS.put(ModItems.tempad.get(), ItemUpgradeHandler::new);
        UPGRADE_ITEMS.put(ModItems.timedoorProjector.get(), stack -> RudimentaryUpgradeHandler.INSTANCE);
        ACCESS_ITEMS.put(ModItems.locationBroadcaster.get(), stack -> ModComponents.isEnabled(stack) ? DefaultAccess.Public : null);
        ACCESS_ITEMS.put(ModItems.screeningDevice.get(), ScreeningDeviceAccess::create);
        ITEM_ITEMS.put(ModItems.cardWallet.get(), WalletInventory::of);
        // Block entities (workstation, timedoor_projector, metronome) serve their capabilities from getCapability.
        filled = true;
    }

    public static void attachItem(AttachCapabilitiesEvent<ItemStack> event) {
        if (!filled) fillTables();
        ItemStack stack = event.getObject();
        Item item = stack.getItem();
        Function<ItemStack, ChrononHandler> chronons = CHRONON_ITEMS.get(item);
        Function<ItemStack, UpgradeHandler> upgrades = UPGRADE_ITEMS.get(item);
        Function<ItemStack, PlayerAccess> access = ACCESS_ITEMS.get(item);
        Function<ItemStack, IItemHandler> itemHandler = ITEM_ITEMS.get(item);
        if (chronons == null && upgrades == null && access == null && itemHandler == null) return;
        event.addCapability(ITEM_KEY, new ICapabilityProvider() {
            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == ChrononHandler.CAPABILITY && chronons != null) return optional(cap, chronons.apply(stack));
                if (cap == UpgradeHandler.CAPABILITY && upgrades != null) return optional(cap, upgrades.apply(stack));
                if (cap == PlayerAccess.CAPABILITY && access != null) return optional(cap, access.apply(stack));
                if (cap == ForgeCapabilities.ITEM_HANDLER && itemHandler != null) return optional(cap, itemHandler.apply(stack));
                return LazyOptional.empty();
            }
        });
    }

    private static <T, H> LazyOptional<T> optional(Capability<T> cap, @Nullable H handler) {
        if (handler == null) return LazyOptional.empty();
        return LazyOptional.of(() -> handler).cast();
    }

    public static void attachEntity(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) return;
        TempadPlayerData data = new TempadPlayerData();
        LazyOptional<TempadPlayerData> optional = LazyOptional.of(() -> data);
        event.addCapability(TempadPlayerData.ID, new ICapabilitySerializable<CompoundTag>() {
            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                return cap == TempadPlayerData.CAPABILITY ? optional.cast() : LazyOptional.empty();
            }

            @Override
            public CompoundTag serializeNBT() {
                return data.save();
            }

            @Override
            public void deserializeNBT(CompoundTag tag) {
                data.load(tag);
            }
        });
    }

    public static void clone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        original.reviveCaps();
        original.getCapability(TempadPlayerData.CAPABILITY).ifPresent(old ->
                event.getEntity().getCapability(TempadPlayerData.CAPABILITY).ifPresent(fresh -> fresh.copyFrom(old)));
        original.invalidateCaps();
    }
}
