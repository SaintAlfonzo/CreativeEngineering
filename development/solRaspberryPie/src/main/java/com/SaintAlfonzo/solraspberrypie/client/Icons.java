// SPDX-License-Identifier: GPL-3.0-only
package com.SaintAlfonzo.solraspberrypie.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Locale;
import java.util.Map;

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

	/** Vanilla's full armor point (gui/icons.png), 9x9. */
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

	/** Grays out whatever is drawn under a {@code size} square (above item models too). */
	public static void dim(PoseStack pose, int x, int y, int size) {
		pose.pushPose();
		pose.translate(0, 0, 300);
		GuiComponent.fill(pose, x - 1, y - 1, x + size + 1, y + size + 1, 0xB0D8D0C0);
		pose.popPose();
	}

	private static final Map<List<? extends String>, ItemStack> CONFIGURED = new java.util.IdentityHashMap<>();

	/**
	 * The first item of a config list that is loaded (G5d: an item of another mod, then a vanilla fallback). An entry
	 * may carry NBT in braces after the id. Empty when none is loaded or every entry is malformed.
	 */
	public static ItemStack configured(List<? extends String> ids) {
		// Per frame: the config list object is the key (a config reload brings a new list), no join or copy
		// (PR-RP-20). The stack is shared: callers draw it and never change it.
		return CONFIGURED.computeIfAbsent(ids, k -> {
			for (String entry : ids) {
				int brace = entry.indexOf('{');
				ResourceLocation id = ResourceLocation.tryParse((brace < 0 ? entry : entry.substring(0, brace)).trim());
				if (id == null || !ForgeRegistries.ITEMS.containsKey(id)) continue;
				ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getValue(id));
				if (brace >= 0) {
					try {
						stack.setTag(TagParser.parseTag(entry.substring(brace)));
					} catch (CommandSyntaxException e) {
						continue;
					}
				}
				return stack;
			}
			return ItemStack.EMPTY;
		});
	}

	/**
	 * A diversity value as printed by Raspberry Pie: two decimals, rounded down (plan_diet_books_r2.md ruling 4),
	 * so 4.999 shows 4.99 and never reads as a milestone of 5 not reached yet. The small
	 * epsilon keeps 1.40 stored as 1.3999999 at 1.40.
	 */
	public static String diversity(double v) {
		return String.format(Locale.ROOT, "%.2f", Math.floor(v * 100 + 1e-6) / 100.0);
	}

	/** Grays out a {@code size} square without covering a frame drawn around it. */
	public static void dimInner(PoseStack pose, int x, int y, int size) {
		pose.pushPose();
		pose.translate(0, 0, 300);
		GuiComponent.fill(pose, x, y, x + size, y + size, 0xB0D8D0C0);
		pose.popPose();
	}

	public static String num(double v) {
		return v == Math.floor(v) ? String.valueOf((long) v) : String.format(Locale.ROOT, "%.1f", v);
	}

	private Icons() {}
}
