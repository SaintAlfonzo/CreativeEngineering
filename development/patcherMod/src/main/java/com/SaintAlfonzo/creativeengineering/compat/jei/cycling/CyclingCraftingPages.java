package com.SaintAlfonzo.creativeengineering.compat.jei.cycling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Builds the cycling crafting pages for special recipes JEI hides (feature_writeup/jei_coverage.md part 1).
 *
 * <p>Every page comes from the mod's own recipe: candidate grids are run through {@link CraftingProbe}
 * and only the ones it accepts are shown, with its real result. A recipe that is missing (mod not
 * loaded, recipe disabled) or accepts none of the candidates just gives no page, and the summary line
 * in latest.log says so. Recipes another mod's JEI plugin already shows are left to that plugin.
 */
final class CyclingCraftingPages {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Level level;
    private final List<CyclingCraftingDisplay> pages = new ArrayList<>();
    /** Source recipe id -> variants shown (0 = the recipe exists but no candidate matched). */
    private final Map<String, Integer> summary = new LinkedHashMap<>();
    private List<Item> allItems;
    private List<Item> damageable;

    private CyclingCraftingPages(Level level) {
        this.level = level;
    }

    static List<CyclingCraftingDisplay> build(Level level) {
        CyclingCraftingPages p = new CyclingCraftingPages(level);
        p.run("pack", p::pack);
        p.run("repair", p::repair);
        p.run("vanilla", p::vanilla);
        p.run("dyeing", p::dyeing);
        p.run("alexsmobs", p::alexsmobs);
        p.run("ae2", p::ae2);
        p.run("bedspreads", p::bedspreads);
        p.run("botania", p::botania);
        p.run("chimes", p::chimes);
        p.run("cloudstorage", p::cloudstorage);
        p.run("computercraft", p::computercraft);
        p.run("constructionwand", p::constructionwand);
        p.run("customizableelytra", p::customizableElytra);
        p.run("enderstorage", p::enderstorage);
        p.run("fairylights", p::fairylights);
        p.run("immersiveengineering", p::immersiveEngineering);
        p.run("littlelogistics", p::littleLogistics);
        p.run("nock_enough_arrows", p::paintArrows);
        p.run("oldguns", p::oldGuns);
        p.run("quark", p::quark);
        p.run("storagedrawers", p::storageDrawers);
        List<String> empty = new ArrayList<>();
        int variants = 0;
        for (Map.Entry<String, Integer> e : p.summary.entrySet()) {
            variants += e.getValue();
            if (e.getValue() == 0) {
                empty.add(e.getKey());
            }
        }
        LOGGER.info("[jei cycle] {} crafting pages, {} variants, from {} special recipes", p.pages.size(), variants, p.summary.size());
        if (!empty.isEmpty()) {
            LOGGER.warn("[jei cycle] no candidate grid matched, so no page for: {}", String.join(", ", empty));
        }
        return p.pages;
    }

    static ResourceLocation pageId(CraftingRecipe recipe, String path) {
        ResourceLocation src = recipe.getId();
        return new ResourceLocation(src.getNamespace(), src.getPath() + "/jei_cycle/" + path);
    }

    private void run(String group, Runnable builder) {
        try {
            builder.run();
        } catch (Throwable t) {
            LOGGER.warn("[jei cycle] {} pages failed", group, t);
        }
    }

    // ------------------------------------------------------------------ pack

    private void pack() {
        // Elytra + catalyst: crafts a fresh elytra, keeps the original.
        fromIngredients("creativeengineering:elytraduplication/dragon_scale_elytra");
    }

    // ------------------------------------------------------------------ repairing

    /** Two-item grid repair (vanilla, IE) and Mimicream: every damaged item, plain and enchanted. */
    private void repair() {
        for (String id : List.of("minecraft:repair_item", "immersiveengineering:ie_item_repair")) {
            withRecipe(id, (r, probe) -> {
                for (Item item : damageable()) {
                    ItemStack a = damaged(item, 0.6f);
                    ItemStack b = damaged(item, 0.7f);
                    probe.add(CraftingProbe.cells(a, b));
                    ItemStack ench = enchanted(a);
                    if (ench != null) {
                        probe.add(CraftingProbe.cells(ench, b));
                    }
                }
                emit(id, probe.page("all", true));
            });
        }
    }

    private void alexsmobs() {
        ItemStack cream = stack("alexsmobs:mimicream");
        if (cream.isEmpty()) {
            return;
        }
        withRecipe("alexsmobs:mimicream_repair_recipe", (r, probe) -> {
            for (Item item : damageable()) {
                ItemStack worn = damaged(item, 0.75f);
                probe.add(CraftingProbe.ring(worn, cream));
                ItemStack ench = enchanted(worn);
                if (ench != null) {
                    probe.add(CraftingProbe.ring(ench, cream));
                }
            }
            emit(r.getId().toString(), probe.page("all", false));
        });
    }

    // ------------------------------------------------------------------ vanilla

    private void vanilla() {
        withRecipe("minecraft:banner_duplicate", (r, probe) -> {
            for (String pattern : List.of("cr", "mc", "bri")) {
                for (DyeColor c : DyeColor.values()) {
                    ItemStack blank = banner(c, null, null);
                    probe.add(CraftingProbe.cells(banner(c, pattern, contrast(c)), blank));
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });

        withRecipe("minecraft:book_cloning", (r, probe) -> {
            ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
            CompoundTag tag = book.getOrCreateTag();
            tag.putString("title", "Book");
            tag.putString("author", "Steve");
            tag.putInt("generation", 0);
            ListTag pagesTag = new ListTag();
            pagesTag.add(StringTag.valueOf("{\"text\":\"\"}"));
            tag.put("pages", pagesTag);
            for (int k = 1; k <= 8; k++) {
                probe.add(withCopies(book, new ItemStack(Items.WRITABLE_BOOK), k));
            }
            emit(r.getId().toString(), probe.page("all", true));
        });

        withRecipe("minecraft:map_cloning", (r, probe) -> {
            ItemStack map = new ItemStack(Items.FILLED_MAP);
            map.getOrCreateTag().putInt("map", 0);
            for (int k = 1; k <= 8; k++) {
                probe.add(withCopies(map, new ItemStack(Items.MAP), k));
            }
            emit(r.getId().toString(), probe.page("all", true));
        });

        // Fireworks: every star shape x effect x colour, fades, then rockets of each flight with and without stars.
        List<ItemStack> stars = new ArrayList<>();
        withRecipe("minecraft:firework_star", (r, probe) -> {
            List<ItemStack> shapes = new ArrayList<>();
            shapes.add(ItemStack.EMPTY);
            for (Item i : List.of(Items.FIRE_CHARGE, Items.GOLD_NUGGET, Items.FEATHER, Items.CREEPER_HEAD)) {
                shapes.add(new ItemStack(i));
            }
            List<ItemStack[]> effects = List.of(new ItemStack[0], new ItemStack[] {new ItemStack(Items.DIAMOND)},
                    new ItemStack[] {new ItemStack(Items.GLOWSTONE_DUST)},
                    new ItemStack[] {new ItemStack(Items.DIAMOND), new ItemStack(Items.GLOWSTONE_DUST)});
            for (ItemStack shape : shapes) {
                for (ItemStack[] effect : effects) {
                    for (ItemStack dye : dyes()) {
                        List<ItemStack> g = new ArrayList<>();
                        g.add(new ItemStack(Items.GUNPOWDER));
                        g.add(dye);
                        if (!shape.isEmpty()) {
                            g.add(shape);
                        }
                        g.addAll(List.of(effect));
                        ItemStack out = probe.add(CraftingProbe.cells(g.toArray(ItemStack[]::new)));
                        if (!out.isEmpty() && shape.isEmpty() && effect.length == 0) {
                            stars.add(out);
                        }
                    }
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
        withRecipe("minecraft:firework_star_fade", (r, probe) -> {
            for (ItemStack star : stars) {
                for (ItemStack dye : dyes()) {
                    probe.add(CraftingProbe.cells(star, dye));
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
        withRecipe("minecraft:firework_rocket", (r, probe) -> {
            ItemStack paper = new ItemStack(Items.PAPER);
            ItemStack powder = new ItemStack(Items.GUNPOWDER);
            for (int flight = 1; flight <= 3; flight++) {
                probe.add(CraftingProbe.cells(withCopies(paper, powder, flight).toArray(ItemStack[]::new)));
            }
            for (ItemStack star : stars) {
                for (int flight = 1; flight <= 3; flight++) {
                    List<ItemStack> g = new ArrayList<>(withCopies(paper, powder, flight).subList(0, flight + 1));
                    g.add(star);
                    probe.add(CraftingProbe.cells(g.toArray(ItemStack[]::new)));
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
    }

    // ------------------------------------------------------------------ dyeing

    /** Dye recipes on whole families of items: one page per item, every colour (and dye mixes where allowed). */
    private void dyeing() {
        Set<Item> done = new HashSet<>();
        dyePages("minecraft:armor_dye", i -> true, done);
        dyePages("quark:dye_item", i -> true, done);
        dyePages("immersiveengineering:crafting/curtain_colour", inMod("immersiveengineering"), done);
        dyePages("fantasyfurniture:dyeable", inMod("fantasyfurniture"), done);
        dyePages("suppsquared:sack_dye", inMod("suppsquared"), done);
    }

    private void dyePages(String id, Predicate<Item> filter, Set<Item> done) {
        withRecipe(id, (r, probe) -> {
            int before = pages.size();
            int variants = 0;
            ItemStack red = new ItemStack(DyeItem.byColor(DyeColor.RED));
            for (Item item : allItems()) {
                if (done.contains(item) || !filter.test(item)) {
                    continue;
                }
                ItemStack base = new ItemStack(item);
                if (probe.craft(CraftingProbe.cells(base, red)).isEmpty()) {
                    continue;
                }
                CraftingProbe one = new CraftingProbe(r, level);
                addDyeVariants(one, base);
                CyclingCraftingDisplay page = one.page(path(item), true);
                if (page != null) {
                    pages.add(page);
                    variants += page.variants();
                    done.add(item);
                }
            }
            summary.merge(id, variants, Integer::sum);
            if (pages.size() == before) {
                summary.put(id, 0);
            }
        });
    }

    /** Each colour alone, then two- and three-dye mixes (kept only when the recipe takes them). */
    private void addDyeVariants(CraftingProbe probe, ItemStack base) {
        List<ItemStack> d = dyes();
        for (ItemStack dye : d) {
            probe.add(CraftingProbe.cells(base, dye));
        }
        for (int c = 0; c < 16; c++) {
            probe.add(CraftingProbe.cells(base, d.get(c), d.get((c + 5) % 16)));
        }
        for (int c = 0; c < 16; c++) {
            probe.add(CraftingProbe.cells(base, d.get(c), d.get((c + 4) % 16), d.get((c + 11) % 16)));
        }
    }

    // ------------------------------------------------------------------ mods

    /** AE2: four cable anchors around any block that can be a facade. */
    private void ae2() {
        ItemStack anchor = stack("ae2:cable_anchor");
        if (anchor.isEmpty()) {
            return;
        }
        withRecipe("ae2:special/facade", (r, probe) -> {
            for (Item item : allItems()) {
                if (item instanceof BlockItem) {
                    probe.add(CraftingProbe.at(1, anchor, 3, anchor, 5, anchor, 7, anchor, 4, new ItemStack(item)));
                }
            }
            emit(r.getId().toString(), probe.page("all", false));
        });
    }

    private void bedspreads() {
        List<ItemStack> patterned = new ArrayList<>();
        withRecipe("bedspreads:add_pattern", (r, probe) -> {
            String[] patterns = {"cr", "mc", "bri", "bo"};
            for (DyeColor bed : DyeColor.values()) {
                for (DyeColor base : DyeColor.values()) {
                    ItemStack banner = banner(base, patterns[(bed.getId() + base.getId()) % patterns.length], contrast(base));
                    ItemStack out = probe.add(CraftingProbe.cells(stack("minecraft:" + bed.getName() + "_bed"), banner));
                    if (!out.isEmpty()) {
                        patterned.add(out);
                    }
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
        withRecipe("bedspreads:remove_pattern", (r, probe) -> {
            patterned.forEach(bed -> probe.add(CraftingProbe.cells(bed)));
            emit(r.getId().toString(), probe.page("all", true));
        });
    }

    private void botania() {
        List<ItemStack> lenses = itemsWhere(i -> id(i).getNamespace().equals("botania") && id(i).getPath().startsWith("lens_"));
        List<ItemStack> cosmetics = itemsWhere(i -> id(i).getNamespace().equals("botania") && id(i).getPath().startsWith("cosmetic_"));

        List<ItemStack> dressed = new ArrayList<>();
        withRecipe("botania:dynamic/cosmetic_attach", (r, probe) -> {
            if (cosmetics.isEmpty()) {
                return;
            }
            List<ItemStack> baubles = new ArrayList<>();
            for (ItemStack s : itemsWhere(inMod("botania"))) {
                if (!cosmetics.contains(s) && !probe.craft(CraftingProbe.cells(s, cosmetics.get(0))).isEmpty()) {
                    baubles.add(s);
                }
            }
            for (int i = 0; i < Math.max(baubles.size(), cosmetics.size()) && !baubles.isEmpty(); i++) {
                ItemStack out = probe.add(CraftingProbe.cells(baubles.get(i % baubles.size()), cosmetics.get(i % cosmetics.size())));
                if (!out.isEmpty()) {
                    dressed.add(out);
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
        withRecipe("botania:dynamic/cosmetic_remove", (r, probe) -> {
            dressed.forEach(s -> probe.add(CraftingProbe.cells(s)));
            emit(r.getId().toString(), probe.page("all", true));
        });

        oneIngredientOver("botania:dynamic/keep_ivy", "botania:resolute_ivy", damageableStacks(), "all");
        oneIngredientOver("botania:dynamic/phantom_ink_apply", "botania:phantom_ink",
                itemsWhere(i -> i instanceof ArmorItem), "all");
        List<ItemStack> enchantedGear = new ArrayList<>();
        for (ItemStack s : damageableStacks()) {
            ItemStack e = enchanted(s);
            if (e != null) {
                enchantedGear.add(e);
            }
        }
        oneIngredientOver("botania:dynamic/spell_cloth_apply", "botania:spell_cloth", enchantedGear, "all");

        withRecipe("botania:dynamic/laputa_shard_upgrade", (r, probe) -> {
            ItemStack essence = stack("botania:life_essence");
            for (int lvl = 0; lvl < 20; lvl++) {
                ItemStack shard = stack("botania:laputa_shard");
                if (lvl > 0) {
                    shard.getOrCreateTag().putInt("level", lvl);
                }
                probe.add(CraftingProbe.cells(shard, essence));
            }
            emit(r.getId().toString(), probe.page("all", true));
        });

        withRecipe("botania:dynamic/lens_dye", (r, probe) -> {
            List<ItemStack> colours = new ArrayList<>(dyes());
            colours.add(stack("botania:mana_pearl"));
            for (ItemStack lens : lenses) {
                for (ItemStack c : colours) {
                    probe.add(CraftingProbe.cells(lens, c));
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });

        ItemStack blaster = stack("botania:mana_gun");
        withRecipe("botania:dynamic/mana_gun_add_clip", (r, probe) -> {
            probe.add(CraftingProbe.cells(blaster, stack("botania:clip")));
            emit(r.getId().toString(), probe.page("all", true));
        });
        List<ItemStack> loaded = new ArrayList<>();
        withRecipe("botania:dynamic/mana_gun_add_lens", (r, probe) -> {
            for (ItemStack lens : lenses) {
                ItemStack out = probe.add(CraftingProbe.cells(blaster, lens));
                if (!out.isEmpty()) {
                    loaded.add(out);
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
        withRecipe("botania:dynamic/mana_gun_remove_lens", (r, probe) -> {
            loaded.forEach(s -> probe.add(CraftingProbe.cells(s)));
            emit(r.getId().toString(), probe.page("all", true));
        });

        withRecipe("botania:dynamic/merge_vial", (r, probe) -> {
            for (ItemStack vial : subtypes(stack("botania:vial").getItem())) {
                probe.add(CraftingProbe.cells(vial, vial));
            }
            emit(r.getId().toString(), probe.page("all", true));
        });

        // Composite lenses come from Botania's own composite recipe (its plugin shows that one), then split.
        List<ItemStack> composites = new ArrayList<>();
        recipe("botania:dynamic/composite_lens").ifPresent(comp -> {
            CraftingProbe make = new CraftingProbe(comp, level);
            ItemStack slime = new ItemStack(Items.SLIME_BALL);
            for (int i = 0; i < lenses.size(); i++) {
                ItemStack out = make.craft(CraftingProbe.cells(lenses.get(i), lenses.get((i + 1) % lenses.size()), slime));
                if (!out.isEmpty()) {
                    composites.add(out);
                }
            }
        });
        withRecipe("botania:dynamic/split_lens", (r, probe) -> {
            composites.forEach(s -> probe.add(CraftingProbe.cells(s)));
            emit(r.getId().toString(), probe.page("all", true));
        });

        withRecipe("botania:dynamic/black_hole_talisman_extract", (r, probe) -> {
            for (Item block : List.of(Items.COBBLESTONE, Items.STONE, Items.DIRT, Items.SAND, Items.NETHERRACK)) {
                ItemStack talisman = stack("botania:black_hole_talisman");
                CompoundTag tag = talisman.getOrCreateTag();
                tag.putString("blockName", id(block).toString());
                tag.putInt("blockCount", 1000);
                probe.add(CraftingProbe.cells(talisman));
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
    }

    /** Glass wind bell: dye above sets the base colour, dye below the trim. */
    private void chimes() {
        withRecipe("chimes:glass_bells_custom", (r, probe) -> {
            ItemStack bell = stack("chimes:glass_bells");
            List<ItemStack> d = dyes();
            for (int base = 0; base < 16; base++) {
                for (int trim = 0; trim < 16; trim++) {
                    probe.add(CraftingProbe.at(1, d.get(base), 4, bell, 7, d.get(trim)));
                }
            }
            for (int c = 0; c < 16; c++) {
                probe.add(CraftingProbe.at(1, d.get(c), 4, bell));
                probe.add(CraftingProbe.at(4, bell, 7, d.get(c)));
            }
            emit(r.getId().toString(), probe.page("all", false));
        });
    }

    /** Balloon arrows: eight arrows around a balloon of each colour (balloons dyed by the armor-dye recipe). */
    private void cloudstorage() {
        ItemStack balloon = stack("cloudstorage:balloon");
        if (balloon.isEmpty()) {
            return;
        }
        List<ItemStack> balloons = new ArrayList<>();
        balloons.add(balloon);
        recipe("minecraft:armor_dye").ifPresent(dye -> {
            CraftingProbe probe = new CraftingProbe(dye, level);
            for (ItemStack d : dyes()) {
                ItemStack out = probe.craft(CraftingProbe.cells(balloon, d));
                if (!out.isEmpty()) {
                    balloons.add(out);
                }
            }
        });
        withRecipe("cloudstorage:balloon_arrow", (r, probe) -> {
            balloons.forEach(b -> probe.add(CraftingProbe.ring(b, new ItemStack(Items.ARROW))));
            emit(r.getId().toString(), probe.page("all", false));
        });
    }

    private void computercraft() {
        withRecipe("computercraft:colour", (r, probe) -> {
            int variants = 0;
            List<ItemStack> coloured = new ArrayList<>();
            for (String id : List.of("computer_normal", "computer_advanced", "computer_command", "turtle_normal", "turtle_advanced",
                    "pocket_computer_normal", "pocket_computer_advanced", "disk")) {
                ItemStack base = stack("computercraft:" + id);
                if (base.isEmpty()) {
                    continue;
                }
                CraftingProbe one = new CraftingProbe(r, level);
                addDyeVariants(one, base);
                if (!one.isEmpty()) {
                    coloured.add(one.results.get(0));
                }
                variants += emitCounted(one.page(id, true));
            }
            // Sponge washes the colour off.
            CraftingProbe wash = new CraftingProbe(r, level);
            coloured.forEach(s -> wash.add(CraftingProbe.cells(s, new ItemStack(Items.SPONGE))));
            variants += emitCounted(wash.page("sponge", true));
            summary.merge(r.getId().toString(), variants, Integer::sum);
        });
        withRecipe("computercraft:disk", (r, probe) -> {
            for (ItemStack dye : dyes()) {
                probe.add(CraftingProbe.cells(new ItemStack(Items.PAPER), new ItemStack(Items.REDSTONE), dye));
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
        withRecipe("computercraft:printout", (r, probe) -> {
            ItemStack page = stack("computercraft:printed_page");
            ItemStack pagesItem = stack("computercraft:printed_pages");
            ItemStack string = new ItemStack(Items.STRING);
            ItemStack leather = new ItemStack(Items.LEATHER);
            for (int n = 2; n <= 8; n++) {
                List<ItemStack> g = new ArrayList<>(withCopies(string, page, n));
                probe.add(CraftingProbe.cells(g.toArray(ItemStack[]::new)));
            }
            probe.add(CraftingProbe.cells(pagesItem, page, string));
            probe.add(CraftingProbe.cells(page, page, leather, string));
            probe.add(CraftingProbe.cells(pagesItem, leather, string));
            emit(r.getId().toString(), probe.page("all", true));
        });
    }

    private void constructionwand() {
        withRecipe("constructionwand:dynamic/wand_upgrade", (r, probe) -> {
            for (String wand : List.of("stone_wand", "iron_wand", "diamond_wand", "infinity_wand")) {
                for (String core : List.of("core_angel", "core_destruction")) {
                    probe.add(CraftingProbe.cells(stack("constructionwand:" + wand), stack("constructionwand:" + core)));
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
    }

    private void customizableElytra() {
        List<ItemStack> elytras = new ArrayList<>(List.of(new ItemStack(Items.ELYTRA)));
        ItemStack custom = stack("customizableelytra:customizable_elytra");
        if (!custom.isEmpty()) {
            elytras.add(custom);
        }
        List<ItemStack> dyed = new ArrayList<>();
        withRecipe("minecraft:elytra_dye_recipe", (r, probe) -> {
            for (ItemStack e : elytras) {
                int before = probe.results.size();
                addDyeVariants(probe, e);
                dyed.addAll(probe.results.subList(before, Math.min(probe.results.size(), before + 16)));
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
        withRecipe("minecraft:elytra_banner_recipe", (r, probe) -> {
            String[] patterns = {"cr", "mc", "bri", "bo"};
            for (ItemStack e : elytras) {
                for (DyeColor c : DyeColor.values()) {
                    probe.add(CraftingProbe.cells(e, banner(c, patterns[c.getId() % patterns.length], contrast(c))));
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
        oneIngredientOver("minecraft:elytra_hide_cape_recipe", "minecraft:paper", elytras, "all");
        oneIngredientOver("minecraft:elytra_wing_glow_recipe", "minecraft:glow_ink_sac", elytras, "all");
        List<ItemStack> wings = new ArrayList<>();
        withRecipe("minecraft:elytra_wing_split_recipe", (r, probe) -> {
            List<ItemStack> sources = new ArrayList<>(elytras);
            sources.addAll(dyed);
            for (ItemStack e : sources) {
                ItemStack out = probe.add(CraftingProbe.cells(e));
                if (!out.isEmpty()) {
                    wings.add(out);
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
        withRecipe("minecraft:elytra_wing_combination_recipe", (r, probe) -> {
            for (int i = 0; i < wings.size(); i++) {
                ItemStack a = wings.get(i);
                ItemStack b = wings.get((i + 1) % wings.size());
                if (probe.add(CraftingProbe.cells(a, b)).isEmpty()) {
                    // Some wing pairs are left/right sensitive: try them the other way round.
                    probe.add(CraftingProbe.cells(b, a));
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
    }

    /** Ender chest / tank / pouch in the middle, a dye above the left, middle or right frequency slot. */
    private void enderstorage() {
        Map<String, String> items = Map.of("enderstorage:recolour_ender_chest", "enderstorage:ender_chest",
                "enderstorage:recolour_ender_tank", "enderstorage:ender_tank",
                "enderstorage:recolour_ender_pouch", "enderstorage:ender_pouch");
        for (Map.Entry<String, String> e : items.entrySet()) {
            withRecipe(e.getKey(), (r, probe) -> {
                ItemStack item = stack(e.getValue());
                for (int col = 0; col < 3; col++) {
                    for (ItemStack dye : dyes()) {
                        probe.add(CraftingProbe.at(col, dye, 4, item));
                    }
                }
                emit(r.getId().toString(), probe.page("all", false));
            });
        }
    }

    /** Copy colour: a coloured fairy-light item and another one, which takes the colour. */
    private void fairylights() {
        withRecipe("fairylights:copy_color", (r, probe) -> {
            List<ItemStack> dyeable = new ArrayList<>();
            ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(new ResourceLocation("fairylights", "dyeable")))
                    .forEach(i -> dyeable.add(new ItemStack(i)));
            for (int i = 0; i < dyeable.size(); i++) {
                ItemStack target = dyeable.get((i + 1) % dyeable.size());
                List<ItemStack> colours = subtypes(dyeable.get(i).getItem());
                for (int c = 0; c < Math.min(16, colours.size()); c++) {
                    probe.add(CraftingProbe.cells(colours.get(c), target));
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
    }

    private void immersiveEngineering() {
        oneIngredientOver("immersiveengineering:earmuffs_attach", "immersiveengineering:earmuffs",
                itemsWhere(i -> i instanceof ArmorItem a && a.getSlot() == EquipmentSlot.HEAD), "all");
        oneIngredientOver("immersiveengineering:powerpack_attach", "immersiveengineering:powerpack",
                itemsWhere(i -> i instanceof ArmorItem a && a.getSlot() == EquipmentSlot.CHEST), "all");
        oneIngredientOver("immersiveengineering:flare_bullet_color", "immersiveengineering:flare", dyes(), "all");
        oneIngredientOver("immersiveengineering:potion_bullet_fill", "immersiveengineering:potion",
                subtypes(Items.POTION), "all");

        withRecipe("immersiveengineering:jerrycan_refill", (r, probe) -> {
            List<ItemStack> targets = itemsWhere(i -> id(i).getNamespace().equals("immersiveengineering")
                    && List.of("drill", "chemthrower", "buzzsaw", "jerrycan").contains(id(i).getPath()));
            for (String fluid : List.of("immersiveengineering:biodiesel", "immersiveengineering:plantoil",
                    "immersiveengineering:ethanol", "immersiveengineering:creosote", "minecraft:water")) {
                ItemStack can = filled(stack("immersiveengineering:jerrycan"), fluid);
                if (can.isEmpty()) {
                    continue;
                }
                for (ItemStack target : targets) {
                    probe.add(CraftingProbe.cells(target, can));
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });

        withRecipe("immersiveengineering:speedloader_load", (r, probe) -> {
            ItemStack loader = stack("immersiveengineering:speedloader");
            for (ItemStack bullet : itemsWhere(inMod("immersiveengineering"))) {
                if (probe.craft(CraftingProbe.cells(loader, bullet)).isEmpty()) {
                    continue;
                }
                probe.add(CraftingProbe.cells(loader, bullet));
                probe.add(withCopies(loader, bullet, 8));
            }
            emit(r.getId().toString(), probe.page("all", true));
        });

        withRecipe("immersiveengineering:shaderbag_downgrading", (r, probe) -> {
            subtypes(stack("immersiveengineering:shader").getItem()).forEach(s -> probe.add(CraftingProbe.cells(s)));
            emit(r.getId().toString(), probe.page("all", true));
        });

        withRecipe("immersiveengineering:revolver_cycle", (r, probe) -> {
            subtypes(stack("immersiveengineering:revolver").getItem()).forEach(s -> probe.add(CraftingProbe.cells(s)));
            emit(r.getId().toString(), probe.page("all", true));
        });
    }

    /** A route with stops plus 1-3 blank routes: copies. */
    private void littleLogistics() {
        Map<String, String> routes = Map.of("littlelogistics:tug_route_copy", "littlelogistics:tug_route",
                "littlelogistics:loco_route_copy", "littlelogistics:locomotive_route");
        for (Map.Entry<String, String> e : routes.entrySet()) {
            withRecipe(e.getKey(), (r, probe) -> {
                ItemStack blank = stack(e.getValue());
                ItemStack filled = blank.copy();
                ListTag nodes = new ListTag();
                for (int n = 0; n < 2; n++) {
                    CompoundTag coords = new CompoundTag();
                    coords.putDouble("x", n * 32);
                    coords.putDouble("y", 64);
                    coords.putDouble("z", n * 16);
                    CompoundTag node = new CompoundTag();
                    node.put("coordinates", coords);
                    nodes.add(node);
                }
                CompoundTag route = new CompoundTag();
                route.put("nodes", nodes);
                filled.getOrCreateTag().put("route", route);
                for (int k = 1; k <= 3; k++) {
                    probe.add(withCopies(filled, blank, k));
                }
                emit(r.getId().toString(), probe.page("all", true));
            });
        }
    }

    private void paintArrows() {
        withRecipe("nock_enough_arrows:paint_arrow_dye", (r, probe) -> {
            ItemStack arrow = stack("nock_enough_arrows:paint_arrow");
            for (ItemStack dye : dyes()) {
                if (probe.add(CraftingProbe.ring(dye, arrow)).isEmpty()) {
                    probe.add(CraftingProbe.cells(arrow, dye));
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
    }

    private void oldGuns() {
        withRecipe("oldguns:refill_powder_horn", (r, probe) -> {
            ItemStack horn = stack("oldguns:powder_horn");
            for (float worn : new float[] {0.5f, 1f}) {
                ItemStack h = horn.isDamageableItem() ? damaged(horn.getItem(), worn) : horn;
                for (Ingredient ing : r.getIngredients()) {
                    for (ItemStack powder : ing.getItems()) {
                        if (!powder.is(horn.getItem())) {
                            probe.add(CraftingProbe.cells(h, powder));
                        }
                    }
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
    }

    private void quark() {
        // Chest from eight planks of mixed woods: each cell cycles through the planks, offset so they differ.
        for (String id : List.of("quark:tweaks/crafting/utility/chests/mixed_chest_wood", "quark:building/crafting/chests/mixed_chest")) {
            withRecipe(id, (r, probe) -> {
                List<ItemStack> planks = new ArrayList<>();
                ForgeRegistries.ITEMS.tags().getTag(ItemTags.PLANKS).forEach(i -> planks.add(new ItemStack(i)));
                for (int k = 0; k < planks.size(); k++) {
                    ItemStack[] g = new ItemStack[9];
                    for (int cell = 0, n = 0; cell < 9; cell++) {
                        if (cell != 4) {
                            g[cell] = planks.get((k + n++) % planks.size());
                        }
                    }
                    probe.add(List.of(g[0], g[1], g[2], g[3], ItemStack.EMPTY, g[5], g[6], g[7], g[8]));
                }
                emit(id, probe.page("all", false));
            });
        }
    }

    private void storageDrawers() {
        withRecipe("storagedrawers:add_upgrade", (r, probe) -> {
            List<ItemStack> mod = itemsWhere(inMod("storagedrawers"));
            List<ItemStack> upgrades = itemsWhere(i -> inMod("storagedrawers").test(i) && id(i).getPath().endsWith("_upgrade"));
            if (upgrades.isEmpty()) {
                return;
            }
            List<ItemStack> drawers = new ArrayList<>();
            for (ItemStack s : mod) {
                for (ItemStack u : upgrades) {
                    if (!probe.craft(CraftingProbe.cells(s, u)).isEmpty()) {
                        drawers.add(s);
                        break;
                    }
                }
            }
            for (ItemStack u : upgrades) {
                for (ItemStack d : drawers) {
                    probe.add(CraftingProbe.cells(d, u));
                }
            }
            emit(r.getId().toString(), probe.page("all", true));
        });
    }

    // ------------------------------------------------------------------ shared builders

    /** Cycles each ingredient of an ordinary-ingredient special recipe in step, placed in reading order. */
    private void fromIngredients(String id) {
        withRecipe(id, (r, probe) -> {
            List<Ingredient> ings = r.getIngredients();
            int n = 1;
            for (Ingredient ing : ings) {
                n = Math.max(n, ing.getItems().length);
            }
            for (int k = 0; k < n; k++) {
                List<ItemStack> g = new ArrayList<>();
                for (Ingredient ing : ings) {
                    ItemStack[] items = ing.getItems();
                    g.add(items.length == 0 ? ItemStack.EMPTY : items[k % items.length]);
                }
                probe.add(g);
            }
            emit(id, probe.page("all", true));
        });
    }

    /** Each of {@code over} plus one fixed item, shapeless. */
    private void oneIngredientOver(String id, String fixedId, List<ItemStack> over, String path) {
        ItemStack fixed = stack(fixedId);
        if (fixed.isEmpty()) {
            recipe(id).ifPresent(r -> summary.put(id, 0));
            return;
        }
        withRecipe(id, (r, probe) -> {
            for (ItemStack s : over) {
                if (!s.is(fixed.getItem())) {
                    probe.add(CraftingProbe.cells(s, fixed));
                }
            }
            emit(id, probe.page(path, true));
        });
    }

    private interface RecipeBody {
        void build(CraftingRecipe recipe, CraftingProbe probe);
    }

    private void withRecipe(String id, RecipeBody body) {
        recipe(id).ifPresent(r -> {
            try {
                body.build(r, new CraftingProbe(r, level));
            } catch (Throwable t) {
                summary.putIfAbsent(id, 0);
                LOGGER.warn("[jei cycle] {} failed", id, t);
            }
            summary.putIfAbsent(id, 0);
        });
    }

    private Optional<CraftingRecipe> recipe(String id) {
        return level.getRecipeManager().byKey(new ResourceLocation(id))
                .filter(CraftingRecipe.class::isInstance).map(CraftingRecipe.class::cast);
    }

    private void emit(String id, @Nullable CyclingCraftingDisplay page) {
        summary.merge(id, emitCounted(page), Integer::sum);
    }

    private int emitCounted(@Nullable CyclingCraftingDisplay page) {
        if (page == null) {
            return 0;
        }
        pages.add(page);
        return page.variants();
    }

    // ------------------------------------------------------------------ stacks

    private static ResourceLocation id(Item item) {
        return ForgeRegistries.ITEMS.getKey(item);
    }

    private static Predicate<Item> inMod(String namespace) {
        return i -> namespace.equals(id(i).getNamespace());
    }

    private static String path(Item item) {
        ResourceLocation rl = id(item);
        return rl.getNamespace() + "_" + rl.getPath().replace('/', '_');
    }

    private static ItemStack stack(String id) {
        ResourceLocation rl = new ResourceLocation(id);
        return ForgeRegistries.ITEMS.containsKey(rl) ? new ItemStack(ForgeRegistries.ITEMS.getValue(rl)) : ItemStack.EMPTY;
    }

    private List<Item> allItems() {
        if (allItems == null) {
            allItems = List.copyOf(ForgeRegistries.ITEMS.getValues());
        }
        return allItems;
    }

    private List<ItemStack> itemsWhere(Predicate<Item> filter) {
        List<ItemStack> list = new ArrayList<>();
        for (Item i : allItems()) {
            if (filter.test(i)) {
                list.add(new ItemStack(i));
            }
        }
        return list;
    }

    private List<Item> damageable() {
        if (damageable == null) {
            List<Item> list = new ArrayList<>();
            for (Item i : allItems()) {
                try {
                    if (new ItemStack(i).isDamageableItem()) {
                        list.add(i);
                    }
                } catch (Throwable ignored) {
                    // a broken item: leave it out
                }
            }
            damageable = list;
        }
        return damageable;
    }

    private List<ItemStack> damageableStacks() {
        List<ItemStack> list = new ArrayList<>();
        for (Item i : damageable()) {
            list.add(damaged(i, 0.5f));
        }
        return list;
    }

    private static ItemStack damaged(Item item, float worn) {
        ItemStack s = new ItemStack(item);
        s.setDamageValue(Math.max(1, Math.min(s.getMaxDamage() - 1, Math.round(s.getMaxDamage() * worn))));
        return s;
    }

    /** A copy with Mending and Unbreaking where they apply (else the first enchantment that does), or null. */
    @Nullable
    private static ItemStack enchanted(ItemStack s) {
        ItemStack e = s.copy();
        boolean any = false;
        for (Enchantment ench : List.of(Enchantments.MENDING, Enchantments.UNBREAKING)) {
            if (ench.canEnchant(e)) {
                e.enchant(ench, ench.getMaxLevel());
                any = true;
            }
        }
        if (!any) {
            for (Enchantment ench : ForgeRegistries.ENCHANTMENTS.getValues()) {
                if (!ench.isCurse() && ench.canEnchant(e)) {
                    e.enchant(ench, ench.getMaxLevel());
                    any = true;
                    break;
                }
            }
        }
        return any ? e : null;
    }

    private static List<ItemStack> dyes() {
        List<ItemStack> list = new ArrayList<>(16);
        for (DyeColor c : DyeColor.values()) {
            list.add(new ItemStack(DyeItem.byColor(c)));
        }
        return list;
    }

    private static DyeColor contrast(DyeColor c) {
        return c == DyeColor.WHITE ? DyeColor.RED : c == DyeColor.BLACK ? DyeColor.YELLOW : DyeColor.WHITE;
    }

    /** A banner of {@code base}, with one {@code pattern} layer in {@code colour} when given. */
    private static ItemStack banner(DyeColor base, @Nullable String pattern, @Nullable DyeColor colour) {
        ItemStack b = stack("minecraft:" + base.getName() + "_banner");
        if (pattern != null && colour != null) {
            CompoundTag layer = new CompoundTag();
            layer.putString("Pattern", pattern);
            layer.putInt("Color", colour.getId());
            ListTag patterns = new ListTag();
            patterns.add(layer);
            CompoundTag be = new CompoundTag();
            be.put("Patterns", patterns);
            b.getOrCreateTag().put("BlockEntityTag", be);
        }
        return b;
    }

    /** {@code first} then {@code k} copies of {@code copy}, as grid cells. */
    private static List<ItemStack> withCopies(ItemStack first, ItemStack copy, int k) {
        ItemStack[] g = new ItemStack[9];
        g[0] = first;
        for (int i = 1; i <= k && i < 9; i++) {
            g[i] = copy;
        }
        return java.util.Arrays.asList(g);
    }

    /** The item's creative-search variants (NBT subtypes such as brews, potions, colours). */
    private static List<ItemStack> subtypes(Item item) {
        NonNullList<ItemStack> list = NonNullList.create();
        try {
            item.fillItemCategory(CreativeModeTab.TAB_SEARCH, list);
        } catch (Throwable ignored) {
            // leave it with the plain stack
        }
        if (list.isEmpty()) {
            list.add(new ItemStack(item));
        }
        return list;
    }

    /** {@code container} filled with a bucket of the fluid through its fluid handler, or EMPTY. */
    private static ItemStack filled(ItemStack container, String fluidId) {
        ResourceLocation rl = new ResourceLocation(fluidId);
        if (container.isEmpty() || !ForgeRegistries.FLUIDS.containsKey(rl)) {
            return ItemStack.EMPTY;
        }
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(rl);
        ItemStack copy = container.copy();
        return copy.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).map(h -> {
            int in = h.fill(new FluidStack(fluid, 100_000), IFluidHandler.FluidAction.EXECUTE);
            return in > 0 ? h.getContainer() : ItemStack.EMPTY;
        }).orElse(ItemStack.EMPTY);
    }
}
