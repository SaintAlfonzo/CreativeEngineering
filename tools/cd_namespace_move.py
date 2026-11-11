#!/usr/bin/env python3
"""Step 5 of feature_writeup/plan_cd_namespace.md: one-off rewrite of references to the ids in tools/cd_namespace_ids.json.

Default is a dry run with per-root counts and the old-namespace paths left alone; --apply writes. Exact ids only, over an
allow-list. Swap lists are special: only their survivors (object keys) change, the old ids they list must stay.
"""
import collections
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OLD = "creativeengineering"
NEW = "creativedelight"
LIST = os.path.join(ROOT, "tools/cd_namespace_ids.json")

ROOTS = [
    "development", "kubejs/data", "kubejs/assets", "kubejs/server_scripts", "kubejs/client_scripts",
    "kubejs/startup_scripts", "kubejs/generation_scripts", "config", "tools",
]
SKIP_DIRS = {"build", "bin", "out", ".gradle", "moved_code_backup", "libs", "batches", "__pycache__", "exported"}
SKIP_FILES = {"cd_namespace_ids.py", "cd_namespace_ids.json", "cd_namespace_move.py", "cdns_probe.py"}
SKIP_PATH_PARTS = ("/changelog/", "/upstream/")
EXTS = (".js", ".json", ".json5", ".java", ".py", ".toml", ".snbt", ".cfg", ".txt", ".properties", ".ini")

# Model predicate that is not a registered id; moves with the lunchbox_skin serializer.
EXTRA_IDS = {"lunchbox_dyed"}

ID_RE = re.compile(r"(?<![\w.-])(#?)" + OLD + r":([a-z0-9_./-]*[a-z0-9_/-])")
LANG_RE = re.compile(r"\b(item|block)\." + OLD + r"\.([a-z0-9_]+)")


def build():
    with open(LIST, encoding="utf-8") as f:
        data = json.load(f)
    exact = {}
    for path in data["ids"] + data["d4_ids"] + data["capabilities"] + sorted(EXTRA_IDS):
        exact[path] = NEW
    for path in data["models"] + data["textures"]:
        exact[path] = NEW
    tags = dict(data["tags"])
    return exact, tags, data["tag_prefixes"], set(data["ids"])


def target(path, is_tag, exact, tags, prefixes):
    """Target namespace of an old path. A #tag is only ever a tag; a bare id may be either, ids win."""
    if not is_tag and path in exact:
        return exact[path]
    if path in tags:
        return tags[path]
    for prefix, ns in prefixes.items():
        if path.startswith(prefix):
            return ns
    return None


def rewrite(text, exact, tags, prefixes, moved, left):
    count = [0]

    def sub_id(m):
        ns = target(m[2], bool(m[1]), exact, tags, prefixes)
        if ns is None or ns == OLD:
            left[m[1] + m[2]] += 1
            return m[0]
        count[0] += 1
        return f"{m[1]}{ns}:{m[2]}"

    def sub_lang(m):
        if m[2] not in moved:
            return m[0]
        count[0] += 1
        return f"{m[1]}.{NEW}.{m[2]}"

    text = ID_RE.sub(sub_id, text)
    text = LANG_RE.sub(sub_lang, text)
    return text, count[0]


def rewrite_swap_list(text, exact, tags, prefixes):
    """Only object keys of replaces/merges (survivors): the old ids in the arrays are the record of what was saved."""
    data = json.loads(text)
    count = 0
    for section in ("replaces", "merges"):
        block = data.get(section)
        if not block:
            continue
        out = {}
        for key, olds in block.items():
            new_key = key
            if key.startswith(OLD + ":"):
                ns = target(key.split(":", 1)[1], False, exact, tags, prefixes)
                if ns and ns != OLD:
                    new_key = f"{ns}:{key.split(':', 1)[1]}"
                    count += 1
            if new_key in out:
                out[new_key] = out[new_key] + olds
            else:
                out[new_key] = olds
        data[section] = out
    return json.dumps(data, indent=2, ensure_ascii=False) + "\n", count


def main():
    apply_it = "--apply" in sys.argv
    exact, tags, prefixes, moved = build()
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
                if name in SKIP_FILES or name.startswith("cdns_") or not name.endswith(EXTS):
                    continue
                path = os.path.join(dp, name)
                try:
                    with open(path, encoding="utf-8", newline="") as f:
                        text = f.read()
                except (UnicodeDecodeError, OSError):
                    continue
                if OLD not in text:
                    continue
                if "/swaps/" in rel_dir and rel_dir.endswith("/swaps/"):
                    if name == "cd_namespace.json":
                        continue
                    new, n = rewrite_swap_list(text, exact, tags, prefixes)
                    swap_count += n
                else:
                    new, n = rewrite(text, exact, tags, prefixes, moved, left)
                if not n:
                    continue
                per_root["/".join(rel_dir.split("/")[:3])] += n
                changed.append((path, new))
    for key, n in per_root.most_common():
        print(f"{n:6d}  {key}")
    print(f"files {len(changed)}, references {sum(per_root.values())}, swap survivors {swap_count}")
    if "--left" in sys.argv:
        print("left alone:", " ".join(f"{k}x{v}" for k, v in left.most_common()))
    if apply_it:
        for path, new in changed:
            with open(path, "w", encoding="utf-8", newline="") as f:
                f.write(new)
        print("applied")
    else:
        print("dry run; pass --apply to write")


if __name__ == "__main__":
    main()
