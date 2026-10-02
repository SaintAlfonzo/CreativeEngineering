package io.github.mortuusars.exposure;

import com.mojang.brigadier.arguments.ArgumentType;
import io.github.mortuusars.exposure.backport.component.DataComponentType;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

/**
 * Upstream {@code Register} (an Architectury {@code @ExpectPlatform} facade) with the NeoForge {@code RegisterImpl}
 * folded in, as Forge 1.19.2 code.
 * <p>
 * 1.19.2 differences: creative tabs, criterion triggers and data components have no registry. Tabs and triggers
 * are created right away ({@link CreativeModeTab} registers itself in its constructor, triggers go through
 * {@link CriteriaTriggers#register}); data components are plain objects (see {@link DataComponentType}).
 */
public class Register {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Exposure.ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Exposure.ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Exposure.ID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Exposure.ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Exposure.ID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Exposure.ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Exposure.ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Exposure.ID);
    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, Exposure.ID);
    public static final DeferredRegister<Feature<?>> WORLD_GEN_FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, Exposure.ID);
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, Exposure.ID);
    public static final DeferredRegister<ResourceLocation> CUSTOM_STATS = DeferredRegister.create(Registry.CUSTOM_STAT_REGISTRY, Exposure.ID);

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        ITEMS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        COMMAND_ARGUMENT_TYPES.register(modEventBus);
        WORLD_GEN_FEATURES.register(modEventBus);
        PARTICLE_TYPES.register(modEventBus);
        CUSTOM_STATS.register(modEventBus);
    }

    public static <T extends Block> Supplier<T> block(String id, Supplier<T> supplier) {
        return BLOCKS.register(id, supplier);
    }

    public static <T extends BlockEntityType<E>, E extends BlockEntity> Supplier<T> blockEntityType(String id, Supplier<T> sup) {
        return BLOCK_ENTITY_TYPES.register(id, sup);
    }

    @SuppressWarnings("DataFlowIssue")
    public static <T extends BlockEntity> BlockEntityType<T> newBlockEntityType(BlockEntitySupplier<T> blockEntitySupplier, Block... validBlocks) {
        return BlockEntityType.Builder.of(blockEntitySupplier::create, validBlocks).build(null);
    }

    public static <T extends Item> Supplier<T> item(String id, Supplier<T> supplier) {
        return ITEMS.register(id, supplier);
    }

    /** 1.19.2 tabs aren't registered: the tab is created now and adds itself to {@link CreativeModeTab#TABS}. */
    public static <T extends CreativeModeTab> Supplier<T> creativeTab(String id, Supplier<T> supplier) {
        T tab = supplier.get();
        return () -> tab;
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id, EntityType.EntityFactory<T> factory, MobCategory category, float width, float height, int clientTrackingRange, boolean velocityUpdates, int updateInterval) {
        return ENTITY_TYPES.register(id, () -> EntityType.Builder.of(factory, category).sized(width, height)
                .clientTrackingRange(clientTrackingRange).setShouldReceiveVelocityUpdates(velocityUpdates)
                .updateInterval(updateInterval).build(id));
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id, EntityType.EntityFactory<T> factory, MobCategory category, boolean receiveVelocityUpdates, Consumer<EntityType.Builder<T>> typeBuilder) {
        return ENTITY_TYPES.register(id, () -> {
            EntityType.Builder<T> builder = EntityType.Builder.of(factory, category);
            builder.setShouldReceiveVelocityUpdates(receiveVelocityUpdates);
            typeBuilder.accept(builder);
            return builder.build(id);
        });
    }

    public static <T extends SoundEvent> Supplier<T> soundEvent(String id, Supplier<T> supplier) {
        return SOUND_EVENTS.register(id, supplier);
    }

    public static <T extends AbstractContainerMenu> Supplier<MenuType<T>> menuType(String id, MenuTypeSupplier<T> supplier) {
        return MENU_TYPES.register(id, () -> IForgeMenuType.create(supplier::create));
    }

    public static <T extends Recipe<?>> Supplier<RecipeType<T>> recipeType(String name, Supplier<RecipeType<T>> supplier) {
        return RECIPE_TYPES.register(name, supplier);
    }

    public static Supplier<RecipeSerializer<?>> recipeSerializer(String name, Supplier<RecipeSerializer<?>> supplier) {
        return RECIPE_SERIALIZERS.register(name, supplier);
    }

    /**
     * Triggers have no registry in 1.19.2 (they carry their own id): created now, handed to vanilla's
     * {@link CriteriaTriggers#register} by {@link #registerCriteriaTriggers} on the main thread in common setup
     * (mod constructors run in parallel and vanilla's trigger map isn't thread-safe).
     */
    public static <T extends CriterionTrigger<?>> Supplier<T> criterionTrigger(String name, Supplier<T> supplier) {
        T trigger = supplier.get();
        PENDING_CRITERIA_TRIGGERS.add(trigger);
        return () -> trigger;
    }

    private static final java.util.List<CriterionTrigger<?>> PENDING_CRITERIA_TRIGGERS = new java.util.ArrayList<>();

    public static void registerCriteriaTriggers() {
        PENDING_CRITERIA_TRIGGERS.forEach(CriteriaTriggers::register);
        PENDING_CRITERIA_TRIGGERS.clear();
    }

    public static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>> Supplier<ArgumentTypeInfo<A, T>> commandArgumentType(String id, Class<A> infoClass, I argumentTypeInfo) {
        return COMMAND_ARGUMENT_TYPES.register(id, () -> ArgumentTypeInfos.registerByClass(infoClass, argumentTypeInfo));
    }

    public static <T extends FeatureConfiguration> Supplier<Feature<?>> worldGenFeature(String name, Supplier<Feature<T>> featureSupplier) {
        return WORLD_GEN_FEATURES.register(name, featureSupplier);
    }

    public static <T> DataComponentType<T> dataComponentType(String name, Consumer<DataComponentType.Builder<T>> builderConsumer) {
        DataComponentType.Builder<T> builder = DataComponentType.builder();
        builderConsumer.accept(builder);
        return builder.build(Exposure.resource(name));
    }

    public static <T extends ParticleType<? extends ParticleOptions>> Supplier<T> particleType(String name, Supplier<T> supplier) {
        return PARTICLE_TYPES.register(name, supplier);
    }

    @FunctionalInterface
    public interface BlockEntitySupplier<T extends BlockEntity> {
        @NotNull T create(BlockPos pos, BlockState state);
    }

    @FunctionalInterface
    public interface MenuTypeSupplier<T extends AbstractContainerMenu> {
        @NotNull T create(int containerId, Inventory playerInventory, FriendlyByteBuf buffer);
    }
}
