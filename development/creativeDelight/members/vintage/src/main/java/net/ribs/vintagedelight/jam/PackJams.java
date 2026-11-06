// Pack addition (CreativeEngineering2, TODO dishes/jams #8). Own work, GPLv3; not part of Vintage Delight's MIT code.
package net.ribs.vintagedelight.jam;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.ribs.vintagedelight.VintageDelight;
import net.ribs.vintagedelight.block.ModBlocks;
import net.ribs.vintagedelight.block.custom.MasonJarBlock;
import net.ribs.vintagedelight.item.ModFoods;
import net.ribs.vintagedelight.item.ModItems;
import net.ribs.vintagedelight.item.custom.MasonJarItem;
import net.ribs.vintagedelight.item.custom.NutMilkJarItem;
import vectorwing.farmersdelight.common.item.ConsumableItem;

/**
 * The pack's jam line in Fruits Delight's shape, for the jams Fruits Delight does not have. Per jam: a mason jar of jam
 * (Vintage's placeable jar), a jam block (sticky, honey-like), a jello block (slime-like), a bowl of jello, the jam and
 * melted jello fluids (no buckets, as Fruits Delight's), and three cauldron stages (fruit, jam, jello; JamCauldrons).
 * Also: the nut milk jar, and one placed mason jar block per Fruits Delight jam (FdJamJars), so its jams stand on a
 * table as Vintage jars. A jar holds one bottle's worth (125 mB of fluid, 8 to a block).
 */
public final class PackJams {
    private PackJams() {}

    /** name: id stem; english: display stem; newJar: the jar item is registered here (older ones are in ModItems). */
    public record Jam(String name, String english, boolean newJar, boolean ownJamFluid) {}

    public static final List<Jam> JAMS = List.of(
        new Jam("ancient_fruit", "Ancient Fruit", true, true),
        new Jam("banana", "Banana", true, true),
        new Jam("cherry", "Cherry", false, true),
        new Jam("green_grape", "Green Grape", false, true),
        // Mulberry jam fluid is Create Central Kitchen's (create_central_kitchen:mulberry_jam)
        new Jam("mulberry", "Mulberry", true, false),
        new Jam("pepper", "Pepper", false, true),
        new Jam("raspberry", "Raspberry", false, true),
        new Jam("red_grape", "Red Grape", false, true),
        new Jam("salmonberry", "Salmonberry", false, true),
        new Jam("strawberry", "Strawberry", true, true));

    /** Fruits Delight's 22 jams (fruitsdelight:<name>_jelly), placed as fd_<name>_jam_jar. */
    public static final List<String> FD_JAMS = List.of("apple", "bayberry", "blueberry", "chorus", "cranberry", "durian",
        "fig", "glowberry", "hamimelon", "hawberry", "kiwi", "lemon", "lychee", "mango", "mangosteen", "melon", "orange",
        "peach", "pear", "persimmon", "pineapple", "sweetberry");

    /** Items in the creative tab go into ModItems.ITEMS; cauldron items (JEI display only) into this one. */
    static final DeferredRegister<Item> HIDDEN_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VintageDelight.MODID);
    static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(ForgeRegistries.Keys.FLUID_TYPES, VintageDelight.MODID);
    static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, VintageDelight.MODID);

    public static final class Line {
        public final Jam jam;
        public RegistryObject<Block> jarBlock;          // the placeable jar (vintagedelight:<name>_jam_jar)
        public Supplier<? extends Item> jarItem;
        public RegistryObject<Block> jamBlock, jelloBlock;
        public RegistryObject<Item> jello;
        public RegistryObject<Block> fruitCauldron, jamCauldron, jelloCauldron;
        public RegistryObject<FluidType> jamType, jelloType;
        public RegistryObject<ForgeFlowingFluid> jamSource, jamFlowing, jelloSource, jelloFlowing;

        Line(Jam jam) { this.jam = jam; }
    }

    public static final Map<String, Line> LINES = new LinkedHashMap<>();
    public static final List<RegistryObject<Block>> NEW_JAR_BLOCKS = new ArrayList<>();
    public static final Map<String, RegistryObject<Block>> FD_JAR_BLOCKS = new LinkedHashMap<>();
    public static RegistryObject<Block> NUT_MILK_JAR_BLOCK;
    public static RegistryObject<Item> NUT_MILK_JAR;

    private static boolean done;

    /** Called from the mod constructor before the registers fire. */
    public static void register(IEventBus bus) {
        if (done) return;
        done = true;
        boolean fd = ModList.get().isLoaded("fruitsdelight");
        for (Jam jam : JAMS) {
            Line l = new Line(jam);
            String n = jam.name();
            if (jam.newJar()) {
                RegistryObject<Item>[] item = new RegistryObject[1];
                l.jarBlock = ModBlocks.BLOCKS.register(n + "_jam_jar", () -> jar(() -> item[0].get()));
                item[0] = ModItems.ITEMS.register(n + "_jam_jar",
                    () -> new MasonJarItem(l.jarBlock.get(), ModItems.jarItem(ModFoods.FRUIT_JAM)));
                l.jarItem = item[0];
                NEW_JAR_BLOCKS.add(l.jarBlock);
            } else {
                ResourceLocation id = new ResourceLocation(VintageDelight.MODID, n + "_jam_jar");
                l.jarItem = () -> ForgeRegistries.ITEMS.getValue(id);
            }
            l.jamBlock = ModBlocks.BLOCKS.register(n + "_jam_block",
                () -> new JamStorageBlock(Properties.copy(Blocks.HONEY_BLOCK), n));
            ModItems.ITEMS.register(n + "_jam_block", () -> new BlockItem(l.jamBlock.get(), ModItems.props()));
            l.jelloBlock = ModBlocks.BLOCKS.register(n + "_jello_block",
                () -> new JelloStorageBlock(Properties.copy(Blocks.SLIME_BLOCK), n));
            ModItems.ITEMS.register(n + "_jello_block", () -> new BlockItem(l.jelloBlock.get(), ModItems.props()));
            l.jello = ModItems.ITEMS.register(n + "_jello", () -> new ConsumableItem(ModItems.props()
                .food(PackFoods.JELLO).craftRemainder(Items.BOWL).stacksTo(16)));

            if (jam.ownJamFluid()) {
                l.jamType = FLUID_TYPES.register(n + "_jam", () -> fluidType(n + "_jam_block"));
                l.jamSource = FLUIDS.register(n + "_jam", () -> new ForgeFlowingFluid.Source(fluidProps(l, true)));
                l.jamFlowing = FLUIDS.register("flowing_" + n + "_jam", () -> new ForgeFlowingFluid.Flowing(fluidProps(l, true)));
            }
            l.jelloType = FLUID_TYPES.register(n + "_jello", () -> fluidType(n + "_jello_block"));
            l.jelloSource = FLUIDS.register(n + "_jello", () -> new ForgeFlowingFluid.Source(fluidProps(l, false)));
            l.jelloFlowing = FLUIDS.register("flowing_" + n + "_jello", () -> new ForgeFlowingFluid.Flowing(fluidProps(l, false)));

            if (fd) {
                // The cauldron classes extend Fruits Delight's cauldron block: only touched when it is loaded
                l.fruitCauldron = cauldron(n + "_cauldron", JamCauldrons::fruitCauldron);
                l.jamCauldron = cauldron(n + "_jam_cauldron", JamCauldrons::levelCauldron);
                l.jelloCauldron = cauldron(n + "_jello_cauldron", JamCauldrons::levelCauldron);
            }
            LINES.put(n, l);
        }

        RegistryObject<Item>[] milk = new RegistryObject[1];
        NUT_MILK_JAR_BLOCK = ModBlocks.BLOCKS.register("nut_milk_jar", () -> jar(() -> milk[0].get()));
        milk[0] = ModItems.ITEMS.register("nut_milk_jar",
            () -> new NutMilkJarItem(NUT_MILK_JAR_BLOCK.get(), ModItems.jarItem(ModFoods.NUT_MILK)));
        NUT_MILK_JAR = milk[0];
        NEW_JAR_BLOCKS.add(NUT_MILK_JAR_BLOCK);

        if (fd) {
            for (String f : FD_JAMS) {
                ResourceLocation id = new ResourceLocation("fruitsdelight", f + "_jelly");
                FD_JAR_BLOCKS.put(f, ModBlocks.BLOCKS.register("fd_" + f + "_jam_jar",
                    () -> jar(() -> ForgeRegistries.ITEMS.getValue(id))));
            }
        }

        HIDDEN_ITEMS.register(bus);
        FLUID_TYPES.register(bus);
        FLUIDS.register(bus);
        if (fd) JamCauldrons.init(bus);
        FdJamJars.init(fd);
    }

    private static RegistryObject<Block> cauldron(String id, Supplier<Block> factory) {
        RegistryObject<Block> b = ModBlocks.BLOCKS.register(id, factory);
        HIDDEN_ITEMS.register(id, () -> new BlockItem(b.get(), new Item.Properties()));
        return b;
    }

    public static MasonJarBlock jar(Supplier<? extends Item> item) {
        return new MasonJarBlock(Properties.copy(Blocks.GLASS).strength(0.3F).sound(SoundType.GLASS), item);
    }

    private static ForgeFlowingFluid.Properties fluidProps(Line l, boolean jam) {
        return jam ? new ForgeFlowingFluid.Properties(l.jamType, l.jamSource, l.jamFlowing)
            : new ForgeFlowingFluid.Properties(l.jelloType, l.jelloSource, l.jelloFlowing);
    }

    /** Thick, slow, no bucket and no world block (Fruits Delight's jam fluids are virtual too). */
    private static FluidType fluidType(String texture) {
        ResourceLocation tex = new ResourceLocation(VintageDelight.MODID, "block/" + texture);
        return new FluidType(FluidType.Properties.create().density(1400).viscosity(4000)) {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    @Override public ResourceLocation getStillTexture() { return tex; }
                    @Override public ResourceLocation getFlowingTexture() { return tex; }
                });
            }
        };
    }

    /** Blocks that render cut out (the jars). */
    public static List<Block> cutoutBlocks() {
        List<Block> l = new ArrayList<>();
        NEW_JAR_BLOCKS.forEach(b -> l.add(b.get()));
        FD_JAR_BLOCKS.values().forEach(b -> l.add(b.get()));
        return l;
    }
}
