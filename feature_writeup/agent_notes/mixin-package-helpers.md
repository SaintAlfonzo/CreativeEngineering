# Helper classes in a mixin package crash at load

Mixin owns every class under the `package` of a `*.mixins.json` (sub-packages such as `.mixin.client` included). Any
class there that is loaded as a normal class (a nested helper, record, enum, static holder, anonymous class, or a
top-level helper not listed in the config) throws `IllegalClassLoadError: <class> is in a defined mixin package ...
owned by <config> and cannot be referenced directly` the first time the mixed-in code touches it. A nested class
compiles to `Outer$Inner`, so `@Unique private static final class Frame` inside a mixin is enough to crash.

Put helpers in a normal package of the same mod (for example the mod root) and reference them from the mixin.
Lambdas and method references are fine (they become synthetic methods merged into the target). The config `plugin`
class must also live outside the mixin package.

Hits so far: Creative Labels crafting helper (2026-10-21, moved to `crafting/`), Creative Oxidation
`BlockItemMixin$Frame` (2026-11-25, client crash at startup, moved to `PlaceFrame` in the member root). Check a built jar
with `unzip -Z1 <member jar> | grep 'mixin/.*\$'` (must print nothing).
