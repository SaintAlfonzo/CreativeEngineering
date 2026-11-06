package com.SaintAlfonzo.more_mooblooms.entity;

import com.SaintAlfonzo.more_mooblooms.MMConfig;
import com.SaintAlfonzo.more_mooblooms.MoreMooblooms;
import com.SaintAlfonzo.more_mooblooms.item.MoobloomSpawnEggItem;
import com.SaintAlfonzo.more_mooblooms.spawn.MoobloomSpawns;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomBreeding;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariant;
import com.SaintAlfonzo.more_mooblooms.variant.MoobloomVariants;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * {@code more_mooblooms:moobloom}: a cow with flowers on its back. Written from scratch on vanilla {@link Cow}
 * (nothing of Buzzier Bees' moobloom). feature_writeup/mooblooms.md 4.3-4.6.
 * <ul>
 *   <li>Synced and saved: {@code Variant} (variant id), {@code Sheared} and {@code Flowers} (4.4).</li>
 *   <li>Flowers: each of the three positions grows its own weighted roll from the variant's pool. Kept empty
 *       while bare or a calf; rolled when it first shows flowers and again on every regrowth; a saved flower no
 *       longer in the pool is re-rolled on load ({@link #refreshFlowers}).</li>
 *   <li>Shearing: grown -> drops the flowers it carries and goes bare; bare -> turns back into a cow (config
 *       {@code shearBareToCow}, with three weighted pool drops when {@code bareShearDrops}). Every shear source
 *       (player and deployer through Forge, dispenser through vanilla) ends in {@link #shearInternal}.</li>
 *   <li>Grazing: {@link EatBlockGoal} like a sheep; {@link #ate} regrows the flowers at {@code regrowChance}.</li>
 *   <li>Food: the variant's flowers (breeding, baby growth, tempting) instead of wheat.</li>
 *   <li>Breeding: the calf's variant comes from {@link MoobloomBreeding} (combinations, breeds true, random).</li>
 * </ul>
 */
public class Moobloom extends Cow implements Shearable, IForgeShearable {

    private static final EntityDataAccessor<String> DATA_VARIANT =
            SynchedEntityData.defineId(Moobloom.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_SHEARED =
            SynchedEntityData.defineId(Moobloom.class, EntityDataSerializers.BOOLEAN);
    /** The grown flowers, one block state string per position joined by {@link #FLOWER_SEPARATOR}; "" = none. */
    private static final EntityDataAccessor<String> DATA_FLOWERS =
            SynchedEntityData.defineId(Moobloom.class, EntityDataSerializers.STRING);
    private static final String FLOWER_SEPARATOR = "|";

    /** Entity event the eat goal broadcasts (same byte as the sheep's): starts the head-down animation. */
    private static final byte EVENT_EAT = 10;

    private int eatAnimationTick;
    private EatBlockGoal eatBlockGoal;

    /** Parse cache for {@link #getFlowers} (the renderer asks every frame). */
    private String flowersKey = "";
    private List<BlockState> flowers = List.of();

    public Moobloom(EntityType<? extends Moobloom> type, Level level) {
        super(type, level);
    }

    // ---- variant / sheared state ----

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_VARIANT, "");
        entityData.define(DATA_SHEARED, false);
        entityData.define(DATA_FLOWERS, "");
    }

    public MoobloomVariant getVariant() {
        return MoobloomVariants.get(level.isClientSide, getVariantId());
    }

    @Nullable
    public ResourceLocation getVariantId() {
        String s = entityData.get(DATA_VARIANT);
        return s.isEmpty() ? null : ResourceLocation.tryParse(s);
    }

    public void setVariant(MoobloomVariant variant) {
        entityData.set(DATA_VARIANT, variant.id().toString());
    }

    public boolean isSheared() {
        return entityData.get(DATA_SHEARED);
    }

    public void setSheared(boolean sheared) {
        entityData.set(DATA_SHEARED, sheared);
    }

    /** Flowers are drawn on grown, unsheared adults only. */
    public boolean showsFlowers() {
        return !isBaby() && !isSheared();
    }

    /** The grown flowers in {@link MoobloomVariant.Slot} order; empty while bare or a calf. */
    public List<BlockState> getFlowers() {
        String key = entityData.get(DATA_FLOWERS);
        if (!key.equals(flowersKey)) {
            List<BlockState> parsed = new ArrayList<>();
            if (!key.isEmpty()) {
                for (String s : key.split(Pattern.quote(FLOWER_SEPARATOR))) {
                    try {
                        parsed.add(MoobloomVariant.parseState(s));
                    } catch (RuntimeException e) {
                        parsed.add(Blocks.AIR.defaultBlockState()); // a block this side doesn't have: draw nothing
                    }
                }
            }
            flowers = List.copyOf(parsed);
            flowersKey = key;
        }
        return flowers;
    }

    private void setFlowers(List<BlockState> states) {
        StringBuilder sb = new StringBuilder();
        for (BlockState state : states) {
            if (sb.length() > 0) sb.append(FLOWER_SEPARATOR);
            sb.append(BlockStateParser.serialize(state));
        }
        entityData.set(DATA_FLOWERS, sb.toString());
    }

    /** JEI render only: sets the drawn flowers on a client-side display entity. */
    public void setDisplayFlowers(List<BlockState> states) {
        setFlowers(states);
    }

    /**
     * Server side: keeps {@code Flowers} in step with the moobloom's state. Not showing flowers -> none. Showing
     * them -> one per position from the variant's pool; with {@code reroll} every position is rolled fresh,
     * otherwise only positions that are missing or hold a flower the pool no longer has.
     */
    public void refreshFlowers(boolean reroll) {
        if (level.isClientSide) return;
        if (!showsFlowers()) {
            if (!entityData.get(DATA_FLOWERS).isEmpty()) entityData.set(DATA_FLOWERS, "");
            return;
        }
        MoobloomVariant variant = getVariant();
        List<BlockState> current = getFlowers();
        List<BlockState> next = new ArrayList<>(MoobloomVariant.Slot.COUNT);
        for (int i = 0; i < MoobloomVariant.Slot.COUNT; i++) {
            BlockState state = !reroll && i < current.size() ? current.get(i) : null;
            next.add(state != null && variant.entryFor(state) != null ? state : variant.roll(random).state());
        }
        if (!next.equals(current)) setFlowers(next);
    }

    /** A calf growing up rolls its flowers; anything turned back into a calf loses them. */
    @Override
    protected void ageBoundaryReached() {
        super.ageBoundaryReached();
        refreshFlowers(false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ResourceLocation id = getVariantId();
        if (id != null) tag.putString("Variant", id.toString());
        tag.putBoolean("Sheared", isSheared());
        ListTag flowerList = new ListTag();
        for (BlockState state : getFlowers()) flowerList.add(StringTag.valueOf(BlockStateParser.serialize(state)));
        tag.put("Flowers", flowerList);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Variant", 8)) {
            ResourceLocation id = ResourceLocation.tryParse(tag.getString("Variant"));
            if (MoobloomVariants.has(level.isClientSide, id)) {
                entityData.set(DATA_VARIANT, id.toString());
            } else if (!level.isClientSide && !MoobloomVariants.all(false).isEmpty()) {
                MoobloomVariant fallback = MoobloomVariants.getDefault(false);
                MoreMooblooms.LOGGER.warn("[more_mooblooms] moobloom {} has unknown variant '{}', now {}",
                        getUUID(), tag.getString("Variant"), fallback.id());
                setVariant(fallback);
            } else if (id != null) {
                entityData.set(DATA_VARIANT, id.toString());
            }
        } else if (!level.isClientSide && getVariantId() == null) {
            // /summon with NBT skips finalizeSpawn, so nothing else would ever pick one
            setVariant(MoobloomVariants.pickWeighted(false, random));
        }
        if (tag.contains("Sheared")) setSheared(tag.getBoolean("Sheared"));
        if (tag.contains("Flowers", 9)) {
            // /summon and egg NBT may set them too (handy for tests); a bad state is re-rolled below
            List<BlockState> states = new ArrayList<>();
            ListTag list = tag.getList("Flowers", 8);
            for (int i = 0; i < list.size(); i++) {
                try {
                    states.add(MoobloomVariant.parseState(list.getString(i)));
                } catch (RuntimeException e) {
                    states.add(Blocks.AIR.defaultBlockState());
                }
            }
            setFlowers(states);
        }
        refreshFlowers(false);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData groupData, @Nullable CompoundTag tag) {
        // A spawn egg's EntityTag (Variant, Sheared) is applied after this, so a plain egg ends up random. A
        // natural spawn already has its variant from checkSpawnRules.
        if (getVariantId() == null) setVariant(MoobloomVariants.pickWeighted(false, random));
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, groupData, tag);
        refreshFlowers(false);
        return data;
    }

    /** "Rosebloom" etc. (WTHIT, death messages, /summon feedback). */
    @Override
    protected Component getTypeName() {
        return Component.translatable(getVariant().translationKey());
    }

    @Override
    public ItemStack getPickResult() {
        return MoobloomSpawnEggItem.forVariant(getVariant().id());
    }

    // ---- goals, grazing, food ----

    @Override
    protected void registerGoals() {
        eatBlockGoal = new EatBlockGoal(this);
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 2.0));
        goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        goalSelector.addGoal(3, new TemptGoal(this, 1.25, new FlowerIngredient(this), false));
        goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
        goalSelector.addGoal(5, eatBlockGoal);
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    /** Tempting checks the moobloom's current variant (it can change after the goal is built). */
    private static final class FlowerIngredient extends Ingredient {
        private final Moobloom owner;

        FlowerIngredient(Moobloom owner) {
            super(Stream.empty());
            this.owner = owner;
        }

        @Override
        public boolean test(@Nullable ItemStack stack) {
            return stack != null && owner.isFood(stack);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    /**
     * Natural spawns (a biome modifier adding mooblooms directly, not a swapped cow): the spawner calls this on the
     * new, positioned moobloom before {@code finalizeSpawn}, so the variant is picked here, from the biome's rule
     * (else weighted random). A block the variant lists in {@code spawns_in_dark_on} passes with no grass or light;
     * anything else needs the cow's rules. The type-level placement check (MMEntities) let every variant's dark
     * blocks through, so a soul block fails here for every variant but the witherbloom.
     */
    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType reason) {
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) return super.checkSpawnRules(level, reason);
        if (getVariantId() == null) {
            MoobloomVariant v = MoobloomSpawns.directVariant(level.getBiome(blockPosition()), random);
            setVariant(v != null ? v : MoobloomVariants.pickWeighted(false, random));
        }
        BlockState below = level.getBlockState(blockPosition().below());
        if (getVariant().spawnsInDarkOn(below)) return true;
        return below.is(BlockTags.ANIMALS_SPAWNABLE_ON) && super.checkSpawnRules(level, reason);
    }

    /** A variant's dark spawn blocks score like grass for it (where it likes to wander). */
    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return getVariant().spawnsInDarkOn(level.getBlockState(pos.below())) ? 10.0F : super.getWalkTargetValue(pos, level);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return getVariant().isFlower(stack);
    }

    @Override
    protected void customServerAiStep() {
        eatAnimationTick = eatBlockGoal.getEatAnimationTick();
        super.customServerAiStep();
    }

    @Override
    public void aiStep() {
        if (level.isClientSide) eatAnimationTick = Math.max(0, eatAnimationTick - 1);
        super.aiStep();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_EAT) {
            eatAnimationTick = 40;
        } else {
            super.handleEntityEvent(id);
        }
    }

    /** 0..1: how far the head is lowered (the sheep's grazing animation timing). */
    public float getHeadEatPositionScale(float partialTick) {
        if (eatAnimationTick <= 0) return 0.0F;
        if (eatAnimationTick >= 4 && eatAnimationTick <= 36) return 1.0F;
        return eatAnimationTick < 4
                ? (eatAnimationTick - partialTick) / 4.0F
                : -(eatAnimationTick - 40 - partialTick) / 4.0F;
    }

    /** Head pitch while grazing: tilted down with a small chewing wobble; else the normal look pitch. */
    public float getHeadEatAngleScale(float partialTick) {
        if (eatAnimationTick > 4 && eatAnimationTick <= 36) {
            float t = (eatAnimationTick - 4 - partialTick) / 32.0F;
            return Mth.PI / 5 + 0.22F * Mth.sin(t * 28.7F);
        }
        return eatAnimationTick > 0 ? Mth.PI / 5 : getXRot() * Mth.DEG_TO_RAD;
    }

    public boolean isGrazing() {
        return eatAnimationTick > 0;
    }

    /** Called by {@link EatBlockGoal} after eating grass: maybe regrow, and babies grow up faster (like lambs). */
    @Override
    public void ate() {
        super.ate();
        if (isSheared() && random.nextDouble() < MMConfig.num(MMConfig.REGROW_CHANCE)) {
            setSheared(false);
            refreshFlowers(true); // a fresh roll (D46)
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY(1.0), getZ(), 5, 0.4, 0.2, 0.4, 0.0);
            }
        }
        if (isBaby()) ageUp(60);
    }

    // ---- breeding ----

    /** The calf's variant: a combination rule, else breeds true, else random by breed_weight (4.8). */
    @Override
    public Moobloom getBreedOffspring(ServerLevel level, AgeableMob other) {
        Moobloom child = MMEntities.MOOBLOOM.get().create(level);
        if (child != null) {
            MoobloomVariant mate = other instanceof Moobloom m ? m.getVariant() : getVariant();
            child.setVariant(MoobloomBreeding.childOf(getVariant(), mate, random));
        }
        return child;
    }

    // ---- shearing ----

    @Override
    public boolean readyForShearing() {
        return isAlive() && !isBaby() && (!isSheared() || MMConfig.on(MMConfig.SHEAR_BARE_TO_COW));
    }

    @Override
    public boolean isShearable(@NotNull ItemStack item, Level level, BlockPos pos) {
        return readyForShearing();
    }

    /** Forge path: player shears and Create deployers. The caller spawns the returned drops. */
    @Override
    public @NotNull List<ItemStack> onSheared(@Nullable Player player, @NotNull ItemStack item, Level level,
                                              BlockPos pos, int fortune) {
        gameEvent(GameEvent.SHEAR, player);
        return shearInternal(player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS);
    }

    /** Vanilla path: dispensers. */
    @Override
    public void shear(SoundSource source) {
        for (ItemStack stack : shearInternal(source)) {
            level.addFreshEntity(new ItemEntity(level, getX(), getY(1.0), getZ(), stack));
        }
    }

    private List<ItemStack> shearInternal(SoundSource source) {
        level.playSound(null, this, SoundEvents.MOOSHROOM_SHEAR, source, 1.0F, 1.0F);
        if (level.isClientSide) return Collections.emptyList();
        if (!isSheared()) {
            List<ItemStack> drops = grownFlowerDrops();
            setSheared(true);
            refreshFlowers(false);
            return drops;
        }
        List<ItemStack> drops = MMConfig.on(MMConfig.BARE_SHEAR_DROPS) ? poolDrops() : Collections.emptyList();
        ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, getX(), getY(0.5), getZ(), 1, 0.0, 0.0, 0.0, 0.0);
        Cow cow = EntityType.COW.create(level);
        if (cow != null) replaceWith(this, cow);
        return drops;
    }

    /** First shear: one of each flower it is carrying, at that pool entry's chance. */
    private List<ItemStack> grownFlowerDrops() {
        MoobloomVariant variant = getVariant();
        List<ItemStack> out = new ArrayList<>();
        for (BlockState state : getFlowers()) {
            MoobloomVariant.Flower entry = variant.entryFor(state);
            ItemStack stack = entry != null ? entry.rollDrop(random) : new ItemStack(state.getBlock().asItem());
            if (!stack.isEmpty()) out.add(stack);
        }
        return merge(out);
    }

    /** Second (bare) shear: one weighted pool pick per position, each at its chance (D44). */
    private List<ItemStack> poolDrops() {
        MoobloomVariant variant = getVariant();
        List<ItemStack> out = new ArrayList<>();
        for (int i = 0; i < MoobloomVariant.Slot.COUNT; i++) {
            ItemStack stack = variant.roll(random).rollDrop(random);
            if (!stack.isEmpty()) out.add(stack);
        }
        return merge(out);
    }

    /** Same items into one stack, so two poppies drop as one item entity. */
    private static List<ItemStack> merge(List<ItemStack> stacks) {
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack stack : stacks) {
            ItemStack same = out.stream().filter(s -> ItemStack.isSameItemSameTags(s, stack)
                    && s.getCount() < s.getMaxStackSize()).findFirst().orElse(null);
            if (same != null) same.grow(stack.getCount());
            else out.add(stack.copy());
        }
        return out;
    }

    // ---- conversions ----

    /**
     * Turns {@code cow} into a grown moobloom of {@code variant} (feeding). The variant is set before the
     * moobloom joins the world, so clients never see a frame of the default variant.
     */
    @Nullable
    public static Moobloom fromCow(Cow cow, MoobloomVariant variant) {
        Moobloom moobloom = MMEntities.MOOBLOOM.get().create(cow.level);
        if (moobloom == null) return null;
        moobloom.setVariant(variant);
        moobloom.setSheared(false);
        moobloom.setAge(cow.getAge());
        moobloom.refreshFlowers(true);
        replaceWith(cow, moobloom);
        return moobloom;
    }

    /**
     * Replaces {@code from} with {@code to} in the world, carrying over what a player would notice: position,
     * rotation, health, age, name, persistence, invulnerability, no-AI, leash and vehicle. Like
     * {@code Mob.convertTo}, which doesn't keep health or the leash, and adds the entity before we can set data.
     */
    public static void replaceWith(Mob from, Mob to) {
        to.moveTo(from.getX(), from.getY(), from.getZ(), from.getYRot(), from.getXRot());
        to.yBodyRot = from.yBodyRot;
        to.yHeadRot = from.yHeadRot;
        to.setHealth(Math.min(from.getHealth(), to.getMaxHealth()));
        if (from instanceof AgeableMob a && to instanceof AgeableMob b) b.setAge(a.getAge());
        if (from.hasCustomName()) {
            to.setCustomName(from.getCustomName());
            to.setCustomNameVisible(from.isCustomNameVisible());
        }
        if (from.isPersistenceRequired()) to.setPersistenceRequired();
        to.setInvulnerable(from.isInvulnerable());
        to.setNoAi(from.isNoAi());
        ForgeEventFactory.onLivingConvert(from, to);
        from.level.addFreshEntity(to);
        Entity leashHolder = from.getLeashHolder();
        if (leashHolder != null) {
            from.dropLeash(true, false);
            to.setLeashedTo(leashHolder, true);
        }
        Entity vehicle = from.getVehicle();
        if (vehicle != null) {
            from.stopRiding();
            to.startRiding(vehicle, true);
        }
        from.discard();
    }
}
