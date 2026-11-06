// Pack addition (CreativeEngineering2, TODO dishes/jams #1, #8). Own work, GPLv3. Uses Fruits Delight's cauldron
// classes as a library (extends FDCauldronBlock, adds to its JEI cauldron list); loaded only when Fruits Delight is.
package net.ribs.vintagedelight.jam;

import dev.xkmc.fruitsdelight.content.cauldrons.CauldronRecipe;
import dev.xkmc.fruitsdelight.content.cauldrons.FDCauldronBlock;
import dev.xkmc.fruitsdelight.content.cauldrons.FDCauldronInteraction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.ribs.vintagedelight.item.ModItems;

/**
 * Cauldron jam for the pack's jams, Fruits Delight's chain: a water cauldron with a lemon slice is lemonade; heated,
 * each fruit makes it a fruit cauldron (12 levels, one per fruit, 4 per jam jar put back in); full and heated, sugar
 * makes it a jam cauldron (3 jars); heated, a slime ball makes that jello. An empty mason jar takes one jar of jam, a
 * bowl one bowl of jello. A jar of jam in a full water cauldron gives a fruit cauldron at 4. Fruits Delight's own jam
 * cauldrons are emptied with an empty mason jar too (was a glass bottle; jams are mason jars, dishes/jams #1).
 */
public final class JamCauldrons {
    private JamCauldrons() {}

    public static final IntegerProperty FRUIT_LEVEL = IntegerProperty.create("level", 1, 12);
    public static final IntegerProperty JAM_LEVEL = IntegerProperty.create("level", 1, 3);
    public static final int MAX = 12;

    /** The fruit for each jam (the pot recipe's fruit, by id: the cauldrons are keyed by item). Missing mods skip. */
    static final Map<String, List<String>> FRUITS = Map.of(
        "ancient_fruit", List.of("quark:ancient_fruit"),
        "banana", List.of("neapolitan:banana", "alexsmobs:banana"),
        "cherry", List.of("vinery:cherry"),
        "green_grape", List.of("vinery:white_grape", "vinery:jungle_grapes_white", "vinery:savanna_grapes_white", "vinery:taiga_grapes_white"),
        "red_grape", List.of("vinery:red_grape", "vinery:jungle_grapes_red", "vinery:savanna_grapes_red", "vinery:taiga_grapes_red"),
        "mulberry", List.of("upgrade_aquatic:mulberry"),
        "pepper", List.of("vintagedelight:pickled_pepper"),
        "raspberry", List.of("hearthandharvest:raspberry"),
        "salmonberry", List.of("delightful:salmonberries"),
        "strawberry", List.of("neapolitan:strawberries"));

    public static class FruitCauldron extends FDCauldronBlock {
        public FruitCauldron(Properties p) {
            super(p);
            this.registerDefaultState(this.defaultBlockState().setValue(FRUIT_LEVEL, MAX));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
            b.add(FRUIT_LEVEL);
        }
    }

    public static class LevelCauldron extends FDCauldronBlock {
        public LevelCauldron(Properties p) {
            super(p);
            this.registerDefaultState(this.defaultBlockState().setValue(JAM_LEVEL, 3));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
            b.add(JAM_LEVEL);
        }
    }

    static Block fruitCauldron() {
        return new FruitCauldron(Properties.copy(Blocks.WATER_CAULDRON));
    }

    static Block levelCauldron() {
        return new LevelCauldron(Properties.copy(Blocks.WATER_CAULDRON));
    }

    static void init(IEventBus bus) {
        bus.addListener((FMLLoadCompleteEvent e) -> e.enqueueWork(JamCauldrons::setup));
    }

    private static Item item(String id) {
        Item i = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
        return i == null || i == Items.AIR ? null : i;
    }

    private static void setup() {
        Item jar = ModItems.EMPTY_MASON_JAR.get();
        Block lemonBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("fruitsdelight", "lemonade_cauldron"));
        Item water = item("fruitsdelight:dummy_cauldron");
        if (!(lemonBlock instanceof FDCauldronBlock lemon) || water == null) return;

        for (PackJams.Line l : PackJams.LINES.values()) {
            FDCauldronBlock fruit = (FDCauldronBlock) l.fruitCauldron.get();
            FDCauldronBlock jamC = (FDCauldronBlock) l.jamCauldron.get();
            FDCauldronBlock jelloC = (FDCauldronBlock) l.jelloCauldron.get();
            Item jamJar = l.jarItem.get();
            Item jello = l.jello.get();
            if (jamJar == null || jamJar == Items.AIR) continue;

            CauldronRecipe.create(CauldronInteraction.WATER, water, jamJar, 1, FDCauldronInteraction.of(
                s -> s.getValue(LayeredCauldronBlock.LEVEL) == 3 ? fruit.defaultBlockState().setValue(FRUIT_LEVEL, 4) : null),
                fruit.asItem());
            for (String id : FRUITS.getOrDefault(l.jam.name(), List.of())) {
                Item f = item(id);
                if (f == null) continue;
                CauldronRecipe.create(lemon, f, MAX, FDCauldronInteraction.withHeat(
                    s -> fruit.defaultBlockState().setValue(FRUIT_LEVEL, 1)), fruit.asItem());
                fruit.getInteractions().put(f, FDCauldronInteraction.withHeat(
                    s -> s.getValue(FRUIT_LEVEL) == MAX ? null : s.setValue(FRUIT_LEVEL, s.getValue(FRUIT_LEVEL) + 1)));
            }
            fruit.getInteractions().put(jamJar, FDCauldronInteraction.of(
                s -> s.getValue(FRUIT_LEVEL) == MAX ? null : s.setValue(FRUIT_LEVEL, Math.min(MAX, s.getValue(FRUIT_LEVEL) + 4))));
            CauldronRecipe.create(fruit, Items.SUGAR, 1, FDCauldronInteraction.withHeat(
                s -> s.getValue(FRUIT_LEVEL) == MAX ? jamC.defaultBlockState() : null), jamC.asItem());
            CauldronRecipe.create(jamC, Items.SLIME_BALL, 1, FDCauldronInteraction.withHeat(
                s -> jelloC.defaultBlockState().setValue(JAM_LEVEL, s.getValue(JAM_LEVEL))), jelloC.asItem());
            CauldronRecipe.create(jamC.getInteractions(), jamC.asItem(), jar, 3, FDCauldronInteraction.of(
                s -> s.getValue(JAM_LEVEL) == 1 ? Blocks.CAULDRON.defaultBlockState() : s.setValue(JAM_LEVEL, s.getValue(JAM_LEVEL) - 1),
                new ItemStack(jamJar), SoundEvents.BOTTLE_FILL), Items.CAULDRON);
            CauldronRecipe.create(jelloC.getInteractions(), jelloC.asItem(), Items.BOWL, 3, FDCauldronInteraction.of(
                s -> s.getValue(JAM_LEVEL) == 1 ? Blocks.CAULDRON.defaultBlockState() : s.setValue(JAM_LEVEL, s.getValue(JAM_LEVEL) - 1),
                new ItemStack(jello), SoundEvents.BOTTLE_FILL), Items.CAULDRON);
        }

        // Fruits Delight's jam cauldrons: the glass bottle that took a jam is the empty mason jar now (map and JEI list)
        List<Item> fdJam = new ArrayList<>();
        for (Block b : ForgeRegistries.BLOCKS.getValues()) {
            ResourceLocation id = ForgeRegistries.BLOCKS.getKey(b);
            if (id == null || !id.getNamespace().equals("fruitsdelight") || !id.getPath().endsWith("_jelly_cauldron")) continue;
            if (!(b instanceof FDCauldronBlock c)) continue;
            CauldronInteraction take = c.getInteractions().remove(Items.GLASS_BOTTLE);
            if (take != null) {
                c.getInteractions().put(jar, take);
                fdJam.add(c.asItem());
            }
        }
        CauldronRecipe.LIST.replaceAll(r -> r.item() == Items.GLASS_BOTTLE && fdJam.contains(r.block())
            ? new CauldronRecipe(r.block(), jar, r.count(), r.outBlock(), r.outItem(), r.requiresHeat()) : r);
    }
}
