package com.SaintAlfonzo.creativecompat.surface;

import java.util.UUID;

import fuzs.blockrunner.client.helper.FieldOfViewHelper;
import fuzs.blockrunner.data.BlockSpeedManager;
import net.minecraft.world.entity.player.Player;

/** BlockRunner's side of {@link PassFov}, in its own class so nothing loads BlockRunner classes without BlockRunner. */
final class BlockRunnerFov {

	private BlockRunnerFov() {}

	/** BlockRunner's own answer: its option is on, its modifier is present, not scoping in first person. */
	static boolean shouldRemove(Player player) {
		return FieldOfViewHelper.shouldRemoveBlockSpeedModifier(player);
	}

	static UUID uuid() {
		return BlockSpeedManager.SPEED_MODIFIER_BLOCK_SPEED_UUID;
	}
}
