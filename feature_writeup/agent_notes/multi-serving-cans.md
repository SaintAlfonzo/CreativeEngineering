# multi-serving-cans

> Creative Common food/CanServings - cans hold n servings (NBT, not durability), bar+tooltip, empty/crushed/nothing roll, NCH `cans` arg for shaped/shapeless via KubeJS custom ingredient action

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Built 2026-09-28 (feature_writeup/food.md 3.6, FO-14..22). Jar built.

- Servings: kubejs/generation_scripts/food/can_servings.json (dict key -> n) -> server_scripts/tags/cantags.js -> `#creativeengineering:cans/servings/<n>` (+ parents cans/servings, cans; keys i.t_can_servings, i.t_cans). Java parses the n from the tag path.
- NBT `CanServings` only on opened cans, so full cans keep stack 64; real durability was rejected (stack size 1 + enchantable).
- Eating: ItemStackCanServingsMixin @Inject HEAD on ItemStack m_41671_; runs the can's own finishUsingItem on a 1-count copy (keeps Canned Goods procedures), discards its empty can; open cans in the inventory eaten first. Bar: same mixin on m_150947_/48/49.
- Roll: CEConfig [canning] emptyCanChance 0.7 / crushedCanChance 0.25 / rest nothing.
- Recipes: NCH shaped(..., damage, cans) / shapeless(..., damage, replace, cans) now return the recipe; attaches `creativeengineering:can_serving` custom ingredient action. **KubeJS clears CustomIngredientAction.MAP on every server script load** (ServerScriptManager), so NCH re-registers via compat.KubeJSCanAction.register() each call.
- Defaults double can value (2 servings x Canned Goods' 2-food values) until parity tuning.

Related: [food-tuning-system](food-tuning-system.md), [patchermod-coremod](patchermod-coremod.md), memory `centralize-recipes-tags-items`.
