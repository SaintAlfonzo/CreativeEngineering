// PneumaticCraft info tooltips shown without holding Shift, in plain gray (ui/tooltips #6, #9).
// PNC's TooltipEventHandler (ItemTooltipEvent, NORMAL priority) adds its item text (key from
// ICustomTooltipName.getTranslationKey) only while Shift is held, in aqua with its own colour codes, and a
// "Hold Shift" line otherwise. KubeJS' ItemEvents.tooltip runs earlier (Architectury, HIGH priority), so it can't see
// that line; this listener runs after it (LOW) and swaps the hint for the text itself: wrapped by PNC's own splitter
// (which also turns ${br} into line breaks), colour codes stripped, gray. While Shift is held PNC's own lines stay.
// Pack text overrides (none since the glycerol one went, 2026-11-02) are lang entries in kubejs/assets/pneumaticcraft/lang/en_us.json.
// Registered once from startup (a client script would add another listener on every reload).

if (Platform.isClientEnvironment()) (function () {
	var $PncForge = Java.loadClass('net.minecraftforge.common.MinecraftForge')
	var $EventPriority = Java.loadClass('net.minecraftforge.eventbus.api.EventPriority')
	var $ItemTooltipEvent = Java.loadClass('net.minecraftforge.event.entity.player.ItemTooltipEvent')
	var $TranslatableContents = Java.loadClass('net.minecraft.network.chat.contents.TranslatableContents')
	var $PncI18n = Java.loadClass('net.minecraft.client.resources.language.I18n')
	var $PncTooltipName = Java.loadClass('me.desht.pneumaticcraft.common.item.ICustomTooltipName')
	var $PncUtils = Java.loadClass('me.desht.pneumaticcraft.common.util.PneumaticCraftUtils')

	var HINT = 'pneumaticcraft.gui.tooltip.sneakForInfo'
	var CODES = /§./g

	$PncForge.EVENT_BUS.addListener($EventPriority.LOW, false, $ItemTooltipEvent, event => {
		var text = event.getToolTip()
		for (var n = text.size() - 1; n >= 1; n--) {
			var contents = text.get(n).getContents()
			if (!(contents instanceof $TranslatableContents) || String(contents.getKey()) !== HINT) continue
			var key = $PncTooltipName.getTranslationKey(event.getItemStack(), true)
			if (!$PncI18n.exists(key)) return
			var lines = $PncUtils.splitString($PncI18n.get(key, []))
			text.remove(n)
			for (var i = 0; i < lines.size(); i++) {
				text.add(n + i, Text.of(String(lines.get(i)).replace(CODES, '')).gray())
			}
			return
		}
	})
})()
