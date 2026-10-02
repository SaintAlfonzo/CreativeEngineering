package com.SaintAlfonzo.creativeengineering.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Makes every Level's shared {@code random} safe to touch from more than one thread.
 *
 * THE BUG:
 *   Level#random (f_46441_) is a plain LegacyRandomSource. Its next() does a single
 *   compareAndSet on the seed and, if another thread got there first, throws
 *   "Accessing LegacyRandomSource from multiple threads" - a hard server crash. Regrowth calls
 *   entity.level.random.nextDouble() every tick for every mob, so it's the usual victim
 *   (debug/crash-2026-09-27_22.10.51-server.txt), but the real culprit is some off-thread code
 *   using the same random. The dump only records the losing thread, so it can't be named;
 *   Distant Horizons' FEATURES-mode world gen (mod Feature.place on DH threads) is the prime
 *   suspect.
 *
 * THE FIX:
 *   Vanilla Level already builds a ThreadSafeLegacyRandomSource (RandomSource#createThreadSafe,
 *   m_216337_) for its own threadSafeRandom field. Redirect the constructor's
 *   RandomSource#create (m_216327_) calls to that factory: its next() retries the CAS instead
 *   of throwing. The two calls are the one-shot randValue seed (f_46435_) and the random field
 *   itself; both are unseeded, so nothing deterministic changes. Covers ServerLevel and
 *   ClientLevel alike.
 */
@Mixin(value = Level.class, remap = false)
public class LevelThreadSafeRandomMixin {

	@Redirect(method = "<init>", remap = false, require = 1,
			at = @At(value = "INVOKE", remap = false,
					target = "Lnet/minecraft/util/RandomSource;m_216327_()Lnet/minecraft/util/RandomSource;"))
	private RandomSource creativeengineering$threadSafeRandom() {
		return RandomSource.createThreadSafe();
	}
}
