# Brief: gear / echo

Written 2026-10-21 from [TODO.md](../todo/TODO.md) `# gear`, Sub `echo`. Reference writeups:
[gear_tuning.md](../gear_tuning.md) 2.5 (echo tools), [armor_effects.md](../armor_effects.md) 2.3 (on-hit table), 2.4
(echo armor bonus), F5 / F7. Code: [echo_tools.js](../../kubejs/startup_scripts/items/echo_tools.js),
[tool_effects.js](../../kubejs/startup_scripts/func/tool_effects.js) (registrations 109-133),
[server_scripts/magic/tool_effects.js](../../kubejs/server_scripts/magic/tool_effects.js),
[tools/echo_texture.py](../../tools/echo_texture.py), textures
`kubejs/assets/creativeengineering/textures/item/echo_{pickaxe,axe,shovel,hoe}.png(.mcmeta)`.

Both rows cite "scratchpad 2026-10-19" and memory `echo-tier-gear`; the source line (git `2e8687f`,
`change_scratchpad.md`) is the whole spec: "change echo tool potion buffs to be better, but simpler than dragon. give
echo tools human generated textures (only non human textures in pack)". The memory note isn't in the repo.

## Scope and order

Open rows: #1, #2 (both Planned). No Testing / Complete rows in this subfamily.

- **#1 first** (decide the buffs; KubeJS for the four pack tools, a small patcherMod mixin if the CDD echo sword
  joins). Restart for tooltips, nothing else.
- **#2 any time** (art; the pipeline exists). Independent of #1.

Related rows: `potions/effects #5` (F5 main-hand keying, F7 echo Phantom Bane level) and `potions/effects #6` (dragon
bonuses; echo must stay below them). `gear/tuning #12` makes the echo assembly configurable but not the effects.

---

## echo #1: Echo tool buffs
Status Planned / Work KubeJS / `gear/echo #1`

### Goal
The echo tools' effects are worth having, clearly below dragon's, and simple: one idea per tool, one table for all
five echo tools including the sword.

### Now
- Pack echo tools (`creativeengineering:echo_pickaxe/axe/shovel/hoe`): `global.toolEffects.onHit(i.echo_*, echoHit)`
  (tool_effects.js 117-133), `echoHit` = Weakness II 100 t + Darkness II 100 t to the target. No held effect.
- CDD echo sword (`create_deep_dark:echo_sword`): **not** in the framework. CDD's own
  `create_deep_dark.procedures.EchoSwordEffectProcedure` (verified in `create_deep_dark-1.7.0`): a
  `LivingAttackEvent` subscriber, `private static execute(Event, Entity, Entity)`; if the attacker's main-hand item is
  the echo sword it adds Weakness (100 t, amp 1) and Darkness (100 t, amp 1). Tooltip lines are three
  `list.add(Component.literal(...))` calls in `EchoSwordItem#m_7373_` ("§7On hit, inflicts to target:",
  "§7- §cWeakness II §7(5 sec.)", "§7- §cDarkness II §7(5 sec.)").
- **Darkness does nothing to mobs** (it's a client fog effect on the player who has it; from memory of 1.19.2, no
  attribute modifier). So against mobs the echo tools only apply Weakness II (-8 melee damage). That's the "better"
  in the row.
- Dragon for comparison (armor_effects.md 2.3): two on-hit effects plus one held effect per tool, mixed mod effects
  (Exsanguination, Soul Cross, Trippy...).
- Framework limits: `held()` durations ignored (F3), on-hit keyed to the main hand only (F5).

### Build
1. Decide the table (D1). Recommended, one effect idea per tool, vanilla effects only (that's the "simpler"):

| Tool | On hit (target) | Held (wielder) |
|---|---|---|
| Echo sword | Weakness II 5 s, Slowness II 3 s | |
| Echo axe | Weakness II 5 s, Slowness II 3 s | |
| Echo pickaxe | | Haste I |
| Echo shovel | | Haste I |
| Echo hoe | Weakness II 5 s | |

   Dragon pickaxe is planned at Haste (`potions/effects #6`); make it Haste II there so echo Haste I stays below it.
2. [tool_effects.js](../../kubejs/startup_scripts/func/tool_effects.js) 117-133: replace `echoHit` with the new
   specs (`{ effect: 'minecraft:slowness', amplifier: 1, duration: 60 }` etc.), add
   `global.toolEffects.held(i.echo_pickaxe, { effect: 'minecraft:haste', amplifier: 0, duration: 50 })`.
3. Echo sword into the framework (D2):
   - patcherMod mixin `EchoSwordEffectProcedureMixin`, `@Mixin(value = EchoSwordEffectProcedure.class, remap = false)`,
     `@Inject(method = "execute(Lnet/minecraftforge/eventbus/api/Event;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)` → `ci.cancel()`.
   - Tooltip: `@Redirect` the three `List.add` calls in `EchoSwordItem#m_7373_` (`remap = false` for the class,
     the method is SRG `m_7373_`) to no-ops, so the framework's tooltip (client_scripts/tool_effects_tooltip.js) is
     the only one. Pattern: `EchoArmorTooltipMixin` / `EchoArmorEffectProcedureMixin`.
   - `global.toolEffects.onHit(i.echo_sword, echoHit)`. Dict key `i.echo_sword` exists.
   - Register the mixin in `creativeengineering.mixins.json` (next to `EchoArmorTooltipMixin`, line 35).
4. armor_effects.md 2.3 table and AE-13 / AE-14; tooltips check.

Alternative to step 3 without Java: leave the sword on CDD's Weakness + Darkness and only change the four pack tools
(inconsistent sword vs axe).

### Ids and data
Effects `minecraft:weakness`, `minecraft:slowness`, `minecraft:haste`. Items `i.echo_sword`
(`create_deep_dark:echo_sword`), `i.echo_pickaxe` etc. (`creativeengineering:`).

### Decisions
- D1 the buffs: the table above (**recommended**), or keep Weakness II and swap Darkness for Slowness only.
- D2 echo sword joins the framework: **yes** (one table, one tooltip style; costs one small mixin).

### Gotchas
- CDD's procedure fires on `LivingAttackEvent` (before shields / invulnerability); the framework fires on
  `EntityEvents.hurt`. After D2 the sword stops applying effects to a blocking target. Fine.
- Held Haste on two tools stacks to Haste I only (same effect, same amplifier), and conflicts with nothing in the
  echo armor bonus (Night Vision + Phantom Bane).
- Startup script: restart, not `/reload`, for the tooltip side; the server handler reads the same data.

### Tests
- AE-13 (update): echo axe on a zombie → Weakness II + Slowness II; no Darkness.
- AE-25: echo pickaxe in either hand → Haste I icon; tooltip "While held: Haste I (infinite)".
- AE-26 (after D2): echo sword tooltip shows only the framework lines (no duplicate "On hit" header); hit a zombie →
  the new effects, no Darkness.

---

## echo #2: Human-made echo textures
Status Planned / Work resource pack / `gear/echo #2`

### Goal
Hand-drawn sprites for the four echo tools, keeping the CDD-style animated glow.

### Now
- `echo_pickaxe/axe/shovel/hoe.png` are 16x192 (12 frames) with `.png.mcmeta` (`frametime 2`, frames 11 → 0), made by
  `tools/echo_texture.py starter` (a recolour of vanilla diamond tools) then `animate`. In git since the initial
  commit (`609cd21`). They live in KubeJS assets, not a resource pack.
- The echo sword is CDD's own sprite (human-made). The echo shield sprites
  (`textures/entity/shield/echo_shield_base[_nopattern].png`) are not covered by this row.
- `echo_texture.py animate <frame0.png>` takes a 16x16 drawing; pixels painted in the 12 `RAMP` colours (lines 45-49)
  crawl, `OUTLINE` (53) and `HANDLE` (54) stay static. Needs Pillow.

### Build
1. Draw four 16x16 frame-0 sprites (pickaxe, axe, shovel, hoe) in the echo sword's style: dark outline, sculk-teal
   head, the glow in exact `RAMP` colours. Use CDD's `echo_sword.png` frame 0 as the reference (from the CDD jar,
   `assets/create_deep_dark/textures/item/echo_sword.png`).
2. Save each as `kubejs/assets/creativeengineering/textures/item/echo_<tool>.png` (16x16), run
   `python3 tools/echo_texture.py animate kubejs/assets/creativeengineering/textures/item/echo_<tool>.png` for each; it
   writes the 16x192 strip and the `.mcmeta` in place.
3. F3+T in game (KubeJS assets reload with resource packs), check the animation.

### Decisions
- None. (Optional: the echo shield sprites too, if they were also generated; check with the user.)

### Gotchas
- Any colour that is not exactly a `RAMP` entry is static; anti-aliased glow pixels won't animate.
- The TODO Work column says "resource pack"; the files are KubeJS assets.

### Tests
- GR-04 (existing): JEI shows the four tools with animated textures.

---

## Writeup corrections made

None (gear_tuning.md and armor_effects.md describe the echo tools correctly).
