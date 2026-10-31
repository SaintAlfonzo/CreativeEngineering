/*
 * Ported from Hearth and Harvest 1.4.1 (NeoForge 1.21.1) by AlabasterLeking, MIT License:
 * alabaster.hearthandharvest.common.item.WateringCanItem. Ported to Forge 1.19.2 for Creative Engineering:
 * data components became stack NBT, and the client now claims a charged click on a crop so the server
 * fertilises once per click (upstream also sent the use packet, a second 3x3 application). Soil watering is
 * Creative Engineering's addition.
 */
package com.SaintAlfonzo.creativeengineering.wateringcan;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * {@code hearthandharvest:watering_can}. Holds 16 water (refilled from a water source) and 16 fertiliser (one
 * item of {@code #hearthandharvest:bonemeal_substitutes} per click from the off hand, one kind at a time). A click on
 * a bonemealable block spends one of each and applies bone meal to the 3x3 around it; a click on fire or a lit
 * campfire-like block spends one water and puts it out. A click on a plant or soil also soaks the soil in the 3x3
 * (farmland moisture to full); with no fertiliser, or nothing to grow, that alone spends one water.
 */
public class WateringCanItem extends Item {

    public static final int MAX_WATER = 16;
    public static final int MAX_FERTILIZER = 16;
    private static final int BAR_COLOR = 0x0437F2;

    private static final String TAG_WATER = "WaterLevel";
    private static final String TAG_FERTILIZER = "BonemealLevel";
    private static final String TAG_FERTILIZER_ITEM = "FertilizerItem";

    public WateringCanItem(Properties properties) {
        super(properties.stacksTo(1));
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
        return Math.round(13.0F * total / (MAX_WATER + MAX_FERTILIZER));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack can = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(can);

        // Load fertiliser from the off hand, one item per click.
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(WateringCanItems.BONEMEAL_SUBSTITUTES)) {
            Item offItem = offhand.getItem();
            int fertilizer = getFertilizer(can);
            Item loaded = getFertilizerItem(can);
            if (fertilizer < MAX_FERTILIZER) {
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

        if (state.is(Blocks.WATER) && getWater(can) < MAX_WATER) {
            if (!level.isClientSide()) {
                setWater(can, MAX_WATER);
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
        if (player == null || getWater(can) <= 0) return InteractionResult.PASS;
        boolean fertilize = getFertilizer(can) > 0 && level.getBlockState(pos).getBlock() instanceof BonemealableBlock;
        BlockPos soil = soilLevel(level, pos);
        boolean wet = soil != null && hasDrySoil(level, soil);
        if (!fertilize && !wet) return InteractionResult.PASS;
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        ServerLevel server = (ServerLevel) level;
        boolean grew = fertilize && applyBonemeal(server, pos);
        if (wet) wetSoil(server, soil);
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

    private static boolean hasDrySoil(Level level, BlockPos center) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockState state = level.getBlockState(p.set(center.getX() + dx, center.getY(), center.getZ() + dz));
                if (isSoil(state) && state.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE) return true;
            }
        }
        return false;
    }

    /** Moisture to full on every soil block of the 3x3 at the soil's height. */
    private static void wetSoil(ServerLevel level, BlockPos center) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
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

    /** Bone meal on the 3x3 at the clicked block's height; true if any block grew. */
    private static boolean applyBonemeal(ServerLevel level, BlockPos center) {
        boolean any = false;
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                p.set(center.getX() + dx, center.getY(), center.getZ() + dz);
                BlockState state = level.getBlockState(p);
                if (state.getBlock() instanceof BonemealableBlock growable
                        && growable.isValidBonemealTarget(level, p, state, false)
                        && growable.isBonemealSuccess(level, level.random, p, state)) {
                    growable.performBonemeal(level, level.random, p, state);
                    level.levelEvent(2005, p, 0);
                    any = true;
                }
            }
        }
        return any;
    }

    private static void consumeBoth(ItemStack stack) {
        setWater(stack, getWater(stack) - 1);
        setFertilizer(stack, getFertilizer(stack) - 1);
    }

    // ---- stack state (NBT; a key at 0 is removed, and the fertiliser kind goes with the last charge) ----

    public static int getWater(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(TAG_WATER);
    }

    public static int getFertilizer(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(TAG_FERTILIZER);
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
                .append(": " + getWater(stack) + " / " + MAX_WATER)
                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC));
        tooltip.add(fertilizerLabel.copy()
                .append(": " + getFertilizer(stack) + " / " + MAX_FERTILIZER)
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
