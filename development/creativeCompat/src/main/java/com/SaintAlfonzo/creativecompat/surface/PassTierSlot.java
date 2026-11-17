package com.SaintAlfonzo.creativecompat.surface;

/**
 * Duck interface on every {@code BlockState} ({@code mixin/BlockStatePassMixin}): one int slot that remembers the
 * state's pass-through tier for the current tag generation, so {@link PassThrough#tierOf(net.minecraft.world.level.block.state.BlockState)}
 * is a field read instead of a map lookup (scratch run r1115: the map lookup was a third of the hook's time). Packed as
 * {@code generation << 5 | (tier ordinal + 1)}, 0 = no tier, so one plain int write publishes both halves.
 */
public interface PassTierSlot {

    int creativecompat$passTier();

    void creativecompat$setPassTier(int packed);
}
