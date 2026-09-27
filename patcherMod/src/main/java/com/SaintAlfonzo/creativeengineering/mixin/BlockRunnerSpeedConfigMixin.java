package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.surface.SurfaceSpeeds;
import com.google.gson.JsonObject;

import fuzs.blockrunner.data.BlockSpeedManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Puts CEConfig {@code [surface.speed]} into BlockRunner ({@link SurfaceSpeeds#apply}).
 * BlockRunner's private {@code deserialize(FileReader)} reads {@code config/blockrunner.json} into
 * a JsonObject and passes it to the public {@code deserialize(JsonObject)}; this edits the object in
 * between. Only the file path is hooked: clients call {@code deserialize(JsonObject)} directly with
 * the values the server sent, and those must stay the server's.
 *
 * <p>{@code require = 0}, like {@code BlockRunnerBlockStateMixin}: a BlockRunner update that changes
 * this method doesn't crash the game; {@link SurfaceSpeeds#onServerStarted} logs an error instead.
 */
@Mixin(value = BlockSpeedManager.class, remap = false)
public abstract class BlockRunnerSpeedConfigMixin {

    @ModifyArg(method = "deserialize(Ljava/io/FileReader;)V",
            at = @At(value = "INVOKE", target = "Lfuzs/blockrunner/data/BlockSpeedManager;deserialize(Lcom/google/gson/JsonObject;)V"),
            require = 0)
    private JsonObject creativeengineering$surfaceSpeeds(JsonObject json) {
        try {
            return SurfaceSpeeds.apply(json);
        } catch (Throwable ignored) {
            return json; // keep BlockRunner's own values
        }
    }
}
