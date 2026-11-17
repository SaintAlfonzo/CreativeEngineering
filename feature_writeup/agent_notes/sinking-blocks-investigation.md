# sinking-blocks-investigation

> Soul-sand-style sinking for soft/dust/spongy/very_sticky - user rejected hitbox changes; purely visual camera + model offset (Creative Common CameraSinkMixin/EntitySinkRenderMixin + #creativeengineering:sinking); status in feature_writeup/blockrunner.md

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Full reference and tests BR-22…26 and BR-30…33: `feature_writeup/blockrunner.md` §3.4. Since 2026-09-27, full framed/copycat blocks sink like their newest camo (`SinkingRender.depthAt` -> `CamoSpeed.camo`), gated by `[surface.sinking] copyCamo` (default true, independent of `[surface.camouflage] copyCamo`; both moved from `[block_sinking]`/`[camouflage_speed]` on 2026-09-27). User rule: camo sinking only on frames whose own shape is a full block (isCollisionShapeFullBlock, same as any sinking block), and no tooltip mentions it. Built, but still not in mods/ as of 2026-09-27 12:43 (the game was open for every build; the user noticed it missing in game).

**Decision (owner, 2026-09-26):** no collision/hitbox changes — visual only. Soul sand's real sink is its 14/16 collision box (support/visual shape stay full), and copying that would break iron golem spawns on sand, drowned beach AI, shulker attaching, etc. (entityCanStandOn callers). So that route was dropped.

**Built:** `surface/SinkingRender` + client mixins `CameraSinkMixin` and `EntitySinkRenderMixin`. The user cares most about the first-person camera behaving exactly like a real sink.
- The depth is computed geometrically at the entity's previous-tick position (xo/yo/zo) and current position, then lerped by partialTick. This eases in and out over one tick exactly like a real position change (landing, stepping on or off). The rule: feet within 1e-3 of the top face of a full-collision block in `#creativeengineering:sinking`. onGround isn't used, since there's no old-tick value for it. Uses getRootVehicle, so riders sink with their mount.
- Camera: `@Mixin(value = Camera.class, remap = false)`, HEAD of setup (m_90575_) stashes the entity and partialTick; `@ModifyArg` index 1 on the single `setPosition(DDD)` (m_90584_) lowers eye Y *before* the third-person pull-back, so every view matches a real sink.
- Model: `EntityRenderDispatcher#render` (m_114384_): `@ModifyVariable` STORE ordinal 0 lowers the renderer's `vec3` offset (only Vec3 local). Model and name tag move; shadow, fire and F3+B hitbox don't.
- Config: CEConfig `[surface.sinking] depthPixels` (default 2, 0 = off; was `[block_sinking]`). Tag: nested `#blockrunner:surface/{soft,dust,spongy,very_sticky}` in blocktags.js. Honey (15/16) and mud (14/16) are skipped as non-full; they already sink for real.
- Not done: the crosshair pick ray still starts at the true eye, 2px above the camera. This only matters on block edges; lowering it would mean touching getEyePosition/pick.
- Related: [blockrunner-speed-tiers](blockrunner-speed-tiers.md), [patchermod-coremod](patchermod-coremod.md).
