// global.LOOT — thin conventions + validation layer over LootJS (lootjs-forge 2.9.1, 2.x API).
//
// Everything lives in this ONE server script (not a startup script) so a plain /reload
// rebuilds it — startup scripts only run at game launch. Nothing here needs startup scope.
//
// Bindings available: LootType, ItemFilter, LootEntry, Interval (added by LootJSPlugin).
// The `event` in every helper is the LootModificationEventJS from  LootJS.modifiers(e => {}).
// event.addLootTypeModifier(...) / event.addLootTableModifier(...) return a builder that chains:
//   .removeLoot(filter) .addLoot(entry) .addWeightedLoot(entry) .replaceLoot(filter, entry, all)
//   .modifyLoot(filter, stack=>stack) .pool(b=>) .group(b=>) .apply(ctx=>) .logName(str)
//   + conditions: .randomChance(f) .anyStructure([...],bool) .anyBiome(...) .anyDimension(...)
//
// Anything a loot table can express (remove, reweight, boost, add an entry or a pool, per table or
// everywhere) is NOT done here: that's the pre-roll framework (05_tuning.js, calls in 07_rules.js,
// knobs in loot_config.json), which rewrites the table JSON before the roll. LootJS 2.x only ever
// sees loot AFTER the roll, so it can't change a weight and a removal leaves an empty slot.
// This lib is for what only LootJS can do in 10-40: edits scoped by where the chest is (structure,
// biome, dimension) or who opened it, stack-size caps, per-roll code. See loot/README.md.

global.LOOT = {

  modId: 'creativeengineering',
  strict: true, // true = throw on an unknown item/tag id, false = warn and continue

  // ── content lists (fill from a /dumploot export) ────────────────────────────

  // Reusable item-filter groups. Entries: 'ns:id' | '#ns:tag' | /regex/.
  // (Pack-wide removals live in loot_config.json, not here.)
  groups: {
    foodJunk: [],
    redundantTools: [],
    sherds: [],
    musicDiscs: [],
  },

  // Reusable "add" pools. Each entry: { item, weight, count, chance }
  //   item   'ns:id'                   (required)
  //   weight integer pool weight        (default 1)
  //   count  integer stack size         (default 1)
  //   chance 0..1 per-entry drop chance (optional; omit = always)
  tiers: {
    common: [],
    uncommon: [],
    rare: [],
    treasure: [],
  },

  // ── internals ──────────────────────────────────────────────────────────────

  _warn(ctx, msg) {
    var text = '[LOOT] ' + ctx + ': ' + msg
    if (global.LOOT.strict) throw new Error(text)
    console.warn(text)
  },

  // Validate one filter token. Strings ('id' / '#tag') are checked against the registry;
  // regexes and LootJS ItemFilter constants pass through untouched. Returns true if usable.
  _checkToken(token, ctx) {
    if (token == null) {
      global.LOOT._warn(ctx, 'null/undefined filter token')
      return false
    }
    if (typeof token !== 'string') return true // /regex/, ItemFilter.ARMOR, ...

    var id = token.trim()
    var ok = id.charAt(0) === '#'
      ? Ingredient.of(id).itemIds.length > 0
      : Item.exists(id)
    if (!ok) global.LOOT._warn(ctx, 'item/tag "' + id + '" does not exist or is empty')
    return ok
  },

  // Validate a group (array or single token); returns only the usable tokens.
  _cleanTokens(tokens, ctx) {
    var arr = Array.isArray(tokens) ? tokens : [tokens]
    return arr.filter(function (t, n) {
      return global.LOOT._checkToken(t, ctx + '[' + n + ']')
    })
  },

  // ── targeting ──────────────────────────────────────────────────────────────

  // Every chest table in the game (vanilla + modded, regardless of file path).
  chestType(event) {
    return event.addLootTypeModifier(LootType.CHEST)
  },

  // Specific tables. `filter` may be 'ns:chests/x' | /regex/ | ['ns:a', /b/, ...].
  tables(event, filter) {
    return event.addLootTableModifier(filter)
  },

  // ── entries / pools ────────────────────────────────────────────────────────

  // Build a LootJS LootEntry from a { item, weight, count, chance } spec (or a plain id).
  // Returns null (and warns) on an unknown id.
  entry(spec, ctx) {
    ctx = ctx || 'LOOT.entry'
    var item = typeof spec === 'string' ? spec : (spec && spec.item)
    if (!global.LOOT._checkToken(item, ctx)) return null

    var count = (spec && spec.count) || 1
    var e = LootEntry.of(Item.of(item, count))
    if (spec && spec.weight) e = e.withWeight(spec.weight)
    if (spec && spec.chance != null && spec.chance < 1) {
      var p = spec.chance
      e = e.when(function (c) { c.randomChance(p) })
    }
    return e
  },

  // builder.addWeightedLoot(entry) for every spec in a tiers-style array. Skips unknown ids.
  addPool(builder, entries, ctx) {
    ctx = ctx || 'LOOT.addPool'
    ;(entries || []).forEach(function (spec, n) {
      var e = global.LOOT.entry(spec, ctx + '[' + n + ']')
      if (e) builder.addWeightedLoot(e)
    })
    return builder
  },

  // ── stack size ─────────────────────────────────────────────────────────────

  // Cap the stack size of an existing item wherever it appears in the targeted tables.
  capCount(builder, filter, max) {
    return builder.modifyLoot(filter, function (stack) {
      return stack.count > max ? stack.withCount(max) : stack
    })
  },

  // ── removal ────────────────────────────────────────────────────────────────

  // Validate + removeLoot a group (array or single token). No-op if nothing valid.
  remove(builder, group, ctx) {
    var tokens = global.LOOT._cleanTokens(group, ctx || 'LOOT.remove')
    if (tokens.length) builder.removeLoot(tokens)
    return builder
  },
}

console.log('global.LOOT ready')
