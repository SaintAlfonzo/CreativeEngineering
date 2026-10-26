// requires: cdtest_not_a_mod
// Scratch test suite (SQ-CDT): the startup loader must honour `// requires:` like the pack folder, so this never runs.
global.cdtestStartupAbsentRan = true
console.error('[cdtest] FAIL startup requires: cdtest_startup_absent.js ran although cdtest_not_a_mod is absent')
