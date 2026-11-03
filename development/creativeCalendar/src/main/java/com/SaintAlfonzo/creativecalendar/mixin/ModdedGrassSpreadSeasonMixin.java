package com.SaintAlfonzo.creativecalendar.mixin;

import com.SaintAlfonzo.creativecalendar.seasons.CropSeasons;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.loading.LoadingModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Crop seasons for modded grass blocks whose spread does not run through {@link GrassSpreadSeasonMixin} (farming/crops
 * #28, feature_writeup/crops.md 2.12.1). Each target's random tick spreads only after a light check on the block above;
 * the hook sits on that check, so a typed block whose season is off skips the spread with {@code offPenalty}'s chance,
 * and whatever the tick does before it (covered grass turning back to dirt, IW path wear recovering) is untouched.
 * <ul>
 *   <li>Immersive Weathering {@code ModGrassBlock}, which IW registers as {@code minecraft:grass_block} and extends as
 *   {@code rooted_grass_block}: after the vanilla spread (already rolled once in {@link GrassSpreadSeasonMixin}) it
 *   spreads over mycelium and rooted dirt. That second spread gets its own roll, so each kind of spread is slowed by
 *   the same share.</li>
 *   <li>Botania's six coloured grasses ({@code BotaniaGrassBlock}, random tick runs {@code tick}).</li>
 *   <li>Vinery grass slab ({@code SpreadableGrassSlabBlock}) and Vintage Delight lush grass block
 *   ({@code LushGrassBlock}): own random ticks that never call the vanilla one.</li>
 * </ul>
 * Pseudo, several targets: any of these mods may be missing; the mixin config gate needs one of them.
 */
@Pseudo
@Mixin(targets = {
        "com.ordana.immersive_weathering.blocks.soil.ModGrassBlock",
        "vazkii.botania.common.block.BotaniaGrassBlock",
        "net.satisfy.vinery.core.block.SpreadableGrassSlabBlock",
        "net.ribs.vintagedelight.block.custom.LushGrassBlock"
}, remap = false)
public abstract class ModdedGrassSpreadSeasonMixin {

    /** Crop seasons read Serene Seasons; the mixin config gate only checks the grass mods. LoadingModList, not
     *  ModList: this initializer joins the target's static init, and IW builds ModGrassBlock in vanilla bootstrap,
     *  before ModList exists (boot crash 2026-11-02, r1102b). */
    @Unique private static final boolean creativeengineering$seasons = LoadingModList.get().getModFileById("sereneseasons") != null;

    // m_213898_ = randomTick, m_213897_ = tick (Botania's grass spreads there; the default randomTick calls it);
    // m_46803_ = Level.getMaxLocalRawBrightness(BlockPos), the light check that opens every target's spread
    @Inject(method = {"m_213898_", "m_213897_"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;m_46803_(Lnet/minecraft/core/BlockPos;)I",
                    ordinal = 0),
            cancellable = true, remap = false, require = 0)
    private void creativeengineering$moddedGrassSeason(BlockState state, ServerLevel level, BlockPos pos,
                                                       RandomSource random, CallbackInfo ci) {
        if (!creativeengineering$seasons) return;
        if (CropSeasons.refuseSpread(level, pos, CropSeasons.typeOf(state))) ci.cancel();
    }
}
