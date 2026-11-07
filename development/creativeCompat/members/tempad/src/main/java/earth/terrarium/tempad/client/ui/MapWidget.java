// Behaviour translated from Olympus 1.0.18 (MIT, Terrarium): earth.terrarium.olympus.client.components.map (MapWidget,
// MapRenderer, MapTopologyAlgorithm, MapPalette): a top-down colour map of the loaded chunks around the player.
package earth.terrarium.tempad.client.ui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class MapWidget extends KitWidget {
    /** 1.21 textures/map/decorations/player.png is the first 8x8 icon of 1.19.2's map_icons.png. */
    private static final ResourceLocation MAP_ICONS = new ResourceLocation("textures/map/map_icons.png");
    private static final ResourceLocation TEXTURE = new ResourceLocation("tempad", "olympus_map");
    private static final Component LOADING = Component.translatable("olympus.ui.loading");

    private final State<MapRenderer> mapRenderer;
    private int scale;
    private boolean initialized;
    private ResourceLocation texture = Button.olympus("modal/modal_inset");

    public MapWidget(State<MapRenderer> state) {
        super(0, 0);
        this.mapRenderer = state;
        this.active = false;
    }

    public MapWidget withTexture(ResourceLocation texture) {
        this.texture = texture;
        return this;
    }

    public MapWidget withScale(int scale) {
        this.scale = scale;
        return this;
    }

    @Override
    protected void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        Sprites.blit(pose, texture, x, y, width, height);
        if (!initialized) {
            refreshMap();
            initialized = true;
        }
        MapRenderer map = mapRenderer.get();
        if (map == null) {
            var font = Minecraft.getInstance().font;
            font.drawShadow(pose, LOADING, x + width / 2f - font.width(LOADING) / 2f, y + height / 2f, 0xFFFFFF);
            return;
        }
        if (map.scale != scale * 2 + 16) refreshMap();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        map.render(pose, x + 1, y + 1, width - 2, height - 2);
        pose.pushPose();
        pose.translate(0, 0, 2);
        renderPlayerAvatar(player, pose);
        pose.popPose();
    }

    public void refreshMap() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        ChunkPos chunkPos = player.chunkPosition();
        int minX = chunkPos.getMinBlockX() - scale;
        int minZ = chunkPos.getMinBlockZ() - scale;
        int maxX = chunkPos.getMaxBlockX() + scale + 1;
        int maxZ = chunkPos.getMaxBlockZ() + scale + 1;
        if (scale / 8 > 12) {
            CompletableFuture.supplyAsync(() -> Topology.getColors(minX, minZ, maxX, maxZ, player.clientLevel, player))
                    .thenAcceptAsync(colors -> mapRenderer.set(new MapRenderer(colors, scale * 2 + 16)), Minecraft.getInstance());
        } else {
            mapRenderer.set(new MapRenderer(Topology.getColors(minX, minZ, maxX, maxZ, player.clientLevel, player), scale * 2 + 16));
        }
    }

    private void renderPlayerAvatar(LocalPlayer player, PoseStack pose) {
        float left = width / 2f;
        float top = height / 2f;
        double px = player.getX(), pz = player.getZ();
        double ox = px % 16.0 + (px >= 0 ? -8 : 8);
        double oz = pz % 16.0 + (pz >= 0 ? -8 : 8);
        ox *= width / 144.0;
        oz *= height / 144.0;
        pose.pushPose();
        pose.translate(x + left + ox, y + top + oz, 0);
        pose.mulPose(Vector3f.ZP.rotationDegrees(player.getYRot()));
        pose.translate(-4, -4, 0);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, MAP_ICONS);
        blit(pose, 0, 0, 0f, 0f, 8, 8, 128, 128);
        pose.popPose();
    }

    public static final class MapRenderer {
        private final int scale;

        public MapRenderer(int[][] colors, int scale) {
            DynamicTexture dynamicTexture = new DynamicTexture(scale, scale, true);
            Minecraft.getInstance().getTextureManager().register(TEXTURE, dynamicTexture);
            NativeImage image = dynamicTexture.getPixels();
            if (image != null) {
                for (int i = 0; i < scale; i++) {
                    for (int j = 0; j < scale; j++) image.setPixelRGBA(i, j, colors[i][j]);
                }
                dynamicTexture.upload();
            }
            this.scale = scale;
        }

        void render(PoseStack pose, int x, int y, int width, int height) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            pose.pushPose();
            pose.translate(x, y, 0.01);
            Matrix4f m = pose.last().pose();
            BufferBuilder bb = Tesselator.getInstance().getBuilder();
            bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bb.vertex(m, 0f, height, -0.01f).uv(0f, 1f).endVertex();
            bb.vertex(m, width, height, -0.01f).uv(1f, 1f).endVertex();
            bb.vertex(m, width, 0f, -0.01f).uv(1f, 0f).endVertex();
            bb.vertex(m, 0f, 0f, -0.01f).uv(0f, 0f).endVertex();
            Tesselator.getInstance().end();
            pose.popPose();
        }
    }

    static final class Topology {
        /** MapPalette.COLORS (ABGR after the static block), indexed [LOWEST, LOW, NORMAL, HIGH]. */
        private static final int[][] PALETTE = {
                {0, 0, 0, 0}, {10139187, 7970610, 6786610, 5339696}, {16247203, 14731922, 13416325, 10324839},
                {13027271, 10988203, 9474966, 6974575}, {16728064, 14428678, 11605769, 8456971}, {11918335, 9087964, 7371444, 5526663},
                {10792109, 9804186, 7698039, 5461334}, {3572504, 2257939, 1466395, 1330204}, {16777215, 14212573, 11645365, 8684424},
                {10791864, 9277854, 7566977, 5658209}, {8151615, 7098421, 6111275, 4795680}, {7040626, 5790302, 4671565, 3816000},
                {6335742, 5213675, 3958988, 3231154}, {9402184, 8087102, 6575154, 4931366}, {16777200, 14473939, 12039343, 9473419},
                {13993767, 12873254, 11361574, 9784356}, {14117602, 12018127, 10508989, 8014494}, {6332621, 5867704, 5008790, 4083059},
                {15066408, 12959020, 10590769, 7893289}, {9486616, 7646743, 5803029, 3762709}, {15497387, 13528723, 11099509, 8604758},
                {4737096, 4276545, 3552822, 2829099}, {9474192, 8684676, 7171437, 5723991}, {4752016, 4353923, 3692906, 3096915},
                {6831271, 6764439, 5845115, 5056350}, {3168935, 2967191, 2570363, 2303582}, {6313520, 5719852, 4666661, 3614239},
                {7043120, 5925933, 4675365, 3359776}, {9456944, 8600365, 6956839, 5251110}, {1644568, 1512725, 1183760, 788745},
                {11839284, 10916407, 9929015, 8677939}, {5755063, 5290671, 4495508, 3829108}, {4625151, 4290266, 3825073, 3292550},
                {249641, 178217, 431148, 683572}, {8021806, 7230763, 5848356, 4533022}, {6950400, 6162689, 4981763, 3736587},
                {13022360, 11771530, 9666163, 7496028}, {9856034, 8867872, 7156764, 5514522}, {9196116, 8342356, 6766413, 5190721},
                {6710914, 6249846, 5262688, 4406604}, {13145906, 11173417, 8543256, 6111503}, {6975282, 5923630, 4673574, 3357984},
                {9920329, 8998467, 7223610, 5582645}, {3550497, 3155486, 2563096, 1971219}, {8417117, 7561556, 6179910, 4799032},
                {5396310, 4935503, 4079937, 3289908}, {7554373, 6831684, 5584191, 4335924}, {5258071, 4470094, 3484735, 2630194},
                {4733729, 4206622, 3416857, 2562068}, {5066024, 4343076, 3422238, 2435864}, {8803371, 7946536, 6434083, 4857117},
                {2300687, 2037005, 1642251, 1182472}, {11687213, 10567210, 8529190, 6496553}, {9190468, 8271687, 6696773, 5121595},
                {6035741, 5182745, 4198676, 3149071}, {1408886, 1273713, 1201499, 1063237}, {3638904, 3373427, 2843232, 2311243},
                {5319232, 4793654, 3874860, 3021086}, {1288551, 1284457, 1211482, 1203537}, {6905438, 6050900, 5065033, 4341310},
                {13482123, 12165247, 9927785, 7691605}, {7904648, 7180158, 5993577, 4807510}};

        static {
            for (int[] row : PALETTE) {
                for (int j = 0; j < row.length; j++) {
                    int c = row[j];
                    row[j] = 0xFF000000 | (c & 0xFF) << 16 | ((c >> 8) & 0xFF) << 8 | ((c >> 16) & 0xFF);
                }
            }
        }

        private static int paletteColor(int index, MaterialColor.Brightness brightness) {
            if (index >= PALETTE.length) return MaterialColor.getColorFromPackedId(MaterialColor.byId(index).getPackedId(brightness));
            return PALETTE[index][switch (brightness) {
                case LOWEST -> 0;
                case LOW -> 1;
                case NORMAL -> 2;
                case HIGH -> 3;
            }];
        }

        static int[][] getColors(int minX, int minZ, int maxX, int maxZ, ClientLevel level, LocalPlayer player) {
            int[][] colors = new int[maxX - minX][maxZ - minZ];
            BlockPos.MutableBlockPos pos1 = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos pos2 = new BlockPos.MutableBlockPos();
            for (int x = minX; x < maxX; x++) {
                pos1.setX(x);
                double depth = 0.0;
                for (int z = minZ - 1; z < maxZ; z++) {
                    double fluidDepth = 0.0;
                    pos1.setZ(z);
                    LevelChunk chunk = level.getChunk(SectionPos.blockToSectionCoord(x), SectionPos.blockToSectionCoord(z));
                    if (chunk.isEmpty()) continue;
                    MaterialColor color = MaterialColor.NONE;
                    double yDiff = 0.0;
                    int y = level.dimensionType().hasCeiling()
                            ? findBlockWithAirAbove(level, new BlockPos(x, player.getBlockY(), z))
                            : chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
                    BlockState state = null;
                    if (y > level.getMinBuildHeight() + 1) {
                        do {
                            pos1.setY(--y);
                            state = chunk.getBlockState(pos1);
                        } while (!shouldRender(state, level, pos1) && y > level.getMinBuildHeight());
                        if (y > level.getMinBuildHeight() && !state.getFluidState().isEmpty()) {
                            int y2 = y - 1;
                            pos2.set(pos1);
                            BlockState below;
                            do {
                                pos2.setY(y2--);
                                below = chunk.getBlockState(pos2);
                                fluidDepth++;
                            } while (y2 > level.getMinBuildHeight() && !below.getFluidState().isEmpty());
                            state = correctStateForFluidBlock(level, state, pos1);
                        }
                        yDiff += y;
                        color = state == null ? MaterialColor.NONE : state.getMapColor(level, pos1);
                    }
                    fluidDepth /= 2.5;
                    MaterialColor.Brightness brightness;
                    if (color == MaterialColor.WATER) {
                        double darkness = fluidDepth * 0.1 + ((x + z) & 1) * 0.2;
                        brightness = darkness < 0.5 ? MaterialColor.Brightness.LOW : darkness > 0.9 ? MaterialColor.Brightness.HIGH : MaterialColor.Brightness.NORMAL;
                    } else {
                        double darkness = (yDiff - depth) * 4.0 / 5.0 + (((x + z) & 1) - 0.5) * 0.4;
                        brightness = darkness > 0.6 ? MaterialColor.Brightness.HIGH : darkness < -0.6 ? MaterialColor.Brightness.LOW : MaterialColor.Brightness.NORMAL;
                    }
                    depth = yDiff;
                    if (z != minZ - 1) colors[x - minX][z - minZ] = tintShade(color, state, level, pos1, brightness);
                }
            }
            return colors;
        }

        private static BlockState correctStateForFluidBlock(Level level, BlockState state, BlockPos pos) {
            FluidState fluid = state.getFluidState();
            return !fluid.isEmpty() && !state.isFaceSturdy(level, pos, Direction.UP) ? fluid.createLegacyBlock() : state;
        }

        private static boolean shouldRender(BlockState state, Level level, BlockPos pos) {
            if (state.getMapColor(level, pos) == MaterialColor.NONE) return false;
            // 1.21 BlockState.canBeReplaced() is 1.19.2's material replaceable flag.
            return !state.getFluidState().isEmpty() || state.is(Blocks.SNOW) || !state.getMaterial().isReplaceable();
        }

        private static int tintShade(MaterialColor color, @Nullable BlockState state, Level level, BlockPos pos, MaterialColor.Brightness brightness) {
            if (color == MaterialColor.WATER || color == MaterialColor.GRASS || color == MaterialColor.PLANT) {
                int tint = BiomeColors.getAverageGrassColor(level, pos);
                if (color == MaterialColor.WATER) tint = BiomeColors.getAverageWaterColor(level, pos);
                if (color == MaterialColor.PLANT) tint = BiomeColors.getAverageFoliageColor(level, pos);
                int c = rgb2abgr(tint);
                if (color == MaterialColor.WATER) c = brighter(c);
                return shade(c, brightness);
            }
            if (state == null) return 0;
            int tint = Minecraft.getInstance().getBlockColors().getColor(state, level, pos, 0);
            if (tint == -1) return paletteColor(color.id, brightness);
            return shade(rgb2abgr(Minecraft.getInstance().getBlockColors().getColor(state, level, pos)), brightness);
        }

        private static int shade(int c, MaterialColor.Brightness brightness) {
            return switch (brightness) {
                case LOWEST -> c;
                case LOW -> darker(c);
                case NORMAL -> darker(darker(c));
                case HIGH -> darker(darker(darker(c)));
            };
        }

        private static int rgb2abgr(int rgb) {
            return rgb & 0xFF00FF00 | (rgb & 0xFF) << 16 | rgb >> 16 & 0xFF | 0xFF000000;
        }

        private static int darker(int abgr) {
            int r = abgr >> 16 & 0xFF, g = abgr >> 8 & 0xFF, b = abgr & 0xFF;
            return Math.max((int) (r * 0.7f), 0) << 16 | Math.max((int) (g * 0.7f), 0) << 8 | Math.max((int) (b * 0.7f), 0) | 0xFF000000;
        }

        private static int brighter(int abgr) {
            int r = abgr >> 16 & 0xFF, g = abgr >> 8 & 0xFF, b = abgr & 0xFF;
            if (r == 0 && g == 0 && b == 0) return 0xFF030303;
            if (r > 0 && r < 3) r = 3;
            if (g > 0 && g < 3) g = 3;
            if (b > 0 && b < 3) b = 3;
            return Math.min((int) (r * 1.1f), 255) << 16 | Math.min((int) (g * 1.1f), 255) << 8 | Math.min((int) (b * 1.1f), 255) | 0xFF000000;
        }

        private static int findBlockWithAirAbove(ClientLevel level, BlockPos pos) {
            int offset = 0;
            int y = pos.getY();
            BlockPos.MutableBlockPos m = pos.mutable();
            int attempts = 0;
            while (level.getBlockState(m).isAir() || !level.getBlockState(m.above()).isAir() && !level.getBlockState(m.below()).isAir()) {
                offset = offset <= 0 ? -offset + 1 : -offset;
                m.setY(offset + y);
                if (++attempts > 500) return y;
            }
            return m.getY();
        }
    }
}
