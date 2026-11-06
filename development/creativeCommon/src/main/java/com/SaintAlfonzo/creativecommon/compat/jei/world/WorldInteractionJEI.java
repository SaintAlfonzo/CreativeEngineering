package com.SaintAlfonzo.creativecommon.compat.jei.world;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;

import com.SaintAlfonzo.creativecommon.CreativeCommon;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Registers every {@link WorldTab} (feature_writeup/jei_coverage.md 4). Rows per tab, in this order:
 * <ol>
 *   <li>{@code assets/creativecommon/jei_world/<tab>/*.json} from every resource pack (KubeJS's
 *       {@code kubejs/assets} is one), files in name order: {@code generated.json} from tools/gen_jei_world.py,
 *       {@code manual.json} by hand. Format: {@link WorldRow}.</li>
 *   <li>{@link WorldRows}: rows KubeJS startup scripts added, and Java providers (config- and tag-driven tabs).</li>
 * </ol>
 * Resources are client side, so this works the same on a dedicated server; a tab with no rows doesn't show.
 */
@JeiPlugin
public class WorldInteractionJEI implements IModPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation UID = new ResourceLocation(CreativeCommon.NS, "world_interactions");
    private static final String DIR = "jei_world";

    // The Burning and Campfire rows are Creative Compat's (compat.jei.BurnRows / CampfireRows), registered through
    // WorldRows.provider from its constructor, before JEI loads this plugin.

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
        WorldSlotRenderer slotRenderer = new WorldSlotRenderer(
                registration.getJeiHelpers().getIngredientManager().getIngredientRenderer(VanillaTypes.ITEM_STACK));
        for (WorldTab tab : WorldTab.values()) {
            registration.addRecipeCategories(new WorldInteractionCategory(helper, tab, icon(helper, tab), slotRenderer));
        }
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Map<WorldTab, List<JsonObject>> raw = readResources();
        int total = 0;
        for (WorldTab tab : WorldTab.values()) {
            List<JsonObject> json = new ArrayList<>(raw.getOrDefault(tab, List.of()));
            json.addAll(WorldRows.rows(tab));
            json = PotionRows.expand(json);
            List<WorldRow> rows = new ArrayList<>();
            for (int n = 0; n < json.size(); n++) {
                WorldRow row = WorldRow.parse(json.get(n), tab.id + " row " + n);
                if (row != null) rows.add(row);
            }
            if (!rows.isEmpty()) registration.addRecipes(WorldInteractionCategory.type(tab), rows);
            total += rows.size();
        }
        LOGGER.info("[jei_world] {} rows in {} tabs", total, WorldTab.values().length);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (WorldTab tab : WorldTab.values()) {
            for (String catalyst : tab.catalysts) {
                for (ItemStack stack : stacks(catalyst)) {
                    registration.addRecipeCatalyst(stack, WorldInteractionCategory.type(tab));
                }
            }
        }
    }

    /** Rows of every {@code jei_world/<tab>/*.json}, by tab, files in path order. */
    private static Map<WorldTab, List<JsonObject>> readResources() {
        Map<WorldTab, List<JsonObject>> out = new java.util.EnumMap<>(WorldTab.class);
        Map<ResourceLocation, Resource> found = new TreeMap<>(Minecraft.getInstance().getResourceManager()
                .listResources(DIR, rl -> rl.getPath().endsWith(".json")));
        for (Map.Entry<ResourceLocation, Resource> entry : found.entrySet()) {
            String[] parts = entry.getKey().getPath().split("/");
            WorldTab tab = parts.length == 3 ? WorldTab.byId(parts[1]) : null;
            if (tab == null) {
                LOGGER.warn("[jei_world] {}: not jei_world/<tab>/<file>.json for a known tab", entry.getKey());
                continue;
            }
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonObject file = JsonParser.parseReader(reader).getAsJsonObject();
                List<JsonObject> rows = out.computeIfAbsent(tab, k -> new ArrayList<>());
                for (JsonElement row : file.getAsJsonArray("rows")) rows.add(row.getAsJsonObject());
            } catch (Exception e) {
                LOGGER.warn("[jei_world] {}: {}", entry.getKey(), e.toString());
            }
        }
        return out;
    }

    /** The tab's first icon item that exists; Jar Storage is the jar with a small faucet on top. */
    private static IDrawable icon(IGuiHelper helper, WorldTab tab) {
        ItemStack stack = new ItemStack(Items.BARRIER);
        for (String id : tab.icons) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
            if (item != null && item != Items.AIR) {
                stack = new ItemStack(item);
                break;
            }
        }
        if (tab == WorldTab.JAR_STORAGE) {
            List<ItemStack> faucet = stacks("supplementaries:faucet");
            if (!faucet.isEmpty()) return com.SaintAlfonzo.creativecommon.compat.jei.JeiIcons.doubleIcon(helper, stack, faucet.get(0));
        }
        return helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, stack);
    }

    /** An item id or {@code #item_tag} as stacks. */
    private static List<ItemStack> stacks(String id) {
        List<ItemStack> out = new ArrayList<>();
        if (id.startsWith("#")) {
            TagKey<Item> tag = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(id.substring(1)));
            for (Holder<Item> holder : Registry.ITEM.getTagOrEmpty(tag)) out.add(new ItemStack(holder.value()));
        } else {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
            if (item != null && item != Items.AIR) out.add(new ItemStack(item));
        }
        return out;
    }
}
