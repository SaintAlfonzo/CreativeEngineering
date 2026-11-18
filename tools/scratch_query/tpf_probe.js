// Scratch-only probe (run r1113tpf, third-party bug fixes: feature_writeup/thirdparty_fixes.md). Copy into a scratch
// server's kubejs/server_scripts as zz_tpf_probe.js, never into the pack.
//   /ztpf sound                       "[ztpf] raw_copper_block sound=<copper|stone|other>"            (MC-223153)
//   /ztpf bignbt NAME KB plain|rand   gives NAME one paper whose NBT holds about KB kilobytes of string data
//                                     (plain = 'x' repeated: tiny on the wire, tests only the 2 MiB NBT read quota,
//                                     which counts 2 bytes per char; rand = random letters: also a large frame)
//                                     "[ztpf] bignbt NAME strings=N chars=N quotaBytes~N mode=..."
//   /ztpf bigpayload NAME KB          sends NAME a custom payload of KB kilobytes on channel ztpf:big (the 1 MiB
//                                     clientbound payload limit); "[ztpf] bigpayload sent bytes=N" or "... error ..."
//   /ztpf chunksize X Z               "[ztpf] chunksize cx,cz sectionBytes=N blockEntities=N" (the read buffer the
//                                     client checks against 2 MiB in ClientboundLevelChunkPacketData)
//   /ztpf worstchunk X Z              fills the chunk at block X Z with as many distinct block states as fit (no block
//                                     entities, no neighbor updates), then prints chunksize: the upper bound of a
//                                     chunk's section buffer with this pack's block state count
//   /ztpf heavychunk X Z COUNT KB     places COUNT campfires in the chunk at block X Z (rows of 16 at y=-60 upward),
//                                     each holding 4 papers of KB kilobytes of incompressible NBT. A campfire's sync
//                                     tag carries its items, so the chunk packet grows with COUNT x 4 x KB: the
//                                     storage-heavy chunk case (per block entity quota 2 MiB, compressed frame 2 MiB,
//                                     decompressed 8 MiB). "[ztpf] heavychunk placed=N approxBytes=N"
(() => {
	function out(s) { console.info('[ztpf] ' + s) }
	const $Blocks = Java.loadClass('net.minecraft.world.level.block.Blocks')
	const $Block = Java.loadClass('net.minecraft.world.level.block.Block')
	const $SoundType = Java.loadClass('net.minecraft.world.level.block.SoundType')
	const $ItemStack = Java.loadClass('net.minecraft.world.item.ItemStack')
	const $Items = Java.loadClass('net.minecraft.world.item.Items')
	const $CompoundTag = Java.loadClass('net.minecraft.nbt.CompoundTag')
	const $StringBuilder = Java.loadClass('java.lang.StringBuilder')
	const $JRandom = Java.loadClass('java.util.Random')
	// io.netty.* is blocked by the KubeJS class filter (r1114): loaded lazily in bigpayload, which then reports the error
	const $FriendlyByteBuf = Java.loadClass('net.minecraft.network.FriendlyByteBuf')
	const $ResourceLocation = Java.loadClass('net.minecraft.resources.ResourceLocation')
	const $Payload = Java.loadClass('net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket')
	const $ChunkData = Java.loadClass('net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData')
	const $BlockPos = Java.loadClass('net.minecraft.core.BlockPos')
	const $Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
	const $Int = Java.loadClass('com.mojang.brigadier.arguments.IntegerArgumentType')

	function sound() {
		try {
			let st = $Blocks.RAW_COPPER_BLOCK.defaultBlockState().getSoundType()
			let name = st == $SoundType.COPPER ? 'copper' : (st == $SoundType.STONE ? 'stone' : 'other')
			out('raw_copper_block sound=' + name)
		} catch (err) { out('error ' + err) }
	}

	function str(len, rand, rng) {
		let sb = new $StringBuilder(len)
		for (let i = 0; i < len; i++) sb.append(rand ? String.fromCharCode(97 + rng.nextInt(26)) : 'x')
		return sb.toString()
	}

	function bignbt(server, name, kb, mode) {
		try {
			let p = server.getPlayerList().getPlayerByName(name)
			if (p == null) { out('bignbt ' + name + ' no player'); return }
			let rand = mode == 'rand', rng = new $JRandom(42), per = 30000
			let total = kb * 1024, n = Math.max(1, Math.ceil(total / per))
			let tag = new $CompoundTag()
			for (let i = 0; i < n; i++) tag.putString('z' + i, str(per, rand, rng))
			let stack = $Items.PAPER.getDefaultInstance()
			stack.setTag(tag)
			p.getInventory().add(stack)
			out('bignbt ' + name + ' strings=' + n + ' chars=' + (n * per) + ' quotaBytes~' + (n * (36 + 2 * per)) + ' mode=' + mode)
		} catch (err) { out('bignbt error ' + err) }
	}

	function bigpayload(server, name, kb) {
		try {
			let p = server.getPlayerList().getPlayerByName(name)
			if (p == null) { out('bigpayload ' + name + ' no player'); return }
			let $Unpooled = Java.loadClass('io.netty.buffer.Unpooled')
			let buf = new $FriendlyByteBuf($Unpooled.buffer(kb * 1024))
			for (let i = 0; i < kb * 1024; i++) buf.writeByte(i & 0x7f)
			p.connection.send(new $Payload(new $ResourceLocation('ztpf', 'big'), buf))
			out('bigpayload sent bytes=' + (kb * 1024))
		} catch (err) { out('bigpayload error ' + err) }
	}

	function chunksize(level, x, z) {
		try {
			let chunk = level.getChunk(x >> 4, z >> 4)
			let data = new $ChunkData(chunk)
			out('chunksize ' + (x >> 4) + ',' + (z >> 4) + ' sectionBytes=' + data.getReadBuffer().readableBytes() +
				' blockEntities=' + chunk.getBlockEntities().size())
		} catch (err) { out('chunksize error ' + err) }
	}

	function worstchunk(level, x, z) {
		try {
			let states = []
			$Block.BLOCK_STATE_REGISTRY.forEach(s => { if (!s.hasBlockEntity() && !s.isAir()) states.push(s) })
			let bx = (x >> 4) << 4, bz = (z >> 4) << 4, k = 0
			for (let y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++)
				for (let dx = 0; dx < 16; dx++)
					for (let dz = 0; dz < 16; dz++) {
						level.setBlock(new $BlockPos(bx + dx, y, bz + dz), states[k % states.length], 2 | 16 | 32)
						k++
					}
			out('worstchunk placed=' + k + ' distinctStates=' + states.length)
			chunksize(level, x, z)
		} catch (err) { out('worstchunk error ' + err) }
	}

	function heavychunk(level, x, z, count, kb) {
		try {
			const $Campfire = Java.loadClass('net.minecraft.world.level.block.entity.CampfireBlockEntity')
			let bx = (x >> 4) << 4, bz = (z >> 4) << 4, rng = new $JRandom(7), placed = 0
			for (let i = 0; i < count; i++) {
				let pos = new $BlockPos(bx + (i % 16), -60 + Math.floor(i / 256), bz + (Math.floor(i / 16) % 16))
				level.setBlock(pos, $Blocks.CAMPFIRE.defaultBlockState(), 2)
				let be = level.getBlockEntity(pos)
				if (!(be instanceof $Campfire)) continue
				let items = be.getItems()
				for (let s = 0; s < 4; s++) {
					let tag = new $CompoundTag()
					tag.putString('z', str(kb * 1024, true, rng))
					let paper = $Items.PAPER.getDefaultInstance()
					paper.setTag(tag)
					items.set(s, paper)
				}
				be.setChanged()
				level.sendBlockUpdated(pos, be.getBlockState(), be.getBlockState(), 3)
				placed++
			}
			out('heavychunk placed=' + placed + ' approxBytes=' + (placed * 4 * kb * 1024))
		} catch (err) { out('heavychunk error ' + err) }
	}

	ServerEvents.commandRegistry(e => {
		const { commands: C } = e
		e.register(C.literal('ztpf').requires(s => s.hasPermission(2))
			.then(C.literal('sound').executes(ctx => { sound(); return 1 }))
			.then(C.literal('bignbt').then(C.argument('name', $Args.word()).then(C.argument('kb', $Int.integer(1))
				.then(C.argument('mode', $Args.word()).executes(ctx => {
					bignbt(ctx.source.server, String($Args.getString(ctx, 'name')), $Int.getInteger(ctx, 'kb'),
						String($Args.getString(ctx, 'mode'))); return 1 })))))
			.then(C.literal('bigpayload').then(C.argument('name', $Args.word()).then(C.argument('kb', $Int.integer(1))
				.executes(ctx => {
					bigpayload(ctx.source.server, String($Args.getString(ctx, 'name')), $Int.getInteger(ctx, 'kb')); return 1 }))))
			.then(C.literal('chunksize').then(C.argument('x', $Int.integer()).then(C.argument('z', $Int.integer())
				.executes(ctx => { chunksize(ctx.source.level, $Int.getInteger(ctx, 'x'), $Int.getInteger(ctx, 'z')); return 1 }))))
			.then(C.literal('worstchunk').then(C.argument('x', $Int.integer()).then(C.argument('z', $Int.integer())
				.executes(ctx => { worstchunk(ctx.source.level, $Int.getInteger(ctx, 'x'), $Int.getInteger(ctx, 'z')); return 1 }))))
			.then(C.literal('heavychunk').then(C.argument('x', $Int.integer()).then(C.argument('z', $Int.integer())
				.then(C.argument('count', $Int.integer(1, 4096)).then(C.argument('kb', $Int.integer(1))
					.executes(ctx => { heavychunk(ctx.source.level, $Int.getInteger(ctx, 'x'), $Int.getInteger(ctx, 'z'),
						$Int.getInteger(ctx, 'count'), $Int.getInteger(ctx, 'kb')); return 1 })))))))
	})
})()
