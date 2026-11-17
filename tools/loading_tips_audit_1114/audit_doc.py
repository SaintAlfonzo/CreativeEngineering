import sys
S,OUT=sys.argv[1],sys.argv[2]; exec(open(S+'/audit_decisions.py').read())
rows={int(l.split('\t')[0]):l.rstrip('\n').split('\t') for l in open(S+'/tips_archive_2026-11-14/effective.tsv')}
e=lambda x:x.replace('|','\\|')
sid=lambda i:rows[i][2].split(':',1)[1]
o=[]
o.append("### 2.1 Merged (one tip per feature)\n\nThe first tip keeps its id and takes the new text; the others go to `remove`.\n\n| Kept id | Merged in (#) | New text | Why |\n|---|---|---|---|")
for tg in sorted(M):
	mem,txt,why=M[tg]
	if not mem: continue
	o.append(f"| {tg} `{sid(tg)}` | {', '.join(str(x) for x in mem)} | {e(txt) if txt else '(text unchanged)'} | {e(why)} |")
o.append("\n### 2.2 Rewritten\n\n| # | Before | After | Why |\n|---|---|---|---|")
for i in sorted(list(T)+[k for k,v in M.items() if not v[0] and v[1]]):
	txt,why=(T[i] if i in T else (M[i][1],M[i][2]))
	o.append(f"| {i} | {e(rows[i][3])} | {e(txt)} | {e(why)} |")
o.append("\n### 2.3 Removed with no replacement\n\n| # | Tip | Why |\n|---|---|---|")
for i in sorted(R): o.append(f"| {i} | {e(rows[i][3])} | {e(R[i])} |")
o.append("\n### 2.4 Merged-away tips (for reference)\n\n| # | Tip | Into |\n|---|---|---|")
into={x:tg for tg,(mem,_,_) in M.items() for x in mem}
for i in sorted(into): o.append(f"| {i} | {e(rows[i][3])} | {into[i]} |")
o.append("\n## 3. Fact checks still open\n\nThe tip is applied as written; check it in game or in code, and fix the tip if the check fails.\n\n| # | Tip now | Check |\n|---|---|---|")
for i in sorted(V):
	now = T[i][0] if i in T else (M[i][1] if i in M and M[i][1] else rows[i][3])
	o.append(f"| {i} | {e(now)} | {e(V[i])} |")
open(OUT,'w').write('\n'.join(o)+'\n')
