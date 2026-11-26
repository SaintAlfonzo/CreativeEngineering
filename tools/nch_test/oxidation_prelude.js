// SPDX-License-Identifier: GPL-3.0-only
// Extra stubs for oxidation_aging_test.js (after prelude.js): a ServerEvents.recipes collector, Item.of / Fluid.of
// returning plain values, and an e.recipes.create stub that records kubejs-create calls next to e.custom.
var __serverRecipes = [];
var ServerEvents = { recipes: function (fn) { __serverRecipes.push(fn) } };
Item.of = function (id, nbt) { return id };
var Fluid = { of: function (id, amount) { return { fluid: id, amount: amount } } };
function __kjs(kind) {
	return function (out, input) {
		var r = { kind: kind, out: out, in: Array.isArray(input) ? input : [input], id: null };
		__recipes.push(r);
		return { id: function (s) { r.id = String(s); return this } };
	};
}
__E.recipes = { create: { filling: __kjs('create.filling'), splashing: __kjs('create.splashing') } };
// every fluid exists (NCH._fluidExists reads the Forge registry through these two classes)
Java.loadClass = function (n) {
	if (n === 'net.minecraftforge.registries.ForgeRegistries') return { FLUIDS: { containsKey: function () { return true } } };
	if (n === 'net.minecraft.resources.ResourceLocation') return { of: function (id) { return id } };
	return {};
};
