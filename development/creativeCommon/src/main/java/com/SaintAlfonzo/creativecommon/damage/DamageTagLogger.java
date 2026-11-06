package com.SaintAlfonzo.creativecommon.damage;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.SaintAlfonzo.creativecommon.CommonConfig;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import org.slf4j.Logger;

/**
 * Discovery aid, off by default ({@code [damage_tags] logSeenSources} in the common config). While
 * on, the first time each distinct (message id, flag set) hurts any living entity on the server it
 * is written to the log once:
 * <pre>
 * [damage-tags] seen msgId='ieTesla' flags=armor_piercing tags=electric attacker=... direct=... victim=... amount=4.0
 * </pre>
 * {@code tags=NONE} means no data-defined tag claims that message id - only its built-in flags
 * classify it - which is exactly the list of sources still worth a tag file. Grep the log for
 * {@code [damage-tags] seen}. The "already seen" memory is per session, so a restart logs
 * everything again. {@link LivingAttackEvent} fires at the very start of {@code hurt()}, before
 * shields or armour touch the hit, and {@code receiveCanceled} keeps this listener running even
 * when another mod (Immersive Engineering's shield upgrades) cancels the event first.
 */
public final class DamageTagLogger {

    private static final Logger LOGGER = LogUtils.getLogger();

    private record Seen(String msgId, int mask) {}

    private static final Set<Seen> SEEN = ConcurrentHashMap.newKeySet();

    private DamageTagLogger() {}

    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void onAttack(LivingAttackEvent event) {
        if (!enabled()) {
            return;
        }
        LivingEntity victim = event.getEntity();
        if (victim.level.isClientSide) {
            return;
        }

        DamageSource source = event.getSource();
        int mask = DamageFlag.mask(source);
        if (!SEEN.add(new Seen(source.getMsgId(), mask))) {
            return;
        }

        Set<ResourceLocation> dataTags = new LinkedHashSet<>();
        for (ResourceLocation tag : DamageTags.tagsOf(source)) {
            if (!DamageFlag.isFlagTag(tag)) {
                dataTags.add(tag);
            }
        }
        LOGGER.info("[damage-tags] seen msgId='{}' flags={} tags={} attacker={} direct={} victim={} amount={}",
                source.getMsgId(), DamageFlag.describe(mask), describe(dataTags),
                typeOf(source.getEntity()), typeOf(source.getDirectEntity()), typeOf(victim), event.getAmount());
    }

    private static boolean enabled() {
        try {
            return CommonConfig.DAMAGE_TAGS.logSeenSources.get();
        } catch (RuntimeException notLoaded) {
            return false;
        }
    }

    private static String describe(Set<ResourceLocation> tags) {
        if (tags.isEmpty()) {
            return "NONE";
        }
        StringBuilder sb = new StringBuilder();
        for (ResourceLocation tag : tags) {
            sb.append(sb.length() == 0 ? "" : ",")
                    .append(DamageTags.NAMESPACE.equals(tag.getNamespace()) ? tag.getPath() : tag.toString());
        }
        return sb.toString();
    }

    private static String typeOf(@Nullable Entity entity) {
        if (entity == null) {
            return "-";
        }
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        return key == null ? entity.getClass().getSimpleName() : key.toString();
    }
}
