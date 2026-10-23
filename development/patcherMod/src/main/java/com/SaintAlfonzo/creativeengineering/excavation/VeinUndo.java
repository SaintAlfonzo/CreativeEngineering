package com.SaintAlfonzo.creativeengineering.excavation;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import oreexcavation.undo.BlockHistory;
import oreexcavation.undo.ExcavateHistory;

/**
 * Rules on Ore Excavation's {@code /undo_excavation}, checked from {@code mixin/OreExcavationCommandUndoMixin}
 * before Ore Excavation runs its own checks (exact drops and XP back in the inventory, nothing in the way):
 * a per-player cooldown after each successful undo ({@code [vein_undo] cooldownSeconds}), and a refusal when the
 * newest vein mine broke a block in {@code #creativeengineering:vein_undo_denied} or, with
 * {@code denyBlockEntities}, any block with a block entity. The tag is filled by KubeJS
 * (server_scripts/tags/blocktags.js). Ore Excavation keeps its history in memory, so does the cooldown.
 */
public final class VeinUndo {

    public static final TagKey<Block> DENIED =
            BlockTags.create(new ResourceLocation(CreativeEngineering.MODID, "vein_undo_denied"));

    /** Player id to {@link System#nanoTime()} of their last successful undo. */
    private static final Map<UUID, Long> LAST_UNDO = new ConcurrentHashMap<>();

    private VeinUndo() {}

    /** Why this player can't undo right now, or null when Ore Excavation may go ahead. */
    public static Component refusal(Player player) {
        long cooldownNanos = CEConfig.VEIN_UNDO.cooldownSeconds.get() * 1_000_000_000L;
        Long last = LAST_UNDO.get(player.getUUID());
        if (last != null) {
            long left = cooldownNanos - (System.nanoTime() - last);
            if (left > 0) {
                return Component.translatable("creativeengineering.vein_undo.cooldown", (left + 999_999_999L) / 1_000_000_000L);
            }
        }

        ArrayDeque<ExcavateHistory> undos = VeinUndoAccess.scheduler().creativeengineering$undoHistory().get(player.getUUID());
        ExcavateHistory newest = undos == null ? null : undos.peek();
        if (newest == null) return null; // Ore Excavation says "no history" itself

        boolean denyBlockEntities = CEConfig.VEIN_UNDO.denyBlockEntities.get();
        for (BlockHistory block : ((VeinUndoAccess.History) newest).creativeengineering$history()) {
            BlockState state = ((VeinUndoAccess.Block) block).creativeengineering$state();
            if (state.is(DENIED) || (denyBlockEntities && state.hasBlockEntity())) {
                return Component.translatable("creativeengineering.vein_undo.denied", state.getBlock().getName());
            }
        }
        return null;
    }

    public static void startCooldown(Player player) {
        LAST_UNDO.put(player.getUUID(), System.nanoTime());
    }
}
