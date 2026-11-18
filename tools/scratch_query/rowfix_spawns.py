#!/usr/bin/env python3
"""One-off (r1118 rowfix-1, 2026-11-18): give rows that use a perfBot their section never spawns a self-contained,
idempotent spawn. The spawn lived in a batch setup row (`## ...-setup | ... (not a test row)`) that the move into
feature_writeup/testing/rows/*.qb dropped (perf/runs/r1118/judge/j1.md). Positions and forceloads come from the setup
row of the batch named in the row's @from. `execute unless entity NAME run pbot spawn ...` spawns only when the bot is
not online, so a chain of rows keeps the bot where the previous row left it (the old batch semantics).

    python3 tools/scratch_query/rowfix_spawns.py [--dry]     insert the block into each row below (skips rows that have it)

SPEC_J3 (r1118 judge-3, 2026-11-18): rows that use the bot only through vanilla commands (`give qa`, `attribute qa`,
`item replace entity qa`, `effect give qe`, ...), which the rowfix-1 scan (`pbot <verb> NAME` only) missed. Rows whose
source spawn sits on a `{H..}` / `{FO..}` / `{PL..}` home marker are not in it (the marker setup is lost too).
"""
import sys
from pathlib import Path

ROWS = Path(__file__).resolve().parents[2] / 'feature_writeup/testing/rows'
MARK = '# self-contained since r1118 (rowfix-1, 2026-11-18)'


def spawn(name, pos, dim='minecraft:overworld', mode='survival'):
	return '> execute unless entity %s run pbot spawn %s %s %s %s' % (name, name, pos, dim, mode)


C2 = ('r1113 c2_a1 SQ-C2-00', ['> forceload add 296 290 360 380', spawn('c2b', '300.5 -60 296.5')])
C3G = ('r1113 c3_a1 SQ-C3A-setup', ['> forceload add 1000 1000 1480 1080', spawn('G', '1000 -60 1000')])
P5G = ('r1115 p5_a1 P5-setup', ['> forceload add 576 496 671 559', spawn('G', '615 -60 512')])
P5H = ('r1115 p5_a1 P5-setup', ['> forceload add 160 192 335 223', spawn('hb1', '178 -60 204')])
QA0 = ('r1101 p1_server / r1031 a1_world bot row', [spawn('qa', '0 -60 0')])
PTP = ('r1115 pth_a0 P11-setup', ['> forceload add 992 976 1231 1111', spawn('P', '1000.5 -60 990.5')])
PTPQ = ('r1115 pth_a0 P11-setup', PTP[1] + [spawn('Q', '1002.5 -60 990.5')])
PSB = ('r1116 ps15b_a P2-setup', ['> forceload add 992 976 1231 1215', spawn('P', '1000.5 -60 990.5')])
HHG = ('SQ-HH-1500 setup (not in every plan)', ['> forceload add 470 470 620 620', spawn('G', '500 -60 490')])
CB = ('r1113 c3_b1 SQ-C3B-setup', ['> forceload add 0 0', '> setblock 0 199 0 minecraft:stone',
								  '> setblock 0 199 2 minecraft:stone', spawn('cb', '0.5 200 0.5')])
TXA = ('SQ-TX-12 (not in every plan)', ['> forceload add 944 896 975 911',
										'> execute unless entity @e[type=minecraft:iron_golem,x=961,y=-60,z=900,distance=..3] run summon minecraft:iron_golem 961 -60 900 {NoAI:1b}',
										spawn('A', '960 -60 900')])

SPEC = {
	'BBQS.qb': {r: C2 for r in ('SQ-BBQS-02', 'SQ-BBQS-03')},
	'BBQW.qb': {'SQ-BBQW-02': ('r1115 p6_a1 (row before SQ-BBQW-02)', ['> forceload add 296 356 316 376', spawn('c6b', '303.5 -60 366.5')]),
				'SQ-BBQW-03': C2},
	'CBW.qb': {'SQ-CBW-02': C2,
			   'SQ-CBW-03': ('r1115 p4_a1 P4-setup and its c2b row', ['> forceload add 296 356 316 376', '> forceload add 470 460 620 620',
																	 spawn('c2b', '303.5 -60 366.5'), spawn('G', '500 -60 490')])},
	'CEQ.qb': {'SQ-CEQ-02': C2},
	'CFS.qb': {'SQ-CFS-03': C2},
	'PJL.qb': {'SQ-PJL-02': C2},
	'CIF.qb': {'SQ-CIF-02': CB, 'SQ-CIF-03': CB},
	'CM.qb': {'SQ-KT-21': QA0},
	'HR.qb': {'SQ-HR-02': QA0, 'SQ-HR-03': QA0},
	'WC.qb': {'SQ-WC-05': QA0},
	'WCT.qb': {r: QA0 for r in ('SQ-WCT-02', 'SQ-WCT-03', 'SQ-WCT-04', 'SQ-WCT-05')},
	'EFX.qb': {'SQ-EFX-13': ('r1031 a1_followup', [spawn('qe', '-50 -60 -50')])},
	'FCK.qb': dict({r: C3G for r in ('SQ-FCK-01', 'SQ-FCK-10', 'SQ-FCK-11', 'SQ-FCK-12', 'SQ-FCK-14', 'SQ-FCK-15',
									 'SQ-FCK-17', 'SQ-FCK-18', 'SQ-AZW-02', 'SQ-MG-23', 'SQ-BOT-03', 'SQ-FCB-10')},
				   **{'SQ-AZW-01': ('r1115 p3_a1 P3-00', ['> forceload add 1984 1984 2143 2143', spawn('m3', '2060 -60 2060')])}),
	'IP.qb': {'SQ-PRT-01': ('r1113 fix_c3a1 SQ-C3F-setup', ['> forceload add 1000 1000 1480 1080', spawn('qa', '1350 -60 1030')]),
			  'SQ-PRT-02': ('r1113 c3_a1 SQ-C3A-setup', ['> forceload add 1000 1000 1480 1080', spawn('qa', '1350 -60 1030')]),
			  'SQ-PRT-03': ('r1113 c3_a1 SQ-C3A-setup', ['> forceload add 1000 1000 1480 1080', spawn('qm', '1200 -60 1065')])},
	'HOP.qb': dict({r: P5G for r in ('SQ-HOP-02', 'SQ-HOP-04', 'SQ-HOP-05', 'SQ-HOP-07', 'SQ-HOP-09', 'SQ-HOP-10')},
				   **{r: P5H for r in ('SQ-HH-266 (rerun)', 'SQ-HH-266b', 'SQ-HH-266c')}),
	'HH.qb': {r: HHG for r in ('SQ-HH-1505', 'SQ-HH-1506', 'SQ-HH-1507', 'SQ-HH-1509', 'SQ-HH-1516', 'SQ-HH-1522',
							   'SQ-HH-1534', 'SQ-HH-1535', 'SQ-HH-1536', 'SQ-HH-1537', 'SQ-HH-1539', 'SQ-HH-1540',
							   'SQ-HH-1546', 'SQ-HH-1559', 'SQ-HH-1560', 'SQ-HH-1566', 'SQ-HH-1569', 'SQ-HH-1603',
							   'SQ-HH-1604', 'SQ-HH-1606')},
	'PTH.qb': dict({'SQ-PTH-04': PTPQ}, **{r: PTP for r in ('SQ-PTH-08', 'SQ-PTH-09', 'SQ-PTH-10', 'SQ-PTH-11', 'SQ-PTH-12', 'SQ-PTH-13')}),
	'PS15B.qb': {r: PSB for r in ('SQ-PS15B-03', 'SQ-PS15B-04', 'SQ-PS15B-08', 'SQ-PS15B-11')},
	'SY.qb': {'SQ-SY-21': ('r1115 p6_a3 P6-reload', ['> forceload add 990 990 1070 1060', spawn('c6t', '1000.5 -60 1050.5')])},
	'TX.qb': {r: TXA for r in ('SQ-TX-13', 'SQ-TX-14', 'SQ-TX-15')},
}


MARK_J3 = '# self-contained since r1118 (judge-3, 2026-11-18)'
QAF = ('r1101 p1_server / r1031 a1_world, a2_variants bot row', [spawn('qa', '0 -60 0')])
QAC = ('r1031 b1_gameplay / w2_metas_spawns bot row', [spawn('qa', '0 120 0', mode='creative')])
QE = ('r1031 a1_followup', [spawn('qe', '-50 -60 -50')])
SPEC_J3 = {
	'AC.qb': {'SQ-AC-02': QAF, 'SQ-AC-03': QAF},
	'SALT.qb': {'SQ-AC-02': QAF},
	'CS.qb': {'SQ-CS-03': QAF, 'SQ-CS-09': QAC},
	'GF31.qb': {'SQ-GF31-03': QAF},
	'MCL.qb': {'SQ-MCL-02': QAF},
	'SG.qb': {'SQ-SG-02': QAF},
	'WC.qb': {'SQ-WC-03': QAF},
	'RD.qb': {'SQ-VG-01': QAF},
	'IWF.qb': {'SQ-IWF-02': QAC, 'SQ-IWF-03': QAC},
	'META31.qb': {'SQ-META31-09': QAC},
	'PF16.qb': {'SQ-PF16-03': QAC},
	'UC.qb': {'SQ-UC-03': QAC},
	'VS.qb': {'SQ-VS-02': ('r1106spf b1 setup', [spawn('qa', '-20 -60 -24')])},
	'EFX.qb': {r: QE for r in ('SQ-EFX-05', 'SQ-EFX-06', 'SQ-EFX-07', 'SQ-EFX-08', 'SQ-EFX-10')},
}


def apply(path, spec, dry, mark=MARK):
	lines = path.read_text().split('\n')
	out, i, done = [], 0, []
	while i < len(lines):
		ln = lines[i]
		out.append(ln)
		i += 1
		if not ln.startswith('## '):
			continue
		rid = ln[3:].partition('|')[0].strip()
		if rid not in spec:
			continue
		# copy the row's meta and leading comment lines, then insert before its first step
		while i < len(lines) and (lines[i].startswith('@') or lines[i].startswith('# ')):
			out.append(lines[i])
			i += 1
		body = lines[i:i + 40]
		if any(l.startswith(mark) for l in out[-12:]) or (body and body[0].startswith(mark)):
			continue
		src, block = spec[rid]
		out.extend(['%s: bot spawn from %s' % (mark, src)] + block + ['sleep 3'])
		done.append(rid)
	missing = sorted(set(spec) - set(done))
	if not dry:
		path.write_text('\n'.join(out))
	return done, missing


def main():
	dry = '--dry' in sys.argv
	for name, spec in SPEC.items():
		done, missing = apply(ROWS / name, spec, dry)
		print('%s: %d rows%s' % (name, len(done), ('; not found or already done: ' + ', '.join(missing)) if missing else ''))
	for name, spec in SPEC_J3.items():
		done, missing = apply(ROWS / name, spec, dry, MARK_J3)
		print('%s (judge-3): %d rows%s' % (name, len(done), ('; not found or already done: ' + ', '.join(missing)) if missing else ''))


if __name__ == '__main__':
	main()
