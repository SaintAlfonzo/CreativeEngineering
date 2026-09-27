ServerEvents.recipes(e=>{
console.log('Coin starting')

const R = global.NCH
var f = global.fluid
var i = global.item;

// ============================================================
// Lightmans coins
// ============================================================

//Remove lightmans chocolate currency
e.remove({output: i.coinpile_chocolate_copper})
e.remove({input: i.coinpile_chocolate_copper})
e.remove({output: i.coinpile_chocolate_iron})
e.remove({input: i.coinpile_chocolate_iron})
e.remove({output: i.coinpile_chocolate_gold})
e.remove({input: i.coinpile_chocolate_gold})
e.remove({output: i.coinpile_chocolate_emerald})
e.remove({input: i.coinpile_chocolate_emerald})
e.remove({output: i.coinpile_chocolate_diamond})
e.remove({input: i.coinpile_chocolate_diamond})
e.remove({output: i.coinpile_chocolate_netherite})
e.remove({input: i.coinpile_chocolate_netherite})

console.log('Coin done')
})
