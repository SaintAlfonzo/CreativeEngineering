// Creative Compat; moved from kubejs/server_scripts/economy/coin.js on 2026-11-01.
ServerEvents.recipes(e=>{
console.log('Coin starting')

const R = global.NCH
var f = global.fluid
var i = global.item;

// ============================================================
// Lightmans coins
// ============================================================

//Remove lightmans chocolate currency
// G5 begin: R.remove filters only
;[i.coinpile_chocolate_copper, i.coinpile_chocolate_iron, i.coinpile_chocolate_gold,
  i.coinpile_chocolate_emerald, i.coinpile_chocolate_diamond, i.coinpile_chocolate_netherite].forEach(coin => {
// G5 end
	R.remove({ output: coin }, e)
	R.remove({ input: coin }, e)
})

console.log('Coin done')
})
