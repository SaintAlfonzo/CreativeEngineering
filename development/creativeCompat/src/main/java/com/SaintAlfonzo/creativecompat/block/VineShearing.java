package com.SaintAlfonzo.creativecompat.block;

import com.SaintAlfonzo.creativecompat.CompatConfig;

import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Shears stop any vine growing, extending the sheared grass ({@code fertile=false}, feature_writeup/vine_shearing.md)
 * and Quark's Shear Vines (vanilla vine -> {@code quark:cut_vine}) to every other vine in the pack.
 *
 * <ul>
 *   <li>Modded {@link VineBlock}s (Floral Enchantment's flower vines, Botania's solid vine, any later one) get a
 *       {@link #FERTILE} state from {@code mixin/VineBlockShearMixin}; shears clear it, and a vine with
 *       {@code fertile=false} skips its random tick, so it neither spreads nor grows down. As with Quark, the same
 *       vine hanging below the cut is removed ({@code [vine_shearing] cutBelow}). {@code block.VineShearingClient}
 *       draws it with the bottom trimmed, like Quark's cut vine.</li>
 *   <li>Tips of weeping, twisting and cave vines and kelp (any {@link GrowingPlantHeadBlock}): shears set the tip to
 *       its maximum age, which never random-ticks. This is 1.20's shears behaviour; no new state. A cave vine tip with
 *       glow berries is left to its own berry picking, as 1.20 does.</li>
 * </ul>
 * Left alone: vanilla vine (Quark), Quark's own vines (never grow), Vinery's jungle grape vine (shears already toggle
 * its {@code sterilized} state), Immersive Weathering's ivy (shears already set it to its last age).
 */
public final class VineShearing {

    /** Same name and sense as the sheared grass: true = grows (the first value, so {@code any()} keeps it on). */
    public static final BooleanProperty FERTILE = BooleanProperty.create("fertile");

    /** Vine classes (and their subclasses) that keep their own handling and get no {@link #FERTILE} state. */
    private static final Set<String> OWN_HANDLING = Set.of(
            "vazkii.quark.base.block.QuarkVineBlock",
            "net.satisfy.vinery.core.block.GrapeVineBlock");

    private VineShearing() {}

    /** Called from the mixin while a block builds its state definition. Vanilla's own vine is Quark's. */
    public static boolean addsFertile(Class<?> type) {
        if (type == VineBlock.class) return false;
        for (Class<?> c = type; c != null && c != VineBlock.class; c = c.getSuperclass()) {
            if (OWN_HANDLING.contains(c.getName())) return false;
        }
        return true;
    }

    public static boolean isSheared(BlockState state) {
        return state.hasProperty(FERTILE) && !state.getValue(FERTILE);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (!stack.canPerformAction(ToolActions.SHEARS_CARVE)) return;
        if (!CompatConfig.VINE_SHEARING.enabled.get()) return;
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        Player player = event.getEntity();

        if (block instanceof VineBlock && state.hasProperty(FERTILE)) {
            if (!state.getValue(FERTILE)) return;
            // Quark's Shear Vines sound
            level.playSound(player, pos, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, 0.5F, 1.0F);
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(FERTILE, false), 11);
                if (CompatConfig.VINE_SHEARING.cutBelow.get()) {
                    BlockPos below = pos.below();
                    while (level.getBlockState(below).is(block)) {
                        level.removeBlock(below, false);
                        below = below.below();
                    }
                }
                hurt(player, event, stack);
                level.gameEvent(player, GameEvent.SHEAR, pos);
            }
            succeed(event, level);
        } else if (block instanceof GrowingPlantHeadBlock head && !head.isMaxAge(state)) {
            if (state.hasProperty(CaveVines.BERRIES) && CaveVines.hasGlowBerries(state)) return;
            // 1.20 ShearsItem.useOn on a growing plant tip
            level.playSound(player, pos, SoundEvents.GROWING_PLANT_CROP, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!level.isClientSide) {
                BlockState capped = head.getMaxAgeState(state);
                level.setBlockAndUpdate(pos, capped);
                hurt(player, event, stack);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, capped));
            }
            succeed(event, level);
        }
    }

    private static void hurt(Player player, PlayerInteractEvent.RightClickBlock event, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));
        }
    }

    private static void succeed(PlayerInteractEvent.RightClickBlock event, Level level) {
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        event.setCanceled(true);
    }
}
