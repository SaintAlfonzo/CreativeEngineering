package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.util.StaleJarHandles;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.TimeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Frees the file handles Forge leaks for each mod jar ({@link StaleJarHandles}) just before GLFW starts, so GLFW's
 * X11 connection and wakeup pipe get numbers below 1024. Without it a client with about 420 or more jars died in
 * {@code glfwCreateWindow} ("stack smashing detected", or a SIGSEGV in libglfw) (feature_writeup/jar_handle_leak.md).
 * Freeing them at window creation is too late: glfwInit opens those descriptors earlier.
 *
 * <p>Vanilla-class target by SRG name ({@code m_240147_} = {@code RenderSystem.initBackendSystem}, static, which
 * calls glfwInit) with {@code remap = false}, same idiom as {@link SyncGlDebugOutputMixin}.
 */
@Mixin(value = RenderSystem.class, remap = false)
public class FdReleaseMixin {

	@Inject(method = "m_240147_", at = @At("HEAD"))
	private static void creativecommon$releaseStaleJarHandles(CallbackInfoReturnable<TimeSource.NanoTimeSource> cir) {
		StaleJarHandles.release();
	}
}
