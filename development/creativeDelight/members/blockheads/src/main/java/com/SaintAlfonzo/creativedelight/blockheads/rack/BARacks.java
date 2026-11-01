package com.SaintAlfonzo.creativedelight.blockheads.rack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.SaintAlfonzo.creativedelight.blockheads.BlockheadsAdditionsMod;
import com.mojang.logging.LogUtils;
import net.blay09.mods.cookingforblockheads.item.ModItems;
import net.blay09.mods.cookingforblockheads.tile.ModBlockEntities;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

/**
 * The wood spice racks, tool racks and fruit baskets ({@link RackWoods}), under Cooking for Blockheads' namespace, in its creative tab.
 * They use CfB's own spice rack, tool rack and fruit basket block entity types (so CfB's renderers draw what they hold and the cooking
 * table finds them), whose valid-block sets only name CfB's two blocks; {@link #allowOnBlockEntityTypes} adds ours.
 */
public final class BARacks {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BlockheadsAdditionsMod.NAMESPACE);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BlockheadsAdditionsMod.NAMESPACE);

    public static final List<RegistryObject<Block>> SPICE_RACKS = new ArrayList<>();
    public static final List<RegistryObject<Block>> TOOL_RACKS = new ArrayList<>();
    public static final List<RegistryObject<Block>> FRUIT_BASKETS = new ArrayList<>();

    static {
        for (String wood : RackWoods.WOODS) {
            SPICE_RACKS.add(register(wood + "_spice_rack", WoodSpiceRackBlock::new));
            TOOL_RACKS.add(register(wood + "_tool_rack", WoodToolRackBlock::new));
            FRUIT_BASKETS.add(register(wood + "_fruit_basket", WoodFruitBasketBlock::new));
        }
    }

    private BARacks() {
    }

    private static RegistryObject<Block> register(String name, Supplier<Block> block) {
        RegistryObject<Block> b = BLOCKS.register(name, block);
        ITEMS.register(name, () -> new BlockItem(b.get(), new Item.Properties().tab(ModItems.creativeModeTab)));
        return b;
    }

    /** Common setup (enqueued): adds the racks to CfB's spice rack and tool rack block entity types. */
    public static void allowOnBlockEntityTypes() {
        addValidBlocks(ModBlockEntities.spiceRack.get(), SPICE_RACKS);
        addValidBlocks(ModBlockEntities.toolRack.get(), TOOL_RACKS);
        addValidBlocks(ModBlockEntities.fruitBasket.get(), FRUIT_BASKETS);
    }

    private static void addValidBlocks(BlockEntityType<?> type, List<RegistryObject<Block>> blocks) {
        try {
            // BlockEntityType.validBlocks (an ImmutableSet), SRG f_58915_
            Field field = ObfuscationReflectionHelper.findField(BlockEntityType.class, "f_58915_");
            @SuppressWarnings("unchecked")
            Set<Block> valid = new HashSet<>((Set<Block>) field.get(type));
            blocks.forEach(b -> valid.add(b.get()));
            field.set(type, valid);
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("[blockheads_additions] could not add the wood racks to {}: they will not keep their contents",
                    ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(type), e);
        }
    }
}
