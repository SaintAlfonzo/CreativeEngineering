// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/context/WorkstationContext.kt. Translated to Java for Forge
// 1.19.2: ExtraByteCodecs.BLOCK_POS -> BufCodec on FriendlyByteBuf block positions.
package earth.terrarium.tempad.api.context;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.common.block.WorkstationBE;
import earth.terrarium.tempad.common.utils.BufCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public class WorkstationContext implements SyncableContext<BlockPos> {
    public static final ContextType<BlockPos> TYPE = new ContextType<>(Tempad.id("workstation"),
            BufCodec.of(FriendlyByteBuf::writeBlockPos, FriendlyByteBuf::readBlockPos));

    public final Player player;
    private final BlockPos data;

    public WorkstationContext(Player player, BlockPos data) {
        this.player = player;
        this.data = data;
    }

    @Override
    public ContextType<BlockPos> getType() {
        return TYPE;
    }

    @Override
    public BlockPos getData() {
        return data;
    }

    @Nullable
    public WorkstationBE getWorkstation() {
        return player.level.getBlockEntity(data) instanceof WorkstationBE station ? station : null;
    }

    @Override
    public ItemStack getStack() {
        WorkstationBE station = getWorkstation();
        return station == null ? ItemStack.EMPTY : station.inventory.getStackInSlot(0);
    }

    @Override
    public void setStack(ItemStack stack) {
        WorkstationBE station = getWorkstation();
        if (station == null) return;
        station.inventory.setStackInSlot(0, stack);
        station.setChanged();
        if (station.getLevel() != null) {
            station.getLevel().sendBlockUpdated(station.getBlockPos(), station.getBlockState(), station.getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void addStack(ItemStack stack) {
        player.getInventory().placeItemBackInInventory(stack);
    }
}
