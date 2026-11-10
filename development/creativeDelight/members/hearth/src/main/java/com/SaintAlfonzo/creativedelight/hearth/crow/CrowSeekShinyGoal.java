package com.SaintAlfonzo.creativedelight.hearth.crow;

import com.SaintAlfonzo.creativedelight.hearth.nest.NestBlockEntity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Theft by a wild crow (C6, hearth_and_harvest.md 5.8 N24-8): one item, count 1, from a player's hotbar, or from an item a
 * player threw (that case can tame, 1 in 3, and gives the item back). The loot goes to a nest within 16 if one is near,
 * otherwise the crow flies away from the nearest player. Despawn, timeout and drops are in {@link CrowEvents}.
 */
public class CrowSeekShinyGoal extends Goal {
    private static final double SCAN_RADIUS = 10.0D;
    private static final double REACH_SQR = 4.0D;
    private static final double SPEED = 1.2D;
    private static final int SCAN_TICKS = 20;
    private static final int CHASE_TICKS = 200;
    private static final int NEST_RADIUS = 16;
    private static final int NEST_WAIT = 20;
    private static final double ESCAPE_FROM_SQR = 256.0D;

    private final TamableAnimal crow;
    @Nullable
    private Player victim;
    @Nullable
    private ItemEntity thrown;
    @Nullable
    private BlockPos nest;
    private boolean nestSearched;
    private int nestTimer;
    @Nullable
    private Vec3 escape;
    private long nextScan;
    private int chaseTicks;
    private boolean completed;

    public CrowSeekShinyGoal(TamableAnimal crow) {
        this.crow = crow;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean carrying(CrowWariness wariness) {
        return wariness.isCarrying() && !crow.getMainHandItem().isEmpty();
    }

    /** Stealable: in the shiny list, plain (no enchantment, name or other NBT) and not food, which AM would eat in the beak. */
    public static boolean stealable(ItemStack stack) {
        return !stack.isEmpty() && stack.is(CrowTags.SHINY_ITEMS) && !stack.hasTag() && !stack.isEdible();
    }

    @Override
    public boolean canUse() {
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness == null || !CrowLogic.isWild(crow) || !(crow.level instanceof ServerLevel level)) {
            return false;
        }
        long now = level.getGameTime();
        if (now < nextScan) {
            return false;
        }
        nextScan = now + SCAN_TICKS;
        if (carrying(wariness)) {
            return nearestPlayer(ESCAPE_FROM_SQR) != null || nearbyNest() != null;
        }
        if (!CrowConfig.STEAL_SHINY_ITEMS.get() || !crow.getMainHandItem().isEmpty() || crow.getTarget() != null
                || CrowAccess.isFetchingItem(crow) || wariness.isAlarmed(level)) {
            return false;
        }
        thrown = findThrownItem();
        if (thrown != null) {
            return true;
        }
        if (crow.getRandom().nextDouble() >= CrowConfig.STEAL_CHANCE.get()) {
            return false;
        }
        victim = findVictim();
        return victim != null;
    }

    @Override
    public boolean canContinueToUse() {
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness == null || !CrowLogic.isWild(crow) || completed) {
            return false;
        }
        if (carrying(wariness)) {
            return true;
        }
        if (thrown != null) {
            return validThrown(thrown) && chaseTicks < CHASE_TICKS;
        }
        return victim != null && validVictim(victim) && chaseTicks < CHASE_TICKS;
    }

    @Override
    public void start() {
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness != null) {
            wariness.setSnatching(true, victim != null ? victim.getUUID() : null);
        }
        chaseTicks = 0;
        completed = false;
        nest = null;
        nestSearched = false;
        nestTimer = -1;
        escape = null;
    }

    @Override
    public void stop() {
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness != null) {
            wariness.setSnatching(false, null);
        }
        nextScan = crow.level.getGameTime() + (completed ? 100 + crow.getRandom().nextInt(80) : SCAN_TICKS);
        victim = null;
        thrown = null;
        nest = null;
        escape = null;
        completed = false;
        crow.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        CrowWariness wariness = CrowWariness.of(crow);
        if (wariness == null) {
            return;
        }
        if (carrying(wariness)) {
            carry(wariness);
        } else if (thrown != null) {
            chaseThrown(wariness);
        } else if (victim != null) {
            chaseVictim(wariness);
        }
    }

    private void chaseVictim(CrowWariness wariness) {
        chaseTicks++;
        crow.getLookControl().setLookAt(victim, 30.0F, 30.0F);
        if (crow.distanceToSqr(victim) > REACH_SQR) {
            CrowLogic.fly(crow, victim.getX(), victim.getY() + 1.0D, victim.getZ(), SPEED);
            return;
        }
        int slot = pickSlot(victim);
        if (slot < 0) {
            victim = null;
            return;
        }
        Inventory inventory = victim.getInventory();
        ItemStack taken = inventory.removeItem(slot, 1);
        inventory.setChanged();
        victim.displayClientMessage(Component.translatable("hearthandharvest.crow.stole", taken.getHoverName()), true);
        crow.playSound(SoundEvents.ITEM_PICKUP, 0.4F, 1.4F);
        CrowLogic.holdLoot(crow, wariness, taken);
        wariness.setSnatching(true, null);
        victim = null;
    }

    private void chaseThrown(CrowWariness wariness) {
        chaseTicks++;
        crow.getLookControl().setLookAt(thrown.getX(), thrown.getY() + 0.3D, thrown.getZ());
        if (crow.distanceToSqr(thrown) > REACH_SQR) {
            CrowLogic.moveTo(crow, thrown.getX(), thrown.getY(), thrown.getZ(), SPEED);
            return;
        }
        Player thrower = thrower(thrown);
        ItemStack remaining = thrown.getItem().copy();
        ItemStack single = remaining.split(1);
        crow.take(thrown, 1);
        if (remaining.isEmpty()) {
            thrown.discard();
        } else {
            thrown.setItem(remaining);
        }
        thrown = null;
        crow.playSound(SoundEvents.ITEM_PICKUP, 0.4F, 1.4F);
        if (thrower != null && CrowLogic.tryTame(crow, thrower, 3)) {
            thrower.getInventory().placeItemBackInInventory(single);
            completed = true;
        } else {
            CrowLogic.holdLoot(crow, wariness, single);
        }
    }

    private void carry(CrowWariness wariness) {
        if (!nestSearched) {
            nestSearched = true;
            nest = nearbyNest();
        }
        if (nest != null) {
            if (!crow.level.getBlockState(nest).is(CrowTags.NESTS)) {
                nest = null;
                nestSearched = false;
            } else {
                flyToNest(wariness);
            }
            return;
        }
        fleePlayers();
    }

    private void flyToNest(CrowWariness wariness) {
        double x = nest.getX() + 0.5D;
        double y = nest.getY() + 1.1D;
        double z = nest.getZ() + 0.5D;
        if (crow.distanceToSqr(x, y, z) >= 1.5D) {
            nestTimer = -1;
            CrowLogic.fly(crow, x, y, z, SPEED * 1.2D);
            return;
        }
        crow.getNavigation().stop();
        if (nestTimer < 0) {
            nestTimer = NEST_WAIT;
        } else if (--nestTimer <= 0) {
            deposit(wariness);
        }
    }

    private void deposit(CrowWariness wariness) {
        ItemStack stack = crow.getMainHandItem().copy();
        crow.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        wariness.stopCarrying();
        completed = true;
        if (crow.level.getBlockEntity(nest) instanceof NestBlockEntity stash) {
            stack = stash.insert(stack);
            stash.markCrowStash();
        }
        CrowLogic.spawnProtected(crow, stack);
    }

    /** No nest: keep away from the nearest player; with none around the goal lets go and AM's idle flight carries on. */
    private void fleePlayers() {
        Player player = nearestPlayer(ESCAPE_FROM_SQR);
        if (player == null) {
            completed = true;
            return;
        }
        if (escape == null || crow.distanceToSqr(escape) < 4.0D) {
            escape = CrowLogic.fleeTarget(crow, player.getX(), player.getZ());
        }
        if (escape != null) {
            CrowLogic.fly(crow, escape.x, escape.y, escape.z, SPEED * 1.2D);
        }
    }

    @Nullable
    private Player nearestPlayer(double rangeSqr) {
        return crow.level.getNearestPlayer(crow, Math.sqrt(rangeSqr));
    }

    @Nullable
    private BlockPos nearbyNest() {
        if (!CrowConfig.STASH_IN_NESTS.get()) {
            return null;
        }
        return BlockPos.findClosestMatch(crow.blockPosition(), NEST_RADIUS, 3, pos -> crow.level.getBlockState(pos).is(CrowTags.NESTS)).map(BlockPos::immutable).orElse(null);
    }

    private boolean validVictim(Player player) {
        return player.isAlive() && !player.isCreative() && !player.isSpectator() && pickSlot(player) >= 0;
    }

    @Nullable
    private Player findVictim() {
        return crow.level.getEntitiesOfClass(Player.class, crow.getBoundingBox().inflate(SCAN_RADIUS), this::validVictim).stream()
                .min(Comparator.comparingDouble(crow::distanceToSqr)).orElse(null);
    }

    private int pickSlot(Player player) {
        List<Integer> slots = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            if (stealable(inventory.getItem(slot))) {
                slots.add(slot);
            }
        }
        return slots.isEmpty() ? -1 : slots.get(crow.getRandom().nextInt(slots.size()));
    }

    private boolean validThrown(ItemEntity item) {
        return item.isAlive() && item.getThrower() != null && stealable(item.getItem()) && !nearNest(item.blockPosition());
    }

    @Nullable
    private ItemEntity findThrownItem() {
        return crow.level.getEntitiesOfClass(ItemEntity.class, crow.getBoundingBox().inflate(SCAN_RADIUS), this::validThrown).stream()
                .min(Comparator.comparingDouble(crow::distanceToSqr)).orElse(null);
    }

    private boolean nearNest(BlockPos pos) {
        for (BlockPos check : BlockPos.betweenClosed(pos.offset(-1, -2, -1), pos.offset(1, 2, 1))) {
            if (crow.level.getBlockState(check).is(CrowTags.NESTS)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private Player thrower(ItemEntity item) {
        if (item.getThrower() == null || !(crow.level instanceof ServerLevel level)) {
            return null;
        }
        Entity entity = level.getEntity(item.getThrower());
        return entity instanceof Player player ? player : null;
    }
}
