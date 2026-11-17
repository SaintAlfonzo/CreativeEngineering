# gui-title-plaque-stretch

> Engineers GUIs title plaque stretched at runtime for long container titles (compat/guititle + 2 client mixins, CEConfig [gui_title]); status in feature_writeup/fixes_compat.md

Moved from agent memory 2026-11-16 (last edited there 2026-10-05). Repo status docs win over any status line here.

Built 2026-10-05 into the patcherMod 1.0.5 jar (no version bump).

- **The problem:** resource pack `Engineers_GUIs_1.18.2_v.01.0.zip` paints a fixed plaque (fill about 67 px wide, 4 px copper caps) behind container titles. Long titles ran past it, e.g. Quark "Large Trapped Crimson Chest", shulker colors, Everything Copper minecarts, horse names.
- **compat/guititle/TitlePlaqueLayout:** pure Java, no MC classes. It detects the plaque from the texture pixels at row titleY+3 (light warm fill, cap ends at a dark outline) and plans tile + end-cap blits. Titles wider than the GUI get maxTextWidth and are truncated with "...".
- **compat/guititle/TitlePlaques:** runtime. It caches per texture and clears the cache on resource reload. The overflow log is `kubejs/exported/gui_title_overflow.json`.
- **Mixins:**
  - `RenderSystemShaderTextureMixin` (HEAD `m_157456_`) captures the first texture bound in renderBg.
  - `ContainerTitlePlaqueMixin`: @Inject before and after `m_7286_` in `m_6305_`, plus @ModifyArg (text, x) on ordinal-0 `Font.m_92889_` in `m_7027_`.
- **Scope:** only screens that do not override renderLabels. MerchantScreen draws its title elsewhere, and villager2 has a second plaque that would false-match.
- **Verified:**
  - Headless harness against the real textures, with rendered previews.
  - A throwaway client boot (memory `client-launch-for-defaults`): handlers merged into AbstractContainerScreen and RenderSystem with no errors.
- **Why:** the user wanted long titles to stop running into the plaque outline, and wanted to find where it happens.
- **How to apply:**
  - Install the jar.
  - Open renamed or long-named chests, shulkers and furnaces.
  - Check the overflow JSON.
  - Toggle with [gui_title] stretchPlaque / logOverflow.

See [patchermod-coremod](patchermod-coremod.md).
