package com.SaintAlfonzo.creativecommon.mixin;

import com.SaintAlfonzo.creativecommon.swap.SwapReport;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Block half of {@link SwapReport}: checks a chunk's palettes as {@code ChunkSerializer.read} (m_188230_) starts. */
@Mixin(ChunkSerializer.class)
public abstract class SwapReportChunkMixin {

	@Inject(method = "m_188230_", at = @At("HEAD"), remap = false)
	private static void creativecommon$reportUnresolved(ServerLevel level, PoiManager poi, ChunkPos pos, CompoundTag tag,
			CallbackInfoReturnable<ProtoChunk> cir) {
		SwapReport.noteChunk(tag);
	}
}
