package com.SaintAlfonzo.creativeengineering.stepstool.client;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.stepstool.Stepstool;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

/**
 * The client half of {@link Stepstool}: the step-up keys, stepping off on input, and the stool drawn under every
 * player standing on one (the local player too, so it shows in first person).
 */
public final class StepstoolClient {

    public static final KeyMapping STEP_UP = new KeyMapping("key.creativeengineering.stepstool",
            KeyConflictContext.IN_GAME, InputConstants.UNKNOWN, "key.categories.movement");

    private static int holdTicks;
    // The step-up keys must be let go before they do anything again (after a step up, a step off or a refusal).
    private static boolean latched;
    // Jump steps off only on a fresh press: the jump half of Sprint + Jump is still down right after stepping up.
    private static boolean jumpWasDown;

    private StepstoolClient() {
    }

    /** MOD bus. */
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(STEP_UP);
    }

    /** Fired every tick before the player moves, so stepping off and the held-jump block take effect this tick. */
    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) {
            return;
        }
        Input input = event.getInput();
        Options options = Minecraft.getInstance().options;
        boolean combo = CEConfig.STEPSTOOL.sprintJump.get() && options.keySprint.isDown() && options.keyJump.isDown();
        boolean trigger = combo || STEP_UP.isDown();
        boolean moving = input.forwardImpulse != 0.0F || input.leftImpulse != 0.0F;
        boolean jumpPressed = options.keyJump.isDown() && !jumpWasDown;
        jumpWasDown = options.keyJump.isDown();
        if (!trigger) {
            latched = false;
            holdTicks = 0;
        }

        if (Stepstool.CLIENT.containsKey(player.getId())) {
            if (moving || input.shiftKeyDown || jumpPressed) {
                // Off at once (the server agrees a moment later), so the player falls this tick.
                Stepstool.CLIENT.remove(player.getId());
                Stepstool.CHANNEL.sendToServer(new Stepstool.Request(false));
                latched = trigger;
            }
            // A jump from the stool top would go a block higher than the player expects.
            input.jumping = false;
            return;
        }

        boolean ready = Stepstool.canStepUp(player) && !moving;
        if (combo && ready) {
            input.jumping = false; // the jump half of Sprint + Jump: no jump while it steps up
        }
        if (trigger && ready && !latched) {
            if (++holdTicks >= CEConfig.STEPSTOOL.holdTicks.get()) {
                Stepstool.CHANNEL.sendToServer(new Stepstool.Request(true));
                latched = true;
                holdTicks = 0;
            }
        } else {
            holdTicks = 0;
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS || Stepstool.CLIENT.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) {
            return;
        }
        Vec3 cam = event.getCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        BlockRenderDispatcher blocks = mc.getBlockRenderer();
        for (Map.Entry<Integer, Stepstool.Anchor> entry : Stepstool.CLIENT.entrySet()) {
            Entity entity = level.getEntity(entry.getKey());
            Stepstool.Anchor a = entry.getValue();
            BlockState state = Stepstool.stoolBlock(a.stool());
            if (entity == null || state == null || entity.isInvisibleTo(mc.player)) {
                continue;
            }
            poseStack.pushPose();
            poseStack.translate(a.x() - 0.5 - cam.x, a.y() - cam.y, a.z() - 0.5 - cam.z);
            int light = LevelRenderer.getLightColor(level, new BlockPos(a.x(), a.y() + 0.5, a.z()));
            blocks.renderSingleBlock(state, poseStack, buffers, light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
            poseStack.popPose();
        }
        buffers.endBatch();
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        Stepstool.CLIENT.clear();
        latched = false;
        jumpWasDown = false;
        holdTicks = 0;
    }
}
