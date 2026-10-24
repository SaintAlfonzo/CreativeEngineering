// PneumaticCraft info tooltips shown without holding Shift (ui/tooltips #6).
// PNC's TooltipEventHandler (ItemTooltipEvent, NORMAL priority) adds "gui.tooltip.item.<id>" only while Shift is
// held and a "Hold Shift" line otherwise. KubeJS' ItemEvents.tooltip runs earlier (Architectury, HIGH priority), so
// it can't see that line; this listener runs after it (LOW) and swaps the hint for the text itself.
// The texts are lang overrides in kubejs/assets/pneumaticcraft/lang/en_us.json.
// Registered once from startup (a client script would add another listener on every reload).

if (Platform.isClientEnvironment()) (function () {
	var $PncForge = Java.loadClass('net.minecraftforge.common.MinecraftForge')
	var $EventPriority = Java.loadClass('net.minecraftforge.eventbus.api.EventPriority')
	var $ItemTooltipEvent = Java.loadClass('net.minecraftforge.event.entity.player.ItemTooltipEvent')
	var $TranslatableContents = Java.loadClass('net.minecraft.network.chat.contents.TranslatableContents')
	var $PncForgeRegistries = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')

	var PNC_ALWAYS_SHOWN = {}
	PNC_ALWAYS_SHOWN[global.item.glycerol] = true

	$PncForge.EVENT_BUS.addListener($EventPriority.LOW, false, $ItemTooltipEvent, event => {
		var id = String($PncForgeRegistries.ITEMS.getKey(event.getItemStack().getItem()))
		if (!PNC_ALWAYS_SHOWN[id]) return
		var text = event.getToolTip()
		for (var n = text.size() - 1; n >= 1; n--) {
			var contents = text.get(n).getContents()
			if (contents instanceof $TranslatableContents
				&& String(contents.getKey()) === 'pneumaticcraft.gui.tooltip.sneakForInfo') {
				text.set(n, Text.translate('gui.tooltip.item.' + id.replace(':', '.')).aqua())
			}
		}
	})
})()
