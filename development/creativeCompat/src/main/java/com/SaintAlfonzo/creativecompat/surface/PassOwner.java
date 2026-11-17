package com.SaintAlfonzo.creativecompat.surface;

import java.lang.reflect.Method;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;

/**
 * SecurityCraft's reinforced cobweb lets its owner through (its {@code entityInside} returns before
 * {@code makeStuckInBlock} for the owner, phase 0 brief 1.2 and implication 4). The generic stuck cancel removes that
 * check, so {@link PassThrough} asks here instead: a player who owns the block entity at the cell passes through it.
 * Through reflection on SecurityCraft's public API ({@code IOwnable#isOwnedBy(Player)}), so Compat needs no
 * SecurityCraft jar to build.
 */
final class PassOwner {

	private PassOwner() {}

	private static final Class<?> OWNABLE;
	private static final Method IS_OWNED_BY;

	static {
		Class<?> c = null;
		Method m = null;
		if (ModList.get() != null && ModList.get().isLoaded("securitycraft")) {
			try {
				c = Class.forName("net.geforcemods.securitycraft.api.IOwnable");
				m = c.getMethod("isOwnedBy", Player.class);
			} catch (Throwable t) {
				c = null;
				m = null;
			}
		}
		OWNABLE = c;
		IS_OWNED_BY = m;
	}

	/** The entity is a player who owns the SecurityCraft block at {@code pos}. */
	static boolean ownerPasses(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (IS_OWNED_BY == null || !(entity instanceof Player player) || !state.hasBlockEntity()) {
			return false;
		}
		BlockEntity be = level.getBlockEntity(pos);
		if (!OWNABLE.isInstance(be)) {
			return false;
		}
		try {
			return (Boolean) IS_OWNED_BY.invoke(be, player);
		} catch (Throwable t) {
			return false;
		}
	}
}
