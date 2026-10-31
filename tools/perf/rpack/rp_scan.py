#!/usr/bin/env python3
"""Offline resource pack cost scan (feature_writeup/perf_rpack.md section 3). No game needed.

    python3 tools/perf/rpack/rp_scan.py [--all] [--out DIR]

Reads every `file/...` pack in options.txt (priority order; later = higher) and, with --all, the disabled zips and
folders in resourcepacks/ too. Each pack is compared with what it replaces: the mod jars in mods/, the vanilla
client jar (downloads/1.19.2.jar) and lower enabled packs. Per pack:

  atlas      block/item textures: pixels added to the block atlas (frame size, after the texture it replaces). 1.19.2
             stitches only textures a model uses, so a texture counts when one of the pack's models uses it or a mod /
             vanilla model the pack does not replace does (models of the texture's namespace and vanilla's)
  upscaled   block/item textures larger than the one they replace
  mip        candidates: atlas textures whose size would lower the whole atlas's mip level below options.txt
             mipmapLevels. Only a stitched texture does it; the bench's latest.log ("limits mip level") is the check
             (2026-10-29: the block atlas still gets mip 4 with the two enabled candidates, so they are not stitched)
  anim       animated textures: count, frames, pixels uploaded per second (20 ticks / frametime x frame area,
             every tick when interpolated), new vs replacing an animation
  elems      model elements added over the models replaced (more quads to mesh and draw)
  dead       files 1.19.2 never reads: shadowed by a higher pack, namespace with no mod, OptiFine-only, outside assets/
  blocks / entities   ids the pack retextures or remodels (the in-game bench builds one scene per pack from these):
             blocks through texture -> base block models using it -> blockstates using those models, or the pack's
             own blockstates; entities from textures/entity/<name>, verified in game

Writes OUT/rpack-scan.txt and OUT/rpack-scan.json (default OUT: perf/reports). The JSON feeds rp_plan.py.
"""
import argparse
import io
import json
import os
import re
import struct
import sys
import zipfile

INST = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../.."))
VANILLA = os.path.join(INST, "../../libraries/net/minecraft/client/1.19.2-20220805.130853/client-1.19.2-20220805.130853-extra.jar")
ATLAS_DIRS = ("textures/block/", "textures/item/")
OPTIFINE = re.compile(r"(^|/)(optifine|mcpatcher|ctm|cit|cem|emissive)(/|$)|\.properties$|\.jem$|\.jpm$")


def png_size(data):
    if len(data) >= 24 and data[:8] == b"\x89PNG\r\n\x1a\n":
        return struct.unpack(">II", data[16:24])
    return None


class Source:
    """A zip or folder of assets; reads members lazily."""

    def __init__(self, path):
        self.path = path
        self.zip = zipfile.ZipFile(path) if os.path.isfile(path) else None
        if self.zip:
            self.sizes = {i.filename: (i.file_size, i.compress_size) for i in self.zip.infolist() if not i.is_dir()}
        else:
            self.sizes = {}
            for root, _, files in os.walk(path):
                for f in files:
                    p = os.path.join(root, f)
                    s = os.path.getsize(p)
                    self.sizes[os.path.relpath(p, path).replace(os.sep, "/")] = (s, s)

    def read(self, name, n=None):
        try:
            if self.zip:
                with self.zip.open(name) as f:
                    return f.read(n) if n else f.read()
            with open(os.path.join(self.path, name), "rb") as f:
                return f.read(n) if n else f.read()
        except (KeyError, OSError):
            return None

    def json(self, name):
        d = self.read(name)
        if d is None:
            return None
        try:
            return json.loads(d.decode("utf-8-sig"))
        except ValueError:
            return None


def anim_info(src, png, size):
    """(frames, frametime, interpolate, frame_w, frame_h) or None when not animated."""
    meta = src.json(png + ".mcmeta")
    if not meta or "animation" not in meta or not size:
        return None
    a = meta["animation"] or {}
    w, h = size
    fw = a.get("width", min(w, h) if "height" not in a else w)
    fh = a.get("height", fw)
    count = max(1, (w // max(fw, 1)) * (h // max(fh, 1)))
    frames = a.get("frames")
    ft = a.get("frametime", 1) or 1
    if frames:
        times = [f.get("time", ft) if isinstance(f, dict) else ft for f in frames]
        n, ft = len(times), sum(times) / len(times)
    else:
        n = count
    return n, ft, bool(a.get("interpolate")), fw, fh


def low_bit(x):
    return x & -x if x else 1


def elements(model):
    return len(model.get("elements", [])) if isinstance(model, dict) else 0


class Base:
    """Everything below the packs: mod jars + vanilla. path -> Source, plus the set of namespaces."""

    def __init__(self):
        self.where = {}
        self.namespaces = {"minecraft", "realms", "forge"}
        jars = [VANILLA] + sorted(os.path.join(INST, "mods", j) for j in os.listdir(os.path.join(INST, "mods"))
                                  if j.endswith(".jar"))
        for j in jars:
            try:
                src = Source(j)
            except (zipfile.BadZipFile, OSError):
                continue
            for name in src.sizes:
                if name.startswith("assets/"):
                    self.where.setdefault(name, src)
                    self.namespaces.add(name.split("/")[1])
            if src.zip:
                toml = src.read("META-INF/mods.toml") or b""
                self.namespaces.update(re.findall(r'modId\s*=\s*"([a-z0-9_.-]+)"', toml.decode("utf-8", "replace")))
        for d in os.listdir(INST):
            if d.endswith("_upstream"):  # in-tree ports read their donor jars from these
                for root, _, files in os.walk(os.path.join(INST, d)):
                    for f in files:
                        if f.endswith(".jar"):
                            try:
                                for name in zipfile.ZipFile(os.path.join(root, f)).namelist():
                                    if name.startswith("assets/"):
                                        self.namespaces.add(name.split("/")[1])
                            except (zipfile.BadZipFile, OSError):
                                pass
        self._idx = {}
        kjs = os.path.join(INST, "kubejs/assets")
        if os.path.isdir(kjs):
            self.namespaces.update(os.listdir(kjs))


def ref_name(v, kind):
    """'ns:block/x' or 'block/x' -> 'assets/ns/<kind>/block/x'."""
    ns, _, p = v.partition(":") if ":" in v else ("minecraft", "", v)
    return f"assets/{ns}/{kind}/{p}"


def model_refs(bs):
    """Model names a blockstate JSON uses (variants and multipart)."""
    out = []

    def add(m):
        for x in (m if isinstance(m, list) else [m]):
            if isinstance(x, dict) and isinstance(x.get("model"), str):
                out.append(x["model"])
    if isinstance(bs, dict):
        for v in (bs.get("variants") or {}).values():
            add(v)
        for part in bs.get("multipart") or []:
            if isinstance(part, dict):
                add(part.get("apply"))
    return out


def block_index(base, ns):
    """For one namespace of the base: texture path -> block ids, and model path -> block ids."""
    if ns in base._idx:
        return base._idx[ns]
    model_blocks, tex_blocks = {}, {}
    pre = f"assets/{ns}/blockstates/"
    for name, src in base.where.items():
        if name.startswith(pre) and name.endswith(".json"):
            bid = f"{ns}:{name[len(pre):-5]}"
            for m in model_refs(src.json(name)):
                model_blocks.setdefault(ref_name(m, "models") + ".json", set()).add(bid)
    for mpath, bids in model_blocks.items():
        src = base.where.get(mpath)
        for v in ((src.json(mpath) if src else None) or {}).get("textures", {}).values():
            if isinstance(v, str) and not v.startswith("#"):
                tex_blocks.setdefault(ref_name(v, "textures") + ".png", set()).update(bids)
    base._idx[ns] = (model_blocks, tex_blocks)
    return base._idx[ns]


def tex_refs(base, ns):
    """Texture path in namespace ns -> base model paths (that namespace's and vanilla's) that use it."""
    key = "tex:" + ns
    if key not in base._idx:
        out = {}
        for name, src in base.where.items():
            if "/models/" in name and name.endswith(".json") and name.split("/")[1] in (ns, "minecraft"):
                for v in ((src.json(name) or {}).get("textures") or {}).values():
                    if isinstance(v, str) and not v.startswith("#"):
                        t = ref_name(v, "textures") + ".png"
                        if t.split("/")[1] == ns:
                            out.setdefault(t, set()).add(name)
        base._idx[key] = out
    return base._idx[key]


def scan_pack(pid, src, base, lower, higher_paths, mip_levels):
    r = {"id": pid, "files": 0, "bytes": 0, "zip_bytes": 0, "textures": 0, "atlas_px": 0, "atlas_px_delta": 0,
         "upscaled": [], "mip_limit": [], "anim": [], "anim_upload_px_s": 0, "elems_delta": 0, "models": 0,
         "blockstates": 0, "sounds_bytes": 0, "shaders": 0, "dead": {"shadowed": 0, "no_mod": 0, "optifine": 0,
         "outside_assets": 0}, "dead_bytes": 0, "blocks": set(), "entities": set(), "no_mod_ns": set()}
    meta = src.json("pack.mcmeta") or {}
    r["pack_format"] = (meta.get("pack") or {}).get("pack_format")

    refs = set()  # textures the pack's own models point at: a new texture nothing references is never stitched
    for name in src.sizes:
        if name.startswith("assets/") and "/models/" in name and name.endswith(".json"):
            for v in ((src.json(name) or {}).get("textures") or {}).values():
                if isinstance(v, str) and not v.startswith("#"):
                    ns_, _, p_ = v.partition(":") if ":" in v else ("minecraft", "", v)
                    refs.add(f"assets/{ns_}/textures/{p_}.png")

    def stitched(name, ns):
        """In the block atlas: used by one of the pack's models, or by a base model the pack does not replace."""
        return name in refs or any(m not in src.sizes for m in tex_refs(base, ns).get(name, ()))

    def original(name):
        for s in lower:
            if name in s.sizes:
                return s
        return base.where.get(name)

    for name, (size, csize) in src.sizes.items():
        r["files"] += 1
        r["bytes"] += size
        r["zip_bytes"] += csize
        if not name.startswith("assets/"):
            if name not in ("pack.mcmeta", "pack.png") and "/" in name:
                r["dead"]["outside_assets"] += 1
                r["dead_bytes"] += size
            continue
        parts = name.split("/")
        if len(parts) < 3:
            continue
        ns, rest = parts[1], "/".join(parts[2:])
        if name in higher_paths:
            r["dead"]["shadowed"] += 1
            r["dead_bytes"] += size
            continue
        if ns not in base.namespaces:
            r["dead"]["no_mod"] += 1
            r["dead_bytes"] += size
            r["no_mod_ns"].add(ns)
            continue
        if OPTIFINE.search(rest):
            r["dead"]["optifine"] += 1
            r["dead_bytes"] += size
            continue
        if rest.startswith("shaders/"):
            r["shaders"] += 1
        if rest.startswith("sounds/") and name.endswith(".ogg"):
            r["sounds_bytes"] += size
        if rest.startswith("blockstates/") and name.endswith(".json"):
            r["blockstates"] += 1
            r["blocks"].add(ns + ":" + rest[len("blockstates/"):-5])
        if rest.startswith("models/") and name.endswith(".json"):
            r["models"] += 1
            model = src.json(name)
            o = original(name)
            r["elems_delta"] += elements(model) - (elements(o.json(name)) if o else 0)
            if rest.startswith("models/block/"):
                r["blocks"].update(block_index(base, ns)[0].get(name, ()))
        if rest.startswith("textures/entity/") and name.endswith(".png"):
            ent = rest[len("textures/entity/"):-4].split("/")[0]
            r["entities"].add(f"{ns}:{ent}")
        if not name.endswith(".png") or not rest.startswith("textures/"):
            continue
        r["textures"] += 1
        sz = png_size(src.read(name, 24) or b"")
        an = anim_info(src, name, sz)
        o = original(name)
        osz = png_size(o.read(name, 24) or b"") if o else None
        oan = anim_info(o, name, osz) if o and osz else None
        in_atlas = rest.startswith(ATLAS_DIRS) and stitched(name, ns)
        if an and (in_atlas or not rest.startswith(ATLAS_DIRS)):
            n, ft, interp, fw, fh = an
            px_s = fw * fh * (20.0 if interp else 20.0 / ft)
            r["anim"].append({"tex": f"{ns}:{rest[9:-4]}", "frames": n, "frametime": round(ft, 2), "interp": interp,
                              "frame": f"{fw}x{fh}", "replaces_anim": bool(oan), "upload_px_s": round(px_s)})
            r["anim_upload_px_s"] += px_s - (osz and oan and oan[3] * oan[4] * (20.0 if oan[2] else 20.0 / oan[1]) or 0)
        if not in_atlas or not sz:
            continue
        fw, fh = (an[3], an[4]) if an else sz
        ofw, ofh = ((oan[3], oan[4]) if oan else osz) if osz else (0, 0)
        r["atlas_px"] += fw * fh
        r["atlas_px_delta"] += fw * fh - ofw * ofh
        if ofw and fw * fh > ofw * ofh:
            r["upscaled"].append(f"{ns}:{rest[9:-4]} {ofw}x{ofh}->{fw}x{fh}")
        r["blocks"].update(block_index(base, ns)[1].get(name, ()))
        lim = min(low_bit(fw), low_bit(fh))
        if lim < (1 << mip_levels):
            r["mip_limit"].append(f"{ns}:{rest[9:-4]} {fw}x{fh} -> mip {lim.bit_length() - 1}")
    for k in ("blocks", "entities", "no_mod_ns"):
        r[k] = sorted(r[k])
    r["anim_upload_px_s"] = round(r["anim_upload_px_s"])
    return r


def options():
    opts = {}
    for line in open(os.path.join(INST, "options.txt"), errors="replace"):
        k, _, v = line.rstrip("\n").partition(":")
        opts[k] = v
    return opts


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--all", action="store_true", help="also scan disabled packs in resourcepacks/")
    ap.add_argument("--out", default=os.path.join(INST, "perf/reports"))
    a = ap.parse_args()
    opts = options()
    order = json.loads(opts.get("resourcePacks", "[]"))
    incompatible = set(json.loads(opts.get("incompatibleResourcePacks", "[]")))
    mip = int(opts.get("mipmapLevels", "4"))
    rp = os.path.join(INST, "resourcepacks")
    enabled = [p for p in order if p.startswith("file/") and os.path.exists(os.path.join(rp, p[5:]))]
    disabled = ["file/" + f for f in sorted(os.listdir(rp)) if "file/" + f not in order
                and (f.endswith(".zip") or os.path.isfile(os.path.join(rp, f, "pack.mcmeta")))] if a.all else []
    base = Base()
    srcs = {p: Source(os.path.join(rp, p[5:])) for p in enabled + disabled}
    out = []
    for i, pid in enumerate(enabled + disabled):
        on = pid in enabled
        lower = [srcs[p] for p in reversed(enabled[:i])] if on else [srcs[p] for p in reversed(enabled)]
        higher = set().union(*(srcs[p].sizes for p in enabled[i + 1:])) if on else set()
        r = scan_pack(pid, srcs[pid], base, lower, higher, mip)
        r.update(enabled=on, priority=order.index(pid) if on else None, incompatible=pid in incompatible)
        out.append(r)
    os.makedirs(a.out, exist_ok=True)
    js = os.path.join(a.out, "rpack-scan.json")
    json.dump({"mipmapLevels": mip, "order": order, "packs": out}, open(js, "w"), indent=1)
    txt = report(out, mip)
    open(os.path.join(a.out, "rpack-scan.txt"), "w").write(txt)
    print(txt.split("\n\n## ")[0])
    print("\nfull report:", os.path.join(a.out, "rpack-scan.txt"), "| json:", js)


def report(packs, mip):
    rows = ["# Resource pack scan (offline)", "",
            f"Atlas px: block/item atlas pixels added over what they replace (x4 bytes, x1.33 with mipmaps).",
            f"Anim px/s: animated pixels uploaded per second beyond the replaced texture. Mip: textures that lower the "
            f"atlas mip level below {mip}. Elems: model elements added. Dead: files 1.19.2 never reads.", "",
            f"{'pack':44} {'on':3} {'fmt':>3} {'files':>5} {'KB':>6} {'atlas px':>9} {'ups':>4} {'mip':>3} "
            f"{'anim':>4} {'anim px/s':>9} {'elems':>6} {'dead':>5} {'blocks':>6} {'ents':>4}"]
    key = lambda r: (not r["enabled"], -(abs(r["atlas_px_delta"]) + r["anim_upload_px_s"] / 20 + 50 * len(r["mip_limit"])))
    for r in sorted(packs, key=key):
        d = sum(r["dead"].values())
        rows.append(f"{r['id'][5:][:44]:44} {'y' if r['enabled'] else '-':3} {str(r['pack_format']):>3} {r['files']:>5} "
                    f"{r['zip_bytes'] // 1024:>6} {r['atlas_px_delta']:>9} {len(r['upscaled']):>4} "
                    f"{len(r['mip_limit']):>3} {len(r['anim']):>4} {r['anim_upload_px_s']:>9} {r['elems_delta']:>6} "
                    f"{d:>5} {len(r['blocks']):>6} {len(r['entities']):>4}")
    tot = [r for r in packs if r["enabled"]]
    px = sum(r["atlas_px_delta"] for r in tot)
    rows += ["", f"Enabled total: atlas {px} px (~{px * 4 * 4 / 3 / 1048576:.1f} MB VRAM with mipmaps), "
                 f"anim {sum(r['anim_upload_px_s'] for r in tot)} px/s, "
                 f"{sum(len(r['mip_limit']) for r in tot)} mip limiters, "
                 f"{sum(sum(r['dead'].values()) for r in tot)} dead files"]
    for r in sorted(packs, key=key):
        rows += ["", f"## {r['id'][5:]}" + ("" if r["enabled"] else " (disabled)"),
                 f"format {r['pack_format']}{' (marked incompatible)' if r['incompatible'] else ''}, "
                 f"{r['files']} files, {r['bytes'] // 1024} KB unpacked, {r['textures']} textures, {r['models']} models, "
                 f"{r['blockstates']} blockstates, sounds {r['sounds_bytes'] // 1024} KB, shader files {r['shaders']}",
                 "dead: " + ", ".join(f"{k} {v}" for k, v in r["dead"].items()) + f" ({r['dead_bytes'] // 1024} KB)"
                 + (f"; namespaces with no mod: {', '.join(r['no_mod_ns'])}" if r["no_mod_ns"] else "")]
        for label, items in (("upscaled", r["upscaled"]), ("mip limit", r["mip_limit"])):
            if items:
                rows.append(f"{label} ({len(items)}): " + "; ".join(items[:12]) + (" ..." if len(items) > 12 else ""))
        for an in sorted(r["anim"], key=lambda x: -x["upload_px_s"])[:10]:
            rows.append(f"anim {an['tex']} {an['frame']} x{an['frames']} ft {an['frametime']}"
                        f"{' interp' if an['interp'] else ''}{' (replaces anim)' if an['replaces_anim'] else ' (new)'}"
                        f" {an['upload_px_s']} px/s")
        if r["blocks"]:
            rows.append(f"blocks ({len(r['blocks'])}): " + ", ".join(r["blocks"][:15]) + (" ..." if len(r["blocks"]) > 15 else ""))
        if r["entities"]:
            rows.append(f"entities ({len(r['entities'])}): " + ", ".join(r["entities"][:15]))
    return "\n".join(rows) + "\n"


if __name__ == "__main__":
    sys.exit(main())
