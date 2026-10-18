// SCRATCH SERVER ONLY: copy into srv/kubejs/server_scripts/. Not part of the pack (lives under feature_writeup/).
//
// /zfridgesort run    build a 2-tall CFB fridge 2 blocks SE of the caller, fill it (one 64-stack of
//                     mushroom stew, max 32 under the stack policy, plus normal stacks after it), run
//                     Quark's container sort on it, report before/after totals
// /zfridgesort fill   build + fill only (a joined player then presses Quark's sort button)
// /zfridgesort check  report totals of the last built fridge against the fill
//
// Console caller = world spawn (spawn chunks are always loaded, so no ungenerated-chunk load).
// PASS = totals unchanged and no slot over its max stack size (the 64 stew becomes 32 + 32).
// Stock Quark (no QuarkSortingHandlerMixin) loses 32 stew plus every stack sorted after it.

const ZFS_FILL = [
	// combined slot -> [item, count]; 0-26 = top fridge, 27-53 = bottom fridge
	[3, 'minecraft:bread', 20],
	[30, 'minecraft:mushroom_stew', 64],
	[40, 'minecraft:apple', 30],
	[50, 'minecraft:carrot', 15],
	[12, 'minecraft:cooked_beef', 9],
]
let zfsLastPos = null

ServerEvents.commandRegistry(event => {
	const { commands: Commands } = event
	const zfsMode = mode => Commands.literal(mode).executes(ctx => zfsRun(ctx.source, mode))
	event.register(
		Commands.literal('zfridgesort')
			.requires(src => src.hasPermission(2))
			.then(zfsMode('run'))
			.then(zfsMode('fill'))
			.then(zfsMode('check'))
	)
})

function zfsTotals(cont) {
	const totals = {}
	let over = 0
	for (let zfsSlot = 0; zfsSlot < cont.getContainerSize(); zfsSlot++) {
		const st = cont.getItem(zfsSlot)
		if (st.isEmpty()) continue
		const key = String(st.getItem())
		totals[key] = (totals[key] || 0) + st.getCount()
		if (st.getCount() > st.getMaxStackSize()) over++
	}
	return { totals: totals, over: over }
}

function zfsExpected() {
	const totals = {}
	ZFS_FILL.forEach(row => {
		const key = String(Item.of(row[1]).getItem())
		totals[key] = (totals[key] || 0) + row[2]
	})
	return totals
}

function zfsSame(a, b) {
	const keys = Object.keys(a).concat(Object.keys(b))
	return keys.every(k => (a[k] || 0) === (b[k] || 0))
}

function zfsRun(src, mode) {
	const level = src.getLevel()
	if (mode !== 'check') {
		const ForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
		const ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')
		const BlockPos = Java.loadClass('net.minecraft.core.BlockPos')
		const fridge = ForgeRegistries.BLOCKS.getValue(new ResourceLocation('cookingforblockheads:fridge')).defaultBlockState()
		const bottom = new BlockPos(src.getPosition()).east(2).south(2)
		level.setBlock(bottom.below(), Java.loadClass('net.minecraft.world.level.block.Blocks').STONE.defaultBlockState(), 3)
		level.setBlock(bottom, fridge, 3)
		level.setBlock(bottom.above(), fridge, 3)
		zfsLastPos = bottom
		const cont = level.getBlockEntity(bottom).getCombinedContainer()
		cont.clearContent()
		ZFS_FILL.forEach(row => cont.setItem(row[0], Item.of(row[1], row[2])))
		src.sendSuccess(Text.yellow(`[zfridgesort] built 2-tall fridge at ${bottom.getX()} ${bottom.getY()} ${bottom.getZ()} (size ${cont.getContainerSize()})`), false)
		if (mode === 'run') {
			const SortingHandler = Java.loadClass('vazkii.quark.base.handler.SortingHandler')
			const InvWrapper = Java.loadClass('net.minecraftforge.items.wrapper.InvWrapper')
			SortingHandler.sortInventory(new InvWrapper(cont), 0, cont.getContainerSize(), null)
		}
	}
	if (zfsLastPos === null) {
		src.sendFailure(Text.red('[zfridgesort] no fridge built yet'))
		return 0
	}
	const be = level.getBlockEntity(zfsLastPos)
	if (be === null) {
		src.sendFailure(Text.red('[zfridgesort] fridge is gone'))
		return 0
	}
	const now = zfsTotals(be.getCombinedContainer())
	const want = zfsExpected()
	if (mode === 'fill') {
		src.sendSuccess(Text.yellow(`[zfridgesort] filled: have=${JSON.stringify(now.totals)} oversized=${now.over} (1 expected before sorting)`), false)
		return 1
	}
	const ok = zfsSame(now.totals, want) && now.over === 0
	const line = `[zfridgesort] ${mode}: ${ok ? 'PASS' : 'FAIL'} have=${JSON.stringify(now.totals)} want=${JSON.stringify(want)} oversized=${now.over}`
	console.info(line)
	src.sendSuccess(ok ? Text.green(line) : Text.red(line), false)
	return ok ? 1 : 0
}
