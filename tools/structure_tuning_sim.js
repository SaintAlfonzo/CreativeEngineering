// Offline check for structure tuning: runs the real kubejs/data/creativeworldgen/kubejs/worldgen/structure_overrides.js
// in node with the KubeJS globals stubbed, against the current structure_config.json + structure_tags.json
// + structure_groups.json. Prints the same [structure-tuning] summary / errors the game logs. No game needed.
//   node tools/structure_tuning_sim.js                  summary + warnings + coverage
//   node tools/structure_tuning_sim.js report.md        also write the report there (never over the real one)
//   node tools/structure_tuning_sim.js report.md out/   also write every structure_set json it would add,
//                                                       plus out/frequencies.json (what patcherMod would roll)
//                                                       and out/rules.json + out/fits.json (the biome rules and fits it would get)
//   --no-patchermod                                     pretend the jar lacks StructureFrequencies (and StructureBiomeRules/Fits)
//   --density=X                                         CEConfig [worldgen.debug] structureDensity
//   --groups-off                                        CEConfig [worldgen.systems] structureGroups = false
//   --no-meta                                           pretend the jar lacks MetaStructure (meta structures left out)
//   --test-overrides[=FILE]                             CEConfig [worldgen.debug] testOverrides = true (FILE instead of
//                                                       structure_test_overrides.json, e.g. a bad one for the refusal test)
// Also runs the script's ServerEvents.tags handlers and prints the structure tags (meta/*, biome fits) and the biome
// tags (biome fits) it adds.
// Node, not Rhino: catches config, tag and math mistakes, not Rhino-only quirks (see the Rhino harness).
const fs = require('fs')
const path = require('path')
const ROOT = path.resolve(__dirname, '..') + '/'
const SCRIPT = ROOT + 'kubejs/data/creativeworldgen/kubejs/worldgen/structure_overrides.js'
const REAL_REPORT = 'kubejs/generation_scripts/worldgen/structure_tag_report.md'
const argv = process.argv.slice(2)
const noPatcher = argv.includes('--no-patchermod')
const densityArg = argv.find(a => a.startsWith('--density='))
const density = densityArg ? Number(densityArg.split('=')[1]) : 1
const groupsOff = argv.includes('--groups-off')
const noMeta = argv.includes('--no-meta')
const testArg = argv.find(a => a === '--test-overrides' || a.startsWith('--test-overrides='))
const testFile = testArg && testArg.includes('=') ? path.resolve(testArg.split('=')[1]) : null
const TEST_PATH = 'kubejs/generation_scripts/worldgen/structure_test_overrides.json'
const [reportOut, filesOut] = argv.filter(a => !a.startsWith('--'))

let report = null
let handler = null
let tagsHandler = null
let biomeTagsHandler = null
let fits = null
let staged = null
let biomeFactors = null
let rules = null
let replacements = null
let priority = null
const logs = []
const log = level => (...a) => logs.push(level + a.join(' '))
const classes = {
  'com.SaintAlfonzo.creativecommon.util.WorldgenFiles': { writeText: (p, text) => { if (p === REAL_REPORT) report = text } },
  'com.SaintAlfonzo.creativeworldgen.worldgen.StructureFrequencies': { stage: json => { staged = JSON.parse(json) }, stageBiomeFactors: json => { biomeFactors = JSON.parse(json) } },
  'com.SaintAlfonzo.creativeworldgen.worldgen.StructureBiomeRules': { stage: json => { rules = JSON.parse(json) } },
  'com.SaintAlfonzo.creativeworldgen.worldgen.StructureBiomeFits': { stage: json => { fits = JSON.parse(json) } },
  'com.SaintAlfonzo.creativeworldgen.worldgen.WorldgenConfig': { structureDensity: () => density, structureGroupsEnabled: () => !groupsOff, metaStructuresEnabled: () => true, structureReplacementsEnabled: () => true, testOverrides: () => !!testArg },
  'com.SaintAlfonzo.creativeworldgen.worldgen.MetaStructure': {},
  'com.SaintAlfonzo.creativeworldgen.worldgen.PlacementPriority': { stage: json => { priority = JSON.parse(json) } },
  'com.SaintAlfonzo.creativeworldgen.worldgen.MetaSlots': { stageReplacements: json => { replacements = JSON.parse(json) } },
}
const addedTags = {}
const addedBiomeTags = {}
const sandbox = {
  console: { info: log(''), log: log(''), warn: log('WARN '), error: log('ERROR ') },
  JsonIO: { readString: p => fs.readFileSync(p === TEST_PATH && testFile ? testFile : ROOT + p, 'utf8') },
  Java: {
    loadClass: name => {
      if (noPatcher && /StructureFrequencies|StructureBiomeRules|StructureBiomeFits|MetaStructure|MetaSlots|PlacementPriority/.test(name)) throw new Error("Class '" + name + "' is not found")
      if (noMeta && /MetaStructure|MetaSlots/.test(name)) throw new Error("Class '" + name + "' is not found")
      if (!classes[name]) throw new Error('sim has no stub for ' + name)
      return classes[name]
    },
  },
  ServerEvents: { highPriorityData: fn => { handler = fn }, tags: (type, fn) => { if (type === 'worldgen/structure') tagsHandler = fn; if (type === 'worldgen/biome') biomeTagsHandler = fn } },
  global: { tags: { add: (e, tag, ids) => [].concat(ids).forEach(id => e.add(tag, id)) } },
}
new Function(...Object.keys(sandbox), fs.readFileSync(SCRIPT, 'utf8'))(...Object.values(sandbox))

const files = {}
handler({ add: (p, text) => { files[p] = text } })
let printed = 0
const flush = () => { logs.slice(printed).forEach(l => console.log(l)); printed = logs.length }
flush()

const isMetaFile = p => p.includes(':worldgen/structure/')
const n = Object.keys(files).filter(p => !isMetaFile(p)).length
console.log(n + ' structure_set file(s) would be added')
Object.keys(files).filter(isMetaFile).forEach(p => console.log('meta structure file: ' + p))
if (tagsHandler) {
  tagsHandler({ add: (tag, id) => { (addedTags[tag] = addedTags[tag] || []).push(id) } })
  flush()
  Object.entries(addedTags).forEach(([t, ids]) => console.log('structure tag ' + t + ': ' + ids.length + ' (' + ids.join(', ') + ')'))
}
if (biomeTagsHandler) {
  biomeTagsHandler({ add: (tag, id) => { (addedBiomeTags[tag] = addedBiomeTags[tag] || []).push(id) } })
  flush()
  Object.entries(addedBiomeTags).forEach(([t, ids]) => console.log('biome tag ' + t + ': ' + ids.length))
}
if (staged) {
  const count = Object.values(staged).reduce((a, m) => a + Object.keys(m).length, 0)
  console.log('frequency table: ' + count + ' structure(s) in ' + Object.keys(staged).length + ' set(s)')
}
if (biomeFactors) Object.entries(biomeFactors).forEach(([set, f]) => console.log('biome frequency ' + set + ': ' + JSON.stringify(f)))
if (rules) Object.entries(rules).forEach(([n, r]) => console.log('biome rule ' + n + ': ' + r.biomes.join(' ') + ', ' + r.allow.length + ' allowed'))
if (priority) console.log('priority: ' + Object.keys(priority.ranks).length + ' ranked, companions ' + Object.keys(priority.companions).join(' '))
if (replacements) Object.entries(replacements).forEach(([id, r]) => console.log('replacement ' + id + ': ' + Math.round(r.share * 100) + '% of ' + r.replaces + ' slots'))
if (fits) Object.entries(fits.structures).forEach(([id, names]) => console.log('biome fit ' + id + ': ' + names.join(' + ')))
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
  if (biomeFactors) fs.writeFileSync(path.join(filesOut, 'biome_frequency.json'), JSON.stringify(biomeFactors, null, 2))
  if (rules) fs.writeFileSync(path.join(filesOut, 'rules.json'), JSON.stringify(rules, null, 2))
  if (fits) fs.writeFileSync(path.join(filesOut, 'fits.json'), JSON.stringify(fits, null, 2))
  if (replacements) fs.writeFileSync(path.join(filesOut, 'replacements.json'), JSON.stringify(replacements, null, 2))
  if (priority) fs.writeFileSync(path.join(filesOut, 'priority.json'), JSON.stringify(priority, null, 2))
  if (Object.keys(addedBiomeTags).length) fs.writeFileSync(path.join(filesOut, 'biome_tags.json'), JSON.stringify(addedBiomeTags, null, 2))
  if (Object.keys(addedTags).length) fs.writeFileSync(path.join(filesOut, 'tags.json'), JSON.stringify(addedTags, null, 2))
  console.log('structure_set files -> ' + filesOut)
}
process.exit(logs.some(l => l.startsWith('ERROR')) ? 1 : 0)
