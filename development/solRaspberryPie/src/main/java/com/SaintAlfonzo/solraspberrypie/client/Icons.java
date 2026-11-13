package com.SaintAlfonzo.solraspberrypie.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

/** Small drawing helpers shared by the screens and the widget. */
public final class Icons {
    public static void effect(PoseStack pose, ResourceLocation id, int x, int y) {
        MobEffect effect = id == null ? null : ForgeRegistries.MOB_EFFECTS.getValue(id);
        if (effect == null) return;
        TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(effect);
        RenderSystem.setShaderTexture(0, sprite.atlas().location());
        GuiComponent.blit(pose, x, y, 0, 18, 18, sprite);
    }

    public static String num(double v) {
        return v == Math.floor(v) ? String.valueOf((long) v) : String.format(Locale.ROOT, "%.1f", v);
    }

    private Icons() {}
}
