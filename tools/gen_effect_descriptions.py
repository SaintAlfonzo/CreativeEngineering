#!/usr/bin/env python3
# Fills in missing JEED (Just Enough Effect Descriptions, mods/jeed-*.jar) tooltip text.
#
# JEED reads "effect.<mod>.<path>.description" lang keys (falls back to "No description
# available" when absent). It ships its own assets/jeed/lang/en_us.json covering a lot of
# common addon effects, but plenty of installed mods still have no entry — and lang keys
# from any namespace merge into one flat table at runtime, so we don't need to touch JEED's
# jar: a KubeJS virtual resourcepack overlay works fine.
#
# This script:
#   1. Reads every en_us table the client sees through tools/lang_index.py (vanilla and Forge jars, the upstream
#      jars the ports read, every mod jar with its jar-in-jar members, kubejs/assets, client script renames,
#      enabled resource packs) for "effect.<mod>.<path>" (display name) and "effect.<mod>.<path>.description".
#   2. Leaves out this script's own output (kubejs/assets/creativeengineering/lang), so its placeholders never
#      count as "found".
#   3. Adds any pack-original MobEffects that skip a lang name entirely (e.g.
#      creativeengineering:magic_resistance uses a hardcoded Text.of(...) display name) —
#      see EXTRA_EFFECTS below. Grep for new ones with:
#        grep -rn "StartupEvents.registry('mob_effect'" kubejs/startup_scripts/
#   4. For every registered effect (kubejs/exported/mob_effects.json from /dumppotions, when present) with a name
#      but no description anywhere, writes the PORTED text below if there is one, else a placeholder, into
#      kubejs/assets/creativeengineering/lang/en_us.json (created if missing). Names that some jar still carries
#      for an effect nobody registers (essence:wet, Xaero's old *_beneficial switches) get nothing.
#
# PORTED: descriptions taken from other effect-description mods (newer JEED, MEED, Effect Insights, Effect
# Descriptions, Potion Descriptions) for effects the installed jars don't describe. To look for more:
#   python3 tools/gen_effect_descriptions.py --sources [jar ...]
# lists, for every effect still on a placeholder, what each source jar says (default jars: SOURCE_DIRS). It
# writes nothing in that mode; copy a text into PORTED with its source to use it.
#
# Idempotent: re-running only fills keys that are still absent OR still carry the
# PLACEHOLDER_MARKER. Once you hand-write a real description (removing the marker), this
# script leaves it alone — safe to re-run after every mod update.

import zipfile, json, os, re, sys, glob

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import lang_index  # noqa: E402

ROOT = str(lang_index.INSTANCE)
# The Vinery backport reads its lang from vinery_upstream/ at runtime (feature_writeup/alcohol.md B4.8); lang_index
# reads both jars there. Names of effects the port dropped (1.3.10 only) get nothing: main() keeps registered ones.
KUBEJS_ASSETS = os.path.join(ROOT, "kubejs", "assets")
OUT_FILE = os.path.join(KUBEJS_ASSETS, "creativeengineering", "lang", "en_us.json")
MOB_EFFECTS = os.path.join(ROOT, "kubejs", "exported", "mob_effects.json")
# Other effect-description mods (not installed; their lang only). tools/.cache is git-ignored: the jars there were
# fetched 2026-10-20 (Modrinth: JEED 1.21-2.3.5, MEED 8.1.0, Effect Insights 26.3.1, Potion Descriptions 1.8)
# or copied from the ATLauncher download cache (JEED 1.16.5-1.6 / 1.19.2-1.18 / 1.20-2.2.5, Effect Descriptions 8.0.2).
SOURCE_DIRS = [os.path.join(ROOT, "tools", ".cache", "effect_desc_sources")]

NAME_RE = re.compile(r"^effect\.([a-z0-9_]+)\.([a-z0-9_]+)$")
DESC_RE = re.compile(r"^effect\.([a-z0-9_]+)\.([a-z0-9_]+)\.description$")

PLACEHOLDER_MARKER = "TODO: write a description"

# Pack-original MobEffects registered via KubeJS/Java with no lang name key at all
# (display name set directly via .displayName(Text.of(...)) or a Java Component).
# key: "mod:path" -> display name to use when generating the placeholder text.
EXTRA_EFFECTS = {
    "creativeengineering:magic_resistance": "Magic Resistance",
    # Everything Copper ships no name for it (effects.md F1); the placeholder still needs a key to sit on.
    "everythingcopper:copper_poisoning": "Copper Poisoning",
}

# Effects the dumped registry (mob_effects.json) doesn't have yet: the Vinery port's trippy stays registered.
EXTRA_REGISTERED = {"vinery:trippy"}

# "mod:path" -> (source, text): a description from another effect-description mod, used instead of a placeholder.
PORTED = {
    "via_romana:travellers_fatigue": ("Effect Insights 26.3.1", "Prevents fast travel."),
}
SOURCE_DESC_RE = re.compile(r"^effect\.([a-z0-9_]+)\.([a-z0-9_]+)\.(?:description|desc)$")


def scan():
    names, descs = {}, {}
    table, _ = lang_index.build(skip_kubejs_ns=("creativeengineering",))  # not our own output (OUT_FILE)
    for k, v in table.items():
        m = NAME_RE.match(k)
        if m:
            names[(m.group(1), m.group(2))] = v
            continue
        m = DESC_RE.match(k)
        if m:
            descs[(m.group(1), m.group(2))] = v

    for eid, display in EXTRA_EFFECTS.items():
        mod, path = eid.split(":", 1)
        names.setdefault((mod, path), display)

    return names, descs


def registered():
    """(mod, path) of every registered effect, or None when there's no /dumppotions export to go by."""
    if not os.path.exists(MOB_EFFECTS):
        return None
    with open(MOB_EFFECTS, "r", encoding="utf-8") as f:
        ids = {eid for ns in json.load(f).values() for eid in ns}
    ids |= EXTRA_REGISTERED | set(EXTRA_EFFECTS)
    return {tuple(eid.split(":", 1)) for eid in ids}


def source_jars(args):
    if args:
        return args
    return sorted(j for d in SOURCE_DIRS for j in glob.glob(os.path.join(d, "*.jar")))


def report_sources(jars, wanted):
    found = {}
    for path in jars:
        try:
            with zipfile.ZipFile(path) as z:
                for name in z.namelist():
                    if not re.match(r"assets/[^/]+/lang/en_us\.json$", name):
                        continue
                    try:
                        obj = json.loads(z.read(name).decode("utf-8", "ignore"))
                    except ValueError:
                        continue
                    for k, v in obj.items():
                        m = SOURCE_DESC_RE.match(k)
                        if m and (m.group(1), m.group(2)) in wanted:
                            found.setdefault((m.group(1), m.group(2)), []).append((os.path.basename(path), v))
        except (zipfile.BadZipFile, FileNotFoundError):
            print(f"[gen_effect_descriptions] can't read {path}", file=sys.stderr)
    for mod, path in sorted(wanted):
        hits = found.get((mod, path), [])
        print(f"{mod}:{path}: " + ("none" if not hits else ""))
        for src, text in hits:
            print(f"    {src}: {text}")


def main():
    names, descs = scan()
    live = registered()

    missing = sorted(k for k in names if k not in descs and (live is None or k in live))

    if "--sources" in sys.argv:
        with open(OUT_FILE, "r", encoding="utf-8") as f:
            overlay = json.load(f)
        wanted = {k for k in missing
                  if overlay.get(f"effect.{k[0]}.{k[1]}.description", PLACEHOLDER_MARKER).startswith(PLACEHOLDER_MARKER)}
        report_sources(source_jars(sys.argv[sys.argv.index("--sources") + 1:]), wanted)
        return

    existing = {}
    if os.path.exists(OUT_FILE):
        with open(OUT_FILE, "r", encoding="utf-8") as f:
            existing = json.load(f)

    added, kept_placeholder, skipped_curated = 0, 0, 0
    for mod, path in missing:
        key = f"effect.{mod}.{path}.description"
        display = names[(mod, path)] or f"{mod}:{path}"
        current = existing.get(key)
        ported = PORTED.get(f"{mod}:{path}")
        if ported and (current is None or current.startswith(PLACEHOLDER_MARKER)):
            existing[key] = ported[1]
            added += 1
        elif current is None:
            existing[key] = f"{PLACEHOLDER_MARKER} for {display}."
            added += 1
        elif current.startswith(PLACEHOLDER_MARKER):
            kept_placeholder += 1
        else:
            skipped_curated += 1  # hand-written description already present, don't touch

    # Drop stale placeholder keys for effects that now have a real description
    # elsewhere (e.g. JEED shipped an update) so the overlay doesn't fight it.
    stale = [
        k for k in list(existing.keys())
        if (m := DESC_RE.match(k))
        and (m.group(1), m.group(2)) in descs
        and existing[k].startswith(PLACEHOLDER_MARKER)
    ]
    # ...and placeholders for effects no installed mod names or registers any more (removed mods, dropped effects).
    stale += [
        k for k in list(existing.keys())
        if k not in stale
        and (m := DESC_RE.match(k))
        and ((m.group(1), m.group(2)) not in names or (live is not None and (m.group(1), m.group(2)) not in live))
        and existing[k].startswith(PLACEHOLDER_MARKER)
    ]
    for k in stale:
        del existing[k]

    os.makedirs(os.path.dirname(OUT_FILE), exist_ok=True)
    with open(OUT_FILE, "w", encoding="utf-8") as f:
        json.dump(dict(sorted(existing.items())), f, indent=2, ensure_ascii=False)
        f.write("\n")

    print(f"[gen_effect_descriptions] effects with a name: {len(names)}", file=sys.stderr)
    print(f"[gen_effect_descriptions] missing descriptions found: {len(missing)}", file=sys.stderr)
    print(f"[gen_effect_descriptions] new placeholders / ported texts written: {added}", file=sys.stderr)
    print(f"[gen_effect_descriptions] placeholders already present: {kept_placeholder}", file=sys.stderr)
    print(f"[gen_effect_descriptions] real descriptions left untouched: {skipped_curated}", file=sys.stderr)
    print(f"[gen_effect_descriptions] stale placeholders removed: {len(stale)}", file=sys.stderr)
    print(f"[gen_effect_descriptions] -> {os.path.relpath(OUT_FILE, ROOT)}", file=sys.stderr)


if __name__ == "__main__":
    main()
