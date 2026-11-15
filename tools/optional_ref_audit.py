#!/usr/bin/env python3
"""Optional-mod reference audit (feature_writeup/plan_bugfix_1114.md 5.2; results in creative_split.md 7.21).

A plain lint: reads the in-tree sources and the installed jars, not the dumps, so it is not in generators.json.

For every data and asset JSON of our jars (development/*/src/main/resources, bundle members, Creative Delight's
generated packs/worldgen, and the instance kubejs/ trees each jar copies in, build.gradle scriptTrees / dataTrees) it collects the
namespaced ids, maps each namespace to its owning mod (installed jars' mods.toml and data/assets folders, plus
has.js's _modOf rows) and flags a reference to a mod the unit does not always have. "Always" = the unit's own mod ids
(bundle and members) plus the transitive mandatory dependencies from the installed mods.toml files.

A reference is guarded when it sits in:
  - a packs/<seg>[/...] split whose path segments (split on '+') name the mod (CompartmentPacks, packs/mods/<modid>/,
    packs/worldgen/<mod>/, Worldgen's packs/<mod>/);
  - an object whose "conditions" / "forge:conditions" hold {"type": "forge:mod_loaded", "modid": <mod>} (inside
    forge:and too; never inside forge:not);
  - an object with "requires": <mod> or a list (wild_crops.json rules and similar script data);
  - a tag entry {"id": ..., "required": false}.
Java: every "ns:path" or new ResourceLocation("ns", ...) literal of a foreign namespace in a file that never names
that mod id in an isLoaded / NEEDS_MOD / requires context is flagged as a registry lookup outside an isLoaded check.
Mixin classes (gated by their plugin tables, creative_split.md 7.16, 7.17) are skipped.

Instance files outside our namespaces (kubejs/data/<other>/) are the pack's own and may name any installed mod;
--pack also audits them for references to our optional jars (everything but Creative Common).

Severity (what an absent mod does): registry = worldgen / biome modifier / structure, the datapack fails to load;
loot = loot table or loot modifier fails to parse; tag = the tag fails ("missing references"); recipe = one recipe
skipped with an error; data = a mod-specific data folder; script = JSON a script reads; asset = missing model or
texture only.

Usage:
  python3 tools/optional_ref_audit.py            # report flagged references, grouped by file; exit 1 if any
                                                 # registry / loot / tag flag is left (2 = only lower ones)
  python3 tools/optional_ref_audit.py --all      # also list allowlisted and lower-severity hits in detail
  python3 tools/optional_ref_audit.py --pack     # also audit the instance's own kubejs/data and assets
  python3 tools/optional_ref_audit.py --json     # the flagged list as JSON (for fix scripts)
  python3 tools/optional_ref_audit.py --guarded  # list the optional mods that are referenced behind a guard
                                                 # (the SQ-DEP-09 drop-one set)
"""
import fnmatch
import io
import json
import os
import re
import sys
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DEV = os.path.join(ROOT, "development")
MODS = os.path.join(ROOT, "mods")
HAS_JS = os.path.join(DEV, "creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/has.js")

ALWAYS = {"minecraft", "forge", "c", "realms", "brigadier"}
# Namespaces one of our always-present mods also registers into (ARR overrides, memory arr-mod-overrides-route): the
# Ender's Delight Additions member registers its blocks and items as endersdelight:*. A reference into one counts as
# present for any unit that has the member (the upstream mod's own ids under it are then not checked).
NS_ALSO = {"endersdelight": {"enders_additions"}}
# Projects not audited: Power Grid is a gate-jar port (plan_power_grid.md) and reported only; the rest are not in the pack.
SKIP_PROJECTS = {"shared", "solRaspberryPie"}
REPORT_ONLY = {"backportedPowerGrid"}

# Explicit allowlist: (path prefix relative to ROOT, reason). Every entry is a triaged false positive or an accepted
# soft reference; keep the reason so a later session can drop the row when the reason goes.
ALLOW = [
    ("development/*/src/main/resources/data/*/swaps/",
     "id swap lists (old id -> new id, Creative Common swap loader): the old side is absent by design and the new side "
     "is resolved only when present"),
    ("development/creativeCommon/src/main/java/com/SaintAlfonzo/creativecommon/effect/PhantomBaneRules.java",
     "looks the effect up once and checks phantomBane != null at every use"),
    ("development/creativeDelight/src/main/java/com/SaintAlfonzo/creativedelight/compat/farmersrespite/CaffeinatedPhantomBane.java",
     "looks both effects up once and returns when either is null"),
    ("development/creativeDelight/src/main/resources/creativedelight/village_farms.json",
     "VillageFarmCrops looks each block up with ForgeRegistries.BLOCKS.containsKey and skips absent ones"),
    ("kubejs/data/creativedelight/kubejs/_worldgen/wild_crops.json",
     "script data: _biome_rules.js skips a feature or biome whose namespace is not loaded (Platform.isLoaded) and a rule "
     "whose requires is absent; the build splits the rules' own files by requires"),
    ("kubejs/data/creativeworldgen/kubejs/loot/loot_index.json",
     "script data: an index of loot table ids from the dump; 05_tuning.js only edits tables that exist"),
    ("development/creativeDelight/src/main/resources/creativedelight/soil_yield.json",
     "soil_yield.js reads it; a crop whose base is missing is skipped with a warning (NaN row)"),
    ("kubejs/data/creativedelight/worldgen/",
     "splitWorldgen build source (dev only, never deployed, prep_srv.sh drops it); the build splits it into "
     "packs/worldgen/<mod>/ and fails on an unrouted namespace, and the split output is audited instead"),
]
# Namespaces that are not mods: tag-only or id-shaped strings in our data.
NOT_MODS = {
    "c", "forge", "minecraft", "realms",
    # sound event / resource-pack style ids and text that look like ids
    "http", "https",
    # NeoForge data keys that upstream translators rewrite (Tempad, Exposure ports); not a mod in this pack
    "neoforge",
    # Loading Tips entry ids (Common's bundled resource pack), not a mod
    "loading_tips",
}

ID_RE = re.compile(r"^#?([a-z0-9_.\-]+):([a-z0-9_./\-]+)$")
JAVA_RL2 = re.compile(r'new\s+ResourceLocation\(\s*"([a-z0-9_.\-]+)"\s*,\s*"([a-z0-9_./\-]+)"\s*\)')
JAVA_RL1 = re.compile(r'"([a-z0-9_.\-]+):([a-z0-9_./\-]+)"')
RISKY_GET = re.compile(r"ForgeRegistries\.(MOB_EFFECTS|ENTITY_TYPES|POTIONS|RECIPE_SERIALIZERS|RECIPE_TYPES|ENCHANTMENTS|"
                       r"BLOCK_ENTITY_TYPES|MENU_TYPES|SOUND_EVENTS|ATTRIBUTES|PARTICLE_TYPES)\.getValue\(")
JAVA_REG = re.compile(r"ForgeRegistries\.|Registry\.[A-Z_]+\.(get|getOptional|containsKey)|BuiltinRegistries|"
                      r"registryAccess\(\)|getValue\(|ResourceLocation\(|TagKey\.create|ItemTags\.create|BlockTags\.create")


# ---------------------------------------------------------------- mods.toml reading (Python 3.10: no tomllib)

def parse_mods_toml(text):
    """Returns ({modId}, {modId: set(mandatory dep ids)}, {modId: set(optional dep ids)})."""
    ids, mand, opt = [], {}, {}
    section, cur = None, {}

    def flush():
        if section == "mods" and cur.get("modId"):
            ids.append(cur["modId"])
        elif section and section.startswith("dependencies.") and cur.get("modId"):
            owner = section[len("dependencies."):]
            m = cur.get("mandatory", "true").lower() == "true"
            if "type" in cur:
                m = cur["type"].lower() == "required"
            (mand if m else opt).setdefault(owner, set()).add(cur["modId"])

    for raw in text.splitlines():
        line = raw.split("#", 1)[0].strip() if not raw.strip().startswith("description") else raw.strip()
        h = re.match(r"^\[\[\s*([^\]]+?)\s*\]\]", line)
        if h:
            flush()
            section, cur = h.group(1).strip('"'), {}
            continue
        if re.match(r"^\[[^\[]", line):
            flush()
            section, cur = None, {}
            continue
        kv = re.match(r'''^([A-Za-z_]+)\s*=\s*["']?([^"']*)["']?''', line)
        if kv and section:
            cur[kv.group(1)] = kv.group(2).strip()
    flush()
    if not ids:
        # inline-table form: mods = [ { modId = 'x', ... } ]
        ids = re.findall(r"""modId\s*=\s*["']([^"']+)["']""", text.split("[[dependencies", 1)[0])
    return set(ids), mand, opt


def read_jar(data, mods, ns_jar, depth=0):
    try:
        z = zipfile.ZipFile(io.BytesIO(data))
    except zipfile.BadZipFile:
        return
    names = z.namelist()
    jar_ids = set()
    if "META-INF/mods.toml" in names:
        ids, mand, opt = parse_mods_toml(z.read("META-INF/mods.toml").decode("utf-8", "replace"))
        jar_ids = ids
        for i in ids:
            mods.setdefault(i, {"mand": set(), "opt": set()})
            mods[i]["mand"] |= mand.get(i, set())
            mods[i]["opt"] |= opt.get(i, set())
    nss = set()
    for n in names:
        p = n.split("/")
        if len(p) > 2 and p[0] in ("data", "assets"):
            nss.add(p[1])
    for ns in nss:
        ns_jar.setdefault(ns, []).append(sorted(jar_ids))
    if depth < 2:
        for n in names:
            if n.startswith("META-INF/jarjar/") and n.endswith(".jar"):
                read_jar(z.read(n), mods, ns_jar, depth + 1)


def installed():
    mods, ns_jar = {}, {}
    for f in sorted(os.listdir(MODS)):
        if f.endswith(".jar"):
            with open(os.path.join(MODS, f), "rb") as fh:
                read_jar(fh.read(), mods, ns_jar)
    return mods, ns_jar


def mod_of_rows():
    rows = {}
    try:
        text = open(HAS_JS, encoding="utf-8").read()
        block = re.search(r"global\.has\._modOf\s*=\s*\{(.*?)\}", text, re.S)
        if block:
            for a, b in re.findall(r"'([^']+)'\s*:\s*'([^']+)'", block.group(1)):
                rows[a] = b
    except OSError:
        pass
    return rows


def build_ns_map(mods, ns_jar):
    overrides = mod_of_rows()

    def owner(ns):
        if ns in overrides:
            return overrides[ns]
        if ns in mods:
            return ns
        jars = [j for j in ns_jar.get(ns, []) if j]
        uniq = {tuple(j) for j in jars}
        if len(uniq) == 1:
            return list(uniq)[0][0]
        return None
    return owner


# ---------------------------------------------------------------- units (our jars)

def props(path):
    out = {}
    try:
        for line in open(path, encoding="utf-8"):
            m = re.match(r"^\s*([A-Za-z_]+)\s*=\s*(.*?)\s*$", line)
            if m:
                out[m.group(1)] = m.group(2)
    except OSError:
        pass
    return out


def units():
    res = []
    for proj in sorted(os.listdir(DEV)):
        d = os.path.join(DEV, proj)
        p = props(os.path.join(d, "gradle.properties"))
        if proj in SKIP_PROJECTS or "mod_id" not in p:
            continue
        ids = {p["mod_id"]}
        roots = [os.path.join(d, "src/main/resources")]
        java = [os.path.join(d, "src/main/java")]
        mdir = os.path.join(d, "members")
        if os.path.isdir(mdir):
            for m in sorted(os.listdir(mdir)):
                mp = props(os.path.join(mdir, m, "gradle.properties"))
                if "mod_id" in mp:
                    ids.add(mp["mod_id"])
                    ids |= {x.strip() for x in mp.get("bundle_extra_mods", "").split(",") if x.strip()}
                roots.append(os.path.join(mdir, m, "src/main/resources"))
                java.append(os.path.join(mdir, m, "src/main/java"))
        gen = os.path.join(d, "build/resources/main/packs/worldgen")
        if os.path.isdir(gen):
            roots.append(os.path.join(d, "build/resources/main/packs/worldgen"))
        # the instance trees the jar copies in (build.gradle scriptTrees / dataTrees, e.g. 'data/creativedelight/kubejs')
        kj = []
        try:
            gradle = open(os.path.join(d, "build.gradle"), encoding="utf-8").read()
        except OSError:
            gradle = ""
        for m in re.finditer(r"^def (?:scriptTrees|dataTrees)\s*=\s*\[([^\]]*)\]", gradle, re.M):
            for tree in re.findall(r"'([^']+)'", m.group(1)):
                t = os.path.join(ROOT, "kubejs", tree)
                if os.path.isdir(t):
                    kj.append((t, tree))
        BUNDLES[p["mod_id"]] = set(ids)
        res.append({"project": proj, "ids": ids, "roots": roots, "java": java, "kubejs": kj})
    return res


BUNDLES = {}  # bundle mod id -> every mod id it nests (filled by units()); requiring a bundle brings its members


def closure(ids, mods):
    seen, todo = set(), list(ids)
    while todo:
        i = todo.pop()
        if i in seen:
            continue
        seen.add(i)
        todo.extend(mods.get(i, {}).get("mand", ()))
        todo.extend(BUNDLES.get(i, ()))
    return seen


# ---------------------------------------------------------------- JSON walk

def severity(rel_after_root):
    """rel path inside a resources root, e.g. data/x/loot_tables/a.json or packs/mods/vinery/data/x/tags/..."""
    p = rel_after_root.split("/")
    # strip pack prefixes down to data/ or assets/
    while p and p[0] not in ("data", "assets"):
        p = p[1:]
    if len(p) < 3:
        return "data"
    if p[0] == "assets":
        return "script" if "kubejs" in p[2:3] else "asset"
    kind = p[2]
    if kind == "kubejs" or "_worldgen" in p:
        return "script"
    if kind in ("worldgen", "dimension", "dimension_type", "structures") or (kind == "forge" and len(p) > 3
                                                                             and p[3] in ("biome_modifier", "structure_modifier")):
        return "registry"
    if kind in ("loot_tables", "loot_modifiers") or (kind == "forge" and len(p) > 3 and p[3] == "loot_modifiers"):
        return "loot"
    if kind == "tags":
        return "tag"
    if kind == "recipes":
        return "recipe"
    if kind == "advancements":
        return "skip"
    return "data"


def path_guards(rel, is_mod):
    """Mod ids a packs/ split guards, from every path segment under packs/."""
    p = rel.split("/")
    if "packs" not in p:
        return set()
    out = set()
    for seg in p[p.index("packs") + 1:]:
        if seg in ("data", "assets"):
            break
        for s in seg.split("+"):
            m = is_mod(s)
            if m:
                out.add(m)
    return out


def folder_guard(rel, is_mod):
    """data/<ns>/<folder>/ where <folder> is a mod id (data/x/moonlight/soft_fluids): only that mod reads the folder."""
    p = rel.split("/")
    while p and p[0] not in ("data", "assets"):
        p = p[1:]
    if len(p) > 3 and p[0] == "data":
        m = is_mod(p[2])
        if m and p[2] not in ALWAYS:
            return {m}
    return set()


def cond_mods(obj):
    out = set()

    def walk(c, neg=False):
        if isinstance(c, list):
            for x in c:
                walk(x, neg)
        elif isinstance(c, dict):
            t = c.get("type", "")
            if t == "forge:mod_loaded" and not neg and isinstance(c.get("modid"), str):
                out.add(c["modid"])
            elif t == "forge:not":
                walk(c.get("value"), True)
            elif t in ("forge:and", "forge:or"):
                walk(c.get("values"), neg)
    for k in ("conditions", "forge:conditions"):
        if k in obj:
            walk(obj[k])
    return out


# Keys whose value is only resolved later and is empty or skipped when absent: a TagKey outside a tag file (an unknown
# tag is empty), a template pool element's structure template (looked up when a piece generates).
# A loot_table_id condition and a loot_table entry's name are table ids (an unknown table is a validation warning and
# rolls nothing), which is how loot tables reach an optional item: a one-item table in that mod's packs/ split.
SOFT_KEYS = {"tag", "location", "loot_table_id", "random_name"}


def walk_json(node, guards, emit, is_mod, key=None, in_tag_file=False):
    if isinstance(node, dict):
        g = guards | {is_mod(m) or m for m in cond_mods(node)}
        req = node.get("requires")
        if isinstance(req, str):
            g = g | {is_mod(req) or req}
        elif isinstance(req, list):
            g = g | {is_mod(r) or r for r in req if isinstance(r, str)}
        if node.get("required") is False and isinstance(node.get("id"), str):
            return
        # Moonlight soft fluids are skipped when "from_mod" is absent; More Mooblooms skips an "optional" flower
        if isinstance(node.get("from_mod"), str):
            g = g | {is_mod(node["from_mod"]) or node["from_mod"]}
        if node.get("optional") is True:
            return
        if node.get("type") in ("minecraft:loot_table", "loot_table") and isinstance(node.get("name"), str):
            node = {k: v for k, v in node.items() if k != "name"}
        for k, v in node.items():
            if k in ("conditions", "forge:conditions") and isinstance(v, list) and any(
                    isinstance(x, dict) and str(x.get("type", "")).startswith("forge:") for x in v):
                continue
            walk_json(v, g, emit, is_mod, k, in_tag_file)
    elif isinstance(node, list):
        for x in node:
            walk_json(x, guards, emit, is_mod, key, in_tag_file)
    elif isinstance(node, str):
        m = ID_RE.match(node)
        if m:
            if not in_tag_file and (node.startswith("#") or key in SOFT_KEYS):
                return
            emit(m.group(1), node, guards)


# ---------------------------------------------------------------- audit

def main():
    args = set(sys.argv[1:])
    mods, ns_jar = installed()
    owner = build_ns_map(mods, ns_jar)
    our_ids = set()
    us = units()
    for u in us:
        our_ids |= u["ids"]

    def is_mod(s):
        if s in mods:
            return s
        o = owner(s)
        return o if o in mods else None

    _exp = {}

    def expand(g):
        # a guard on a mod also guards what that mod requires (enders_additions -> endersdelight, member -> Farmer's)
        if g not in _exp:
            _exp[g] = closure(g, mods)
        return _exp[g]

    flags = []      # (sev, unit, file, ns, mod, sample)
    allowed = []    # (file, reason)
    guarded = {}    # mod -> set(files)
    unknown = {}    # ns -> set(files)

    def audit_file(path, rel_in_root, unit, always, label):
        relroot = os.path.relpath(path, ROOT)
        for pre, why in ALLOW:
            if fnmatch.fnmatch(relroot, pre + "*") if "*" in pre else relroot.startswith(pre):
                allowed.append((relroot, why))
                return
        sev = severity(rel_in_root)
        if sev == "skip" or path.endswith("pack.mcmeta") or "/lang/" in rel_in_root:
            return
        try:
            data = json.load(open(path, encoding="utf-8"))
        except (ValueError, OSError):
            return
        pg = path_guards(rel_in_root, is_mod) | folder_guard(rel_in_root, is_mod)
        hits = {}

        def emit(ns, sample, guards):
            if ns in NOT_MODS or ns in ALWAYS:
                return
            mod = owner(ns)
            if mod is None:
                if ns not in mods:
                    unknown.setdefault(ns, set()).add(relroot)
                    return
                mod = ns
            if mod in always or NS_ALSO.get(ns, set()) & always:
                return
            g = expand(frozenset(guards | pg))
            if mod in g or ns in g:
                guarded.setdefault(mod, set()).add(relroot)
                return
            hits.setdefault((ns, mod), sample)

        walk_json(data, set(), emit, is_mod, None, sev == "tag")
        for (ns, mod), sample in sorted(hits.items()):
            flags.append((sev, label, relroot, ns, mod, sample))

    def walk_root(root, unit, always, label, prefix=""):
        for dp, dn, fn in os.walk(root):
            dn[:] = [x for x in dn if x not in ("build", "moved_code_backup", ".gradle")]
            for f in fn:
                if f.endswith(".json"):
                    full = os.path.join(dp, f)
                    rel = prefix + os.path.relpath(full, root).replace(os.sep, "/")
                    audit_file(full, rel, unit, always, label)

    java_flags = []
    for u in us:
        always = closure(u["ids"], mods) | ALWAYS
        label = u["project"] + (" (report only)" if u["project"] in REPORT_ONLY else "")
        for r in u["roots"]:
            if not os.path.isdir(r):
                continue
            prefix = "packs/worldgen/" if r.endswith("packs/worldgen") else ""
            walk_root(r, u, always, label, prefix)
        for t, tree in u["kubejs"]:
            walk_root(t, u, always, label, tree + "/")
        for jr in u["java"]:
            for dp, dn, fn in os.walk(jr):
                if "/mixin" in dp.replace(os.sep, "/"):
                    continue
                for f in fn:
                    if not f.endswith(".java"):
                        continue
                    full = os.path.join(dp, f)
                    relj = os.path.relpath(full, ROOT)
                    why = next((w for pre, w in ALLOW if relj.startswith(pre)), None)
                    if why:
                        allowed.append((relj, why))
                        continue
                    src = open(full, encoding="utf-8", errors="replace").read()
                    if not JAVA_REG.search(src):
                        continue
                    found = {}
                    for m in list(JAVA_RL2.finditer(src)) + list(JAVA_RL1.finditer(src)):
                        ns = m.group(1)
                        if ns in NOT_MODS or ns in ALWAYS:
                            continue
                        mod = owner(ns) or (ns if ns in mods else None)
                        if mod is None or mod in always or NS_ALSO.get(ns, set()) & always:
                            continue
                        found.setdefault(mod, (ns, m.group(0)))
                    # Items, blocks and fluids are defaulted registries (air / empty for an absent id); only a lookup in a
                    # registry that answers null, with no null or Optional check in the next lines, can crash.
                    lines = src.split("\n")
                    risky = any(RISKY_GET.search(l) and not re.search(r"null|Optional|isPresent",
                                                                      " ".join(lines[i:i + 4]))
                                for i, l in enumerate(lines))
                    for mod, (ns, sample) in sorted(found.items()):
                        gated = re.search(r'(isLoaded|NEEDS_MOD|requires|modLoaded|has\()[^;\n]{0,120}"' + re.escape(mod) + '"', src) \
                            or re.search(r'"' + re.escape(mod) + r'"[^;\n]{0,80}(isLoaded|loaded)', src)
                        relroot = os.path.relpath(full, ROOT)
                        if gated:
                            guarded.setdefault(mod, set()).add(relroot)
                        else:
                            java_flags.append(("java" if risky else "java-soft", label, relroot, ns, mod, sample))

    if "--pack" in args:
        optional_ours = our_ids - {"creativecommon"}
        pack_always = (set(mods) - optional_ours) | ALWAYS
        copied = {os.path.normpath(t) for u in us for t, _ in u["kubejs"]}
        for side in ("data", "assets"):
            base = os.path.join(ROOT, "kubejs", side)
            for dp, dn, fn in os.walk(base):
                if os.path.normpath(dp) in copied:
                    dn[:] = []
                    continue
                for f in fn:
                    if f.endswith(".json"):
                        full = os.path.join(dp, f)
                        audit_file(full, side + "/" + os.path.relpath(full, base).replace(os.sep, "/"), None,
                                   pack_always, "pack")

    order = {"registry": 0, "loot": 1, "tag": 2, "recipe": 3, "java": 4, "data": 5, "script": 6, "java-soft": 7,
             "asset": 8}
    allf = sorted(flags + java_flags, key=lambda x: (order[x[0]], x[1], x[2]))
    heavy = [f for f in allf if f[0] in ("registry", "loot", "tag")]
    detail = allf if "--all" in args else [f for f in allf if f[0] not in ("asset", "java-soft")]
    if "--json" in args:
        json.dump([{"severity": f[0], "unit": f[1], "file": f[2], "namespace": f[3], "mod": f[4], "sample": f[5]}
                   for f in allf], sys.stdout, indent=1)
        print()
        sys.exit(1 if heavy else (2 if allf else 0))
    files = {}
    for sev, label, rel, ns, mod, sample in detail:
        files.setdefault((sev, label, rel), []).append(f"{mod} ({sample})")
    for (sev, label, rel), what in files.items():
        print(f"[{sev}] {label}: {rel}: {'; '.join(what)}")
    counts = {}
    for f in allf:
        counts[f[0]] = counts.get(f[0], 0) + 1
    nfiles = len({(f[2]) for f in allf})
    print(f"\nflagged: {len(allf)} references in {nfiles} files; by severity "
          + ", ".join(f"{k} {v}" for k, v in sorted(counts.items(), key=lambda kv: order[kv[0]])))
    if ("asset" in counts or "java-soft" in counts) and "--all" not in args:
        print("  (asset and java-soft hits listed with --all)")
    print(f"allowlisted files: {len(allowed)} ({len({a[1] for a in allowed})} allowlist rows)")
    if unknown:
        print("namespaces no installed jar provides (stale or dev-only ids): "
              + ", ".join(f"{ns} ({len(v)})" for ns, v in sorted(unknown.items())))
        if "--all" in args:
            for ns, v in sorted(unknown.items()):
                print(f"  {ns}: " + ", ".join(sorted(v)[:6]) + (" ..." if len(v) > 6 else ""))
    if "--guarded" in args or "--all" in args:
        print("guarded optional mods (drop-one set):")
        for mod, v in sorted(guarded.items()):
            print(f"  {mod}: {len(v)} files")
    sys.exit(1 if heavy else (2 if [f for f in allf if f[0] not in ("asset", "java-soft")] else 0))


if __name__ == "__main__":
    main()
