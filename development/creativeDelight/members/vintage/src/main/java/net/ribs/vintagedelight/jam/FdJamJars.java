// Pack addition (CreativeEngineering2, TODO dishes/jams #1). Own work, GPLv3.
package net.ribs.vintagedelight.jam;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.registries.ForgeRegistries;
import net.ribs.vintagedelight.block.custom.MasonJarBlock;

/**
 * Fruits Delight's jams are mason jars (dishes/jams #1): placed, they are a Vintage mason jar block of that jam
 * (vintagedelight:fd_<fruit>_jam_jar, 1 to 4 per block, its interior texture) instead of Fruits Delight's bottle
 * block. Same use as a Vintage jar: on a block of the same jam it adds one; sneaking places a new block; otherwise
 * the jam is eaten as before. The block gives the Fruits Delight jam back (loot table, empty-hand take, pick block).
 */
public final class FdJamJars {
    private FdJamJars() {}

    private static Map<Item, MasonJarBlock> byItem;

    static void init(boolean fd) {
        if (fd) MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, FdJamJars::onUse);
    }

    private static Map<Item, MasonJarBlock> map() {
        if (byItem == null) {
            Map<Item, MasonJarBlock> m = new HashMap<>();
            PackJams.FD_JAR_BLOCKS.forEach((fruit, block) -> {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("fruitsdelight", fruit + "_jelly"));
                if (item != null) m.put(item, (MasonJarBlock) block.get());
            });
            byItem = m;
        }
        return byItem;
    }

    private static void onUse(PlayerInteractEvent.RightClickBlock e) {
        ItemStack stack = e.getItemStack();
        MasonJarBlock jar = map().get(stack.getItem());
        if (jar == null) return;
        Player player = e.getEntity();
        Level level = e.getLevel();
        BlockPos pos = e.getPos();
        BlockState state = level.getBlockState(pos);
        boolean sameJar = state.is(jar) && state.getValue(MasonJarBlock.JARS) < MasonJarBlock.MAX_JARS;
        if (!sameJar && !player.isShiftKeyDown()) return;   // eaten as before

        if (sameJar) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(MasonJarBlock.JARS, state.getValue(MasonJarBlock.JARS) + 1), 3);
                level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) stack.shrink(1);
            }
        } else {
            BlockPlaceContext ctx = new BlockPlaceContext(new UseOnContext(player, e.getHand(), e.getHitVec()));
            BlockPos at = ctx.getClickedPos();
            if (!ctx.canPlace() || !player.mayUseItemAt(at, ctx.getClickedFace(), stack)) return;
            BlockState place = jar.getStateForPlacement(ctx);
            if (place == null || !place.canSurvive(level, at)) return;
            if (!level.isClientSide) {
                place = place.setValue(MasonJarBlock.WATERLOGGED, level.getFluidState(at).getType() == Fluids.WATER);
                level.setBlock(at, place, Block.UPDATE_ALL);
                level.playSound(null, at, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.getAbilities().instabuild) stack.shrink(1);
            }
        }
        e.setCanceled(true);
        e.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
    }
}
