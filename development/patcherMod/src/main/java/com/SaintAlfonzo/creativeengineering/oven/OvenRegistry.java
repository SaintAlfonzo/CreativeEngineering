package com.SaintAlfonzo.creativeengineering.oven;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Everything the oven registers: {@code creativeengineering:oven} (block, item, block entity, menu) and the
 * {@code creativeengineering:baking} recipe type + serializer. Plan and behaviour: feature_writeup/oven.md.
 * Recipes are KubeJS ({@code global.NCH.ceBaking}, kubejs/data/creative_delight/kubejs/creativeengineering/baking.js).
 */
public final class OvenRegistry {

    public static final String NAME = "oven";
    public static final String BAKING = "baking";
    public static final ResourceLocation BAKING_ID = new ResourceLocation(CreativeEngineering.MODID, BAKING);

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CreativeEngineering.MODID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CreativeEngineering.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CreativeEngineering.MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, CreativeEngineering.MODID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CreativeEngineering.MODID);
    private static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, CreativeEngineering.MODID);

    // FD stove stats (Properties.copy(Blocks.BRICKS)), light 13 while heated.
    public static final RegistryObject<Block> OVEN = BLOCKS.register(NAME, () -> new OvenBlock(
            BlockBehaviour.Properties.copy(Blocks.BRICKS)
                    .lightLevel(state -> state.getValue(OvenBlock.LIT) ? 13 : 0)));

    public static final RegistryObject<Item> OVEN_ITEM = ITEMS.register(NAME, () -> new BlockItem(OVEN.get(),
            new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<BlockEntityType<OvenBlockEntity>> OVEN_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register(NAME, () -> BlockEntityType.Builder.of(OvenBlockEntity::new, OVEN.get()).build(null));

    public static final RegistryObject<MenuType<OvenMenu>> OVEN_MENU =
            MENU_TYPES.register(NAME, () -> IForgeMenuType.create(OvenMenu::new));

    public static final RegistryObject<RecipeSerializer<BakingRecipe>> BAKING_SERIALIZER =
            SERIALIZERS.register(BAKING, BakingRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<BakingRecipe>> BAKING_TYPE =
            TYPES.register(BAKING, () -> RecipeType.simple(BAKING_ID));

    private OvenRegistry() {}

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        SERIALIZERS.register(modEventBus);
        TYPES.register(modEventBus);
    }
}
