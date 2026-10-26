#!/usr/bin/env python3
"""Diff two mod jars without decompiling: file listing, changed files, lang keys.

Usage: python3 tools/jar_diff.py OLD.jar NEW.jar [--lang en_us] [--show-json PATH_SUBSTR]

Prints added / removed / changed paths grouped by top-level area (assets, data, classes),
then the en_us lang keys added, removed or with a changed value. --show-json prints a
unified diff for changed .json files whose path contains the substring (repeatable).
Paths are compared as-is, so a namespace or folder move shows as one removal plus one addition.
"""
import argparse
import difflib
import hashlib
import json
import zipfile


def load(jar):
    out = {}
    with zipfile.ZipFile(jar) as z:
        for info in z.infolist():
            if info.is_dir():
                continue
            out[info.filename] = z.read(info.filename)
    return out


def area(path):
    if path.endswith(".class"):
        return "classes"
    parts = path.split("/")
    if parts[0] in ("assets", "data") and len(parts) > 2:
        return "/".join(parts[:3])
    return parts[0]


def lang(files, code):
    merged = {}
    for path, data in files.items():
        if path.endswith(f"/lang/{code}.json"):
            try:
                merged.update(json.loads(data.decode("utf-8")))
            except ValueError:
                pass
    return merged


def grouped(title, paths):
    if not paths:
        return
    print(f"\n## {title} ({len(paths)})")
    by_area = {}
    for p in sorted(paths):
        by_area.setdefault(area(p), []).append(p)
    for a, ps in sorted(by_area.items()):
        print(f"  [{a}] {len(ps)}")
        for p in ps:
            print(f"    {p}")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("old")
    ap.add_argument("new")
    ap.add_argument("--lang", default="en_us")
    ap.add_argument("--show-json", action="append", default=[])
    ap.add_argument("--no-classes", action="store_true", help="hide class files from the listings")
    args = ap.parse_args()

    old, new = load(args.old), load(args.new)
    if args.no_classes:
        old = {k: v for k, v in old.items() if not k.endswith(".class")}
        new = {k: v for k, v in new.items() if not k.endswith(".class")}
    added = set(new) - set(old)
    removed = set(old) - set(new)
    digest = lambda b: hashlib.sha1(b).hexdigest()
    changed = {p for p in set(old) & set(new) if digest(old[p]) != digest(new[p])}
    print(f"old {args.old}: {len(old)} files; new {args.new}: {len(new)} files")
    print(f"added {len(added)}, removed {len(removed)}, changed {len(changed)}")
    grouped("Added", added)
    grouped("Removed", removed)
    grouped("Changed", changed)

    lo, ln = lang(old, args.lang), lang(new, args.lang)
    ka, kr = sorted(set(ln) - set(lo)), sorted(set(lo) - set(ln))
    kc = sorted(k for k in set(lo) & set(ln) if lo[k] != ln[k])
    print(f"\n## Lang {args.lang}: +{len(ka)} -{len(kr)} ~{len(kc)}")
    for k in ka:
        print(f"  + {k} = {ln[k]}")
    for k in kr:
        print(f"  - {k} = {lo[k]}")
    for k in kc:
        print(f"  ~ {k}: {lo[k]!r} -> {ln[k]!r}")

    for sub in args.show_json:
        for p in sorted(changed):
            if sub in p and p.endswith(".json"):
                a = old[p].decode("utf-8", "replace").splitlines()
                b = new[p].decode("utf-8", "replace").splitlines()
                print(f"\n## diff {p}")
                for line in difflib.unified_diff(a, b, "old", "new", lineterm="", n=1):
                    print(line)


if __name__ == "__main__":
    main()
