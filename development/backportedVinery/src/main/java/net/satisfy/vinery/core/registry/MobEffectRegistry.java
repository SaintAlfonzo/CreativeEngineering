package net.satisfy.vinery.core.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.effect.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Port: upstream keeps the effects behind ids and looks them up as holders; 1.19.2 has no holders, so the
 * {@link RegistryObject}s are kept by id and {@link #get(ResourceLocation)} answers {@link #getHolder} calls.
 */
public class MobEffectRegistry {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Vinery.MOD_ID);
    private static final Map<ResourceLocation, RegistryObject<MobEffect>> BY_ID = new HashMap<>();

    // B7.3: health_effect, luck_effect and resistance_effect are gone; minecraft:health_boost / luck / resistance cover them
    public static final ResourceLocation ARMOR_EFFECT = Vinery.identifier("armor_effect");
    public static final ResourceLocation EXPERIENCE_EFFECT = Vinery.identifier("experience_effect");
    public static final ResourceLocation IMPROVED_JUMP_BOOST = Vinery.identifier("double_jump");
    public static final ResourceLocation PARTY_EFFECT = Vinery.identifier("party_effect");
    public static final ResourceLocation TELEPORT = Vinery.identifier("teleport");
    public static final ResourceLocation CREEPER_EFFECT = Vinery.identifier("creeper_effect");
    public static final ResourceLocation CLIMBING_EFFECT = Vinery.identifier("climbing_effect");
    public static final ResourceLocation FROSTY_ARMOR_EFFECT = Vinery.identifier("frosty_armor");
    public static final ResourceLocation JELLIE = Vinery.identifier("jellie");
    public static final ResourceLocation LAVA_WALKER = Vinery.identifier("lava_walker");
    public static final ResourceLocation MAGNET = Vinery.identifier("magnet");
    public static final ResourceLocation WATER_WALKER = Vinery.identifier("water_walker");
    /** D3: 1.3.10's trippy (a client post shader) stays; the pack's echo "discombobulate" tool effect uses it. */
    public static final ResourceLocation TRIPPY = Vinery.identifier("trippy");

    static {
        register(ARMOR_EFFECT, () -> new VineryEffect(MobEffectCategory.BENEFICIAL, 0x56CBFD).withModifier(Attributes.ARMOR, "armor", 2.0).withModifier(Attributes.ARMOR_TOUGHNESS, "armor_toughness", 3.0));
        register(EXPERIENCE_EFFECT, () -> new VineryEffect(MobEffectCategory.BENEFICIAL, 0x00FF00));
        register(IMPROVED_JUMP_BOOST, () -> new VineryEffect(MobEffectCategory.BENEFICIAL, 0x00FF00));
        register(PARTY_EFFECT, () -> new VineryEffect(MobEffectCategory.BENEFICIAL, 0xFF0000));
        register(TELEPORT, TeleportEffect::new);
        register(CREEPER_EFFECT, CreeperEffect::new);
        register(CLIMBING_EFFECT, ClimbingEffect::new);
        register(FROSTY_ARMOR_EFFECT, FrostyArmorEffect::new);
        register(JELLIE, JellyEffect::new);
        register(LAVA_WALKER, LavaWalkerEffect::new);
        register(MAGNET, MagnetEffect::new);
        register(WATER_WALKER, WaterWalkerEffect::new);
        register(TRIPPY, () -> new VineryEffect(MobEffectCategory.BENEFICIAL, 0xB80070));
    }

    private static void register(ResourceLocation id, java.util.function.Supplier<MobEffect> effect) {
        BY_ID.put(id, EFFECTS.register(id.getPath(), effect));
    }

    public static MobEffect get(ResourceLocation id) {
        RegistryObject<MobEffect> effect = BY_ID.get(id);
        if (effect == null) {
            throw new IllegalArgumentException("MobEffect with id " + id + " does not exist");
        }
        return effect.get();
    }

    /** Upstream name; 1.19.2 has no holders. */
    public static MobEffect getHolder(ResourceLocation id) {
        return get(id);
    }
}
