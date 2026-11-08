// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/compat/JadePlugin.kt (registerClient and the six
// component providers, ChrononElement). Translated to Java for Forge 1.19.2 as a WTHIT client plugin (the pack has WTHIT,
// not Jade): Jade tooltip lines -> WTHIT body lines; Jade's ProgressElement -> WTHIT's BarComponent (same fraction and
// text, WTHIT's bar art); ChrononElement -> an ITooltipComponent drawing the same energy bar. Every provider reads the
// client-side block entity / entity, as upstream's (no server data), and is switched by its WthitPlugin toggle.
package earth.terrarium.tempad.client.compat;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.locations.DirectLocation;
import earth.terrarium.tempad.api.locations.IndirectLocation;
import earth.terrarium.tempad.api.locations.LocationGetter;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.client.TempadUI;
import earth.terrarium.tempad.client.ui.Gfx;
import earth.terrarium.tempad.common.block.MetronomeBe;
import earth.terrarium.tempad.common.block.MetronomeBlock;
import earth.terrarium.tempad.common.block.RudimentaryTempadBE;
import earth.terrarium.tempad.common.block.RudimentaryTempadBlock;
import earth.terrarium.tempad.common.block.WorkstationBE;
import earth.terrarium.tempad.common.block.WorkstationBlock;
import earth.terrarium.tempad.common.block.timedoor_marker.AbstractMarkerBe;
import earth.terrarium.tempad.common.block.timedoor_marker.AbstractMarkerBlock;
import earth.terrarium.tempad.common.compat.WthitPlugin;
import earth.terrarium.tempad.common.entity.TimedoorEntity;
import earth.terrarium.tempad.common.registries.ModComponents;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IClientRegistrar;
import mcp.mobius.waila.api.IEntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.ITooltipComponent;
import mcp.mobius.waila.api.IWailaClientPlugin;
import mcp.mobius.waila.api.component.BarComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class WthitClientPlugin implements IWailaClientPlugin {
    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(TIMEDOOR, TimedoorEntity.class);
        registrar.body(CHRONON, Block.class);
        registrar.body(ANCHOR, AbstractMarkerBlock.class);
        registrar.body(RUDIMENTARY, RudimentaryTempadBlock.class);
        registrar.body(METRONOME, MetronomeBlock.class);
        registrar.body(WORKSTATION, WorkstationBlock.class);
    }

    private static Component createdBy(GameProfile owner) {
        return Component.translatable("item.tempad.location_card.created_by", Component.literal(owner.getName()).withStyle(ChatFormatting.AQUA))
                .withStyle(ChatFormatting.GRAY);
    }

    private static Component shiftKeyInfo() {
        return Component.translatable("misc.tempad.shift_key_info",
                        Component.keybind(Minecraft.getInstance().options.keyShift.getName()).withStyle(ChatFormatting.GRAY))
                .withStyle(ChatFormatting.DARK_GRAY);
    }

    /** Upstream TimedoorComponentProvider. */
    public static final IEntityComponentProvider TIMEDOOR = new IEntityComponentProvider() {
        @Override
        public void appendBody(ITooltip tooltip, IEntityAccessor accessor, IPluginConfig config) {
            if (!config.getBoolean(WthitPlugin.TIMEDOOR)) return;
            if (!(accessor.getEntity() instanceof TimedoorEntity timedoorEntity)) return;
            if (timedoorEntity.getClosingTime() - TimedoorEntity.ANIMATION_LENGTH > 0) {
                tooltip.addLine(Component.translatable("jade.tempad.will_close", Math.round(timedoorEntity.getClosingTime() / 20f)));
            } else if (timedoorEntity.getClosingTime() != -1) {
                tooltip.addLine(Component.translatable("jade.tempad.closing"));
            }
            Vec3 target = timedoorEntity.getTargetPos();
            var dimension = timedoorEntity.getTargetDimension().location();
            tooltip.addLine(Component.translatable("jade.tempad.pos",
                    Component.translatable("dimension." + dimension.getNamespace() + "." + dimension.getPath()),
                    (int) target.x() + ", " + (int) target.y() + ", " + (int) target.z()));
        }
    };

    /** Upstream AnchorComponentProvider. */
    public static final IBlockComponentProvider ANCHOR = new IBlockComponentProvider() {
        @Override
        public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            if (!config.getBoolean(WthitPlugin.ANCHOR)) return;
            if (!(accessor.getBlockEntity() instanceof AbstractMarkerBe marker)) return;
            if (marker.owner != null) tooltip.addLine(createdBy(marker.owner));
            if (marker.id != null) {
                if (accessor.getPlayer().isShiftKeyDown()) {
                    tooltip.addLine(Component.translatable("item.tempad.location_card.id", marker.id.toString()).withStyle(ChatFormatting.DARK_GRAY));
                } else {
                    tooltip.addLine(shiftKeyInfo());
                }
            }
        }
    };

    /** Upstream RudimentaryTempadComponentProvider. */
    public static final IBlockComponentProvider RUDIMENTARY = new IBlockComponentProvider() {
        @Override
        public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            if (!config.getBoolean(WthitPlugin.RUDIMENTARY)) return;
            if (!(accessor.getBlockEntity() instanceof RudimentaryTempadBE projector)) return;
            LocationGetter pos = projector.portalTarget;
            if (pos instanceof DirectLocation direct) {
                NamedGlobalVec3 location = direct.location();
                tooltip.addLine(MutableComponent.create(location.name().getContents()).withStyle(style -> style.withColor(Tempad.ORANGE.getValue())));
                tooltip.addLine(location.dimensionText().withStyle(ChatFormatting.GRAY));
                tooltip.addLine(Component.literal("X: " + location.x()).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.addLine(Component.literal("Y: " + location.y()).withStyle(ChatFormatting.DARK_GRAY));
                tooltip.addLine(Component.literal("Z: " + location.z()).withStyle(ChatFormatting.DARK_GRAY));
            } else if (pos instanceof IndirectLocation indirect) {
                tooltip.addLine(indirect.info());
                tooltip.addLine(createdBy(indirect.accessor()));
                if (!accessor.getPlayer().isShiftKeyDown()) {
                    tooltip.addLine(shiftKeyInfo());
                } else {
                    tooltip.addLine(Component.translatable("item.tempad.location_card.id", indirect.id().toString()).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        }
    };

    /** Upstream MetronomeComponentProvider. */
    public static final IBlockComponentProvider METRONOME = new IBlockComponentProvider() {
        @Override
        public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            if (!config.getBoolean(WthitPlugin.METRONOME)) return;
            if (!(accessor.getBlockEntity() instanceof MetronomeBe blockEntity)) return;
            if (blockEntity.bootTime > 0) {
                float progress = 1 - (blockEntity.bootTime / 100f);
                tooltip.addLine(new BarComponent(progress, 0xFF000000 | Tempad.ORANGE.getValue(),
                        Component.translatable("block.tempad.metronome.booting", (int) (progress * 100)).append("%")));
            }
            if (blockEntity.owner != null) tooltip.addLine(createdBy(blockEntity.owner));
        }
    };

    /** Upstream WorkstationComponentProvider. */
    public static final IBlockComponentProvider WORKSTATION = new IBlockComponentProvider() {
        @Override
        public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            if (!config.getBoolean(WthitPlugin.WORKSTATION)) return;
            if (!(accessor.getBlockEntity() instanceof WorkstationBE blockEntity)) return;
            if (blockEntity.maxDownloadTime > 0) {
                float progress = 1f - (blockEntity.downloadTime / (float) blockEntity.maxDownloadTime);
                tooltip.addLine(new BarComponent(progress, 0xFF000000 | Gfx.DARK_GRAY,
                        Component.translatable("block.tempad.workstation.installing", (int) (progress * 100)).append("%")));
            }
            GameProfile owner = ModComponents.getOwner(blockEntity.inventory.getStackInSlot(0));
            if (owner != null) tooltip.addLine(createdBy(owner));
        }
    };

    /** Upstream ChrononComponentProvider (registered for every block). */
    public static final IBlockComponentProvider CHRONON = new IBlockComponentProvider() {
        @Override
        public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
            if (!config.getBoolean(WthitPlugin.CHRONON)) return;
            BlockEntity blockEntity = accessor.getBlockEntity();
            if (blockEntity == null) return;
            ChrononHandler chronons = ChrononHandler.of(blockEntity);
            if (chronons != null) tooltip.addLine(new ChrononElement(chronons));
        }
    };

    /** Upstream ChrononElement: the 74 x 13 energy bar of the Tempad screens. */
    public record ChrononElement(ChrononHandler chronons) implements ITooltipComponent {
        @Override
        public int getWidth() {
            return 74;
        }

        @Override
        public int getHeight() {
            return 13;
        }

        @Override
        public void render(PoseStack pose, int x, int y, float delta) {
            TempadUI.renderEnergyBar(pose, Minecraft.getInstance().font, x, y, chronons.getPower(), chronons.getMaxPower());
        }
    }
}
