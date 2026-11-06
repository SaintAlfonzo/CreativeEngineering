// Offline check for the chest-loot framework: runs the real kubejs/server_scripts/loot/05_tuning.js +
// 07_rules.js (and tags/loottags.js + 95_tag_capture.js for '#tag' rules) in node with the KubeJS globals
// stubbed, against the raw loot tables from the jars (via gen_loot_config.load_sources) and the /dumptags
// export. Prints the same [loot-tuning] lines the game logs, then checks the result:
//   - no banned item is left in any rewritten table
//   - packed food is a split: loose + crates keep the loose entry's old share, crates take `rate` of it
//   - reverted tables start from vanilla's pools
//   - restored injections, aliases and '#tag' rules landed
// The mod-config switches (PneumaticCraft / Lightman) are read from the real config files.
//   node tools/loot_tuning_sim.js                 summary + checks
//   node tools/loot_tuning_sim.js <table id>...   also print those tables' pools before/after
// Node, not Rhino: catches config, tag and math mistakes, not Rhino-only quirks (see the Rhino harness).
const fs = require('fs')
const path = require('path')
const { execFileSync } = require('child_process')
const ROOT = path.resolve(__dirname, '..') + '/'
const show = process.argv.slice(2)

// ---- raw tables (pre-tuning), same loader the generator uses
const raw = JSON.parse(execFileSync('python3', ['-c',
  'import sys, json; sys.path.insert(0, "tools"); import gen_loot_config as g; t, _ = g.load_sources(); json.dump(t, sys.stdout)'],
  { cwd: ROOT, maxBuffer: 1 << 30 }).toString())

// ---- registry stand-ins from the exports
const itemIds = new Set()
for (const f of fs.readdirSync(ROOT + 'kubejs/exported/items_by_mod')) {
  const text = fs.readFileSync(ROOT + 'kubejs/exported/items_by_mod/' + f, 'utf8')
  if (text.trim()) JSON.parse(text).items.forEach(id => itemIds.add(id))
}
const tagExport = JSON.parse(fs.readFileSync(ROOT + 'kubejs/exported/tags/item_tags.json', 'utf8'))

// ---- mod config switches, from the files
const pncOn = /^\s*enable_dungeon_loot\s*=\s*true/m.test(fs.readFileSync(ROOT + 'config/pneumaticcraft-common.toml', 'utf8'))
const lcText = fs.readFileSync(ROOT + 'config/lightmanscurrency-common.lcconfig', 'utf8')
const lcSection = name => new RegExp('\\[' + name.replace('.', '\\.') + '\\][^\\[]*?enabled=(\\w+)').exec(lcText)[1] === 'true'
const lcChest = lcSection('loot.chests')
const lcEntity = lcSection('loot.entities')
const javaBool = v => ({ get: () => v })
const JAVA = {
  'me.desht.pneumaticcraft.common.config.ConfigHelper': { common: () => ({ general: { enableDungeonLoot: javaBool(pncOn) } }) },
  'io.github.lightman314.lightmanscurrency.LCConfig': { COMMON: { enableChestLoot: javaBool(lcChest), enableEntityDrops: javaBool(lcEntity) } },
}

// ---- KubeJS stubs
const logs = []
const log = level => (...a) => logs.push(level + a.join(' '))
const handlers = { loot: [], tags: [] }
const javaList = a => ({ size: () => a.length, get: k => a[k] })
let additions = null
const sandbox = {
  global: {},
  console: { info: log(''), log: log(''), warn: log('WARN '), error: log('ERROR ') },
  JsonIO: { readString: p => fs.readFileSync(ROOT + p, 'utf8'), parseRaw: s => JSON.parse(s) },
  Java: { loadClass: n => { if (!JAVA[n]) throw new Error('no stub for ' + n); return JAVA[n] } },
  Item: { exists: id => itemIds.has(id) },
  Ingredient: { of: t => ({ itemIds: tagExport[t.replace(/^#/, '')] || [] }) },
  Utils: { getServer: () => null },
  Text: { of: s => ({ red: () => s }) },
  StartupEvents: { postInit: () => {} },
  ServerEvents: {
    genericLootTables: fn => handlers.loot.push(fn),
    tags: (type, fn) => { if (type === 'item') handlers.tags.push(fn) },
    commandRegistry: () => {},
  },
  LootJS: { modifiers: () => {} },
  PlayerEvents: { loggedIn: () => {} },
  LootType: {}, ItemFilter: {},
  writeExportJson: (p, data) => { additions = data },
}
const run = rel => new Function(...Object.keys(sandbox), fs.readFileSync(ROOT + rel, 'utf8'))(...Object.values(sandbox))
run('development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/_lib/item_dict.js')
run('development/creativeCommon/src/main/resources/kubejs_startup/creativecommon/func/tag_helper.js')
run('kubejs/server_scripts/loot/05_tuning.js')
run('kubejs/server_scripts/loot/07_rules.js')
run('kubejs/server_scripts/tags/loottags.js')
run('kubejs/server_scripts/loot/95_tag_capture.js')
const T = sandbox.global.lootTuning

// ---- tag event: export members + this pack's adds ('#refs' nested, '/regex/' strings matched)
const added = {}
const members = (tag, seen = new Set()) => {
  if (seen.has(tag)) return []
  seen.add(tag)
  const out = new Set(tagExport[tag] || [])
  for (const e of added[tag] || []) {
    if (e.startsWith('#')) members(e.slice(1), seen).forEach(m => out.add(m))
    else if (e.startsWith('/')) {
      const re = new RegExp(e.slice(1, e.lastIndexOf('/')))
      itemIds.forEach(id => { if (re.test(id)) out.add(id) })
    } else out.add(e)
  }
  return [...out]
}
const tagEvent = {
  getClass: () => ({ getSimpleName: () => 'TagEventJS' }),
  add: (tag, e) => { (added[tag] = added[tag] || []).push(String(e)) },
  remove: () => {},
  get: tag => ({ getObjectIds: () => javaList(members(tag)) }),
}
handlers.tags.forEach(fn => fn(tagEvent))

// ---- pre-roll (modify() on a missing id creates an empty table, as in game)
const after = {}
const created = new Set()
const lootEvent = {
  modify: (id, fn) => {
    if (!raw[id]) created.add(id)
    const pools = raw[id] ? raw[id].pools || [] : []
    const b = { pools: { toString: () => JSON.stringify(pools) }, type: raw[id] ? raw[id].type : 'minecraft:generic' }
    try {
      fn(b)
      after[id] = { type: b.type, pools: b.pools }
    } catch (err) {
      if (!/unchanged|missing-table/.test(String(err))) throw err
    }
  },
}
handlers.loot.forEach(fn => fn(lootEvent))
logs.forEach(l => console.log(l))

// ---- checks
const problems = []
const norm = s => (s.includes(':') ? s : 'minecraft:' + s)
const isItem = e => norm(e.type || 'minecraft:item') === 'minecraft:item' && e.name
const total = pool => pool.entries.reduce((s, e) => s + (e.weight ?? 1), 0)
const shareOf = (pool, pred) => pool.entries.filter(pred).reduce((s, e) => s + (e.weight ?? 1), 0) / total(pool)
const named = id => e => isItem(e) && norm(e.name) === id
// does any frequency/remove/ban rule touch this item in this table? (its share then moves on purpose)
const tuned = (id, table) => T.bans.some(b => b.test(id)) || T.explicit[id] !== undefined ||
  T.patterns.some(r => r.test(id) && !r.except.some(x => x.test(id)) && (!r.tables || r.tables.some(x => x.test(table))))
const basePools = id => {
  const op = T.ops[id]
  if (op && op.vanilla) return op.vanilla.pools || []
  return raw[id] ? raw[id].pools || [] : []
}

// bans
for (const [id, t] of Object.entries(after)) {
  for (const pool of t.pools) for (const e of pool.entries || []) {
    if (!isItem(e)) continue
    const ban = T.bans.find(b => b.test(norm(e.name)))
    if (ban) problems.push(`${id}: banned ${e.name} still present (${ban.name})`)
  }
}

// packed split: crates take `rate` of the loose entry; where nothing else in the pool moves,
// loose + crates keep exactly the loose entry's old share
const perPair = {}
let splitChecked = 0
for (const [id, t] of Object.entries(after)) {
  const before = basePools(id)
  t.pools.forEach((pool, pi) => {
    const old = before[pi]
    if (!old) return
    for (const pair of T.packs) {
      const crate = shareOf(pool, named(pair.packed))
      const loose = shareOf(pool, named(pair.unpacked))
      if (!crate || shareOf(old, named(pair.packed))) continue
      const k = pair.packed + ' <- ' + pair.unpacked
      perPair[k] = perPair[k] || { pools: 0, boosted: 0 }
      perPair[k].pools++
      const base = pair.rate ?? T.packDefault
      let want = base
      T.packBoosts.forEach(b => { if (b.test(id)) want *= b.mult })
      want = Math.min(want, 0.9)
      if (want !== base) perPair[k].boosted++
      const got = crate / (crate + loose)
      if (Math.abs(got - want) > 0.02 * want + 0.005) problems.push(`${id} pool ${pi}: ${pair.packed} takes ${got.toFixed(3)} of the loose entry, want ${want}`)
      const oldNames = new Set(old.entries.filter(isItem).map(e => norm(e.name)))
      const quiet = [...oldNames].every(n => n === pair.unpacked || !tuned(n, id)) &&
        pool.entries.filter(isItem).every(e => [pair.unpacked, pair.packed].includes(norm(e.name)) || oldNames.has(norm(e.name)))
      if (quiet) {
        const was = shareOf(old, named(pair.unpacked))
        if (Math.abs(crate + loose - was) > 0.02 * was) problems.push(`${id} pool ${pi}: loose+crates ${(crate + loose).toFixed(4)} != old loose share ${was.toFixed(4)}`)
        splitChecked++
      }
    }
  })
}

// vanilla reverts: every reverted table starts from vanilla's pools (goldenfood's own book gone)
let reverted = 0
for (const [id, op] of Object.entries(T.ops)) {
  if (!op.vanilla) continue
  reverted++
  const t = after[id]
  if (!t) { problems.push(`${id}: marked for useVanilla but not rewritten`); continue }
  const gfOld = t.pools.some(p => (p.entries || []).some(e => (e.functions || []).some(f => /\{Enchantments:\[\{id:"goldenfood/.test(f.tag || ''))))
  if (gfOld) problems.push(`${id}: goldenfood's own book entry is still there`)
  if (t.pools.length < (op.vanilla.pools || []).length) problems.push(`${id}: fewer pools than vanilla`)
}

// restored injections, aliases
const restored = []
for (const [id, op] of Object.entries(T.ops)) {
  for (const pa of op.pools) {
    const sub = pa.pool.entries[0].name
    const ok = after[id] && after[id].pools.some(p => (p.entries || []).some(e => e.name === sub))
    if (!ok) problems.push(`${id}: ${pa.label} pool (${sub}) missing`)
    else restored.push(`${id.replace('minecraft:chests/village/', '')} <- ${pa.label}`)
  }
}
const aliases = Object.keys(T.aliases)
for (const a of aliases) if (!after[a] || !created.has(a)) problems.push(`alias ${a}: not created`)

// the Golden Food book must be in no table (reverted goldenfood copies, no companion, no village add)
const gfBook = e => isItem(e) && norm(e.name) === 'minecraft:enchanted_book' && (e.functions || []).some(f => /goldenfood:gf_enchantment/.test(f.tag || ''))
const gfTables = Object.entries(after).filter(([, t]) => t.pools.some(p => (p.entries || []).some(gfBook))).map(([id]) => id)
if (gfTables.length) problems.push(`Golden Food book still in: ${gfTables.join(', ')}`)

// tag rules
for (const r of T.patterns.filter(r => r.key.startsWith('#'))) {
  const tag = r.key.slice(1)
  const n = Object.keys(T.tagMembers[tag] || {}).length
  const hit = Object.keys(T.index.items).filter(id => r.test(id))
  console.log(`tag rule ${r.key} x${r.factor}: ${n} members, ${hit.length} in chest loot`)
  if (!n) problems.push(`${r.key}: no members captured`)
}

const st = T.stats
console.log(`packed: ${T.packs.length} pairs, ${st.packSplit} loose entries split into ${st.packEntries} crate entries, ` +
  `${st.packPresent} already present, ${st.packFallback} skipped (composite pools); ${splitChecked} splits checked exactly`)
for (const pair of T.packs) {
  const k = pair.packed + ' <- ' + pair.unpacked
  const c = perPair[k]
  console.log(`  ${k}: ${c ? `${c.pools} pools (${c.boosted} boosted)` : 'no chest pool holds the loose item'}`)
}
console.log(`golden food: ${st.boosted} boosted entries; the Golden Food book is in ${gfTables.length} tables`)
console.log(`goldenfood: ${reverted} tables reverted to vanilla; restored: ${restored.join(', ')}`)
console.log(`aliases: ${aliases.length} created`)
console.log(`bans: ${T.bans.length}, ${st.banRemovedPre} entries removed pre-roll, ${st.alarms.length} alarms`)
console.log(`post-roll only: ${st.postOnly.length} tuned items (${st.postOnly.join(', ') || 'none'})`)
console.log(`config switches: PNC dungeon loot ${pncOn}, Lightman chests ${lcChest}, entities ${lcEntity}`)
console.log(`runtime additions file: ${additions ? Object.keys(additions.tables).length + ' tables' : 'NOT written'}`)
if (!additions) problems.push('loot_runtime_additions.json not written')

for (const id of show) {
  const dump = pools => (pools || []).map((p, pi) => `  pool ${pi}: ` + (p.entries || []).map(e =>
    `${(e.name || e.type || '?').replace('minecraft:', '')}${(e.functions || []).some(f => f.tag) ? '(nbt)' : ''}` +
    `${(e.functions || []).filter(f => f.function === 'minecraft:set_count' && typeof f.count === 'number').map(f => 'x' + f.count).join('')}` +
    `=${e.weight ?? 1}`).join(' ')).join('\n')
  console.log(`\n${id}\nbefore:\n${dump(raw[id] && raw[id].pools)}\nafter:\n${after[id] ? dump(after[id].pools) : '  (unchanged)'}`)
}

if (problems.length) {
  console.log('\nPROBLEMS:\n' + problems.join('\n'))
  process.exit(1)
}
console.log('\nall checks passed')
process.exit(logs.some(l => l.startsWith('ERROR')) ? 1 : 0)
