package com.SaintAlfonzo.creativedelight.barbeques.item;

import com.SaintAlfonzo.creativedelight.barbeques.BBQLang;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * The six seasonings of 1.3.0, values unchanged (barbeques_delight.md 3.8). One per skewer, stored on the stack as the
 * string tag {@value #TAG} (1.3.0's {@code seasoning} data component). Marked for the pack seasoning fold
 * ({@code food/seasoning #11}); until then this is upstream's own mechanic.
 */
public enum BBQSeasoning {
    CUMIN(BBQLang.CUMIN, BBQLang.CUMIN_INFO, ChatFormatting.YELLOW, true),
    PEPPER(BBQLang.PEPPER, BBQLang.PEPPER_INFO, ChatFormatting.GRAY, true),
    CHILI(BBQLang.CHILI, BBQLang.CHILI_INFO, ChatFormatting.RED, true),
    HONEY_MUSTARD(BBQLang.HONEY, BBQLang.HONEY_INFO, ChatFormatting.YELLOW, false),
    BUFFALO(BBQLang.BUFFALO, BBQLang.BUFFALO_INFO, ChatFormatting.GOLD, false),
    BARBEQUE(BBQLang.BBQ, BBQLang.BBQ_INFO, ChatFormatting.GOLD, false);

    public static final String TAG = "BBQSeasoning";

    private final BBQLang title;
    private final BBQLang info;
    public final ChatFormatting color;
    /** Powder (64 uses, allowed on raw skewers) or sauce (16 uses, cooked skewers only: O28). */
    public final boolean powder;

    BBQSeasoning(BBQLang title, BBQLang info, ChatFormatting color, boolean powder) {
        this.title = title;
        this.info = info;
        this.color = color;
        this.powder = powder;
    }

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String itemId() {
        return getName() + (powder ? "_powder" : "_sauce");
    }

    @Nullable
    public static BBQSeasoning get(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG)) {
            return null;
        }
        String name = tag.getString(TAG);
        for (BBQSeasoning s : values()) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    public void set(ItemStack stack) {
        stack.getOrCreateTag().putString(TAG, getName());
    }

    public MutableComponent getTitle() {
        return title.get().withStyle(color);
    }

    public MutableComponent getInfo() {
        return info.get().withStyle(color);
    }

    /** On-eat actions: chili and buffalo burn for 2, cumin heals 2. */
    public void onFinish(Player player) {
        if (this == CHILI || this == BUFFALO) {
            player.hurt(DamageSource.IN_FIRE, 2.0F);
        }
        if (this == CUMIN) {
            player.heal(2.0F);
        }
    }

    public void modify(FoodProperties.Builder builder) {
        if (this == CHILI) {
            builder.alwaysEat();
        }
        if (this == PEPPER) {
            builder.fast();
        }
    }

    /** Sauces rewrite each of the food's own effects; powders leave them as they are. */
    public void appendEffect(FoodProperties.Builder builder, MobEffectInstance ins, float chance) {
        int dur = ins.getDuration();
        int amp = ins.getAmplifier();
        if (this == HONEY_MUSTARD) {
            chance *= 2.0F;
            dur /= 2;
        } else if (this == BUFFALO) {
            dur /= 2;
            amp++;
        } else if (this == BARBEQUE) {
            dur *= 2;
        }
        int finalDur = dur;
        int finalAmp = amp;
        builder.effect(() -> new MobEffectInstance(ins.getEffect(), finalDur, finalAmp), chance);
    }
}
