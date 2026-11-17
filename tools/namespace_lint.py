#!/usr/bin/env python3
"""Namespace lint: every id a script or jar writes lands in the namespace of the jar that owns it.

plan_rules_fixes.md section 6 (D22, V-25 to V-28, V-31, "no jar registers wrongly"). The owner namespace comes from
the folder a file sits in:
	kubejs/data/<ns>/kubejs/, kubejs/assets/<ns>/kubejs/      -> <ns>
	development/<Jar>/.../kubejs_startup/ (members included)  -> the jar's mod id (lower-case folder name)
	kubejs/startup_scripts/, server_scripts/, client_scripts/ -> creativeengineering (the pack folder)

Checks:
	reg       a registration (`e.create(...)`, `reg.item(...)`) whose id is in another namespace and is not listed
	          in tools/namespace_allowlist.csv (id, reason). Ids given as dict keys are resolved through Common's dicts;
	          'ns:' + name reads the namespace only.
	nch       outside the pack folder: a recipe helper alias bound to `global.NCH` instead of
	          `global.NCH.forPack('<owner>')`, a direct `global.NCH.<helper>(` recipe call, or a forPack argument that
	          is not the owner
	logname   a LootJS log name `logName('XX/...')` whose prefix is not the owner's tag (R.logName makes it)
	java      `DeferredRegister.create(..., MODID)` whose mod id is not one the jar declares in its mods.toml; the
	          allowlist takes these as `<declared mod id>-><namespace>`

	python3 tools/namespace_lint.py            # report mode: print the summary, exit 0
	python3 tools/namespace_lint.py -v         # also every hit
	python3 tools/namespace_lint.py --write    # also write feature_writeup/namespace_report.md (generated)
	python3 tools/namespace_lint.py --strict   # exit 1 on any hit (after the P3 fixes)
"""
import csv
import datetime as dt
import re
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(ROOT / 'tools'))
import id_owner_lint  # noqa: E402

DICT_DIR = ROOT / 'development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib'
ALLOW_FILE = ROOT / 'tools/namespace_allowlist.csv'
REPORT = ROOT / 'feature_writeup/namespace_report.md'
PACK_NS = 'creativeengineering'
SKIP = re.compile(r'/build/|moved_code_backup/|_upstream/|/exported/|generation_scripts/')
# The tags R.logName writes (neoCraftHelper.js _packTags); a namespace not listed uses itself.
PACK_TAGS = {
	'creativeengineering': 'CE', 'creativedelight': 'CD', 'creativeworldgen': 'CWG', 'creativebrewing': 'CB',
	'creativeloot': 'CL', 'creativecommon': 'CCM', 'creativecompat': 'CCP', 'creativeequipment': 'CEQ',
	'creativemagic': 'CMG', 'creativecalendar': 'CCAL', 'creativedevtools': 'CDEV', 'creativelabels': 'CLAB',
}
# Global dicts and the aliases scripts bind them to.
DICTS = {'item': 'item_dict.js', 'blocks': 'block_dict.js', 'fluid': 'fluid_dict.js', 'effect': 'effect_dict.js'}
DEFAULT_ALIASES = {'i': 'item', 'item': 'item', 'b': 'blocks', 'f': 'fluid', 'ef': 'effect'}

ASSIGN = re.compile(r'^\s*[A-Za-z_$][\w$]*\.([\w$]+)\s*=\s*[\'"]([^\'"]+)[\'"]')
ALIAS = re.compile(r'\b(?:var|let|const)\s+(\w+)\s*=\s*global\.(item|blocks|fluid|effect)\b\s*[;\n]')
CREATE = re.compile(r'\b(?:e|event)\.create\(\s*([^,)]+)')
REG_CALL = re.compile(r'\b(\w+)\.(item|block|fluid|effect)\(\s*\w+\s*,\s*([^,)]+)')
REG_BIND = re.compile(r'\b(?:var|let|const)\s+(\w+)\s*=\s*global\.reg\.forPack\(\s*[\'"]([^\'"]+)[\'"]\s*\)')
NCH_ALIAS = re.compile(r'\b(?:var|let|const)\s+(\w+)\s*=\s*global\.NCH\s*(?:;|$)', re.M)
FORPACK = re.compile(r'global\.NCH\.forPack\(\s*[\'"]([^\'"]+)[\'"]\s*\)')
NCH_DIRECT = re.compile(r'global\.NCH\.(\w+)\(')
NCH_NOT_RECIPES = {'cfg', 'cfgClass', 'cfgValue', 'forPack', 'withNamespace', 'ns', 'registerConfig', 'logName', 'has',
	'toId', 'getAbsentCounts'}
# R.logName('x/y') and forPack('ns').logName('x/y') make the prefix themselves; a literal 'XX/...' anywhere else is checked.
LOGNAME = re.compile(r'(?<!R\.)(?<!\'\)\.)\blogName\(\s*[\'"]([A-Za-z]+)/')
HELPER_FILES = re.compile(r'kubejs_startup/creativecommon/func/')
DEFREG = re.compile(r'DeferredRegister\.create\(\s*([^;]*?)\)\s*;', re.S)


def load_dicts():
	values = {}
	for name, fname in DICTS.items():
		table = {}
		path = DICT_DIR / fname
		if path.exists():
			for line in path.read_text(encoding='utf-8').splitlines():
				m = ASSIGN.match(line)
				if m:
					table[m.group(1)] = m.group(2)
		values[name] = table
	return values


def load_allow():
	allow = {}
	if ALLOW_FILE.exists():
		with ALLOW_FILE.open(newline='', encoding='utf-8') as fh:
			for row in csv.DictReader(fh):
				allow[row['id'].strip()] = row['reason'].strip()
	return allow


def owner_of(rel):
	parts = rel.split('/')
	if parts[0] == 'kubejs' and len(parts) > 3 and parts[1] in ('data', 'assets') and parts[3] == 'kubejs':
		return parts[2], False
	if parts[0] == 'kubejs' and len(parts) > 1 and parts[1] in ('startup_scripts', 'server_scripts', 'client_scripts'):
		return PACK_NS, True
	if parts[0] == 'development' and 'kubejs_startup' in parts:
		return parts[1].lower(), False
	return None, False


def script_files():
	roots = [ROOT / 'kubejs/startup_scripts', ROOT / 'kubejs/server_scripts', ROOT / 'kubejs/client_scripts']
	roots += sorted((ROOT / 'kubejs/data').glob('*/kubejs')) + sorted((ROOT / 'kubejs/assets').glob('*/kubejs'))
	roots += sorted((ROOT / 'development').glob('*/src/main/resources/kubejs_startup'))
	roots += sorted((ROOT / 'development').glob('*/members/*/src/main/resources/kubejs_startup'))
	for r in roots:
		for p in sorted(r.rglob('*.js')):
			rel = str(p.relative_to(ROOT))
			if not SKIP.search('/' + rel):
				yield p, rel


def resolve_js(expr, aliases, dicts):
	expr = expr.strip()
	m = re.fullmatch(r'[\'"]([^\'"]+)[\'"]', expr)
	if m:
		return m.group(1)
	m = re.match(r'[\'"]([a-z0-9_.-]+):[^\'"]*[\'"]\s*\+', expr)
	if m:
		return m.group(1) + ':*'
	m = re.fullmatch(r'(?:global\.(item|blocks|fluid|effect)|(\w+))\.(\w+)', expr)
	if m:
		table = m.group(1) or aliases.get(m.group(2))
		if table:
			return dicts[table].get(m.group(3))
	return None


def check_scripts(dicts, allow, hits, unresolved):
	for path, rel in script_files():
		owner, pack_folder = owner_of(rel)
		if not owner:
			continue
		text = path.read_text(encoding='utf-8', errors='replace')
		lines = text.splitlines()
		aliases = dict(DEFAULT_ALIASES)
		for name, table in ALIAS.findall(text):
			aliases[name] = table
		reg_binds = dict(REG_BIND.findall(text))
		startup = '/kubejs_startup/' in rel or rel.startswith('kubejs/startup_scripts/')
		for n, line in enumerate(lines, 1):
			code = line.split('//', 1)[0] if not line.lstrip().startswith('//') else ''
			if not code.strip():
				continue
			ids = []
			if startup:
				for m in CREATE.finditer(code):
					ids.append(('create', m.group(1)))
				for m in REG_CALL.finditer(code):
					if m.group(1) in reg_binds:
						ids.append(('reg.' + m.group(2), m.group(3), reg_binds[m.group(1)]))
			for entry in ids:
				expr = entry[1]
				full = resolve_js(expr, aliases, dicts)
				if full is None:
					unresolved[rel].append((n, expr.strip()))
					continue
				if len(entry) == 3 and ':' not in full:
					full = entry[2] + ':' + full
				if ':' not in full:
					continue
				ns = full.split(':', 1)[0]
				if ns != owner and full not in allow:
					hits['reg'].append((rel, n, f'{full} registered in the {owner} pack'))
			if not pack_folder and not HELPER_FILES.search(rel):
				for m in FORPACK.finditer(code):
					if m.group(1) != owner:
						hits['nch'].append((rel, n, f"forPack('{m.group(1)}') in the {owner} pack"))
				for m in NCH_ALIAS.finditer(code):
					hits['nch'].append((rel, n, f'{m.group(1)} = global.NCH (use global.NCH.forPack(\'{owner}\'))'))
				for m in NCH_DIRECT.finditer(code):
					if m.group(1) not in NCH_NOT_RECIPES and not m.group(1).startswith('_'):
						hits['nch'].append((rel, n, f'global.NCH.{m.group(1)}( direct call (namespace {PACK_NS})'))
			for m in LOGNAME.finditer(code):
				tag = PACK_TAGS.get(owner, owner)
				if m.group(1) != tag:
					hits['logname'].append((rel, n, f"logName('{m.group(1)}/...') in the {owner} pack (tag {tag}, use R.logName)"))


def check_java(allow, hits, unresolved):
	roots = id_owner_lint.mod_roots()
	files = [p for p in id_owner_lint.java_files(id_owner_lint.DEV)]
	consts = id_owner_lint.Consts(files)
	for p in files:
		text = p.read_text(errors='replace')
		if 'DeferredRegister' not in text:
			continue
		root = id_owner_lint.root_of(p, roots)
		if root is None:
			continue
		own = id_owner_lint.declared_ids(root) | {roots[root]}
		rel = str(p.relative_to(ROOT))
		for m in DEFREG.finditer(text):
			args = id_owner_lint.split_args(m.group(1))
			if len(args) < 2:
				continue
			mod = consts.resolve(args[-1], p)
			n = text.count('\n', 0, m.start()) + 1
			if mod is None:
				unresolved[rel].append((n, args[-1].strip()))
			elif mod not in own and not mod.startswith('${') and not any(f'{o}->{mod}' in allow for o in own):
				hits['java'].append((rel, n, f'DeferredRegister for {mod}; the jar declares {", ".join(sorted(own))}'))


def main():
	verbose = '-v' in sys.argv
	dicts = load_dicts()
	allow = load_allow()
	hits = defaultdict(list)
	unresolved = defaultdict(list)
	check_scripts(dicts, allow, hits, unresolved)
	check_java(allow, hits, unresolved)
	total = sum(len(v) for v in hits.values())
	titles = {
		'reg': 'registrations in another namespace, not allowlisted',
		'nch': 'recipe helper not bound to the folder namespace (outside the pack folder)',
		'logname': 'LootJS log names with another prefix',
		'java': 'Java DeferredRegister for a mod id the jar does not declare',
	}
	out = []
	for key in ('reg', 'nch', 'logname', 'java'):
		per_file = defaultdict(int)
		for rel, _, _ in hits[key]:
			per_file[rel] += 1
		out.append(f'## {key}: {titles[key]}: {len(hits[key])} hits in {len(per_file)} files')
		for rel, c in sorted(per_file.items(), key=lambda kv: -kv[1]):
			out.append(f'   {c:5d}  {rel}')
		if verbose:
			for rel, n, msg in hits[key]:
				out.append(f'      {rel}:{n}  {msg}')
	nun = sum(len(v) for v in unresolved.values())
	out.append(f'## unresolved: {nun} registration or mod id arguments the lint cannot read (variables): not checked')
	if verbose:
		for rel, items in sorted(unresolved.items()):
			for n, expr in items:
				out.append(f'      {rel}:{n}  {expr}')
	out.append(f'namespace_lint: {total} hits ({len(allow)} allowlisted ids)')
	print('\n'.join(out))
	if '--write' in sys.argv:
		write_report(hits, unresolved, allow, titles)
	return 1 if ('--strict' in sys.argv and total) else 0


def write_report(hits, unresolved, allow, titles):
	today = dt.date.today().isoformat()
	lines = ['# Namespace report (generated)', '',
		f'Generated by `python3 tools/namespace_lint.py --write` on {today}. Do not edit: fix the scripts or the',
		'allowlist (`tools/namespace_allowlist.csv`) and rerun. This is the "no jar registers wrongly" list of',
		'[plan_rules_fixes.md](plan_rules_fixes.md) section 6: each hit is fixed in P3 or allowlisted with a reason.', '']
	for key in ('reg', 'nch', 'logname', 'java'):
		lines.append(f'## {titles[key][0].upper() + titles[key][1:]} ({len(hits[key])})')
		lines.append('')
		if not hits[key]:
			lines.append('None.')
		else:
			lines.append('| File | Line | Hit |')
			lines.append('|---|---|---|')
			for rel, n, msg in hits[key]:
				lines.append(f'| `{rel}` | {n} | {msg.replace("|", "/")} |')
		lines.append('')
	lines.append(f'## Allowlisted ids ({len(allow)})')
	lines.append('')
	for k, v in sorted(allow.items()):
		lines.append(f'- `{k}`: {v}')
	lines.append('')
	nun = sum(len(v) for v in unresolved.values())
	lines.append(f'## Not checked ({nun})')
	lines.append('')
	lines.append('Arguments the lint cannot resolve (a variable or a computed id), per file:')
	lines.append('')
	for rel, items in sorted(unresolved.items()):
		lines.append(f'- `{rel}`: {len(items)}')
	lines.append('')
	REPORT.write_text('\n'.join(lines), encoding='utf-8')
	print(f'wrote {REPORT.relative_to(ROOT)}')


if __name__ == '__main__':
	sys.exit(main())
