package com.SaintAlfonzo.creativeengineering.mixin;

import com.SaintAlfonzo.creativeengineering.worldgen.WorldgenConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.quark.content.world.undergroundstyle.PermafrostStyle;
import vazkii.quark.content.world.undergroundstyle.base.BasicUndergroundStyle;
import vazkii.quark.content.world.undergroundstyle.base.UndergroundStyleGenerator;

/**
 * One permafrost in the world: Quark's frozen peaks cave style ({@code PermafrostStyle}) lays Immersive Weathering's
 * {@code immersive_weathering:permafrost} where it laid {@code quark:permafrost} (renamed Froststone, crafted only).
 * Quark sets the style's floor and ceiling in {@code PermafrostModule.register}, during block registration, when
 * IW's block may not exist yet, so the swap happens at the first fill instead. {@code PermafrostStyle.fillFloor}
 * calls this class's {@code fillFloor} before growing its pillars, so the pillars follow too. Walls stay packed ice.
 * Gated by {@code [worldgen.systems] permafrostMerge}; off puts Quark's block back.
 */
@Mixin(value = BasicUndergroundStyle.class, remap = false)
public abstract class QuarkPermafrostStyleMixin {

    @Shadow(remap = false) public BlockState floorState;
    @Shadow(remap = false) public BlockState ceilingState;

    @Unique private static BlockState creativeengineering$iwPermafrost;
    @Unique private BlockState creativeengineering$quarkFloor;
    @Unique private BlockState creativeengineering$quarkCeiling;

    @Inject(method = "fillFloor", at = @At("HEAD"), remap = false)
    private void creativeengineering$floor(UndergroundStyleGenerator.Context ctx, BlockPos pos, BlockState state, CallbackInfo ci) {
        creativeengineering$swap();
    }

    @Inject(method = "fillCeiling", at = @At("HEAD"), remap = false)
    private void creativeengineering$ceiling(UndergroundStyleGenerator.Context ctx, BlockPos pos, BlockState state, CallbackInfo ci) {
        creativeengineering$swap();
    }

    @Unique
    private void creativeengineering$swap() {
        if (!((Object) this instanceof PermafrostStyle)) return;
        if (creativeengineering$quarkFloor == null) {
            creativeengineering$quarkFloor = floorState;
            creativeengineering$quarkCeiling = ceilingState;
        }
        if (!WorldgenConfig.permafrostMergeEnabled()) {
            floorState = creativeengineering$quarkFloor;
            ceilingState = creativeengineering$quarkCeiling;
            return;
        }
        if (creativeengineering$iwPermafrost == null) {
            Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("immersive_weathering", "permafrost"));
            if (b == null || b == Blocks.AIR) return;
            creativeengineering$iwPermafrost = b.defaultBlockState();
        }
        floorState = creativeengineering$iwPermafrost;
        ceilingState = creativeengineering$iwPermafrost;
    }
}
