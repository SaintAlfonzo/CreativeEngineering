package com.SaintAlfonzo.creativeengineering.compat;

import com.mojang.logging.LogUtils;
import com.redlimerl.detailab.ClientInitializer;
import com.redlimerl.detailab.DetailArmorBar;
import com.redlimerl.detailab.api.DetailArmorBarAPI;
import com.redlimerl.detailab.api.render.ArmorBarRenderManager;
import com.redlimerl.detailab.api.render.TextureOffset;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.Arrays;

/**
 * Registers custom Detail Armor Bar (modid "detailab") icons/colors for modded armor sets that
 * ship with no built-in entry there, so they render as plain grey icons on the vanilla armor
 * bar otherwise. DetailArmorBarAPI is that mod's own public, intentional extension point (see
 * its "For Developer (API Document)" page) - this is a plain client-setup registration call,
 * not a mixin.
 *
 * <p>Traced from the decompiled 2.6.4 jar: {@code ArmorBarRenderManager}'s color is a straight
 * RGBA tint ({@code RenderSystem#setShaderColor} under the icon draw call), not a fixed
 * palette - any {@link Color} works. Vanilla leather armor already proves this: DetailArmorBar's
 * own {@code ClientInitializer} reads the dye color off the leather ItemStack and tints its
 * neutral grey icon with it. {@link #recolor} uses the same technique on the plain iron icon
 * instead (sheet coords x=63 full / x=54 half - the same offset {@code CustomArmorBar.DEFAULT}
 * itself falls back to for unregistered armor), since it's equally unpatterned/tint-friendly and
 * reads as a more neutral "unranked" base than leather's own icon. One quirk carried over from
 * its draw call: alpha is divided by 100, not 255, so anything above ~100 just clamps to fully
 * opaque.
 *
 * <p>Two ways in:
 * <ul>
 *   <li>{@link #recolor} - neutral icon, custom color, zero new art.</li>
 *   <li>{@link #texture} - custom icon coordinates. Defaults to DetailArmorBar's own
 *       {@code armor_bar.png} (128x128) when no texture is given, which lets you pick one of
 *       its existing baked-in material icons at a different offset before bespoke art exists;
 *       pass a real {@link ResourceLocation} once it does.</li>
 * </ul>
 *
 * <p><b>Same-material grouping:</b> DetailArmorBar decides whether to draw a divider between two
 * adjacent armor-bar icons by reference-comparing the {@code CustomArmorBar} each one resolved to
 * ({@code ArmorBarRenderer#render}: {@code am1.getSecond() == am2.getSecond()}), not by comparing
 * how they look. Two pieces with visually identical icons still get a divider between them if
 * they came from two different {@code register()} calls. So every vanilla-tier material that
 * modded armor reuses (diamond, iron, gold, chainmail, netherite, turtle, leather) is registered
 * here as ONE call covering the vanilla items *and* every modded piece built on that material -
 * see {@link #register}'s "Vanilla-material groups" section. Registering the vanilla items
 * ourselves (rather than reusing whatever {@code CustomArmorBar} instance DetailArmorBar's own
 * {@code ClientInitializer} already put in the map) is required, not optional: this mod loads
 * "AFTER" detailab (mods.toml), so our registration simply overwrites theirs in the map with a
 * render function that draws the identical icon - a no-op visually, but now one shared instance.
 */
public class DetailArmorBarCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String MODID = "detailab";

    // Plain iron icon - same offset CustomArmorBar.DEFAULT falls back to for unregistered armor
    // (ClientInitializer#initializeModFeatures) - unpatterned, so it tints cleanly like leather's
    // own neutral icon does, without leather's "unranked" connotation.
    private static final int NEUTRAL_ICON_X = 63;
    private static final int NEUTRAL_ICON_HALF_X = 54;
    // Outline coordinates every one of DetailArmorBar's built-in material groups shares, inside
    // its own sheet - only valid when reusing that sheet (i.e. the 4-arg texture() overload).
    private static final TextureOffset DEFAULT_OUTLINE = new TextureOffset(9, 0);
    private static final TextureOffset DEFAULT_OUTLINE_HALF = new TextureOffset(27, 0);
    // Real netherite icon (confirmed against patcherMod's own MCP->SRG mapping, not just visual
    // inspection: Items.NETHERITE_HELMET/etc = f_42480_/f_42481_/f_42482_/f_42483_, registered at
    // this exact offset in ClientInitializer). Untinted texture-reuse for "matches real netherite".
    private static final int NETHERITE_ICON_X = 9;
    private static final int NETHERITE_ICON_HALF_X = 0;
    // Real icon offsets for the other vanilla ArmorMaterials, traced the same way as netherite
    // above out of DetailArmorBar's own decompiled ClientInitializer#initializeModFeatures (each
    // group's TextureOffset args, matched to Items fields via patcherMod's own MCP->SRG mapping).
    // Iron has no entry here since NEUTRAL_ICON_X/NEUTRAL_ICON_HALF_X above already IS the real
    // iron icon's own offset (ClientInitializer registers vanilla iron there too, just under its
    // own CustomArmorBar instance rather than literally the DEFAULT singleton - see register()'s
    // iron group).
    private static final int DIAMOND_ICON_X = 27;
    private static final int DIAMOND_ICON_HALF_X = 18;
    private static final int CHAINMAIL_ICON_X = 81;
    private static final int CHAINMAIL_ICON_HALF_X = 72;
    private static final int GOLD_ICON_X = 99;
    private static final int GOLD_ICON_HALF_X = 90;
    private static final int LEATHER_ICON_X = 117;
    private static final int LEATHER_ICON_HALF_X = 108;
    private static final int TURTLE_ICON_X = 45;
    private static final int TURTLE_ICON_HALF_X = 36;

    // Bespoke "_tint.png" sheets (not DetailArmorBar's own armor_bar.png) - one per armor set,
    // hand-painted and already fully colored (untinted draw, Color.WHITE). Every one shares the
    // same layout: half icon at (0,0), full icon at (9,0), both 9x9, padded out to a 27x9 canvas
    // so the low-durability/mending outline overlay (always drawn, every frame, per
    // CustomArmorBar#drawOutLine) has a genuinely blank 9x9 tile to point at instead of
    // re-sampling one of the two real icons - same "guaranteed-transparent" technique this file
    // originally used for just the dragon sheet, now shared by all of them.
    private static final int TINT_SHEET_WIDTH = 27;
    private static final int TINT_SHEET_HEIGHT = 9;
    private static final TextureOffset TINT_ICON_FULL = new TextureOffset(9, 0);
    private static final TextureOffset TINT_ICON_HALF = new TextureOffset(0, 0);
    private static final TextureOffset TINT_NO_OUTLINE = new TextureOffset(18, 0);

    private static final ResourceLocation FARADAY_ARMOR_BAR = tintSheet("faraday_tint.png");
    private static final ResourceLocation PRISMARINE_ARMOR_BAR = tintSheet("prismarine_tint.png");
    private static final ResourceLocation FELSIC_ARMOR_BAR = tintSheet("felsic_tint.png");
    private static final ResourceLocation ECHO_ARMOR_BAR = tintSheet("echo_tint.png");
    private static final ResourceLocation DRAGON_ARMOR_BAR = tintSheet("dragon_tint.png");
    private static final ResourceLocation MANASTEEL_ARMOR_BAR = tintSheet("manasteel_tint.png");
    private static final ResourceLocation ELEMENTIUM_ARMOR_BAR = tintSheet("elementium_tint.png");
    private static final ResourceLocation TERRASTEEL_ARMOR_BAR = tintSheet("terrasteel_tint.png");
    private static final ResourceLocation MANAWEAVE_ARMOR_BAR = tintSheet("manaweave_tint.png");
    private static final ResourceLocation BONE_ARMOR_BAR = tintSheet("bone_tint.png");
    private static final ResourceLocation SLIME_ARMOR_BAR = tintSheet("slime_tint.png");
    private static final ResourceLocation WITHER_ARMOR_BAR = tintSheet("wither_tint.png");

    private static ResourceLocation tintSheet(String fileName) {
        return new ResourceLocation("creativeengineering", "textures/gui/" + fileName);
    }

    private DetailArmorBarCompat() {}
    public static void register() {
        if (!ModList.get().isLoaded(MODID)) {
            LOGGER.info("DetailArmorBar not loaded, skipping armor bar registrations");
            return;
        }

        // Felsic Gear.
        tintedArmor(FELSIC_ARMOR_BAR,
                "felsic_gear:felsic_helmet", "felsic_gear:felsic_chestplate",
                "felsic_gear:felsic_leggings", "felsic_gear:felsic_boots");

        // Create: Deep Dark echo armor.
        tintedArmor(ECHO_ARMOR_BAR,
                "create_deep_dark:echo_armor_helmet", "create_deep_dark:echo_armor_chestplate",
                "create_deep_dark:echo_armor_leggings", "create_deep_dark:echo_armor_boots");

        // DragonLoot dragon armor.
        tintedArmor(DRAGON_ARMOR_BAR,
                "dragonloot:dragon_helmet", "dragonloot:dragon_chestplate", "dragonloot:upgraded_dragon_chestplate",
                "dragonloot:dragon_leggings", "dragonloot:dragon_boots");

        // Botania mana-tier armor
        tintedArmor(MANASTEEL_ARMOR_BAR,
                "botania:manasteel_helmet", "botania:manasteel_chestplate",
                "botania:manasteel_leggings", "botania:manasteel_boots");
        tintedArmor(ELEMENTIUM_ARMOR_BAR,
                "botania:elementium_helmet", "botania:elementium_chestplate",
                "botania:elementium_leggings", "botania:elementium_boots");
        tintedArmor(TERRASTEEL_ARMOR_BAR,
                "botania:terrasteel_helmet", "botania:terrasteel_chestplate",
                "botania:terrasteel_leggings", "botania:terrasteel_boots");
        tintedArmor(MANAWEAVE_ARMOR_BAR,
                "botania:manaweave_helmet", "botania:manaweave_chestplate",
                "botania:manaweave_leggings", "botania:manaweave_boots");

        // Immersive Engineering 
        tintedArmor(FARADAY_ARMOR_BAR,
                "immersiveengineering:armor_faraday_head", "immersiveengineering:armor_faraday_chest",
                "immersiveengineering:armor_faraday_legs", "immersiveengineering:armor_faraday_feet");

        // Immersive Armors
        tintedArmor(BONE_ARMOR_BAR,
                "immersive_armors:bone_helmet", "immersive_armors:bone_chestplate",
                "immersive_armors:bone_leggings", "immersive_armors:bone_boots");
        tintedArmor(SLIME_ARMOR_BAR,
                "immersive_armors:slime_helmet", "immersive_armors:slime_chestplate",
                "immersive_armors:slime_leggings", "immersive_armors:slime_boots");
        tintedArmor(WITHER_ARMOR_BAR,
                "immersive_armors:wither_helmet", "immersive_armors:wither_chestplate",
                "immersive_armors:wither_leggings", "immersive_armors:wither_boots");
        tintedArmor(PRISMARINE_ARMOR_BAR,
                "immersive_armors:prismarine_helmet", "immersive_armors:prismarine_chestplate",
                "immersive_armors:prismarine_leggings", "immersive_armors:prismarine_boots");

        // Immersive Armors
        recolor(new Color(0x513A25),
                "immersive_armors:steampunk_helmet", "immersive_armors:steampunk_chestplate",
                "immersive_armors:steampunk_leggings", "immersive_armors:steampunk_boots");
        recolor(new Color(0x543426),
                "immersive_armors:wooden_helmet", "immersive_armors:wooden_chestplate",
                "immersive_armors:wooden_leggings", "immersive_armors:wooden_boots");

        // Cardboard recolor
        recolor(new Color(0xB9885C),
                "dustrial_decor:cardboard_helmet", "dustrial_decor:cardboard_chestplate",
                "dustrial_decor:cardboard_leggings", "dustrial_decor:cardboard_boots");

        // IE steel / PneumaticCraft compressed iron
        recolor(new Color(0x848484),
                "immersiveengineering:armor_steel_head", "immersiveengineering:armor_steel_chest",
                "immersiveengineering:armor_steel_legs", "immersiveengineering:armor_steel_feet",
                "pneumaticcraft:compressed_iron_helmet", "pneumaticcraft:compressed_iron_chestplate",
                "pneumaticcraft:compressed_iron_boots", "pneumaticcraft:compressed_iron_leggings",
                "pneumaticcraft:pneumatic_helmet", "pneumaticcraft:pneumatic_chestplate",
                "pneumaticcraft:pneumatic_boots", "pneumaticcraft:pneumatic_leggings");

        // --- Vanilla-material groups --------------------------------------------------------
        int diamondY = 9 + DetailArmorBar.isVanillaTexture();
        texture(null, new TextureOffset(DIAMOND_ICON_X, diamondY), new TextureOffset(DIAMOND_ICON_HALF_X, diamondY), Color.WHITE,
                "minecraft:diamond_helmet", "minecraft:diamond_chestplate",
                "minecraft:diamond_leggings", "minecraft:diamond_boots",
                "creategoggles:diamond_backtank", "creategoggles:goggle_diamond_helmet");

        int chainmailY = 9 + DetailArmorBar.isVanillaTexture();
        texture(null, new TextureOffset(CHAINMAIL_ICON_X, chainmailY), new TextureOffset(CHAINMAIL_ICON_HALF_X, chainmailY), Color.WHITE,
                "minecraft:chainmail_helmet", "minecraft:chainmail_chestplate",
                "minecraft:chainmail_leggings", "minecraft:chainmail_boots",
                "creategoggles:chainmail_backtank", "creategoggles:goggle_chainmail_helmet");

        int goldY = 9 + DetailArmorBar.isVanillaTexture();
        texture(null, new TextureOffset(GOLD_ICON_X, goldY), new TextureOffset(GOLD_ICON_HALF_X, goldY), Color.WHITE,
                "minecraft:golden_helmet", "minecraft:golden_chestplate",
                "minecraft:golden_leggings", "minecraft:golden_boots",
                "creategoggles:golden_backtank", "creategoggles:goggle_golden_helmet");

        int netheriteY = 9 + DetailArmorBar.isVanillaTexture();
        texture(null, new TextureOffset(NETHERITE_ICON_X, netheriteY), new TextureOffset(NETHERITE_ICON_HALF_X, netheriteY), Color.WHITE,
                "minecraft:netherite_helmet", "minecraft:netherite_chestplate",
                "minecraft:netherite_leggings", "minecraft:netherite_boots",
                "create:netherite_backtank", "create:netherite_diving_helmet", "create:netherite_diving_boots",
                "creategoggles:goggle_netherite_helmet", "creategoggles:goggle_netherite_diving_helmet",
                "creategbd:beam_reactor_helmet");

        int turtleY = 9 + DetailArmorBar.isVanillaTexture();
        texture(null, new TextureOffset(TURTLE_ICON_X, turtleY), new TextureOffset(TURTLE_ICON_HALF_X, turtleY), Color.WHITE,
                "minecraft:turtle_helmet", "creategoggles:goggle_turtle_helmet");

        int ironY = 9 + DetailArmorBar.isVanillaTexture();
        texture(null, new TextureOffset(NEUTRAL_ICON_X, ironY), new TextureOffset(NEUTRAL_ICON_HALF_X, ironY), Color.WHITE,
                "minecraft:iron_helmet", "minecraft:iron_chestplate",
                "minecraft:iron_leggings", "minecraft:iron_boots",
                "creategoggles:iron_backtank", "creategoggles:goggle_iron_helmet");

        // Leather - dynamic per-stack dye color 
        int leatherY = 9 + DetailArmorBar.isVanillaTexture();
        dyedTexture(new TextureOffset(LEATHER_ICON_X, leatherY), new TextureOffset(LEATHER_ICON_HALF_X, leatherY),
                "minecraft:leather_helmet", "minecraft:leather_chestplate",
                "minecraft:leather_leggings", "minecraft:leather_boots",
                "creategoggles:leather_backtank", "creategoggles:goggle_leather_helmet",
                "immersive_armors:divine_helmet", "immersive_armors:divine_chestplate",
                "immersive_armors:divine_leggings", "immersive_armors:divine_boots",
                "immersive_armors:robe_helmet", "immersive_armors:robe_chestplate",
                "immersive_armors:robe_leggings", "immersive_armors:robe_boots");

        // Copper - Weathered gear obtainable?
        Color copperBase = new Color(0xC26B4C);
        recolor(copperBase,
                "everythingcopper:copper_helmet", "everythingcopper:copper_chestplate",
                "everythingcopper:copper_leggings", "everythingcopper:copper_boots",
                "create:copper_backtank", "create:copper_diving_helmet", "create:copper_diving_boots",
                "creategoggles:goggle_diving_helmet");
        recolor(new Color(0xA87762),
                "everythingcopper:exposed_copper_helmet", "everythingcopper:exposed_copper_chestplate",
                "everythingcopper:exposed_copper_leggings", "everythingcopper:exposed_copper_boots");
        recolor(new Color(0x66A977),
                "everythingcopper:weathered_copper_helmet", "everythingcopper:weathered_copper_chestplate",
                "everythingcopper:weathered_copper_leggings", "everythingcopper:weathered_copper_boots");
        recolor(new Color(0x53A178),
                "everythingcopper:oxidized_copper_helmet", "everythingcopper:oxidized_copper_chestplate",
                "everythingcopper:oxidized_copper_leggings", "everythingcopper:oxidized_copper_boots");
    }

    /** Neutral icon, tinted with {@code color} **/
    public static void recolor(Color color, String... itemIds) {
        int y = 9 + DetailArmorBar.isVanillaTexture();
        texture(null, new TextureOffset(NEUTRAL_ICON_X, y), new TextureOffset(NEUTRAL_ICON_HALF_X, y), color, itemIds);
    }

    /** Bespoke, already-colored "_tint.png" sheet laid out per {@link #TINT_ICON_FULL}/{@link #TINT_ICON_HALF}. */
    private static void tintedArmor(ResourceLocation sheet, String... itemIds) {
        texture(sheet, TINT_SHEET_WIDTH, TINT_SHEET_HEIGHT, TINT_ICON_FULL, TINT_ICON_HALF,
                TINT_NO_OUTLINE, TINT_NO_OUTLINE, Color.WHITE, itemIds);
    }

    /**
     * Draws {@code full}/{@code half} from {@code texture} (or DetailArmorBar's own
     * armor_bar.png when {@code texture} is null), tinted with {@code color} (pass
     * {@link Color#WHITE} for untinted). The low-durability/mending outline overlay is drawn
     * from that same sheet at the shared {@link #DEFAULT_OUTLINE} coordinates - fine when
     * reusing DetailArmorBar's own sheet, but a genuinely custom sheet needs its own matching
     * neutral outline icon there, or use the full overload to point it elsewhere.
     */
    public static void texture(@Nullable ResourceLocation texture, TextureOffset full, TextureOffset half, Color color, String... itemIds) {
        texture(texture, 128, 128, full, half, DEFAULT_OUTLINE, DEFAULT_OUTLINE_HALF, color, itemIds);
    }

    /** Full control, for a bespoke sheet with its own dimensions and outline icon placement. */
    public static void texture(@Nullable ResourceLocation texture, int textureWidth, int textureHeight,
                                TextureOffset full, TextureOffset half, TextureOffset outline, TextureOffset outlineHalf,
                                Color color, String... itemIds) {
        ResourceLocation sheet = texture != null ? texture : ClientInitializer.GUI_ARMOR_BAR;
        ArmorItem[] armor = resolveArmor(itemIds);
        if (armor.length == 0) return;

        DetailArmorBarAPI.customArmorBarBuilder()
                .armor(armor)
                .render(itemStack -> new ArmorBarRenderManager(sheet, textureWidth, textureHeight, full, half, outline, outlineHalf, color))
                .register();
    }

    /**
     * Same as {@link #texture} but tints per-{@link net.minecraft.world.item.ItemStack} off a
     * {@link DyeableLeatherItem}'s own dye color instead of one fixed {@link Color} - mirrors
     * DetailArmorBar's own ClientInitializer#initializeModFeatures entry for vanilla leather armor.
     */
    private static void dyedTexture(TextureOffset full, TextureOffset half, String... itemIds) {
        ArmorItem[] armor = resolveArmor(itemIds);
        if (armor.length == 0) return;

        DetailArmorBarAPI.customArmorBarBuilder()
                .armor(armor)
                .render(itemStack -> new ArmorBarRenderManager(ClientInitializer.GUI_ARMOR_BAR, 128, 128, full, half,
                        DEFAULT_OUTLINE, DEFAULT_OUTLINE_HALF,
                        new Color(((DyeableLeatherItem) itemStack.getItem()).getColor(itemStack))))
                .register();
    }

    private static ArmorItem[] resolveArmor(String... itemIds) {
        ArmorItem[] armor = new ArmorItem[itemIds.length];
        int count = 0;
        for (String id : itemIds) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
            if (!(item instanceof ArmorItem armorItem)) {
                LOGGER.warn("DetailArmorBarCompat: '{}' is not a registered ArmorItem, skipping", id);
                continue;
            }
            armor[count++] = armorItem;
        }
        return count == itemIds.length ? armor : Arrays.copyOf(armor, count);
    }
}
