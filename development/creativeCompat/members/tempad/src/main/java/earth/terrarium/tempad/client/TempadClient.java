// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/client/TempadClient.kt (and the
// event wiring of ModKeybinds.kt). Translated to Java for Forge 1.19.2. Called from the Tempad constructor only on
// Dist.CLIENT. Registers the parts of the phases ported so far (TP-D7); the rest is listed in PORT_NOTES.md section 5.
// The projector's client extension (arm pose) is registered by RudimentaryTempadItem.initializeClient.
package earth.terrarium.tempad.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Either;
import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.locations.LocationGetter;
import earth.terrarium.tempad.api.tva_device.ChrononData;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.client.block.SpatialAnchorRenderer;
import earth.terrarium.tempad.client.block.WorkstationRenderer;
import earth.terrarium.tempad.client.entity.TimedoorRenderTypes;
import earth.terrarium.tempad.client.entity.TimedoorRenderer;
import earth.terrarium.tempad.client.screen.MetronomeScreen;
import earth.terrarium.tempad.client.screen.WalletScreen;
import earth.terrarium.tempad.client.screen.tempad.NewLocationScreen;
import earth.terrarium.tempad.client.screen.tempad.PortalSetupScreen;
import earth.terrarium.tempad.client.screen.tempad.SettingsScreen;
import earth.terrarium.tempad.client.screen.tempad.TeleportScreen;
import earth.terrarium.tempad.client.screen.tempad.TimelineScreen;
import earth.terrarium.tempad.client.tooltip.ChrononTooltip;
import earth.terrarium.tempad.client.tooltip.UpgradesTooltip;
import earth.terrarium.tempad.client.ui.Sprites;
import earth.terrarium.tempad.common.block.timedoor_marker.AbstractMarkerBe;
import earth.terrarium.tempad.common.data.InstalledUpgradesComponent;
import earth.terrarium.tempad.common.menu.AbstractTempadMenu;
import earth.terrarium.tempad.common.registries.ModBlocks;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModEntities;
import earth.terrarium.tempad.common.registries.ModItems;
import earth.terrarium.tempad.common.registries.ModMenus;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.IOException;

public final class TempadClient {
    private TempadClient() {
    }

    public static final BooleanItemPropertyFunction twisterAttachedProperty = (stack, level, entity, seed) -> ModComponents.isTwisterEquipped(stack);

    public static final BooleanItemPropertyFunction inUseProperty = (stack, level, entity, seed) -> {
        if (!(entity instanceof Player player)) return false;
        if (!(player.containerMenu instanceof AbstractTempadMenu<?> menu)) return false;
        return menu.ctx.getStack() == stack;
    };

    public static final ClampedItemPropertyFunction charge4Property = (stack, level, entity, seed) -> {
        ChrononHandler tank = ChrononHandler.of(stack);
        if (tank == null) return 0f;
        return step((float) tank.getPower() / tank.getMaxPower(), 0.25f);
    };

    public static final BooleanItemPropertyFunction enabledProperty = (stack, level, entity, seed) -> ModComponents.isEnabled(stack);

    public static final BooleanItemPropertyFunction screeningEnabled = (stack, level, entity, seed) -> ModComponents.getAccessId(stack) != null;

    public static final BooleanItemPropertyFunction writtenProperty = (stack, level, entity, seed) -> ModComponents.getPortalTarget(stack) != null;

    public static final BooleanItemPropertyFunction hasCardsProperty = (stack, level, entity, seed) -> {
        if (stack.getItem() == ModItems.cardWallet.get()) {
            for (ItemStack card : ModComponents.getWalletContents(stack)) {
                if (!card.isEmpty()) return true;
            }
        }
        return false;
    };

    public static final BlockColor blockColor = (state, level, pos, tintIndex) -> {
        if (level != null && pos != null) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AbstractMarkerBe marker) return marker.getColor().getValue();
        }
        return Tempad.ORANGE.getValue();
    };

    public static final ItemColor itemColor = (stack, tintIndex) -> {
        Color color = ModComponents.getColor(stack);
        return color != null ? color.getValue() : Tempad.ORANGE.getValue();
    };

    public static float step(float value, float step) {
        if (value == 0f) return 0f;
        if (value == 1f) return 1f;
        float current = step;
        while (value > current && current + step <= 1f) {
            current += step;
        }
        return current;
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(TempadClient::setup);
        modBus.addListener(TempadClient::registerRenderers);
        modBus.addListener(TempadClient::registerShaders);
        modBus.addListener(TempadClient::registerTooltip);
        modBus.addListener(TempadClient::registerReloadListeners);
        modBus.addListener(TempadClient::registerBlockColors);
        modBus.addListener(TempadClient::registerItemColors);
        modBus.addListener((RegisterKeyMappingsEvent event) -> ModKeybinds.registerKeyBinding(event));
        MinecraftForge.EVENT_BUS.addListener(TempadClient::appendTooltip);
        MinecraftForge.EVENT_BUS.addListener(TempadClient::onTooltipAdded);
        MinecraftForge.EVENT_BUS.addListener(ModKeybinds::onClientTick);
        MinecraftForge.EVENT_BUS.addListener(TempadClient::updateRainbow);
    }

    private static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.teleport.get(), TeleportScreen::new);
            MenuScreens.register(ModMenus.newLocation.get(), NewLocationScreen::new);
            MenuScreens.register(ModMenus.settings.get(), SettingsScreen::new);
            MenuScreens.register(ModMenus.timeline.get(), TimelineScreen::new);
            MenuScreens.register(ModMenus.portalSetup.get(), PortalSetupScreen::new);
            MenuScreens.register(ModMenus.metronome.get(), MetronomeScreen::new);
            MenuScreens.register(ModMenus.wallet.get(), WalletScreen::new);

            Item tempad = ModItems.tempad.get();
            ItemProperties.register(tempad, Tempad.id("in_use"), inUseProperty);
            ItemProperties.register(tempad, Tempad.id("attached"), twisterAttachedProperty);
            register(tempad, "charge", charge4Property);
            register(ModItems.chrononCell.get(), "charge", charge4Property);
            register(ModItems.chrononBattery.get(), "charge", charge4Property);
            register(ModItems.chronometer.get(), "charge", charge4Property);
            register(ModItems.chrononGenerator.get(), "charge", charge4Property);
            register(ModItems.locationBroadcaster.get(), "enabled", enabledProperty);
            register(ModItems.screeningDevice.get(), "enabled", screeningEnabled);
            register(ModItems.locationCard.get(), "written", writtenProperty);
            register(ModItems.timedoorProjector.get(), "has_card", writtenProperty);
            register(ModItems.cardWallet.get(), "full", hasCardsProperty);
        });
    }

    private static void register(Item item, String name, ItemPropertyFunction function) {
        ItemProperties.register(item, Tempad.id(name), function);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.timedoor.get(), TimedoorRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.timedoorMarkerBE.get(), ctx -> new SpatialAnchorRenderer<>(ctx.getBlockRenderDispatcher()));
        event.registerBlockEntityRenderer(ModBlocks.chronomarkBE.get(), ctx -> new SpatialAnchorRenderer<>(ctx.getBlockRenderDispatcher()));
        event.registerBlockEntityRenderer(ModBlocks.workstationBE.get(), ctx -> new WorkstationRenderer(ctx.getItemRenderer()));
    }

    /**
     * The rendertype_timedoor core shader from the upstream jar (served as is). Upstream builds the program with the
     * POSITION_COLOR format although its JSON and buffers carry Position, Color, UV0, UV2; 1.19.2 binds attribute
     * locations from the format given here, so the port passes the buffer's real format (POSITION_COLOR_TEX_LIGHTMAP).
     */
    private static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceManager(), new ResourceLocation("rendertype_timedoor"),
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), shader -> TimedoorRenderTypes.timedoorShader = shader);
        } catch (IOException e) {
            Tempad.LOGGER.error("Could not load the rendertype_timedoor shader", e);
        }
    }

    private static void registerTooltip(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(ChrononData.class, ChrononTooltip::new);
        event.register(InstalledUpgradesComponent.class, UpgradesTooltip::new);
    }

    private static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(blockColor, ModBlocks.timedoorMarker.get());
    }

    private static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(itemColor, ModItems.timedoorMarker.get());
        event.register(itemColor, ModItems.chronomark.get());
    }

    private static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> {
            Sprites.clear();
            TimedoorRenderTypes.clearTextures();
        });
    }

    private static void appendTooltip(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() == ModItems.tempad.get()) {
            InstalledUpgradesComponent upgrades = ModComponents.getInstalledUpgrades(stack);
            if (!upgrades.upgrades().isEmpty()) {
                event.getTooltipElements().add(Math.min(2, event.getTooltipElements().size()), Either.right(upgrades));
            }
        }
    }

    private static void onTooltipAdded(ItemTooltipEvent event) {
        LocationGetter target = ModComponents.getPortalTarget(event.getItemStack());
        if (target != null) target.addToTooltip(event.getToolTip()::add, Screen.hasShiftDown());
    }

    /**
     * Resourceful Lib animates its RAINBOW colour on the client; the port does it here, a full hue turn every 10 s
     * (200 client ticks).
     */
    private static float rainbowHue;

    private static void updateRainbow(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        rainbowHue = (rainbowHue + 0.005f) % 1f;
        Color.setRainbowValue(0xFF000000 | Mth.hsvToRgb(rainbowHue, 1f, 1f));
    }
}
