#!/usr/bin/env python3
"""feature_writeup/namespace_move.md: rewrite of references to the ids in tools/owner_namespace_ids.json, and the
resource folder moves. Generalises tools/cd_namespace_move.py (one-off for Creative Delight).

Default is a dry run with per-root counts; --apply writes and moves files (git mv). Rewrites:
  'creativeengineering:path' and '#creativeengineering:path' strings (also solapplepie: for the lunchbox),
  Java new ResourceLocation("creativeengineering", "path"),
  lang keys item./block./entity.<old>.<path>.
A bare id resolves as a registered id first, then tag, data id, model, texture. Swap lists keep their old ids: only
survivors (object keys) change. Trees moved by their own change (Worldgen, Calendar, Devtools, Labels, Exposure) are
skipped. --left lists the old-namespace references left alone.
"""
import collections
import json
import os
import re
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LIST = os.path.join(ROOT, "tools/owner_namespace_ids.json")
OLDS = ("creativeengineering", "solapplepie")

ROOTS = [
    "development", "kubejs/data", "kubejs/assets", "kubejs/server_scripts", "kubejs/client_scripts",
    "kubejs/startup_scripts", "kubejs/generation_scripts", "config", "configs_edited", "tools", "textureswapper",
]
SKIP_DIRS = {"build", "bin", "out", ".gradle", "moved_code_backup", "libs", "batches", "__pycache__", "exported",
             "upstream", "creative_delight_test"}
# Moved by their own change (namespace_move.md step 6 and 7).
SKIP_PREFIXES = (
    "development/creativeWorldgen/", "development/creativeCalendar/", "development/creativeDevtools/",
    "development/creativeLabels/", "development/exposureBackport/", "kubejs/data/creativeworldgen/",
    "kubejs/generation_scripts/worldgen/", "config/creativeworldgen", "config/creativecalendar",
    "configs_edited/root_config/creativeworldgen", "configs_edited/root_config/creativecalendar",
)
SKIP_FILES = {"owner_namespace_ids.py", "owner_namespace_ids.json", "owner_namespace_move.py", "cd_namespace_ids.py",
              "cd_namespace_ids.json", "cd_namespace_move.py", "id_owner_lint.py"}
SKIP_PATH_PARTS = ("/changelog/",)
EXTS = (".js", ".json", ".json5", ".java", ".py", ".toml", ".snbt", ".cfg", ".txt", ".properties", ".ini", ".mcmeta")

OLD_ALT = "|".join(OLDS)
ID_RE = re.compile(r"(?<![\w.-])(#?)(" + OLD_ALT + r"):([a-z0-9_./-]*[a-z0-9_/-])")
JAVA_RE = re.compile(r'(\(\s*)"(' + OLD_ALT + r')"(\s*,\s*)"([a-z0-9_./-]+)"')
LANG_RE = re.compile(r"\b(item|block|entity)\.(" + OLD_ALT + r")\.([a-z0-9_]+)\b")

# Folder moves: (source, destination). Jar resource folders are added per owner from the id list.
EXTRA_MOVES = [
    ("kubejs/assets/creativeengineering/textures/entity/canned_golem", "kubejs/assets/creativedelight/textures/entity/canned_golem"),
    ("kubejs/data/creativeengineering/paintings", "kubejs/data/creativecompat/paintings"),
    ("kubejs/assets/solapplepie/models/item/netherite_lunchbox.json", "kubejs/assets/creativedelight/models/item/netherite_lunchbox.json"),
]
JARS = {"creativeequipment": "creativeEquipment", "creativemagic": "creativeMagic", "creativecompat": "creativeCompat"}


def load():
    with open(LIST, encoding="utf-8") as f:
        return json.load(f)


def resolver(data):
    ids = data["ids"]
    # Creative Delight's tags from plan_cd_namespace.md: its rewrite missed Java's two-argument form.
    cd = json.load(open(os.path.join(ROOT, "tools/cd_namespace_ids.json"), encoding="utf-8"))
    tags = {t: ns for t, ns in cd["tags"].items() if ns != "creativeengineering"}
    tags.update(data["tags"])
    prefixes = dict(cd["tag_prefixes"])
    prefixes.update(data["tag_prefixes"])
    others = {}
    for key in ("data", "models", "textures"):
        for path, ns in data[key].items():
            others.setdefault(path, ns)
    data_prefixes = {p: ns for p, ns in data["data"].items() if p.endswith("_")}
    caps = data["capabilities"]

    def target(old_ns, path, is_tag):
        full = f"{old_ns}:{path}"
        if not is_tag and full in ids:
            return ids[full]
        if old_ns != "creativeengineering":
            return None
        if not is_tag and full in caps:
            return caps[full]
        if path in tags:
            return tags[path]
        for p, ns in prefixes.items():
            if path.startswith(p):
                return ns
        if is_tag:
            return None
        if path in others:
            return others[path]
        for p, ns in data_prefixes.items():
            if path.startswith(p):
                return ns
        return None
    return target


def rewrite(text, target, lang_paths, left):
    count = [0]

    def sub_id(m):
        ns = target(m[2], m[3], bool(m[1]))
        if ns is None:
            left[f"{m[1]}{m[2]}:{m[3]}"] += 1
            return m[0]
        count[0] += 1
        return f"{m[1]}{ns}:{m[3]}"

    def sub_java(m):
        ns = target(m[2], m[4], False)
        if ns is None:
            left[f"java {m[2]}:{m[4]}"] += 1
            return m[0]
        count[0] += 1
        return f'{m[1]}"{ns}"{m[3]}"{m[4]}"'

    def sub_lang(m):
        ns = lang_paths.get(f"{m[2]}:{m[3]}")
        if ns is None:
            return m[0]
        count[0] += 1
        return f"{m[1]}.{ns}.{m[3]}"

    text = ID_RE.sub(sub_id, text)
    text = JAVA_RE.sub(sub_java, text)
    text = LANG_RE.sub(sub_lang, text)
    return text, count[0]


def rewrite_swap_list(text, target):
    data = json.loads(text)
    count = 0
    for section in ("replaces", "merges"):
        block = data.get(section)
        if not block:
            continue
        out = {}
        for key, olds in block.items():
            new_key = key
            ns_old, path = key.split(":", 1)
            if ns_old in OLDS:
                ns = target(ns_old, path, False)
                if ns:
                    new_key = f"{ns}:{path}"
                    count += 1
            out[new_key] = out.get(new_key, []) + olds
        data[section] = out
    return json.dumps(data, indent=2, ensure_ascii=False) + "\n", count


def moves():
    out = list(EXTRA_MOVES)
    for ns, jar in JARS.items():
        base = f"development/{jar}/src/main/resources"
        for kind in ("assets", "data"):
            out.append((f"{base}/{kind}/creativeengineering", f"{base}/{kind}/{ns}"))
        packs = os.path.join(ROOT, base, "packs")
        if os.path.isdir(packs):
            for pack in sorted(os.listdir(packs)):
                out.append((f"{base}/packs/{pack}/data/creativeengineering", f"{base}/packs/{pack}/data/{ns}"))
    return out


def git_move(src, dst):
    """Moves a file or merges a folder into an existing one, file by file, with git mv."""
    s = os.path.join(ROOT, src)
    if not os.path.exists(s):
        return 0
    if os.path.isfile(s):
        pairs = [(s, os.path.join(ROOT, dst))]
    else:
        pairs = []
        for dp, _, files in os.walk(s):
            for f in files:
                p = os.path.join(dp, f)
                pairs.append((p, os.path.join(ROOT, dst, os.path.relpath(p, s))))
    for a, b in pairs:
        if os.path.exists(b):
            raise SystemExit(f"move target exists: {os.path.relpath(b, ROOT)}")
        os.makedirs(os.path.dirname(b), exist_ok=True)
        r = subprocess.run(["git", "mv", a, b], cwd=ROOT, capture_output=True, text=True)
        if r.returncode:
            os.rename(a, b)  # untracked file
    for dp, dn, files in os.walk(s, topdown=False):
        if not os.listdir(dp):
            os.rmdir(dp)
    return len(pairs)


def main():
    apply_it = "--apply" in sys.argv
    data = load()
    target = resolver(data)
    lang_paths = {old: ns for old, ns in data["ids"].items()}
    left = collections.Counter()
    per_root = collections.Counter()
    changed = []
    swap_count = 0
    for root in ROOTS:
        for dp, dn, files in os.walk(os.path.join(ROOT, root)):
            dn[:] = [d for d in dn if d not in SKIP_DIRS]
            rel_dir = os.path.relpath(dp, ROOT).replace(os.sep, "/") + "/"
            if any(p in rel_dir for p in SKIP_PATH_PARTS):
                continue
            for name in files:
                rel = rel_dir + name
                if name in SKIP_FILES or rel.startswith(SKIP_PREFIXES) or not name.endswith(EXTS):
                    continue
                path = os.path.join(dp, name)
                try:
                    with open(path, encoding="utf-8", newline="") as f:
                        text = f.read()
                except (UnicodeDecodeError, OSError):
                    continue
                if not any(o in text for o in OLDS):
                    continue
                if rel_dir.endswith("/swaps/"):
                    if name == "owner_namespace.json":
                        continue
                    new, n = rewrite_swap_list(text, target)
                    swap_count += n
                else:
                    new, n = rewrite(text, target, lang_paths, left)
                if not n:
                    continue
                per_root["/".join(rel_dir.split("/")[:3])] += n
                changed.append((path, new))
    for key, n in per_root.most_common():
        print(f"{n:6d}  {key}")
    print(f"files {len(changed)}, references {sum(per_root.values())}, swap survivors {swap_count}")
    if "--left" in sys.argv:
        print("left alone:", " ".join(f"{k}x{v}" for k, v in left.most_common()))
    planned = [(a, b) for a, b in moves() if os.path.exists(os.path.join(ROOT, a))]
    for a, b in planned:
        print(f"move {a} -> {b}")
    if apply_it:
        for path, new in changed:
            with open(path, "w", encoding="utf-8", newline="") as f:
                f.write(new)
        moved = sum(git_move(a, b) for a, b in planned)
        print(f"applied; {moved} files moved")
    else:
        print("dry run; pass --apply to write")


if __name__ == "__main__":
    main()
