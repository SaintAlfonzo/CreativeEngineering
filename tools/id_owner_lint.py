#!/usr/bin/env python3
"""Flag ids in the in-tree mods that name a jar other than the one that owns the code.

Plan: feature_writeup/plan_owner_ids.md. Saved ids (registered content, NBT keys, capability ids, keybinds) keep
their old namespace on purpose and are not checked, or are allow-listed below.

Checks, per mod root (a dir with src/main and its own mod id):
  subscriber  @Mod.EventBusSubscriber(modid = X)    X must be the root's mod id (else Forge never registers it)
  wthit       waila_plugins.json keys, and the tooltip line ids (setLine keys, and any new ResourceLocation
              with a creative* namespace) in the plugin classes
  jei         getPluginUid() and JEI RecipeType namespaces
  channel     NetworkRegistry.newSimpleChannel name
  ce-name     outside Creative Engineering (own jars only): literal("ce...") commands, CE* / Ce* class names,
              CE_ constants, and a "ce" mod id

Usage: python3 tools/id_owner_lint.py [-v]   (exit 1 when anything is flagged)
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DEV = ROOT / "development"
SKIP_PARTS = {"build", "moved_code_backup", ".gradle", "bin", "out"}

# Roots whose ids come from an upstream port: only the id checks apply, never ce-name.
OWN_PREFIXES = ("creative", "cdev")

# (check, root mod id, id) pairs kept on purpose. See plan section 4.
ALLOW = {
    # JEI category that mirrors the registered recipe type creativeengineering:baking (like dragon_breathing).
    ("jei", "creativedelight", "creativeengineering:baking"),
}

STR_CONST = re.compile(r'\b(?:public\s+|private\s+|protected\s+)?static\s+final\s+String\s+(\w+)\s*=\s*([^;]+);')
CLASS_DECL = re.compile(r'\b(?:class|interface|enum|record)\s+(\w+)')
RL_CONST = re.compile(r'\bResourceLocation\s+(\w+)\s*=\s*(new\s+ResourceLocation\s*\([^;]*?\))\s*;', re.S)
SUBSCRIBER = re.compile(r'@(?:Mod\.)?EventBusSubscriber\s*\(([^)]*)\)', re.S)
MODID_ARG = re.compile(r'modid\s*=\s*([\w."]+)')
SET_LINE = re.compile(r'\.setLine\s*\(\s*([\w.]+)\s*,')
RL_INLINE = re.compile(r'new\s+ResourceLocation\s*\(\s*([\w."]+)\s*,')
PLUGIN_UID = re.compile(r'getPluginUid\s*\(\s*\)\s*\{\s*return\s+([^;]+);', re.S)
JEI_TYPE = re.compile(r'RecipeType(?:\.create\s*\(\s*([^,]+),|<>\s*\(\s*(new\s+ResourceLocation\s*\([^)]*\)|[\w.]+))')
CHANNEL = re.compile(r'newSimpleChannel\s*\(\s*(new\s+ResourceLocation\s*\([^)]*\)|[\w.()"]+)', re.S)
LITERAL_CE = re.compile(r'\bliteral\s*\(\s*"(ce[a-z]\w*)"')
CE_CONST = re.compile(r'\bstatic\s+final\s+[\w<>\[\], ?]+\s+(CE_\w+)\s*=')


def java_files(base):
    for p in base.rglob("*.java"):
        if not SKIP_PARTS.intersection(p.relative_to(DEV).parts):
            yield p


def mod_roots():
    """Map root dir -> mod id."""
    roots = {}
    for toml in DEV.rglob("src/main/resources/META-INF/mods.toml"):
        if SKIP_PARTS.intersection(toml.relative_to(DEV).parts):
            continue
        root = toml.parents[4]
        mod_id = None
        props = root / "gradle.properties"
        if props.exists():
            m = re.search(r'^mod_id\s*=\s*(\S+)', props.read_text(), re.M)
            mod_id = m and m.group(1)
        if not mod_id:
            m = re.search(r'modId\s*=\s*"([^"$]+)"', toml.read_text())
            mod_id = m and m.group(1)
        if mod_id:
            roots[root] = mod_id
    return roots


def declared_ids(root):
    """Every [[mods]] entry's modId in the root's mods.toml (a jar can hold several mods)."""
    toml = root / "src/main/resources/META-INF/mods.toml"
    ids = set(re.findall(r'\[\[mods\]\]\s*modId\s*=\s*"([^"$]+)"', toml.read_text())) if toml.exists() else set()
    return ids


def root_of(path, roots):
    best = None
    for r in roots:
        if r in path.parents and (best is None or len(r.parts) > len(best.parts)):
            best = r
    return best


class Consts:
    """String constants: Class.FIELD -> raw expression, resolved lazily."""

    def __init__(self, files):
        self.raw = {}       # "Class.FIELD" -> (expr, defining file)
        self.by_file = {}   # path -> {FIELD: expr}
        for p in files:
            text = p.read_text(errors="replace")
            cls = CLASS_DECL.search(text)
            local = {}
            for name, expr in STR_CONST.findall(text):
                local[name] = expr.strip()
                if cls:
                    self.raw[f"{cls.group(1)}.{name}"] = (expr.strip(), p)
            self.by_file[p] = local

    def resolve(self, expr, path, depth=0):
        expr = expr.strip()
        if depth > 8:
            return None
        if expr.startswith('"') and expr.endswith('"'):
            return expr[1:-1]
        parts = expr.split(".")
        if len(parts) >= 2:
            key = f"{parts[-2]}.{parts[-1]}"
            if key in self.raw:
                expr2, where = self.raw[key]
                return self.resolve(expr2, where, depth + 1)
        local = self.by_file.get(path, {})
        if expr in local:
            return self.resolve(local[expr], path, depth + 1)
        return None


def rl_namespace(expr, path, text, consts):
    """Namespace of a ResourceLocation expression: new ResourceLocation(ns, path), or a field holding one."""
    expr = expr.strip()
    m = re.match(r'new\s+ResourceLocation\s*\(\s*(.+)\)\s*$', expr, re.S)
    if m:
        args = split_args(m.group(1))
        if len(args) >= 2:
            return consts.resolve(args[0], path)
        if len(args) == 1:
            v = consts.resolve(args[0], path)
            return v.split(":", 1)[0] if v and ":" in v else None
        return None
    name = expr.split(".")[-1]
    for fname, rl in RL_CONST.findall(text):
        if fname == name:
            return rl_namespace(rl, path, text, consts)
    return None


def split_args(s):
    out, depth, cur = [], 0, ""
    for ch in s:
        if ch == "," and depth == 0:
            out.append(cur)
            cur = ""
            continue
        depth += ch in "(<"
        depth -= ch in ")>"
        cur += ch
    if cur.strip():
        out.append(cur)
    return [a.strip() for a in out]


def main():
    verbose = "-v" in sys.argv
    roots = mod_roots()
    files = list(java_files(DEV))
    consts = Consts(files)
    findings, unresolved = [], []

    def flag(check, root, path, line, what, ns_or_id):
        mod_id = roots[root]
        if (check, mod_id, ns_or_id) in ALLOW:
            return
        rel = path.relative_to(ROOT)
        findings.append(f"{check:10} {rel}:{line}  {what}  (jar {mod_id})")

    def line_of(text, pos):
        return text.count("\n", 0, pos) + 1

    # waila_plugins.json keys
    for root, mod_id in roots.items():
        wp = root / "src/main/resources/waila_plugins.json"
        if wp.exists():
            for key in json.loads(wp.read_text()):
                if key.split(":", 1)[0] != mod_id:
                    flag("wthit", root, wp, 1, f"plugin id {key}", key)

    wthit_classes = set()
    for root in roots:
        wp = root / "src/main/resources/waila_plugins.json"
        if wp.exists():
            for spec in json.loads(wp.read_text()).values():
                for cls in spec.get("entrypoints", {}).values():
                    wthit_classes.add(cls.split(".")[-1])

    for p in files:
        root = root_of(p, roots)
        if root is None:
            continue
        mod_id = roots[root]
        text = p.read_text(errors="replace")
        own = root.name.startswith(OWN_PREFIXES) or root.parent.parent.name.startswith("creativeDevtools")
        is_eng = mod_id == "creativeengineering"

        accepted = {mod_id} | declared_ids(root)

        def check_ns(check, ns, pos, what):
            if ns is None:
                unresolved.append(f"{check:10} {p.relative_to(ROOT)}:{line_of(text, pos)}  {what}")
            elif ns not in accepted:
                flag(check, root, p, line_of(text, pos), f"{what} -> {ns}", f"{ns}:{what}")

        for m in SUBSCRIBER.finditer(text):
            a = MODID_ARG.search(m.group(1))
            if a:
                check_ns("subscriber", consts.resolve(a.group(1), p), m.start(), "modid")

        cls = CLASS_DECL.search(text)
        if cls and cls.group(1) in wthit_classes:
            seen = set()
            for m in list(SET_LINE.finditer(text)) + list(RL_INLINE.finditer(text)):
                if m.re is SET_LINE:
                    if any(f == m.group(1) for f, _ in RL_CONST.findall(text)):
                        continue  # its declaration is checked as an inline id
                    ns = rl_namespace(m.group(1), p, text, consts)
                    what = f"line {m.group(1)}"
                else:
                    if re.search(r'(?:Tags|TagKey)\.create\s*\([^;]*$', text[max(0, m.start() - 80):m.start()]):
                        continue  # tag key: pack data, kept (plan section 4)
                    ns = consts.resolve(m.group(1), p)
                    what = "line id"
                ln = line_of(text, m.start())
                if ns and ns.startswith("creative") and ns != mod_id and ln not in seen:
                    seen.add(ln)
                    flag("wthit", root, p, ln, f"{what} -> {ns}", ns)

        for m in PLUGIN_UID.finditer(text):
            check_ns("jei", rl_namespace(m.group(1), p, text, consts), m.start(), "plugin uid")

        if "mezz.jei" in text:
            for m in JEI_TYPE.finditer(text):
                if m.group(1):
                    ns = consts.resolve(m.group(1), p)
                    if ns is None and "getNamespace" in m.group(1):
                        continue  # mirrors a registered type id
                    what = "category"
                else:
                    ns = rl_namespace(m.group(2), p, text, consts)
                    what = "category"
                    if ns is None and "new ResourceLocation" not in m.group(2):
                        continue  # field from a registered recipe type
                if ns in ("armor_trims",):
                    continue  # names another mod's own category on purpose
                full = f"{ns}:{what}"
                if ns is not None and ns != mod_id and (("jei", mod_id, full) not in ALLOW):
                    # report the path too, for the baking allow-list
                    tail = text[m.end():m.end() + 80]
                    pm = re.match(r'\s*"([^"]+)"', tail)
                    idstr = f"{ns}:{pm.group(1)}" if pm else ns
                    if ("jei", mod_id, idstr) in ALLOW:
                        continue
                    flag("jei", root, p, line_of(text, m.start()), f"category {idstr}", idstr)
                elif ns is None:
                    unresolved.append(f"jei        {p.relative_to(ROOT)}:{line_of(text, m.start())}  category")

        for m in CHANNEL.finditer(text):
            arg = m.group(1)
            if "(" in arg and "ResourceLocation" not in arg:
                continue  # helper like Mod.id("main"): namespace is the port's own
            check_ns("channel", rl_namespace(arg, p, text, consts), m.start(), "channel")

        if own and not is_eng:
            for m in LITERAL_CE.finditer(text):
                if m.group(1) != "census":
                    flag("ce-name", root, p, line_of(text, m.start()), f'command /{m.group(1)}', m.group(1))
            for m in re.finditer(r'\b(?:class|interface|enum|record)\s+((?:CE|Ce)(?!Q[A-Z])[A-Z]\w*)', text):
                flag("ce-name", root, p, line_of(text, m.start()), f"class {m.group(1)}", m.group(1))
            for m in CE_CONST.finditer(text):
                flag("ce-name", root, p, line_of(text, m.start()), f"constant {m.group(1)}", m.group(1))

    for root, mod_id in roots.items():
        if mod_id.startswith("ce") and mod_id != "creativeengineering" and not mod_id.startswith("creative"):
            flag("ce-name", root, root / "gradle.properties", 1, f"mod id {mod_id}", mod_id)

    for f in sorted(set(findings)):
        print(f)
    if verbose:
        for u in sorted(set(unresolved)):
            print("unresolved " + u)
    print(f"{len(set(findings))} flagged, {len(set(unresolved))} unresolved (-v lists them)")
    return 1 if findings else 0


if __name__ == "__main__":
    sys.exit(main())
