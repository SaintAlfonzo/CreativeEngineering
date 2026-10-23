package com.SaintAlfonzo.creativeengineering.food;

import com.SaintAlfonzo.creativeengineering.worldgen.WorldgenFiles;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Spice of Life for placed pies and cakes (TODO food/types-and-tuning #5). SoL only records the vanilla cake when it is
 * bitten from the block (as Farmer's Delight's cake slice); every other placed pie or cake fed the player without a
 * record. {@code mixin.ServerPlayerGameModePlacedFoodMixin} hands every server-side block use here with the state from
 * before it; a bite (same block with a new state, the block eaten away, or a candle cake turned into its cake) records
 * the slice item:
 * <ul>
 *   <li>Farmer's Delight {@code PieBlock} and its subclasses: the pie's own slice item.</li>
 *   <li>Cakes and puddings: kubejs/generation_scripts/food/placed_food_slices.json (block id -> item id), read at
 *       server start. Cakes without a slice item count as the cake slice, like the vanilla cake.</li>
 * </ul>
 * A knife cuts a slice instead of biting (pies, fig pudding), so a knife in hand records nothing; the slice counts when
 * it is eaten. Fake players (deployers) never eat.
 */
public final class PlacedFoodSol {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String FILE = "kubejs/generation_scripts/food/placed_food_slices.json";
    private static final TagKey<Item> KNIVES = ItemTags.create(new ResourceLocation("forge", "tools/knives"));

    private static Map<Block, Item> slices = Map.of();

    private PlacedFoodSol() {}

    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        slices = load();
    }

    private static Map<Block, Item> load() {
        Map<Block, Item> out = new HashMap<>();
        try {
            String text = WorldgenFiles.readText(FILE);
            if (text == null) {
                LOGGER.warn("PlacedFoodSol: {} is missing; only pies count", FILE);
                return out;
            }
            JsonObject blocks = JsonParser.parseString(text).getAsJsonObject().getAsJsonObject("blocks");
            for (var e : blocks.entrySet()) {
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(e.getKey()));
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(e.getValue().getAsString()));
                if (block == null || block == Blocks.AIR) continue; // mod not installed
                if (item == null || !item.isEdible()) {
                    LOGGER.warn("PlacedFoodSol: {} -> {} is not a food item, skipped", e.getKey(), e.getValue().getAsString());
                    continue;
                }
                out.put(block, item);
            }
        } catch (Exception ex) {
            LOGGER.error("PlacedFoodSol: could not read {}", FILE, ex);
        }
        return out;
    }

    /** After {@code ServerPlayerGameMode#useItemOn}; {@code before} is the state at {@code pos} before the use. */
    public static void afterUse(ServerPlayer player, Level level, BlockPos pos, BlockState before, ItemStack held,
                                InteractionResult result) {
        if (!result.consumesAction() || player instanceof FakePlayer || held.is(KNIVES)) return;
        BlockState after = level.getBlockState(pos);
        if (after == before) return;

        Block block = before.getBlock();
        Item slice;
        if (after.getBlock() == block || after.isAir()) {
            if (block == Blocks.CAKE) return; // SoL records this one itself
            slice = sliceFor(block);
        } else if (block instanceof AbstractCandleBlock) {
            slice = sliceFor(after.getBlock()); // candle cake bitten: it is now its cake with one bite
        } else {
            return;
        }
        if (slice != null && ModList.get().isLoaded("solapplepie")) {
            SolBridge.record(player, slice);
        }
    }

    private static Item sliceFor(Block block) {
        Item mapped = slices.get(block);
        if (mapped != null) return mapped;
        if (ModList.get().isLoaded("farmersdelight")) return FdPies.slice(block);
        return null;
    }

    /** Farmer's Delight classes stay out of the outer class, so it loads without the mod. */
    private static final class FdPies {
        static Item slice(Block block) {
            if (block instanceof vectorwing.farmersdelight.common.block.PieBlock pie) {
                ItemStack stack = pie.getPieSliceItem();
                return stack.isEmpty() ? null : stack.getItem();
            }
            return null;
        }
    }

    /** Spice of Life classes stay out of the outer class, so it loads without the mod. */
    private static final class SolBridge {
        static void record(ServerPlayer player, Item slice) {
            com.tarinoita.solsweetpotato.tracking.FoodTracker.updateFoodList(slice, player);
        }
    }
}
