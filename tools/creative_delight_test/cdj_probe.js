// Creative Delight nested-jar probe (scratch_queue.md SQ-CDJ-01 / -06). Scratch-only: copy it into the scratch
// server's kubejs/server_scripts as zz_cdj_probe.js; never in the pack.
//
//   /cdjtest   members' items exist; fishing net block tags (SQ-FN-04); End's enderman_gristle loot modifier drops on
//              a knife kill, not on a bare-hand kill
//
// Needs the chunk at X, Z loaded: `forceload add 400 400` from the console about 30 s before. Results are logged as
// "[cdjtest] PASS|FAIL|INFO <name>: <detail>" in logs/latest.log, then "[cdjtest] done".
(() => {
    const $CJForgeReg = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $CJFakeFactory = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $CJGameProfile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $CJUUID = Java.loadClass('java.util.UUID')
    const $CJEntityType = Java.loadClass('net.minecraft.world.entity.EntityType')
    const $CJDamage = Java.loadClass('net.minecraft.world.damagesource.DamageSource')
    const $CJHand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $CJItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $CJAABB = Java.loadClass('net.minecraft.world.phys.AABB')

    const $CJTagKey = Java.loadClass('net.minecraft.tags.TagKey')
    const $CJRegistry = Java.loadClass('net.minecraft.core.Registry')
    const $CJRL = Java.loadClass('net.minecraft.resources.ResourceLocation')

    const X = 400, Y = -60, Z = 400
    const MEMBERS = { oceansdelight: 1, oceanic_delight: 1, ends_delight: 1 }
    const KELPS = ['minecraft:kelp', 'minecraft:kelp_plant'].concat(['ochre', 'polar', 'thorny', 'tongue']
        .map(k => 'upgrade_aquatic:' + k + '_kelp').concat(['ochre', 'polar', 'thorny', 'tongue'].map(k => 'upgrade_aquatic:' + k + '_kelp_plant')))
    const SEA_GRASSES = ['minecraft:seagrass', 'minecraft:tall_seagrass', 'oceanic_delight:wild_sea_grape',
        'oceanic_delight:sea_grape_plant', 'upgrade_aquatic:blue_pickerelweed', 'upgrade_aquatic:purple_pickerelweed',
        'upgrade_aquatic:tall_blue_pickerelweed', 'upgrade_aquatic:tall_purple_pickerelweed']
    let results = { pass: 0, fail: 0 }

    function log(kind, name, detail) {
        if (kind == 'PASS') results.pass++
        if (kind == 'FAIL') results.fail++
        console.info('[cdjtest] ' + kind + ' ' + name + ': ' + detail)
    }
    function check(name, ok, detail) { log(ok ? 'PASS' : 'FAIL', name, detail) }

    function itemsAround(level) {
        return level.getEntitiesOfClass($CJItemEntity, new $CJAABB(X - 3, Y - 3, Z - 3, X + 4, Y + 5, Z + 4))
    }

    // kills n endermen with the given main-hand item ('' = bare hand), returns the gristle count dropped
    function killEndermen(level, n, held) {
        let gristle = 0
        for (let i = 0; i < n; i++) {
            itemsAround(level).forEach(e => e.discard())
            let m = $CJEntityType.ENDERMAN.create(level)
            m.setPos(X + 0.5, Y, Z + 0.5)
            level.addFreshEntity(m)
            let pl = $CJFakeFactory.get(level, new $CJGameProfile($CJUUID.randomUUID(), 'cdjprobe' + i))
            if (held) pl.setItemInHand($CJHand.MAIN_HAND, Item.of(held, 1))
            m.attack($CJDamage.playerAttack(pl), 1000)   // KubeJS hides hurt(); attack() calls it
            itemsAround(level).forEach(e => {
                let st = e.getItem()
                if (String(st.getId()) == 'ends_delight:enderman_gristle') gristle += st.getCount()
            })
        }
        itemsAround(level).forEach(e => e.discard())
        return gristle
    }

    function run(level) {
        results = { pass: 0, fail: 0 }
        let counts = {}
        $CJForgeReg.ITEMS.getKeys().forEach(rl => {
            let ns = String(rl.getNamespace())
            if (MEMBERS[ns]) counts[ns] = (counts[ns] || 0) + 1
        })
        Object.keys(MEMBERS).forEach(ns => check('items ' + ns, (counts[ns] || 0) > 0, (counts[ns] || 0) + ' items registered'))

        // SQ-FN-04: the fishing net block tags (loottags.js)
        let tagged = (name, id) => $CJForgeReg.BLOCKS.tags().getTag($CJTagKey.create($CJRegistry.BLOCK_REGISTRY,
            new $CJRL('creativeengineering:fishing_net/' + name))).contains($CJForgeReg.BLOCKS.getValue(new $CJRL(id)))
        KELPS.forEach(id => check('kelp tag ' + id, tagged('kelp', id) && !tagged('sea_grass', id),
            'kelp ' + tagged('kelp', id) + ', sea_grass ' + tagged('sea_grass', id)))
        SEA_GRASSES.forEach(id => check('sea grass tag ' + id, tagged('sea_grass', id) && !tagged('kelp', id),
            'kelp ' + tagged('kelp', id) + ', sea_grass ' + tagged('sea_grass', id)))

        let knife = killEndermen(level, 10, 'farmersdelight:flint_knife')
        check('knife kill drops gristle', knife > 0, knife + ' gristle from 10 knife kills')
        let bare = killEndermen(level, 10, '')
        check('bare-hand kill drops none', bare == 0, bare + ' gristle from 10 bare-hand kills')

        console.info('[cdjtest] done: ' + results.pass + ' pass, ' + results.fail + ' fail')
        return 1
    }

    ServerEvents.commandRegistry(event => {
        const Commands = Java.loadClass('net.minecraft.commands.Commands')
        event.register(
            Commands.literal('cdjtest')
                .requires(src => src.hasPermission(2))
                .executes(ctx => run(ctx.source.getLevel()))
        )
    })
})()
