package com.SaintAlfonzo.creativelabels.client;

import com.SaintAlfonzo.creativelabels.CLRegistry;
import com.SaintAlfonzo.creativelabels.block.FacePaperBlockEntity;
import com.SaintAlfonzo.creativelabels.block.FaceContent;
import com.SaintAlfonzo.creativelabels.block.NoteData;
import com.SaintAlfonzo.creativelabels.block.Spots;
import net.minecraft.core.Direction;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-only registration (C4): only reached through DistExecutor, so nothing here loads on a dedicated server.
 * Phase 1: the geometry loader, Labels' paper model as an additional model, the donor art pack and atlas sprites
 * ({@link DonorArt}), the note shape cache reset on reload ({@link NoteShape}), the block colour handler and the
 * piece highlight ({@link FacePaperHighlight}). Phase 2: the note item colour handler and the
 * {@code creativelabels:look} item property ({@link NoteItemColours}). Phase 3: the label picture and name renderer
 * ({@link FacePaperRenderer}, registered on EntityRenderersEvent.RegisterRenderers).
 */
public final class ClientSetup {

    public static final int WHITE = 0xFFFFFF;

    public static void register(IEventBus modBus) {
        modBus.addListener((ModelEvent.RegisterGeometryLoaders e) ->
                e.register(FacePaperModelLoader.NAME, FacePaperModelLoader.INSTANCE));
        modBus.addListener((ModelEvent.RegisterAdditional e) -> e.register(FacePaperModelLoader.LABEL_MODEL));
        modBus.addListener(DonorArt::onStitch);
        modBus.addListener(DonorArt::onAddPackFinders);
        modBus.addListener((RegisterClientReloadListenersEvent e) ->
                e.registerReloadListener((ResourceManagerReloadListener) manager -> NoteShape.invalidate()));
        modBus.addListener((RegisterColorHandlersEvent.Block e) -> e.register((state, level, pos, tintIndex) -> {
            // Tint index = face ordinal * 5 + spot for a dyed note (BRIEF 4); plain notes use the untinted sprite.
            if (tintIndex < 0 || level == null || pos == null) return WHITE;
            BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof FacePaperBlockEntity paper)) return WHITE;
            int faceIndex = tintIndex / Spots.COUNT, spot = tintIndex % Spots.COUNT;
            if (faceIndex >= 6) return WHITE;
            FaceContent c = paper.face(Direction.values()[faceIndex]);
            NoteData n = c.note(spot);
            return n == null || n.color == NoteData.NO_COLOR ? WHITE : n.color;
        }, CLRegistry.FACE_PAPER.get()));
        modBus.addListener(NoteItemColours::onItemColours);
        modBus.addListener((FMLClientSetupEvent e) -> e.enqueueWork(NoteItemColours::registerProperties));
        // Phase 3: label pictures and names (Moonlight render-to-texture icons); notes stay in the chunk mesh.
        modBus.addListener((EntityRenderersEvent.RegisterRenderers e) ->
                e.registerBlockEntityRenderer(CLRegistry.FACE_PAPER_BE.get(), FacePaperRenderer::new));
        MinecraftForge.EVENT_BUS.addListener(FacePaperHighlight::onHighlight);
    }

    private ClientSetup() {}
}
