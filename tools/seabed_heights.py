#!/usr/bin/env python3
"""Ocean-floor heights per ocean biome from a save's region files (feature_writeup/locate.md 3.7).
    python3 tools/seabed_heights.py saves/WorldG [max_region_files]
Reads OCEAN_FLOOR heightmaps of full chunks (world bottom -64) and the biome palette of the y 32-47 section;
a chunk counts for every ocean biome in that palette, so coastal chunks add some land columns. No dependencies.
"""
import sys, os, zlib, struct, glob, collections, random
def nbt(b, i, t):
    if t==1: return struct.unpack_from('>b',b,i)[0], i+1
    if t==2: return struct.unpack_from('>h',b,i)[0], i+2
    if t==3: return struct.unpack_from('>i',b,i)[0], i+4
    if t==4: return struct.unpack_from('>q',b,i)[0], i+8
    if t==5: return None, i+4
    if t==6: return None, i+8
    if t==7: n=struct.unpack_from('>i',b,i)[0]; return None, i+4+n
    if t==8: n=struct.unpack_from('>H',b,i)[0]; return b[i+2:i+2+n].decode('utf8','replace'), i+2+n
    if t==9:
        et=b[i]; n=struct.unpack_from('>i',b,i+1)[0]; i+=5; out=[]
        for _ in range(n): v,i=nbt(b,i,et); out.append(v)
        return out, i
    if t==10:
        d={}
        while True:
            tt=b[i]; i+=1
            if tt==0: return d, i
            n=struct.unpack_from('>H',b,i)[0]; name=b[i+2:i+2+n].decode('utf8','replace'); i+=2+n
            v,i=nbt(b,i,tt); d[name]=v
    if t==11: n=struct.unpack_from('>i',b,i)[0]; return None, i+4+4*n
    if t==12: n=struct.unpack_from('>i',b,i)[0]; return list(struct.unpack_from('>%dq'%n,b,i+4)), i+4+8*n
def chunks(path):
    data=open(path,'rb').read()
    for k in range(1024):
        off=int.from_bytes(data[k*4:k*4+3],'big')
        if not off: continue
        p=off*4096; ln=struct.unpack_from('>i',data,p)[0]; c=data[p+4]
        if c!=2: continue
        try: raw=zlib.decompress(data[p+5:p+4+ln])
        except Exception: continue
        d,_=nbt(raw,3+struct.unpack_from('>H',raw,1)[0],10); yield d
def unpack(longs, bits, n):
    per=64//bits; mask=(1<<bits)-1; out=[]
    for L in longs:
        L&=(1<<64)-1
        for j in range(per):
            out.append((L>>(j*bits))&mask)
            if len(out)==n: return out
    return out
world=sys.argv[1]; files=glob.glob(os.path.join(world,'region','*.mca')); random.seed(1); random.shuffle(files)
files=files[:int(sys.argv[2]) if len(sys.argv) > 2 else None]
stats=collections.defaultdict(list)
for f in files:
    for d in chunks(f):
        if d.get('Status') not in ('minecraft:full','full'): continue
        hm=d.get('Heightmaps',{}).get('OCEAN_FLOOR');
        if not hm: continue
        hs=[h-64 for h in unpack(hm,9,256)]   # world bottom -64
        # biome at y 40ish section (section y=2 covers 32..47)
        b=None
        for s in d.get('sections',[]):
            if s.get('Y')==2: b=s.get('biomes',{}).get('palette')
        if not b: continue
        for name in b:
            if 'ocean' in name: stats[name].extend(hs)
for name,v in sorted(stats.items(), key=lambda kv:-len(kv[1])):
    v.sort(); n=len(v)
    print(f"{name:45s} cols={n:8d} min={v[0]:4d} p1={v[n//100]:4d} p10={v[n//10]:4d} median={v[n//2]:4d} p90={v[9*n//10]:4d} below48={100*sum(1 for x in v if x<48)/n:5.1f}% below32={100*sum(1 for x in v if x<32)/n:5.1f}% below0={100*sum(1 for x in v if x<0)/n:5.1f}%")
