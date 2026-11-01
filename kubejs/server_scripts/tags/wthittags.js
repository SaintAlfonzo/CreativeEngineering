// WTHIT Silk Touch icon: block tag creativeengineering:wthit/needs_silk_touch = every block whose loot table
// gives the block itself only with Silk Touch. patcherMod compat/WthitSilkTouch reads the tag on the client.
//
// The list lives in kubejs/generation_scripts/wthit/silk_touch_blocks.json, written by
// tools/gen_silk_touch_blocks.py from the loot dump (/cegen run silk_touch_blocks dumps). Rerun it after a mod
// update, then /reload. Reference + tests: feature_writeup/wthit_lines.md 2.11.
//
// Wrapped in an IIFE: all server scripts share one top-level scope.

(function () {

	var LIST_PATH = 'kubejs/generation_scripts/wthit/silk_touch_blocks.json'
	var TAG = 'creativeengineering:wthit/needs_silk_touch'
	var TAG_DIFFERS = 'creativeengineering:wthit/silk_touch_differs'
	var ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
	var ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')

	ServerEvents.tags('block', function (event) {
		var doc
		try {
			doc = JSON.parse(String(JsonIO.readString(LIST_PATH)))
		} catch (err) {
			console.error('[wthit-silk-touch] could not read ' + LIST_PATH + ', no Silk Touch tags: ' + err)
			return
		}
		function registered(list) {
			var present = list.filter(function (id) { return ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(id)) })
			if (present.length != list.length) console.info('[wthit-silk-touch] ' + (list.length - present.length) + ' listed blocks are not registered (rerun the generator)')
			return present
		}
		global.tags.add(event, TAG, registered(doc.required || []))
		global.tags.add(event, TAG_DIFFERS, registered(doc.differs || []))
	})
})()
