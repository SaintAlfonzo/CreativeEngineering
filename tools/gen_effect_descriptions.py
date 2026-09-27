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
#   1. Scans every enabled mod jar's assets/*/lang/en_us.json for "effect.<mod>.<path>"
#      (display name) and "effect.<mod>.<path>.description" keys.
#   2. Scans kubejs/assets/**/lang/en_us.json the same way (existing overlays / this
#      script's own prior output).
#   3. Adds any pack-original MobEffects that skip a lang name entirely (e.g.
#      creativeengineering:magic_resistance uses a hardcoded Text.of(...) display name) —
#      see EXTRA_EFFECTS below. Grep for new ones with:
#        grep -rn "StartupEvents.registry('mob_effect'" kubejs/startup_scripts/
#   4. For every effect with a name but no description anywhere, writes a placeholder into
#      kubejs/assets/creativeengineering/lang/en_us.json (created if missing).
#
# Idempotent: re-running only fills keys that are still absent OR still carry the
# PLACEHOLDER_MARKER. Once you hand-write a real description (removing the marker), this
# script leaves it alone — safe to re-run after every mod update.

import zipfile, json, os, re, sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
MODS_DIR = os.path.join(ROOT, "mods")
KUBEJS_ASSETS = os.path.join(ROOT, "kubejs", "assets")
OUT_FILE = os.path.join(KUBEJS_ASSETS, "creativeengineering", "lang", "en_us.json")

NAME_RE = re.compile(r"^effect\.([a-z0-9_]+)\.([a-z0-9_]+)$")
DESC_RE = re.compile(r"^effect\.([a-z0-9_]+)\.([a-z0-9_]+)\.description$")

PLACEHOLDER_MARKER = "TODO: write a description"

# Pack-original MobEffects registered via KubeJS/Java with no lang name key at all
# (display name set directly via .displayName(Text.of(...)) or a Java Component).
# key: "mod:path" -> display name to use when generating the placeholder text.
EXTRA_EFFECTS = {
    "creativeengineering:magic_resistance": "Magic Resistance",
}


def process_lang_json(data, names, descs):
    try:
        obj = json.loads(data.decode("utf-8"))
    except Exception:
        return
    for k, v in obj.items():
        m = NAME_RE.match(k)
        if m:
            names[(m.group(1), m.group(2))] = v
            continue
        m = DESC_RE.match(k)
        if m:
            descs[(m.group(1), m.group(2))] = v


def scan():
    names, descs = {}, {}

    for fn in sorted(os.listdir(MODS_DIR)):
        if not fn.endswith(".jar"):
            continue
        path = os.path.join(MODS_DIR, fn)
        try:
            with zipfile.ZipFile(path) as z:
                for name in z.namelist():
                    if re.match(r"assets/[^/]+/lang/en_us\.json$", name):
                        with z.open(name) as f:
                            process_lang_json(f.read(), names, descs)
        except zipfile.BadZipFile:
            continue

    for dirpath, _, files in os.walk(KUBEJS_ASSETS):
        for fn in files:
            if fn != "en_us.json" or not dirpath.endswith(os.path.join("lang")):
                continue
            path = os.path.join(dirpath, fn)
            if os.path.abspath(path) == os.path.abspath(OUT_FILE):
                continue  # don't let our own generated placeholders count as "found"
            with open(path, "rb") as f:
                process_lang_json(f.read(), names, descs)

    for eid, display in EXTRA_EFFECTS.items():
        mod, path = eid.split(":", 1)
        names.setdefault((mod, path), display)

    return names, descs


def main():
    names, descs = scan()

    missing = sorted(k for k in names if k not in descs)

    existing = {}
    if os.path.exists(OUT_FILE):
        with open(OUT_FILE, "r", encoding="utf-8") as f:
            existing = json.load(f)

    added, kept_placeholder, skipped_curated = 0, 0, 0
    for mod, path in missing:
        key = f"effect.{mod}.{path}.description"
        display = names[(mod, path)] or f"{mod}:{path}"
        current = existing.get(key)
        if current is None:
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
    for k in stale:
        del existing[k]

    os.makedirs(os.path.dirname(OUT_FILE), exist_ok=True)
    with open(OUT_FILE, "w", encoding="utf-8") as f:
        json.dump(dict(sorted(existing.items())), f, indent=2, ensure_ascii=False)
        f.write("\n")

    print(f"[gen_effect_descriptions] effects with a name: {len(names)}", file=sys.stderr)
    print(f"[gen_effect_descriptions] missing descriptions found: {len(missing)}", file=sys.stderr)
    print(f"[gen_effect_descriptions] new placeholders written: {added}", file=sys.stderr)
    print(f"[gen_effect_descriptions] placeholders already present: {kept_placeholder}", file=sys.stderr)
    print(f"[gen_effect_descriptions] real descriptions left untouched: {skipped_curated}", file=sys.stderr)
    print(f"[gen_effect_descriptions] stale placeholders removed: {len(stale)}", file=sys.stderr)
    print(f"[gen_effect_descriptions] -> {os.path.relpath(OUT_FILE, ROOT)}", file=sys.stderr)


if __name__ == "__main__":
    main()
