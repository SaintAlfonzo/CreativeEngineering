package com.SaintAlfonzo.creativecommon.damage;

import java.util.Collections;
import java.util.Set;

import com.SaintAlfonzo.creativecommon.CreativeCommon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;

/**
 * Damage tags for MC 1.19.2, which has no damage-type registry (that arrived in 1.19.4) and so no
 * vanilla way to say "this hit is electric" or "these sources are fire". A tag is a plain
 * {@link ResourceLocation}; a {@link DamageSource} belongs to it when either
 * <ul>
 *   <li>it is a built-in flag tag and the source has that flag ({@link DamageFlag}: fire, magic,
 *       explosion, projectile, armor_piercing, ...), or</li>
 *   <li>its message id ({@link DamageSource#getMsgId()} - the {@code death.attack.<id>} key) is
 *       listed in a {@code data/<ns>/damage_tags/<name>.json} file.</li>
 * </ul>
 * Tag files use vanilla tag syntax ({@code "replace"}, {@code "values"}; {@code #ns:tag} includes)
 * plus {@code *} wildcards in ids, and merge across datapacks. They reload with {@code /reload}.
 * A hit usually carries several tags at once (a fire explosion is both); callers decide how to
 * combine them.
 *
 * <p>Lookups are cached per (message id, flag mask), so they are cheap enough to call on every hit,
 * and safe from any thread. Before the first data load only the built-in flag tags exist.
 */
public final class DamageTags {

    public static final String NAMESPACE = CreativeCommon.NS;

    private static volatile DamageTagSnapshot snapshot = DamageTagSnapshot.EMPTY;

    private DamageTags() {}

    /** {@code creativecommon:<path>}. */
    public static ResourceLocation id(String path) {
        return new ResourceLocation(NAMESPACE, path);
    }

    /** Every tag {@code source} belongs to, built-in flags first. Never null; unmodifiable. */
    public static Set<ResourceLocation> tagsOf(DamageSource source) {
        return snapshot.tagsFor(source.getMsgId(), DamageFlag.mask(source));
    }

    public static boolean is(DamageSource source, ResourceLocation tag) {
        return tagsOf(source).contains(tag);
    }

    /** Data-defined tags currently loaded, sorted by id (the built-in flag tags are not listed unless a file also defines them). */
    public static Set<ResourceLocation> definedTags() {
        return Collections.unmodifiableSet(snapshot.defs().keySet());
    }

    /** Called by {@link DamageTagLoader} after a data reload. */
    static void install(DamageTagSnapshot next) {
        snapshot = next;
    }
}
