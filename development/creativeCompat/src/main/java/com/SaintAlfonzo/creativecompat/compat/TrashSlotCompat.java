package com.SaintAlfonzo.creativecompat.compat;

import com.SaintAlfonzo.creativecompat.CompatConfig;

import com.SaintAlfonzo.creativecompat.mixin.TrashSlotSaveStateAccessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.blay09.mods.trashslot.TrashSlotSaveState;
import net.blay09.mods.trashslot.api.IGuiContainerLayout;
import net.blay09.mods.trashslot.api.TrashSlotAPI;
import net.blay09.mods.trashslot.client.ContainerSettings;
import net.blay09.mods.trashslot.client.TrashSlotSlot;
import net.blay09.mods.trashslot.client.deletion.DeletionProvider;
import net.blay09.mods.trashslot.client.gui.layout.SimpleGuiContainerLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Pack defaults for TrashSlot (client only). TrashSlot keeps one {@link ContainerSettings} per
 * screen class in {@code TrashSlotSaveState.json} and only switches the slot on for the player
 * inventory, crafting table and chests. This class replaces its {@code getSettings} call (see
 * {@code mixin.TrashSlotGuiHandlerMixin}) so that:
 * <ul>
 *   <li>screens in {@code [trash_slot] enabledScreens} start with the slot on, and an existing
 *       save-state entry gets switched on once (recorded in {@value #APPLIED_FILE}), so a later
 *       toggle-key press by the player still sticks;</li>
 *   <li>{@code positions} gives a screen its own anchor and offset on first open;</li>
 *   <li>Sophisticated Backpacks keep one entry per backpack tier ({@link StorageTierLayout}).</li>
 * </ul>
 * Also holds {@link #CREATIVE}, the deletion used in the creative inventory screen.
 */
public final class TrashSlotCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String APPLIED_FILE = "TrashSlotSaveState.ce.json";
    private static final String BACKPACK_SCREEN = "net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen";

    /** Container ids the pack has already switched on once on this client. */
    @Nullable
    private static TreeSet<String> applied;

    private TrashSlotCompat() {}

    /** Called from client setup when TrashSlot is loaded. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register() {
        try {
            Class cls = Class.forName(BACKPACK_SCREEN, false, TrashSlotCompat.class.getClassLoader());
            TrashSlotAPI.registerLayout(cls, new StorageTierLayout());
        } catch (ClassNotFoundException e) {
            LOGGER.debug("Sophisticated Backpacks not present; no per-tier trash slot layout");
        }
    }

    /** Replacement for {@code TrashSlotSaveState.getSettings(screen, layout)}. */
    public static ContainerSettings settingsFor(AbstractContainerScreen<?> screen, IGuiContainerLayout layout) {
        String id = layout.getContainerId(screen);
        String cls = screen.getClass().getName();
        boolean packOn = isPackEnabled(cls);
        Map<String, ContainerSettings> cached =
                ((TrashSlotSaveStateAccessor) TrashSlotSaveState.getInstance()).creativeengineering$getCachedSettings();

        ContainerSettings settings = cached.get(id);
        boolean changed = false;
        if (settings == null) {
            boolean on = packOn || layout.isEnabledByDefault();
            float[] pos = position(cls);
            settings = pos != null
                    ? new ContainerSettings((int) pos[2], (int) pos[3], pos[0], pos[1], on)
                    : new ContainerSettings(layout.getDefaultSlotX(screen), layout.getDefaultSlotY(screen), 0.5F, 0.5F, on);
            cached.put(id, settings);
            changed = true;
        } else if (packOn && !settings.isEnabled() && !applied().contains(id)) {
            settings.setEnabled(true);
            changed = true;
        }
        if (packOn && applied().add(id)) {
            saveApplied();
        }
        if (changed) {
            TrashSlotSaveState.save();
        }
        return settings;
    }

    private static boolean isPackEnabled(String cls) {
        CompatConfig.TrashSlot cfg = CompatConfig.TRASH_SLOT;
        if (cfg.enabledScreens.get().contains(cls)) return true;
        return cfg.allContainers.get() && !cfg.excludedScreens.get().contains(cls);
    }

    /** {anchorX, anchorY, slotX, slotY} from {@code positions}, or null for TrashSlot's default spot. */
    @Nullable
    private static float[] position(String cls) {
        for (String line : CompatConfig.TRASH_SLOT.positions.get()) {
            String[] parts = line.split("\\|");
            if (parts.length != 5 || !parts[0].trim().equals(cls)) continue;
            try {
                return new float[]{
                        Float.parseFloat(parts[1].trim()), Float.parseFloat(parts[2].trim()),
                        Integer.parseInt(parts[3].trim()), Integer.parseInt(parts[4].trim())};
            } catch (NumberFormatException e) {
                LOGGER.warn("[trash_slot] positions: bad entry '{}' (want class|anchorX|anchorY|x|y)", line);
            }
        }
        return null;
    }

    private static File appliedFile() {
        return new File(Minecraft.getInstance().gameDirectory, APPLIED_FILE);
    }

    private static TreeSet<String> applied() {
        if (applied == null) {
            applied = new TreeSet<>();
            File file = appliedFile();
            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    if (json != null && json.has("switchedOn")) {
                        json.getAsJsonArray("switchedOn").forEach(e -> applied.add(e.getAsString()));
                    }
                } catch (Exception e) {
                    LOGGER.warn("Couldn't read {}; pack trash slot defaults will be applied again", APPLIED_FILE, e);
                }
            }
        }
        return applied;
    }

    private static void saveApplied() {
        JsonObject json = new JsonObject();
        json.addProperty("_comment", "Screens the pack has switched the trash slot on for once (patcherMod TrashSlotCompat).");
        json.add("switchedOn", GSON.toJsonTree(List.copyOf(applied())));
        try (FileWriter writer = new FileWriter(appliedFile())) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            LOGGER.warn("Couldn't write {}", APPLIED_FILE, e);
        }
    }

    /**
     * Sophisticated storage screens: the container id gets the storage item's id appended
     * ({@code ...BackpackScreen#sophisticatedbackpacks:iron_backpack}), so each tier keeps its own
     * position and on/off state. Placed backpacks open the same screen with the same item, so they
     * share their tier's entry.
     */
    public static class StorageTierLayout extends SimpleGuiContainerLayout {
        public StorageTierLayout() {
            enableDefaultCollision();
            enableDefaultSnaps();
        }

        @Override
        public String getContainerId(AbstractContainerScreen<?> screen) {
            String base = super.getContainerId(screen);
            if (!CompatConfig.TRASH_SLOT.splitBackpacksByTier.get()) return base;
            if (screen.getMenu() instanceof StorageContainerMenuBase<?> menu) {
                ItemStack stack = menu.getStorageWrapper().getWrappedStorageStack();
                ResourceLocation key = stack.isEmpty() ? null : ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (key != null) return base + "#" + key;
            }
            return base;
        }
    }

    /**
     * Deletion in the creative inventory screen. Creative is client-authoritative: picking an item
     * up there already removed it from the server's inventory and the cursor stack is never sent
     * to the server, so deleting it only clears the cursor (what vanilla's own destroy slot does).
     * Nothing is kept for undo. TrashSlot's default provider would instead ask the server to trash
     * its own cursor stack, which in creative is empty.
     */
    public static final DeletionProvider CREATIVE = new DeletionProvider() {
        @Override
        public void undeleteLast(Player player, TrashSlotSlot trashSlot, boolean isRightClick) {
        }

        @Override
        public boolean canUndeleteLast() {
            return false;
        }

        @Override
        public void deleteMouseItem(Player player, ItemStack mouseItem, TrashSlotSlot trashSlot, boolean isRightClick) {
            if (!(Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen)) return;
            AbstractContainerMenu menu = screen.getMenu();
            ItemStack rest = ItemStack.EMPTY;
            if (isRightClick) {
                rest = mouseItem.copy();
                rest.shrink(1);
            }
            menu.setCarried(rest);
        }

        @Override
        public void deleteContainerItem(AbstractContainerMenu container, int slotNumber, boolean isDeleteAll, TrashSlotSlot trashSlot) {
            // The creative screen's hovered-slot delete keys go through TrashSlot's own creative path.
        }

        @Override
        public void emptyTrashSlot(TrashSlotSlot trashSlot) {
        }
    };
}
