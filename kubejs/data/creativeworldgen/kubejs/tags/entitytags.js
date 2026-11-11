// requires: creativeworldgen
console.log('entity tags starting')

var t = global.tags

ServerEvents.tags('entity_type', e => {
	// Bubble columns don't push these (patcherMod BubbleColumnImmunityMixin).
	// Upgrade Aquatic's nautilus and thrasher already ignore bubble columns on their own.
	t.add(e, 'creativeworldgen:bubble_column_immune', [
		'minecraft:squid', 'minecraft:glow_squid','minecraft:dolphin','minecraft:turtle',
		'alexsmobs:orca', 'alexsmobs:cachalot_whale','alexsmobs:seal',
		'alexsmobs:giant_squid', 'alexsmobs:giant_squid_part',
		'alexsmobs:hammerhead_shark', 'alexsmobs:frilled_shark',
	])
})
