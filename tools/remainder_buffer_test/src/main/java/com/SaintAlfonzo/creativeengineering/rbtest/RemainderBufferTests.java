package com.SaintAlfonzo.creativeengineering.rbtest;

import java.util.List;

import com.SaintAlfonzo.creativeengineering.CEConfig;
import com.SaintAlfonzo.creativeengineering.CreativeEngineering;
import com.SaintAlfonzo.creativeengineering.cooking.RemainderBuffer;
import com.SaintAlfonzo.creativeengineering.cooking.RemainderBufferHolder;
import com.SaintAlfonzo.creativeengineering.cooking.RemainderSlot;
import com.SaintAlfonzo.creativeengineering.oven.OvenBlockEntity;
import com.SaintAlfonzo.creativeengineering.oven.OvenRegistry;

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
            check(thrown(h, Items.BUCKET) == 1, "bucket not thrown");
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
}
