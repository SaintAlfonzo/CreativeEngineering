import json,sys
S,CT=sys.argv[1],sys.argv[2]; exec(open(S+'/audit_decisions.py').read())
rows={int(l.split('\t')[0]):l.rstrip('\n').split('\t') for l in open(S+'/tips_archive_2026-11-14/effective.tsv')}
d=json.load(open(CT)); tips=d['tips']; rem=list(d['remove'])
idx={t['id']:n for n,t in enumerate(tips)}
def settext(i,txt):
	tid=rows[i][2]
	if tid in idx: tips[idx[tid]]['text']=txt
	else: tips.append({'id':tid,'text':txt}); idx[tid]=len(tips)-1
drop=set()
for i,(txt,_) in T.items(): settext(i,txt)
for tg,(mem,txt,_) in M.items():
	if txt: settext(tg,txt)
	drop.update(mem)
drop.update(R)
for i in sorted(drop):
	tid=rows[i][2]
	if tid not in rem: rem.append(tid)
dropids={rows[i][2] for i in drop}
d['tips']=[t for t in tips if t['id'] not in dropids]; d['remove']=rem
open(CT,'w').write(json.dumps(d,indent=2,ensure_ascii=False)+'\n')
print('tips',len(d['tips']),'remove',len(rem))
