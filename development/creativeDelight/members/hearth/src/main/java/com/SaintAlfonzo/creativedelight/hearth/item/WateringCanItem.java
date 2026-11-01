/*
 * Ported from Hearth and Harvest 1.4.1 (NeoForge 1.21.1) by AlabasterLeking, MIT License:
 * alabaster.hearthandharvest.common.item.WateringCanItem. Ported to Forge 1.19.2 for Creative Engineering:
 * data components became stack NBT, and the client claims a charged click on a crop so the server fertilises once per
 * click (upstream also sent the use packet, a second application). Pack additions: soil watering, tiers
 * (WateringCanTier), filling from a water cauldron or a well.
 */
package com.SaintAlfonzo.creativedelight.hearth.item;

import java.util.List;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.AzaleaBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.FungusBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * {@code hearthandharvest:watering_can} (copper) and the higher tiers ({@link WateringCanTier}). Holds water and
 * fertiliser up to the tier's capacity (one item of {@code #hearthandharvest:bonemeal_substitutes} per click from the
 * off hand, one kind at a time). Filled to full from a water source (free), a water cauldron (the tier's levels) or a
 * well / other water tank (one bucket). A click on a bonemealable block spends one of each and applies bone meal to the
 * tier's area around it; a click on fire or a lit campfire-like block spends one water and puts it out. A click on a
 * plant or soil also soaks the soil in the area (farmland moisture to full); with no fertiliser, or nothing to grow,
 * that alone spends one water. Every block bone meal grew gets splash particles (soil the click soaked already has its
 * own). Tree growers ({@link #isTreeGrower}) are left alone: no bone meal, and a click on one does nothing.
 */
public class WateringCanItem extends Item {

    public static final TagKey<Item> BONEMEAL_SUBSTITUTES =
            TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(HearthAndHarvestMod.MODID, "bonemeal_substitutes"));

    /** Extra blocks the can treats as tree growers (empty by default; for growers no class or vanilla tag catches). */
    public static final TagKey<net.minecraft.world.level.block.Block> TREE_GROWERS =
            TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(HearthAndHarvestMod.MODID, "watering_can_tree_growers"));

    private static final int BAR_COLOR = 0x0437F2;

    private static final String TAG_WATER = "WaterLevel";
    private static final String TAG_FERTILIZER = "BonemealLevel";
    private static final String TAG_FERTILIZER_ITEM = "FertilizerItem";

    private final WateringCanTier tier;

    public WateringCanItem(WateringCanTier tier, Properties properties) {
        super(properties.stacksTo(1));
        this.tier = tier;
    }

    public WateringCanTier getTier() {
        return tier;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getWater(stack) > 0 || getFertilizer(stack) > 0;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return BAR_COLOR;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int total = getWater(stack) + getFertilizer(stack);
        return Math.round(13.0F * total / (2 * tier.capacity));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack can = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(can);

        // Load fertiliser from the off hand, one item per click.
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(BONEMEAL_SUBSTITUTES)) {
            Item offItem = offhand.getItem();
            int fertilizer = getFertilizer(can);
            Item loaded = getFertilizerItem(can);
            if (fertilizer < tier.capacity) {
                if (fertilizer == 0 || loaded == offItem) {
                    if (!level.isClientSide()) {
                        offhand.shrink(1);
                        setFertilizer(can, fertilizer + 1);
                        setFertilizerItem(can, offItem);
                        level.playSound(null, player.blockPosition(), SoundEvents.BONE_MEAL_USE, SoundSource.PLAYERS, 0.8F, 1.0F);
                        if (level instanceof ServerLevel server) {
                            server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                    player.getX(), player.getY() + 0.8, player.getZ(), 10, 0.5, 0.4, 0.5, 0.05);
                        }
                    }
                    return InteractionResultHolder.sidedSuccess(can, level.isClientSide());
                }
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.translatable("tooltip.hearthandharvest.watering_can.wrong_fertilizer",
                            loaded.getDefaultInstance().getHoverName()), true);
                }
            }
        }

        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(can);
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (state.is(Blocks.WATER) && getWater(can) < tier.capacity) {
            if (!level.isClientSide()) {
                setWater(can, tier.capacity);
                level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
            return InteractionResultHolder.sidedSuccess(can, level.isClientSide());
        }

        if (isExtinguishable(state) && getWater(can) > 0) {
            if (!level.isClientSide()) {
                if (state.is(Blocks.FIRE)) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                } else {
                    level.setBlock(pos, state.setValue(CampfireBlock.LIT, false), 3);
                }
                level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F);
                setWater(can, getWater(can) - 1);
            }
            return InteractionResultHolder.sidedSuccess(can, level.isClientSide());
        }

        // Fertilising a looked-at block is useOn's job: a block in reach always reaches useOn first.
        return InteractionResultHolder.pass(can);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack can = context.getItemInHand();
        if (player == null) return InteractionResult.PASS;

        // Filling from a water cauldron or a tank (a well) comes first: neither is soil or a plant.
        InteractionResult fill = tryFill(context, can);
        if (fill != InteractionResult.PASS) return fill;

        if (getWater(can) <= 0) return InteractionResult.PASS;
        BlockState clicked = level.getBlockState(pos);
        if (isTreeGrower(clicked)) return InteractionResult.PASS;
        Area area = Area.of(tier, context);
        boolean fertilize = getFertilizer(can) > 0 && clicked.getBlock() instanceof BonemealableBlock;
        BlockPos soil = soilLevel(level, pos);
        boolean wet = soil != null && hasDrySoil(level, soil, area);
        if (!fertilize && !wet) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ServerLevel server = (ServerLevel) level;
        boolean grew = fertilize && applyBonemeal(server, pos, area);
        if (wet) wetSoil(server, soil, area);
        if (grew) {
            consumeBoth(can);
            level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
            server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.25, 0.3, 0.25, 0.05);
        } else if (wet) {
            setWater(can, getWater(can) - 1);
        } else {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    // ---- filling ----

    /** Water cauldron (the tier's levels, all of them needed) or a water tank such as a well (one bucket). */
    private InteractionResult tryFill(UseOnContext context, ItemStack can) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        boolean full = getWater(can) >= tier.capacity;

        if (state.is(Blocks.WATER_CAULDRON)) {
            if (full) return InteractionResult.PASS;
            int levels = state.getValue(LayeredCauldronBlock.LEVEL);
            if (!level.isClientSide()) {
                // Fewer levels than a full fill takes: all of them, for that share of the capacity (rounded down).
                int taken = Math.min(levels, tier.cauldronLevels);
                int left = levels - taken;
                level.setBlockAndUpdate(pos, left <= 0
                        ? Blocks.CAULDRON.defaultBlockState()
                        : state.setValue(LayeredCauldronBlock.LEVEL, left));
                level.gameEvent(context.getPlayer(), GameEvent.FLUID_PICKUP, pos);
                if (taken >= tier.cauldronLevels) {
                    fillFull(level, pos, can);
                } else {
                    setWater(can, Math.min(tier.capacity, getWater(can) + tier.capacity * taken / tier.cauldronLevels));
                    level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (full) return InteractionResult.PASS;
        IFluidHandler tank = waterTank(level, pos, context.getClickedFace());
        if (tank == null) return InteractionResult.PASS;
        FluidStack bucket = new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME);
        if (tank.drain(bucket, IFluidHandler.FluidAction.SIMULATE).getAmount() < FluidType.BUCKET_VOLUME) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            tank.drain(bucket, IFluidHandler.FluidAction.EXECUTE);
            fillFull(level, pos, can);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void fillFull(Level level, BlockPos pos, ItemStack can) {
        setWater(can, tier.capacity);
        level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    /**
     * Fluid handler of the clicked block's entity; for a two-block block (the well's roof half has no entity) the
     * same block directly above or below.
     */
    @Nullable
    private static IFluidHandler waterTank(Level level, BlockPos pos, Direction face) {
        IFluidHandler handler = fluidHandler(level.getBlockEntity(pos), face);
        if (handler != null) return handler;
        BlockState state = level.getBlockState(pos);
        for (BlockPos other : new BlockPos[] {pos.below(), pos.above()}) {
            if (level.getBlockState(other).is(state.getBlock())) {
                handler = fluidHandler(level.getBlockEntity(other), face);
                if (handler != null) return handler;
            }
        }
        return null;
    }

    @Nullable
    private static IFluidHandler fluidHandler(@Nullable BlockEntity be, Direction face) {
        if (be == null) return null;
        return be.getCapability(ForgeCapabilities.FLUID_HANDLER, face).resolve()
                .or(() -> be.getCapability(ForgeCapabilities.FLUID_HANDLER, null).resolve())
                .orElse(null);
    }

    // ---- area ----

    /** Square area at a given height: x and z offsets from the clicked block, from WateringCanTier.span. */
    private record Area(int x0, int x1, int z0, int z1) {
        static Area of(WateringCanTier tier, UseOnContext context) {
            Vec3 hit = context.getClickLocation();
            BlockPos pos = context.getClickedPos();
            int[] x = tier.span(hit.x - pos.getX());
            int[] z = tier.span(hit.z - pos.getZ());
            return new Area(x[0], x[1], z[0], z[1]);
        }
    }

    /** Height of the soil to wet: the clicked block if it is soil, else the block under it; null if neither. */
    @Nullable
    private static BlockPos soilLevel(Level level, BlockPos clicked) {
        if (isSoil(level.getBlockState(clicked))) return clicked;
        BlockPos below = clicked.below();
        return isSoil(level.getBlockState(below)) ? below : null;
    }

    /** Anything with farmland moisture: farmland, Rich Soil farmland, Fertile Soil (7 = soaked). */
    private static boolean isSoil(BlockState state) {
        return state.hasProperty(FarmBlock.MOISTURE);
    }

    private static boolean hasDrySoil(Level level, BlockPos center, Area a) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = a.x0; dx <= a.x1; dx++) {
            for (int dz = a.z0; dz <= a.z1; dz++) {
                BlockState state = level.getBlockState(p.set(center.getX() + dx, center.getY(), center.getZ() + dz));
                if (isSoil(state) && state.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE) return true;
            }
        }
        return false;
    }

    /** Moisture to full on every soil block of the area at the soil's height. */
    private static void wetSoil(ServerLevel level, BlockPos center, Area a) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = a.x0; dx <= a.x1; dx++) {
            for (int dz = a.z0; dz <= a.z1; dz++) {
                p.set(center.getX() + dx, center.getY(), center.getZ() + dz);
                BlockState state = level.getBlockState(p);
                if (isSoil(state) && state.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE) {
                    level.setBlock(p, state.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), 2);
                    level.sendParticles(ParticleTypes.SPLASH, p.getX() + 0.5, p.getY() + 1.0, p.getZ() + 0.5, 4, 0.3, 0.05, 0.3, 0.0);
                }
            }
        }
        level.playSound(null, center, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.5F, 1.4F);
    }

    private static boolean isExtinguishable(BlockState state) {
        return state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT) || state.is(Blocks.FIRE);
    }

    /** Bone meal on the area at the clicked block's height; true if any block grew. */
    private static boolean applyBonemeal(ServerLevel level, BlockPos center, Area a) {
        boolean any = false;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = a.x0; dx <= a.x1; dx++) {
            for (int dz = a.z0; dz <= a.z1; dz++) {
                p.set(center.getX() + dx, center.getY(), center.getZ() + dz);
                BlockState state = level.getBlockState(p);
                if (state.getBlock() instanceof BonemealableBlock growable
                        && !isTreeGrower(state)
                        && growable.isValidBonemealTarget(level, p, state, false)
                        && growable.isBonemealSuccess(level, level.random, p, state)) {
                    growable.performBonemeal(level, level.random, p, state);
                    level.levelEvent(2005, p, 0);
                    // Soil under it that this click soaks gets its splash in wetSoil; everything else gets it here.
                    BlockState below = level.getBlockState(p.below());
                    if (!(isSoil(below) && below.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE)) {
                        level.sendParticles(ParticleTypes.SPLASH, p.getX() + 0.5, p.getY() + 0.6, p.getZ() + 0.5, 4, 0.3, 0.1, 0.3, 0.0);
                    }
                    any = true;
                }
            }
        }
        return any;
    }

    /**
     * Blocks that grow a tree or tree-like plant: {@code #minecraft:saplings} (vanilla and most modded saplings,
     * azaleas, the mangrove propagule), any {@link SaplingBlock} (modded saplings left out of the tag; the propagule
     * is one too), {@link AzaleaBlock}, {@link FungusBlock} (nether fungi, huge fungi), {@link MushroomBlock} (huge
     * mushrooms), and {@link #TREE_GROWERS}.
     */
    public static boolean isTreeGrower(BlockState state) {
        var block = state.getBlock();
        return state.is(BlockTags.SAPLINGS) || state.is(TREE_GROWERS) || block instanceof SaplingBlock
                || block instanceof AzaleaBlock || block instanceof FungusBlock || block instanceof MushroomBlock;
    }

    private void consumeBoth(ItemStack stack) {
        setWater(stack, getWater(stack) - 1);
        setFertilizer(stack, getFertilizer(stack) - 1);
    }

    // ---- stack state (NBT; a key at 0 is removed, and the fertiliser kind goes with the last charge) ----
    // Reads clamp to the tier's capacity (a can from before the tiers may hold 16 in a lower tier).

    public int getWater(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : Math.min(tag.getInt(TAG_WATER), tier.capacity);
    }

    public int getFertilizer(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : Math.min(tag.getInt(TAG_FERTILIZER), tier.capacity);
    }

    public static Item getFertilizerItem(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_FERTILIZER_ITEM)) return Items.AIR;
        ResourceLocation id = ResourceLocation.tryParse(tag.getString(TAG_FERTILIZER_ITEM));
        Item item = id == null ? null : ForgeRegistries.ITEMS.getValue(id);
        return item == null ? Items.AIR : item;
    }

    private static void setWater(ItemStack stack, int value) {
        setInt(stack, TAG_WATER, value);
    }

    private static void setFertilizer(ItemStack stack, int value) {
        setInt(stack, TAG_FERTILIZER, value);
        if (value <= 0) removeKey(stack, TAG_FERTILIZER_ITEM);
    }

    private static void setFertilizerItem(ItemStack stack, Item item) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        if (id != null) stack.getOrCreateTag().putString(TAG_FERTILIZER_ITEM, id.toString());
    }

    private static void setInt(ItemStack stack, String key, int value) {
        if (value > 0) stack.getOrCreateTag().putInt(key, value);
        else removeKey(stack, key);
    }

    private static void removeKey(ItemStack stack, String key) {
        CompoundTag tag = stack.getTag();
        if (tag == null) return;
        tag.remove(key);
        if (tag.isEmpty()) stack.setTag(null);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        Item fertilizerItem = getFertilizerItem(stack);
        Component fertilizerLabel = fertilizerItem == Items.AIR
                ? Component.translatable("tooltip.hearthandharvest.watering_can.bone_meal")
                : Component.translatable("tooltip.hearthandharvest.watering_can.fertilizer", fertilizerItem.getDefaultInstance().getHoverName());
        tooltip.add(Component.translatable("tooltip.hearthandharvest.watering_can.water")
                .append(": " + getWater(stack) + " / " + tier.capacity)
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        tooltip.add(fertilizerLabel.copy()
                .append(": " + getFertilizer(stack) + " / " + tier.capacity)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.hearthandharvest.watering_can.area", tier.size, tier.size)
                .withStyle(ChatFormatting.DARK_GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
