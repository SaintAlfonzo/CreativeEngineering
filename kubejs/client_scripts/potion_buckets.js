// Vanilla-style potion tooltip for the potion buckets - same layout/wording/colour as
// minecraft:potion/splash_potion/lingering_potion bottles (PotionUtils.addPotionTooltip):
// one line per effect, "Name II (0:30)", coloured blue (beneficial/neutral) or red (harmful),
// plus a grey "No Effects" line for a blank bucket. Items: startup_scripts/items/potion_buckets.js
//
// Colouring uses KubeJS's own MutableComponent.blue()/red()/gray() extension methods
// (dev.latvian.mods.kubejs.core.ComponentKJS - unprefixed per the @RemapPrefixForJS('kjs$')
// pattern, same as jeichanges.js's Text.red(...).bold()) instead of calling
// Component.withStyle(ChatFormatting) directly: that has 3 same-arity overloads
// (ChatFormatting / Style / UnaryOperator<Style>) and throws "ambiguous method" on this Rhino
// build (rhino-forge 1902.2.3-build.284) - see memory rhino-fieldandmethods-gotcha, and the
// note this exact file used to carry about dropping styling entirely for that reason.
// (armor_effects.js/echo_tools.js sidestep the same wall with raw "section-sign" strings, but
// that produces a visually different "grey dash + coloured name + grey duration" line, not
// vanilla's single-colour whole-line look - not used here since matching bottles is the point.)
//
// NOT reproduced: vanilla's extra "attribute modifier" lines for effects like Strength/
// Weakness/Speed/Slowness/Luck (the "+130% Attack Damage" etc. appendix, built from
// MobEffect#getAttributeModifiers()) - a further pile of untested Java calls
// (Attribute/AttributeModifier/Operation/DecimalFormat) for a rarely-scrutinised detail.
// Revisit if it turns out to matter.

const $PB_PotionUtils     = Java.loadClass('net.minecraft.world.item.alchemy.PotionUtils')
const $PB_StringUtil      = Java.loadClass('net.minecraft.util.StringUtil')
const $PB_MobEffectCategory = Java.loadClass('net.minecraft.world.effect.MobEffectCategory')

const PB_TOOLTIP_IDS = [
    'creativeengineering:potion_bucket',
    'creativeengineering:splash_potion_bucket',
    'creativeengineering:lingering_potion_bucket',
]

// "M:SS" (StringUtil.formatTickDuration - same helper vanilla's own tooltip uses) or the
// translated infinite-duration marker. isInfiniteDuration() may not exist on every MC/Forge
// build - fall back to treating a negative duration as infinite if the call itself fails.
function pbDurationText(inst) {
    let infinite
    try { infinite = inst.isInfiniteDuration() } catch (err) { infinite = inst.getDuration() < 0 }
    return infinite
        ? Text.translatable('effect.duration.infinite')
        : Text.literal($PB_StringUtil.formatTickDuration(inst.getDuration()))
}

ItemEvents.tooltip(e => {
    PB_TOOLTIP_IDS.forEach(id => {
        e.addAdvanced(id, (item, advanced, text) => {
            const fx = $PB_PotionUtils.getMobEffects(item)
            if (fx.isEmpty()) {
                text.add(Text.translatable('effect.none').gray())
                return
            }
            fx.forEach(inst => {
                let line = Text.translatable(inst.getEffect().getDescriptionId())
                if (inst.getAmplifier() > 0) {
                    line = Text.translatable('potion.withAmplifier', line,
                        Text.translatable('potion.potency.' + inst.getAmplifier()))
                }
                if (inst.getDuration() > 20) {
                    line = Text.translatable('potion.withDuration', line, pbDurationText(inst))
                }

                let harmful = false
                try { harmful = inst.getEffect().getCategory() === $PB_MobEffectCategory.HARMFUL } catch (err) {}
                text.add(harmful ? line.red() : line.blue())
            })
        })
    })
})
