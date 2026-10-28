var global = {};
var console = { log: function () {}, info: function () {}, warn: function () {}, error: function () {} };
var StartupEvents = { postInit: function () {} };
var Platform = { isLoaded: function () { return true } };
var __cbs = [];
var ServerEvents = { recipes: function (cb) { __cbs.push(cb) } };
function __s(v) {
	if (v == null) return String(v);
	if (typeof v === 'string' || typeof v === 'number') return String(v);
	if (Array.isArray(v)) return '[' + v.map(__s).join(', ') + ']';
	if (v.__stack) return (v.count != null && v.count != 1 ? v.count + 'x ' : '') + v.id + (v.chance ? ' @' + v.chance : '');
	if (v.id) return (v.count ? v.count + 'x ' : '') + v.id;
	if (v.fluid) return v.amount + 'mB ' + v.fluid;
	var o = []; for (var k in v) o.push(k + '=' + __s(v[k])); return '{' + o.join(' ') + '}';
}
var Item = { of: function (id, count) { if (id && id.__stack) return id; return { __stack: true, id: id, count: count == null ? 1 : count, withChance: function (c) { this.chance = c; return this } } } };
var Ingredient = { of: function (v) { return { __stack: true, id: __s(v), withCount: function (c) { this.count = c; return this } } } };
var Fluid = { of: function (id, amt) { return { fluid: id, amount: amt } } };
var __log = [];
var __rec = function (name) { return function () {
	var args = Array.prototype.slice.call(arguments);
	var ei = -1; for (var x = 0; x < args.length; x++) if (args[x] === __E) ei = x;
	__log.push(name + '(' + args.map(function (q, x) { return x === ei ? 'e' : __s(q) }).join(' | ') + ')');
} };
var __E = { recipes: { create: {} } };
global.NCH = {
	INCOMPLETE: {},
	_resolveIdCount: function (v) { if (typeof v === 'string') return { id: v, count: 1 }; if (v && v.id) return { id: String(v.id), count: v.count || 1 }; return { id: String(v), count: 1 } },
	ns: function () { return 'creative_delight' },
};
['remove', 'fdCutting', 'smelting', 'smoking', 'campfireCooking', 'shapeless', 'shaped', 'ceBaking', 'fdCooking', 'createMixing',
	'createFilling', 'createCompacting', 'hammering', 'ieArcFurnace', 'createSequencedAssembly'].forEach(function (m) { global.NCH[m] = __rec(m) });
var __run = function (name) {
	__log = [];
	__cbs.forEach(function (cb) { cb(__E) });
	var keep = __log.filter(function (l) { return /^(remove|fdCutting|smelting|smoking|campfireCooking|ceBaking|fdCooking|shapeless)\(/.test(l) });
	return '=== ' + name + ' (' + __log.length + ' calls)\n' + keep.join('\n');
};
