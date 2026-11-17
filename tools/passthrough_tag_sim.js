// Offline check for the pass-through tags (plan_passthrough.md phase 4): runs the real tag_helper.js and Creative
// Compat's passthrough_tags.js in node against the exports and prints each tag's size, unknown ids, dead patterns and
// the helper's overlap warnings. No game needed.
//   node tools/passthrough_tag_sim.js              summary
//   node tools/passthrough_tag_sim.js out.json     also write every creativecompat:passthrough* tag's members, the
//                                                  input of blockrunner_scan.py --audit --tiers and projectile_scan.py --tags
// Block registry: kubejs/exported/surface_audit.json (/dumpsurface, every registered block) plus the /dumptags
// export. Entity registry: /dumpnames entities, /dumptags entity types and tools/projectile_scan.json (run
// tools/projectile_scan.py first for the projectile ids). Node, not Rhino: catches id and pattern mistakes only.
const fs = require('fs')
const ROOT = require('path').resolve(__dirname, '..') + '/'
const read = p => JSON.parse(fs.readFileSync(ROOT + p))
const exists = p => fs.existsSync(ROOT + p)
const SCRIPT = 'kubejs/data/creativecompat/kubejs/tags/passthrough_tags.js'
const LIB = 'development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/'

function registryFor(type) {
  const ids = new Set()
  if (type === 'block') {
    if (exists('kubejs/exported/surface_audit.json')) read('kubejs/exported/surface_audit.json').blocks.forEach(b => ids.add(b.id))
    Object.values(read('kubejs/exported/tags/block_tags_by_block.json')).forEach(m => Object.keys(m).forEach(id => ids.add(id)))
  } else {
    if (exists('kubejs/exported/display_names.json')) (read('kubejs/exported/display_names.json').entries.entity || []).forEach(x => ids.add(x.id))
    Object.values(read('kubejs/exported/tags/entity_type_tags_by_entity_type.json')).forEach(m => Object.keys(m).forEach(id => ids.add(id)))
    if (exists('tools/projectile_scan.json')) read('tools/projectile_scan.json').forEach(r => r.ids.forEach(id => ids.add(id)))
  }
  return ids
}

const warnings = []
const out = {}
function makeEvent(type) {
  const dumpTags = read(type === 'block' ? 'kubejs/exported/tags/block_tags.json' : 'kubejs/exported/tags/entity_type_tags.json')
  const regSet = registryFor(type), registry = [...regSet]
  const entries = {}
  const ent = tag => entries[tag] || (entries[tag] = (dumpTags[tag] || []).map(v => ({ t: 'id', v })))
  const parseRe = s => { const m = /^\/(.*)\/([a-z]*)$/.exec(s); return m ? new RegExp(m[1], m[2]) : null }
  function resolve(tag, seen) {
    seen = seen || new Set(); if (seen.has(tag)) return []; seen.add(tag)
    if (!entries[tag] && !dumpTags[tag]) return []
    const res = new Set()
    ent(tag).forEach(e => e.t === 'id' ? res.add(e.v) : resolve(e.v, seen).forEach(x => res.add(x)))
    return [...res]
  }
  const event = {
    add(tag, x) {
      const s = String(x), L = ent(tag)
      if (s[0] === '#') { if (!dumpTags[s.slice(1)] && !entries[s.slice(1)]) warnings.push(type + ': missing tag ' + s + ' -> ' + tag); L.push({ t: 'tag', v: s.slice(1) }); return }
      const re = parseRe(s)
      if (re) { const hits = registry.filter(id => re.test(id)); if (!hits.length) warnings.push(type + ': regex matched nothing: ' + s + ' -> ' + tag); hits.forEach(id => L.push({ t: 'id', v: id })); return }
      if (s[0] === '@') { registry.filter(id => id.startsWith(s.slice(1) + ':')).forEach(id => L.push({ t: 'id', v: id })); return }
      if (!regSet.has(s)) warnings.push(type + ': unknown id ' + s + ' -> ' + tag)
      L.push({ t: 'id', v: s })
    },
    remove(tag, x) {
      const s = String(x), L = ent(tag), re = parseRe(s)
      for (let k = L.length - 1; k >= 0; k--) {
        const e = L[k]
        if (s[0] === '#' ? (e.t === 'tag' && e.v === s.slice(1)) : (e.t === 'id' && (re ? re.test(e.v) : e.v === s))) L.splice(k, 1)
      }
    },
    removeAll(tag) { entries[tag] = [] },
    get(tag) { const ids = resolve(tag); return { getObjectIds: () => ({ size: () => ids.length, get: k => ids[k] }) } },
    getElementIds: () => ({ toArray: () => registry }),
    getClass: () => ({ getSimpleName: () => 'TagEventJS' }),
    getType: () => 'minecraft:' + type,
  }
  event.finish = () => Object.keys(entries).filter(k => k.startsWith('creativecompat:pass')).forEach(k => out[k] = resolve(k).sort())
  event.registry = regSet
  return event
}

const handlers = {}
global.ServerEvents = { tags: (type, fn) => { handlers[type] = fn } }
global.StartupEvents = { postInit: () => {} }
eval(fs.readFileSync(ROOT + LIB + 'func/has.js', 'utf8'))
const blockEvent = makeEvent('block'), entityEvent = makeEvent('entity_type')
{
  const ns = new Set([...blockEvent.registry, ...entityEvent.registry].map(id => id.split(':')[0]))
  global.has.mod = n => ns.has(n) || ['c', 'forge', 'minecraft'].includes(n)
}
eval(fs.readFileSync(ROOT + LIB + '_lib/item_dict.js', 'utf8'))
eval(fs.readFileSync(ROOT + LIB + '_lib/block_dict.js', 'utf8'))
eval(fs.readFileSync(ROOT + LIB + 'func/tag_helper.js', 'utf8'))
eval(fs.readFileSync(ROOT + SCRIPT, 'utf8'))
const logWarn = console.warn
console.warn = m => warnings.push(String(m))
handlers.block(blockEvent); blockEvent.finish()
handlers.entity_type(entityEvent); entityEvent.finish()
console.warn = logWarn
const undef = Object.keys(out).filter(k => out[k].some(id => id === 'undefined'))
if (undef.length) warnings.push('undefined dict key in: ' + undef.join(', '))
console.log(warnings.length ? 'WARNINGS (' + warnings.length + '):\n' + warnings.join('\n') : 'no unknown ids, dead patterns or overlaps')
Object.keys(out).sort().forEach(k => console.log(k, out[k].length))
if (process.argv[2]) fs.writeFileSync(process.argv[2], JSON.stringify(out, null, 1))
