// Ported from Tempad 3.0.4 (MIT, Terrarium), src/main/kotlin/earth/terrarium/tempad/common/registries/ModEntities.kt. Translated to Java for Forge 1.19.2:
// the entity data serializers register in Forge's ENTITY_DATA_SERIALIZERS registry.
package earth.terrarium.tempad.common.registries;

import earth.terrarium.tempad.Tempad;
import earth.terrarium.tempad.api.sizing.TimedoorPlacementSettings;
import earth.terrarium.tempad.common.entity.TimedoorEntity;
import earth.terrarium.tempad.common.utils.CodecUtils;
import earth.terrarium.tempad.common.utils.Color;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class ModEntities {
    public static final DeferredRegister<EntityDataSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, Tempad.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Tempad.MOD_ID);

    public static final EntityDataSerializer<Color> COLOR_SERIALIZER = serializer((buf, color) -> color.write(buf), Color::read);
    public static final EntityDataSerializer<Vec3> VEC3_SERIALIZER = serializer(CodecUtils::writeVec3, CodecUtils::readVec3);
    public static final EntityDataSerializer<ResourceKey<Level>> DIMENSION_KEY_SERIALIZER = serializer(CodecUtils::writeDimension, CodecUtils::readDimension);
    public static final EntityDataSerializer<TimedoorPlacementSettings> SIZING_SERIALIZER = serializer(TimedoorPlacementSettings::write, TimedoorPlacementSettings::read);

    static {
        SERIALIZERS.register("color", () -> COLOR_SERIALIZER);
        SERIALIZERS.register("vec3", () -> VEC3_SERIALIZER);
        SERIALIZERS.register("dimension_key", () -> DIMENSION_KEY_SERIALIZER);
        SERIALIZERS.register("sizing", () -> SIZING_SERIALIZER);
    }

    public static final RegistryObject<EntityType<TimedoorEntity>> timedoor = ENTITIES.register("timedoor",
            () -> EntityType.Builder.<TimedoorEntity>of(TimedoorEntity::new, MobCategory.MISC).build(Tempad.id("timedoor").toString()));

    private ModEntities() {
    }

    private static <T> EntityDataSerializer<T> serializer(BiConsumer<FriendlyByteBuf, T> writer, Function<FriendlyByteBuf, T> reader) {
        return new EntityDataSerializer<>() {
            @Override
            public void write(FriendlyByteBuf buf, T value) {
                writer.accept(buf, value);
            }

            @Override
            public T read(FriendlyByteBuf buf) {
                return reader.apply(buf);
            }

            @Override
            public T copy(T value) {
                return value;
            }
        };
    }
}
