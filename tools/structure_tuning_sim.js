// Offline check for structure tuning: runs the real kubejs/server_scripts/worldgen/structure_overrides.js
// in node with the KubeJS globals stubbed, against the current structure_config.json + structure_tags.json
// + structure_groups.json. Prints the same [structure-tuning] summary / errors the game logs. No game needed.
//   node tools/structure_tuning_sim.js                  summary + warnings + coverage
//   node tools/structure_tuning_sim.js report.md        also write the report there (never over the real one)
//   node tools/structure_tuning_sim.js report.md out/   also write every structure_set json it would add,
//                                                       plus out/frequencies.json (what patcherMod would roll)
//   --no-patchermod                                     pretend the jar lacks StructureFrequencies
//   --density=X                                         CEConfig [worldgen.debug] structureDensity
//   --groups-off                                        CEConfig [worldgen.systems] structureGroups = false
// Node, not Rhino: catches config, tag and math mistakes, not Rhino-only quirks (see the Rhino harness).
const fs = require('fs')
const path = require('path')
const ROOT = path.resolve(__dirname, '..') + '/'
const SCRIPT = ROOT + 'kubejs/server_scripts/worldgen/structure_overrides.js'
const REAL_REPORT = 'kubejs/generation_scripts/worldgen/structure_tag_report.md'
const argv = process.argv.slice(2)
const noPatcher = argv.includes('--no-patchermod')
const densityArg = argv.find(a => a.startsWith('--density='))
const density = densityArg ? Number(densityArg.split('=')[1]) : 1
const groupsOff = argv.includes('--groups-off')
const [reportOut, filesOut] = argv.filter(a => !a.startsWith('--'))

let report = null
let handler = null
let staged = null
const logs = []
const log = level => (...a) => logs.push(level + a.join(' '))
const classes = {
  'com.SaintAlfonzo.creativeengineering.worldgen.WorldgenFiles': { writeText: (p, text) => { if (p === REAL_REPORT) report = text } },
  'com.SaintAlfonzo.creativeengineering.worldgen.StructureFrequencies': { stage: json => { staged = JSON.parse(json) } },
  'com.SaintAlfonzo.creativeengineering.worldgen.WorldgenConfig': { structureDensity: () => density, structureGroupsEnabled: () => !groupsOff },
}
const sandbox = {
  console: { info: log(''), log: log(''), warn: log('WARN '), error: log('ERROR ') },
  JsonIO: { readString: p => fs.readFileSync(ROOT + p, 'utf8') },
  Java: {
    loadClass: name => {
      if (noPatcher && name.endsWith('StructureFrequencies')) throw new Error("Class '" + name + "' is not found")
      if (!classes[name]) throw new Error('sim has no stub for ' + name)
      return classes[name]
    },
  },
  ServerEvents: { highPriorityData: fn => { handler = fn } },
}
new Function(...Object.keys(sandbox), fs.readFileSync(SCRIPT, 'utf8'))(...Object.values(sandbox))

const files = {}
handler({ add: (p, text) => { files[p] = text } })
logs.forEach(l => console.log(l))

const n = Object.keys(files).length
console.log(n + ' structure_set file(s) would be added')
if (staged) {
  const count = Object.values(staged).reduce((a, m) => a + Object.keys(m).length, 0)
  console.log('frequency table: ' + count + ' structure(s) in ' + Object.keys(staged).length + ' set(s)')
}
if (report) {
  const cov = /## Coverage\n([\s\S]*?)(\n## |$)/.exec(report)
  console.log(cov ? '\nCoverage:\n' + cov[1].trim() : '\n(no coverage section)')
  if (reportOut) { fs.writeFileSync(reportOut, report); console.log('report -> ' + reportOut) }
}
if (filesOut) {
  fs.mkdirSync(filesOut, { recursive: true })
  Object.entries(files).forEach(([p, text]) => {
    const f = path.join(filesOut, p.replace(':', '/'))
    fs.mkdirSync(path.dirname(f), { recursive: true })
    fs.writeFileSync(f, text)
  })
  if (staged) fs.writeFileSync(path.join(filesOut, 'frequencies.json'), JSON.stringify(staged, null, 2))
  console.log('structure_set files -> ' + filesOut)
}
process.exit(logs.some(l => l.startsWith('ERROR')) ? 1 : 0)
