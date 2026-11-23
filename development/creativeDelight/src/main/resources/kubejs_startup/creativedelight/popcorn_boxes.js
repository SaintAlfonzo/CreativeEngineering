// Creative Delight startup script. Item keys of the placeable popcorn boxes (food/seasoning #3; blocks: CD Java
// food/popcorn/PopcornBoxes, with Corn Delight only). Kept here until they move into Creative Common's item_dict.js
// (Common was being built by another session when they were added); remove this file then.
var i = global.item
global.popcornBoxes = { plain: null, cheese: 'cheese', onion: 'onion', garlic: 'garlic', ghost_pepper: 'ghost_pepper',
	void_pepper: 'void_pepper', cream_and_onion: 'cream_and_onion' }   // variant -> global.flavor key (null: plain)
Object.keys(global.popcornBoxes).forEach(v => {
	if (!i[v + '_popcorn_box']) i[v + '_popcorn_box'] = 'creativedelight:' + v + '_popcorn_box'
})
