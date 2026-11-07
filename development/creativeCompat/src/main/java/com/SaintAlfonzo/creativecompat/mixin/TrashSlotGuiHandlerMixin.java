package com.SaintAlfonzo.creativecompat.mixin;

import com.SaintAlfonzo.creativecompat.compat.TrashSlotCompat;
import net.blay09.mods.balm.api.event.client.screen.ScreenInitEvent;
import net.blay09.mods.trashslot.api.IGuiContainerLayout;
import net.blay09.mods.trashslot.client.ContainerSettings;
import net.blay09.mods.trashslot.client.TrashSlotGuiHandler;
import net.blay09.mods.trashslot.client.TrashSlotSlot;
import net.blay09.mods.trashslot.client.deletion.DeletionProvider;
import net.blay09.mods.trashslot.client.gui.TrashSlotComponent;
import net.blay09.mods.trashslot.config.TrashSlotConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * TrashSlot's per-screen setup and two creative-mode fixes; pack defaults themselves live in
 * {@link TrashSlotCompat}.
 *
 * <p>{@code onScreenInit} keeps TrashSlot's own body with four targeted changes (no overwrite):
 * settings come from {@link TrashSlotCompat#settingsFor} instead of {@code TrashSlotSaveState.getSettings}
 * (never {@code NONE}, so the slot component is always built); the creative inventory screen and the survival
 * inventory of a creative player get a slot (TrashSlot skipped both); and the spectator and server-missing early
 * returns also clear the previous screen's state instead of leaving it active.
 *
 * <p>In the creative screen the trash slot uses {@link TrashSlotCompat#CREATIVE} (clears the
 * client-side cursor), and the "delete hovered slot" key branch is kept off: it sends the picker
 * menu's slot index to the server, which would apply it to the player's real inventory menu and
 * delete the wrong slot. TrashSlot's own creative delete-key branch, which maps the index, still runs.
 *
 * <p>The trashed item's tooltip ({@code onScreenDrawn}) is drawn with Forge's overload that carries the
 * stack, so AppleSkin's food bars show on a trashed food item.
 */
@Mixin(value = TrashSlotGuiHandler.class, remap = false)
public abstract class TrashSlotGuiHandlerMixin {
    @Shadow @Final private static TrashSlotSlot trashSlot;
    @Shadow private static TrashSlotComponent trashSlotComponent;
    @Shadow private static ContainerSettings currentContainerSettings;

    @Unique
    private static void creativecompat$clearState() {
        currentContainerSettings = ContainerSettings.NONE;
        trashSlotComponent = null;
    }

    /** Settings from the pack's defaults per screen and per backpack tier. */
    @Redirect(method = "onScreenInit", at = @At(value = "INVOKE",
            target = "Lnet/blay09/mods/trashslot/TrashSlotSaveState;getSettings(Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;Lnet/blay09/mods/trashslot/api/IGuiContainerLayout;)Lnet/blay09/mods/trashslot/client/ContainerSettings;"))
    private static ContainerSettings creativecompat$settings(AbstractContainerScreen<?> screen, IGuiContainerLayout layout) {
        return TrashSlotCompat.settingsFor(screen, layout);
    }

    /**
     * The creative inventory screen check: its only use is {@code instanceof CreativeModeInventoryScreen}, so no screen
     * here lets the creative screen go on like any container screen.
     */
    @Redirect(method = "onScreenInit", at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/blay09/mods/balm/api/event/client/screen/ScreenInitEvent$Post;getScreen()Lnet/minecraft/client/gui/screens/Screen;"))
    private static Screen creativecompat$noCreativeSkip(ScreenInitEvent.Post event) {
        return null;
    }

    /** The survival inventory of a creative player: read as not creative, so it gets a slot. */
    @Redirect(method = "onScreenInit", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD,
            target = "Lnet/minecraft/world/entity/player/Abilities;f_35937_:Z"))
    private static boolean creativecompat$noCreativeInventorySkip(Abilities abilities) {
        return false;
    }

    /** A spectator's screen: also drop the previous screen's slot. */
    @Redirect(method = "onScreenInit", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;m_5833_()Z"))
    private static boolean creativecompat$spectatorClears(Player player) {
        boolean spectator = player.isSpectator();
        if (spectator) creativecompat$clearState();
        return spectator;
    }

    /** TrashSlot missing on the server (first screen, the one with the message): also drop the previous slot. */
    @Inject(method = "onScreenInit", at = @At(value = "FIELD", opcode = Opcodes.PUTSTATIC,
            target = "Lnet/blay09/mods/trashslot/client/TrashSlotGuiHandler;sentMissingMessage:Z"))
    private static void creativecompat$serverMissingClears(ScreenInitEvent.Post event, CallbackInfo ci) {
        creativecompat$clearState();
    }

    @Redirect(
            method = "onMouseClick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/blay09/mods/trashslot/config/TrashSlotConfig;getDeletionProvider()Lnet/blay09/mods/trashslot/client/deletion/DeletionProvider;"
            )
    )
    private static DeletionProvider creativeengineering$creativeDeletion() {
        DeletionProvider provider = TrashSlotConfig.getDeletionProvider();
        if (provider != null && Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen) {
            return TrashSlotCompat.CREATIVE;
        }
        return provider;
    }

    @Redirect(
            method = "runKeyBindings",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/blay09/mods/trashslot/client/ContainerSettings;isEnabled()Z",
                    ordinal = 0
            )
    )
    private static boolean creativeengineering$noSlotDeleteInCreative(ContainerSettings settings) {
        if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen) {
            return false;
        }
        return settings.isEnabled();
    }

    @Redirect(
            method = "onScreenDrawn",
            remap = false,
            require = 1,
            at = @At(
                    value = "INVOKE",
                    remap = false,
                    target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;m_96597_(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/List;II)V"
            )
    )
    private static void creativeengineering$trashTooltipWithStack(AbstractContainerScreen<?> screen, PoseStack poseStack,
                                                                 List<Component> lines, int x, int y) {
        screen.renderComponentTooltip(poseStack, lines, x, y, trashSlot.getItem());
    }
}
