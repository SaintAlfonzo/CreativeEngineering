// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/items/RudimentaryTempadItem.kt (item
// tempad:timedoor_projector). Translated to Java for Forge 1.19.2: the properties are passed in (ModItems adds the creative
// tab); the client extension (arm pose) is set in initializeClient; the block entity's owner attachment -> RudimentaryTempadBE.owner; Slot.contents -> Slot.getItem / set.
// Kept upstream quirks: the "no location" message is never shown (upstream's `?: { ... }` builds a lambda it never
// calls); useOn returns PASS after opening a door, so the use() that follows may try again (blocked by the cooldown).
package earth.terrarium.tempad.common.items;

import earth.terrarium.tempad.api.context.InventoryContext;
import earth.terrarium.tempad.client.RudimentaryTempadClient;
import earth.terrarium.tempad.api.context.SyncableContext;
import earth.terrarium.tempad.api.locations.IndirectLocation;
import earth.terrarium.tempad.api.locations.LocationGetter;
import earth.terrarium.tempad.api.locations.NamedGlobalVec3;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.api.tva_device.UpgradeHandler;
import earth.terrarium.tempad.common.block.RudimentaryTempadBE;
import earth.terrarium.tempad.common.entity.TimedoorEntity;
import earth.terrarium.tempad.common.registries.ModBlocks;
import earth.terrarium.tempad.common.registries.ModComponents;
import earth.terrarium.tempad.common.registries.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class RudimentaryTempadItem extends BlockItem {
    public RudimentaryTempadItem(Properties properties) {
        super(ModBlocks.timedoorProjector.get(), properties.stacksTo(1));
    }

    /**
     * Upstream TempadClient.registerClientExtensions (RudimentaryTempadClient: the projector arm pose). Forge calls this
     * only on the client; the client class is reached through a static call typed as the Forge interface, so a
     * dedicated server never loads it.
     */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(RudimentaryTempadClient.get());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (level.isClientSide) return InteractionResultHolder.success(player.getItemInHand(usedHand));
        openTimedoor(new InventoryContext(player, TempadItem.getSlot(usedHand, player)), player);
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) return super.useOn(context);
        if (player == null) return InteractionResult.PASS;
        openTimedoor(new InventoryContext(player, TempadItem.getSlot(context.getHand(), player)), player);
        return InteractionResult.PASS;
    }

    /** Upstream extension {@code SyncableContext<*>.openTimedoor(player)}. */
    public static void openTimedoor(SyncableContext<?> ctx, Player player) {
        ItemStack stack = ctx.getStack();
        LocationGetter portalTarget = ModComponents.getPortalTarget(stack);
        if (portalTarget == null) return;
        NamedGlobalVec3 pos = portalTarget.get(UpgradeHandler.of(stack), ChrononHandler.of(stack));
        if (pos == null || player.level.isClientSide) return;
        IndirectLocation indirect = portalTarget instanceof IndirectLocation it ? it : null;
        TimedoorEntity.openTimedoor(player, ctx, indirect == null ? null : indirect.provider(), indirect == null ? null : indirect.id(), pos,
                door -> door.setGlitching(true));
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;
        LocationGetter target = ModComponents.getPortalTarget(stack);
        if (target != null) {
            if (slot.hasItem()) return false;
            ItemStack card = new ItemStack(ModItems.locationCard.get());
            ModComponents.setPortalTarget(card, target);
            slot.set(card);
            ModComponents.setPortalTarget(stack, null);
            return true;
        } else if (slot.getItem().is(ModItems.locationCard.get()) && ModComponents.getPortalTarget(slot.getItem()) != null) {
            ModComponents.setPortalTarget(stack, ModComponents.getPortalTarget(slot.getItem()));
            slot.set(minus(slot.getItem(), 1));
            return true;
        }
        return false;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player player, ItemStack stack, BlockState state) {
        if (level.getBlockEntity(pos) instanceof RudimentaryTempadBE blockEntity) {
            LocationGetter target = ModComponents.getPortalTarget(stack);
            if (target != null) blockEntity.portalTarget = target;
            if (player != null) blockEntity.owner = player.getGameProfile();
            blockEntity.chrononContent = ModComponents.getChrononContent(stack);
        }
        return super.updateCustomBlockEntityTag(pos, level, player, stack, state);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (ModComponents.getPortalTarget(stack) == null) {
            if (other.is(ModItems.locationCard.get()) && ModComponents.getPortalTarget(other) != null) {
                ModComponents.setPortalTarget(stack, ModComponents.getPortalTarget(other));
                access.set(minus(other, 1));
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        ChrononHandler chronons = ChrononHandler.of(stack);
        return Optional.ofNullable(chronons == null ? null : chronons.tooltip());
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    /** Upstream {@code operator ItemStack.minus}: a shrunk copy, EMPTY when nothing is left. */
    static ItemStack minus(ItemStack stack, int amount) {
        ItemStack copy = stack.copy();
        copy.shrink(amount);
        return copy.isEmpty() ? ItemStack.EMPTY : copy;
    }
}
