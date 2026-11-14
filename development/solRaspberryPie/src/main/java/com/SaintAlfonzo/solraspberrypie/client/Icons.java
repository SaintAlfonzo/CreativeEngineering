package com.SaintAlfonzo.solraspberrypie.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

/**
 * Small drawing helpers shared by the screens and the widget. Every icon is drawn from vanilla's own GUI sprites,
 * effect sprites or item models; nothing here ships a texture.
 */
public final class Icons {
    public static void effect(PoseStack pose, ResourceLocation id, int x, int y) {
        effect(pose, id, x, y, 18);
    }

    public static void effect(PoseStack pose, ResourceLocation id, int x, int y, int size) {
        MobEffect effect = id == null ? null : ForgeRegistries.MOB_EFFECTS.getValue(id);
        if (effect == null) return;
        TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(effect);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, sprite.atlas().location());
        GuiComponent.blit(pose, x, y, 0, size, size, sprite);
    }

    /** Vanilla's full heart (gui/icons.png), 9x9. */
    public static void heart(PoseStack pose, int x, int y) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        GuiComponent.blit(pose, x, y, 16, 0, 9, 9, 256, 256);
        GuiComponent.blit(pose, x, y, 52, 0, 9, 9, 256, 256);
    }

    /** Vanilla's full armour point (gui/icons.png), 9x9. */
    public static void armor(PoseStack pose, int x, int y) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        GuiComponent.blit(pose, x, y, 34, 9, 9, 9, 256, 256);
    }

    /** An item model drawn at {@code size} pixels (16 = normal). */
    public static void item(ItemStack stack, int x, int y, int size) {
        PoseStack mv = RenderSystem.getModelViewStack();
        mv.pushPose();
        mv.translate(x, y, 0);
        mv.scale(size / 16f, size / 16f, 1);
        RenderSystem.applyModelViewMatrix();
        Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, 0, 0);
        mv.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    /** A one-pixel frame around a {@code size} square. */
    public static void frame(PoseStack pose, int x, int y, int size, int color) {
        GuiComponent.fill(pose, x - 1, y - 1, x + size + 1, y, color);
        GuiComponent.fill(pose, x - 1, y + size, x + size + 1, y + size + 1, color);
        GuiComponent.fill(pose, x - 1, y, x, y + size, color);
        GuiComponent.fill(pose, x + size, y, x + size + 1, y + size, color);
    }

    /** Greys out whatever is drawn under a {@code size} square (above item models too). */
    public static void dim(PoseStack pose, int x, int y, int size) {
        pose.pushPose();
        pose.translate(0, 0, 300);
        GuiComponent.fill(pose, x - 1, y - 1, x + size + 1, y + size + 1, 0xB0D8D0C0);
        pose.popPose();
    }

    public static String num(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.format(Locale.ROOT, "%.1f", v);
    }

    private Icons() {}
}
