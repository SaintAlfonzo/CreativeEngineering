ServerEvents.recipes(e=>{
console.log('chocolate starting')

const R = global.NCH
var f = global.fluid
var i = global.item;

// ============================================================
// Ruby
// ============================================================

//Dragons breath fluid in mixing 
R.remove({ id: 'create_confectionery:ruby_chocolate_recipe' }, e)
R.createMixing(Fluid.of(f.ruby_chocolate, 250), [Fluid.of(f.milk, 250), Fluid.of(f.dragon_breath, 250), i.cocoa_beans, i.any_sugar], e, 'ruby_chocolate_mixing', 'heated')

console.log('chocolate done')
})
