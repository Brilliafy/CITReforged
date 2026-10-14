# CIT Reforged

![CIT Reforged](logo.png)

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.12.2-success)
![Modloader](https://img.shields.io/badge/Modloader-Forge%20%7C%20Cleanroom-orange)
![License](https://img.shields.io/badge/License-MIT-blue)

An optimized, maintained continuation of **CIT Recrafted**: a standalone Custom Item Textures (CIT)
engine for Minecraft 1.12.2.

CIT Reforged reads OptiFine and MCPatcher CIT resource packs directly and renders them without
OptiFine installed, so you can keep vanilla-style item rendering on a modern performance setup. It
works with resource packs shipped inside other mods, supports NBT, durability, stack size,
enchantment and pattern conditions, and adds caching so the extended rule set does not cost frames.

## Credits

CIT Reforged is a continuation, not a rewrite. The original engine was built by **EclipsedReaper**:

- Original project: [CIT Recrafted](https://github.com/EclipsedReaper/CIT-Recrafted) by EclipsedReaper, MIT licensed.
- Continuation, packaging and fixes: Brilliafy.

The MIT license and the original copyright notice are preserved in [LICENSE](LICENSE).

## What CIT is

CIT is a feature introduced by OptiFine (and earlier by MCPatcher) that lets resource pack authors
give items custom textures based on properties such as the item name, its damage value, its
enchantments, or arbitrary NBT. Many packs rely on it to reskin tools, armor, bows and named gear.
Because OptiFine no longer fits well next to modern optimization mods, CIT Reforged provides a
standalone implementation for 1.12.2.

## Features

- Loads OptiFine and MCPatcher CIT rules from selected resource packs, including packs bundled inside
  other mods.
- Matches items by registry name, numeric id, regex or wildcard pattern.
- Conditions on damage (with ranges, percentages and damage masks), stack size, enchantments,
  enchantment levels and arbitrary NBT, including list wildcards.
- Supports `texture`, `model`, bow pulling stages, and fishing rod cast states.
- Per-model LRU rule cache: a stack that was already matched does not re-match its rule every frame.
- Baked model cache: identical texture/model combinations are baked once and reused, including bow
  and fishing rod sub-models.
- Pack-root CIT folders, `.mcmeta` animation files and OptiFine emissive `_e` textures are proxied
  correctly.

## Supported CIT rules

<details>
<summary><b>Click to see all supported CIT properties</b></summary>

- `type`: (Required) Currently only the `item` type is supported.
- `items` / `matchItems`: (Required) The target item id(s). Unlike OptiFine, this engine also accepts
  `regex:`, `iregex:`, `pattern:` and `ipattern:` forms.
- `nbt.*`: Matches NBT data.
- `damage`: Matches damage. Supports values, ranges and percentages.
- `damageMask`: Masks the damage value before matching.
- `enchantments` / `enchantmentIDs`: Matches enchantments by name or numeric id, including enchanted
  books.
- `enchantmentLevels`: Matches enchantment levels. Supports ranges.
- `stackSize`: Matches the number of items in the stack. Supports ranges.
- `texture` / `tile`: Texture to use for the item. Can be a full path or a bare file name.
- `model`: Model to use for the item.
- `texture.bow_pulling_0/1/2`, `texture.fishing_rod_cast`: Bow and fishing rod states.
- `weight`: Priority relative to other matching rules.

See the [OptiFine CIT documentation](https://optifine.readthedocs.io/cit.html) for the original
property reference.

</details>

## Requirements

- Minecraft 1.12.2
- Minecraft Forge 14.23.5.x or Cleanroom 0.6.x
- Java 8

CIT is a client-side rendering feature. Install CIT Reforged on the client. It is not needed on a
dedicated server.

## Installation

1. Install Minecraft Forge or Cleanroom for 1.12.2.
2. Put `cit_reforged-1.0.0.jar` in your `mods` folder.
3. Put a CIT capable resource pack in your `resourcepacks` folder and enable it in game.

## Building

```bash
./gradlew build
```

The build targets Java 8 bytecode. Because the sources use modern Java syntax through Jabel, a newer
JDK (17 or later) is required to compile. The resulting jar is written to `build/libs`.

## License

MIT. Copyright (c) 2026 CIT Recrafted (EclipsedReaper) and Brilliafy. See the [LICENSE](LICENSE)
file.
