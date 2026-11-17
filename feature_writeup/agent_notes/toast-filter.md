# toast-filter

> Tutorial + advancement toasts off pack-wide via patcherMod ToastFilterMixin (CEConfig [toasts]); Rubidium Extra toggles are local-only

Moved from agent memory 2026-11-16 (last edited there unknown). Repo status docs win over any status line here.

2026-10-19 the user asked to disable tutorial and advancement toasts. `mixin/ToastFilterMixin` (client) cancels `ToastComponent.addToast` (`m_94922_`) per CEConfig `[toasts] tutorial / advancement` (both default false). Tutorial = vanilla TutorialToast + Exposure `BetterTutorialToast` + Fairy Lights `ClippyController$Balloon`; advancement = AdvancementToast + Patchouli `LexiconToast` (matched by class name, no compile deps). Built into 1.0.9 jar in build/libs 10-19 (not bumped, not yet copied to mods/: game was running).

**Why:** Rubidium Extra already had `advancement_toast`/`tutorial_toast` false in config/sodium-extra-options.json, but build_deploy.py classes that file `player`, so other clients never got it, and it misses mod tutorial toasts.

**How to apply:** to hide another toast family, add a set + toggle there. Other toast classes in the pack: vanilla RecipeToast/SystemToast, AE2 FinishedJobToast, CC ItemToast, FTB Quests CustomToast/RewardToast/ToastQuestObject, FTB Library SimpleToast, KubeJS NotificationToast. Writeup: feature_writeup/toasts.md (TO-01..07); the user kept KubeJS, DABR, FTB, Quests, WTHIT, CC, AE2 toasts on. Related: [no-advancements](no-advancements.md), [patchermod-coremod](patchermod-coremod.md).
