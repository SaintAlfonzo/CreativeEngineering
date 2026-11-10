#!/usr/bin/env python3
"""Turns SQ-HH full-suite queue rows (scratch_queue.md) into qrun batches (run r1108hh).
    hh_queue_to_qb.py stats                       per block: rows, parsed checks, rows with something unparsed
    hh_queue_to_qb.py gen ID_FROM ID_TO OUT.qb    writes rows ID_FROM..ID_TO (numeric part, SQ-HH only)
    hh_queue_to_qb.py show ID                     prints what a row parsed into
Commands column: backticked spans in order; console commands get '>', qrun directives stay; 'wait N s' = sleep.
Pass column: `expect ...` style directives stay, "log matches `RE`" = expect, the rest goes into one look line.
Setup prose (prep_srv, cp, boot) is skipped: the run script does that. Unknown first words are reported."""
import os, re, sys, pathlib
ROOT = pathlib.Path(__file__).resolve().parents[2]
Q = ROOT / 'feature_writeup/testing/scratch_queue.md'
DIRECT = {'sleep','waitlog','expect','reject','count','capture','shell','pace','look','repeat','end','bootexpect','bootreject','kjsexpect','kjsreject'}
CONSOLE = {'zq','zhh1','zhh2','zhh3','zhh5','zact','pbot','give','setblock','fill','summon','kill','tp','teleport','forceload','gamerule','time','weather','data','execute','effect','clear','item','reload','say','difficulty','place','locate','tag','scoreboard','cdevplace','cdevgen','dumpall','attribute','enchant','xp','experience','setworldspawn','spawnpoint','seed','zgv','ztag','team','function','stop','save-all','kubejs','serene','season','sereneseasons','forge'}
SKIP = re.compile(r'^(bash |prep_srv|cp |scratch_server|grep |unzip |mkdir |rm |python3 |qrun\.py )')
def rows():
    lines = Q.read_text().splitlines()
    start = next(i for i, l in enumerate(lines) if l.startswith('## SQ-HH full suite'))
    for l in lines[start:]:
        if not re.match(r'\| [SC]Q-HH-\d+', l): continue
        cells = [c.strip().replace('\\|', '|') for c in re.split(r'(?<!\\)\|', l)[1:-1]]
        if len(cells) >= 5: yield cells
TOK = re.compile(r'`([^`]*)`|\bshell:|\bwait (\d+) ?s\b')
def parse_cmds(c):
    out, bad = [], []
    shell = False
    for m in TOK.finditer(c):
        if m.group(0) == 'shell:': shell = True; continue
        if m.group(2): out.append(f'sleep {m.group(2)}'); continue
        t = m.group(1).strip()
        if t.startswith('/'): t = t[1:]
        w = t.split(' ')[0] if t else ''
        if w in DIRECT: out.append(t); shell = False
        elif w in CONSOLE: out.append('> ' + t); shell = False
        elif shell: out.append('shell ' + t)
        elif SKIP.match(t): out.append('# setup/shell: ' + t)
        else: bad.append(t)
    return out, bad
def parse_pass(p):
    out = []
    rest = p
    for m in re.finditer(r'`((?:expect|reject|count|bootexpect|bootreject|kjsexpect|kjsreject) [^`]*)`', p):
        out.append(m.group(1)); rest = rest.replace(m.group(0), '', 1)
    for m in re.finditer(r'(?:log|boot log) matches `([^`]*)`', p):
        out.append('expect ' + m.group(1)); rest = rest.replace(m.group(0), '', 1)
    rest2 = re.sub(r'[;,.\s]|and', '', rest)
    return out, (p if rest2 else '')
# run-time fixes for rows whose text is wrong on this build (BATCH-FAULT notes in the run doc)
OVR = {
 'SQ-HH-1226': [('zhh1 drops 3432 -60 3002 3', 'zhh1 drops 3432 -60 3002 6')],
 'SQ-HH-1252': [('zhh1 drops 3240 -60 3145 3', 'zhh1 drops 3240 -60 3145 6')],
 'SQ-HH-1309': [('zhh1 drops 3384 -60 3386 3', 'zhh1 drops 3384 -60 3386 6')],
}
EXTRA = {i: ['> data get entity bot1 Inventory'] for i in ('SQ-HH-1226','SQ-HH-1252','SQ-HH-1309')}
for i in ('SQ-HH-1225','SQ-HH-1234','SQ-HH-1236','SQ-HH-1327'): EXTRA[i] = ['> data get entity bot1 Inventory']
EXTRA['SQ-HH-1259'] = ['> data get entity bot2 Health']
PRE = {"SQ-HH-1298": ["> kill @e[tag=epg1]"], "SQ-HH-1300": ["> kill @e[tag=etp1]"], 'SQ-HH-1286': ['> setblock 3240 -60 3290 minecraft:air'], 'SQ-HH-1292': ['> kill @e[tag=ep4]'], 'SQ-HH-1237': ['> kill @e[tag=mn4]']}
OVR['SQ-HH-1292'] = [('sleep 3\n', ''), ('Tags:["ep4"]', 'Tags:["ep4"]')]
def fix_expect(c):
    # NBT compound order differs between stack and block dumps
    c = re.sub(r'\\\{Count: 1b, id: "([^"]+)"\\\}', lambda m: '\\{(Count: 1b, id: "%s"|id: "%s", Count: 1b)\\}' % (m.group(1), m.group(1)), c)
    m = re.match(r'expect has the following block data: \\\{Amount: (.+?)\\b\.\*(hearthandharvest:\w+)$', c)
    if m: c = 'expect has the following block data: (?=.*Amount: %s\\b)(?=.*%s)' % (m.group(1), m.group(2))
    c = c.replace('\\[\\{Count: 5b, Slot: 0b, id: "minecraft:apple"\\}\\]', 'Count: 5b')
    # floats print in full: 0.3 is 0.30000001192092896, 0.35 is 0.3499999940395355
    def fl(m):
        b = m.group(1); lo = str(int(b) - 1).zfill(len(b))
        return 'modifier 0\\.(?:%s0{3,}\\d*|%s9{3,}\\d*|%s), effects' % (b, lo, b)
    c = re.sub(r'modifier 0\\\.\((\d+)\|[^)]*\), effects', fl, c)
    return c
def num(i): return int(i.rsplit('-', 1)[1])
if __name__ == '__main__':
    cmd = sys.argv[1]
    all_rows = list(rows())
    if cmd == 'stats':
        blocks = [('T12',1101,1328),('Mach+WG',201,329),('Crow',401,502),('Gear',1500,1612)]
        for n,a,b in blocks:
            r=[x for x in all_rows if x[0].startswith('SQ') and a<=num(x[0])<=b]
            bad=[(x[0],parse_cmds(x[2])[1]) for x in r if parse_cmds(x[2])[1]]
            auto=sum(1 for x in r if parse_pass(x[3])[0] and not parse_pass(x[3])[1])
            look=sum(1 for x in r if parse_pass(x[3])[1])
            print(n,len(r),'full-auto',auto,'look',look,'unparsed-cmd rows',len(bad))
            for i,t in bad[:int(sys.argv[2]) if len(sys.argv)>2 else 6]: print('   ',i,[s[:70] for s in t][:4])
    elif cmd == 'show':
        x=[r for r in all_rows if r[0].endswith('-'+sys.argv[2])][0]
        print(parse_cmds(x[2])); print(parse_pass(x[3]))
    elif cmd == 'gen':
        a,b=int(sys.argv[2]),int(sys.argv[3])
        with open(sys.argv[4],'w') as f:
            f.write('# generated by hh_queue_to_qb.py from scratch_queue.md (r1108hh)\npace 1\n\n')
            for x in all_rows:
                if not x[0].startswith('SQ') or not a<=num(x[0])<=b: continue
                cm,bad=parse_cmds(x[2]); ch,lk=parse_pass(x[3])
                for old,new in OVR.get(x[0],[]): cm=[c.replace(old,new) for c in cm]
                if x[0].rsplit('-',1)[1] in os.environ.get('HH_CLEAN','').split(','):
                    # rerun on a used fixture: remove the row's old mobs, loose items and placed blocks first
                    tags=re.findall(r'Tags:\["([^"]+)"',' '.join(cm)); clean=[f'> kill @e[tag={t}]' for t in tags]
                    clean+=[f'> setblock {" ".join(c.split()[2:5])} minecraft:air' for c in cm if c.startswith('> setblock ')]
                    cm=clean+['sleep 2','> kill @e[type=minecraft:item]']+cm
                cm=PRE.get(x[0],[])+cm+EXTRA.get(x[0],[]); ch=[fix_expect(c) for c in ch]
                f.write(f'## {x[0]} | {x[3][:70].strip()}\n')
                fast='pace 0.2' in x[2] or 'pace 0.2' in x[3] or x[0]=='SQ-HH-1292'
                if fast: f.write('pace 0.2\n')
                for c in cm: f.write(c+'\n')
                for c in ch: f.write(c+'\n')
                if bad: f.write('look UNPARSED CMD: '+' ;; '.join(bad)[:400]+'\n')
                if lk: f.write('look '+lk.replace('\n',' ')+'\n')
                if fast: f.write('pace 1\n')
                f.write('\n')
