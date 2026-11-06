package com.SaintAlfonzo.creativedelight.rustic.registry;

import com.SaintAlfonzo.creativedelight.rustic.RusticDelightMod;
import com.SaintAlfonzo.creativedelight.rustic.content.CoffeeCropBlock;
import com.SaintAlfonzo.creativedelight.rustic.content.CottonCropBlock;
import com.SaintAlfonzo.creativedelight.rustic.content.PancakeStackBlock;
import com.SaintAlfonzo.creativedelight.rustic.content.PepperMedleyBlock;
import com.SaintAlfonzo.creativedelight.rustic.content.RiceRollRoyaleBlock;
import com.SaintAlfonzo.creativedelight.rustic.content.RusticCropBlock;
import com.SaintAlfonzo.creativedelight.rustic.content.RusticWildCropBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.block.PieBlock;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/** The blocks of Rustic Delight 1.7.0 (every 1.3.0 id among them), in upstream's order. */
public final class RDBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, RusticDelightMod.MODID);

    /** Bell pepper colours, base three first; Pale seeds grow orange, white, pink; Dark seeds blue, purple, black. */
    public static final List<String> COLOURS = List.of("green", "yellow", "red", "orange", "white", "pink", "blue", "purple", "black");

    public static final RegistryObject<Block> COTTON_SEEDS_BAG = bag("cotton_seeds_bag");
    public static final RegistryObject<Block> BELL_PEPPER_SEEDS_BAG = bag("bell_pepper_seeds_bag");
    public static final RegistryObject<Block> PALE_BELL_PEPPER_SEEDS_BAG = bag("pale_bell_pepper_seeds_bag");
    public static final RegistryObject<Block> DARK_BELL_PEPPER_SEEDS_BAG = bag("dark_bell_pepper_seeds_bag");
    public static final RegistryObject<Block> COFFEE_BEANS_BAG = bag("coffee_beans_bag");
    public static final RegistryObject<Block> ROASTED_COFFEE_BEANS_BAG = bag("roasted_coffee_beans_bag");

    public static final RegistryObject<Block> COTTON_BOLL_CRATE = crate("cotton_boll_crate");
    /** {@code bell_pepper_<colour>_crate}, in {@link #COLOURS} order. */
    public static final Map<String, RegistryObject<Block>> PEPPER_CRATES = byColour(colour -> crate("bell_pepper_" + colour + "_crate"));
    public static final RegistryObject<Block> CALAMARI_CRATE = crate("calamari_crate");
    /** The pack's own crate for its glowing calamari (TODO creativedelight/members #25); not in upstream. */
    public static final RegistryObject<Block> GLOWING_CALAMARI_CRATE = crate("glowing_calamari_crate");
    /** The giant peppers, {@code bell_pepper_<colour>_block}. */
    public static final Map<String, RegistryObject<Block>> GIANT_PEPPERS = byColour(colour -> BLOCKS.register("bell_pepper_" + colour + "_block",
            () -> new Block(Properties.copy(Blocks.MELON))));

    public static final RegistryObject<Block> SYRUP_CHEESECAKE = cheesecake("syrup_cheesecake", () -> RDItems.SYRUP_CHEESECAKE_SLICE.get());
    public static final RegistryObject<Block> CHERRY_BLOSSOM_CHEESECAKE = cheesecake("cherry_blossom_cheesecake", () -> RDItems.CHERRY_BLOSSOM_CHEESECAKE_SLICE.get());
    public static final RegistryObject<Block> COFFEE_CHEESECAKE = cheesecake("coffee_cheesecake", () -> RDItems.COFFEE_CHEESECAKE_SLICE.get());

    /**
     * The plain plate serves Supplementaries' pancake: Rustic's own plain single is not registered (owner 2026-11-01,
     * TODO creativedelight/members #15). Air when Supplementaries is absent, then the plate gives nothing back.
     */
    public static final RegistryObject<Block> PANCAKES = pancakes("pancakes",
            () -> ForgeRegistries.ITEMS.getValue(new ResourceLocation("supplementaries", "pancake")));
    public static final RegistryObject<Block> HONEY_PANCAKES = pancakes("honey_pancakes", () -> RDItems.HONEY_PANCAKE.get());
    public static final RegistryObject<Block> CHOCOLATE_PANCAKES = pancakes("chocolate_pancakes", () -> RDItems.CHOCOLATE_PANCAKE.get());
    public static final RegistryObject<Block> CHERRY_BLOSSOM_PANCAKES = pancakes("cherry_blossom_pancakes", () -> RDItems.CHERRY_BLOSSOM_PANCAKE.get());
    public static final RegistryObject<Block> VEGETABLE_PANCAKES = pancakes("vegetable_pancakes", () -> RDItems.VEGETABLE_PANCAKE.get());
    public static final RegistryObject<Block> PUMPKIN_PANCAKES = pancakes("pumpkin_pancakes", () -> RDItems.PUMPKIN_PANCAKE.get());
    public static final RegistryObject<Block> COFFEE_PANCAKES = pancakes("coffee_pancakes", () -> RDItems.COFFEE_PANCAKE.get());

    public static final RegistryObject<Block> RICE_ROLL_ROYALE = BLOCKS.register("rice_roll_royale", () -> new RiceRollRoyaleBlock(Properties.copy(Blocks.CAKE)));
    public static final RegistryObject<Block> BELL_PEPPER_MEDLEY = medley("bell_pepper_medley", "green", "yellow", "red");
    public static final RegistryObject<Block> PALE_BELL_PEPPER_MEDLEY = medley("pale_bell_pepper_medley", "orange", "white", "pink");
    public static final RegistryObject<Block> DARK_BELL_PEPPER_MEDLEY = medley("dark_bell_pepper_medley", "blue", "purple", "black");

    public static final RegistryObject<Block> WILD_COTTON = wild("wild_cotton", MobEffects.MOVEMENT_SPEED, 10);
    public static final RegistryObject<Block> WILD_BELL_PEPPERS = wild("wild_bell_peppers", MobEffects.INVISIBILITY, 10);
    public static final RegistryObject<Block> WILD_PALE_BELL_PEPPERS = wild("wild_pale_bell_peppers", MobEffects.NIGHT_VISION, 10);
    public static final RegistryObject<Block> WILD_DARK_BELL_PEPPERS = wild("wild_dark_bell_peppers", MobEffects.FIRE_RESISTANCE, 10);
    public static final RegistryObject<Block> WILD_COFFEE = wild("wild_coffee", MobEffects.JUMP, 12);

    public static final RegistryObject<Block> COTTON_CROP = BLOCKS.register("cotton", () -> new CottonCropBlock(cropProperties()));
    public static final RegistryObject<Block> BELL_PEPPER_CROP = BLOCKS.register("bell_peppers",
            () -> new RusticCropBlock(cropProperties(), RDItems.BELL_PEPPER_SEEDS));
    public static final RegistryObject<Block> PALE_BELL_PEPPER_CROP = BLOCKS.register("pale_bell_peppers",
            () -> new RusticCropBlock(cropProperties(), RDItems.PALE_BELL_PEPPER_SEEDS));
    public static final RegistryObject<Block> DARK_BELL_PEPPER_CROP = BLOCKS.register("dark_bell_peppers",
            () -> new RusticCropBlock(cropProperties(), RDItems.DARK_BELL_PEPPER_SEEDS));
    public static final RegistryObject<Block> COFFEE_CROP = BLOCKS.register("coffee", () -> new CoffeeCropBlock(cropProperties()));

    public static final RegistryObject<Block> POTTED_WILD_COTTON = potted("potted_wild_cotton", WILD_COTTON);
    public static final RegistryObject<Block> POTTED_WILD_BELL_PEPPERS = potted("potted_wild_bell_peppers", WILD_BELL_PEPPERS);
    public static final RegistryObject<Block> POTTED_WILD_PALE_BELL_PEPPERS = potted("potted_wild_pale_bell_peppers", WILD_PALE_BELL_PEPPERS);
    public static final RegistryObject<Block> POTTED_WILD_DARK_BELL_PEPPERS = potted("potted_wild_dark_bell_peppers", WILD_DARK_BELL_PEPPERS);
    public static final RegistryObject<Block> POTTED_WILD_COFFEE = potted("potted_wild_coffee", WILD_COFFEE);

    private RDBlocks() {
    }

    private static Properties cropProperties() {
        return Properties.copy(Blocks.WHEAT);
    }

    private static RegistryObject<Block> bag(String name) {
        return BLOCKS.register(name, () -> new Block(Properties.copy(Blocks.WHITE_WOOL)));
    }

    private static RegistryObject<Block> crate(String name) {
        return BLOCKS.register(name, () -> new Block(Properties.of(Material.WOOD).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    }

    private static RegistryObject<Block> cheesecake(String name, Supplier<Item> slice) {
        return BLOCKS.register(name, () -> new PieBlock(Properties.copy(Blocks.CAKE), slice));
    }

    private static RegistryObject<Block> pancakes(String name, Supplier<Item> serving) {
        return BLOCKS.register(name, () -> new PancakeStackBlock(serving, Properties.copy(Blocks.CAKE)));
    }

    private static RegistryObject<Block> medley(String name, String... colours) {
        // RDItems is read inside the suppliers only: it is initialised after this class.
        List<Supplier<Item>> peppers = Arrays.stream(colours).<Supplier<Item>>map(colour -> () -> RDItems.STUFFED_PEPPERS.get(colour).get()).toList();
        return BLOCKS.register(name, () -> new PepperMedleyBlock(Properties.copy(Blocks.CAKE), peppers));
    }

    private static RegistryObject<Block> wild(String name, MobEffect stewEffect, int stewDuration) {
        return BLOCKS.register(name, () -> new RusticWildCropBlock(stewEffect, stewDuration, Properties.copy(Blocks.TALL_GRASS)));
    }

    private static RegistryObject<Block> potted(String name, RegistryObject<Block> plant) {
        return BLOCKS.register(name, () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, plant, Properties.copy(Blocks.POTTED_POPPY)));
    }

    static <T> Map<String, T> byColour(Function<String, T> factory) {
        Map<String, T> map = new LinkedHashMap<>();
        COLOURS.forEach(colour -> map.put(colour, factory.apply(colour)));
        return map;
    }
}
