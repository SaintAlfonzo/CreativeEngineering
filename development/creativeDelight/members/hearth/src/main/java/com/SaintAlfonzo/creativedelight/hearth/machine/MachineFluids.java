package com.SaintAlfonzo.creativedelight.hearth.machine;

import com.SaintAlfonzo.creativedelight.hearth.HearthAndHarvestMod;
import com.SaintAlfonzo.creativedelight.hearth.fluid.HHFluids;
import java.util.IdentityHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Right-clicking a machine's tank with a fluid container: the 1.4.1 fluid helper (buckets through their handlers,
 * bottles through a fluid-to-bottle table). All bottles count 250 mB here; the port's wines are plain single-dose drinks.
 */
public final class MachineFluids {
    public static final int BOTTLE = 250;
    private static Map<Fluid, Item> fluidToBottle;
    private static Map<Item, Fluid> bottleToFluid;

    private MachineFluids() {
    }

    private static void build() {
        if (fluidToBottle != null) return;
        Map<Fluid, Item> toBottle = new IdentityHashMap<>();
        HHFluids.ALL.forEach((name, entry) -> {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(HearthAndHarvestMod.MODID, name));
            if (item != null && item != Items.AIR) toBottle.put(entry.source.get(), item);
        });
        put(toBottle, new ResourceLocation(HearthAndHarvestMod.MODID, "goat_milk"), new ResourceLocation(HearthAndHarvestMod.MODID, "goat_milk_bottle"));
        put(toBottle, new ResourceLocation(HearthAndHarvestMod.MODID, "brine"), new ResourceLocation(HearthAndHarvestMod.MODID, "brine_bottle"));
        // The pack's survivors of 1.4.1's melon juice and cooking oil fluids (3.2 Stomping basin)
        put(toBottle, new ResourceLocation("create_central_kitchen", "melon_juice"), new ResourceLocation("farmersdelight", "melon_juice"));
        put(toBottle, new ResourceLocation("createaddition", "seed_oil"), new ResourceLocation("rusticdelight", "cooking_oil"));
        Map<Item, Fluid> toFluid = new IdentityHashMap<>();
        toBottle.forEach((fluid, item) -> toFluid.put(item, fluid));
        fluidToBottle = toBottle;
        bottleToFluid = toFluid;
    }

    private static void put(Map<Fluid, Item> map, ResourceLocation fluidId, ResourceLocation itemId) {
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidId);
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (fluid != null && fluid != Fluids.EMPTY && item != null && item != Items.AIR) map.put(fluid, item);
    }

    @Nullable
    public static Item bottleFor(Fluid fluid) {
        build();
        Fluid source = fluid instanceof FlowingFluid flowing ? flowing.getSource() : fluid;
        return fluidToBottle.get(source);
    }

    @Nullable
    public static Fluid fluidForBottle(Item item) {
        build();
        return bottleToFluid.get(item);
    }

    public static boolean isWaterBottle(ItemStack stack) {
        return stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER;
    }

    public static ItemStack waterBottle() {
        return PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
    }

    public static int comparator(@Nullable IFluidHandler tank) {
        if (tank == null || tank.getTanks() == 0) return 0;
        int amount = tank.getFluidInTank(0).getAmount();
        int capacity = tank.getTankCapacity(0);
        if (amount <= 0 || capacity <= 0) return 0;
        return Mth.floor((float) amount / (float) capacity * 14.0F) + 1;
    }

    /** Empties or fills the held container against the tank; returns true if anything happened. Server only. */
    public static boolean useOnTank(Level level, BlockPos pos, Player player, InteractionHand hand, IFluidHandler tank) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty() || tank == null || tank.getTanks() == 0) return false;
        FluidStack before = tank.getFluidInTank(0);
        FluidStack incoming = emptied(held, tank);
        if (!incoming.isEmpty()) {
            ItemStack result = container(held, tank, true);
            tank.fill(incoming, IFluidHandler.FluidAction.EXECUTE);
            sound(level, pos, incoming.getFluid().getFluidType(), held.is(Items.GLASS_BOTTLE) || bottleOrWater(held), false);
            give(player, hand, held, result);
            return true;
        }
        int amount = filled(held, tank);
        if (amount > 0) {
            ItemStack result = container(held, tank, false);
            tank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
            sound(level, pos, before.getFluid().getFluidType(), held.is(Items.GLASS_BOTTLE), true);
            give(player, hand, held, result);
            return true;
        }
        return false;
    }

    private static boolean bottleOrWater(ItemStack stack) {
        return isWaterBottle(stack) || fluidForBottle(stack.getItem()) != null;
    }

    /** What the held container would pour in (simulated). */
    private static FluidStack emptied(ItemStack held, IFluidHandler tank) {
        if (isWaterBottle(held)) {
            FluidStack water = new FluidStack(Fluids.WATER, BOTTLE);
            return tank.fill(water, IFluidHandler.FluidAction.SIMULATE) == BOTTLE ? water : FluidStack.EMPTY;
        }
        Fluid bottled = fluidForBottle(held.getItem());
        if (bottled != null) {
            FluidStack contents = new FluidStack(bottled, BOTTLE);
            return tank.fill(contents, IFluidHandler.FluidAction.SIMULATE) == BOTTLE ? contents : FluidStack.EMPTY;
        }
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(held, 1)).orElse(null);
        if (handler == null) return FluidStack.EMPTY;
        return FluidUtil.tryFluidTransfer(tank, handler, space(tank), false);
    }

    /** mB the held empty container would take out (simulated). */
    private static int filled(ItemStack held, IFluidHandler tank) {
        FluidStack inside = tank.getFluidInTank(0);
        if (inside.isEmpty()) return 0;
        if (held.is(Items.GLASS_BOTTLE)) {
            if (inside.getFluid().isSame(Fluids.WATER) && inside.getAmount() >= BOTTLE) return BOTTLE;
            return bottleFor(inside.getFluid()) != null && inside.getAmount() >= BOTTLE ? BOTTLE : 0;
        }
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(held, 1)).orElse(null);
        if (handler == null) return 0;
        return FluidUtil.tryFluidTransfer(handler, tank, inside.getAmount(), false).getAmount();
    }

    /** The container after the transfer, built on a copy (tank not yet changed). */
    private static ItemStack container(ItemStack held, IFluidHandler tank, boolean emptying) {
        if (emptying) {
            if (isWaterBottle(held) || fluidForBottle(held.getItem()) != null) return new ItemStack(Items.GLASS_BOTTLE);
            IFluidHandlerItem handler = FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(held, 1)).orElse(null);
            FluidStack moved = FluidUtil.tryFluidTransfer(tank, handler, space(tank), false);
            handler.drain(moved, IFluidHandler.FluidAction.EXECUTE);
            return handler.getContainer();
        }
        FluidStack inside = tank.getFluidInTank(0);
        if (held.is(Items.GLASS_BOTTLE)) {
            if (inside.getFluid().isSame(Fluids.WATER)) return waterBottle();
            return new ItemStack(bottleFor(inside.getFluid()));
        }
        IFluidHandlerItem handler = FluidUtil.getFluidHandler(ItemHandlerHelper.copyStackWithSize(held, 1)).orElse(null);
        FluidStack moved = FluidUtil.tryFluidTransfer(handler, tank, inside.getAmount(), false);
        handler.fill(moved, IFluidHandler.FluidAction.EXECUTE);
        return handler.getContainer();
    }

    private static int space(IFluidHandler tank) {
        return tank.getTankCapacity(0) - tank.getFluidInTank(0).getAmount();
    }

    private static void give(Player player, InteractionHand hand, ItemStack held, ItemStack result) {
        if (player.getAbilities().instabuild || result.isEmpty()) return;
        if (held.getCount() == 1) {
            player.setItemInHand(hand, result);
            return;
        }
        held.shrink(1);
        ItemStack rest = ItemHandlerHelper.insertItemStacked(new InvWrapper(player.getInventory()), result, false);
        if (!rest.isEmpty()) ItemHandlerHelper.giveItemToPlayer(player, rest);
    }

    private static void sound(Level level, BlockPos pos, FluidType type, boolean bottle, boolean filling) {
        SoundEvent event;
        if (bottle) {
            event = filling ? SoundEvents.BOTTLE_FILL : SoundEvents.BOTTLE_EMPTY;
        } else {
            event = type.getSound(filling ? SoundActions.BUCKET_FILL : SoundActions.BUCKET_EMPTY);
            if (event == null) event = filling ? SoundEvents.BUCKET_FILL : SoundEvents.BUCKET_EMPTY;
        }
        level.playSound(null, pos, event, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
