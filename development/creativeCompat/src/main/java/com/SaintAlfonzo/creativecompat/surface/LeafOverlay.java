package com.SaintAlfonzo.creativecompat.surface;

import com.SaintAlfonzo.creativecompat.CompatConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fml.ModList;

/**
 * Leaf overlay (plan_post_scratch_1115.md G4e, G7.14), client: while the local player is in a leaf layer block, the
 * screen shows that pile's leaves, drawn over the view like the powder snow frost. The texture is the leaf type's own
 * block texture at runtime (Immersive Weathering oak pile -> oak leaves; Twigs bamboo leaves and the full bamboo leaves
 * -> their own), tiled with transparency and tinted with the block color at the pile (biome foliage for tinted
 * leaves). No texture file of its own. Opacity grows with the layers (0.7 at 8). A pile whose texture cannot be found
 * shows nothing. Switch {@code [surface.passthrough.layers] leafOverlay} (on by default).
 */
public final class LeafOverlay {

	private LeafOverlay() {}

	/** Tile size on screen, GUI pixels. */
	private static final int TILE = 64;
	private static final float MAX_ALPHA = 0.7F;

	public static void register(RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.FROSTBITE.id(), "leaf_pile", LeafOverlay::render);
	}

	static void render(ForgeGui gui, PoseStack pose, float partialTick, int width, int height) {
		if (!CompatConfig.SPEC.isLoaded() || !CompatConfig.SURFACE.passthrough.leafOverlay.get()) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null || !mc.options.getCameraType().isFirstPerson()) return;
		PassState st = PassThrough.state(mc.player, false);
		if (st == null || st.result.leafLayers < 2) return;
		BlockPos pos = BlockPos.of(st.result.leafCell);
		BlockState pile = mc.level.getBlockState(pos);
		if (!PassThrough.openLayers(pile)) return;
		BlockState source = sourceLeaves(pile);
		TextureAtlasSprite sprite = mc.getBlockRenderer().getBlockModel(source).getParticleIcon(ModelData.EMPTY);
		if (sprite == null || MissingTextureAtlasSprite.getLocation().equals(sprite.getName())) return;
		int color = mc.getBlockColors().getColor(source, mc.level, pos, 0);
		float r = 1.0F, g = 1.0F, b = 1.0F;
		if (color != -1) {
			r = (color >> 16 & 255) / 255.0F;
			g = (color >> 8 & 255) / 255.0F;
			b = (color & 255) / 255.0F;
		}
		int max = Math.max(1, PassGrowth.layersMax(pile));
		float alpha = MAX_ALPHA * Math.min(1.0F, st.result.leafLayers / (float) max);
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(r, g, b, alpha);
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		for (int x = 0; x < width; x += TILE) {
			for (int y = 0; y < height; y += TILE) {
				GuiComponent.blit(pose, x, y, 0, TILE, TILE, sprite);
			}
		}
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
	}

	/** The leaves block a pile is made of (IW leaf piles), else the pile itself. */
	static BlockState sourceLeaves(BlockState pile) {
		if (ModList.get().isLoaded("immersive_weathering")) {
			BlockState s = IwLeaves.source(pile);
			if (s != null) return s;
		}
		return pile;
	}

	/** Immersive Weathering classes only load here, with IW present. */
	static final class IwLeaves {
		static BlockState source(BlockState pile) {
			if (pile.getBlock() instanceof com.ordana.immersive_weathering.blocks.LeafPileBlock lp && lp.getLeafType() != null
					&& lp.getLeafType().leaves != null) {
				return lp.getLeafType().leaves.defaultBlockState();
			}
			return null;
		}
	}
}
