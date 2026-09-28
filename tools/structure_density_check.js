// Offline check of CEConfig [worldgen.debug] structureDensity: runs tools/structure_tuning_sim.js at density 1
// and at several multipliers, and checks every live structure_set's expected rate (frequency / spacing^2 for
// grids, count for concentric rings) changed by exactly that multiplier, except sets the applier reports as
// capped (their grid can't get any denser). Also checks the codec limits (separation < spacing, 0 < frequency
// <= 1) and that legacy_type_1 never keeps a changed frequency. node tools/structure_density_check.js
const { execFileSync } = require('child_process')
const fs = require('fs')
const os = require('os')
const path = require('path')
const ROOT = path.resolve(__dirname, '..')
const SIM = path.join(ROOT, 'tools/structure_tuning_sim.js')
const config = JSON.parse(fs.readFileSync(path.join(ROOT, 'kubejs/generation_scripts/worldgen/structure_config.json'), 'utf8'))

function run(density) {
  const out = fs.mkdtempSync(path.join(os.tmpdir(), 'density-'))
  const log = execFileSync('node', [SIM, path.join(out, 'report.md'), out, '--density=' + density], { encoding: 'utf8' })
  const sets = {}
  const walk = dir => fs.readdirSync(dir, { withFileTypes: true }).forEach(e => {
    const f = path.join(dir, e.name)
    if (e.isDirectory()) walk(f)
    else if (f.endsWith('.json') && f.includes('worldgen/structure_set')) {
      const rel = path.relative(out, f).split(path.sep)
      sets[rel[0] + ':' + rel.slice(3).join('/').replace(/\.json$/, '')] = JSON.parse(fs.readFileSync(f, 'utf8'))
    }
  })
  walk(out)
  fs.rmSync(out, { recursive: true, force: true })
  const cappedLine = /hit their densest grid and stay below x[\d.]+: (.*)\. Set it/.exec(log)
  return { sets, capped: new Set(cappedLine ? cappedLine[1].split(', ') : []) }
}

function rate(p) {
  const f = p.frequency === undefined ? 1 : p.frequency
  if (typeof p.spacing === 'number') return f / (p.spacing * p.spacing)
  if (typeof p.count === 'number') return f * p.count
  return null
}

const base = run(1)
function basePlacement(id) {
  if (base.sets[id]) return base.sets[id]
  const c = config.structure_sets[id]
  return c ? { placement: c.original, structures: c.structures } : null
}

let checks = 0, fails = 0
const fail = msg => { fails++; if (fails <= 10) console.log('FAIL ' + msg) }
for (const m of [4, 0.25, 30, 0.01]) {
  const r = run(m)
  let exact = 0, rings = 0, cappedSeen = 0
  for (const [id, doc] of Object.entries(r.sets)) {
    if (!doc.structures.length) continue   // emptied sets are never scaled
    const b = basePlacement(id)
    if (!b) { fail(`x${m} ${id}: no baseline`); continue }
    const p = doc.placement, q = b.placement
    checks++
    if (typeof p.spacing === 'number' && !(p.separation < p.spacing && p.separation >= 0)) fail(`x${m} ${id}: separation ${p.separation} / spacing ${p.spacing}`)
    if (p.frequency !== undefined && !(p.frequency > 0 && p.frequency <= 1)) fail(`x${m} ${id}: frequency ${p.frequency}`)
    if (p.frequency_reduction_method === 'legacy_type_1' && p.frequency !== q.frequency) fail(`x${m} ${id}: legacy_type_1 kept with a new frequency`)
    const ratio = rate(p) / rate(q)
    if (r.capped.has(id)) { cappedSeen++; if (!(ratio < m && ratio >= 1)) fail(`x${m} ${id}: capped but x${ratio}`); continue }
    if (typeof p.count === 'number') { rings++; if (Math.abs(ratio - m) / m > 0.5 / q.count + 1e-3) fail(`x${m} ${id}: rings x${ratio}`); continue }
    if (Math.abs(ratio - m) / m > 1e-4) fail(`x${m} ${id}: rate x${ratio.toFixed(5)}, wanted x${m}`)
    else exact++
  }
  if (cappedSeen !== r.capped.size) fail(`x${m}: ${r.capped.size} sets reported capped, ${cappedSeen} found`)
  console.log(`ok   density x${m}: ${exact} exact, ${rings} ring set(s) exact, ${cappedSeen} capped at their densest grid (${exact + rings + cappedSeen} live sets)`)
}
console.log(fails ? `${fails} FAILED of ${checks}` : `ALL ${checks} DENSITY CHECKS PASSED`)
process.exit(fails ? 1 : 0)
