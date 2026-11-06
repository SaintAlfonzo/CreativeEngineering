package com.SaintAlfonzo.creativedelight.barbeques.block;

import com.SaintAlfonzo.creativedelight.barbeques.BBQLang;
import com.SaintAlfonzo.creativedelight.barbeques.compat.PackSeasonings;
import com.SaintAlfonzo.creativedelight.barbeques.recipe.GrillingRecipe;
import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQBlocks;
import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQItems;
import com.SaintAlfonzo.creativedelight.barbeques.registry.BBQRecipes;
import com.SaintAlfonzo.creativecommon.campfire.CampfireRainInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import vectorwing.farmersdelight.common.block.entity.HeatableBlockEntity;
import vectorwing.farmersdelight.common.registry.ModParticleTypes;
import vectorwing.farmersdelight.common.registry.ModSounds;

import javax.annotation.Nullable;

/**
 * The Grill's two slots (1.3.0 GrillBlockEntity). Per slot, progress counts up while heated and down by 2 per tick while
 * cold (B5, kept). Flip from half time; a flipped skewer turns into its grilled form at full time, keeping its NBT (so
 * its seasonings carry over) and gaining the pack seasoning {@code grilled} (food/seasoning #13); at twice the time it becomes a Burnt Skewer (seasoning lost, B5). Unflipped skewers only burn.
 * Heat: a Farmer's Delight heat source below, or the grill's own lit campfire.
 *
 * Port changes: B1 (a burnt entry no longer re-syncs every tick); the campfire goes out in rain by the pack rule
 * (barbeques_delight.md 3.4.1), read from Common's CampfireRainInfo and the #creativecommon:campfire/rain_douses
 * tag, which the grill is in.
 */
public class GrillBlockEntity extends BlockEntity implements HeatableBlockEntity, BlockSlot {
    public final ItemEntry[] entries = {new ItemEntry(), new ItemEntry()};
    private int rainTicks;

    public GrillBlockEntity(BlockPos pos, BlockState state) {
        super(BBQBlocks.GRILL_BE.get(), pos, state);
    }

    @Override
    public int size() {
        return entries.length;
    }

    public ItemStack getStack(int i) {
        return i >= 0 && i < size() ? entries[i].stack : ItemStack.EMPTY;
    }

    public boolean addItem(int i, ItemStack stack) {
        return i >= 0 && i < size() && entries[i].addItem(this, stack);
    }

    @Override
    public AABB getBox() {
        return GrillBlock.OUTER.bounds().move(getBlockPos()).move(0.0, 0.0625, 0.0).deflate(0.01F);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GrillBlockEntity be) {
        boolean heat = be.isHeated();
        for (ItemEntry e : be.entries) {
            e.tick(be, heat);
        }
        if (level.isClientSide) {
            if (heat) {
                be.addParticles();
            }
        } else {
            be.rainTick(level, pos, state);
        }
    }

    /** Same rule as Creative Compat's CampfireRain for the plain campfire. */
    private void rainTick(Level level, BlockPos pos, BlockState state) {
        if (!state.getValue(GrillBlock.HAS_CAMPFIRE) || !state.getValue(GrillBlock.LIT)
                || !CampfireRainInfo.rainDouses() || !state.is(CampfireRainInfo.RAIN_DOUSES)
                || level.getBlockState(pos.below()).is(CampfireRainInfo.RAIN_PROOF_FUEL)
                || !level.isRainingAt(pos.above())) {
            rainTicks = 0;
            return;
        }
        if (++rainTicks < CampfireRainInfo.rainOutTicks()) {
            return;
        }
        rainTicks = 0;
        GrillBlock.extinguish(level, pos, state);
    }

    public boolean canFlip(int i) {
        return i >= 0 && i < size() && entries[i].canFlip();
    }

    public boolean flip(int i) {
        return i >= 0 && i < size() && entries[i].flip(this);
    }

    public boolean isFlipped(int i) {
        return i >= 0 && i < size() && entries[i].flipped;
    }

    public boolean isHeated() {
        BlockState state = getBlockState();
        if (state.getValue(GrillBlock.HAS_CAMPFIRE) && state.getValue(GrillBlock.LIT)) {
            return true;
        }
        return level != null && isHeated(level, getBlockPos());
    }

    public boolean isBarbecuing() {
        if (level == null || !isHeated()) {
            return false;
        }
        for (ItemEntry e : entries) {
            if (!e.stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void addParticles() {
        if (level == null) {
            return;
        }
        BlockPos pos = getBlockPos();
        RandomSource r = level.random;
        if (r.nextFloat() < 0.2F && isBarbecuing()) {
            double x = pos.getX() + 0.5 + (r.nextDouble() * 0.4 - 0.2);
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5 + (r.nextDouble() * 0.4 - 0.2);
            double motionY = r.nextBoolean() ? 0.015 : 0.005;
            level.addParticle(ModParticleTypes.STEAM.get(), x, y, z, 0.0, motionY, 0.0);
        }
        for (int i = 0; i < entries.length; i++) {
            if (entries[i].stack.isEmpty()) {
                continue;
            }
            float v1 = getOffset(i);
            Direction dir = getBlockState().getValue(HorizontalDirectionalBlock.FACING);
            Vec2 offset = dir.get2DDataValue() % 2 == 0 ? new Vec2(v1, 0.0F) : new Vec2(0.0F, v1);
            double x = pos.getX() + 0.5 - dir.getStepX() * offset.x + dir.getClockWise().getStepX() * offset.x;
            double y = pos.getY() + 1.0;
            double z = pos.getZ() + 0.5 - dir.getStepZ() * offset.y + dir.getClockWise().getStepZ() * offset.y;
            for (int j = 0; j < (entries[i].smoking() ? 8 : 1); j++) {
                if (r.nextFloat() < 0.2F) {
                    double dx = (r.nextDouble() + r.nextDouble() - 1.0) * 0.1F;
                    double dz = (r.nextDouble() + r.nextDouble() - 1.0) * 0.1F;
                    level.addParticle(ParticleTypes.SMOKE, x + dx, y, z + dz, 0.0, 5.0E-4, 0.0);
                }
            }
        }
    }

    public void inventoryChanged() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < entries.length; i++) {
            entries[i].load(i < list.size() ? list.getCompound(i) : new CompoundTag());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ListTag list = new ListTag();
        for (ItemEntry e : entries) {
            list.add(e.save());
        }
        tag.put("Entries", list);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static class ItemEntry {
        public int time;
        public int duration;
        public boolean flipped;
        public boolean burnt;
        public ItemStack stack = ItemStack.EMPTY;

        void tick(GrillBlockEntity be, boolean heated) {
            Level level = be.level;
            if (level == null || stack.isEmpty()) {
                return;
            }
            if (!heated) {
                if (time > 0) {
                    time = Math.max(0, time - 2);
                }
                return;
            }
            time++;
            if (time < duration) {
                return;
            }
            if (time >= duration * 2) {
                // B1: burn once; 1.3.0 re-set the stack and re-synced the grill every tick from here on
                if (!burnt) {
                    burnt = true;
                    stack = new ItemStack(BBQItems.BURNT_SKEWER.get());
                    be.inventoryChanged();
                }
                return;
            }
            if (flipped && time == duration && !level.isClientSide) {
                GrillingRecipe recipe = find(level, stack);
                if (recipe != null) {
                    CompoundTag tag = stack.getTag();
                    stack = recipe.output.copy();
                    if (tag != null) {
                        stack.getOrCreateTag().merge(tag.copy());
                    }
                    // food/seasoning #13: grilling on the Grill (only here) adds the grilled seasoning
                    PackSeasonings.addGrilled(stack);
                    be.inventoryChanged();
                }
            }
        }

        @Nullable
        static GrillingRecipe find(Level level, ItemStack stack) {
            for (GrillingRecipe r : level.getRecipeManager().getAllRecipesFor(BBQRecipes.GRILLING.get())) {
                if (r.matches(stack)) {
                    return r;
                }
            }
            return null;
        }

        public boolean canFlip() {
            return !stack.isEmpty() && !flipped && !burnt && time >= duration / 2;
        }

        public boolean smoking() {
            return burnt || canFlip() || flipped && time >= duration;
        }

        boolean flip(GrillBlockEntity be) {
            if (be.level == null || !canFlip()) {
                return false;
            }
            flipped = true;
            time = duration / 2;
            if (!be.level.isClientSide) {
                be.inventoryChanged();
            }
            be.level.playSound(null, be.getBlockPos(), ModSounds.BLOCK_SKILLET_ADD_FOOD.get(), SoundSource.BLOCKS, 0.8F, 1.0F);
            return true;
        }

        boolean addItem(GrillBlockEntity be, ItemStack add) {
            if (be.level == null) {
                return false;
            }
            GrillingRecipe recipe = find(be.level, add);
            if (recipe == null) {
                return false;
            }
            if (!be.level.isClientSide) {
                duration = recipe.barbecuingTime;
                time = 0;
                flipped = burnt = false;
                stack = add.copy();
                stack.setCount(1);
                add.shrink(1);
                be.inventoryChanged();
            }
            be.level.playSound(null, be.getBlockPos(), SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 0.7F, 1.0F);
            return true;
        }

        /** The grill line (1.3.0's Jade text, now WTHIT): seconds to the flip, then to done; Ready to Flip; Cooked; Burnt. */
        public static Component status(int time, int duration, boolean flipped, boolean burnt) {
            if (burnt) {
                return BBQLang.BURNT.get();
            }
            if (!flipped) {
                return time < duration / 2 ? BBQLang.COOK.get((duration / 2 - time + 19) / 20) : BBQLang.FLIP.get();
            }
            return time < duration ? BBQLang.COOK.get((duration - time + 19) / 20) : BBQLang.COOKED.get();
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putInt("Time", time);
            tag.putInt("Duration", duration);
            tag.putBoolean("Flipped", flipped);
            tag.putBoolean("Burnt", burnt);
            tag.put("Item", stack.save(new CompoundTag()));
            return tag;
        }

        void load(CompoundTag tag) {
            time = tag.getInt("Time");
            duration = tag.getInt("Duration");
            flipped = tag.getBoolean("Flipped");
            burnt = tag.getBoolean("Burnt");
            stack = ItemStack.of(tag.getCompound("Item"));
        }
    }
}
