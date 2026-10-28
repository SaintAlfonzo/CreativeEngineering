ServerEvents.recipes(e=>{
console.log('Coin starting')

const R = global.NCH
var f = global.fluid
var i = global.item;

// ============================================================
// Lightmans coins
// ============================================================

//Remove lightmans chocolate currency
;[i.coinpile_chocolate_copper, i.coinpile_chocolate_iron, i.coinpile_chocolate_gold,
  i.coinpile_chocolate_emerald, i.coinpile_chocolate_diamond, i.coinpile_chocolate_netherite].forEach(coin => {
	R.remove({ output: coin }, e)
	R.remove({ input: coin }, e)
})

console.log('Coin done')
})
