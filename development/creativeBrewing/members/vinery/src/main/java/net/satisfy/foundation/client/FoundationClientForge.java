package net.satisfy.foundation.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.satisfy.foundation.Foundation;
import net.satisfy.foundation.armor.ArmorSet;
import net.satisfy.foundation.armor.ArmorSets;
import net.satisfy.foundation.armor.Wearing;
import net.satisfy.foundation.banner.CompletionistBannerRenderer;
import net.satisfy.foundation.particle.ColoredDripParticle;
import net.satisfy.foundation.particle.DriftingParticle;
import net.satisfy.foundation.particle.DyeSplashParticle;
import net.satisfy.foundation.rarity.FoundationRarities;
import net.satisfy.foundation.rarity.FoundationRarity;
import net.satisfy.foundation.registry.FoundationEntities;
import net.satisfy.foundation.registry.FoundationParticles;
import net.satisfy.foundation.seat.ChairRenderer;

import java.util.List;

/** Client side of the lib: particles, the seat renderer, the banner layer, rarity names and armor set tooltips. */
@Mod.EventBusSubscriber(modid = Foundation.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class FoundationClientForge {
    private FoundationClientForge() {
    }

    @SubscribeEvent
    public static void onParticles(RegisterParticleProvidersEvent event) {
        event.register(FoundationParticles.DYE_SPLASH.get(), DyeSplashParticle.Provider::new);
        event.register(FoundationParticles.COLORED_DRIP.get(), ColoredDripParticle.Provider::new);
        event.register(FoundationParticles.LEAF.get(), sprites -> new DriftingParticle.Provider(sprites, DriftingParticle.Style.LEAF, DriftingParticle.MotionProfile.LEAF));
    }

    @SubscribeEvent
    public static void onLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(CompletionistBannerRenderer.LAYER_LOCATION, CompletionistBannerRenderer::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(FoundationEntities.CHAIR.get(), ChairRenderer::new);
    }

    /** Upstream's ClientTooltipEvent hooks: rarity colours the name, armor set pieces list the set. */
    @Mod.EventBusSubscriber(modid = Foundation.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static final class Tooltips {
        private Tooltips() {
        }

        @SubscribeEvent
        public static void onTooltip(ItemTooltipEvent event) {
            FoundationClientForge.onTooltip(event);
        }
    }

    private static void onTooltip(ItemTooltipEvent event) {
        List<Component> lines = event.getToolTip();
        FoundationRarity rarity = FoundationRarities.get(event.getItemStack());
        if (rarity != null && !lines.isEmpty()) {
            lines.set(0, rarity.apply(lines.get(0)));
        }
        ArmorSets.find(event.getItemStack().getItem()).filter(ArmorSet::isTooltipVisible).ifPresent(set -> appendSet(set, lines));
    }

    private static void appendSet(ArmorSet set, List<Component> tooltip) {
        Player player = Minecraft.getInstance().player;
        List<List<Item>> pieces = set.pieces();
        int worn = player == null ? 0 : set.wornPieces(player);
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable(set.nameKey()).append(" (" + worn + "/" + pieces.size() + ")").withStyle(ChatFormatting.AQUA));
        for (List<Item> piece : pieces) {
            boolean pieceWorn = player != null && Wearing.isWearingAny(player, piece);
            tooltip.add(Component.literal("- [").append(name(piece)).append("]").withStyle(pieceWorn ? ChatFormatting.GREEN : ChatFormatting.GRAY));
        }
        if (set.bonusKey() != null) {
            boolean active = player != null && set.isBonusActive(player);
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tooltip.foundation.set_bonus").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable(set.bonusKey()).withStyle(active ? ChatFormatting.DARK_GREEN : ChatFormatting.GRAY));
        }
    }

    private static Component name(List<Item> piece) {
        MutableComponent name = Component.empty();
        for (int i = 0; i < piece.size(); i++) {
            if (i > 0) name.append(" / ");
            name.append(piece.get(i).getDescription());
        }
        return name;
    }
}
