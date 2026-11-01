package net.satisfy.foundation.armor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class ArmorSet {
    private final String nameKey;
    private final List<List<Supplier<? extends Item>>> pieces;
    @Nullable
    private final String bonusKey;
    @Nullable
    private final Predicate<LivingEntity> bonusActive;
    private final BooleanSupplier tooltipVisible;

    private ArmorSet(Builder builder) {
        this.nameKey = builder.nameKey;
        this.pieces = List.copyOf(builder.pieces);
        this.bonusKey = builder.bonusKey;
        this.bonusActive = builder.bonusActive;
        this.tooltipVisible = builder.tooltipVisible;
    }

    public static Builder builder(String nameKey) {
        return new Builder(nameKey);
    }

    public String nameKey() {
        return nameKey;
    }

    @Nullable
    public String bonusKey() {
        return bonusKey;
    }

    public boolean isTooltipVisible() {
        return tooltipVisible.getAsBoolean();
    }

    public List<List<Item>> pieces() {
        return pieces.stream().map(piece -> piece.stream().<Item>map(Supplier::get).toList()).toList();
    }

    public boolean contains(Item item) {
        return pieces.stream().anyMatch(piece -> piece.stream().anyMatch(supplier -> supplier.get() == item));
    }

    public int wornPieces(LivingEntity entity) {
        return (int) pieces().stream().filter(piece -> Wearing.isWearingAny(entity, piece)).count();
    }

    public boolean isComplete(LivingEntity entity) {
        return wornPieces(entity) == pieces.size();
    }

    public boolean isBonusActive(LivingEntity entity) {
        return bonusActive != null ? bonusActive.test(entity) : isComplete(entity);
    }

    public static final class Builder {
        private final String nameKey;
        private final List<List<Supplier<? extends Item>>> pieces = new ArrayList<>();
        private String bonusKey;
        private Predicate<LivingEntity> bonusActive;
        private BooleanSupplier tooltipVisible = () -> true;

        private Builder(String nameKey) {
            this.nameKey = nameKey;
        }

        @SafeVarargs
        public final Builder piece(Supplier<? extends Item>... alternatives) {
            pieces.add(List.of(alternatives));
            return this;
        }

        public Builder bonus(String bonusKey) {
            this.bonusKey = bonusKey;
            return this;
        }

        public Builder bonusActive(Predicate<LivingEntity> bonusActive) {
            this.bonusActive = bonusActive;
            return this;
        }

        public Builder tooltipVisible(BooleanSupplier tooltipVisible) {
            this.tooltipVisible = Objects.requireNonNull(tooltipVisible);
            return this;
        }

        public ArmorSet register() {
            ArmorSet set = new ArmorSet(this);
            ArmorSets.register(set);
            return set;
        }
    }
}
