package com.SaintAlfonzo.creative_labels;

import com.SaintAlfonzo.creative_labels.block.FacePaperBlock;
import com.SaintAlfonzo.creative_labels.block.FacePaperBlockEntity;
import com.SaintAlfonzo.creative_labels.crafting.NoteCopyRecipe;
import com.SaintAlfonzo.creative_labels.crafting.NoteDyeRecipe;
import com.SaintAlfonzo.creative_labels.crafting.NoteEraseRecipe;
import com.SaintAlfonzo.creative_labels.crafting.NoteSpecialRecipe;
import com.SaintAlfonzo.creative_labels.item.EraserItem;
import com.SaintAlfonzo.creative_labels.item.LabelItem;
import com.SaintAlfonzo.creative_labels.item.StickyNoteItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Registry ids (BRIEF 1): block {@code face_paper} (no item), block entity {@code face_paper}, items
 * {@code sticky_note}, {@code label}, {@code eraser}, recipe serializers {@code note_dye}, {@code note_special},
 * {@code note_copy}, {@code note_erase} (the recipe JSONs are the pack's, through an NCH helper; none ship here).
 */
public final class CLRegistry {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CreativeLabels.ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, CreativeLabels.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CreativeLabels.ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, CreativeLabels.ID);

    public static final RegistryObject<FacePaperBlock> FACE_PAPER =
            BLOCKS.register("face_paper", () -> new FacePaperBlock(FacePaperBlock.properties()));

    public static final RegistryObject<BlockEntityType<FacePaperBlockEntity>> FACE_PAPER_BE =
            BLOCK_ENTITIES.register("face_paper",
                    () -> BlockEntityType.Builder.of(FacePaperBlockEntity::new, FACE_PAPER.get()).build(null));

    public static final RegistryObject<StickyNoteItem> STICKY_NOTE =
            ITEMS.register("sticky_note", () -> new StickyNoteItem(new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
    public static final RegistryObject<LabelItem> LABEL =
            ITEMS.register("label", () -> new LabelItem(new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));
    public static final RegistryObject<EraserItem> ERASER =
            ITEMS.register("eraser", () -> new EraserItem(new Item.Properties().tab(CreativeModeTab.TAB_DECORATIONS)));

    public static final RegistryObject<SimpleRecipeSerializer<NoteDyeRecipe>> NOTE_DYE =
            RECIPE_SERIALIZERS.register("note_dye", () -> new SimpleRecipeSerializer<>(NoteDyeRecipe::new));
    public static final RegistryObject<SimpleRecipeSerializer<NoteSpecialRecipe>> NOTE_SPECIAL =
            RECIPE_SERIALIZERS.register("note_special", () -> new SimpleRecipeSerializer<>(NoteSpecialRecipe::new));
    public static final RegistryObject<SimpleRecipeSerializer<NoteCopyRecipe>> NOTE_COPY =
            RECIPE_SERIALIZERS.register("note_copy", () -> new SimpleRecipeSerializer<>(NoteCopyRecipe::new));
    public static final RegistryObject<SimpleRecipeSerializer<NoteEraseRecipe>> NOTE_ERASE =
            RECIPE_SERIALIZERS.register("note_erase", () -> new SimpleRecipeSerializer<>(NoteEraseRecipe::new));

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        ITEMS.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
    }

    private CLRegistry() {}
}
