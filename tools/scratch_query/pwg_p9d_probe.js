// Scratch-only probe for the Create: Power Grid port, phase P9d rows (SQ-PWG-84 onward, plan_power_grid.md P9d: the
// Integrated Circuit, the boost recipes, the electrical gizmo assembly). Copy into a scratch server's
// kubejs/server_scripts as zz_pwg_p9d_probe.js next to the P2 to P9c probes (zpwg7: fp, wear, hold, dig, zap, baton,
// hp; zpwg9c: line, deployer, depot, feed, loop, show); never into the pack. Uses its own Forge fake player
// ("[zpwg9d]") for the hand path, and reads / changes the main hand of zpwg7's fake player ("[zpwg7]", same profile, so
// FakePlayerFactory returns the same player) for the boosted equipment rows. No chunk loads here: `forceload add` the
// area first (Rhino deadlock).
//   fp X Y Z                 moves [zpwg9d] to stand at X Y Z (survival; do this before zappass so the zap
//                            projectile spawns in a loaded chunk)
//   check                    P9d items and classes, the boost_recipe serializer, the six P9d recipes (class, type,
//                            serializer), Boosted amounts per tool (30 % of max damage), ItemBoostUtils.findRecipe
//                            (chip + each tool, + a boosted drill, + a stick), BoostRecipe.matches on a boosted tool,
//                            RecipeApplierMixin unit check (boost recipe on a damaged, named, enchanted drill)
//   hand ITEM                [zpwg9d]: main hand ITEM, off hand 2 Integrated Circuits; the chip's use (off hand), then
//                            its finishUsingItem (the 32-tick eat, called directly: fake players never tick); tool
//                            Boosted and chip count; then a second use on the boosted tool
//   handswap                 B28: as hand portable_drill, but the main hand becomes a diamond sword before the finish
//   zappass                  [zpwg9d]: an unboosted zapper (main) with a chip in the off hand passes (no projectile);
//                            a boosted one with the chip shoots and uses one boost
//   boost [N]                [zpwg7]'s main hand: setBoosted (or Boosted = N when given; 0 removes it); prints it
//   boosted                  [zpwg7]'s main hand: Boosted, damage, drill speed level
//   drill0                   [zpwg7]: drill speed level back to 0 (the fake player never ticks, so the level from earlier
//                            digs never decays)
//   tool X Y Z ITEM DAMAGE [belt]   ITEM with Damage DAMAGE, name "zpwg9d", Unbreaking I, tag ProbeKeep:1 into the item
//                            handler at X Y Z (a depot), or with "belt" onto the belt segment at X Y Z
//   tag X Y Z                stacks in the item handler at X Y Z (a depot): Boosted, Damage, name, enchantments, keys
// Output: "[zpwg9d] ..." lines in logs/latest.log, then "[zpwg9d] done".
(() => {
    const $Args = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $FR = Java.loadClass('net.minecraftforge.registries.ForgeRegistries')
    const $RL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $Pos = Java.loadClass('net.minecraft.core.BlockPos')
    const $Dir = Java.loadClass('net.minecraft.core.Direction')
    const $Hand = Java.loadClass('net.minecraft.world.InteractionHand')
    const $Fake = Java.loadClass('net.minecraftforge.common.util.FakePlayerFactory')
    const $Profile = Java.loadClass('com.mojang.authlib.GameProfile')
    const $UUID = Java.loadClass('java.util.UUID')
    const $Compound = Java.loadClass('net.minecraft.nbt.CompoundTag')
    const $ItemStack = Java.loadClass('net.minecraft.world.item.ItemStack')
    const $ItemEntity = Java.loadClass('net.minecraft.world.entity.item.ItemEntity')
    const $GameType = Java.loadClass('net.minecraft.world.level.GameType')
    const $Caps = Java.loadClass('net.minecraftforge.common.capabilities.ForgeCapabilities')
    const $Comp = Java.loadClass('net.minecraft.network.chat.Component')
    const $Ench = Java.loadClass('net.minecraft.world.item.enchantment.Enchantments')
    const $BEB = Java.loadClass('com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour')
    const $DBI = Java.loadClass('com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour')
    const $Applier = Java.loadClass('com.simibubi.create.foundation.recipe.RecipeApplier')
    const $RecipeWrapper = Java.loadClass('net.minecraftforge.items.wrapper.RecipeWrapper')
    const $ISH = Java.loadClass('net.minecraftforge.items.ItemStackHandler')
    const $ListTag = Java.loadClass('net.minecraft.nbt.ListTag')
    const $ART = Java.loadClass('com.simibubi.create.AllRecipeTypes')
    const PROFILE = new $Profile($UUID.fromString('5a7c0e57-0b0e-4a11-9d51-70e9a1c0f00d'), '[zpwg9d]')
    const PROFILE7 = new $Profile($UUID.fromString('5a7c0e57-0b0e-4a11-9d51-70e9a1c0ffee'), '[zpwg7]')
    const TOOLS = ['portable_drill', 'portable_saw', 'electrobaton', 'electrozapper']
    const P9D_RECIPES = ['mechanical_crafting/integrated_circuit', 'sequenced_assembly/electrical_gizmo',
        'deploying/portable_drill_boosting', 'deploying/portable_saw_boosting', 'deploying/electrobaton_boosting',
        'deploying/electrozapper_boosting']
    let PG = null
    function pg() {
        if (PG === null) PG = {
            Boost: Java.loadClass('org.patryk3211.powergrid.equipment.ItemBoostUtils'),
            Chip: Java.loadClass('org.patryk3211.powergrid.equipment.BoostingChipItem'),
            Recipe: Java.loadClass('org.patryk3211.powergrid.equipment.BoostRecipe'),
            Zap: Java.loadClass('org.patryk3211.powergrid.equipment.zapper.ZapProjectileEntity'),
            SeqItem: Java.loadClass('com.simibubi.create.content.processing.sequenced.SequencedAssemblyItem'),
            MechCraft: Java.loadClass('com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe'),
            SeqRecipe: Java.loadClass('com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe'),
        }
        return PG
    }
    function out(s) { console.info('[zpwg9d] ' + s) }
    // getClass() is filtered by KubeJS (r1113 pwg_p9b_probe.js): class names by instanceof of the expected classes
    function recipeClass(r) {
        if (r instanceof pg().Recipe) return 'BoostRecipe'
        if (r instanceof pg().MechCraft) return 'MechanicalCraftingRecipe'
        if (r instanceof pg().SeqRecipe) return 'SequencedAssemblyRecipe'
        let m = String(r).match(/([A-Za-z0-9_$]+)@[0-9a-f]+$/)
        return 'other(' + (m ? m[1] : String(r)) + ')'
    }
    // unbreaking level read from the Enchantments list (no Forge getEnchantmentLevel overload question in Rhino)
    function unbreaking(stack) {
        let t = stackTag(stack)
        if (!t.contains('Enchantments')) return 0
        let l = t.getList('Enchantments', 10)
        for (let i = 0; i < l.size(); i++) if (String(l.getCompound(i).getString('id')) == 'minecraft:unbreaking') return l.getCompound(i).getShort('lvl')
        return 0
    }
    function pos(x, y, z) { return new $Pos(x, y, z) }
    function rl(s) { return s.indexOf(':') >= 0 ? new $RL(s) : new $RL('powergrid', s) }
    function item(id) { return $FR.ITEMS.getValue(rl(id)) }
    function idOf(stack) { return String($FR.ITEMS.getKey(stack.getItem())) }
    function fake(level) { return $Fake.get(level, PROFILE) }
    function fake7(level) { return $Fake.get(level, PROFILE7) }
    function stackTag(stack) {
        let t = stack.save(new $Compound())
        return t.contains('tag') ? t.getCompound('tag') : new $Compound()
    }
    function boostLine(stack) {
        if (stack.isEmpty()) return 'empty'
        let t = stackTag(stack)
        let s = idOf(stack) + ' x' + stack.getCount() + ' Boosted=' + (t.contains('Boosted') ? t.getInt('Boosted') : 'none')
        if (stack.isDamageableItem()) s += ' Damage=' + stack.getDamageValue() + '/' + stack.getMaxDamage()
        if (stack.hasCustomHoverName()) s += ' name=' + stack.getHoverName().getString()
        s += ' unbreaking=' + unbreaking(stack)
        if (t.contains('ProbeKeep')) s += ' ProbeKeep=' + t.getInt('ProbeKeep')
        return s
    }
    function cap(level, P, which) {
        let be = level.getBlockEntity(P)
        if (be == null) return null
        let c = be.getCapability(which, null)
        if (c == null || !c.isPresent()) return null
        return c.resolve().get()
    }
    function recipe(level, id) {
        let o = level.getRecipeManager().byKey(rl(id))
        return o.isPresent() ? o.get() : null
    }
    function probeTool(id, damage) {
        let s = new $ItemStack(item(id), 1)
        s.setDamageValue(damage)
        s.setHoverName($Comp.literal('zpwg9d'))
        // KubeJS remaps ItemStack.enchant to its own copy-returning form (r1113 pwg16: unbreaking=0): write the NBT
        let en = new $Compound()
        en.putString('id', 'minecraft:unbreaking')
        en.putShort('lvl', 1)
        let el = new $ListTag()
        el.add(en)
        s.getOrCreateTag().put('Enchantments', el)
        s.getOrCreateTag().putInt('ProbeKeep', 1)
        return s
    }
    function recipeType(level, r) {
        let rm = level.getRecipeManager()
        let found = []
        let names = { DEPLOYING: 'create:deploying', MECHANICAL_CRAFTING: 'create:mechanical_crafting', SEQUENCED_ASSEMBLY: 'create:sequenced_assembly' }
        for (let k in names) {
            try { if (rm.getAllRecipesFor($ART[k].getType()).contains(r)) found.push(names[k]) } catch (err) { found.push(k + '?' + err) }
        }
        return found.length ? found.join('+') : 'other'
    }
    function check(level) {
        let chip = item('integrated_circuit'), inc = item('incomplete_electrical_gizmo')
        out('item integrated_circuit ' + (chip == null ? 'MISSING' : (chip instanceof pg().Chip ? 'BoostingChipItem' : 'other') + ' chip=' + (chip instanceof pg().Chip) + ' maxStack=' + chip.getMaxStackSize()))
        out('item incomplete_electrical_gizmo ' + (inc == null ? 'MISSING' : (inc instanceof pg().SeqItem ? 'SequencedAssemblyItem' : 'other') + ' seq=' + (inc instanceof pg().SeqItem)))
        out('item electrical_gizmo ' + (item('electrical_gizmo') == null ? 'MISSING' : 'ok'))
        let ser = $FR.RECIPE_SERIALIZERS.getValue(rl('boost_recipe'))
        out('serializer powergrid:boost_recipe ' + (ser == null ? 'MISSING' : 'ok ' + (ser === pg().Recipe.SERIALIZER)))
        P9D_RECIPES.forEach(id => {
            let r = recipe(level, id)
            if (r == null) { out('recipe ' + id + ' MISSING'); return }
            // KubeJS remaps getType() to the serializer id (r1113 pwg16): name the type whose recipe list holds r
            out('recipe ' + id + ' class=' + recipeClass(r) + ' type=' + recipeType(level, r) + ' serializer='
                + $FR.RECIPE_SERIALIZERS.getKey(r.getSerializer()) + ' result=' + idOf(r.getResultItem()))
        })
        let chipStack = new $ItemStack(chip, 1)
        TOOLS.forEach(t => {
            let s = new $ItemStack(item(t), 1)
            let r = pg().Boost.findRecipe(level, chipStack, s)
            let b = s.copy()
            pg().Boost.setBoosted(b, true)
            out('tool ' + t + ' maxDamage=' + s.getMaxDamage() + ' setBoosted -> Boosted=' + stackTag(b).getInt('Boosted')
                + ' isBoosted=' + pg().Boost.isBoosted(b) + ' findRecipe=' + (r == null ? 'none' : r.getId()))
        })
        let bd = new $ItemStack(item('portable_drill'), 1)
        pg().Boost.setBoosted(bd, true)
        let rb = pg().Boost.findRecipe(level, chipStack, bd)
        out('findRecipe boosted drill=' + (rb == null ? 'none' : rb.getId()))
        let rs = pg().Boost.findRecipe(level, chipStack, new $ItemStack(item('minecraft:stick'), 1))
        out('findRecipe stick=' + (rs == null ? 'none' : rs.getId()))
        let drillRecipe = recipe(level, 'deploying/portable_drill_boosting')
        if (drillRecipe != null) {
            let inv = new $RecipeWrapper(new $ISH(2))
            inv.setItem(0, bd)
            inv.setItem(1, chipStack.copy())
            // matches(RecipeWrapper, Level) has a bridge matches(Container, Level): call by signature
            let MATCH = 'matches(net.minecraftforge.items.wrapper.RecipeWrapper,net.minecraft.world.level.Level)'
            out('matches boosted drill=' + drillRecipe[MATCH](inv, level))
            inv.setItem(0, new $ItemStack(item('portable_drill'), 1))
            out('matches fresh drill=' + drillRecipe[MATCH](inv, level))
            let input = probeTool('portable_drill', 30)
            out('applier in ' + boostLine(input))
            // overloaded (ItemEntity / ItemStack, same arity): call by signature (as pwg_p9c_probe.js)
            let outs = $Applier['applyRecipeOn(net.minecraft.world.item.ItemStack,net.minecraft.world.item.crafting.Recipe)'](input, drillRecipe)
            out('applier outputs ' + outs.size())
            outs.forEach(o => out('applier out ' + boostLine(o)))
        }
    }
    function useChip(level, p) {
        let chip = p.getOffhandItem()
        let res
        try { res = String(chip.use(level, p, $Hand.OFF_HAND).getResult()) } catch (err) { res = 'error ' + err }
        out('use chip result=' + res + ' using=' + p.isUsingItem() + (p.isUsingItem() ? ' usedHand=' + p.getUsedItemHand() + ' remaining=' + p.getUseItemRemainingTicks() : ''))
        return p.isUsingItem()
    }
    function finishChip(level, p) {
        let chip = p.getOffhandItem()
        let rest
        // ItemStack.finishUsingItem(Level, LivingEntity): the three-argument form is Item's
        try { rest = chip.finishUsingItem(level, p) } catch (err) { out('finish error ' + err); rest = chip }
        p.stopUsingItem()
        out('finish main=' + boostLine(p.getMainHandItem()) + ' chips=' + p.getOffhandItem().getCount() + ' returned=' + (rest == null ? 'null' : rest.getCount()))
    }
    function prepHands(level, mainId) {
        let p = fake(level)
        p.setGameMode($GameType.SURVIVAL)
        p.stopUsingItem()
        p.getInventory().clearContent()
        p.setItemInHand($Hand.MAIN_HAND, new $ItemStack(item(mainId), 1))
        p.setItemInHand($Hand.OFF_HAND, new $ItemStack(item('integrated_circuit'), 2))
        out('hands main=' + boostLine(p.getMainHandItem()) + ' off=' + boostLine(p.getOffhandItem()))
        return p
    }
    function hand(level, id) {
        let p = prepHands(level, id)
        if (useChip(level, p)) finishChip(level, p)
        out('again')
        if (useChip(level, p)) finishChip(level, p)
    }
    function handswap(level) {
        let p = prepHands(level, 'portable_drill')
        if (!useChip(level, p)) return
        p.setItemInHand($Hand.MAIN_HAND, new $ItemStack(item('minecraft:diamond_sword'), 1))
        out('swapped main to ' + boostLine(p.getMainHandItem()))
        finishChip(level, p)
    }
    function zappass(level) {
        let p = prepHands(level, 'electrozapper')
        let zaps = () => level.getEntitiesOfClass(pg().Zap, p.getBoundingBox().inflate(8)).size()
        let n0 = zaps()
        let res = String(p.getMainHandItem().use(level, p, $Hand.MAIN_HAND).getResult())
        out('zappass unboosted use=' + res + ' projectiles ' + n0 + ' -> ' + zaps() + ' main=' + boostLine(p.getMainHandItem()))
        pg().Boost.setBoosted(p.getMainHandItem(), true)
        n0 = zaps()
        try { res = String(p.getMainHandItem().use(level, p, $Hand.MAIN_HAND).getResult()) } catch (err) { res = 'error ' + err }
        out('zappass boosted use=' + res + ' projectiles ' + n0 + ' -> ' + zaps() + ' main=' + boostLine(p.getMainHandItem()) + ' chips=' + p.getOffhandItem().getCount())
    }
    function boost(level, n) {
        let s = fake7(level).getMainHandItem()
        if (n == null) pg().Boost.setBoosted(s, true)
        else if (n <= 0) s.removeTagKey('Boosted')
        else s.getOrCreateTag().putInt('Boosted', n)
        out('boost [zpwg7] ' + boostLine(s))
    }
    function boosted(level) {
        let p = fake7(level)
        let lvl = 'n/a'
        try { lvl = p.powerGrid$drillSpeedMultiplier() } catch (err) { lvl = 'error ' + err }
        out('boosted [zpwg7] ' + boostLine(p.getMainHandItem()) + ' speedMult=' + lvl)
    }
    function drill0(level) {
        let p = fake7(level)
        try { p.powerGrid$receiveSpeed(0); out('drill0 speedMult=' + p.powerGrid$drillSpeedMultiplier()) } catch (err) { out('drill0 error ' + err) }
    }
    function tool(level, P, id, damage, belt) {
        let s = probeTool(id, damage)
        let rest = s.copy()
        if (belt) {
            let be = level.getBlockEntity(P)
            let dbi = be == null ? null : $BEB.get(be, $DBI.TYPE)
            if (dbi != null) rest = dbi.handleInsertion(s.copy(), $Dir.DOWN, false)
        } else {
            let h = cap(level, P, $Caps.ITEM_HANDLER)
            if (h != null) for (let i = 0; i < h.getSlots() && !rest.isEmpty(); i++) rest = h.insertItem(i, rest, false)
        }
        if (!rest.isEmpty()) {
            let e = new $ItemEntity(level, P.getX() + 0.5, P.getY() + 0.9, P.getZ() + 0.5, rest)
            e.setDeltaMovement(0, 0, 0)
            level.addFreshEntity(e)
            out('tool ' + boostLine(s) + ' REFUSED, dropped as an item entity')
        } else out('tool ' + boostLine(s) + ' into ' + (belt ? 'belt' : 'handler') + ' at ' + P.getX() + ' ' + P.getY() + ' ' + P.getZ())
    }
    function tag(level, P) {
        let h = cap(level, P, $Caps.ITEM_HANDLER)
        if (h == null) { out('tag: no item handler at ' + P); return }
        let parts = []
        for (let i = 0; i < h.getSlots(); i++) {
            let s = h.getStackInSlot(i)
            if (!s.isEmpty()) parts.push(i + ':' + boostLine(s) + ' keys=' + stackTag(s).getAllKeys())
        }
        out('tag ' + P.getX() + ' ' + P.getY() + ' ' + P.getZ() + ' ' + (parts.length ? parts.join(' | ') : 'none'))
    }
    function run(level, a) {
        let cmd = a[0]
        if (cmd == 'check') { check(level); return }
        if (cmd == 'fp') {
            let p = fake(level)
            p.setGameMode($GameType.SURVIVAL)
            let mx = parseFloat(a[1]) + 0.5, my = parseFloat(a[2]), mz = parseFloat(a[3]) + 0.5
            try { p.absMoveTo(mx, my, mz, 0, 0) } catch (e1) { p.teleportTo(mx, my, mz) }
            p.setOnGround(true)
            out('fp at ' + p.getX() + ' ' + p.getY() + ' ' + p.getZ())
            return
        }
        if (cmd == 'hand') { hand(level, a[1]); return }
        if (cmd == 'handswap') { handswap(level); return }
        if (cmd == 'zappass') { zappass(level); return }
        if (cmd == 'boost') { boost(level, a.length > 1 ? parseInt(a[1]) : null); return }
        if (cmd == 'boosted') { boosted(level); return }
        if (cmd == 'drill0') { drill0(level); return }
        let P = pos(parseInt(a[1]), parseInt(a[2]), parseInt(a[3]))
        if (cmd == 'tool') tool(level, P, a[4], parseInt(a[5]), a[6] == 'belt')
        else if (cmd == 'tag') tag(level, P)
        else out('unknown subcommand ' + cmd)
    }
    ServerEvents.commandRegistry(e => {
        const C = e.commands
        e.register(C.literal('zpwg9d').requires(s => s.hasPermission(2))
            .then(C.argument('args', $Args.greedyString()).executes(ctx => {
                try {
                    run(ctx.source.getLevel(), String($Args.getString(ctx, 'args')).split(' '))
                } catch (err) { out('error ' + err) }
                out('done')
                return 1
            })))
    })
})()
