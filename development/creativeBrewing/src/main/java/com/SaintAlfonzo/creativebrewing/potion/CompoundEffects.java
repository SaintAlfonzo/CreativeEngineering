package com.SaintAlfonzo.creativebrewing.potion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Effects that are compounds of other effects ({@code potions/effects #10}, feature_writeup/effects.md 2.7): the parts are
 * listed under "When Applied:" in item tooltips ({@code potion/CompoundEffectTooltip}), the JEI effect hover and the JEED
 * description ({@code mixin/JeedEffectRendererMixin}, {@code mixin/JeedEffectInfoMixin}). Text only, except Caffeinated's
 * Phantom Bane, which {@link PhantomBaneRules} really applies. A part is another effect (named, with the compound's level
 * when {@code levelled}) or a lang key for behaviour no effect covers.
 */
public final class CompoundEffects {

    /** One part: an effect id, or a lang key ({@code effect == null}). */
    public record Part(@Nullable ResourceLocation effect, @Nullable String textKey, boolean levelled) {
        static Part effect(String id, boolean levelled) {
            return new Part(new ResourceLocation(id), null, levelled);
        }

        static Part text(String key) {
            return new Part(null, "effect.creativebrewing.part." + key, false);
        }
    }

    private static final Map<ResourceLocation, List<Part>> PARTS = new LinkedHashMap<>();

    static {
        // Caffeinated: +10% speed and +20% attack speed are its own modifiers (listed by the game already).
        add("farmersrespite:caffeinated", Part.effect("better_beacons:phantom_bane", false));
        // Stimulation clears Slowness and Mining Fatigue each tick; Sweetening / Refreshing refuse them.
        add("create_confectionery:stimulation",
                Part.effect("fruitsdelight:refreshing", false), Part.effect("fruitsdelight:sweetening", false));
        add("fruitsdelight:recovering", Part.effect("alexsmobs:poison_resistance", false), Part.text("wither_immunity"));
        add("vinery:double_jump", Part.effect("minecraft:jump_boost", true), Part.text("mid_air_jump"));
        add("immersiveengineering:flashed", Part.effect("minecraft:slowness", false), Part.text("screen_flash"));
        add("alexsmobs:sunbird_blessing", Part.effect("minecraft:slow_falling", false), Part.text("elytra_lift"));
    }

    private CompoundEffects() {}

    private static void add(String compound, Part... parts) {
        PARTS.put(new ResourceLocation(compound), List.of(parts));
    }

    /** The parts of {@code effect}, empty when it isn't a compound. */
    public static List<Part> parts(MobEffect effect) {
        ResourceLocation id = ForgeRegistries.MOB_EFFECTS.getKey(effect);
        List<Part> parts = id == null ? null : PARTS.get(id);
        return parts == null ? List.of() : parts;
    }

    /** One tooltip line per part of {@code instance}'s effect, coloured by the part's category (blue for text parts). */
    public static List<Component> partLines(MobEffectInstance instance) {
        List<Component> lines = new ArrayList<>();
        for (Part part : parts(instance.getEffect())) {
            Component name = partName(part, instance.getAmplifier());
            if (name != null) lines.add(name);
        }
        return lines;
    }

    /** "Part, Part" for the JEED description; null when the effect isn't a compound. */
    @Nullable
    public static Component describe(MobEffect effect) {
        List<Part> parts = parts(effect);
        if (parts.isEmpty()) return null;
        MutableComponent list = Component.empty();
        boolean first = true;
        for (Part part : parts) {
            Component name = partName(part, 0);
            if (name == null) continue;
            if (!first) list.append(", ");
            list.append(Component.literal(name.getString()));
            first = false;
        }
        return Component.translatable("effect.creativebrewing.compound.parts", list);
    }

    @Nullable
    private static Component partName(Part part, int amplifier) {
        if (part.effect() == null) {
            return Component.translatable(part.textKey()).withStyle(ChatFormatting.BLUE);
        }
        MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(part.effect());
        if (effect == null) return null;
        MutableComponent name = effect.getDisplayName().copy();
        if (part.levelled() && amplifier > 0) {
            name = Component.translatable("potion.withAmplifier", name, Component.translatable("potion.potency." + amplifier));
        }
        return name.withStyle(effect.getCategory().getTooltipFormatting());
    }

    /**
     * Inserts the parts of every compound in {@code effects} under {@code tooltip}'s "When Applied:" header, adding the
     * header (after a blank line) at {@code fallbackIndex} when there is none.
     */
    public static void insert(List<Component> tooltip, List<MobEffectInstance> effects, int fallbackIndex) {
        List<Component> lines = new ArrayList<>();
        for (MobEffectInstance instance : effects) {
            lines.addAll(partLines(instance));
        }
        if (lines.isEmpty()) return;
        int header = -1;
        for (int i = 0; i < tooltip.size(); i++) {
            if (tooltip.get(i).getContents() instanceof TranslatableContents t && "potion.whenDrank".equals(t.getKey())) {
                header = i;
                break;
            }
        }
        if (header >= 0) {
            tooltip.addAll(header + 1, lines);
            return;
        }
        int at = Math.max(0, Math.min(fallbackIndex, tooltip.size()));
        List<Component> block = new ArrayList<>();
        block.add(Component.empty());
        block.add(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));
        block.addAll(lines);
        tooltip.addAll(at, block);
    }
}
