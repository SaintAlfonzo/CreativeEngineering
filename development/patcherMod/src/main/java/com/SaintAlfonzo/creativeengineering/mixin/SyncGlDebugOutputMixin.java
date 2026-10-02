package com.SaintAlfonzo.creativeengineering.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GLCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fixes the client JVM never exiting after quitting (window closes, process lingers at 0% CPU,
 * ATLauncher stays hidden waiting on it). Proven with gdb on a hung client:
 *
 * <ul>
 *   <li>Vanilla sets up GL debug output <i>asynchronously</i> ({@code Minecraft.<init>} calls
 *       {@code RenderSystem.initRenderer(glDebugVerbosity, false)}), and Distant Horizons swaps in
 *       its own callback via LWJGL's {@code GLUtil.setupDebugMessageCallback}, also async. With
 *       async output the NVIDIA driver invokes the callback from its own worker thread, so LWJGL
 *       JNI-attaches that driver thread to the JVM.</li>
 *   <li>On quit, {@code System.exit} -> {@code vm_exit} -> libc {@code exit()} -> the
 *       {@code libGLX_nvidia} destructor {@code pthread_join}s that worker; the worker's LWJGL TSD
 *       destructor calls {@code DetachCurrentThread}, which parks forever once the VM has exited.
 *       Deadlock.</li>
 * </ul>
 *
 * <p>Enabling {@code GL_DEBUG_OUTPUT_SYNCHRONOUS} guarantees every debug callback runs on the
 * thread that issued the GL call (the Render thread), so no driver thread is ever attached. It is
 * context state, so it also covers callbacks installed later (DH's). Enabled unconditionally -
 * vanilla only sets it when {@code sync} is true <i>and</i> verbosity > 0, but DH turns debug
 * output on itself regardless of the vanilla verbosity.
 *
 * <p>Vanilla-class target, matched by SRG name ({@code m_69580_} = {@code initRenderer(int,
 * boolean)}, static) with {@code remap = false} - same idiom as {@link CreativeMenuDisableMixin}.
 */
@Mixin(value = RenderSystem.class, remap = false)
public class SyncGlDebugOutputMixin {

    @Inject(method = "m_69580_", at = @At("TAIL"))
    private static void creativeengineering$syncGlDebugOutput(int debugVerbosity, boolean synchronous, CallbackInfo ci) {
        GLCapabilities caps = GL.getCapabilities();
        if (caps.OpenGL43 || caps.GL_KHR_debug) {
            GL11.glEnable(GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        }
    }
}
