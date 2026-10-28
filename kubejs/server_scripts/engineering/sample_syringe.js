// Reserved for Creative Cultivation: the sample syringe exists only to start meat cultures, so it is marked with the rest
// of the cultivated-meat code (cultures, culture blocks, their recipes, tags, lang and textures) for a future own mod,
// Creative Cultivation, the way potion brewing and alcohol are marked for Creative Brewing (creative_delight.md 29.5).
//
// Sample Syringe — right-click or attack (left-click) a sampleable passive mob to swap an
// empty syringe in hand for the matching filled one. See:
//   startup_scripts/items/sample_syringe.js   (item registration - empty + 7 filled items)
//   startup_scripts/func/sample_syringe.js    (global.sampleSyringe — sample list + stack builder)

;(function () {
    const $EntityType = Java.loadClass('net.minecraft.world.entity.EntityType')
    const $Player = Java.loadClass('net.minecraft.world.entity.player.Player')
    const $InteractionHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $SoundEvents = Java.loadClass('net.minecraft.sounds.SoundEvents')
    const $SoundSource = Java.loadClass('net.minecraft.sounds.SoundSource')

    const entityTypeId = entity => $EntityType.getKey(entity.getType()).toString()

    // Shared by both the right-click and attack handlers below.
    function trySample(player, target, stack) {
        const level = player.getLevel()
        if (level.isClientSide()) return false

        const typeId = entityTypeId(target)
        if (!global.sampleSyringe.isSampleable(typeId)) return false

        if (global.sampleSyringe.isFull(stack)) {
            player.tell(Text.of('This syringe already has a sample.').red())
            return false
        }

        const filled = global.sampleSyringe.buildFilledStack(typeId)
        if (!filled) return false

        // Replace the empty syringe in hand with the filled one outright - the two are
        // now distinct items (not one item + NBT), so there's nothing to mutate in place.
        player.setItemInHand($InteractionHand.MAIN_HAND, filled)

        // NOTE: target.x()/y()/z() looked right but aren't - Entity has a bare numeric `x`
        // field that Rhino's combined field+method wrapper resolves ahead of the method,
        // so target.x() tried to CALL a number ("Cannot call property x ... it is a
        // number") and killed this handler on every single sample. getX()/getY()/getZ()
        // are the real, unambiguous methods - see memory rhino-fieldandmethods-gotcha.
        level.playSound(null, target.getX(), target.getY(), target.getZ(), $SoundEvents.BOTTLE_FILL, $SoundSource.PLAYERS, 1.0, 1.4)
        player.tell(Text.of('Sampled ' + global.sampleSyringe.SAMPLES[typeId].name + '.').green())
        return true
    }

    ItemEvents.entityInteracted(event => {
        const stack = event.getItem()
        if (String(stack.getItem().getId()) !== global.sampleSyringe.ITEM_ID) return
        if (String(event.getHand()) !== 'MAIN_HAND') return

        if (trySample(event.getPlayer(), event.getTarget(), stack)) event.cancel()
    })

    EntityEvents.hurt(event => {
        let attacker
        try { attacker = event.getSource().getActual() } catch (err) { return } // KubeJS renames DamageSource.getEntity() to getActual()
        if (!attacker || !(attacker instanceof $Player)) return

        let stack
        try { stack = attacker.getMainHandItem() } catch (err) { return }
        if (String(stack.getItem().getId()) !== global.sampleSyringe.ITEM_ID) return

        trySample(attacker, event.getEntity(), stack)
    })
})()
