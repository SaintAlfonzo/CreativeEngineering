package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.LegacyIds;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * StructureCheck#loadStructures (m_197311_) reads the structure starts of a chunk on disk without loading it (/locate,
 * explorer maps, "is this structure already here") and skips unknown ids; the old creativeengineering: structure ids
 * are renamed first, as in ChunkSerializerLegacyStructuresMixin (worldgen/LegacyIds).
 */
@Mixin(value = StructureCheck.class, remap = false)
public class StructureCheckLegacyStructuresMixin {

	@Shadow(remap = false) @Final private RegistryAccess f_197238_;

	@Inject(method = "m_197311_(Lnet/minecraft/nbt/CompoundTag;)Lit/unimi/dsi/fastutil/objects/Object2IntMap;",
			at = @At("HEAD"), require = 1, remap = false)
	private void creativeworldgen$legacyStarts(CompoundTag chunk, CallbackInfoReturnable<Object2IntMap<Structure>> cir) {
		if (chunk != null && chunk.contains("structures", Tag.TAG_COMPOUND)) {
			LegacyIds.upgradeStructures(chunk.getCompound("structures"), f_197238_.registryOrThrow(Registry.STRUCTURE_REGISTRY));
		}
	}
}
