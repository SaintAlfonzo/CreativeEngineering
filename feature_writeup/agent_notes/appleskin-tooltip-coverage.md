# appleskin-tooltip-coverage

> AppleSkin food bars: why screens lose them, and the fixes for every known food screen (meal slots, keg, FTB, AE2, Patchouli, Wares, TrashSlot, Pretty Pipes); status in feature_writeup/tooltips.md (TT-)

Moved from agent memory 2026-11-16 (last edited there 2026-10-05). Repo status docs win over any status line here.

AppleSkin 2.4.2 adds food bars only in RenderTooltipEvent.GatherComponents and needs event.getItemStack() non-empty. Forge 43 Screen passes the stack only via renderTooltip(ps, ItemStack, x, y), the Forge overloads taking an ItemStack, or a caller-set Screen.tooltipStack (private; AT needed, Sophisticated does this). renderComponentTooltip(List,x,y), renderTooltip(List,Optional,x,y), the Font-only overloads and FormattedCharSequence lists all send EMPTY or skip the event.

Audit 2026-10-05 (bytecode call-graph scan of mods/, then decompiled each hit). Unsupported, food-relevant: JEI 11.8.1 recipe slots + catalyst column; Lightman's Currency trade buttons/storage tabs; doapi recipe book (Let's Do: ghost slots + results); FTB Quests task/reward/description item hovers (FTB TooltipList draws text itself); Wares AgreementScreen; Pretty Pipes ItemTerminalWidget; AE2 crafting status/plan tables + pattern-encoding amount slots + guidebook; Patchouli GuiBook; PNC Amadron offers; TrashSlot slot. Not food-relevant: Etched album jukebox, ProjectRed RedUI ItemStackNode, CreativeCore config GUIs, Sophisticated stonecutter/chipped upgrade lists, AE2 stonecutting panel, exposureBackport ItemListScreen (our code).

**Fixed 2026-10-05** (patcherMod 1.0.5 build/libs):
- JEI: `JeiRecipeLayoutTooltipMixin` (RecipeLayout.drawOverlays) + `JeiRecipesGuiTooltipMixin` (static `lambda$render$6` = catalysts). Redirect slot.getTooltip() to stash getDisplayedIngredient(), redirect JeiTooltip.draw to setIngredient first. Side effect: recipe slots now also show tooltip images (bundles etc.), like the item list.
- LC: `LCEasyScreenHelperMixin` tags each line from EasyScreenHelper.getTooltipFromItem in `compat/LightmansTooltipStacks` (Guava weak identity map); `LCEasyGuiGraphicsMixin` HEAD-cancels EasyGuiGraphics.renderComponentTooltip(List,II) when a tagged line is present and uses Forge's overload with the stack. Known gap: a custom-named sale whose item tooltip is one line (name replaced) has no tagged line left.

**Re-audit 2026-10-20** (method-level javap scan, `getTooltipFromItem` via any owner + item-name lines drawn stackless). New gaps (all FIXED later 2026-10-20, see below):
- "Meal display slot" pattern (name + "Served on" via renderComponentTooltip(List,x,y)): FD CookingPotScreen.renderMealDisplayTooltip, Farmer's Respite KettleScreen.renderMealDisplayTooltip, CCK CookingGuideScreen + BrewingGuideScreen m_7025_ (MinersCookingGuide unreachable, no Miner's Delight), and our own OvenScreen.renderSlotTooltips (copied the pattern).
- Brewin port: KegScreen/KegRecipeBookComponent renderTankTooltip (drink fluid; map via BnCFluidItemDisplays). JEI FermentingRecipeCategory: with renderFluidInKeg=true the fluid tank slot is added before the RENDER_ONLY tankard slot at the same spot and JEI picks the first hovered slot, so the drink icon shows the fluid tooltip.
- Vinery port StationRecipeBook.renderTooltip result buttons (source edit: pass entry.result); ghost slots have no tooltip at all.
- TrashSlotGuiHandler.onScreenDrawn confirmed (renderComponentTooltip(List)).
Checked fine: vanilla recipe book (Forge passes stack), Polymorph, Sol food book UIItemStack, CFB recipe book, Create blueprint, all JEI categories (none draw items outside slots; Create GuiGameElement = blocks).

**Fixed 2026-10-20** (status: tooltips.md TT-19..30, fixes_compat 2.24):
- `compat/MealSlotTooltip` + Redirects of `m_96597_` (owner = the screen class itself) in CookingPotScreen/KettleScreen.renderMealDisplayTooltip and CCK Cooking/BrewingGuideScreen.m_7025_. OvenScreen + Vinery StationRecipeBook + Brewin KegScreen/KegRecipeBookComponent: direct source edits (Forge overload with stack; keg passes BnCFluidItemDisplays item).
- JEI keg: JeiRecipeLayoutTooltipMixin now, when the hovered slot shows no item, attaches a RENDER_ONLY item slot under the same mouse point (JEI keeps slots in add order for both draw and hover, so reordering would hide the tankard). Side effect of the JEI fix: recipe slots now get JEI's mod-name line (ModNameFormat override active).
- TrashSlotGuiHandlerMixin redirect; PrettyPipesTerminalTooltipMixin; WaresAgreementTooltipMixin; PatchouliBookTooltipMixin (ordinal 0).
- FTB: `FtbTooltipStacks` remembered from GuiHelper.addStackTooltip / ItemTask.addMouseOverHeader / QuestDescriptionField.addMouseOverText (ItemStack.m_41651_ redirect); ScreenWrapper.m_6305_ + MenuScreenWrapper.m_7027_ redirect TooltipList.render -> Forge renderComponentTooltip(..., font, stack); forget at HEAD.
- AE2: `AeTooltipStacks` pending set before AbstractTableRenderer.render's drawTooltipWithHeader and PatternEncodingTermScreen.m_7025_'s drawTooltip; AEBaseScreen.drawTooltip HEAD draws with stack. Guidebook ItemTooltip.getLines returns ForgeHooksClient.gatherTooltipComponents.
- New compile libs in patcherMod/libs: CCK, PrettyPipes, FTB library + quests, Wares, Patchouli, AE2.

**Why:** user asked for the audit, then for JEI + LC fixes. Fix pattern for the others: same idea, see [patchermod-coremod](patchermod-coremod.md).
**How to apply:** if the JEI jar changes from 11.8.1, re-check the lambda name `lambda$render$6`.
