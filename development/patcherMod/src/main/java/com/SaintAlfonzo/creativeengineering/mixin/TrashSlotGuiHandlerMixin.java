package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.compat.TrashSlotCompat;
import net.blay09.mods.balm.api.event.client.screen.ScreenInitEvent;
import net.blay09.mods.trashslot.TrashSlot;
import net.blay09.mods.trashslot.api.IGuiContainerLayout;
import net.blay09.mods.trashslot.client.ContainerSettings;
import net.blay09.mods.trashslot.client.LayoutManager;
import net.blay09.mods.trashslot.client.ModKeyMappings;
import net.blay09.mods.trashslot.client.TrashSlotGuiHandler;
import net.blay09.mods.trashslot.client.TrashSlotSlot;
import net.blay09.mods.trashslot.client.deletion.DeletionProvider;
import net.blay09.mods.trashslot.client.gui.TrashSlotComponent;
import net.blay09.mods.trashslot.config.TrashSlotConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * TrashSlot's per-screen setup and two creative-mode fixes; pack defaults themselves live in
 * {@link TrashSlotCompat}.
 *
 * <p>{@code onScreenInit} is overwritten (same logic as TrashSlot 12.1.0) with three changes:
 * settings come from {@link TrashSlotCompat#settingsFor} instead of {@code TrashSlotSaveState.getSettings};
 * the creative inventory screen and the survival inventory of a creative player get a slot
 * (TrashSlot skipped both); and the spectator / server-missing early returns also clear the previous
 * screen's state instead of leaving it active.
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
    @Shadow private static boolean sentMissingMessage;

    @Shadow
    private static void showHint(String id, MutableComponent message, int timeToDisplay) {}

    @Shadow
    private static void showHint(String id, MutableComponent message, int timeToDisplay, boolean force) {}

    /**
     * @author SaintAlfonzo
     * @reason pack defaults per screen, per-tier backpacks, trash slot in creative
     */
    @Overwrite
    private static void onScreenInit(ScreenInitEvent.Post event) {
        Screen eventScreen = event.getScreen();
        if (eventScreen.getClass().getName().startsWith("com.replaymod")) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if ((player != null && player.isSpectator()) || !(eventScreen instanceof AbstractContainerScreen<?> screen)) {
            currentContainerSettings = ContainerSettings.NONE;
            trashSlotComponent = null;
            return;
        }

        if (!TrashSlot.isServerSideInstalled && !sentMissingMessage) {
            TrashSlot.logger.info("TrashSlot is not installed on the server and thus will be unavailable.");
            MutableComponent message = Component.translatable("trashslot.serverNotInstalled");
            message.withStyle(ChatFormatting.RED);
            showHint("serverNotInstalled", message, 5000, true);
            sentMissingMessage = true;
            currentContainerSettings = ContainerSettings.NONE;
            trashSlotComponent = null;
            return;
        }

        IGuiContainerLayout layout = LayoutManager.getLayout(screen);
        currentContainerSettings = TrashSlotCompat.settingsFor(screen, layout);
        trashSlotComponent = new TrashSlotComponent(screen, layout, currentContainerSettings, trashSlot);
        if (!currentContainerSettings.isEnabled() && !layout.isEnabledByDefault()) {
            MutableComponent hint = Component.translatable("trashslot.hint.toggleOn",
                    ModKeyMappings.keyBindToggleSlot.getTranslatedKeyMessage());
            showHint("toggleOn", hint, 5000);
        }
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
