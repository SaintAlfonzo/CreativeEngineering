package com.SaintAlfonzo.creativecommon.damage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

/**
 * One immutable, fully-merged generation of the data-defined damage tags. {@link DamageTagLoader}
 * builds a fresh one on every data reload and {@link DamageTags} swaps it in, so a reload also
 * throws away the lookup cache that lives inside it.
 *
 * <p>A tag's members come from three places, all unioned:
 * <ul>
 *   <li>the built-in flag of the same name, if it is one ({@link DamageFlag});</li>
 *   <li>message ids listed in its data files - exact ({@code ieTesla}) or with {@code *}
 *       wildcards ({@code ieRevolver_*});</li>
 *   <li>other tags it {@code #includes}, built-in or data-defined. Includes may point in any
 *       direction, cycles included: resolution simply adds tags until nothing changes.</li>
 * </ul>
 */
final class DamageTagSnapshot {

    static final DamageTagSnapshot EMPTY = new DamageTagSnapshot(Collections.emptyMap());

    /** Message ids seen are a small closed set in practice; this only stops a mod that bakes a unique id into every source from growing the cache without bound. */
    private static final int CACHE_LIMIT = 4096;

    /** One pack's copy of one tag file, already parsed. */
    record Raw(String pack, boolean replace, List<String> values) {}

    /** A data-defined tag after every pack's files have been merged. */
    record Def(ResourceLocation id, Set<String> ids, List<Pattern> globs, Set<ResourceLocation> includes) {
        boolean matchesId(String msgId) {
            if (ids.contains(msgId)) {
                return true;
            }
            for (Pattern glob : globs) {
                if (glob.matcher(msgId).matches()) {
                    return true;
                }
            }
            return false;
        }

        int entryCount() {
            return ids.size() + globs.size() + includes.size();
        }
    }

    private record Key(String msgId, int mask) {}

    private final Map<ResourceLocation, Def> defs;
    private final List<Def> withIncludes;
    private final Map<Key, Set<ResourceLocation>> cache = new ConcurrentHashMap<>();

    private DamageTagSnapshot(Map<ResourceLocation, Def> defs) {
        this.defs = defs;
        List<Def> inc = new ArrayList<>();
        for (Def def : defs.values()) {
            if (!def.includes().isEmpty()) {
                inc.add(def);
            }
        }
        this.withIncludes = inc;
    }

    /**
     * Merges {@code raw} (per tag id: each pack's file, lowest priority first, as
     * {@code ResourceManager#listResourceStacks} returns them) into a snapshot. A file with
     * {@code "replace": true} discards whatever earlier packs contributed, as in vanilla tags.
     * Problems are reported through {@code warn}; none of them stops the load.
     */
    static DamageTagSnapshot build(Map<ResourceLocation, List<Raw>> raw, Consumer<String> warn) {
        Map<ResourceLocation, Def> defs = new TreeMap<>();
        for (Map.Entry<ResourceLocation, List<Raw>> entry : raw.entrySet()) {
            ResourceLocation id = entry.getKey();
            Set<String> ids = new LinkedHashSet<>();
            Map<String, Pattern> globs = new LinkedHashMap<>();
            Set<ResourceLocation> includes = new LinkedHashSet<>();

            for (Raw file : entry.getValue()) {
                if (file.replace()) {
                    ids.clear();
                    globs.clear();
                    includes.clear();
                }
                for (String value : file.values()) {
                    String v = value.trim();
                    if (v.isEmpty()) {
                        continue;
                    }
                    if (v.startsWith("#")) {
                        ResourceLocation inc = ResourceLocation.tryParse(v.substring(1));
                        if (inc == null) {
                            warn.accept(id + " (" + file.pack() + "): '" + v + "' is not a valid tag id - skipped");
                        } else if (inc.equals(id)) {
                            warn.accept(id + " (" + file.pack() + "): includes itself - skipped");
                        } else {
                            includes.add(inc);
                        }
                    } else if (v.indexOf('*') >= 0) {
                        globs.put(v, glob(v));
                    } else {
                        ids.add(v);
                    }
                }
            }
            defs.put(id, new Def(id, Collections.unmodifiableSet(ids),
                    Collections.unmodifiableList(new ArrayList<>(globs.values())),
                    Collections.unmodifiableSet(includes)));
        }

        for (Def def : defs.values()) {
            for (ResourceLocation inc : def.includes()) {
                if (!defs.containsKey(inc) && !DamageFlag.isFlagTag(inc) && namespaceLoaded(inc.getNamespace())) {
                    warn.accept(def.id() + " includes '#" + inc + "', which is neither a built-in flag tag nor defined by any pack - it will never match");
                }
            }
        }
        return new DamageTagSnapshot(Collections.unmodifiableMap(defs));
    }

    /**
     * False for a namespace whose mod is not installed: an include of another mod's tag (Common's fire tag includes
     * {@code #creativedelight:stoves}) is then expected to be undefined, not a typo. True when the mod list is not
     * available (offline harness), so the warning stays.
     */
    private static boolean namespaceLoaded(String namespace) {
        try {
            ModList mods = ModList.get();
            return mods == null || mods.isLoaded(namespace);
        } catch (LinkageError e) {
            return true;
        }
    }

    /** {@code ieRevolver_*} -> a regex where only {@code *} is special (any run of characters, including none). */
    private static Pattern glob(String glob) {
        StringBuilder regex = new StringBuilder();
        for (String part : glob.split("\\*", -1)) {
            if (regex.length() > 0) {
                regex.append(".*");
            }
            regex.append(Pattern.quote(part));
        }
        return Pattern.compile(regex.toString());
    }

    /** Every tag a source with this message id and flag mask belongs to. Never null, never modifiable. */
    Set<ResourceLocation> tagsFor(String msgId, int mask) {
        Key key = new Key(msgId == null ? "" : msgId, mask);
        Set<ResourceLocation> cached = cache.get(key);
        if (cached != null) {
            return cached;
        }

        Set<ResourceLocation> tags = new LinkedHashSet<>();
        DamageFlag.addTags(mask, tags);
        for (Def def : defs.values()) {
            if (def.matchesId(key.msgId())) {
                tags.add(def.id());
            }
        }
        if (!withIncludes.isEmpty()) {
            boolean changed;
            do {
                changed = false;
                for (Def def : withIncludes) {
                    if (tags.contains(def.id())) {
                        continue;
                    }
                    for (ResourceLocation inc : def.includes()) {
                        if (tags.contains(inc)) {
                            tags.add(def.id());
                            changed = true;
                            break;
                        }
                    }
                }
            } while (changed);
        }

        Set<ResourceLocation> frozen = Collections.unmodifiableSet(tags);
        if (cache.size() >= CACHE_LIMIT) {
            cache.clear();
        }
        cache.put(key, frozen);
        return frozen;
    }

    /** The data-defined tags (not the built-in flag ones), sorted by id. */
    Map<ResourceLocation, Def> defs() {
        return defs;
    }
}
