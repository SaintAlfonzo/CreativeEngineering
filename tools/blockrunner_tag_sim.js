// Offline check for the BlockRunner speed tiers: runs the real tag_helper.js + blocktags.js in
// node against the last /dumptags export (kubejs/exported/tags) and prints each tier's size,
// dead patterns and cross-tier overlaps. No game needed. Re-run /dumptags after adding mods.
//   node tools/blockrunner_tag_sim.js                 summary only
//   node tools/blockrunner_tag_sim.js out.json        also write every tier's members
// Node, not Rhino: catches pattern/overlap mistakes, not Rhino-only quirks.
const fs = require('fs')
const ROOT = require('path').resolve(__dirname, '..') + '/'
const byBlock = JSON.parse(fs.readFileSync(ROOT + 'kubejs/exported/tags/block_tags_by_block.json'))
const dumpTags = JSON.parse(fs.readFileSync(ROOT + 'kubejs/exported/tags/block_tags.json'))
const registry = []; Object.values(byBlock).forEach(m => Object.keys(m).forEach(id => registry.push(id)))
// blocks with no tags aren't in the dump; tools/blockrunner_scan.py lists them from the jars
const untaggedFile = ROOT + 'tools/blockrunner_untagged_blocks.json'
if (fs.existsSync(untaggedFile)) JSON.parse(fs.readFileSync(untaggedFile)).forEach(id => registry.push(id))
else console.log('no tools/blockrunner_untagged_blocks.json: run tools/blockrunner_scan.py to include untagged blocks')
const regSet = new Set(registry)
// start from dump but blockrunner tiers reset to jar seeds (kubejs adds get re-applied by the script)
const entries = {}
const seed = { 'blockrunner:quick_blocks': [{t:'id',v:'minecraft:dirt_path'}], 'blockrunner:slightly_quick_blocks': [{t:'tag',v:'minecraft:stone_bricks'}] }
Object.assign(entries, seed)
function parseRe(s) { const m = /^\/(.*)\/([a-z]*)$/.exec(s); return m ? new RegExp(m[1], m[2]) : null }
function ent(tag) { if (!entries[tag]) entries[tag] = (dumpTags[tag] || []).map(v => ({t:'id', v})); return entries[tag] }
const warnings = []
function resolve(tag, seen) {
  seen = seen || new Set(); if (seen.has(tag)) return []; seen.add(tag)
  if (!entries[tag] && !dumpTags[tag]) return []
  const out = new Set()
  ent(tag).forEach(e => e.t === 'id' ? out.add(e.v) : resolve(e.v, seen).forEach(x => out.add(x)))
  return [...out]
}
const event = {
  add(tag, x) {
    const s = String(x), L = ent(tag)
    if (s[0] === '#') { if (!dumpTags[s.slice(1)] && !entries[s.slice(1)]) warnings.push('missing tag ' + s); L.push({t:'tag', v:s.slice(1)}); return }
    const re = parseRe(s)
    if (re) { const hits = registry.filter(id => re.test(id)); if (!hits.length) warnings.push('regex matched nothing: ' + s + ' -> ' + tag); hits.forEach(id => L.push({t:'id', v:id})); return }
    if (s[0] === '@') { registry.filter(id => id.startsWith(s.slice(1) + ':')).forEach(id => L.push({t:'id', v:id})); return }
    if (!regSet.has(s)) warnings.push('unknown id ' + s + ' -> ' + tag)
    L.push({t:'id', v:s})
  },
  remove(tag, x) {
    const s = String(x), L = ent(tag)
    const re = parseRe(s)
    for (let i = L.length - 1; i >= 0; i--) {
      const e = L[i]
      if (s[0] === '#' ? (e.t === 'tag' && e.v === s.slice(1)) : (e.t === 'id' && (re ? re.test(e.v) : e.v === s))) L.splice(i, 1)
    }
  },
  removeAll(tag) { entries[tag] = [] },
  get(tag) { const ids = resolve(tag); return { getObjectIds: () => ({ size: () => ids.length, get: i => ids[i] }) } },
  getElementIds: () => ({ toArray: () => registry }),
  getClass: () => ({ getSimpleName: () => 'TagEventJS' }),
  getType: () => 'minecraft:block',
}
let handler
global.ServerEvents = { tags: (type, fn) => { if (type === 'block') handler = fn } }
global.StartupEvents = { postInit: () => {} }
eval(fs.readFileSync(ROOT + 'kubejs/startup_scripts/_lib/item_dict.js', 'utf8'))
eval(fs.readFileSync(ROOT + 'kubejs/startup_scripts/_lib/block_dict.js', 'utf8'))
eval(fs.readFileSync(ROOT + 'kubejs/startup_scripts/func/tag_helper.js', 'utf8'))
eval(fs.readFileSync(ROOT + 'kubejs/server_scripts/tags/blocktags.js', 'utf8'))
handler(event)
console.log(warnings.length ? 'WARNINGS:\n' + warnings.join('\n') : 'no dead patterns / unknown ids')
const out = {}
Object.keys(entries).filter(k => k.startsWith('blockrunner:')).forEach(k => out[k] = resolve(k).sort())
if (process.argv[2]) fs.writeFileSync(process.argv[2], JSON.stringify(out, null, 1))
Object.keys(out).forEach(k => console.log(k, out[k].length))
