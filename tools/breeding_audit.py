#!/usr/bin/env python3
"""Static breeding audit: which items breed which animals, read from jar bytecode.

Scans every jar in mods/ (and one level of nested jars), finds classes descending from a vanilla animal class,
and for each class that defines isFood (SRG m_6898_) lists the constants its code touches: Items fields (mapped
SRG -> Mojmap), strings (tag ids, item ids), Ingredient/TagKey calls. Output: JSON next to the report.

Usage: python3 tools/breeding_audit.py [out.json]
Approximate: config lists and lambdas show as calls, not items; those rows are flagged.
"""
import io, json, os, re, struct, sys, zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
TSRG = os.path.expanduser('~/.gradle/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config/'
                          '1.19.2-20220805.130853/srg_to_official_1.19.2.tsrg')
FOOD_METHODS = {'m_6898_', 'isFood'}
ANIMAL_ROOT = ('net/minecraft/world/entity/animal/', 'net/minecraft/world/entity/monster/hoglin/',
               'net/minecraft/world/entity/monster/Strider')

# opcode operand lengths (excluding the opcode); tableswitch/lookupswitch/wide handled below
L = {i: 0 for i in range(256)}
for o in (0x10, 0x12, 0x15, 0x16, 0x17, 0x18, 0x19, 0x36, 0x37, 0x38, 0x39, 0x3a, 0xa9, 0xbc): L[o] = 1
for o in list(range(0x11, 0x12)) + [0x13, 0x14, 0x84, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xbb, 0xbd, 0xc0,
                                    0xc1, 0xc6, 0xc7] + list(range(0x99, 0xa9)): L[o] = 2
L[0xc5] = 3
L[0xb9] = 4; L[0xba] = 4; L[0xc8] = 4; L[0xc9] = 4


def load_srg():
    fields = {}
    if os.path.exists(TSRG):
        for ln in open(TSRG):
            p = ln.split()
            if ln.startswith('\t') and len(p) == 2 and p[0].startswith('f_'):
                fields[p[0]] = p[1]
    return fields


def parse(data):
    r = io.BytesIO(data)
    u1 = lambda: r.read(1)[0]
    u2 = lambda: struct.unpack('>H', r.read(2))[0]
    u4 = lambda: struct.unpack('>I', r.read(4))[0]
    r.read(8)
    n = u2(); cp = [None] * n; i = 1
    while i < n:
        t = u1()
        if t == 1:
            ln = u2(); cp[i] = ('u', r.read(ln).decode('utf8', 'replace'))
        elif t in (3, 4): r.read(4)
        elif t in (5, 6): r.read(8); i += 1
        elif t in (7, 8, 16, 19, 20): cp[i] = ('c' if t == 7 else 's' if t == 8 else 'x', u2())
        elif t in (9, 10, 11, 12, 17, 18): cp[i] = ({9: 'f', 10: 'm', 11: 'i', 12: 'nt', 17: 'd', 18: 'd'}[t], u2(), u2())
        elif t == 15: r.read(3)
        i += 1
    u = lambda k: cp[k][1] if cp[k] and cp[k][0] == 'u' else None
    cname = lambda k: u(cp[k][1])
    r.read(2); this = cname(u2()); sup = u2(); sup = cname(sup) if sup else None
    for _ in range(u2()): u2()
    for _ in range(u2()):
        r.read(6)
        for _ in range(u2()):
            u2(); r.read(u4())
    methods = []
    for _ in range(u2()):
        r.read(2); name = u(u2()); desc = u(u2()); code = None
        for _ in range(u2()):
            an = u(u2()); ln = u4(); body = r.read(ln)
            if an == 'Code':
                cl = struct.unpack('>I', body[4:8])[0]; code = body[8:8 + cl]
        methods.append((name, desc, code))
    return this, sup, methods, cp, u, cname


def refs(code, cp, u, cname):
    """constants reached by a method body: strings, getstatic fields, invoked methods"""
    out = []; p = 0
    while p < len(code):
        op = code[p]
        if op in (0x12, 0x13, 0x14):
            k = code[p + 1] if op == 0x12 else struct.unpack('>H', code[p + 1:p + 3])[0]
            if cp[k] and cp[k][0] == 's': out.append(('str', u(cp[k][1])))
        elif op in (0xb2, 0xb4, 0xb6, 0xb7, 0xb8, 0xb9):
            k = struct.unpack('>H', code[p + 1:p + 3])[0]; e = cp[k]
            if e:
                nt = cp[e[2]]
                out.append(({0xb2: 'static', 0xb4: 'field', 0xb6: 'call', 0xb7: 'call', 0xb8: 'call', 0xb9: 'call'}[op],
                            cname(e[1]), u(nt[1])))
        if op == 0xaa:
            q = (p + 4) & ~3; lo, hi = struct.unpack('>ii', code[q + 4:q + 12]); p = q + 12 + 4 * (hi - lo + 1)
        elif op == 0xab:
            q = (p + 4) & ~3; n = struct.unpack('>i', code[q + 4:q + 8])[0]; p = q + 8 + 8 * n
        elif op == 0xc4:
            p += 6 if code[p + 1] == 0x84 else 4
        else:
            p += 1 + L[op]
    return out


def jars():
    d = os.path.join(ROOT, 'mods')
    for f in sorted(os.listdir(d)):
        if f.endswith('.jar'):
            yield f, zipfile.ZipFile(os.path.join(d, f))


def classes(z, jar):
    for n in z.namelist():
        if n.endswith('.class'): yield jar, z.read(n)
        elif n.endswith('.jar') and 'jarjar' in n.lower() or n.endswith('.jar') and 'members' in n.lower():
            try:
                nz = zipfile.ZipFile(io.BytesIO(z.read(n)))
            except Exception:
                continue
            for m in nz.namelist():
                if m.endswith('.class'): yield jar + '!' + os.path.basename(n), nz.read(m)


def main():
    fields = load_srg(); allc = {}
    for jar, z in jars():
        for jn, data in classes(z, jar):
            if b'm_6898_' not in data and b'isFood' not in data and b'Animal' not in data and b'animal' not in data:
                # still needed for hierarchy only if it extends something; cheap skip: parse all small set
                pass
            try:
                this, sup, methods, cp, u, cname = parse(data)
            except Exception:
                continue
            if sup is None: continue
            allc[this] = (sup, jn, methods, cp, u, cname)
    def is_animal(c, seen=0):
        while c and seen < 30:
            if c.startswith(ANIMAL_ROOT) or c in ('net/minecraft/world/entity/TamableAnimal',):
                return True
            c = allc[c][0] if c in allc else None; seen += 1
        return False
    res = []
    for c, (sup, jn, methods, cp, u, cname) in allc.items():
        if not is_animal(c): continue
        for name, desc, code in methods:
            if name in FOOD_METHODS and code:
                items, strs, calls = [], [], []
                for r_ in refs(code, cp, u, cname):
                    if r_[0] == 'str': strs.append(r_[1])
                    elif r_[0] == 'static':
                        if r_[1] == 'net/minecraft/world/item/Items': items.append(fields.get(r_[2], r_[2]))
                        else: calls.append(r_[1].split('/')[-1] + '.' + fields.get(r_[2], r_[2]))
                    elif r_[0] in ('call', 'field'):
                        calls.append(r_[1].split('/')[-1] + '.' + r_[2])
                res.append({'class': c, 'jar': jn, 'super': sup, 'items': sorted(set(items)),
                            'strings': sorted(set(strs)), 'calls': sorted(set(calls))})
    have = {x['class'] for x in res}
    for c, (sup, jn, methods, cp, u, cname) in allc.items():
        if c in have or not is_animal(c) or c.startswith('net/minecraft/'): continue
        # no own isFood: record the goal setup (m_8099_ registerGoals, constructors) and the nearest modded isFood parent
        par = sup
        while par and par in allc and par not in have: par = allc[par][0]
        items, calls = [], []
        for name, desc, code in methods:
            if name in ('m_8099_', 'registerGoals', '<init>', '<clinit>') and code:
                for r_ in refs(code, cp, u, cname):
                    if r_[0] == 'static' and r_[1] == 'net/minecraft/world/item/Items': items.append(fields.get(r_[2], r_[2]))
                    elif r_[0] == 'static' and 'Tag' in r_[1]: calls.append(r_[1].split('/')[-1] + '.' + fields.get(r_[2], r_[2]))
                    elif r_[0] == 'call' and ('Ingredient' in r_[1] or 'TemptGoal' in r_[1] or 'Breed' in r_[1]): calls.append(r_[1].split('/')[-1] + '.' + r_[2])
        res.append({'class': c, 'jar': jn, 'super': sup, 'noIsFood': True, 'inheritsFrom': par if par in have else None,
                    'items': sorted(set(items)), 'strings': [], 'calls': sorted(set(calls))})
    out = sys.argv[1] if len(sys.argv) > 1 else os.path.join(ROOT, 'kubejs', 'exported', 'breeding_audit.json')
    json.dump(sorted(res, key=lambda x: (x['jar'], x['class'])), open(out, 'w'), indent=1)
    print(len(allc), 'classes,', len(res), 'isFood definers ->', out)


main()
