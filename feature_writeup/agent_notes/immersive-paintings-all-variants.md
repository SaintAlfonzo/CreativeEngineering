# immersive-paintings-all-variants

> Every painting variant on Immersive Paintings' Datapacks page; modded ones built at load by Creative Compat (since 2026-11-05), vanilla from gen_immersive_paintings.py

Moved from agent memory 2026-11-16 (last edited there 2026-11-05). Repo status docs win over any status line here.

Mechanics (state lives in feature_writeup/immersive_paintings.md):
- Immersive's PaintingsLoader lists `paintings/**.png` through the server ResourceManager plus a sibling json {name, author, width, height (blocks), resolution}; it keys each painting by the full png id. Placed paintings store that id (entity Motive only; the item has none). Since 2026-11-10 the ids are `creativecompat:paintings/<ns>/<id>.png` (PaintingArt.OUT_NAMESPACE); Compat's ImmersivePaintingMotiveAliasMixin moves old `creativeengineering:` motives on entity load, so the namespace may change again only with such an alias.
- Modded (since 2026-11-05, licence fix): Compat `paintings/PaintingArt` (pure Java, runs in a javac+gson harness against mods/*.jar and kubejs/exported/paintings.json) + `PaintingPack` (hidden SERVER_DATA memory pack via shared `packs` BuiltInPacks). Sources: mod jars by file name, then `[paintings] extraSources` (can override art, can't add variants). Server side, so client resource packs don't reach modded paintings.
- Vanilla 30: `tools/gen_immersive_paintings.py` -> kubejs/data/creativecompat/paintings/minecraft/ (dedicated server has no vanilla textures; Mojang art never in a jar).
- Rules shared by both: upscale nearest to 32 px/block (the Datapacks page opens filtered to 32), animated strip = first frame, off-grid stretched like vanilla; authors from lang .author/.artist, DarkPaintings section credits, else mod displayName.
- Cycle Paintings (vanilla entities) and Immersive's own cycling are separate; leave Immersive's alone.

Related: memory `feature-writeup-pattern`, [cdevgen-generator-runner](cdevgen-generator-runner.md), [dumpall-refresh-workflow](dumpall-refresh-workflow.md), [kubejs-jar-startup-scripts](kubejs-jar-startup-scripts.md).
