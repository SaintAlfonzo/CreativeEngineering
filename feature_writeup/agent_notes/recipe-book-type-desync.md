# recipe-book-type-desync

> Server-join kick \"DecoderException readerIndex(18)+length(1) exceeds writerIndex\" = ClientboundRecipePacket RecipeBookType count mismatch; Nock Enough Arrows creates \"fletching\" only client-side; fixed by Creative Common compat/NockRecipeBookSync

Moved from agent memory 2026-11-16 (last edited there 2026-09-26). Repo status docs win over any status line here.

2026-09-26: after joining CreEngServer the client dropped with "Internal Exception: DecoderException: IndexOutOfBoundsException: readerIndex(18) + length(1) exceeds writerIndex(18)". The stack trace is only in the client's **debug.log** (search for "Failed to sent packet"), not latest.log. It pointed to `ClientboundRecipePacket.<init>` -> `RecipeBookSettings.read`, which reads two booleans per `RecipeBookType.values()`.

Cause: Nock Enough Arrows calls `RecipeBookType.create("fletching")` in the static init of `events.RecipeBookEvents`, which is `@EventBusSubscriber(Dist.CLIENT)`. The client has 8 types (vanilla 4 + FERMENTING/BAKING/COOKING from Brewin'/Clayworks/Farmer's Delight + fletching), while the dedicated server has 7. The server can't simply load that class, because its lambdas are typed on the client-only `RecipeBookCategories`.

Fix: Creative Common `compat/NockRecipeBookSync.register()` (commonSetup enqueueWork) creates "fletching" on DEDICATED_SERVER only, guarded by a check on the name.

**How to apply:** any new mod that calls `RecipeBookType.create` in client-only code will cause the same kick. Scan jars for the descriptor `(Ljava/lang/String;)Lnet/minecraft/world/inventory/RecipeBookType;` and check whether each call site runs on the server.

Related: [creengserver-dedicated-server](creengserver-dedicated-server.md), [patchermod-coremod](patchermod-coremod.md), memory `debugging-with-latest-log`.
