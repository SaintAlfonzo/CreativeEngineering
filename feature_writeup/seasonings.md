# Seasonings and sauces: catalog

Written 2026-10-31. Every seasoning and sauce in the pack today, the ones Barbeque's Delight (BBQ) adds, and the one
planned (`grilled`). Nothing here is built beyond what the Now columns say. Mechanism docs: [food.md](food.md) 3.12 (pack
framework), [barbeques_delight.md](barbeques_delight.md) 3.3 and 4.1 (BBQ and the fold design), brief
[food_seasoning.md](briefs/food_seasoning.md) (Salt). TODO rows: `food/seasoning #3`, `#11`, `#12`, `#13`.

**Type rule used here.** *Sauce*: what the source calls a sauce, and anything that rewrites the food's existing effects.
*Seasoning*: everything else (adds a bonus or a flag). Pack seasonings are all seasonings; BBQ's three powders are
seasonings, its three sauces are sauces.

## 1. Catalog (current state)

"Now" = what the pack does today for pack and Salt rows, and what the upstream jar does for BBQ rows (not installed yet).
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
| Cumin Powder (BBQ) | seasoning | BBQ skewers (raw and grilled) | click on a skewer on the grill or cutting board; **64 uses**, 1 per application | Heals 2 health (one heart) on eating | all other BBQ seasonings (one per skewer) | buffalo sauce (proposed) | none: butcher trades dropped, owner adds a temporary source (`food/seasoning #12`) |
| Pepper Powder (BBQ) | seasoning | BBQ skewers | click; **64 uses** | Fast eating | all other BBQ seasonings | honey mustard sauce (proposed) | none (as above) |
| Chili Powder (BBQ) | seasoning | BBQ skewers | click; **64 uses** | Always edible; 2 fire damage on eating | all other BBQ seasonings | barbeque sauce (proposed) | none (as above) |
| Honey Mustard Sauce (BBQ) | sauce | BBQ skewers; changes nothing but the name on skewers without effects | click; **16 uses** | Each effect: chance x2, duration /2 | all other BBQ seasonings | pepper powder (proposed) | none (as above) |
| Buffalo Sauce (BBQ) | sauce | BBQ skewers; name only on skewers without effects | click; **16 uses** | Each effect: amplifier +1, duration /2; 2 fire damage on eating | all other BBQ seasonings | cumin powder (proposed: heal 2 offsets the fire damage) | none (as above) |
| Barbeque Sauce (BBQ) | sauce | BBQ skewers; name only on skewers without effects | click; **16 uses** | Each effect: duration x2 | all other BBQ seasonings | chili powder (proposed) | none (as above) |
| Grilled (planned, `food/seasoning #13`) | seasoning (state) | raw BBQ skewers and the raw versions of the pack skewers, as they cook on the Grill | the Grill station, **not** the shapeless cooked recipe; does not count toward the cap | +2 saturation (pack points, capped at the food's hunger; read from "+2 shanks saturation", decision 3 in barbeques_delight.md 4.3) | none | any | cooking on the Grill |

Notes:
- "One per skewer" for BBQ is the upstream rule and holds until the fold. Names read "Cooked" now, not "Grilled" (barbeques_delight.md 3.9). Skewers carry a BBQ name prefix ("Chili Flavored ...").
- Seasoning is lost when a skewer burns (B5, kept).
- The pack framework has no `use count`: every pack seasoning is consumed one item per craft. **After the fold BBQ seasonings work the same:** a craft consumes one item, and the crafts and trades that give them give several items, scaled to about the old 64 or 16 uses (ratio: barbeques_delight.md 4.3 #1). Before the fold each application costs 1 durability.
- BBQ skewers: **powders may go on a raw skewer and copy over to the cooked one when it is grilled; the three sauces may not go on a raw skewer** (decided 2026-10-31; the port refuses them, upstream allowed them).
- A grilled cooked skewer and a crafted one are the same item but carry different seasonings (`grilled` or not), so they do not stack.
- Pack seasonings are not on skewers (skewers are not in the skewer family yet), and BBQ seasonings are not on popcorn.
- Salt on BBQ skewers is deferred with the family fold (the saltable tag is family-driven).

## 2. Cap on seasonings per food (decided 2026-10-31)

- Pack-wide cap on the number of seasonings on one food, configurable, **default 2** (proposed CEConfig key
  `[food] maxSeasonings`). Replaces BBQ's one per skewer at the fold.
- `grilled` **does not count** toward the cap. That is configurable too: proposed `[food] grilledCountsTowardMax`,
  default `false`.
- Salt's `Salted` is a separate tag and does not count (unchanged).
- The `combines` allowlists still decide which pairs are legal; the cap is an extra upper limit.
- Both config files are hand-edited: back them up under `configs_edited/` when added.

## 3. Skewers and the BBQ system (decided 2026-10-31)

- The BBQ system stays: the Ingredients Basin makes **raw skewers**, the Grill turns them into **grilled skewers**,
  seasonings go on top.
- The skewers of other mods (the 8 pack skewers) are **added to the BBQ system**: each gets a **raw version** (new item), the Grill
  cooks it into the existing item, and BBQ seasonings apply to them.
- **Raw skewers still exist**: edible, **worse food** than the cooked version and **sometimes poisonous**, and they are
  still grilled into the cooked version. Only chicken takes the "nasty" (hazard-style) downside; other raw
  meats are worse food but not nasty. Values and poison chance: `food/types-and-tuning #19`.
- Whether raw skewers can be smelted or smoked into a cooked skewer without `grilled` is a noted open decision
  (`dishes/meals #49`); until then only the Grill cooks them. Cooked skewers can also be crafted from cooked
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

- Temporary source for the six BBQ seasonings: owner (`food/seasoning #12`).
- Raw skewer food values and poison chance: `food/types-and-tuning #19`.
- Items per old durable item after the fold (barbeques_delight.md 4.3 #1).
- The cooked-ingredient assembly recipes per line (4.3 #8).
