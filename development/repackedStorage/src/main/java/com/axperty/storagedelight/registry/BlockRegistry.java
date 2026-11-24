package com.axperty.storagedelight.registry;

import com.axperty.storagedelight.block.BookshelfDoorBlock;
import com.axperty.storagedelight.block.CabinetCountertopBlock;
import com.axperty.storagedelight.block.CabinetVariantBlock;
import com.axperty.storagedelight.block.DrawerBlock;
import com.axperty.storagedelight.block.DrawerBooksBlock;
import com.axperty.storagedelight.block.DrawerDoorBlock;
import com.axperty.storagedelight.block.GlassCabinetBlock;
import com.axperty.storagedelight.block.SmallDrawersBlock;
import com.axperty.storagedelight.block.half.HalfCabinetBlocks;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Storage Delight 25.12.09 with the Creative Engineering additions (feature_writeup/storage_delight.md section 5):
 * the Polished Blackstone, Polished Diorite, Stone Bricks and Polished Tuff countertops on the nine woods, and
 * the bamboo set; and a half cabinet after every cabinet (TODO kitchen #41, section 6: "cabinet" in the id becomes
 * "half_cabinet"). Table-driven: every block is {@link #ALL} by registry name, grouped by {@link Kind} for the block
 * entity types (a half cabinet shares its full cabinet's kind and type).
 */
public class BlockRegistry {
    public enum Kind {
        DRAWER(DrawerBlock::new, null),
        DRAWER_DOOR(DrawerDoorBlock::new, null),
        DRAWER_BOOKS(DrawerBooksBlock::new, null),
        GLASS_CABINET(GlassCabinetBlock::new, HalfCabinetBlocks.Glass::new),
        CABINET_VARIANT(CabinetVariantBlock::new, HalfCabinetBlocks.Variant::new),
        BOOKSHELF_DOOR(BookshelfDoorBlock::new, null),
        SMALL_DRAWERS(SmallDrawersBlock::new, null),
        COUNTERTOP(CabinetCountertopBlock::new, HalfCabinetBlocks.Countertop::new);

        final Function<Properties, Block> factory;
        /** The half cabinet of this kind, or null when the kind is not a cabinet. */
        final Function<Properties, Block> halfFactory;

        Kind(Function<Properties, Block> factory, Function<Properties, Block> halfFactory) {
            this.factory = factory;
            this.halfFactory = halfFactory;
        }
    }

    /** Wood names in registration order; the two nether woods burn nothing. */
    public static final String[] WOODS = {"oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "crimson", "warped"};
    private static final String[] ORIGINAL_STONES = {"polished_deepslate", "polished_andesite"};
    private static final String[] ADDED_STONES = {"polished_blackstone", "polished_diorite", "stone_bricks", "polished_tuff"};

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "storagedelight");
    public static final Map<String, RegistryObject<Block>> ALL = new LinkedHashMap<>();
    public static final Map<Kind, List<RegistryObject<Block>>> BY_KIND = new EnumMap<>(Kind.class);
    /** Every half cabinet id, in registration order (each right after its full cabinet). */
    public static final List<String> HALF_CABINETS = new ArrayList<>();

    static {
        for (String wood : WOODS) {
            addWood(wood, ORIGINAL_STONES);
        }
        for (String wood : WOODS) {
            for (String stone : ADDED_STONES) {
                add(Kind.COUNTERTOP, wood + "_cabinet_with_" + stone + "_countertop");
            }
        }
        addWood("bamboo", ORIGINAL_STONES);
        for (String stone : ADDED_STONES) {
            add(Kind.COUNTERTOP, "bamboo_cabinet_with_" + stone + "_countertop");
        }
    }

    private static void addWood(String wood, String[] stones) {
        add(Kind.DRAWER, wood + "_drawer");
        add(Kind.DRAWER_DOOR, wood + "_drawer_with_door");
        add(Kind.DRAWER_BOOKS, wood + "_drawer_with_books");
        add(Kind.SMALL_DRAWERS, "small_" + wood + "_drawers");
        add(Kind.BOOKSHELF_DOOR, wood + "_bookshelf_with_door");
        add(Kind.GLASS_CABINET, "glass_" + wood + "_cabinet");
        add(Kind.CABINET_VARIANT, wood + "_cabinet_with_glass_doors");
        add(Kind.CABINET_VARIANT, wood + "_single_door_cabinet");
        for (String stone : stones) {
            add(Kind.COUNTERTOP, wood + "_cabinet_with_" + stone + "_countertop");
        }
    }

    private static void add(Kind kind, String name) {
        put(kind, name, kind.factory);
        if (kind.halfFactory != null) {
            String half = halfName(name);
            put(kind, half, kind.halfFactory);
            HALF_CABINETS.add(half);
        }
    }

    /** glass_oak_cabinet -> glass_oak_half_cabinet, oak_cabinet_with_glass_doors -> oak_half_cabinet_with_glass_doors. */
    public static String halfName(String cabinet) {
        return cabinet.replaceFirst("cabinet", "half_cabinet");
    }

    private static void put(Kind kind, String name, Function<Properties, Block> factory) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> factory.apply(Properties.copy(Blocks.OAK_PLANKS)));
        ALL.put(name, block);
        BY_KIND.computeIfAbsent(kind, k -> new ArrayList<>()).add(block);
    }

    public static Block[] blocks(Kind kind) {
        return BY_KIND.get(kind).stream().map(RegistryObject::get).toArray(Block[]::new);
    }
}
