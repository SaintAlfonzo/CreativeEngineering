// requires: creativedelight
// requires: solraspberrypie
// requires: lightmanscurrency
// Foods out of the diet (plan_food_diversity_1121.md I15): 0 diversity, never counted, not in either log.
// Lightman's chocolate coins: unobtainable in the pack (events off in lightmanscurrency-common.lcconfig).
var i = global.item

ServerEvents.tags('item', e => {
	global.tags.add(e, 'solraspberrypie:no_diet', [i.coin_chocolate_copper, i.coin_chocolate_iron, i.coin_chocolate_gold,
		i.coin_chocolate_emerald, i.coin_chocolate_diamond, i.coin_chocolate_netherite])
})
