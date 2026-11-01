package com.SaintAlfonzo.creativeengineering.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * Beautify's botanist workbench as a flower pot (feature_writeup/flower_pots.md, TODO framed-and-decor #5). It takes
 * exactly what the vanilla pot takes: the plant list is the vanilla pot's live {@link FlowerPotBlock#getFullPotsView()}
 * map, read on every click, and the plant is drawn from that entry's {@code potted_*} model
 * ({@link BotanistPotClient}). The block gets its block entity from mixin/BotanistWorkbenchPotMixin; workbenches
 * placed by a structure template start with a random vanilla flower (mixin/StructureTemplateBotanistPotMixin).
 */
public final class BotanistPot {

    public static final ResourceLocation WORKBENCH_ID = new ResourceLocation("beautify", "botanist_workbench");
    static final String PLANT_TAG = "Plant";

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CreativeEngineering.MODID);

    public static final RegistryObject<BlockEntityType<BotanistPotBlockEntity>> TYPE =
            BLOCK_ENTITY_TYPES.register("botanist_pot", () -> BlockEntityType.Builder.of(BotanistPotBlockEntity::new,
                    ForgeRegistries.BLOCKS.getValue(WORKBENCH_ID)).build(null));

    @Nullable
    private static Block workbench;

    /** Not in the vanilla pot map; the workbench takes it anyway (flower_pots.md 3.2). */
    static final ResourceLocation GLOW_LICHEN = new ResourceLocation("minecraft", "glow_lichen");

    private BotanistPot() {}

    public static void register(IEventBus modEventBus) {
        if (ModList.get().isLoaded("beautify")) BLOCK_ENTITY_TYPES.register(modEventBus);
    }

    /** The workbench block, or null when Beautify is absent. */
    @Nullable
    static Block workbench() {
        if (workbench == null && ModList.get().isLoaded("beautify")) {
            Block block = ForgeRegistries.BLOCKS.getValue(WORKBENCH_ID);
            if (block != null && block != Blocks.AIR) workbench = block;
        }
        return workbench;
    }

    private static Map<ResourceLocation, Supplier<? extends Block>> fullPots() {
        return ((FlowerPotBlock) Blocks.FLOWER_POT).getFullPotsView();
    }

    /** The {@code potted_*} block the vanilla pot uses for this plant, or null if the plant has none (any more). */
    @Nullable
    public static Block pottedFor(@Nullable ResourceLocation plant) {
        if (plant == null) return null;
        // Glow lichen has no potted block: it stands in for itself, drawn from PlantBoxModels.GLOW_LICHEN_POT
        if (GLOW_LICHEN.equals(plant)) return Blocks.GLOW_LICHEN;
        Supplier<? extends Block> potted = fullPots().get(plant);
        Block block = potted == null ? null : potted.get();
        return block == null || block == Blocks.AIR ? null : block;
    }

    /** Vanilla FlowerPotBlock.use, except that a click that changes nothing passes, so blocks still place against it. */
    public static InteractionResult use(Level level, BlockPos pos, Player player, InteractionHand hand) {
        if (!(level.getBlockEntity(pos) instanceof BotanistPotBlockEntity pot)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        ResourceLocation heldPlant = held.getItem() instanceof BlockItem item
                ? ForgeRegistries.BLOCKS.getKey(item.getBlock()) : null;
        boolean pottable = pottedFor(heldPlant) != null;
        ResourceLocation current = pot.plant();
        if (current == null) {
            if (!pottable) return InteractionResult.PASS;
            if (!level.isClientSide) {
                pot.setPlant(heldPlant);
                player.awardStat(Stats.POT_FLOWER);
                if (!player.getAbilities().instabuild) held.shrink(1);
            }
        } else {
            if (pottable) return InteractionResult.PASS;
            if (!level.isClientSide) {
                ItemStack out = plantStack(current);
                if (held.isEmpty()) player.setItemInHand(hand, out);
                else if (!player.addItem(out)) player.drop(out, false);
                pot.setPlant(null);
            }
        }
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Drops the plant when the workbench goes (mined, exploded, replaced). */
    public static void dropPlant(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BotanistPotBlockEntity pot && pot.plant() != null) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), plantStack(pot.plant()));
        }
    }

    private static ItemStack plantStack(ResourceLocation plant) {
        Block block = ForgeRegistries.BLOCKS.getValue(plant);
        return block == null ? ItemStack.EMPTY : new ItemStack(block);
    }

    /**
     * Gives every workbench in a processed template a random vanilla flower (minecraft namespace, in
     * {@code #minecraft:flowers}, has a vanilla pot entry, not the wither rose) as block-entity NBT, which template
     * placement loads into the new block entity. Workbenches that already carry NBT are left alone.
     * Returns null when nothing changed.
     */
    @Nullable
    public static List<StructureBlockInfo> seed(List<StructureBlockInfo> infos, StructurePlaceSettings settings) {
        Block bench = workbench();
        if (bench == null) return null;
        List<StructureBlockInfo> out = null;
        List<ResourceLocation> flowers = null;
        for (int i = 0; i < infos.size(); i++) {
            StructureBlockInfo info = infos.get(i);
            if (info.nbt != null || !info.state.is(bench)) continue;
            if (flowers == null) {
                flowers = vanillaFlowers();
                if (flowers.isEmpty()) return null;
            }
            RandomSource random = settings.getRandom(info.pos);
            CompoundTag nbt = new CompoundTag();
            nbt.putString(PLANT_TAG, flowers.get(random.nextInt(flowers.size())).toString());
            if (out == null) out = new ArrayList<>(infos);
            out.set(i, new StructureBlockInfo(info.pos, info.state, nbt));
        }
        return out;
    }

    private static List<ResourceLocation> vanillaFlowers() {
        List<ResourceLocation> flowers = new ArrayList<>();
        for (ResourceLocation plant : fullPots().keySet()) {
            if (!"minecraft".equals(plant.getNamespace()) || pottedFor(plant) == null) continue;
            Block block = ForgeRegistries.BLOCKS.getValue(plant);
            if (block == null || block == Blocks.WITHER_ROSE || !block.defaultBlockState().is(BlockTags.FLOWERS)) continue;
            flowers.add(plant);
        }
        flowers.sort(null);
        return flowers;
    }
}
