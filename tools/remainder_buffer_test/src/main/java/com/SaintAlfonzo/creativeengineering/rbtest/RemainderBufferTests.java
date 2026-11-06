package com.SaintAlfonzo.creativeengineering.rbtest;

import java.util.List;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.SaintAlfonzo.creativedelight.cooking.RemainderBuffer;
import com.SaintAlfonzo.creativedelight.cooking.RemainderBufferHolder;
import com.SaintAlfonzo.creativedelight.cooking.RemainderSlot;
import com.SaintAlfonzo.creativedelight.oven.OvenBlockEntity;
import com.SaintAlfonzo.creativedelight.oven.OvenRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.AfterBatch;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import umpaz.farmersrespite.common.block.entity.KettleBlockEntity;
import umpaz.farmersrespite.common.block.entity.container.KettleMenu;
import umpaz.farmersrespite.common.registry.FRBlocks;
import umpaz.farmersrespite.common.registry.FRItems;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.registry.ModItems;

@GameTestHolder(CreativeEngineering.MODID)
@PrefixGameTestTemplate(false)
public class RemainderBufferTests {

    private static final String TEMPLATE = "rbtest_empty";
    private static final BlockPos POS = new BlockPos(2, 2, 2);

    // ------------------------------------------------------------------ helpers

    private static void floor(GameTestHelper h) {
        for (int x = 0; x < 6; ++x) for (int z = 0; z < 6; ++z) h.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
    }

    private static CookingPotBlockEntity pot(GameTestHelper h) {
        floor(h);
        h.setBlock(POS.below(), Blocks.MAGMA_BLOCK);
        h.setBlock(POS, ModBlocks.COOKING_POT.get());
        return (CookingPotBlockEntity) h.getBlockEntity(POS);
    }

    private static KettleBlockEntity kettle(GameTestHelper h) {
        floor(h);
        h.setBlock(POS.below(), Blocks.MAGMA_BLOCK);
        h.setBlock(POS, FRBlocks.KETTLE.get());
        return (KettleBlockEntity) h.getBlockEntity(POS);
    }

    private static RemainderBuffer buffer(Object blockEntity) {
        return ((RemainderBufferHolder) blockEntity).ce$getRemainderBuffer();
    }

    private static int thrown(GameTestHelper h, Item item) {
        AABB box = new AABB(h.absolutePos(BlockPos.ZERO)).inflate(8);
        List<ItemEntity> items = h.getLevel().getEntities(EntityType.ITEM, box, e -> e.getItem().is(item));
        return items.stream().mapToInt(e -> e.getItem().getCount()).sum();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new net.minecraft.gametest.framework.GameTestAssertException(message);
    }

    private static void hotCocoa(ItemStackHandler inv, ItemStack milk) {
        inv.setStackInSlot(0, milk);
        inv.setStackInSlot(1, new ItemStack(Items.SUGAR));
        inv.setStackInSlot(2, new ItemStack(Items.COCOA_BEANS));
        inv.setStackInSlot(3, new ItemStack(Items.COCOA_BEANS));
    }

    // ------------------------------------------------------------------ pot

    /** RB-T1: a milk bottle's bottle plates the hot cocoa (recycle into the container slot); nothing thrown. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void potRecyclesBottle(GameTestHelper h) {
        CookingPotBlockEntity pot = pot(h);
        hotCocoa(pot.getInventory(), new ItemStack(ModItems.MILK_BOTTLE.get()));
        h.succeedWhen(() -> {
            ItemStack out = pot.getInventory().getStackInSlot(CookingPotBlockEntity.OUTPUT_SLOT);
            check(out.is(ModItems.HOT_COCOA.get()) && out.getCount() == 1, "no plated hot cocoa yet: " + out);
            check(pot.getInventory().getStackInSlot(CookingPotBlockEntity.CONTAINER_SLOT).isEmpty(), "container slot not used up");
            check(buffer(pot).getStackInSlot(0).isEmpty() && buffer(pot).getStackInSlot(1).isEmpty(), "buffer not empty");
            check(thrown(h, Items.GLASS_BOTTLE) == 0, "a bottle was thrown");
        });
    }

    /** RB-T2: a milk bucket's bucket goes to the buffer (hot cocoa isn't served in a bucket); nothing thrown. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void potBucketToBuffer(GameTestHelper h) {
        CookingPotBlockEntity pot = pot(h);
        hotCocoa(pot.getInventory(), new ItemStack(Items.MILK_BUCKET));
        h.succeedWhen(() -> {
            check(pot.getMeal().is(ModItems.HOT_COCOA.get()), "not cooked yet");
            ItemStack kept = buffer(pot).getStackInSlot(0);
            check(kept.is(Items.BUCKET) && kept.getCount() == 1, "bucket not in buffer slot 0: " + kept);
            check(thrown(h, Items.BUCKET) == 0, "a bucket was thrown");
        });
    }

    /** RB-T3: both slots blocked -> the pot doesn't cook; free a slot -> it cooks and keeps the bucket. */
    @GameTest(template = TEMPLATE, timeoutTicks = 800)
    public static void potStallsWhenFull(GameTestHelper h) {
        CookingPotBlockEntity pot = pot(h);
        buffer(pot).setStackInSlot(0, new ItemStack(Items.BUCKET, 16));
        buffer(pot).setStackInSlot(1, new ItemStack(Items.BOWL, 64));
        hotCocoa(pot.getInventory(), new ItemStack(Items.MILK_BUCKET));
        h.startSequence()
                .thenExecuteAfter(300, () -> {
                    check(pot.getMeal().isEmpty(), "cooked although the buffer was full");
                    check(pot.getInventory().getStackInSlot(0).is(Items.MILK_BUCKET), "ingredients used");
                    check(thrown(h, Items.BUCKET) == 0, "a bucket was thrown");
                    buffer(pot).setStackInSlot(1, ItemStack.EMPTY);
                })
                .thenWaitUntil(() -> {
                    check(pot.getMeal().is(ModItems.HOT_COCOA.get()), "not cooked after freeing a slot");
                    check(buffer(pot).getStackInSlot(1).is(Items.BUCKET), "bucket not in the freed slot");
                    check(thrown(h, Items.BUCKET) == 0, "a bucket was thrown");
                })
                .thenSucceed();
    }

    @BeforeBatch(batch = "stall_off")
    public static void stallOff(ServerLevel level) {
        CEConfig.COOKING.stallWhenFull.set(false);
    }

    @AfterBatch(batch = "stall_off")
    public static void stallOn(ServerLevel level) {
        CEConfig.COOKING.stallWhenFull.set(true);
    }

    /** RB-T4: stallWhenFull off: a full buffer doesn't stop the pot, the bucket is thrown as before. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400, batch = "stall_off")
    public static void potThrowsWhenStallOff(GameTestHelper h) {
        CookingPotBlockEntity pot = pot(h);
        buffer(pot).setStackInSlot(0, new ItemStack(Items.BUCKET, 16));
        buffer(pot).setStackInSlot(1, new ItemStack(Items.BOWL, 64));
        hotCocoa(pot.getInventory(), new ItemStack(Items.MILK_BUCKET));
        h.succeedWhen(() -> {
            check(pot.getMeal().is(ModItems.HOT_COCOA.get()), "not cooked");
            check(thrown(h, Items.BUCKET) == 1, "buckets thrown: " + thrown(h, Items.BUCKET) + ", buffer " + buffer(pot).getStackInSlot(0) + " / " + buffer(pot).getStackInSlot(1));
        });
    }

    /** RB-T5: side/bottom handler = FD's 9 slots + 2 extract-only buffer slots; top unchanged; invalidated on removal. */
    @GameTest(template = TEMPLATE)
    public static void potAutomation(GameTestHelper h) {
        CookingPotBlockEntity pot = pot(h);
        buffer(pot).setStackInSlot(0, new ItemStack(Items.BUCKET, 2));
        pot.getInventory().setStackInSlot(CookingPotBlockEntity.OUTPUT_SLOT, new ItemStack(ModItems.HOT_COCOA.get()));
        LazyOptional<IItemHandler> down = pot.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.DOWN);
        IItemHandler side = pot.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.NORTH).orElseThrow(IllegalStateException::new);
        IItemHandler bottom = down.orElseThrow(IllegalStateException::new);
        IItemHandler top = pot.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).orElseThrow(IllegalStateException::new);
        check(bottom.getSlots() == 11 && side.getSlots() == 11, "side/bottom slots: " + bottom.getSlots());
        check(top.getSlots() == 9, "top slots: " + top.getSlots());
        check(bottom.extractItem(8, 64, false).is(ModItems.HOT_COCOA.get()), "output not extracted");
        check(bottom.insertItem(9, new ItemStack(Items.BUCKET), false).getCount() == 1, "buffer accepted an insert");
        ItemStack got = bottom.extractItem(9, 64, false);
        check(got.is(Items.BUCKET) && got.getCount() == 2, "buffer extract: " + got);
        check(buffer(pot).getStackInSlot(0).isEmpty(), "buffer not emptied");
        check(bottom.insertItem(7, new ItemStack(Items.BOWL), false).isEmpty(), "container slot insert broken");
        h.getLevel().removeBlock(h.absolutePos(POS), false);
        check(!down.isPresent(), "handler still valid after removal");
        h.succeed();
    }

    /** RB-T6: breaking the pot drops the buffer. */
    @GameTest(template = TEMPLATE)
    public static void potBreakDrops(GameTestHelper h) {
        CookingPotBlockEntity pot = pot(h);
        buffer(pot).setStackInSlot(0, new ItemStack(Items.BUCKET, 3));
        buffer(pot).setStackInSlot(1, new ItemStack(Items.BOWL, 2));
        h.getLevel().destroyBlock(h.absolutePos(POS), true);
        h.succeedWhen(() -> {
            check(thrown(h, Items.BUCKET) == 3, "buckets dropped: " + thrown(h, Items.BUCKET));
            check(thrown(h, Items.BOWL) == 2, "bowls dropped: " + thrown(h, Items.BOWL));
        });
    }

    /** RB-T7: the buffer is saved and loaded with the block entity (ForgeCaps). */
    @GameTest(template = TEMPLATE)
    public static void potSaveLoad(GameTestHelper h) {
        CookingPotBlockEntity pot = pot(h);
        buffer(pot).setStackInSlot(1, new ItemStack(Items.BOWL, 5));
        CompoundTag tag = pot.saveWithFullMetadata();
        check(tag.getCompound("ForgeCaps").contains("creativeengineering:remainder_buffer"), "not saved: " + tag);
        BlockState state = h.getBlockState(POS);
        BlockEntity copy = BlockEntity.loadStatic(h.absolutePos(POS), state, tag);
        check(copy != null, "load failed");
        ItemStack loaded = buffer(copy).getStackInSlot(1);
        check(loaded.is(Items.BOWL) && loaded.getCount() == 5, "loaded: " + loaded);
        h.succeed();
    }

    /** RB-T8: the pot and kettle menus get two take-only slots after the player inventory; shift-click empties one. */
    @GameTest(template = TEMPLATE)
    public static void menus(GameTestHelper h) {
        CookingPotBlockEntity pot = pot(h);
        Player player = h.makeMockPlayer();
        buffer(pot).setStackInSlot(0, new ItemStack(Items.BOWL, 5));
        CookingPotMenu menu = new CookingPotMenu(1, player.getInventory(), pot, new SimpleContainerData(4));
        check(menu.slots.size() == 9 + 36 + 2, "pot menu slots: " + menu.slots.size());
        check(menu.getSlot(45) instanceof RemainderSlot && menu.getSlot(46) instanceof RemainderSlot, "no remainder slots");
        check(!menu.getSlot(45).mayPlace(new ItemStack(Items.BOWL)), "remainder slot accepts items");
        check(menu.getSlot(45).getItem().is(Items.BOWL), "slot doesn't show the buffer");
        menu.quickMoveStack(player, 45);
        check(buffer(pot).getStackInSlot(0).isEmpty(), "shift-click left: " + buffer(pot).getStackInSlot(0));
        check(player.getInventory().countItem(Items.BOWL) == 5, "player got " + player.getInventory().countItem(Items.BOWL));
        // FD's own shift-click still works (output -> player)
        pot.getInventory().setStackInSlot(CookingPotBlockEntity.OUTPUT_SLOT, new ItemStack(ModItems.HOT_COCOA.get(), 2));
        menu.quickMoveStack(player, CookingPotBlockEntity.OUTPUT_SLOT);
        check(player.getInventory().countItem(ModItems.HOT_COCOA.get()) == 2, "FD output shift-click broken");

        BlockPos kettlePos = POS.east(2);
        h.setBlock(kettlePos, FRBlocks.KETTLE.get());
        KettleBlockEntity kettle = (KettleBlockEntity) h.getBlockEntity(kettlePos);
        KettleMenu kettleMenu = new KettleMenu(2, player.getInventory(), kettle, new SimpleContainerData(4));
        check(kettleMenu.slots.size() == 5 + 36 + 2, "kettle menu slots: " + kettleMenu.slots.size());
        check(kettleMenu.getSlot(41) instanceof RemainderSlot, "no kettle remainder slot");
        h.succeed();
    }

    // ------------------------------------------------------------------ kettle

    /** RB-T9: strong green tea: one bottle plates the drink, any second bottle is buffered; nothing thrown. */
    @GameTest(template = TEMPLATE, timeoutTicks = 2800)
    public static void kettleStrongTea(GameTestHelper h) {
        KettleBlockEntity kettle = kettle(h);
        ItemStack tea = new ItemStack(FRItems.GREEN_TEA.get());
        int expectedBuffered = tea.hasCraftingRemainingItem() ? 1 : 0;
        kettle.getInventory().setStackInSlot(0, tea);
        kettle.getInventory().setStackInSlot(1, new ItemStack(Items.HONEY_BOTTLE));
        h.succeedWhen(() -> {
            ItemStack out = kettle.getInventory().getStackInSlot(KettleBlockEntity.OUTPUT_SLOT);
            check(out.is(FRItems.STRONG_GREEN_TEA.get()), "no plated strong green tea yet: " + out);
            ItemStack kept = buffer(kettle).getStackInSlot(0);
            check(kept.getCount() == expectedBuffered && (expectedBuffered == 0 || kept.is(Items.GLASS_BOTTLE)),
                    "buffer: " + kept + ", expected bottles " + expectedBuffered);
            check(thrown(h, Items.GLASS_BOTTLE) == 0, "a bottle was thrown");
            check(kettle.getInventory().getStackInSlot(KettleBlockEntity.CONTAINER_SLOT).isEmpty(), "spare bottle left in the container slot");
        });
    }

    // ------------------------------------------------------------------ oven

    /** RB-T10: oven test recipe (milk bottle + sugar -> bread): the bottle goes to the oven's buffer; automation sees it. */
    @GameTest(template = TEMPLATE, timeoutTicks = 300)
    public static void ovenBuffer(GameTestHelper h) {
        floor(h);
        h.setBlock(POS.below(), Blocks.MAGMA_BLOCK);
        h.setBlock(POS, OvenRegistry.OVEN.get());
        OvenBlockEntity oven = (OvenBlockEntity) h.getBlockEntity(POS);
        oven.getInventory().setStackInSlot(0, new ItemStack(ModItems.MILK_BOTTLE.get()));
        oven.getInventory().setStackInSlot(1, new ItemStack(Items.SUGAR));
        h.succeedWhen(() -> {
            check(oven.getInventory().getStackInSlot(OvenBlockEntity.OUTPUT_SLOT).is(Items.BREAD), "not baked");
            check(oven.getRemainders().getStackInSlot(0).is(Items.GLASS_BOTTLE), "bottle not buffered");
            check(thrown(h, Items.GLASS_BOTTLE) == 0, "a bottle was thrown");
            IItemHandler handler = oven.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.NORTH).orElseThrow(IllegalStateException::new);
            check(handler.getSlots() == OvenBlockEntity.INVENTORY_SIZE + 2, "oven handler slots: " + handler.getSlots());
            check(handler.extractItem(OvenBlockEntity.INVENTORY_SIZE, 1, true).is(Items.GLASS_BOTTLE), "oven buffer not extractable");
        });
    }

    /** RB-T11: the oven's update tag carries the buffer, and a tag without it doesn't wipe the client copy (kitchen #14). */
    @GameTest(template = TEMPLATE)
    public static void ovenUpdateTagKeepsBuffer(GameTestHelper h) {
        floor(h);
        h.setBlock(POS, OvenRegistry.OVEN.get());
        OvenBlockEntity oven = (OvenBlockEntity) h.getBlockEntity(POS);
        oven.getRemainders().setStackInSlot(0, new ItemStack(Items.GLASS_BOTTLE, 3));
        CompoundTag tag = oven.getUpdateTag();
        check(tag.contains("Remainders"), "update tag has no buffer: " + tag);

        OvenBlockEntity client = new OvenBlockEntity(h.absolutePos(POS), h.getBlockState(POS));
        client.getRemainders().setStackInSlot(1, new ItemStack(Items.BUCKET));
        client.load(tag);
        ItemStack loaded = client.getRemainders().getStackInSlot(0);
        check(loaded.is(Items.GLASS_BOTTLE) && loaded.getCount() == 3, "loaded: " + loaded);
        check(client.getRemainders().getStackInSlot(1).isEmpty(), "stale slot 1 kept: " + client.getRemainders().getStackInSlot(1));

        tag.remove("Remainders");
        client.load(tag);
        check(client.getRemainders().getStackInSlot(0).is(Items.GLASS_BOTTLE), "a tag without the buffer wiped it");
        h.succeed();
    }

    /** RB-T12: the pot's update tag carries the buffer through ForgeCaps (what the pot and kettle client copies rely on). */
    @GameTest(template = TEMPLATE)
    public static void potUpdateTagHasBuffer(GameTestHelper h) {
        CookingPotBlockEntity pot = pot(h);
        buffer(pot).setStackInSlot(0, new ItemStack(Items.BOWL, 2));
        CompoundTag tag = pot.getUpdateTag();
        check(tag.getCompound("ForgeCaps").contains("creativeengineering:remainder_buffer"), "not in update tag: " + tag);
        h.succeed();
    }

    // ------------------------------------------------------------------ bowl count (TODO cooking/kitchen #4)

    /**
     * Bowls a station holds: in ingredients that leave one, in the plating slot, in the leftover slots, and one per
     * plated result. A cook must neither make nor lose one (a thrown bowl counts as lost; not counted from item
     * entities because RB-T6 drops bowls next door).
     */
    private static int bowls(ItemStackHandler inv, RemainderBuffer buffer, int ingredientSlots, int containerSlot,
                             int outputSlot, Item plated) {
        int n = 0;
        for (int i = 0; i < ingredientSlots; ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.getCraftingRemainingItem().is(Items.BOWL)) n += stack.getCount();
        }
        if (inv.getStackInSlot(containerSlot).is(Items.BOWL)) n += inv.getStackInSlot(containerSlot).getCount();
        for (int i = 0; i < buffer.getSlots(); ++i) {
            if (buffer.getStackInSlot(i).is(Items.BOWL)) n += buffer.getStackInSlot(i).getCount();
        }
        if (inv.getStackInSlot(outputSlot).is(plated)) n += inv.getStackInSlot(outputSlot).getCount();
        return n;
    }

    /** Test recipe tomato sauce (bowl leftover) + carrot -> beef stew (served in a bowl), {@code sauces} times. */
    private static void potBowlCase(GameTestHelper h, int containerBowls, int sauces, int mealsWaiting) {
        CookingPotBlockEntity pot = pot(h);
        ItemStackHandler inv = pot.getInventory();
        check(new ItemStack(ModItems.TOMATO_SAUCE.get()).getCraftingRemainingItem().is(Items.BOWL), "tomato sauce leaves no bowl");
        inv.setStackInSlot(0, new ItemStack(ModItems.TOMATO_SAUCE.get(), sauces));
        inv.setStackInSlot(1, new ItemStack(Items.CARROT, sauces));
        if (containerBowls > 0) inv.setStackInSlot(CookingPotBlockEntity.CONTAINER_SLOT, new ItemStack(Items.BOWL, containerBowls));
        if (mealsWaiting > 0) inv.setStackInSlot(CookingPotBlockEntity.MEAL_DISPLAY_SLOT, new ItemStack(ModItems.BEEF_STEW.get(), mealsWaiting));
        int before = bowls(inv, buffer(pot), 6, CookingPotBlockEntity.CONTAINER_SLOT, CookingPotBlockEntity.OUTPUT_SLOT, ModItems.BEEF_STEW.get());
        h.succeedWhen(() -> {
            check(inv.getStackInSlot(0).isEmpty(), "not all cooked yet: " + inv.getStackInSlot(0));
            int after = bowls(inv, buffer(pot), 6, CookingPotBlockEntity.CONTAINER_SLOT, CookingPotBlockEntity.OUTPUT_SLOT, ModItems.BEEF_STEW.get());
            check(after == before, "bowls before " + before + ", after " + after + " (container "
                    + inv.getStackInSlot(CookingPotBlockEntity.CONTAINER_SLOT) + ", buffer " + buffer(pot).getStackInSlot(0)
                    + " / " + buffer(pot).getStackInSlot(1) + ", output " + inv.getStackInSlot(CookingPotBlockEntity.OUTPUT_SLOT)
                    + ", meal " + pot.getMeal() + ")");
        });
    }

    /** RB-T13a: plating slot empty: the sauce's bowl plates the stew. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void potBowlsSlotEmpty(GameTestHelper h) {
        potBowlCase(h, 0, 1, 0);
    }

    /** RB-T13b: one bowl in the plating slot: it plates the stew, the sauce's bowl goes to a leftover slot. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void potBowlsSlotOne(GameTestHelper h) {
        potBowlCase(h, 1, 1, 0);
    }

    /** RB-T13c: ten bowls in the plating slot, three cooks. */
    @GameTest(template = TEMPLATE, timeoutTicks = 600)
    public static void potBowlsSlotTen(GameTestHelper h) {
        potBowlCase(h, 10, 3, 0);
    }

    /** RB-T13d: two stews already waiting for bowls, plating slot empty. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void potBowlsMealWaiting(GameTestHelper h) {
        potBowlCase(h, 0, 1, 2);
    }

    /** RB-T13e: oven test recipe tomato sauce + sugar -> bread plated in a bowl, two bowls in the plating slot. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void ovenBowls(GameTestHelper h) {
        floor(h);
        h.setBlock(POS.below(), Blocks.MAGMA_BLOCK);
        h.setBlock(POS, OvenRegistry.OVEN.get());
        OvenBlockEntity oven = (OvenBlockEntity) h.getBlockEntity(POS);
        ItemStackHandler inv = oven.getInventory();
        inv.setStackInSlot(0, new ItemStack(ModItems.TOMATO_SAUCE.get(), 2));
        inv.setStackInSlot(1, new ItemStack(Items.SUGAR, 2));
        inv.setStackInSlot(OvenBlockEntity.CONTAINER_SLOT, new ItemStack(Items.BOWL, 2));
        int before = bowls(inv, oven.getRemainders(), 6, OvenBlockEntity.CONTAINER_SLOT, OvenBlockEntity.OUTPUT_SLOT, Items.BREAD);
        h.succeedWhen(() -> {
            check(inv.getStackInSlot(0).isEmpty(), "not all baked yet: " + inv.getStackInSlot(0));
            int after = bowls(inv, oven.getRemainders(), 6, OvenBlockEntity.CONTAINER_SLOT, OvenBlockEntity.OUTPUT_SLOT, Items.BREAD);
            check(after == before, "bowls before " + before + ", after " + after);
            check(inv.getStackInSlot(OvenBlockEntity.OUTPUT_SLOT).getCount() == 2, "not plated: " + inv.getStackInSlot(OvenBlockEntity.OUTPUT_SLOT));
        });
    }

    // ------------------------------------------------------------------ plating delay (TODO cooking/kitchen #13)

    /**
     * RB-T14: hot cocoa with a milk bottle: once cooked, the recycled bottle sits in the container slot with the
     * cocoa unplated; it is plated recycleShowTicks (10) later.
     */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void potRecycleDelay(GameTestHelper h) {
        CookingPotBlockEntity pot = pot(h);
        hotCocoa(pot.getInventory(), new ItemStack(ModItems.MILK_BOTTLE.get()));
        long[] cookedAt = new long[1];
        h.startSequence()
                .thenWaitUntil(() -> check(pot.getMeal().is(ModItems.HOT_COCOA.get()), "not cooked yet"))
                .thenExecute(() -> {
                    cookedAt[0] = h.getTick();
                    check(pot.getInventory().getStackInSlot(CookingPotBlockEntity.CONTAINER_SLOT).is(Items.GLASS_BOTTLE),
                            "recycled bottle not shown in the container slot");
                    check(pot.getInventory().getStackInSlot(CookingPotBlockEntity.OUTPUT_SLOT).isEmpty(), "plated at once");
                })
                .thenWaitUntil(() -> check(pot.getInventory().getStackInSlot(CookingPotBlockEntity.OUTPUT_SLOT)
                        .is(ModItems.HOT_COCOA.get()), "not plated yet"))
                .thenExecute(() -> check(h.getTick() - cookedAt[0] >= 9,
                        "plated after " + (h.getTick() - cookedAt[0]) + " ticks"))
                .thenSucceed();
    }

    /** RB-T15: oven (tomato sauce + sugar -> bread in a bowl, plating slot empty): the sauce's bowl shows, then plates. */
    @GameTest(template = TEMPLATE, timeoutTicks = 400)
    public static void ovenRecycleDelay(GameTestHelper h) {
        floor(h);
        h.setBlock(POS.below(), Blocks.MAGMA_BLOCK);
        h.setBlock(POS, OvenRegistry.OVEN.get());
        OvenBlockEntity oven = (OvenBlockEntity) h.getBlockEntity(POS);
        oven.getInventory().setStackInSlot(0, new ItemStack(ModItems.TOMATO_SAUCE.get()));
        oven.getInventory().setStackInSlot(1, new ItemStack(Items.SUGAR));
        long[] bakedAt = new long[1];
        h.startSequence()
                .thenWaitUntil(() -> check(oven.getMeal().is(Items.BREAD), "not baked yet"))
                .thenExecute(() -> {
                    bakedAt[0] = h.getTick();
                    check(oven.getInventory().getStackInSlot(OvenBlockEntity.CONTAINER_SLOT).is(Items.BOWL),
                            "recycled bowl not shown in the plating slot");
                })
                .thenWaitUntil(() -> check(oven.getInventory().getStackInSlot(OvenBlockEntity.OUTPUT_SLOT).is(Items.BREAD),
                        "not plated yet"))
                .thenExecute(() -> check(h.getTick() - bakedAt[0] >= 9, "plated after " + (h.getTick() - bakedAt[0]) + " ticks"))
                .thenSucceed();
    }
}
