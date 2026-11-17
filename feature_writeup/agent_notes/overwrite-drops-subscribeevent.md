# overwrite-drops-subscribeevent

> Mixin @Overwrite does not keep the target method's annotations, so overwriting a @SubscribeEvent handler silently unsubscribes it

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

Mixin 0.8.x replaces the target method on `@Overwrite` and merges annotations only for fields and shadow methods. Overwriting a `@SubscribeEvent` handler silently unsubscribes it unless the mixin method repeats `@SubscribeEvent`. `@Inject` keeps the original method and is not affected.

**Why:** the Visceral Heap particle overwrite lost it; no particle providers registered and the client crashed at load with `Redundant texture list for particle ...`.

**How to apply:** when a load crash shows 40+ `channel already registered` errors, those come from Minecraft's recovery reload re-running common setup. Read the first `Caught error loading resourcepacks` instead. See memory `forge-1192-port-api-gotchas`.
