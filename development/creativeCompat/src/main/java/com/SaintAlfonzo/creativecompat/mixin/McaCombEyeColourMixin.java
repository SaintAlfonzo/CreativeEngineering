package com.SaintAlfonzo.creativecompat.mixin;

import forge.net.mca.MCA;
import forge.net.mca.client.gui.CombScreen;
import forge.net.mca.client.gui.VillagerEditorScreen;
import forge.net.mca.cobalt.network.NetworkHandler;
import forge.net.mca.entity.VillagerEntityMCA;
import forge.net.mca.entity.ai.Genetics;
import forge.net.mca.network.c2s.DamageItemMessage;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MCA Comb: eye colour and eye shape controls on its hairstyle page, for the player's own look and for a combed villager.
 * MCA paints the eyes into 22 face textures (2 shapes x 11 colours, same colour order for both genders) and picks one from the
 * FACE gene, n = (int) clamp(FACE * 22, 0, 21) in FaceLayer.getSkin. The buttons set FACE to the middle of the chosen slot on
 * the editor's villager copy; the hair previews copy that villager every frame, so they show the new eyes. MCA's own save
 * (syncVillagerData, from Done or a hairstyle pick) sends it; the server's "sync" command saves the whole tag, so no server
 * part is needed. Changed eyes cost one comb use, charged at that save; Escape discards them like MCA's other edits.
 */
@Mixin(value = VillagerEditorScreen.class, remap = false)
public abstract class McaCombEyeColourMixin extends Screen {

    @Unique
    private static final String[] CE_EYE_COLOURS = {
            "brown", "pink", "amber", "purple", "green", "blue", "magenta", "cyan", "red", "grey", "yellow"
    };
    @Unique
    private static final int CE_FACES = 2 * CE_EYE_COLOURS.length;

    @Shadow
    @Final
    protected VillagerEntityMCA villager;

    @Unique
    private boolean ce$eyesChanged;

    private McaCombEyeColourMixin(Component title) {
        super(title);
    }

    @Inject(method = "setPage", at = @At("TAIL"), require = 1)
    private void ce$addEyeButtons(String page, CallbackInfo ci) {
        if (!((Object) this instanceof CombScreen) || !"hair".equals(page)) {
            return;
        }
        int y = this.height / 2 - 100;
        int x = this.width / 2 + 92;
        Button label = this.addRenderableWidget(new Button(x + 20, y, 68, 20, ce$colourLabel(), b -> {
        }));
        label.active = false;
        this.addRenderableWidget(new Button(x, y, 20, 20, Component.literal("<"), b -> {
            ce$setFace(ce$shape(), (ce$colour() + CE_EYE_COLOURS.length - 1) % CE_EYE_COLOURS.length);
            label.setMessage(ce$colourLabel());
        }));
        this.addRenderableWidget(new Button(x + 88, y, 20, 20, Component.literal(">"), b -> {
            ce$setFace(ce$shape(), (ce$colour() + 1) % CE_EYE_COLOURS.length);
            label.setMessage(ce$colourLabel());
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 192, y, 100, 20, ce$shapeLabel(), b -> {
            ce$setFace(1 - ce$shape(), ce$colour());
            b.setMessage(ce$shapeLabel());
        }));
    }

    @Inject(method = "syncVillagerData", at = @At("HEAD"), require = 1)
    private void ce$chargeComb(CallbackInfo ci) {
        if (this.ce$eyesChanged && (Object) this instanceof CombScreen) {
            this.ce$eyesChanged = false;
            NetworkHandler.sendToServer(new DamageItemMessage(MCA.locate("comb")));
        }
    }

    @Unique
    private int ce$face() {
        return (int) Math.min(CE_FACES - 1, Math.max(0, this.villager.getGenetics().getGene(Genetics.FACE) * CE_FACES));
    }

    @Unique
    private int ce$shape() {
        return ce$face() / CE_EYE_COLOURS.length;
    }

    @Unique
    private int ce$colour() {
        return ce$face() % CE_EYE_COLOURS.length;
    }

    @Unique
    private void ce$setFace(int shape, int colour) {
        int face = shape * CE_EYE_COLOURS.length + colour;
        this.villager.getGenetics().setGene(Genetics.FACE, (face + 0.5F) / CE_FACES);
        this.ce$eyesChanged = true;
    }

    @Unique
    private Component ce$colourLabel() {
        return Component.translatable("creativeengineering.mca_eyes.colour." + CE_EYE_COLOURS[ce$colour()]);
    }

    @Unique
    private Component ce$shapeLabel() {
        return Component.translatable("creativeengineering.mca_eyes.shape", ce$shape() + 1);
    }
}
