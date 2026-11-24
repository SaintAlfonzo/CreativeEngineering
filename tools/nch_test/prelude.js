// Stubs for running Creative Common's real neoCraftHelper.js outside the game (tools/test_nch_blueprint.sh).
// Every mod counts as loaded, every item exists, every tag has one item; e.custom records the recipe JSON.
var global = {};
var console = { log: function () {}, info: function () {}, warn: function () {}, error: function (m) { __errors.push(String(m)) } };
var __errors = [];
var Platform = { isLoaded: function () { return true } };
var StartupEvents = { postInit: function () {}, init: function () {}, registry: function () {} };   // the dicts' validation hooks
var Java = { loadClass: function (n) { return {} } };
var Item = { exists: function () { return true } };
var Ingredient = { of: function () { return { itemIds: ['minecraft:stone'] } } };
var __recipes = [];
var __E = { custom: function (json) { var r = { json: json, id: null }; __recipes.push(r); return { id: function (s) { r.id = String(s); return this } } } };
