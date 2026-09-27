// Meat cultures — raw lab-grown precursor for each cultivated-meat line (recipes that
// grow/cook these into their vanilla-ish meat counterparts: server_scripts/cooking/cultivated_meat.js
// — still WIP over there). Registered here since none of the 7 had an e.create() anywhere
// despite already having textures/models under assets/creativeengineering/{textures,models}/item/.
//
// Plain items for now, no .food() — add nutrition once the cooking recipes are designed.

StartupEvents.registry('item', e => {
    e.create('creativeengineering:culture_beef').displayName('Beef Culture')
    e.create('creativeengineering:culture_pork').displayName('Pork Culture')
    e.create('creativeengineering:culture_mutton').displayName('Mutton Culture')
    e.create('creativeengineering:culture_poultry').displayName('Poultry Culture')
    e.create('creativeengineering:culture_rabbit').displayName('Rabbit Culture')
    e.create('creativeengineering:culture_cod').displayName('Cod Culture')
    e.create('creativeengineering:culture_salmon').displayName('Salmon Culture')
})
