// requires: roll_delight
// requires: goldenfood
// Roll Delight port: food value copies (rows of global.foodValueCopies, applied by creativeengineering/food_value_copies.js;
// feature_writeup/roll_delight.md 2.3). Creative Delight startup script: an edit needs a CD rebuild and restart.
//   golden_roll <- golden_dried_kelp   the golden tier, as every Golden Food food (4 / 1.2, always edible, Regeneration
//                                      II 5 s, Absorption 2 min, Nourishment 2 min); upstream was 12 / 1.2. The donor
//                                      for the copy is Golden Food's gilded kelp, the nearest base to a rice and kelp roll.
// The Golden Roll Slice keeps the member's own value (golden tier cut, no effects): one roll cut in three must not
// hand out the golden effects three times.
global.foodValueCopies = global.foodValueCopies || {}
Object.assign(global.foodValueCopies, {
	golden_roll: 'golden_dried_kelp',
})
