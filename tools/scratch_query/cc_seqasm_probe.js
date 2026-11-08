// Scratch-only probe (never in the pack): copy into a scratch server's kubejs/server_scripts as zz_cc_seqasm_probe.js.
// SQ-CPP-02 / SQ-CC rows: walks a Create sequenced assembly recipe on an enchanted, renamed input the way a deployer /
// press does it (SequencedAssemblyRecipe.getRecipe(level, stack, type, class) returns the current step's recipe, whose
// forced result is the private advance(stack)), and prints the NBT of every step and of the final result. Checks that
// Creative Engineering's SequencedAssemblyRecipeMixin carries Enchantments and display through.
//   /zsa <recipe id>     e.g. /zsa creativeequipment:createsequencedassembly/echo_shovel_sequenced_assembly
// Output: "[zsa] ..." lines, then "[zsa] done". Notes: KubeJS's ItemStack.enchant returns a copy; Rhino sees no
// advance() (private) and no Mojmap getItems/getTag here, hence getStacks / getNbt.
(() => {
    const $ZSAArgs = Java.loadClass('com.mojang.brigadier.arguments.StringArgumentType')
    const $ZSARL = Java.loadClass('net.minecraft.resources.ResourceLocation')
    const $ZSAComp = Java.loadClass('net.minecraft.network.chat.Component')
    const $ZSAEnch = Java.loadClass('net.minecraft.world.item.enchantment.Enchantments')
    const $ZSASeq = Java.loadClass('com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe')
    const $ZSAProc = Java.loadClass('com.simibubi.create.content.processing.recipe.ProcessingRecipe')
    function out(s) { console.info('[zsa] ' + s) }
    function run(server, id) {
        try {
            let opt = server.getRecipeManager().byKey(new $ZSARL(id))
            if (!opt.isPresent()) { out('error no recipe ' + id); return }
            let r = opt.get()
            let level = server.overworld()
            let st = r.getIngredient().getStacks().toList().get(0).copy()
            st = st.enchant($ZSAEnch.UNBREAKING, 3)
            st.setHoverName($ZSAComp.literal('Probe Gear'))
            out('input ' + st.getId() + ' ' + st.getNbt())
            let types = []
            r.getSequence().forEach(s => { let t = s.getRecipe().getType(); if (types.indexOf(t) < 0) types.push(t) })
            let steps = 0
            while (steps < 64) {
                let step = null
                for (let t of types) {
                    let o = $ZSASeq.getRecipe(level, st, t, $ZSAProc)
                    if (o.isPresent()) { step = o.get(); break }
                }
                if (step == null) { out('no step recipe for ' + st.getId() + ' ' + st.getNbt()); break }
                st = step.rollResults().get(0)
                steps++
                out('step ' + steps + ' ' + st.getId() + ' ' + st.getNbt())
                if (String(st.getId()) != String(r.getTransitionalItem().getId())) break
            }
            let tag = String(st.getNbt())
            out('result ' + st.getId() + ' enchantments=' + (tag.indexOf('Enchantments') >= 0) + ' name=' +
                (tag.indexOf('Probe Gear') >= 0) + ' steps=' + steps)
        } catch (err) { out('error ' + err) }
        out('done')
    }
    // /zsa bottle <potion id>: IE Bottling Machine lookup, glass bottle + REGULAR potion fluid; /zsa refine <potion id>:
    //   IE Refinery lookup, SPLASH potion fluid + the first registered *dragon_breath fluid (SQ-CPP-03 without building
    //   the multiblocks); /zsa selftest: every IE recipe with a FluidTagInput against its own first matching fluid.
    // Fluids go into Java calls as KubeJS objects (Fluid.of(id, amount, nbt)): KubeJS converts every FluidStack a
    // script passes to a Java method, and a raw FluidStack does not survive that (fl.isFluidStackIdentical(fl) is
    // false), which once made every IE fluid recipe look broken (r1107cc).
    function zsaFluid(fs) { return Fluid.of(String($ZSAFR().getKey(fs.getFluid())), fs.getAmount(), fs.hasTag() ? String(fs.getTag()) : undefined) }
    function $ZSAFR() { return Java.loadClass('net.minecraftforge.registries.ForgeRegistries').FLUIDS }
    function potionCheck(server, what, pid) {
        try {
            let level = server.overworld()
            let nbt = b => '{Bottle:"' + b + '",Potion:"' + pid + '"}'
            if (what == 'selftest') {
                let stats = {}, fails = []
                server.getRecipeManager().getRecipes().forEach(r => {
                    let ins = []
                    try { if (r.fluidInput) ins.push(r.fluidInput) } catch (err) { }
                    try { if (r.input0) { ins.push(r.input0); ins.push(r.input1) } } catch (err) { }
                    try { if (r.fluidInputs) for (let k = 0; k < r.fluidInputs.length; k++) ins.push(r.fluidInputs[k]) } catch (err) { }
                    ins.forEach(fi => {
                        if (fi == null) return
                        let ms
                        try { ms = fi.getMatchingFluidStacks() } catch (err) { return }
                        if (ms.size() == 0) return
                        let m = ms.get(0)
                        let key = String(r.getType()) + (m.hasTag() ? ' nbt' : ' plain')
                        let ok = fi.testIgnoringAmount(zsaFluid(m))
                        stats[key] = stats[key] || [0, 0]
                        stats[key][ok ? 0 : 1]++
                        if (!ok && fails.length < 6) fails.push(r.getId() + ' ' + m.getTag())
                    })
                })
                for (let k in stats) out('selftest ' + k + ' pass ' + stats[k][0] + ' fail ' + stats[k][1])
                fails.forEach(f => out('selftest fail ' + f))
            } else if (what == 'bottle') {
                let BMR = Java.loadClass('blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe')
                let r = BMR.findRecipe(level, [Item.of('minecraft:glass_bottle')], Fluid.of('create:potion', 1000, nbt('REGULAR')))
                out('bottle recipe ' + (r == null ? 'none' : r.getId() + ' -> ' + r.output.get().get(0) + ' ' + r.output.get().get(0).getNbt()))
            } else {
                let RR = Java.loadClass('blusunrize.immersiveengineering.api.crafting.RefineryRecipe')
                let splash = Fluid.of('create:potion', 1500, nbt('SPLASH'))
                let db = null
                $ZSAFR().getValues().forEach(f => {
                    let k = String($ZSAFR().getKey(f))
                    if (db == null && k.indexOf('dragon_breath') >= 0 && k.indexOf('flowing') < 0) db = k
                })
                if (db == null) { out('refine: no dragon breath fluid'); return }
                let r = RR.findRecipe(level, splash, Fluid.of(db, 250), Item.of('minecraft:air'))
                if (r == null) r = RR.findRecipe(level, Fluid.of(db, 250), splash, Item.of('minecraft:air'))
                out('refine ' + pid + ' splash + ' + db + ' -> ' + (r == null ? 'none' : r.getId() + ' -> ' + $ZSAFR().getKey(r.output.getFluid()) + ' ' + r.output.getTag()))
            }
        } catch (err) { out('error ' + err) }
        out('done')
    }
    // /zsa potionat <x> <y> <z>: what PotionUtils reads for every potion-bearing stack in the overworld container at
    // x y z (SQ-CC-09: an old saved Potion id is remapped on read by Common's PotionByNameRemapMixin, the NBT keeps it)
    function potionAt(server, arg) {
        try {
            let p = arg.split(' ').map(n => parseInt(n))
            let PU = Java.loadClass('net.minecraft.world.item.alchemy.PotionUtils')
            let PR = Java.loadClass('net.minecraftforge.registries.ForgeRegistries').POTIONS
            let be = server.overworld().getBlockEntity(new BlockPos(p[0], p[1], p[2]))
            if (be == null) { out('potionat no block entity'); return }
            for (let k = 0; k < be.getContainerSize(); k++) {
                let st = be.getItem(k)
                if (st.isEmpty()) continue
                let pot = PU.getPotion(st)
                let effs = []
                PU.getMobEffects(st).forEach(m => effs.push(String(m.getEffect().getDescriptionId()) + ' ' + m.getDuration()))
                out('potionat slot ' + k + ' ' + st.getId() + ' nbt ' + st.getNbt() + ' reads ' + PR.getKey(pot) + ' effects ' + effs.join(', '))
            }
        } catch (err) { out('error ' + err) }
        out('done')
    }
    ServerEvents.commandRegistry(e => {
        const { commands: C } = e
        e.register(C.literal('zsa').then(C.literal('potionat').then(C.argument('p', $ZSAArgs.greedyString())
            .executes(ctx => { potionAt(ctx.source.server, $ZSAArgs.getString(ctx, 'p')); return 1 }))))
        e.register(C.literal('zsa').then(C.literal('selftest')
            .executes(ctx => { potionCheck(ctx.source.server, 'selftest', 'minecraft:water'); return 1 })))
        e.register(C.literal('zsa').then(C.literal('bottle').then(C.argument('p', $ZSAArgs.greedyString())
            .executes(ctx => { potionCheck(ctx.source.server, 'bottle', $ZSAArgs.getString(ctx, 'p')); return 1 }))))
        e.register(C.literal('zsa').then(C.literal('refine').then(C.argument('p', $ZSAArgs.greedyString())
            .executes(ctx => { potionCheck(ctx.source.server, 'refine', $ZSAArgs.getString(ctx, 'p')); return 1 }))))
        e.register(C.literal('zsa')
            .then(C.argument('id', $ZSAArgs.greedyString())
                .executes(ctx => { run(ctx.source.server, $ZSAArgs.getString(ctx, 'id')); return 1 })))
    })
})()
