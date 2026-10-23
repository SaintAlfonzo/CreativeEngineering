// Glass bottles on Cooking for Blockheads' milk jar and cow in a jar (TODO food/drinks #6). CfB's own use() only
// takes buckets (1000 mB) and fails on anything else. Here an empty bottle takes 250 mB out as Farmer's Delight's
// milk bottle, and a milk bottle puts 250 mB back, the same as Create's spout and drain. The cow in a jar refills
// itself at cowJarMilkPerTick (config/cookingforblockheads-common.toml).
// Tank: MilkJarBlockEntity.getFluidTank(), a Balm FluidTank (drain / fill take (fluid, amount, simulate); its
// setChanged syncs the client). Milk is CfB's own Compat.getMilkFluid(), the only fluid the tank accepts.

;[global.blocks.cow_jar, global.blocks.milk_jar].forEach(id => {
	BlockEvents.rightClicked(id, e => {
		var i = global.item
		var MB = 250
		var player = e.player
		var stack = e.item
		// Not stack.is(id): ItemStack.is(Item) / is(TagKey) are ambiguous overloads in this Rhino build
		var fill = Ingredient.of(i.farmersdelight_milk_bottle).test(stack)
		if (!fill && !Ingredient.of(i.glass_bottle).test(stack)) return

		var be = e.block.getEntity()
		if (!be || typeof be.getFluidTank !== 'function') return
		var tank = be.getFluidTank()
		var milk = Java.loadClass('net.blay09.mods.cookingforblockheads.compat.Compat').getMilkFluid()

		if (fill) {
			if (tank.fill(milk, MB, true) < MB) return
			tank.fill(milk, MB, false)
		} else {
			if (tank.getAmount() < MB) return
			tank.drain(tank.getFluid(), MB, false)
		}

		if (!player.isCreative()) stack.shrink(1)
		player.give(fill ? i.glass_bottle : i.farmersdelight_milk_bottle)
		var $SoundEvents = Java.loadClass('net.minecraft.sounds.SoundEvents')
		e.getLevel().playSound(null, e.block.getPos(), fill ? $SoundEvents.BOTTLE_EMPTY : $SoundEvents.BOTTLE_FILL,
			Java.loadClass('net.minecraft.sounds.SoundSource').BLOCKS, 1.0, 1.0)
		e.cancel()
	})
})
