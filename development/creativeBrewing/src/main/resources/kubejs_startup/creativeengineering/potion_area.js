// Moved from the pack's startup_scripts/func/potion_area.js 2026-11-01 (creative_brewing.md 4.5).
// Startup script in the CB jar: runs after the pack folder and CD; CB rebuild + restart.
// global.potionArea — spawns the expanding lingering-potion dome for the
// creativeengineering potion buckets (potion_buckets.js (this folder)).
// The right-click behaviour that calls this lives in
// CB server creativeengineering/potion_buckets.js.
//
// A potion bucket spawns a STACK of vanilla AreaEffectClouds - one "ring" per level, up to
// `rings` per bucket - instead of one flat disc:
//   - ring 0 sits at the spawn point with the bucket's full `radius`
//   - each ring above it is smaller and higher up, shaped so the whole stack's silhouette
//     approximates a dome (a hemisphere cross-section: radius_i = radius * sqrt(1 - f^2),
//     height_i = radius * f, where f = i / rings)
//   - every ring grows over time (`growthPerTick`, scaled down for the smaller rings so the
//     whole dome expands roughly uniformly), lasts `lingerTicks`, and carries the same potion
//     effects - so more rings = a rounder, taller-looking cloud, not a flat circle
//
// (An earlier attempt used a patcherMod Mixin to make a single cloud spherical - ripped out,
// see the patchermod-coremod / potion-buckets memory notes for why. This stacked-ring
// approach is pure KubeJS, no Java rebuild needed.)
//
// Every dial below is per-bucket (BUCKETS is keyed by item id, not shared) - edit freely.

console.log('potion_area func loading')

;(function () {
    const $AreaEffectCloud   = Java.loadClass('net.minecraft.world.entity.AreaEffectCloud')
    const $MobEffectInstance  = Java.loadClass('net.minecraft.world.effect.MobEffectInstance')
    const $PotionUtils        = Java.loadClass('net.minecraft.world.item.alchemy.PotionUtils')
    const $ForgeRegistries    = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $ResourceLocation   = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $SoundEvents        = Java.loadClass('net.minecraft.sounds.SoundEvents')
    const $SoundSource        = Java.loadClass('net.minecraft.sounds.SoundSource')

    // ---- tuning: one entry per bucket item, edit freely -----------------------
    //   radius         starting radius of the bottom ring (blocks)
    //   growthPerTick  expansion speed - radius added every tick to the bottom ring (0 = constant size)
    //   lingerTicks    total lifetime of every ring, in ticks (20 = 1s) - "linger time"
    //   waitTicks      short windup before the rings start growing/dosing (vanilla flavour)
    //   effectMult     multiplies the carried potion's own effect durations
    //   rings          ring limit - how many stacked clouds make up the dome (1 = old flat disc)
    const BUCKETS = {
        'creativeengineering:potion_bucket': {
            radius: 1.0, growthPerTick: 0.030, lingerTicks: 35, waitTicks: 16, effectMult: 1.0, rings: 1,
        },
        'creativeengineering:splash_potion_bucket': {
            radius: 1.5, growthPerTick: 0.04, lingerTicks: 50, waitTicks: 8, effectMult: 1.1, rings: 2,
        },
        'creativeengineering:lingering_potion_bucket': {
            radius: 2, growthPerTick: 0.05, lingerTicks: 100, waitTicks: 4, effectMult: 1.2, rings: 4,
        },
    }

    // used only when the bucket carries no potion NBT (no Potion / CustomPotionEffects tag) — { id, seconds, amplifier }
    const DEFAULT_EFFECTS = [
        { id: 'minecraft:night_vision', seconds: 8, amplifier: 0 },
    ]
    // ----------------------------------------------------------------------------

    const mobEffect = (id) => $ForgeRegistries.MOB_EFFECTS.getValue(new $ResourceLocation(id))

    // rebuild a MobEffectInstance with its duration scaled (instant effects untouched)
    function scaledInstance(inst, mult) {
        const dur = inst.getDuration() > 1
            ? Math.max(1, Math.round(inst.getDuration() * mult))
            : inst.getDuration()
        return new $MobEffectInstance(inst.getEffect(), dur, inst.getAmplifier(), inst.isAmbient(), inst.isVisible())
    }

    // NOTE: deliberately NOT calling PotionUtils.getColor(...) here - this Rhino build
    // (rhino-forge 1902.2.3-build.284) cannot disambiguate it: getColor has 3 same-arity
    // overloads (Potion / ItemStack / Collection) and Rhino throws "ambiguous" no matter
    // which one actually matches the argument's runtime type. Blend the colour ourselves
    // from MobEffect.getColor() (a single, unambiguous no-arg method) instead. See memory
    // note rhino-fieldandmethods-gotcha for the wider pattern (same-arity overloads of
    // unrelated types are unsafe to call directly from KubeJS scripts here).
    // effects: a plain JS array of MobEffectInstance (NOT a Java List - no .isEmpty() here).
    function blendColor(stack, effects) {
        try {
            // getOrCreateTag(), never .getTag() - this Rhino build throws "Cannot find
            // function getTag" on a real ItemStack (a 4th reflection gap alongside the other
            // 3 in rhino-fieldandmethods-gotcha); the try/catch here had been silently
            // swallowing that every time, so this branch never actually ran until fixed.
            const tag = stack && stack.getOrCreateTag()
            if (tag && tag.contains('CustomPotionColor')) return tag.getInt('CustomPotionColor')
        } catch (err) {}

        if (!effects || effects.length === 0) return null
        let r = 0, g = 0, b = 0, n = 0
        effects.forEach(inst => {
            const c = inst.getEffect().getColor()
            r += (c >> 16) & 255
            g += (c >> 8) & 255
            b += c & 255
            n++
        })
        return n === 0 ? null : ((Math.round(r / n) << 16) | (Math.round(g / n) << 8) | Math.round(b / n))
    }

    // spawns exactly one AreaEffectCloud "ring" - shared by every level of the dome stack
    function spawnRing(level, x, y, z, radius, growthPerTick, dial, baseEffects, color, owner) {
        const cloud = new $AreaEffectCloud(level, x, y, z)
        cloud.setRadius(radius)
        cloud.setRadiusOnUse(0.0)
        cloud.setWaitTime(dial.waitTicks)
        cloud.setDuration(dial.lingerTicks)
        cloud.setRadiusPerTick(growthPerTick)
        if (owner) cloud.setOwner(owner)
        if (color !== null) cloud.setFixedColor(color)
        baseEffects.forEach(inst => cloud.addEffect(scaledInstance(inst, dial.effectMult)))

        level.addFreshEntity(cloud)
        return cloud
    }

    // Dispenser support: KubeJS has no API to register a DispenserBlock behavior, so
    // patcherMod's PotionBucketDispenser owns the actual DispenserBlock.registerBehavior(...)
    // calls (see CreativeEngineering#commonSetup) and just forwards into whatever handler we
    // install here - keeping global.potionArea.spawn (below) the single place that knows
    // about rings/radii/colours/effects. Right-click still goes straight through
    // CB server creativeengineering/potion_buckets.js -> global.potionArea.spawn directly;
    // this wiring only covers the dispenser path.
    try {
        // no const/let here: Rhino throws "redeclaration of var" for a block-scoped decl in this try
        // owner: the player who placed the dispenser, when online in that level, else null (potions/potion-buckets #3)
        Java.loadClass('com.SaintAlfonzo.creativebrewing.potion.PotionBucketDispenser').setHandler((level, x, y, z, itemId, stack, owner) => {
            global.potionArea.spawn(level, x, y, z, itemId, stack, owner)
        })
        console.log('[potion_area] dispenser handler installed')
    } catch (err) {
        console.warn('[potion_area] could not install dispenser handler (Creative Brewing PotionBucketDispenser missing?): ' + err)
    }

    global.potionArea = {
        BUCKETS: BUCKETS,
        DEFAULT_EFFECTS: DEFAULT_EFFECTS,

        // the effect as a ring applies it (duration x the bucket's effectMult) - the tooltip
        // in CB client creativeengineering/potion_buckets.js shows this, so it matches what the player gets
        scaled(itemId, inst) {
            const dial = BUCKETS[itemId] || BUCKETS['creativeengineering:potion_bucket']
            return scaledInstance(inst, dial.effectMult)
        },

        // level: server-side Level | x,y,z: doubles (base spawn point) | itemId: a key of BUCKETS
        // stack: the potion-bucket ItemStack (read for NBT) | owner: LivingEntity or null
        spawn(level, x, y, z, itemId, stack, owner) {
            const dial = BUCKETS[itemId] || BUCKETS['creativeengineering:potion_bucket']
            const ringCount = Math.max(1, Math.round(dial.rings || 1))

            // effects: prefer the bucket's own potion (getMobEffects is a single, unambiguous
            // overload - unlike getColor above), else fall back to DEFAULT_EFFECTS - but only for a
            // bucket with no potion NBT at all. A no-effect potion (awkward, thick, mundane, water)
            // spawns nothing and returns [], so the caller keeps the bucket.
            const stackEffects = stack ? $PotionUtils.getMobEffects(stack) : null
            let baseEffects = []
            if (stackEffects && !stackEffects.isEmpty()) {
                stackEffects.forEach(inst => baseEffects.push(inst))
            } else if (stack && (stack.getOrCreateTag().contains('Potion') || stack.getOrCreateTag().contains('CustomPotionEffects'))) {
                return []
            } else {
                DEFAULT_EFFECTS.forEach(d => {
                    const me = mobEffect(d.id)
                    if (me) baseEffects.push(new $MobEffectInstance(me, Math.max(1, Math.round(d.seconds * 20)), d.amplifier))
                })
            }
            const color = blendColor(stack, baseEffects)

            const rings = []
            // NOTE: var, not const/let - this Rhino build hoists a loop-body const/let to
            // function scope instead of giving it real per-iteration block scope, so
            // re-running `const f = ...` on iteration 2 throws "redeclaration of var f".
            for (var i = 0; i < ringCount; i++) {
                // f goes 0 (bottom ring, full radius) up to (but never reaching) 1 (top) -
                // radius/height follow a hemisphere's cross-section so the stack reads as a dome
                var f = i / ringCount
                var ringRadius = dial.radius * Math.sqrt(Math.max(0, 1 - f * f))
                var ringY = y + dial.radius * f
                var ringGrowth = dial.growthPerTick * (ringRadius / dial.radius)

                rings.push(spawnRing(level, x, ringY, z, ringRadius, ringGrowth, dial, baseEffects, color, owner))
            }

            // same splash used when emptying a water bucket, not a potion-throw sound - played
            // once for the whole dome, not once per ring
            level.playSound(null, x, y, z, $SoundEvents.BUCKET_EMPTY, $SoundSource.BLOCKS, 1.0, 1.0)
            return rings
        },
    }
})()
