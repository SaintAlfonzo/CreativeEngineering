package com.SaintAlfonzo.creativeworldgen.mixin;

import com.SaintAlfonzo.creativeworldgen.worldgen.DimensionMove;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * D-DIM folder move (worldgen/DimensionMove): at the end of the LevelStorageAccess constructor, which every world open
 * goes through (singleplayer load, dedicated server start, the edit-world screen) after taking the session lock and
 * before level.dat, player files or any level is read.
 */
@Mixin(value = LevelStorageSource.LevelStorageAccess.class, remap = false)
public class LevelStorageAccessDimensionMoveMixin {

	@Inject(method = "<init>", at = @At("TAIL"), require = 1, remap = false)
	private void creativeworldgen$moveDimensionFolders(CallbackInfo ci) {
		DimensionMove.moveFolders(((LevelStorageSource.LevelStorageAccess) (Object) this).getLevelPath(LevelResource.ROOT));
	}
}
