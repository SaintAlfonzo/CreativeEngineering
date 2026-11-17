// Creative Delight startup script, loaded from the jar after the pack's startup_scripts, so the dicts exist here.
// Restart to apply.
//
// Cornmeal: milled / crushed corn, used for tortillas and cornbread batter and as a flour in pancakes, pies and
// cakes (feature_writeup/recipes_cooking.md 2.1). A recolored flour; texture at
// assets/creativeengineering/textures/item/cornmeal.png. Tags: server_scripts/tags/cookingtags.js. Recipes:
// server_scripts/cooking/cooking.js "Cornmeal".

var reg = global.reg.forPack('creativedelight')
StartupEvents.registry('item', e => {
    reg.item(e, global.item.cornmeal).displayName('Cornmeal')
})
