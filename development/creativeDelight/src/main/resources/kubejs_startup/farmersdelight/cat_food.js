// requires: farmersdelight
// Cat Food (TODO dishes/meals #56): Farmer's Delight's dog food for cats. Creative Delight startup script, loaded from the
// jar after the pack's startup_scripts, so global.item exists here. Restart to apply.
// Art: Farmer's Delight's dog food sprite, read in place. Values: the dog food's whole food (row
// below, food_value_copies.js); the bowl comes back when a player eats it. Recipes:
// data/creativedelight/kubejs/farmersdelight/recipes.js. Feeding a tamed cat does what the dog food does to a tamed wolf
// (Farmer's Delight DogFoodItem.DogFoodEvent): full health, Speed, Strength and Resistance for 5 minutes, the bowl back.
var reg = global.reg.forPack('creativedelight')
const $CatFoodCat = Java.loadClass('net.minecraft.world.entity.animal.Cat')
const $CatFoodEffect = Java.loadClass('net.minecraft.world.effect.MobEffectInstance')
const $CatFoodEffects = Java.loadClass('net.minecraft.world.effect.MobEffects')
const $CatFoodSounds = Java.loadClass('net.minecraft.sounds.SoundEvents')
const $CatFoodSource = Java.loadClass('net.minecraft.sounds.SoundSource')
const $CatFoodResult = Java.loadClass('net.minecraft.world.InteractionResult')
const $CatFoodParticles = Java.loadClass('net.minecraft.core.particles.ParticleTypes')
StartupEvents.registry('item', e => {
	var i = global.item
	reg.item(e, i.cat_food).displayName('Cat Food').texture('farmersdelight:item/dog_food').maxStackSize(32)
		.food(f => {
			f.hunger(4).saturation(0.2)
			f.eaten(ev => {
				var p = ev.player
				if (p && !p.isCreative()) p.give(i.bowl)
			})
		})
})

global.foodValueCopies = global.foodValueCopies || {}
global.foodValueCopies.cat_food = 'dog_food'

ForgeEvents.onEvent('net.minecraftforge.event.entity.player.PlayerInteractEvent$EntityInteract', ev => {
	var cat = ev.getTarget()
	if (!(cat instanceof $CatFoodCat) || !cat.isAlive() || !cat.isTame()) return
	var stack = ev.getItemStack()
	if (String(stack.getItem().getId()) !== global.item.cat_food) return
	var player = ev.getEntity()
	var level = cat.level
	cat.setHealth(cat.getMaxHealth())
	;[$CatFoodEffects.MOVEMENT_SPEED, $CatFoodEffects.DAMAGE_BOOST, $CatFoodEffects.DAMAGE_RESISTANCE]
		.forEach(eff => cat.addEffect(new $CatFoodEffect(eff, 6000, 0)))
	level.playSound(null, cat.blockPosition(), $CatFoodSounds.GENERIC_EAT, $CatFoodSource.PLAYERS, 0.8, 0.8)
	if (level.isClientSide()) {
		for (var n = 0; n < 5; n++)
			level.addParticle($CatFoodParticles.HEART, cat.getRandomX(1.0), cat.getRandomY() + 0.5, cat.getRandomZ(1.0), 0, 0, 0)
	}
	if (!player.isCreative()) {
		player.addItem(Item.of(global.item.bowl))
		stack.shrink(1)
	}
	ev.setCancellationResult($CatFoodResult.SUCCESS)
	ev.setCanceled(true)
})
