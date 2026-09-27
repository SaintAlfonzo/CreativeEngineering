// /dumptags [item,block,fluid,entity_type]   ->  writes kubejs/exported/tags/*
// No arg = all four registries.  Also callable from other scripts: dumpTags(['item'])
//
// Per registry <r> it writes two views plus feeds a shared summary:
//
//   tags/<r>_tags.json          { "ns:tag": ["ns:member", ...] }         every tag -> its members
//   tags/<r>_tags_by_<r>.json   { ns: { "ns:member": ["ns:tag", ...] } } every member -> its tags, by mod
//   tags/_summary.json          per registry: tag_count, empty_tags[], single_member_tags[],
//                               biggest_tags[] (top 25 by size)
//
// Verify targets:
//   empty_tags          - referenced somewhere (recipe / other tag) but 0 members. Typos or missing mod content.
//   single_member_tags  - #forge:ingots/foo with one entry: recipes using it aren't really "ambiguous".
//   *_tags_by_*         - diff against a known-good copy after a mod update to catch membership drift.

ServerEvents.commandRegistry(event => {
	var { commands: Commands, arguments: Arguments } = event

	var run = which => ctx => {
		var r = dumpTags(which)
		var parts = Object.keys(r).map(k => `${r[k].tags} ${k}`)
		ctx.source.sendSuccess(Text.green(`Dumped tags (${parts.join(' / ')}) to kubejs/exported/tags/`), false)
		return 1
	}

	event.register(
		Commands.literal('dumptags')
			.requires(src => src.hasPermission(2))
			.executes(run([]))
			.then(
				Commands.argument('registries', Arguments.GREEDY_STRING.create(event))
					.executes(ctx => run(
						Arguments.GREEDY_STRING.getResult(ctx, 'registries')
							.split(/[,\s]+/).map(s => s.trim().toLowerCase()).filter(Boolean)
					)(ctx))
			)
	)
})

// which: array of 'item' | 'block' | 'fluid' | 'entity_type'. Empty = all.
function dumpTags(which) {
	var ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')

	var REGS = {
		item:        ForgeRegistries.ITEMS,
		block:       ForgeRegistries.BLOCKS,
		fluid:       ForgeRegistries.FLUIDS,
		entity_type: ForgeRegistries.ENTITY_TYPES,
	}

	var want = (which && which.length) ? which : Object.keys(REGS)
	var summary = {}
	var out = {}

	want.forEach(label => {
		var reg = REGS[label]
		if (!reg) { console.warn(`[dumptags] unknown registry '${label}'`); return }
		var res = dumpRegistryTags(reg, label)
		if (!res) return
		out[label] = res
		summary[label] = {
			tag_count:          res.tags,
			empty_tags:         res.empty,
			single_member_tags: res.single,
			biggest_tags:       res.biggest,
		}
	})

	writeExportJson('kubejs/exported/tags/_summary.json', summary)
	console.log(`[dumptags] wrote ${Object.keys(out).length} registr(y/ies) to kubejs/exported/tags/`)
	return out
}

function dumpRegistryTags(reg, label) {
	var tagMgr = reg.tags() // ITagManager<T>; null before world load
	if (!tagMgr) { console.warn(`[dumptags] no tag manager for ${label} (run in-world)`); return null }

	var perTag = {}        // "ns:tag" -> [member ids]
	var perMemberByMod = {} // ns -> { "ns:id" -> [tag ids] }
	var empty = []
	var single = []
	var sizes = []         // [tagId, count]
	let tagCount = 0

	var names = tagMgr.getTagNames().iterator()
	while (names.hasNext()) {
		var tagKey = names.next()
		var tagId = tagKey.location().toString()

		var ids = []
		try {
			var members = tagMgr.getTag(tagKey).iterator()
			while (members.hasNext()) {
				let id = null
				try { id = reg.getKey(members.next()).toString() } catch (e) {}
				if (id) ids.push(id)
			}
		} catch (e) {}
		ids.sort()

		perTag[tagId] = ids
		tagCount++
		sizes.push([tagId, ids.length])
		if (ids.length === 0) empty.push(tagId)
		if (ids.length === 1) single.push(tagId)

		ids.forEach(id => {
			var ns = id.substring(0, id.indexOf(':'))
			var bucket = (perMemberByMod[ns] = perMemberByMod[ns] || {})
			;(bucket[id] = bucket[id] || []).push(tagId)
		})
	}

	// sort: tags by id
	var perTagSorted = {}
	Object.keys(perTag).sort().forEach(k => { perTagSorted[k] = perTag[k] })

	// sort: mod, then member id, then tag list
	var perMemberSorted = {}
	Object.keys(perMemberByMod).sort().forEach(ns => {
		var inner = {}
		Object.keys(perMemberByMod[ns]).sort().forEach(id => { inner[id] = perMemberByMod[ns][id].sort() })
		perMemberSorted[ns] = inner
	})

	writeExportJson(`kubejs/exported/tags/${label}_tags.json`, perTagSorted)
	writeExportJson(`kubejs/exported/tags/${label}_tags_by_${label}.json`, perMemberSorted)

	var biggest = sizes.sort((a, b) => b[1] - a[1]).slice(0, 25).map(p => `${p[0]} (${p[1]})`)
	console.log(`[dumptags] ${label}: ${tagCount} tags, ${empty.length} empty, ${single.length} single-member`)
	return { tags: tagCount, empty: empty.sort(), single: single.sort(), biggest: biggest }
}
