// requires: creativeworldgen
console.log('entity tags starting')

var t = global.tags
var en = global.entity   // entity ids: Creative Common _lib/entity_dict.js (mobs/tweaks #4)

ServerEvents.tags('entity_type', e => {
	// Bubble columns don't push these (Creative Common BubbleColumnImmunityMixin).
	// Upgrade Aquatic's nautilus and thrasher already ignore bubble columns on their own.
	t.add(e, 'creativeworldgen:bubble_column_immune', [
		en.squid, en.glow_squid, en.dolphin, en.turtle,
		en.orca, en.cachalot_whale, en.seal,
		en.giant_squid, en.giant_squid_part,
		en.hammerhead_shark, en.frilled_shark,
	])
})
