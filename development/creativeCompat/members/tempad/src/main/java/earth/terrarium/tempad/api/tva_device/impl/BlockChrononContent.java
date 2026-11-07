// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/api/tva_device/impl/BlockChrononContent.kt. Translated to Java for
// Forge 1.19.2 (Kotlin lambdas -> IntConsumer setter / IntSupplier getter).
package earth.terrarium.tempad.api.tva_device.impl;

import earth.terrarium.tempad.api.ActionType;
import earth.terrarium.tempad.api.tva_device.ChrononHandler;
import earth.terrarium.tempad.common.block.MetronomeBe;
import earth.terrarium.tempad.common.block.RudimentaryTempadBE;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class BlockChrononContent implements ChrononHandler {
    public final BlockEntity block;
    private final IntConsumer setter;
    private final IntSupplier getter;
    private final int maxPower;

    public BlockChrononContent(BlockEntity block, IntConsumer setter, IntSupplier getter, int maxPower) {
        this.block = block;
        this.setter = setter;
        this.getter = getter;
        this.maxPower = maxPower;
    }

    @Nullable
    public static BlockChrononContent projector(RudimentaryTempadBE block, int maxPower) {
        if (maxPower <= 0) return null;
        return new BlockChrononContent(block, it -> block.chrononContent = it, () -> block.chrononContent, maxPower);
    }

    @Nullable
    public static BlockChrononContent metronome(MetronomeBe block, int maxPower) {
        if (maxPower <= 0) return null;
        return new BlockChrononContent(block, it -> block.bootChronons = it, () -> block.bootChronons, maxPower);
    }

    public static BlockChrononContent metronomeClient(MetronomeBe block) {
        return new BlockChrononContent(block, it -> block.localChronons = it, () -> block.localChronons, block.localCapacity);
    }

    @Override
    public int getPower() {
        return getter.getAsInt();
    }

    public void setPower(int value) {
        setter.accept(value);
    }

    @Override
    public int getMaxPower() {
        return maxPower;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public int extract(int amount, ActionType action) {
        int extracted = Mth.clamp(getPower(), 0, amount);
        if (action == ActionType.Execute) {
            setPower(getPower() - extracted);
            block.setChanged();
            if (block.getLevel() != null) {
                block.getLevel().sendBlockUpdated(block.getBlockPos(), block.getBlockState(), block.getBlockState(), Block.UPDATE_ALL);
            }
        }
        return extracted;
    }

    @Override
    public int insert(int amount, ActionType action) {
        int inserted = Mth.clamp(amount, 0, maxPower - getPower());
        if (action == ActionType.Execute) {
            setPower(getPower() + inserted);
            block.setChanged();
            if (block.getLevel() != null) {
                block.getLevel().sendBlockUpdated(block.getBlockPos(), block.getBlockState(), block.getBlockState(), Block.UPDATE_ALL);
            }
        }
        return inserted;
    }
}
