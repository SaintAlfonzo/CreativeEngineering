// global.tooltips: the tooltip helpers and palette shared by every script pack (Creative Common, moved from the
// pack's client_scripts/tooltips.js, plan_cb_publish.md M1). Callers read it at event time, inside their
// ItemEvents.tooltip callback; the pack's own rows stay in kubejs/client_scripts/tooltips.js.

var NCH_MCRegistry = Java.loadClass('net.minecraft.core.Registry');
var NCH_TipRL = Java.loadClass('net.minecraft.resources.ResourceLocation');

// plan_cb_publish.md M2 rule G5, as in global.NCH: an item id whose mod is not loaded and that is not a registered
// item is skipped quietly, counted per mod in global.tooltips._absentCounts with one console.info line per mod. A
// missing id of a loaded mod still goes to KubeJS as before, so pack typos stay visible. Tags and non-strings pass.
var tipAbsentCounts = {};
function tipAbsent(id) {
    if (typeof id !== 'string' || id === '' || id.charAt(0) === '#' || id.indexOf(':') < 0) return false;
    var ns = global.has.namespace(id);
    if (global.has.mod(ns)) return false;
    try { if (NCH_MCRegistry.ITEM.containsKey(new NCH_TipRL(id))) return false; } catch (err) {}
    if (!tipAbsentCounts[ns]) console.info('[tooltips] skipping tooltip rows for ' + ns + ', which is not loaded');
    tipAbsentCounts[ns] = (tipAbsentCounts[ns] || 0) + 1;
    return true;
}

// The ids of one row without the absent mods' ones; null when nothing is left.
function tipIds(id) {
    if (!Array.isArray(id)) return tipAbsent(id) ? null : id;
    var kept = [];
    for (var k = 0; k < id.length; k++) if (!tipAbsent(id[k])) kept.push(id[k]);
    return kept.length ? kept : null;
}

function buildTooltipLine(line) {
    var data = { text: line[0], color: line[1] };
    var modifiers = line[2];
    if (modifiers) {
        if (modifiers.bold !== undefined) data.bold = modifiers.bold;
        if (modifiers.italic !== undefined) data.italic = modifiers.italic;
        if (modifiers.underlined !== undefined) data.underlined = modifiers.underlined;
        if (modifiers.strikethrough !== undefined) data.strikethrough = modifiers.strikethrough;
        if (modifiers.obfuscated !== undefined) data.obfuscated = modifiers.obfuscated;}
    return data;
}

function addTooltips(e) {
    var entries = Array.prototype.slice.call(arguments, 1);
    for (var i = 0; i < entries.length; i++) {
        var entry = entries[i];
        var id = tipIds(entry[0]);
        if (id === null) continue;
        var lines = entry.slice(1);
        if (lines.length === 1) {
            e.add(id, buildTooltipLine(lines[0]));
        } else {
            var tipLines = [];
            for (var j = 0; j < lines.length; j++) {
                tipLines.push(buildTooltipLine(lines[j]));}
            e.add(id, tipLines);}}      
}

function addTooltipPattern(e, pattern) {
    var idx = 2;
    var excludeIds = [];
    if (arguments.length > 2 && arguments[2] && arguments[2].exclude) {
        excludeIds = arguments[2].exclude;
        idx = 3;}
    var lines = Array.prototype.slice.call(arguments, idx);
    var data;
    if (lines.length === 1) {
        data = buildTooltipLine(lines[0]);
    } else {
        data = [];
        for (var j = 0; j < lines.length; j++) {
            data.push(buildTooltipLine(lines[j]));}}
    var regex = new RegExp('^' + pattern.replace(/[.+^${}()|[\]\\?]/g, '\\$&').replace(/\*/g, '.*') + '$');
    var keys = NCH_MCRegistry.ITEM.keySet();
    var it = keys.iterator();
    var matchCount = 0;
    while (it.hasNext()) {
        var rl = it.next();
        var id = rl.toString();
        if (regex.test(id) && excludeIds.indexOf(id) === -1) {
            e.add(id, data);
            matchCount++;}}
    console.log('[addTooltipPattern] ' + pattern + ' -> ' + matchCount + ' matches (excluded ' + excludeIds.length + ')');
}

function removeTooltips(e) {
    var ids = Array.prototype.slice.call(arguments, 1);
    for (var i = 0; i < ids.length; i++) {
        var tipId = tipIds(ids[i]);
        if (tipId === null) continue;
        e.addAdvanced(tipId, function(item, advanced, text) {
            // Keep index 0 (display name), strip every line after it
            while (text.size() > 1) {
                text.remove(1);
            }
        });
    }
}

// Strip only the lines built from one translation key (a mod's own flavour text), leaving the name
// and every other line alone - unlike removeTooltips, BlockRunner's "Speed Multiplier" line survives.
// A key ending in '*' matches every key with that prefix (mods that build the key from the item id).
//   removeTooltipKey(e, 'desc.somemod.flavour.thing', i.thing, i.thing_slab)
//   removeTooltipKey(e, 'item.somemod.fruit.tooltip.*', i.apple, i.pear)
function removeTooltipKey(e, key) {
    var ids = Array.prototype.slice.call(arguments, 2);
    var isPrefix = key.charAt(key.length - 1) === '*';
    var prefix = isPrefix ? key.substring(0, key.length - 1) : key;
    for (var k = 0; k < ids.length; k++) {
        var tipId = tipIds(ids[k]);
        if (tipId === null) continue;
        e.addAdvanced(tipId, function(item, advanced, text) {
            for (var n = text.size() - 1; n >= 1; n--) {
                var lineKey = null;
                try { lineKey = String(text.get(n).getContents().getKey()); } catch (err) {}
                if (lineKey !== null && (isPrefix ? lineKey.indexOf(prefix) === 0 : lineKey === key)) {
                    text.remove(n);
                }
            }
        });
    }
}

// ############################## Color ##############################
var col = {};

col.black = 'black';
col.dark_blue = 'dark_blue';
	col.dblue = 'dark_blue';
col.dark_green = 'dark_green';
	col.dgreen = 'dark_green';
col.dark_aqua = 'dark_aqua';
	col.daqua = 'dark_aqua';
col.dark_red = 'dark_red';
	col.dred = 'dark_red';
col.dark_purple = 'dark_purple';
	col.dpurple = 'dark_purple';
col.gold = 'gold';
col.gray = 'gray';
col.dark_gray = 'dark_gray';
	col.dgray = 'dark_gray';
col.blue = 'blue';
col.green = 'green';
col.aqua = 'aqua';
col.red = 'red';
col.light_purple = 'light_purple';
	col.lpurple = 'light_purple';
col.yellow = 'yellow';
col.white = 'white';

col.norm = 'gray';
col.uncm = 'green';
col.rare = 'dark_blue';
col.epic = 'dark_purple';
col.leg = 'aqua';
col.warn = 'dark_red';
col.good = 'dark_green';
col.eng = 'gold';

// The helpers and palette for other script packs (Creative Compat's tooltips.js reads them at event time; each script
// pack has its own scope, only global is shared).
global.tooltips = {
	buildTooltipLine: buildTooltipLine,
	addTooltips: addTooltips,
	addTooltipPattern: addTooltipPattern,
	removeTooltips: removeTooltips,
	removeTooltipKey: removeTooltipKey,
	col: col,
	_absent: tipAbsent,
	_absentCounts: tipAbsentCounts,
}
