package com.SaintAlfonzo.creativedelight.hearth.gear;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * Sneak-use with a Tilling hoe. Goes through the hoe tool action, so modded soils work (B9 fixed), and end stone tills to
 * Ender Crop's Tilled End Stone with a netherite or better hoe (3.2; the H&H block is gone). One durability per block tilled. Even sizes reach one block toward the look direction, as Creative
 * Delight's hoe harvest area does.
 */
public final class TillingEvents {
    private static final ResourceLocation ENDERCROP_TILLED_END_STONE = new ResourceLocation("endercrop", "tilled_end_stone");
    private TillingEvents() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack hoe = event.getItemStack();
        if (!player.isShiftKeyDown() || !(hoe.getItem() instanceof HoeItem)) {
            return;
        }
        int level = EnchantmentHelper.getItemEnchantmentLevel(HHGearRegistry.TILLING.get(), hoe);
        if (level <= 0 || event.getFace() == Direction.DOWN) {
            return;
        }
        Level world = event.getLevel();
        BlockPos center = event.getPos();
        if (!canTill(world, center)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(world.isClientSide));
        if (world.isClientSide) {
            return;
        }
        int[] xs = span(level + 2, player.getLookAngle().x);
        int[] zs = span(level + 2, player.getLookAngle().z);
        boolean tilled = false;
        for (int x = xs[0]; x <= xs[1]; x++) {
            for (int z = zs[0]; z <= zs[1]; z++) {
                BlockPos pos = center.offset(x, 0, z);
                if (till(world, pos, player, hoe, event)) {
                    tilled = true;
                    hoe.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));
                    if (hoe.isEmpty()) {
                        break;
                    }
                }
            }
            if (hoe.isEmpty()) {
                break;
            }
        }
        if (tilled) {
            world.playSound(null, center, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private static int[] span(int size, double look) {
        int low = (size - 1) / 2;
        int high = size / 2;
        return look < 0 ? new int[]{-high, low} : new int[]{-low, high};
    }

    private static boolean canTill(Level world, BlockPos pos) {
        return world.getBlockState(pos.above()).isAir();
    }

    private static boolean till(Level world, BlockPos pos, Player player, ItemStack hoe, PlayerInteractEvent.RightClickBlock event) {
        if (!canTill(world, pos)) {
            return false;
        }
        BlockState state = world.getBlockState(pos);
        BlockState result;
        if (state.is(Blocks.END_STONE)) {
            // End stone (plan_bugfix_1114 3.2): Ender Crop's tilled end stone, netherite or better hoe only; nothing without Ender Crop
            Block tilled = endercropTilledEndStone();
            if (tilled == null || !netheriteOrBetter(hoe)) {
                return false;
            }
            result = tilled.defaultBlockState();
        } else {
            Vec3 hit = Vec3.atCenterOf(pos).add(0.0D, 0.5D, 0.0D);
            UseOnContext context = new UseOnContext(player, event.getHand(), new BlockHitResult(hit, Direction.UP, pos, false));
            result = state.getToolModifiedState(context, ToolActions.HOE_TILL, false);
        }
        if (result == null || result == state) {
            return false;
        }
        world.setBlock(pos, result, 11);
        return true;
    }

    /**
     * End stone tills only with a netherite or better hoe (owner, plan_bugfix_1114 3.2): a TieredItem whose tier sorts
     * at or above netherite in Forge's TierSortingRegistry, so modded tiers above netherite count.
     */
    public static boolean netheriteOrBetter(ItemStack stack) {
        if (!(stack.getItem() instanceof TieredItem tiered)) {
            return false;
        }
        Tier tier = tiered.getTier();
        if (tier == Tiers.NETHERITE) {
            return true;
        }
        List<Tier> sorted = TierSortingRegistry.getSortedTiers();
        int at = sorted.indexOf(tier);
        int netherite = sorted.indexOf(Tiers.NETHERITE);
        if (at < 0 || netherite < 0) {
            // a tier outside the sorting registry: by harvest level, as Forge does for unsorted tiers
            return tier.getLevel() >= Tiers.NETHERITE.getLevel();
        }
        return at > netherite;
    }

    @Nullable
    private static Block endercropTilledEndStone() {
        if (!ModList.get().isLoaded("endercrop")) {
            return null;
        }
        Block block = ForgeRegistries.BLOCKS.getValue(ENDERCROP_TILLED_END_STONE);
        return block == null || block == Blocks.AIR ? null : block;
    }

    /**
     * Ender Crop tills end stone itself in this event (its ForgeEventHandlers.onUseHoe, HOE_TILL, any hoe with Unbreaking
     * by its config). Canceled first, at HIGHEST, for a hoe below netherite: Ender Crop skips canceled events and the hoe
     * does nothing. Same gate as the Tilling enchantment's area till.
     */
    public static void onToolModification(BlockEvent.BlockToolModificationEvent event) {
        if (event.getToolAction() != ToolActions.HOE_TILL || !event.getState().is(Blocks.END_STONE)) {
            return;
        }
        ItemStack held = event.getHeldItemStack();
        if (netheriteOrBetter(held)) {
            return;
        }
        event.setCanceled(true);
        Player player = event.getPlayer();
        if (!event.isSimulated() && player != null && !player.level.isClientSide && held.getItem() instanceof HoeItem) {
            player.displayClientMessage(Component.translatable("message.hearthandharvest.end_stone_needs_netherite"), true);
        }
    }

    static int clampLevel(int level) {
        return Mth.clamp(level, 1, 3);
    }
}
