import json,sys
d=sys.argv[1]
lt=json.load(open(d+'/working/loading_tips.json')); ct=json.load(open(d+'/working/creative_tips.json'))
m={}
for t in lt['tips']: m.setdefault(t['id'],t['text'])
rem=set(ct.get('remove',[]))
src={k:'LT' for k in m}
for t in ct['tips']:
	if isinstance(t,str): continue
	if t['id'] in m: src[t['id']]='LT*' if m[t['id']].rstrip('.!').lower()!=t['text'].rstrip('.!').lower() else 'LTn'
	else: src[t['id']]='CT'
	m[t['id']]=t['text']
for r in rem: m.pop(r,None)
out=open(d+'/effective.tsv','w')
for i,(k,v) in enumerate(m.items(),1): out.write(f"{i}\t{src[k]}\t{k}\t{v}\n")
print(len(m), sum(1 for k in m if src[k]=='CT'), sum(1 for k in m if src[k]=='LT*'), sum(1 for k in m if src[k]=='LT'))
print('unmatched remove', [r for r in rem if r not in src])
