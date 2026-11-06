package ce.shared;

import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base of the in-tree mods' mixin config plugins: a mixin applies only when the mod it serves is loaded, so a jar boots
 * the same without that mod. Two ways to name the mod, both optional:
 * <ul>
 *   <li>a compartment package: a mixin in {@code <compartmentPrefix><modid>.} needs {@code modid} (Creative Delight's
 *       form); a mod in {@code forVersion} only at that exact version (fix mixins for one jar would undo the upstream
 *       fix on a newer one; a mismatch logs a warning);</li>
 *   <li>a table: mixin simple name -> mod ids, applied when any of them is loaded.</li>
 * </ul>
 * A table mod may also have a tested version range ({@code modRange}, Maven range syntax as in {@code mods.toml}): a
 * loaded mod outside its range counts as absent, so its mixins are skipped (one warning per mod) instead of crashing the
 * boot on a changed target class.
 * Any other mixin always applies. Subclasses add {@code postApply} bytecode edits where needed. Shared source
 * (development/shared/mixinplugin, compiled into each jar under its own package; feature_writeup/pack_tooling.md).
 * The subclass needs a public no-argument constructor (Mixin creates it from the config's {@code plugin} entry).
 */
public abstract class GatedMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatedMixinPlugin.class);

    private final String logTag;
    private final String compartmentPrefix;
    private final Map<String, String> forVersion;
    private final Map<String, List<String>> needsMod;
    private final Map<String, String> modRange;
    private final Map<String, Boolean> compartmentOn = new HashMap<>();
    private final Map<String, Boolean> tableModOn = new HashMap<>();

    /**
     * @param logTag            prefix of the log lines, for example {@code [creativedelight]}
     * @param compartmentPrefix mixin package prefix with a trailing dot whose next segment is a mod id, or null
     * @param forVersion        compartment mod id -> the only version it applies to
     * @param needsMod          mixin simple name -> mod ids it serves (any one loaded and in range is enough)
     * @param modRange          table mod id -> tested version range (Maven syntax); a mod without a row has no range
     */
    protected GatedMixinPlugin(String logTag, String compartmentPrefix, Map<String, String> forVersion,
                               Map<String, List<String>> needsMod, Map<String, String> modRange) {
        this.logTag = logTag;
        this.compartmentPrefix = compartmentPrefix;
        this.forVersion = forVersion;
        this.needsMod = needsMod;
        this.modRange = modRange;
    }

    protected GatedMixinPlugin(String logTag, String compartmentPrefix, Map<String, String> forVersion, Map<String, List<String>> needsMod) {
        this(logTag, compartmentPrefix, forVersion, needsMod, Map.of());
    }

    /** Table-only form. */
    protected GatedMixinPlugin(String logTag, Map<String, List<String>> needsMod) {
        this(logTag, null, Map.of(), needsMod, Map.of());
    }

    /** Table-only form with tested version ranges. */
    protected GatedMixinPlugin(String logTag, Map<String, List<String>> needsMod, Map<String, String> modRange) {
        this(logTag, null, Map.of(), needsMod, modRange);
    }

    public static boolean isLoaded(String modId) {
        return LoadingModList.get().getModFileById(modId) != null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        List<String> modIds = needsMod.get(mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1));
        if (modIds != null) return modIds.stream().anyMatch(id -> tableModOn.computeIfAbsent(id, this::tableModOn));
        if (compartmentPrefix == null || !mixinClassName.startsWith(compartmentPrefix)) return true;
        String rest = mixinClassName.substring(compartmentPrefix.length());
        int dot = rest.indexOf('.');
        if (dot < 0) return true;
        return compartmentOn.computeIfAbsent(rest.substring(0, dot), this::compartmentOn);
    }

    private boolean tableModOn(String modId) {
        ModFileInfo file = LoadingModList.get().getModFileById(modId);
        if (file == null) return false;
        String spec = modRange.get(modId);
        if (spec == null) return true;
        ArtifactVersion have = file.getMods().stream().filter(m -> m.getModId().equals(modId)).findFirst()
                .orElse(file.getMods().get(0)).getVersion();
        if (inRange(spec, have)) return true;
        LOGGER.warn("{} {} {} is outside the tested range {}: its mixins are off", logTag, modId, have, spec);
        return false;
    }

    /** True when {@code version} is in the Maven range {@code spec}; a malformed spec counts as out of range. */
    public static boolean inRange(String spec, ArtifactVersion version) {
        try {
            return VersionRange.createFromVersionSpec(spec).containsVersion(version);
        } catch (InvalidVersionSpecificationException e) {
            LOGGER.error("bad mixin version range {}: {}", spec, e.getMessage());
            return false;
        }
    }

    private boolean compartmentOn(String modId) {
        ModFileInfo file = LoadingModList.get().getModFileById(modId);
        if (file == null) return false;
        String want = forVersion.get(modId);
        if (want == null) return true;
        String have = file.getMods().get(0).getVersion().toString();
        if (want.equals(have)) return true;
        LOGGER.warn("{} {} {} is not {}: its fix mixins are off (check the fixes against the new jar)", logTag, modId, have, want);
        return false;
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
