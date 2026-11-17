# create-config-string-widget

> SubMenuConfigScreenMixin + StringEntry - patches a text-field widget into Create's Configs GUI for raw ConfigValue<String> fields, which Create itself can't render

Moved from agent memory 2026-11-16 (last edited there 2026-09-15). Repo status docs win over any status line here.

Built 2026-09-15, follow-up to [config-driven-recipe-values](config-driven-recipe-values.md). Create's own Configs screen
(`SubMenuConfigScreen`, reachable from the pause menu) only has widgets for Boolean/Enum/Number
`ForgeConfigSpec` values - a raw `ConfigValue<String>` (like `CEConfig.CRAFTING.FELSIC_SWORD.potion`)
fell through to a hardcoded `"Impl missing - {class} {name} : {value}"` placeholder row. Confirmed
by decompiling `create-1.19.2-0.5.1.i.jar` with ForgeFlower (cached at
`~/.gradle/caches/forge_gradle/maven_downloader/net/minecraftforge/forgeflower/1.5.605.9/forgeflower-1.5.605.9.jar`
- genuinely useful for reading any mod jar's real logic instead of guessing from javap signatures alone).

**The fix** (2 new files in patcherMod, both client-only):
- `mixin/SubMenuConfigScreenMixin.java` - `@Inject`s into `SubMenuConfigScreen`'s private
  `lambda$init$12(String, Object)` (the `configGroup.valueMap().forEach` dispatch lambda), right
  before its fallback `NEW ConfigScreenList$LabeledEntry` (`@At("NEW")`, `cancellable = true`).
  Captures `humanKey`/`configValue`/`valueSpec`/`value` via `locals = LocalCapture.CAPTURE_FAILEXCEPTION`
  (must be a **prefix** of the live locals in bytecode order - can truncate at the end, can't skip
  the middle). For a String value, builds a `StringEntry` and adds it via a `@Shadow`ed `list` field,
  then `ci.cancel()`.
- `compat/StringEntry.java` - the missing widget itself, modeled on Create's own `NumberEntry`
  (same `ValueEntry<T>` base + `ConfigTextField`), just without the numeric parsing/bounds code.

**The naming trap that cost a rebuild cycle**: this project's `libs/*.jar` (Create, IE, etc.) are
raw `compileOnly fileTree` jars - never deobfuscated, so their classes keep whatever names their
OWN build produced. Empirically (via ForgeFlower + javap on the actual jar), Create's `ValueEntry`
overrides the vanilla `AbstractSelectionList.Entry#render(...)` under the name `m_6311_`, not
`render` - because this project's `minecraft` FG dependency (`mapped_official_1.19.2`) resolves
vanilla classes to **friendly Mojang names at dev time** (confirmed: `Minecraft.getInstance()`,
`EditBox.setValue()`, etc. all compile fine directly), while Create's shipped jar was built/reobbed
independently and kept the raw official-but-unnamed placeholder for this one method. Net rule that
actually matters for writing new code against these `libs/*.jar` classes:
- Calling into **pure vanilla/Forge API** (from the `minecraft` dependency) → use friendly names
  (`getInstance()`, `setValue()`, `children()`, `x`/`y` fields, etc.) - reobfuscation will rewrite
  the call site to the matching SRG name automatically for the shipped jar.
- Calling into a **third-party mod class's own override of a vanilla method** via `super.xxx()`
  (e.g. `ValueEntry`/`LabeledEntry`) → must use that mod's actual raw compiled name directly (found
  via decompiling their jar), since there's no friendly-named method there to resolve against.
- Declaring **your own override** of a vanilla method (like `StringEntry#render`) → use the
  friendly name in source; ForgeGradle's `reobfJar` renames it to the matching SRG name in the
  final jar automatically, so it correctly overrides/gets called by the third-party class's
  identically-SRG-named implementation at runtime. Verified end to end: compiled
  `StringEntry.render(...)` → confirmed via javap on `build/libs/*.jar` (post-`reobfJar`) that it
  became `m_6311_(...)`, and every vanilla EditBox/Minecraft/AbstractWidget call inside it was
  correctly rewritten too (`setValue`→`m_94144_`, `getInstance`→`m_91087_`, `children`→`m_6702_`,
  etc.) - exact same names Create's own compiled classes use.
- Mixin's own string-based targets (`method = "..."`, `@At(target = "...")`) stay in raw/SRG form
  regardless, per the existing `remap = false` convention (see build.gradle's comment on this) -
  confirmed reobf leaves those strings untouched.

Verified by actually running `./gradlew compileJava`, `jar`, and `reobfJar --offline` in this
session (not just read-through) - compiles clean, packages both classes + the mixin registration,
and reobfuscates correctly.

**Real in-game crash found 2026-09-15 (first actual playtest)**: editing the potion field caused
a `StackOverflowError` ("mouseClicked event handler"), and the user separately noticed
`creativeengineering:magic_resistance` (37 chars) looked cut off in the box. Both symptoms trace
to one root cause in vanilla `EditBox` (decompiled to confirm, `net/minecraft/client/gui/
components/EditBox.java`): its default `maxLength` is 32, and `setValue(String)` is
```java
public void setValue(String p_94145_) {
   if (this.filter.test(p_94145_)) {
      this.value = p_94145_.length() > this.maxLength ? p_94145_.substring(0, this.maxLength) : p_94145_;
      this.moveCursorToEnd();
      this.setHighlightPos(this.cursorPos);
      this.onValueChange(p_94145_);   // <- fires the responder with the ORIGINAL arg, not this.value!
   }
}
```
So past 32 chars, the *stored* value gets truncated but the *responder* still receives the full,
untruncated string - a real vanilla quirk, not something `StringEntry` did wrong on its own.
`StringEntry.onValueChange`'s "does the field already display this?" guard
(`!textField.getValue().equals(newValue)`) compared the truncated stored value against the
untruncated argument the responder handed back around, found a permanent mismatch, and called
`setValue()` again - which truncates again, fires the responder with the full string again,
forever: `setValue → truncate → responder(full) → setValue(full) → mismatch → setValue → ...`.
`NumberEntry` never hits this because numeric text never approaches 32 characters, so the
truncation branch is simply never taken there.

**Fix**: `this.textField.setMaxLength(256)` in `StringEntry`'s constructor, before the initial
`setValue()` call - keeps every realistic config string (item/potion/entity ids) under the
truncation branch entirely, so the truncated-vs-original divergence never happens. Confirmed via
javap on the rebuilt jar that `sipush 256` / the `setMaxLength` call is present post-reobf.
Rebuilt, reobf'd, and redeployed to `mods/` in this session (checked no Minecraft process was
running first). **Still not confirmed by an actual playtest** - next step is retrying the same
edit in-game.
