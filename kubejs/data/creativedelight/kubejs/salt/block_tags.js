// requires: creativedelight
// requires: salt
// The Salt's own melt tag (#salt:meltables: its item click and the no-Common fallback of the salt member) follows Creative
// Common's saltable tag (plan_post_scratch_1115 E1, G7.11): upstream listed Blue Ice and a reinforced snow block, which
// salt must not melt. Needs block_dict key b.t_salt_meltables (Creative Common); inert until it exists.
ServerEvents.tags('block', e => {
	if (!global.CD) return
	var b = global.blocks
	if (!b.t_salt_meltables) {
		console.warn('creativedelight salt/block_tags.js: block_dict has no t_salt_meltables; #salt:meltables left as upstream')
		return
	}
	global.tags.wipeTag(e, b.t_salt_meltables)
	global.tags.addTag(e, b.t_salt_meltables, [b.t_melt_saltable])
})
