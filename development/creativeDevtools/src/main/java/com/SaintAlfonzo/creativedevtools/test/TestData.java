package com.SaintAlfonzo.creativedevtools.test;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Data verbs of {@code /cdevtest}, moved from {@code /zq} (tools/scratch_query/zq_probe.js) per owner decision D4:
 * recipes, rdetail, bytype, uses, makes, food, item, attrs, reg, potion, tag, tagsof, states. Recipe reads go through
 * the serializer's registry id, never {@code Recipe.getType()} (Mechanical Extruder throws AbstractMethodError, r1113),
 * and each recipe is read inside its own catch.
 */
final class TestData {

    private TestData() {
    }

    static void register() {
        TestCommand.add("recipes", TestData::recipes);
        TestCommand.add("rdetail", TestData::rdetail);
        TestCommand.add("bytype", TestData::bytype);
        TestCommand.add("uses", TestData::uses);
        TestCommand.add("makes", TestData::makes);
        TestCommand.add("food", TestData::food);
        TestCommand.add("item", TestData::item);
        TestCommand.add("attrs", TestData::attrs);
        TestCommand.add("reg", TestData::reg);
        TestCommand.add("potion", TestData::potion);
        TestCommand.add("tag", TestData::tag);
        TestCommand.add("tagsof", TestData::tagsof);
        TestCommand.add("states", TestData::states);
    }

    private static Collection<Recipe<?>> recipeList(TestCtx c) {
        return c.server.getRecipeManager().getRecipes();
    }

    static String serializer(Recipe<?> r) {
        try {
            return String.valueOf(ForgeRegistries.RECIPE_SERIALIZERS.getKey(r.getSerializer()));
        } catch (Throwable t) {
            return "?";
        }
    }

    static String result(Recipe<?> r) {
        try {
            return TestCtx.describe(r.getResultItem());
        } catch (Throwable t) {
            return "?";
        }
    }

    /** First 3 item ids of an ingredient, "/+N" for the rest. */
    static String ingredient(Ingredient g) {
        ItemStack[] items = g.getItems();
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < Math.min(3, items.length); k++) sb.append(k == 0 ? "" : "/").append(TestCtx.key(items[k].getItem()));
        if (items.length > 3) sb.append("/+").append(items.length - 3);
        return sb.length() == 0 ? "empty" : sb.toString();
    }

    private static String text(TestCtx c) {
        String s = c.args.word("text");
        c.args.end();
        c.args.checkOpts("max");
        return s;
    }

    /** recipes TEXT [max=80]: recipe ids containing TEXT, with result; then the count. */
    private static void recipes(TestCtx c) {
        String text = text(c);
        int max = c.args.optInt("max", 80, 1, 100_000), n = 0;
        for (Recipe<?> r : recipeList(c)) {
            String id = String.valueOf(r.getId());
            if (!id.contains(text)) continue;
            if (++n <= max) c.emit(c.line().kv("id", id).kv("result", result(r)));
        }
        c.emit(c.line().kv("text", text).kv("n", n).kv("shown", Math.min(n, max)));
    }

    /** rdetail TEXT [max=40]: serializer, result, ingredients; extra "part=" lines for rolled results, a sequenced
     * assembly's start and steps, and public tool / ingredient / side fields (CD skewering). */
    private static void rdetail(TestCtx c) {
        String text = text(c);
        int max = c.args.optInt("max", 40, 1, 10_000), n = 0;
        for (Recipe<?> r : recipeList(c)) {
            String id = String.valueOf(r.getId());
            if (!id.contains(text)) continue;
            if (++n > max) continue;
            List<String> ins = new ArrayList<>();
            try {
                for (Ingredient g : r.getIngredients()) ins.add(ingredient(g));
            } catch (Throwable t) {
                ins.add("error:" + t.getClass().getSimpleName());
            }
            c.emit(c.line().kv("id", id).kv("serializer", serializer(r)).kv("result", result(r))
                    .kv("ingredients", ins.isEmpty() ? "-" : String.join(",", ins)));
            String rolls = rolls(r);
            if (rolls != null) c.emit(c.line().kv("id", id).kv("part", "results").kv("value", rolls));
            Object start = call(r, "getIngredient");
            if (start instanceof Ingredient g) c.emit(c.line().kv("id", id).kv("part", "start").kv("value", ingredient(g)));
            List<String> fields = new ArrayList<>();
            for (String f : new String[]{"tool", "ingredient", "side"}) {
                Object g = field(r, f);
                if (g instanceof Ingredient ing) {
                    Object cnt = field(r, f + "Count");
                    fields.add(f + ":" + (cnt != null ? cnt + "x" : "") + ingredient(ing));
                }
            }
            if (!fields.isEmpty()) c.emit(c.line().kv("id", id).kv("part", "fields").kv("value", String.join(",", fields)));
            Object seq = call(r, "getSequence");
            if (seq instanceof List<?> steps) {
                int k = 0;
                for (Object sr : steps) {
                    Object pr = call(sr, "getRecipe");
                    if (!(pr instanceof Recipe<?> step)) continue;
                    List<String> gs = new ArrayList<>();
                    try {
                        for (Ingredient g : step.getIngredients()) gs.add(ingredient(g));
                    } catch (Throwable ignored) {
                    }
                    c.emit(c.line().kv("id", id).kv("part", "step" + (k++)).kv("serializer", serializer(step))
                            .kv("value", String.join(",", gs)));
                }
            }
        }
        c.emit(c.line().kv("text", text).kv("n", n).kv("shown", Math.min(n, max)));
    }

    /** "N id @chance" of each rolled result (Create ProcessingOutput, Farmer's Delight ChanceResult), or null. */
    private static String rolls(Recipe<?> r) {
        Object list = call(r, "getRollableResults");
        if (!(list instanceof Collection<?> outs)) return null;
        List<String> o = new ArrayList<>();
        for (Object out : outs) {
            Object st = call(out, "getStack");
            Object ch = call(out, "getChance");
            if (st instanceof ItemStack s) o.add(TestCtx.describe(s) + "@" + (ch == null ? 1 : ch));
        }
        return String.join(",", o);
    }

    private static Object call(Object o, String name) {
        try {
            Method m = o.getClass().getMethod(name);
            return m.invoke(o);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Object field(Object o, String name) {
        try {
            Field f = o.getClass().getField(name);
            return f.get(o);
        } catch (Throwable t) {
            return null;
        }
    }

    /** bytype SERIALIZER: every recipe of that serializer id, then the count. */
    private static void bytype(TestCtx c) {
        String type = c.args.word("serializer");
        c.args.end();
        c.args.checkOpts("max");
        int max = c.args.optInt("max", 500, 1, 100_000), n = 0;
        for (Recipe<?> r : recipeList(c)) {
            if (!serializer(r).equals(type)) continue;
            if (++n <= max) c.emit(c.line().kv("id", r.getId()).kv("result", result(r)));
        }
        c.emit(c.line().kv("serializer", type).kv("n", n));
    }

    /** uses ITEM: recipes with an ingredient accepting the item; alone = that ingredient accepts nothing else. */
    private static void uses(TestCtx c) {
        Item item = TestCtx.item(c.args.word("item"));
        c.args.end();
        c.args.checkOpts();
        int alone = 0, shared = 0;
        for (Recipe<?> r : recipeList(c)) {
            boolean a = false, s = false;
            try {
                for (Ingredient g : r.getIngredients()) {
                    ItemStack[] st = g.getItems();
                    boolean hit = false;
                    for (ItemStack x : st) if (x.is(item)) hit = true;
                    if (hit && st.length == 1) a = true;
                    else if (hit) s = true;
                }
            } catch (Throwable ignored) {
            }
            if (!a && !s) continue;
            if (a) alone++;
            else shared++;
            c.emit(c.line().kv("id", r.getId()).kv("alone", a).kv("serializer", serializer(r)));
        }
        c.emit(c.line().kv("item", TestCtx.key(item)).kv("alone", alone).kv("shared", shared));
    }

    /** makes ITEM|TEXT: recipes whose result is the item (full id) or whose result id contains TEXT (no ':'). */
    private static void makes(TestCtx c) {
        String arg = c.args.word("item");
        c.args.end();
        c.args.checkOpts();
        int n = 0;
        for (Recipe<?> r : recipeList(c)) {
            String rid;
            try {
                rid = TestCtx.key(r.getResultItem().getItem());
            } catch (Throwable t) {
                continue;
            }
            if (arg.contains(":") ? !rid.equals(arg) : !rid.contains(arg)) continue;
            n++;
            c.emit(c.line().kv("id", r.getId()).kv("result", result(r)).kv("serializer", serializer(r)));
        }
        c.emit(c.line().kv("item", arg).kv("n", n));
    }

    /** food ITEM: nutrition, saturation modifier, saturation points, flags, effects (id:amp:ticks:chance). Read
     * through the stack-sensitive Forge call, as the game eats it. */
    private static void food(TestCtx c) {
        ItemStack st = TestCtx.stack(c.args.word("item"));
        c.args.end();
        c.args.checkOpts();
        FoodProperties fp = st.getFoodProperties(null);
        if (fp == null) {
            c.emit(c.line().kv("item", TestCtx.describe(st)).kv("food", false));
            return;
        }
        StringBuilder ef = new StringBuilder();
        for (Pair<MobEffectInstance, Float> pr : fp.getEffects()) {
            MobEffectInstance i = pr.getFirst();
            ef.append(ef.length() == 0 ? "" : ",").append(ForgeRegistries.MOB_EFFECTS.getKey(i.getEffect())).append(':')
                    .append(i.getAmplifier()).append(':').append(i.getDuration()).append(':').append(pr.getSecond());
        }
        c.emit(c.line().kv("item", TestCtx.describe(st)).kv("food", true).kv("nutrition", fp.getNutrition())
                .kv("modifier", fp.getSaturationModifier()).kv("saturation", fp.getNutrition() * fp.getSaturationModifier() * 2f)
                .kv("meat", fp.isMeat()).kv("always", fp.canAlwaysEat()).kv("fast", fp.isFastFood())
                .kv("use_ticks", st.getUseDuration()).kv("effects", ef.length() == 0 ? "-" : ef.toString()));
    }

    /** item ITEM: class, max damage, max stack size, use duration, rarity, block it places. */
    private static void item(TestCtx c) {
        ItemStack st = TestCtx.stack(c.args.word("item"));
        c.args.end();
        c.args.checkOpts();
        Item it = st.getItem();
        Block b = Block.byItem(it);
        c.emit(c.line().kv("item", TestCtx.key(it)).kv("class", it.getClass().getName()).kv("max_damage", st.getMaxDamage())
                .kv("max_stack", st.getMaxStackSize()).kv("use_ticks", st.getUseDuration()).kv("rarity", st.getRarity())
                .kv("block", b == net.minecraft.world.level.block.Blocks.AIR ? "none" : String.valueOf(ForgeRegistries.BLOCKS.getKey(b))));
    }

    /** attrs ITEM: attribute modifiers of a fresh stack per equipment slot (tune_tools / tune_armor checks). */
    private static void attrs(TestCtx c) {
        ItemStack st = TestCtx.stack(c.args.word("item"));
        c.args.end();
        c.args.checkOpts();
        int n = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            Multimap<Attribute, AttributeModifier> mods = st.getAttributeModifiers(slot);
            for (Map.Entry<Attribute, AttributeModifier> e : mods.entries()) {
                n++;
                c.emit(c.line().kv("slot", slot.getName()).kv("attribute", ForgeRegistries.ATTRIBUTES.getKey(e.getKey()))
                        .kv("op", e.getValue().getOperation()).kv("amount", e.getValue().getAmount()));
            }
        }
        c.emit(c.line().kv("item", TestCtx.describe(st)).kv("n", n));
    }

    private static Registry<?> registry(TestCtx c, String kind) {
        return switch (kind) {
            case "item" -> Registry.ITEM;
            case "block" -> Registry.BLOCK;
            case "fluid" -> Registry.FLUID;
            case "entity" -> Registry.ENTITY_TYPE;
            case "block_entity_type" -> Registry.BLOCK_ENTITY_TYPE;
            case "effect" -> Registry.MOB_EFFECT;
            case "potion" -> Registry.POTION;
            case "enchantment" -> Registry.ENCHANTMENT;
            case "recipe_serializer" -> Registry.RECIPE_SERIALIZER;
            case "menu" -> Registry.MENU;
            case "sound" -> Registry.SOUND_EVENT;
            case "attribute" -> Registry.ATTRIBUTE;
            case "biome" -> c.server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
            default -> throw new TestArgs.Bad("kind must be item, block, fluid, entity, block_entity_type, effect, potion, "
                    + "enchantment, recipe_serializer, menu, sound, attribute or biome");
        };
    }

    /** reg KIND REGEX [max=80]: registry ids matching the regex (find), with the entry's class; then the count. */
    private static void reg(TestCtx c) {
        String kind = c.args.word("kind");
        String regex = c.args.word("regex");
        c.args.end();
        c.args.checkOpts("max");
        int max = c.args.optInt("max", 80, 1, 100_000);
        Pattern p;
        try {
            p = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new TestArgs.Bad("bad regex " + regex);
        }
        Registry<?> reg = registry(c, kind);
        List<ResourceLocation> ids = new ArrayList<>(reg.keySet());
        ids.sort(null);
        int n = 0;
        for (ResourceLocation id : ids) {
            if (!p.matcher(id.toString()).find()) continue;
            if (++n <= max) c.emit(c.line().kv("kind", kind).kv("id", id).kv("class", reg.get(id).getClass().getName()));
        }
        c.emit(c.line().kv("kind", kind).kv("regex", regex).kv("n", n));
    }

    /** potion ID [ING,ING,...]: registered?; with ingredients, what each brews a bottle of it into (Forge registry). */
    private static void potion(TestCtx c) {
        ResourceLocation id = TestCtx.id(c.args.word("potion"));
        String ings = c.args.wordOr(null);
        c.args.end();
        c.args.checkOpts("base");
        boolean has = Registry.POTION.containsKey(id);
        c.emit(c.line().kv("potion", id).kv("registered", has));
        if (!has || ings == null) return;
        Potion potion = Registry.POTION.get(id);
        Item bottle = TestCtx.item(c.args.opt("base", "minecraft:potion"));
        ItemStack base = PotionUtils.setPotion(new ItemStack(bottle == Items.AIR ? Items.POTION : bottle), potion);
        for (String ing : ings.split(",")) {
            ItemStack out = BrewingRecipeRegistry.getOutput(base, TestCtx.stack(ing));
            c.emit(c.line().kv("potion", id).kv("ingredient", ing).kv("output", out.isEmpty() ? "nothing"
                    : TestCtx.key(out.getItem()) + " " + Registry.POTION.getKey(PotionUtils.getPotion(out))));
        }
    }

    /** tag KIND TAG: members of a tag (kinds as for reg). */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void tag(TestCtx c) {
        String kind = c.args.word("kind");
        ResourceLocation id = TestCtx.id(c.args.word("tag").replaceFirst("^#", ""));
        c.args.end();
        c.args.checkOpts();
        Registry reg = registry(c, kind);
        TagKey key = TagKey.create(reg.key(), id);
        List<String> ids = new ArrayList<>();
        for (Object h : reg.getTagOrEmpty(key)) {
            ((net.minecraft.core.Holder<?>) h).unwrapKey().ifPresent(k -> ids.add(((ResourceKey<?>) k).location().toString()));
        }
        ids.sort(null);
        c.emit(c.line().kv("kind", kind).kv("tag", id).kv("n", ids.size()).kv("ids", ids.isEmpty() ? "-" : String.join(",", ids)));
    }

    /** tagsof ID: item tags of the item and block tags of the block with the same id. */
    private static void tagsof(TestCtx c) {
        ResourceLocation id = TestCtx.id(c.args.word("id"));
        c.args.end();
        c.args.checkOpts();
        Map<String, String> found = new TreeMap<>();
        if (ForgeRegistries.ITEMS.containsKey(id)) {
            List<String> t = new ArrayList<>();
            ForgeRegistries.ITEMS.getValue(id).builtInRegistryHolder().tags().forEach(k -> t.add(k.location().toString()));
            t.sort(null);
            found.put("item_tags", t.isEmpty() ? "-" : String.join(",", t));
        }
        if (ForgeRegistries.BLOCKS.containsKey(id)) {
            List<String> t = new ArrayList<>();
            ForgeRegistries.BLOCKS.getValue(id).builtInRegistryHolder().tags().forEach(k -> t.add(k.location().toString()));
            t.sort(null);
            found.put("block_tags", t.isEmpty() ? "-" : String.join(",", t));
        }
        if (found.isEmpty()) throw new TestArgs.Bad("no item or block " + id);
        TestLine l = c.line().kv("id", id);
        found.forEach(l::kv);
        c.emit(l);
    }

    /** states: block-state id count against the 2^20 ceiling. */
    private static void states(TestCtx c) {
        c.args.end();
        c.args.checkOpts();
        int n = Block.BLOCK_STATE_REGISTRY.size();
        c.emit(c.line().kv("states", n).kv("ceiling", 1 << 20).kv("headroom", (1 << 20) - n));
    }
}
