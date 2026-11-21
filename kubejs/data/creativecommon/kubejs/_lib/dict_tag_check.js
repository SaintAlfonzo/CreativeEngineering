// SPDX-License-Identifier: GPL-3.0-only
// Dict tag check (tooling/dictionaries #2). block_dict.js and fluid_dict.js skip their '#tag' entries at startup, and
// NCH only records the fluid tags recipes pass (global.NCH._fluidTagsSeen), because those tags are bound after
// startup and after the recipe load. Once the server has started every tag is bound, so this logs each such tag that
// no registry knows or that has no members. One line per problem, then a summary line.
//
// Runs once per server start (ServerEvents.loaded); a /reload does not rerun it. Block dict tags are tried as block
// tags first, then biome and item tags (a few block dict keys hold biome tags, the melting and season ones). A tag of
// a mod that is not loaded is skipped, like the dicts' own id checks.

ServerEvents.loaded(event => {
	var $Registry = Java.loadClass('net.minecraft.core.Registry')
	var $TagKey = Java.loadClass('net.minecraft.tags.TagKey')
	var $ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')
	var access = event.server.registryAccess()

	var registries = {
		block: $Registry.BLOCK_REGISTRY,
		fluid: $Registry.FLUID_REGISTRY,
		biome: $Registry.BIOME_REGISTRY,
		item: $Registry.ITEM_REGISTRY
	}

	// -1 = no such tag in that registry, else its member count
	function tagSize(kind, tag) {
		try {
			var key = $TagKey.create(registries[kind], new $ResourceLocation(tag.substring(1)))
			var found = access.registryOrThrow(registries[kind]).getTag(key)
			return found.isPresent() ? found.get().size() : -1
		} catch (err) {
			return -1
		}
	}

	var problems = []
	var checked = 0
	function check(label, tag, kinds) {
		if (typeof tag !== 'string' || tag.charAt(0) !== '#' || tag.indexOf('*') >= 0 || !global.has(tag)) return
		checked++
		for (var k = 0; k < kinds.length; k++) {
			var n = tagSize(kinds[k], tag)
			if (n > 0) return
			if (n === 0) {
				problems.push(label + ' -> ' + tag + ' (' + kinds[k] + ' tag is empty)')
				return
			}
		}
		problems.push(label + ' -> ' + tag + ' (no ' + kinds.join(' / ') + ' tag of that name)')
	}

	var b = global.blocks || {}
	for (var bk in b) check('block_dict ' + bk, b[bk], ['block', 'biome', 'item'])
	var f = global.fluid || {}
	for (var fk in f) check('fluid_dict ' + fk, f[fk], ['fluid'])
	var seen = (global.NCH && global.NCH._fluidTagsSeen) || {}
	for (var tag in seen) check('NCH fluid (' + seen[tag] + ')', tag, ['fluid'])

	problems.forEach(p => console.warn('[dict_tag_check] ' + p))
	console.info('[dict_tag_check] ' + checked + ' dict and NCH tags checked, ' + problems.length + ' empty or unknown')
})
