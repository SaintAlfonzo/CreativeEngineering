#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-only
"""One-off (r1118 judge-9, 2026-11-18): restore the Power Grid P11 world setup that the move of the pwgr1.qb batch
into feature_writeup/testing/rows/PWG.qb dropped (perf/runs/r1118/judge/j9.md). The batch's setup rows SQ-PWG-P11A
(forceload of every test area), SETUP9 (gamerules, clear the generated animals) and SETUP* were not rows, so r1118
step s94 ran every section 2 row in unloaded chunks (`read unloaded`, `That position is not loaded`, `paused=true`).

Every runnable row of PWG.qb sections 2 and 3 gets a self-contained block before its first step: the P11A forceloads
(ponder strip left out; it is only for the `zpwg5 nbtall` pass) plus the SQ-PWG-97 spot at Z 2100, the gamerules, and a
one-time kill of the generated animals guarded by a scoreboard flag (the first row of a world runs it; later rows and
the wire entities they build are left alone). The block runs at pace 0.3 and resets pace to 2 (the file default).
Then the row-specific fixes in FIX (text replacements, each applied once) and the SQ-PWG-38B line build prepended to
SQ-PWG-39 (-39, -44 and -07 run on that line; -38B was a batch-only row).

    python3 tools/scratch_query/rowfix_pwg.py [--dry]     (idempotent: rows that carry MARK are skipped)
"""
import re
import sys
from pathlib import Path

INST = Path(__file__).resolve().parents[2]
QB = INST / 'feature_writeup/testing/rows/PWG.qb'
SRC = INST / 'tools/scratch_query/batches/r1113pwg/pwgr1.qb'
MARK = '# self-contained since r1118 (judge-9, 2026-11-18)'
SECTIONS = {'2', '3'}
AREAS = ['990 990 1060 1090', '1090 990 1140 1260', '1085 1385 1125 1520', '1095 1525 1115 1590',
		 '1290 1495 1320 1680', '1090 1695 1130 1800', '1095 1670 1165 1685', '1090 1805 1130 1850',
		 '1135 1855 1200 1915', '1095 1920 1200 1990', '1095 1995 1135 2085', '1095 2095 1110 2110']
BLOCK = [MARK + ': area setup of pwgr1.qb SQ-PWG-P11A / SETUP9 (batch-only rows, lost in the move to rows)',
		 'pace 0.3'] + ['> forceload add ' + a for a in AREAS] + [
	'> gamerule doMobSpawning false', '> gamerule doFireTick false', '> scoreboard objectives add pwgsetup dummy',
	'sleep 4', '> execute unless score #done pwgsetup matches 1 run weather clear',
	'> execute unless score #done pwgsetup matches 1 run kill @e[type=!minecraft:player]',
	'> scoreboard players set #done pwgsetup 1', 'pace 2']
LVL = 'L=$(sed -n "s/^level-name=//p" server.properties); '
# (row, old, new): plain text, replaced once inside the row
FIX = [
	('SQ-PWG-71', r'expect \[zpwg\] mod powergrid 0\.6\.2-ce-19\.2-17', r'expect \[zpwg\] mod powergrid 0\.6\.2-ce-19\.2-20'),
	('SQ-PWG-90', r'expect \[zpwg\] mod powergrid 0\.6\.2-ce-19\.2-17', r'expect \[zpwg\] mod powergrid 0\.6\.2-ce-19\.2-20'),
	# the pack removes powergrid:mixing/acid and adds its own acid recipe outside the powergrid namespace
	# (kubejs/server_scripts/other/power_grid.js, TODO largecontent/mod-additions #82): 116 -> 115
	('SQ-PWG-90', r'expect \[zpwg3\] recipes 116$', r'expect \[zpwg3\] recipes 115$'),
	('SQ-PWG-90', r'expect \[zpwg10\] recipes powergrid 116$', r'expect \[zpwg10\] recipes powergrid 115$'),
	('SQ-PWG-66', 'shell mkdir -p pwgr/computercraft/computer/7 && cp $INST/tools/scratch_query/pwg_p8_cc_startup.lua pwgr/computercraft/computer/7/startup.lua && ls -la pwgr/computercraft/computer/7/',
	 'shell ' + LVL + 'mkdir -p $L/computercraft/computer/7 && cp $INST/tools/scratch_query/pwg_p8_cc_startup.lua $L/computercraft/computer/7/startup.lua && ls -la $L/computercraft/computer/7/'),
	('SQ-PWG-66', 'shell cat pwgr/computercraft/computer/7/pwg_out.txt', 'shell ' + LVL + 'cat $L/computercraft/computer/7/pwg_out.txt'),
	('SQ-PWG-54', 'shell d=p2pwg/datapacks/pwgsolar;', 'shell d=$(sed -n "s/^level-name=//p" server.properties)/datapacks/pwgsolar;'),
	('SQ-PWG-48', "pwgr/serverconfig/create-server.toml", "$(sed -n 's/^level-name=//p' server.properties)/serverconfig/create-server.toml"),
]
# SQ-PWG-21: the P2 forceload keeps chunk 65 loaded at Z 1070; the r1114 rerun (pwgr1r.qb) moved the loop to Z 2200
MOVE21 = [('1000 1070 1050 1075', '1000 2200 1050 2205'), ('1016 1070 1050 1075', '1016 2200 1050 2205'),
		  (' 1070', ' 2200')]


# the rows r1118 planned in these sections (steps s94, s95); boot rows of old phase jars are left as they are
PLANNED = set('SQ-PWG-' + x for x in (
	'13 14 15 16 17 19 20 21 22 23 26 27 28 29 30 31 32 33 34 37 38 39 43 44 07 48 49 50 51 52 53 54 56 57 59 60 61 63 '
	'64 65 66 68 06 69 70 71 73 74 75 76 77 78 79 80 81 83 85 86 87 88 89 90 91 92 93 94 95 96 97 80b 54r').split())


def rows_of(lines):
	"""[(id, section, start, end)] of every '## ' row (end exclusive)."""
	out, sec, cur = [], None, None
	for n, line in enumerate(lines):
		if line.startswith('@@section '):
			sec = line.split()[1]
			if cur:
				out.append(cur + (n,))
				cur = None
		elif line.startswith('## '):
			if cur:
				out.append(cur + (n,))
			cur = (line[3:].split(' |')[0].strip(), sec, n)
	if cur:
		out.append(cur + (len(lines),))
	return out


def first_step(lines, a, b):
	for n in range(a + 1, b):
		s = lines[n].strip()
		if s and not s.startswith('@') and not s.startswith('#'):
			return n
	return None


def batch_row(path, rid):
	lines, out, on = path.read_text().splitlines(), [], False
	for line in lines:
		if line.startswith('## '):
			on = line.startswith('## %s |' % rid)
			continue
		if on:
			out.append(line)
	return out


def main(dry):
	lines = QB.read_text().splitlines()
	edits = []  # (index, new lines to insert before it)
	repl = {}  # index -> new text
	b38 = [l for l in batch_row(SRC, 'SQ-PWG-38B') if l.startswith(('> ', 'sleep', 'waitlog'))]
	for rid, sec, a, b in rows_of(lines):
		if sec not in SECTIONS or rid not in PLANNED:
			continue
		body = lines[a:b]
		if any(l.startswith('@superseded') or l.startswith('@draft') for l in body) or any(MARK in l for l in body):
			continue
		n = first_step(lines, a, b)
		if n is None:
			continue
		block = list(BLOCK)
		if rid == 'SQ-PWG-39':
			block += [MARK + ': the SQ-PWG-38B generator line (pwgr1.qb batch-only row) this row, -44 and -07 run on'] + b38
		edits.append((n, block))
		for r, old, new in FIX:
			if r != rid:
				continue
			hit = [i for i in range(a, b) if old in lines[i]]
			if not hit:
				print('%s: not found: %s' % (rid, old[:60]))
				continue
			repl[hit[0]] = lines[hit[0]].replace(old, new, 1)
		if rid == 'SQ-PWG-21':
			for i in range(n, b):
				s = lines[i]
				if s.startswith(('> ', 'waitlog')):
					for old, new in MOVE21:
						s = s.replace(old, new)
					if s != lines[i]:
						repl[i] = s
			block.append(MARK + ': loop moved to Z 2200 as in pwgr1r.qb (the block above forceloads the P2 area)')
	for i, s in repl.items():
		lines[i] = s
	for n, block in sorted(edits, reverse=True):
		lines[n:n] = block
	print('%d rows, %d line fixes' % (len(edits), len(repl)))
	if not dry:
		QB.write_text('\n'.join(lines) + '\n')


if __name__ == '__main__':
	main('--dry' in sys.argv)
