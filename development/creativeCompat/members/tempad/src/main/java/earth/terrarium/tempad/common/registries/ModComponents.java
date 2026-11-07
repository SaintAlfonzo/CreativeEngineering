// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/registries/ModComponents.kt.
// Translated to Java for Forge 1.19.2 (TP-D5): each data component -> one key in the stack's NBT under the component's
// upstream name (default_app, chronon_content, ...), read and written through these accessors with the upstream
// defaults (withDefault). Setting a value to its "absent" form removes the key, as setting a component to null does.
// Carries every upstream component; twister_data (a DataComponentPatch upstream) is the time twister stack's NBT tag.
package earth.terrarium.tempad.common.registries;

import com.mojang.authlib.GameProfile;
import earth.terrarium.tempad.api.locations.IndirectLocation;
import earth.terrarium.tempad.api.locations.LocationGetter;
import earth.terrarium.tempad.common.data.InstalledUpgradesComponent;
import earth.terrarium.tempad.common.data.PortalPlacementComponent;
import earth.terrarium.tempad.common.utils.CodecUtils;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ModComponents {
    public static final String DEFAULT_APP = "default_app";
    public static final String DEFAULT_MACRO = "default_macro";
    public static final String CHRONON_CONTENT = "chronon_content";
    public static final String CHRONON_CONTENT_TEMPAD = "chronon_content_tempad";
    public static final String CHRONON_CONTENT_TIME_TWISTER = "chronon_content_time_twister";
    public static final String ENABLED = "enabled";
    public static final String TWISTER_EQUIPPED = "twister_equipped";
    public static final String INSTALLED_UPGRADES = "installed_upgrades";
    public static final String COLOR = "color";
    public static final String OWNER = "owner";
    public static final String PORTAL_OFFSET = "portal_offset";
    public static final String PORTAL_TARGET = "portal_target";
    public static final String SELECTED_PORTAL = "selected_portal";
    public static final String LOCKED = "locked";
    public static final String WALLET_CONTENTS = "wallet_contents";
    public static final String ACCESS_ID = "access_id";
    public static final String TWISTER_DATA = "twister_data";
    public static final String ANCHOR_ID = "anchor_id";

    private ModComponents() {
    }

    private static void remove(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        tag.remove(key);
        if (tag.isEmpty()) stack.setTag(null);
    }

    private static ResourceLocation getId(ItemStack stack, String key, ResourceLocation fallback) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(key, Tag.TAG_STRING)) return fallback;
        ResourceLocation id = ResourceLocation.tryParse(tag.getString(key));
        return id == null ? fallback : id;
    }

    private static int getInt(ItemStack stack, String key, int fallback) {
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains(key, Tag.TAG_INT) ? fallback : tag.getInt(key);
    }

    private static boolean getBool(ItemStack stack, String key, boolean fallback) {
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains(key, Tag.TAG_BYTE) ? fallback : tag.getBoolean(key);
    }

    // default_app (default ModApps.teleport)
    public static ResourceLocation getDefaultApp(ItemStack stack) {
        return getId(stack, DEFAULT_APP, ModApps.TELEPORT);
    }

    public static void setDefaultApp(ItemStack stack, ResourceLocation app) {
        stack.getOrCreateTag().putString(DEFAULT_APP, app.toString());
    }

    // default_macro (default ModMacros.teleportToPinned)
    public static ResourceLocation getDefaultMacro(ItemStack stack) {
        return getId(stack, DEFAULT_MACRO, ModMacros.TELEPORT_TO_PINNED);
    }

    public static void setDefaultMacro(ItemStack stack, ResourceLocation macro) {
        stack.getOrCreateTag().putString(DEFAULT_MACRO, macro.toString());
    }

    // chronon_content (default 0)
    public static int getChrononContent(ItemStack stack) {
        return getInt(stack, CHRONON_CONTENT, 0);
    }

    public static void setChrononContent(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(CHRONON_CONTENT, value);
    }

    // chronon_content_tempad (default 0)
    public static int getChrononContentTempad(ItemStack stack) {
        return getInt(stack, CHRONON_CONTENT_TEMPAD, 0);
    }

    public static void setChrononContentTempad(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(CHRONON_CONTENT_TEMPAD, value);
    }

    // chronon_content_time_twister (default 0)
    public static int getChrononContentTimeTwister(ItemStack stack) {
        return getInt(stack, CHRONON_CONTENT_TIME_TWISTER, 0);
    }

    public static void setChrononContentTimeTwister(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(CHRONON_CONTENT_TIME_TWISTER, value);
    }

    // enabled (default true)
    public static boolean isEnabled(ItemStack stack) {
        return getBool(stack, ENABLED, true);
    }

    public static void setEnabled(ItemStack stack, boolean value) {
        stack.getOrCreateTag().putBoolean(ENABLED, value);
    }

    // twister_equipped (default false)
    public static boolean isTwisterEquipped(ItemStack stack) {
        return getBool(stack, TWISTER_EQUIPPED, false);
    }

    public static void setTwisterEquipped(ItemStack stack, boolean value) {
        stack.getOrCreateTag().putBoolean(TWISTER_EQUIPPED, value);
    }

    // installed_upgrades (default empty) {upgrades: [id]}
    public static InstalledUpgradesComponent getInstalledUpgrades(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(INSTALLED_UPGRADES, Tag.TAG_COMPOUND)) return InstalledUpgradesComponent.EMPTY;
        return InstalledUpgradesComponent.load(tag.getCompound(INSTALLED_UPGRADES));
    }

    public static void setInstalledUpgrades(ItemStack stack, InstalledUpgradesComponent value) {
        stack.getOrCreateTag().put(INSTALLED_UPGRADES, value.save());
    }

    // color (no default)
    @Nullable
    public static Color getColor(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains(COLOR) ? null : Color.load(tag.get(COLOR));
    }

    public static void setColor(ItemStack stack, @Nullable Color color) {
        if (color == null) remove(stack, COLOR);
        else stack.getOrCreateTag().put(COLOR, color.save());
    }

    // owner (no default) {id, name}
    @Nullable
    public static GameProfile getOwner(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains(OWNER, Tag.TAG_COMPOUND) ? null : CodecUtils.loadGameProfile(tag.getCompound(OWNER));
    }

    public static void setOwner(ItemStack stack, @Nullable GameProfile owner) {
        if (owner == null) remove(stack, OWNER);
        else stack.getOrCreateTag().put(OWNER, CodecUtils.saveGameProfile(owner));
    }

    // portal_offset (default PortalPlacementComponent(-2.5, 0, 0, 0, true))
    public static PortalPlacementComponent getPortalOffset(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(PORTAL_OFFSET, Tag.TAG_COMPOUND)) return PortalPlacementComponent.DEFAULT;
        return PortalPlacementComponent.load(tag.getCompound(PORTAL_OFFSET));
    }

    public static void setPortalOffset(ItemStack stack, PortalPlacementComponent value) {
        stack.getOrCreateTag().put(PORTAL_OFFSET, value.save());
    }

    // portal_target (no default)
    @Nullable
    public static LocationGetter getPortalTarget(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains(PORTAL_TARGET, Tag.TAG_COMPOUND) ? null : LocationGetter.load(tag.getCompound(PORTAL_TARGET));
    }

    public static void setPortalTarget(ItemStack stack, @Nullable LocationGetter target) {
        if (target == null) remove(stack, PORTAL_TARGET);
        else stack.getOrCreateTag().put(PORTAL_TARGET, target.save());
    }

    // wallet_contents (default 18 empty stacks) [{slot: int, item: ItemStack NBT}], empty slots not written
    public static NonNullList<ItemStack> getWalletContents(ItemStack stack) {
        NonNullList<ItemStack> items = NonNullList.withSize(18, ItemStack.EMPTY);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(WALLET_CONTENTS, Tag.TAG_LIST)) return items;
        ListTag list = tag.getList(WALLET_CONTENTS, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int slot = entry.getInt("slot");
            if (slot >= 0 && slot < items.size()) items.set(slot, ItemStack.of(entry.getCompound("item")));
        }
        return items;
    }

    /** Writes copies of the stacks; no-op on an empty stack (the client wallet menu runs over ItemStack.EMPTY). */
    public static void setWalletContents(ItemStack stack, java.util.List<ItemStack> items) {
        if (stack.isEmpty()) return;
        ListTag list = new ListTag();
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if (item.isEmpty()) continue;
            CompoundTag entry = new CompoundTag();
            entry.putInt("slot", i);
            entry.put("item", item.save(new CompoundTag()));
            list.add(entry);
        }
        if (list.isEmpty()) remove(stack, WALLET_CONTENTS);
        else stack.getOrCreateTag().put(WALLET_CONTENTS, list);
    }

    // access_id (no default)
    @Nullable
    public static ResourceLocation getAccessId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains(ACCESS_ID, Tag.TAG_STRING) ? null : ResourceLocation.tryParse(tag.getString(ACCESS_ID));
    }

    public static void setAccessId(ItemStack stack, @Nullable ResourceLocation id) {
        if (id == null) remove(stack, ACCESS_ID);
        else stack.getOrCreateTag().putString(ACCESS_ID, id.toString());
    }

    // selected_portal (no default)
    @Nullable
    public static IndirectLocation getSelectedPos(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains(SELECTED_PORTAL, Tag.TAG_COMPOUND) ? null : IndirectLocation.load(tag.getCompound(SELECTED_PORTAL));
    }

    public static void setSelectedPos(ItemStack stack, @Nullable IndirectLocation value) {
        if (value == null) remove(stack, SELECTED_PORTAL);
        else stack.getOrCreateTag().put(SELECTED_PORTAL, value.save());
    }

    // twister_data (no default): the attached time twister's own NBT (upstream: its DataComponentPatch)
    @Nullable
    public static CompoundTag getTwisterData(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains(TWISTER_DATA, Tag.TAG_COMPOUND) ? null : tag.getCompound(TWISTER_DATA);
    }

    public static void setTwisterData(ItemStack stack, @Nullable CompoundTag value) {
        if (value == null) remove(stack, TWISTER_DATA);
        else stack.getOrCreateTag().put(TWISTER_DATA, value.copy());
    }

    // anchor_id (no default), UUID string (upstream UUIDUtil.STRING_CODEC)
    @Nullable
    public static java.util.UUID getAnchorId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(ANCHOR_ID, Tag.TAG_STRING)) return null;
        try {
            return java.util.UUID.fromString(tag.getString(ANCHOR_ID));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static void setAnchorId(ItemStack stack, @Nullable java.util.UUID value) {
        if (value == null) remove(stack, ANCHOR_ID);
        else stack.getOrCreateTag().putString(ANCHOR_ID, value.toString());
    }

    // locked (default true)
    public static boolean isLocked(ItemStack stack) {
        return getBool(stack, LOCKED, true);
    }

    public static void setLocked(ItemStack stack, boolean value) {
        stack.getOrCreateTag().putBoolean(LOCKED, value);
    }
}
