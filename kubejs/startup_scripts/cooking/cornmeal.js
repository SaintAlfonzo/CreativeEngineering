// Cornmeal: milled / crushed corn, used for tortillas and cornbread batter and as a flour in pancakes, pies and
// cakes (feature_writeup/cooking.md "cornmeal"). A recolored flour; texture at
// assets/creativeengineering/textures/item/cornmeal.png. Tags: server_scripts/tags/cookingtags.js. Recipes:
// server_scripts/cooking/cooking.js "Cornmeal".

StartupEvents.registry('item', e => {
    e.create(global.item.cornmeal).displayName('Cornmeal')
})
