package com.SaintAlfonzo.creativecompat.mixin;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Supplementaries' "dye blocks" tweak ({@code [tweaks.dye_blocks]}) swaps any block in a Moonlight
 * 16-colour set for its dyed sibling with {@code setBlockAndUpdate}, without carrying the block entity
 * over (its soap twin does). So dyeing a Supp Squared sack deleted its contents, shulker boxes and
 * presents would too, banners lose their patterns, a bed changes one half, and Farmer's Delight canvas
 * signs (a colour set) turned into another colour and lost their text before vanilla's sign code could
 * dye the text.
 *
 * <p>Here the tweak passes on every block with a block entity and on {@code #creativecompat:no_world_dye},
 * so the click falls through to the block's own use (a canvas sign dyes its text, like any sign). Plain
 * blocks (wool, concrete, terracotta, glass, candles ...) still dye in world. Crafting-grid dye and soap
 * recipes are untouched. Which blocks should dye or soap in world is open: [TODO] {@code blocks/dyeing #1}.
 */
@Mixin(targets = "net.mehvahdjukaar.supplementaries.common.events.overrides.DyeBehavior", remap = false)
public abstract class SupplementariesDyeBlocksMixin {

    private static final TagKey<Block> NO_WORLD_DYE = TagKey.create(Registry.BLOCK_REGISTRY,
            new ResourceLocation("creativecompat", "no_world_dye"));

    @Inject(method = "tryPerformingAction", at = @At("HEAD"), cancellable = true, remap = false)
    private void creativeengineering$skipDataBlocks(Level level, Player player, InteractionHand hand, ItemStack stack,
                                                    BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        BlockState state = level.getBlockState(hit.getBlockPos());
        if (state.hasBlockEntity() || state.is(NO_WORLD_DYE)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
