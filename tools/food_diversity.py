#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""Food diversity table for Spice of Life: Raspberry Pie Addition (plan_food_diversity_1121.md section 3).

Reads the /dumpfood export (kubejs/exported/food/foods.json, recipes.json), the food types
(kubejs/generation_scripts/food/food_types.json, canned sources) and the rules
(kubejs/generation_scripts/food/food_diversity.json); writes every food's diversity and the families into
development/creativeDelight/src/main/resources/solraspberrypie/diversity/creativedelight.json (plan_rp_standalone.md P8:
Raspberry Pie reads every solraspberrypie/diversity/*.json in any mod's jar and ships none). A Creative Delight rebuild
applies it.

    python3 tools/food_diversity.py            write the table, print the tier tops
    python3 tools/food_diversity.py --report   also print the rotation table (audit 1.1, golden and gilded split)
    python3 tools/food_diversity.py --check    exit 1 if the written table is stale or the tier order breaks

Rule order per food (first that fits): drinks and no-diet foods 0; cans = canShare x source; cooked meat or fish
heated from a raw source = vanilla meat fit on q (cooked dragon meat between the high top and the dragon dishes);
baking and filling = the pot fit on the inputs; shaped, shapeless and assembled crafts that are no raw food's cooked
version = max(own curve, pot fit); Cooking Pot dishes = own curve, floored at the best food input. Then the dragon,
gilded, feast and golden multipliers (golden and gilded on their source food), then the low / mid / high caps, then
families (max of the members).
"""
import argparse
import json
import math
import os
import re
import statistics
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FOODS = os.path.join(ROOT, 'kubejs/exported/food/foods.json')
RECIPES = os.path.join(ROOT, 'kubejs/exported/food/recipes.json')
TYPES = os.path.join(ROOT, 'kubejs/generation_scripts/food/food_types.json')
RULES = os.path.join(ROOT, 'kubejs/generation_scripts/food/food_diversity.json')
DICT = os.path.join(ROOT, 'development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js')
OUT = os.path.join(ROOT, 'development/creativeDelight/src/main/resources/solraspberrypie/diversity/creativedelight.json')
NOURISH = 'farmersdelight:nourishment'
DRINK_TAG = 'solraspberrypie:drinks'
NO_DIET = {'lightmanscurrency:coin_chocolate_' + m for m in ('copper', 'iron', 'gold', 'emerald', 'diamond', 'netherite')}


def load_dict():
	out = {}
	with open(DICT, encoding='utf-8') as fh:
		for m in re.finditer(r"^i\.(\w+)\s*=\s*'([^']+)'", fh.read(), re.M):
			out[m.group(1)] = m.group(2)
	return out


def curve(q, pts):
	if q <= pts[0][0]:
		return pts[0][1]
	for (q0, v0), (q1, v1) in zip(pts, pts[1:]):
		if q <= q1:
			return v0 + (v1 - v0) * (q - q0) / (q1 - q0)
	return pts[-1][1]


def parse_in(s):
	"""A recipe input string -> list of alternatives ({'item': id} or {'tag': t})."""
	try:
		v = json.loads(s)
	except (ValueError, TypeError):
		return []
	return v if isinstance(v, list) else [v]


class Table:
	def __init__(self):
		self.rules = json.load(open(RULES, encoding='utf-8'))
		self.foods = {f['id']: f for f in json.load(open(FOODS, encoding='utf-8'))['foods']}
		recipes = json.load(open(RECIPES, encoding='utf-8'))['recipes']
		types = json.load(open(TYPES, encoding='utf-8'))
		keys = load_dict()
		self.excluded = {keys[k] for k in types.get('excluded', {}) if k in keys}
		self.cans = {}
		for can, srcs in types.get('canned', {}).items():
			if can in keys:
				ids = [keys[s] for s in (srcs if isinstance(srcs, list) else [srcs]) if s in keys]
				self.cans[keys[can]] = ids
		# Types: food_types.json first (it is ahead of the dump after an edit), the dump's tags as fallback.
		self.type = {}
		for ty, ks in types.get('types', {}).items():
			for k in ks:
				if k in keys:
					self.type[keys[k]] = ty
		for fid, f in self.foods.items():
			if fid in self.type:
				continue
			ts = [t.split('food_type/', 1)[1] for t in f['tags'] if t.startswith('creativedelight:food_type/')]
			self.type[fid] = max(ts, key=len) if ts else ''
		self.tag_members = {}
		for fid, f in self.foods.items():
			for t in f['tags']:
				self.tag_members.setdefault(t, []).append(fid)
		self.by_out = {}
		for r in recipes:
			for o in r.get('out', []):
				if o['id'] in self.foods:
					self.by_out.setdefault(o['id'], []).append(r)
		self.final = {}
		self.why = {}
		self._busy = set()
		self._fitting = False

	# ---- basics
	def q(self, fid):
		f = self.foods[fid]
		return f['nutrition'] * (1 + f['saturationModifier']) / 2

	def base(self, fid):
		return curve(self.q(fid), self.rules['curve'])

	def has_type(self, fid, t):
		ty = self.type.get(fid, '')
		return ty == t or ty.startswith(t + '/')

	def is_drink(self, fid):
		return DRINK_TAG in self.foods[fid]['tags'] or self.has_type(fid, 'drink')

	def tier(self, fid):
		f = self.foods[fid]
		if fid in self.excluded or fid in NO_DIET or self.is_drink(fid):
			return 'none'
		if self.has_type(fid, 'canned'):
			return 'canned'
		if self.rules['dragonIdPart'] in fid:
			return 'dragon'
		if self.has_type(fid, 'golden/enchanted'):
			return 'gilded'
		if self.has_type(fid, 'golden'):
			return 'golden'
		if self.has_type(fid, 'meal/feast') or 'creativedelight:food_family/feast' in f['tags']:
			return 'feast'
		q = self.q(fid)
		if any(e['effect'] == NOURISH for e in f.get('effects', [])) or q >= self.rules['midMax']:
			return 'high'
		return 'mid' if q >= self.rules['lowMax'] else 'low'

	# ---- inputs
	def input_value(self, alt, level):
		"""Value of one recipe input alternative and whether it is a food."""
		nf = self.rules['nonFood']
		if 'item' in alt:
			i = alt['item']
			if i in self.foods and i not in self.excluded:
				return (self.base(i) if self._fitting else self.pre(i, level)), True
			if i in nf['zero']:
				return 0.0, False
			return nf['items'].get(i, nf['default']), False
		if 'tag' in alt:
			members = [m for m in self.tag_members.get(alt['tag'], []) if m not in self.excluded]
			if members:
				return statistics.median((self.base(m) if self._fitting else self.pre(m, level)) for m in members), True
			return nf['default'], False
		return 0.0, False

	def inputs(self, r, level):
		"""(sum of input values per output item, best food input value, best food input id)."""
		total, best, best_id = 0.0, 0.0, None
		for s in r.get('in', []):
			alts = parse_in(s)
			if not alts:
				continue
			vals = [(self.input_value(a, level), a.get('item')) for a in alts]
			(v, food), iid = max(vals, key=lambda x: x[0][0])
			total += v
			if food and v > best:
				best, best_id = v, iid
		n = max(1, sum(o.get('n', 1) for o in r['out'] if o['id'] in self.foods) or 1)
		return total / n, best, best_id

	# ---- the pot fit (R7)
	def fit_pot(self):
		self._fitting = True
		xs, ys, gs = [], [], []
		for fid, rs in self.by_out.items():
			if self.tier(fid) in ('none', 'canned'):
				continue
			for r in rs:
				if r['type'] in self.rules['potTypes']:
					s, b, _ = self.inputs(r, 0)
					xs.append(s)
					ys.append(self.base(fid))
					gs.append(self.base(fid) - b)
		self._fitting = False
		# median ratio of a dish to the sum of its inputs (per output item): scales with what goes in
		self.pot = (statistics.median(y / x for x, y in zip(xs, ys) if x > 0), len(xs))
		self.pot_inputs = statistics.median(xs)
		self.pot_gain = statistics.median(gs) if gs else 0.0

	def pot_value(self, s):
		return self.pot[0] * s

	def fit_meat(self):
		pts = [(self.q(m), self.base(m)) for m in self.rules['vanillaMeats'] if m in self.foods]
		self.meat_k = sum(q * v for q, v in pts) / sum(q * q for q, v in pts)

	# ---- per food, before tiers
	def heated_from_raw(self, fid):
		for r in self.by_out.get(fid, []):
			if r['type'] in self.rules['heatTypes']:
				for s in r.get('in', []):
					for a in parse_in(s):
						if a.get('item') in self.foods and self.has_type(a['item'], 'raw'):
							return True
		return False

	def pre(self, fid, level=0):
		"""Value before tier multipliers, caps and families."""
		if fid in self.final:
			return self.final[fid]
		if fid in self._busy or level > 12:
			return self.base(fid)
		self._busy.add(fid)
		try:
			v, why = self._pre(fid, level + 1)
		finally:
			self._busy.discard(fid)
		self.final[fid] = v
		self.why[fid] = why
		return v

	def _pre(self, fid, level):
		t = self.tier(fid)
		if t == 'none':
			return 0.0, 'out of the diet' if fid in NO_DIET or fid in self.excluded else 'drink'
		if t == 'canned':
			srcs = [s for s in self.cans.get(fid, []) if s in self.foods]
			if srcs:
				return self.rules['canShare'] * max(self.pre(x, level) for x in srcs), 'can'  # redone after tiers
			return self.base(fid), 'can, no source'
		if self.has_type(fid, 'cooked/meat') or self.has_type(fid, 'cooked/fish'):
			if self.heated_from_raw(fid) or fid in self.rules['vanillaMeats']:
				return self.meat_k * self.q(fid), 'meat fit'
		rs = self.by_out.get(fid, [])
		own = self.base(fid)
		pot = [self.inputs(r, level) for r in rs if r['type'] in self.rules['potTypes']]
		if pot:
			best = max(b for _, b, _ in pot)
			if own < best + self.rules['dishFloorStep']:
				return best + self.rules['dishFloorStep'], 'pot, floored at input'
			return own, 'pot'
		repl = [self.inputs(r, level) for r in rs if r['type'] in self.rules['potRuleReplace']]
		# the dump lists no fluid for a filling: add the pot mirror's coating (R3, 1.5 coating items per dish)
		fc = self.rules['fillingCoating']
		coat = fc['share'] * self.pre(fc['item'], level) if fc['item'] in self.foods else 0.0
		repl = [(x + coat, b, i) if r['type'] == 'create:filling' else (x, b, i)
			for r, (x, b, i) in zip([r for r in rs if r['type'] in self.rules['potRuleReplace']], repl)]
		if repl:
			s, best, _ = max(repl)
			return max(self.pot_value(s), best + self.rules['dishFloorStep']), 'pot rule (' + 'baking/filling' + ')'
		if not any(self.has_type(fid, k) for k in self.rules['craftSkipTypes']) and not self.heated_from_raw(fid):
			craft = [self.inputs(r, level) for r in rs if r['type'] in self.rules['potRuleTypes']]
			if craft:
				s, best, _ = max(craft)
				pv = max(self.pot_value(s), best + self.rules['dishFloorStep'])
				if pv > own:
					return pv, 'pot rule (craft)'
		return own, 'curve'

	def golden_source(self, fid, seen=None):
		seen = seen or set()
		seen.add(fid)
		best, best_id = 0.0, None
		for r in self.by_out.get(fid, []):
			for s in r.get('in', []):
				for a in parse_in(s):
					i = a.get('item')
					if i in self.foods and i not in seen:
						if self.tier(i) in ('golden', 'gilded'):
							v = self.golden_source(i, seen)
						else:
							v = self.pre(i)
						if v > best:
							best, best_id = v, i
		if best_id:
			return best
		# Sequenced assembly lists no inputs in the dump: a gilded food's source is its golden food's source.
		ns, path = fid.split(':', 1)
		if path.startswith('enchanted_golden_'):
			rest = path[len('enchanted_'):]
			for cand in (ns + ':' + rest, 'minecraft:' + rest, 'goldenfood:' + rest):
				if cand in self.foods and cand not in seen:
					return self.golden_source(cand, seen)
		return self.base(fid)

	# ---- whole table
	@staticmethod
	def fit_top(values, top, rank):
		"""Multiplier that lifts the rank-th best value (0 = best) to the tier top; values above it are capped."""
		vs = sorted(values, reverse=True)
		ref = vs[min(rank, len(vs) - 1)] if vs else 0
		return top / ref if ref > 0 else 1.0

	def build(self):
		self.fit_meat()
		self.fit_pot()
		ids = [f for f in self.foods if f not in self.excluded]
		for fid in ids:
			self.pre(fid)
		tops = self.rules['tierTops']
		tier = {fid: self.tier(fid) for fid in ids}
		val = {}
		# golden and gilded: source food x multiplier
		for t in ('golden', 'gilded'):
			src = {fid: self.golden_source(fid) for fid in ids if tier[fid] == t}
			m = self.fit_top(src.values(), tops[t], self.rules['topRank'])
			for fid, v in src.items():
				val[fid] = min(v * m, tops[t])
				self.why[fid] = '%s: source %.2f x %.2f' % (t, v, m)
		# dragon and feasts: multiplier fitted to the top; cooked dragon meat between high top and dragon dishes
		dragon_cooked = [fid for fid in ids if tier[fid] == 'dragon'
			and any(self.has_type(fid, k) for k in self.rules['dragonCookedTypes'])]
		dragon_raw = [fid for fid in ids if tier[fid] == 'dragon' and self.has_type(fid, 'raw')]
		dishes = [fid for fid in ids if tier[fid] == 'dragon' and fid not in dragon_cooked and fid not in dragon_raw]
		m = self.fit_top([self.final[f] for f in dishes], tops['dragon'], self.rules['topRank'])
		for fid in dishes:
			val[fid] = min(self.final[fid] * m, tops['dragon'])
			self.why[fid] = 'dragon dish x %.2f' % m
		low_dish = min(val[f] for f in dishes)
		target = (tops['high'] + low_dish) / 2
		mc = target / max(self.final[f] for f in dragon_cooked)
		for fid in dragon_cooked:
			val[fid] = self.final[fid] * mc
			self.why[fid] = 'cooked dragon meat x %.2f' % mc
		for fid in dragon_raw:
			val[fid] = min(self.final[fid], tops['high'])
			self.why[fid] = 'raw dragon meat, curve'
		feasts = [fid for fid in ids if tier[fid] == 'feast']
		mf = self.fit_top([self.final[f] for f in feasts], tops['feast'], self.rules['topRank'])
		for fid in feasts:
			val[fid] = min(self.final[fid] * mf, tops['feast'])
			self.why[fid] = 'feast x %.2f' % mf
		for fid in ids:
			if tier[fid] in ('high', 'mid', 'low'):
				val[fid] = min(self.final[fid], tops[tier[fid]])
			elif tier[fid] == 'none':
				val[fid] = 0.0
		# baking (plan_food_diversity_1121.md R13): best ingredient + the pot's median boost, scaled by the whole batch's input value
		# against a typical pot recipe's, capped one tier above the food's own (low -> mid top, mid -> high top)
		up = {'low': 'mid', 'mid': 'high', 'high': 'high'}
		cand = {}
		for fid in ids:
			if tier[fid] not in up or any(r['type'] in self.rules['potTypes'] for r in self.by_out.get(fid, [])):
				continue
			bakes = []
			for r in self.by_out.get(fid, []):
				if r['type'] == 'creativedelight:baking':
					per, best, _ = self.inputs(r, 0)
					n = max(1, sum(o.get('n', 1) for o in r['out'] if o['id'] == fid))
					bakes.append((per * n / self.pot_inputs if self.pot_inputs else 1.0, best))
			if bakes:
				cand[fid] = bakes

		def baked(k):
			out = {}
			for fid, bakes in cand.items():
				v = max(best + self.pot_gain * min(1.0, k * sh) for sh, best in bakes)
				out[fid] = min(max(v, val[fid]), tops[up[tier[fid]]])
			return out

		def gain(out):
			return statistics.median(out[f] - max(b for _, b in cand[f]) for f in out) if out else 0.0
		lo, hi = 0.0, 50.0  # batch-share scale fitted so baking's median boost matches the pot's
		for _ in range(40):
			k = (lo + hi) / 2
			lo, hi = (k, hi) if gain(baked(k)) < self.pot_gain else (lo, k)
		self.bake_k = (lo + hi) / 2
		for fid, v in baked(self.bake_k).items():
			if v != val[fid]:
				val[fid] = v
				self.why[fid] = 'baking: best input + %.2f x batch share' % self.pot_gain
				if v > tops[tier[fid]]:
					tier[fid] = up[tier[fid]]
		# floors (plan R11): golden at least the average high-tier meal, gilded at least the average feast
		meals = [val[f] for f in ids if tier[f] == 'high' and self.has_type(f, 'meal')]
		floors = {'golden': statistics.mean(meals), 'gilded': statistics.mean(val[f] for f in feasts)}
		self.floors = floors
		for fid in ids:
			t = tier[fid]
			if t in floors and val[fid] < floors[t]:
				val[fid] = floors[t]
				self.why[fid] += ', floored at %.2f' % floors[t]
		# coated food and candies (plan R12): mid-tier diversity, never below the best food input
		cap = tops[self.rules['coated']['tier']]
		for fid in self.coated(ids):
			best = max([b for _, b, _ in (self.inputs(r, 0) for r in self.by_out.get(fid, []) if r['type'] in ('create:filling', 'farmersdelight:cooking'))] or [0.0])
			v = min(val[fid], max(cap, best + self.rules['dishFloorStep']))
			if v < val[fid]:
				val[fid] = v
				self.why[fid] = 'coated: capped at %.2f' % v
			if v <= cap:
				tier[fid] = self.rules['coated']['tier']  # counts (and scales) as a mid-tier food
		for fid in ids:
			if tier[fid] == 'canned':
				srcs = [s for s in self.cans.get(fid, []) if s in val]
				if srcs:
					val[fid] = self.rules['canShare'] * max(val[s] for s in srcs)
					self.why[fid] = 'can: %.0f%% of %s' % (100 * self.rules['canShare'], max(srcs, key=lambda s: val[s]))
				else:
					val[fid] = min(self.final[fid] or 0.0, tops['high'])
		self.families = self.build_families()
		capped = lambda m: tops.get(tier.get(m), tops['high'] if tier.get(m) == 'canned' else 99)
		for name, members in self.families.items():
			top = min(max(val.get(m, 0.0) for m in members), min(capped(m) for m in members))
			for mm in members:
				if mm in val:
					val[mm] = top
		self.scales = self.fit_ceilings(val, tier)
		for fid in val:
			val[fid] *= self.scales.get(self.entry_tier(fid, tier), 1.0)
		self.val = {k: round(v, 4) for k, v in sorted(val.items())}
		self.tiers = tier

	def entry_tier(self, fid, tier):
		"""The tier a food counts in for the ceiling table: a can in its source's tier."""
		tr = tier.get(fid, 'none')
		if tr == 'canned':
			src = [x for x in self.cans.get(fid, []) if x in tier]
			tr = tier[src[0]] if src else 'high'
			if tr == 'canned':
				tr = 'high'
		return tr

	def entries(self, val, tier):
		"""(value, tier) per diet entry: a family once (its head), drinks and no-diet foods left out."""
		head = {m: ms[0] for ms in self.families.values() for m in ms[1:]}
		ent = {}
		for fid, v in val.items():
			tr = self.entry_tier(fid, tier)
			if tr != 'none':
				ent[head.get(fid, fid)] = (v, tr)
		return list(ent.values())

	def fit_ceilings(self, val, tier):
		"""Per-tier scale so the cumulative columns' ceilings step evenly from ceilings.top down to ceilings.low
		(owner, plan R14). Fitted bottom-up: low only, then mid and low, then high, golden, feasts, gilded, dragon."""
		c = self.rules.get('ceilings')
		if not c:
			return {}
		ent = self.entries(val, tier)
		chain = c['chain']  # tiers added from the bottom: low, mid, high, golden, feast, gilded, dragon
		targets = c.get('targets') or [c['low'] + (c['top'] - c['low']) * i / (len(chain) - 1) for i in range(len(chain))]
		scales = {}
		for k, t in enumerate(chain):
			inside = set(chain[:k + 1])

			def ceil(f):
				vs = [v * (f if tr == t else scales.get(tr, 1.0)) for v, tr in ent if tr in inside]
				return max(rotation(vs, n, c['memory']) for n in range(1, c['maxRotation'] + 1))
			lo, hi = 0.0, 20.0
			for _ in range(50):
				m = (lo + hi) / 2
				lo, hi = (m, hi) if ceil(m) < targets[k] else (lo, m)
			scales[t] = (lo + hi) / 2
		self.ceiling_targets = dict(zip(chain, targets))
		return scales

	def coated(self, ids):
		"""Food made by a Create filling (a dip or coating) plus the listed candies; drinks and dragon, golden tiers excluded."""
		c = self.rules['coated']
		out = set(i for i in c['items'] if i in self.foods)
		for fid in ids:
			for r in self.by_out.get(fid, []):
				# a coating dips a food; a filling into a bowl, bottle, jar or cake base is not one
				if r['type'] == 'create:filling' and any(
						a.get('item') in self.foods or (a.get('tag') in self.tag_members)
						for x in r.get('in', []) for a in parse_in(x)):
					out.add(fid)
		skip = set(c.get('skip', []))
		return sorted(f for f in out if f not in skip and self.tier(f) in ('high', 'mid', 'low', 'feast'))

	def build_families(self):
		fam = {}
		cfg = self.rules['families']
		for form in cfg['pepperForms']:
			for g in cfg['pepperGroups']:
				ids = ['rusticdelight:%s_%s' % (form, c) for c in g]
				ids = [i for i in ids if i in self.foods]
				if len(ids) > 1:
					fam['%s_%s' % (form, '_'.join(g))] = ids
		for name, ids in cfg['lists'].items():
			ids = [i for i in ids if i in self.foods]
			if len(ids) > 1:
				fam[name] = ids
		return fam

	def doc(self):
		return {
			'_readme': 'Generated by tools/food_diversity.py from food_diversity.json and the /dumpfood export; do not edit.',
			'values': self.val,
			'families': self.families,
		}


def rotation(vals, n, memory=32):
	top = sorted(vals, reverse=True)[:n]
	if not top:
		return 0.0
	return statistics.mean(top) * (len(top) - len(top) * (len(top) - 1) / (2 * memory))


def report(t):
	r, n = t.pot
	print('pot fit: value = %.3f x inputs per output (median of %d pot recipes); meat fit: value = %.3f x q' % (r, n, t.meat_k))
	chain = t.rules['ceilings']['chain'][::-1]  # dragon .. low
	names = ['None', 'No dragon', 'No dragon, gilded', 'No D, G, feasts', 'No D, G, F, golden', 'Mid and low', 'Low only',
		'High and feasts, no golden']
	ent = t.entries(t.val, t.tiers)
	pools = [[v for v, tr in ent if tr in chain[i:]] for i in range(len(chain))]
	pools.append([v for v, tr in ent if tr in ('feast', 'high', 'mid', 'low')])
	print('scales: ' + ', '.join('%s %.3f' % (k, v) for k, v in t.scales.items()))
	print('| Rotation | ' + ' | '.join(names) + ' |')
	print('|---:|' + '---:|' * len(names))
	for n in (1, 2, 3, 4, 5, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32):
		print('| %d | ' % n + ' | '.join('%.1f' % rotation(pl, n) for pl in pools) + ' |')
	best = [max((rotation(pl, n), n) for n in range(1, 33)) for pl in pools]
	print('| Ceiling (size) | ' + ' | '.join('%.1f (%d)' % b for b in best) + ' |')
	print('| Foods | ' + ' | '.join(str(len(pl)) for pl in pools) + ' |')
	order = chain
	print('Top 5 per tier:')
	head = {m: ms[0] for ms in t.families.values() for m in ms[1:]}
	byid = {}
	for fid, v in t.val.items():
		tr = t.entry_tier(fid, t.tiers)
		if tr != 'none':
			byid[head.get(fid, fid)] = (v, tr)
	for tr in order:
		top = sorted(((v, f) for f, (v, x) in byid.items() if x == tr), reverse=True)[:5]
		print('  %-7s ' % tr + ', '.join('%s %.2f' % (f.split(':')[1], v) for v, f in top))


def tier_tops(t):
	out = {}
	for fid, v in t.val.items():
		tr = t.tiers[fid]
		if tr in ('none', 'canned'):
			continue
		out[tr] = max(out.get(tr, 0.0), v)
	return out


def main():
	ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
	ap.add_argument('--report', action='store_true')
	ap.add_argument('--check', action='store_true')
	args = ap.parse_args()
	t = Table()
	t.build()
	tops = tier_tops(t)
	order = ['dragon', 'gilded', 'feast', 'golden', 'high', 'mid', 'low']
	print('tier tops: ' + ', '.join('%s %.2f' % (k, tops.get(k, 0)) for k in order))
	bad = [(a, b) for a, b in zip(order, order[1:]) if tops.get(a, 0) <= tops.get(b, 0)]
	text = json.dumps(t.doc(), indent=1, sort_keys=False) + '\n'
	if args.check:
		cur = open(OUT, encoding='utf-8').read() if os.path.exists(OUT) else ''
		if cur != text:
			print('FAIL: %s is stale; run python3 tools/food_diversity.py' % os.path.relpath(OUT, ROOT))
			return 1
		if bad:
			print('FAIL: tier order broken at %s' % bad)
			return 1
		print('OK')
		return 0
	os.makedirs(os.path.dirname(OUT), exist_ok=True)
	with open(OUT, 'w', encoding='utf-8') as fh:
		fh.write(text)
	print('wrote %s: %d values, %d families' % (os.path.relpath(OUT, ROOT), len(t.val), len(t.families)))
	if bad:
		print('WARN: tier order broken at %s' % bad)
	if args.report:
		report(t)
	return 0


if __name__ == '__main__':
	sys.exit(main())
