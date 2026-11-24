#!/usr/bin/env python3
"""One-off migration (plan_loading_tips_screens.md section 4, run 2026-11-14; kept for the record).

Restores Loading Tips to the unmodified upstream list (Loading Tips 1.2.1+26.1, 532 tips, text verbatim) with one
slug id per tip, and turns every change the pack made into Creative Tips overrides (`override: true`): a `remove` list
for dropped tips and same-id replacements for changed text. Creative Tips that rewrote an audited upstream tip become
same-id replacements; the rest stay new `creativetips:` tips.

    python3 tools/loading_tips_migrate.py            # dry run: prints the matches and the acceptance check
    python3 tools/loading_tips_migrate.py --write    # also writes both tips.json files (only if the check passes)

Run before the migration only: afterwards both files are in the new format and the script refuses to run.
"""
import difflib
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
UPSTREAM = ROOT / "DecompiledMods/loading_tips-1.2.1+26.1/assets/loading_tips/text/loading_tips.json"
LT = ROOT / "development/creativeCommon/src/main/resources/resourcepacks/loading_tips/assets/loading_tips/loading_tips/tips.json"
CT = ROOT / "kubejs/assets/creativetips/loading_tips/tips.json"
SLUG_MAX = 48


def slugify(text, taken):
	words = re.findall(r"[a-z0-9]+", text.lower().replace("'", ""))
	slug = ""
	for w in words:
		nxt = f"{slug}_{w}" if slug else w
		if len(nxt) > SLUG_MAX and slug:
			break
		slug = nxt
	base, n = slug or "tip", 2
	while slug in taken:
		slug = f"{base}_{n}"
		n += 1
	taken.add(slug)
	return slug


def norm(text):
	return re.sub(r"[.!]+$", "", text.strip()).lower()


def apply(files):
	"""Mirror of LoadingTips.apply: files in (priority, id) order; returns the ordered effective texts."""
	out = {}
	for fid, data in sorted(files, key=lambda f: (f[1].get("priority", 0), f[0])):
		if data.get("replace"):
			out.clear()
		override = data.get("override", False)
		if override:
			for rid in data.get("remove", []):
				out.pop(rid, None)
		for t in data["tips"]:
			tid, text = (t["id"], t["text"]) if isinstance(t, dict) else (None, t)
			text = text.strip()
			if not text:
				continue
			if tid is None:
				tid = f"{fid}:generated/{text}"
			if tid in out and not override:
				continue
			out[tid] = text
	return list(out.values())


def main():
	write = "--write" in sys.argv
	upstream = json.loads(UPSTREAM.read_text(encoding="utf-8"))["tips"]
	lt_old = json.loads(LT.read_text(encoding="utf-8"))
	ct_old = json.loads(CT.read_text(encoding="utf-8"))
	if any(isinstance(t, dict) for t in lt_old["tips"] + ct_old["tips"]):
		sys.exit("already migrated (tips are {id, text} objects)")
	lt_now, ct_now = lt_old["tips"], ct_old["tips"]
	target = lt_now + ct_now

	taken = set()
	up = [(f"loading_tips:{slugify(t, taken)}", t) for t in upstream]

	# 1. Current Loading Tips -> upstream: normalized exact match, then closest remaining (owner rewrites, reword).
	free = {i for i in range(len(up))}
	by_norm = {}
	for i, (_, t) in enumerate(up):
		by_norm.setdefault(norm(t), []).append(i)
	lt_match, fuzzy = {}, []
	for j, t in enumerate(lt_now):
		cand = [i for i in by_norm.get(norm(t), []) if i in free]
		if cand:
			lt_match[j] = cand[0]
			free.discard(cand[0])
	lt_manual = {  # owner rewrites (plan_creative_tips.md section 7), pinned by exact upstream text
		"Gold tools mine faster than diamond, are easy to enchant, and naturally apply Fortune or Looting. Unfortunately they aren't very durable": "Gold tools are faster but break easily.",
		"Villagers trade for emeralds, and every trade gives them experience toward better trades": "Villagers love emeralds.",
		"Fortune works on coal, copper, iron, gold, redstone, lapis, diamond, emerald and quartz ores, and on ancient debris": "Fortune works on coal, diamonds, lapis, copper, and iron.",
		"Beds explode when used outside the Overworld": "Beds explode in the Nether.",
		"Baby mobs have their own models instead of being shrunken adults": "Baby mobs now have unique models instead of being shrunken adults!",
	}
	for j, t in enumerate(lt_now):
		cand = [i for i in free if up[i][1] == lt_manual.get(t)]
		if j not in lt_match and cand:
			lt_match[j] = cand[0]
			free.discard(cand[0])
			fuzzy.append(("LT", 1.0, up[cand[0]][1], t))
	def closest(text, pool):
		best = max(pool, key=lambda i: difflib.SequenceMatcher(None, norm(text), norm(up[i][1])).ratio())
		return best, difflib.SequenceMatcher(None, norm(text), norm(up[best][1])).ratio()
	for j, t in enumerate(lt_now):
		if j not in lt_match:
			i, r = closest(t, free)
			lt_match[j] = i
			free.discard(i)
			fuzzy.append(("LT", r, up[i][1], t))

	# 2. Creative Tips -> the upstream tip each rewrote (pinned by exact upstream text, plan_creative_tips.md).
	manual = {
		"Glass breaks into glass shards unless mined with Silk Touch; shards craft back into glass": "Use a silk touch pickaxe for glass.",
		"Composters turn plants, food, straw, bark and sawdust into bone meal": "Composters turn food into bone meal.",
		"Kill a mob an Axolotl is fighting to gain Regeneration": "Axolotls help fight guardians and grant Regeneration when they kill one!",
		"Gilded apples give massive buffs": "Enchanted apples give massive buffs.",
		"Some Pillager outposts hold caged Allays": "Pillager Outposts hold trapped Allays.",
		"Leather boots let you walk on powder snow": "Leather boots let you walk on snow.",
		"Compasses point to spawn in the Overworld, to the portal you came through in the Nether, and to the exit portal in the End": "Compasses point to world spawn.",
		"Lava flowing over soul soil next to blue ice turns into basalt": "Basalt forms from blue ice and lava.",
		"Water and lava make cobblestone, or other stones when the lava touches certain blocks": "Cobblestone forms from water and lava.",
		"Every player can take an elytra from each End ship": "End ships hold the Elytra.",
		"Facing a Creeper blast with a shield cuts the damage; better shields block more": "Shields can block Creeper explosions if you are facing them.",
		"Sweet berry bushes slow you down and hurt you unless you wear leggings": "Sweet Berry bushes slow you down and deal damage.",
		"A totem of undying saves you from your hand or your charm slot": "Totems of Undying must be held to save you from death.",
		"Lava buckets are a great furnace fuel": "Lava buckets are the best furnace fuel.",
	}
	ct_match = {}
	for j, t in enumerate(ct_now):
		pool = [i for i in free if up[i][1] == manual.get(t)]
		if pool:
			i, r = closest(t, pool)
			ct_match[j] = i
			free.discard(i)
			fuzzy.append(("CT", r, up[i][1], t))
		else:
			fuzzy.append(("CT", 0.0, "(no match: new creativetips tip)", t))

	# 3. Overrides.
	replaced = {}
	for j, i in lt_match.items():
		if lt_now[j] != up[i][1]:
			replaced[i] = lt_now[j]
	for j, i in ct_match.items():
		replaced[i] = ct_now[j]
	remove = [up[i][0] for i in sorted(free)]
	ct_taken = set()
	ct_tips = [{"id": up[i][0], "text": replaced[i]} for i in sorted(replaced)]
	ct_tips += [{"id": f"creativetips:{slugify(t, ct_taken)}", "text": t}
				for j, t in enumerate(ct_now) if j not in ct_match]
	lt_new = {"replace": False, "priority": 0, "tips": [{"id": i, "text": t} for i, t in up]}
	ct_new = {"override": True, "priority": 10, "remove": remove, "tips": ct_tips}

	print("Matches needing review (source, similarity, upstream -> current):")
	for src, r, a, b in sorted(fuzzy, key=lambda f: (f[0], f[1])):
		print(f"  {src} {r:.2f}\n     up:  {a}\n     now: {b}")
	norm_only = sum(1 for i, t in replaced.items() if norm(t) == norm(up[i][1]))
	print(f"\nupstream {len(up)}, removed {len(remove)}, replaced {len(replaced)} "
		  f"({norm_only} normalization only), new creative tips {len(ct_now) - len(ct_match)}")

	# 4. Acceptance: same texts, same set as the old effective list.
	result = apply([("creativetips:loading_tips/tips", ct_new), ("loading_tips:loading_tips/tips", lt_new)])
	ok = sorted(result) == sorted(target) and len(result) == len(target)
	print(f"acceptance: {len(result)} effective vs {len(target)} old -> {'PASS' if ok else 'FAIL'}")
	if not ok:
		print("  missing:", sorted(set(target) - set(result))[:10])
		print("  extra:", sorted(set(result) - set(target))[:10])
		sys.exit(1)
	if write:
		LT.write_text(json.dumps(lt_new, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
		CT.write_text(json.dumps(ct_new, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
		print("wrote", LT.relative_to(ROOT), "and", CT.relative_to(ROOT))


if __name__ == "__main__":
	main()
