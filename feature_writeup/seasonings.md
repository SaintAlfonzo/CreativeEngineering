# Seasonings and sauces: catalog

Written 2026-10-31. Every seasoning and sauce in the pack today, the ones Barbeque's Delight (BBQ) adds, and the one
planned (`grilled`). Nothing here is built beyond what the Now columns say. Mechanism docs: [food.md](food.md) 3.12 (pack
framework), [barbeques_delight.md](barbeques_delight.md) 3.3 and 4.1 (BBQ and the fold design), brief
[food_seasoning.md](briefs/food_seasoning.md) (Salt). TODO rows: `food/seasoning #3`, `#11`, `#12`, `#13`.

**Type rule used here.** *Sauce*: what the source calls a sauce, and anything that rewrites the food's existing effects.
*Seasoning*: everything else (adds a bonus or a flag). Pack seasonings are all seasonings; BBQ's three powders are
seasonings, its three sauces are sauces.

## 1. Catalog (current state)

**Fold built 2026-11-03** (barbeques_delight.md 4.5; in the Creative Delight jar installed 2026-11-03 00:08, not booted; SQ-BBQF-01 to -07): the six BBQ rows below are pack seasonings now (`barbequesdelight:<name>` in `seasonings.js`), put on by a
shapeless craft that consumes one item; seasoning items stack to 64; the cap is `[food] maxSeasonings` (default 2) in
creativedelight-common.toml; `grilled` is built as a station seasoning; the butcher trades are back (12 emeralds for 64
powders or 16 sauces, level 1, 4 uses). The "System now" and "Obtained now" text of the BBQ rows reads as of that build.

"Now" = what the pack does today for pack and Salt rows, and what the upstream jar does for BBQ rows (the port is nested in the
Creative Delight jar in `mods/` as `backportedBarbeques-1.3.0-ce-19.2-01`, upstream behaviour unchanged; the fold of section 4 is not built; deep scan 2026-11-02).
Mutex = may not share a food with. Complementary: **live** = defined in the pack's `combines`; **proposed** = pairing
suggested for the fold (4), not decided.

| Seasoning | Type | Used on now | System now | Effect | Mutex now | Complementary | Obtained now |
|---|---|---|---|---|---|---|---|
| Salt | seasoning | every food in `#salt:can_be_salted` | shapeless special craft `salt:salting`, 1 salt consumed; own `Salted` NBT, separate from the pack framework | +2 nutrition, saturation modifier +0.1 (config `salt-common.toml`), capped | none (separate NBT; combines with any pack seasoning) | any (live) | Salt items (all salt merged to Vintage pink, `food/seasoning #6`); Create: The Salt mixes |
| Cheese | seasoning | popcorn | shapeless craft `season/cheese`, 1 cheese wedge consumed | +2 saturation | onion, garlic, sugar, void pepper | none | any cheese wedge |
| Onion | seasoning | popcorn | shapeless craft, 1 onion consumed | +2 nutrition | cheese, sugar, void pepper | garlic (live) | onion crop |
| Garlic | seasoning | popcorn | shapeless craft, 1 garlic consumed | Speed I, 10 s | cheese, sugar, void pepper | onion (live) | garlic crop |
| Sugar | seasoning | nothing yet (no foods) | shapeless craft, 1 sugar or powdered sugar consumed | +1 nutrition (placeholder) | all others | none | sugar, powdered sugar |
| Void Pepper | seasoning | popcorn (only with Ender's Delight Additions) | shapeless craft, 1 Crushed Void Pepper consumed | +2 nutrition, +0.4 saturation | all others | none | Crushed Void Pepper (Ender's Delight End crop) |
| Cumin Powder (BBQ) | seasoning | BBQ skewers (raw and cooked) | shapeless craft, 1 consumed | Heals 2 health (one heart) on eating | every seasoning without a pair (no pairs defined) | buffalo sauce (proposed) | butcher level 1: 64 for 12 emeralds |
| Pepper Powder (BBQ) | seasoning | BBQ skewers (raw and cooked) | shapeless craft, 1 consumed | Fast eating | every seasoning without a pair | honey mustard sauce (proposed) | butcher level 1: 64 for 12 emeralds |
| Chili Powder (BBQ) | seasoning | BBQ skewers (raw and cooked) | shapeless craft, 1 consumed | Always edible; 2 fire damage on eating | every seasoning without a pair | barbeque sauce (proposed) | butcher level 1: 64 for 12 emeralds |
| Honey Mustard Sauce (BBQ) | sauce | cooked BBQ skewers; changes nothing but the name on skewers without effects | shapeless craft, 1 consumed | Each effect: chance x2, duration /2 | every seasoning without a pair | pepper powder (proposed) | butcher level 1: 16 for 12 emeralds |
| Buffalo Sauce (BBQ) | sauce | cooked BBQ skewers; name only on skewers without effects | shapeless craft, 1 consumed | Each effect: amplifier +1, duration /2; 2 fire damage on eating | every seasoning without a pair | cumin powder (proposed: heal 2 offsets the fire damage) | butcher level 1: 16 for 12 emeralds |
| Barbeque Sauce (BBQ) | sauce | cooked BBQ skewers; name only on skewers without effects | shapeless craft, 1 consumed | Each effect: duration x2 | every seasoning without a pair | chili powder (proposed) | butcher level 1: 16 for 12 emeralds |
| Grilled (built 2026-11-03, `food/seasoning #13`) | seasoning (state) | raw BBQ skewers and the raw versions of the pack skewers, as they cook on the Grill | the Grill station, **not** the shapeless cooked recipe; does not count toward the cap | +2 saturation (pack points, capped at the food's hunger; read from "+2 shanks saturation", decision 3 in barbeques_delight.md 4.3) | none | any | cooking on the Grill |

Notes:
- "One per skewer" for BBQ was the upstream rule; since the fold the pack cap (2) and `combines` decide, and no pairs are defined among the six yet, so it is still one BBQ seasoning per skewer (plus `grilled`). Cooked skewers read plain "Beef Skewer" (O34, built 2026-11-03); prefixes Salted, Garlic, Cumin Flavored, Peppered, Chili Flavored and the three sauce prefixes.
- Seasoning is lost when a skewer burns (B5, kept).
- The pack framework has no `use count`: every pack seasoning is consumed one item per craft. **After the fold BBQ seasonings work the same:** a craft consumes one item, and the crafts and trades that give them give several items, scaled to about the old 64 or 16 uses (ratio: barbeques_delight.md 4.3 #1). Before the fold each application costs 1 durability.
- BBQ skewers: **powders may go on a raw skewer and copy over to the cooked one when it is grilled; the three sauces may not go on a raw skewer** (decided 2026-10-31; the port refuses them, upstream allowed them).
- A grilled cooked skewer and a crafted one are the same item but carry different seasonings (`grilled` or not), so they do not stack.
- Pack seasonings (cheese, onion, garlic, ...) are not on skewers (their families lists are empty), and BBQ seasonings are not on popcorn. The BBQ cooked skewers join the skewer family at the next `/dumpall` + `food_families` run (2026-11-03).
- Salt on BBQ skewers follows the family membership (saltable tag is family-driven), so it opens after the next family run.

## 2. Cap on seasonings per food (decided 2026-10-31)

- Pack-wide cap on the number of seasonings on one food, configurable, **default 2**. Built 2026-11-03 as
  `[food] maxSeasonings` in **creativedelight-common.toml** (not CEConfig: the seasoning framework is Creative Delight's since the carve).
- `grilled` **does not count** toward the cap. That is configurable too: proposed `[food] grilledCountsTowardMax`,
  default `false`. Built 2026-11-03 in the same `[food]` section; on, the Grill also skips `grilled` on a full skewer.
- Salt's `Salted` is a separate tag and does not count (unchanged).
- The `combines` allowlists still decide which pairs are legal; the cap is an extra upper limit.
- The keys appear with their defaults when Forge corrects the file; a hand edit goes to `configs_edited/root_config/` too.

## 3. Skewers and the BBQ system (decided 2026-10-31)

- The BBQ system stays: the Ingredients Basin makes **raw skewers**, the Grill turns them into **grilled skewers**,
  seasonings go on top.
- The skewers of other mods (the 8 pack skewers) are **added to the BBQ system**: each gets a **raw version** (new item), the Grill
  cooks it into the existing item, and BBQ seasonings apply to them.
- **Raw skewers still exist**: edible, **worse food** than the cooked version and **sometimes poisonous**, and they are
  still grilled into the cooked version. Only chicken takes the "nasty" (hazard-style) downside; other raw
  meats are worse food but not nasty. Values and poison chance: `food/types-and-tuning #19`.
- Raw skewers can be smelted, smoked or campfire-cooked into the cooked skewer without `grilled` (`dishes/meals #49`,
  built 2026-11-03; seasonings on the raw skewer are lost on those routes, vanilla cooking keeps no NBT). Cooked skewers can also be crafted from cooked
  ingredients by the skewer family rule (shaped plus one sequenced assembly), and those do **not** get `grilled`
  (`dishes/meals #46`).

## 4. Proposed complementary pairs (fold, not decided)

| Pair | Why |
|---|---|
| Cumin + buffalo sauce | cumin heals 2 and buffalo deals 2 fire damage, net zero, and the sauce still raises the amplifier |
| Pepper + honey mustard sauce | fast eating, and effects with double the chance |
| Chili + barbeque sauce | always edible, and the doubled duration is worth eating at full hunger |

Under a cap of 2 each pair is a legal full set. Proposed rule: BBQ sauces exclude each other (their transforms conflict:
honey mustard halves duration, barbeque doubles it), powders pair with one sauce. Everything else in `combines` is
left to the owner at the fold.

## 5. Open items

- Source for the six BBQ seasonings: butcher trades restored 2026-11-03 (`food/seasoning #18`); crafting recipes pinned (`#17`); the all-seasonings trade pass stays `#12`.
- Raw skewer food values and poison chance: `food/types-and-tuning #19`.
- Items per old durable item: 64 / 16 (O31), used by the trades.
- The cooked-ingredient sequenced assembly per line (the shaped craft is built, 2026-11-03).

## 6. Owner notes (scratchpad 2026-11-04)

Checked against the code 2026-11-04. "Built" items are in the CD jar installed 2026-11-04 19:19 (barbeques_delight.md 3.10,
3.11, 4.6) unless noted; several notes were probably written while playing an older jar.

| Note | State | Row |
|---|---|---|
| Seasoned icon position per item or family (several on one food overlap) | Planned | [TODO](todo/TODO.md) `food/seasoning #9` |
| Clearer off-hand slot (JEI) | Built: off-hand sprite left of the slot | `dishes/meals #62` |
| Grill raw meat for the grilled effect; burnt meat item copied from the burnt skewer | Planned | `food/seasoning #22` |
| Take 1 item from the Ingredients Basin with a full hand | Built: sneak grab | `dishes/meals #62` |
| Several ingredient types per skewer (cabbage leaf types, potato types) | Built: skewering input tags | `dishes/meals #62` |
| Any item in the off hand or basin | Planned (inputs are tag-limited) | `dishes/meals #61` |
| JEI tab needs a name | Built ("Grilling", "Skewering") | `food/seasoning #21` |
| Cumin, pepper and chili powder retextured as dusts | Planned | `textures/food #11` |
| Seasonings missing from the trade JEI | Built: missing only on a dedicated server, filled by trade sync | `food/seasoning #21` |
| Sauced icon missing: new shape, salt icon as fallback | In Progress: salt placeholder built, own shape planned | `food/seasoning #20` |
| Sauces on more meat-like foods (whitelist) | Planned (sauces: cooked BBQ skewers only, 1) | `food/seasoning #23` |
| Keep the Tray; Tray and Ingredients Basin wood variants | Built: 9 vanilla woods each (O39) | `dishes/meals #62`, `largecontent/mod-additions #38` |
| WTHIT "Burnt!!" to "Burnt" | Built (en_us; uk_ua and zh_cn keep "!!") | `food/seasoning #21` |
| Grilled icon in JEI grilling | Built | `dishes/meals #62` |
| Effect chances used more in skewers | Planned | `food/types-and-tuning #19` |
| Grilled icon or line missing | In Progress: the line is built (`food/seasoning #13`), the icon is the salt placeholder | `food/seasoning #20` |
