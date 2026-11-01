package net.satisfy.vinery.core.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.satisfy.vinery.core.Vinery;

public class VineryEffect extends MobEffect {
    public VineryEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    public VineryEffect withModifier(Attribute attribute, String id, double amount) {
        addAttributeModifier(attribute, uuid(Vinery.identifier(id)), amount, AttributeModifier.Operation.ADDITION);
        return this;
    }

    /** Port (P6): 1.19.2 attribute modifiers are keyed by UUID; one stable UUID per upstream id. */
    public static String uuid(net.minecraft.resources.ResourceLocation id) {
        return java.util.UUID.nameUUIDFromBytes(id.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString();
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
