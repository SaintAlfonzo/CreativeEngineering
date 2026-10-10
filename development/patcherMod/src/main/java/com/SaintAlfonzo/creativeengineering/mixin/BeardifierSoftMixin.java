package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.TerrainAdaptation;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Beardifier;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The soft beard (see worldgen/TerrainAdaptation). Checked against the Forge-patched SRG bytecode:
 * <ul>
 *   <li>m_223930_ is the per-start lambda of forStructuresInChunk (static; ChunkPos, rigid list, chunk min x/z,
 *       junction list, StructureStart). HEAD/RETURN mark the start being gathered; its three {@code new Rigid}
 *       (Forge's PieceBeardifierModifier branch, pool pieces, other pieces) go through TerrainAdaptation#rigid.</li>
 *   <li>m_207386_ is compute; its density sum is local slot 5 (a double), and the first
 *       ObjectListIterator#back(int) call rewinds the rigid iterator right after the rigid loop.</li>
 * </ul>
 * Nothing is stored on the Beardifier itself: YUNG's API swaps it for a new one built from these iterators.
 */
@Mixin(value = Beardifier.class, remap = false)
public class BeardifierSoftMixin {

	private static final String GATHER = "m_223930_(Lnet/minecraft/world/level/ChunkPos;Lit/unimi/dsi/fastutil/objects/ObjectList;IILit/unimi/dsi/fastutil/objects/ObjectList;Lnet/minecraft/world/level/levelgen/structure/StructureStart;)V";

	@Shadow @Final protected ObjectListIterator<Beardifier.Rigid> f_158065_;   // pieceIterator (protected in the Forge-patched class)

	@Inject(method = GATHER, at = @At("HEAD"), require = 1)
	private static void creativeengineering$beginGathering(ChunkPos chunk, ObjectList<?> rigids, int minX, int minZ, ObjectList<?> junctions,
			StructureStart start, CallbackInfo ci) {
		TerrainAdaptation.beginGathering(start);
	}

	@Inject(method = GATHER, at = @At("RETURN"), require = 1)
	private static void creativeengineering$endGathering(ChunkPos chunk, ObjectList<?> rigids, int minX, int minZ, ObjectList<?> junctions,
			StructureStart start, CallbackInfo ci) {
		TerrainAdaptation.endGathering();
	}

	@Redirect(method = GATHER, at = @At(value = "NEW",
			target = "(Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/world/level/levelgen/structure/TerrainAdjustment;I)Lnet/minecraft/world/level/levelgen/Beardifier$Rigid;"),
			require = 1)
	private static Beardifier.Rigid creativeengineering$softRigid(BoundingBox box, TerrainAdjustment adjustment, int groundLevelDelta) {
		return TerrainAdaptation.rigid(box, adjustment, groundLevelDelta);
	}

	@ModifyVariable(method = "m_207386_(Lnet/minecraft/world/level/levelgen/DensityFunction$FunctionContext;)D",
			at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/ObjectListIterator;back(I)I", ordinal = 0),
			index = 5, require = 1)
	private double creativeengineering$addSoft(double density, DensityFunction.FunctionContext context) {
		return TerrainAdaptation.addSoft(density, f_158065_, context.blockX(), context.blockY(), context.blockZ());
	}
}
