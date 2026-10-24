# CIT Reforged

![CIT Reforged](logo.png)

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.12.2-success?style=for-the-badge)
![Modloader](https://img.shields.io/badge/Modloader-Forge%20%7C%20Cleanroom-orange?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

**CIT Reforged** is an optimized, standalone **Custom Item Textures (CIT)** engine for **Minecraft 1.12.2** (Forge and CleanroomMC). It renders OptiFine and MCPatcher CIT resource packs natively **without OptiFine installed**.

In modern 1.12.2 performance environments (running mods like CleanroomMC, CensoredASM, VintageFix, Phosphor, or FoamFix), OptiFine frequently causes visual glitches, shader conflicts, or severe crashes. **CIT Reforged** serves as a direct drop-in replacement that brings complete CIT resource pack support to your game with zero rendering overhead.

---

## ⚖️ Differences Between Fork (CIT-Reforged) and Original (CIT-Recrafted)

**CIT Reforged** is an optimized continuation of [CIT-Recrafted](https://github.com/EclipsedReaper/CIT-Recrafted) by EclipsedReaper. While the original proved the concept of standalone 1.12.2 CIT loading, it suffered from severe rendering GC stutter, missing OptiFine features, and pack compatibility bugs. 

CIT Reforged addresses these limitations through a comprehensive architectural overhaul:

### 1. Zero-Allocation Rendering Architecture (No GC Micro-Stutter)
- **Original (`CIT-Recrafted`):** Created a new `ItemCacheKey` instance and performed a full deep-copy (`stack.getTagCompound().copy()`) of every item's entire NBT compound on **every single frame** for **every item rendered**. In busy inventories, hotbars, or modpacks with item frames, this produced millions of short-lived objects and continuous garbage collection lag spikes.
- **Fork (`CIT-Reforged`):** Implements a recyclable `ThreadLocal<ItemCacheKey.LookupKey>` that resets in-place without memory allocation and without cloning NBT. Combined with precomputed hash codes and an LRU rule cache (`ModelCache(500)`), rule matching runs in constant $O(1)$ time with **zero heap churn**.

### 2. Full Dynamic Bow & Fishing Rod State Support
- **Original (`CIT-Recrafted`):** Contained zero support for animated sub-models or item states. Custom bows and fishing rods were locked to static models.
- **Fork (`CIT-Reforged`):** Adds complete support for dynamic item states:
  - **Bow Pulling Stages:** Evaluates player draw duration in real time to render `texture.bow_pulling_0/1/2` and `model.bow_pulling_0/1/2` (along with `bow_standby`).
  - **Fishing Rod Cast State:** Checks active player hook entities to dynamically swap between `texture.fishing_rod_cast` and uncast models.
  - **Perspective Preservation:** Introduces `PerspectiveModelWrapper` so dynamic model overrides maintain accurate First-Person, Third-Person, GUI, and Ground transformations.

### 3. Enchanted Book Compatibility (`StoredEnchantments`)
- **Original (`CIT-Recrafted`):** Only checked the standard `ench` NBT tag. Because Minecraft stores enchanted book enchantments under `StoredEnchantments`, all enchantment-based rules on books failed completely.
- **Fork (`CIT-Reforged`):** Automatically checks both `ench` (standard gear) and `StoredEnchantments` (enchanted books), restoring full pack functionality for custom enchanted book textures.

### 4. Emissive Textures (`_e.png`) and Animated Metadata (`.mcmeta`)
- **Original (`CIT-Recrafted`):** Skipped `.mcmeta` animation registration and ignored OptiFine emissive texture conventions.
- **Fork (`CIT-Reforged`):** Fully registers and proxies `.mcmeta` texture animation descriptors, and automatically registers OptiFine emissive `_e.png` textures so glowing details work seamlessly alongside modern shaders.

### 5. Universal Pack Loading & Path Resolution
- **Original (`CIT-Recrafted`):** Failed to load packs that placed `optifine/cit` or `mcpatcher/cit` directly at the root of the ZIP file (a common pattern in older MCPatcher packs).
- **Fork (`CIT-Reforged`):** Discovers rules at both the ZIP root and under `assets/minecraft/`. Path resolution handles `~/` expansions, relative paths (`./`), and parent directory traversals (`../`).

### 6. Model Deduplication Cache
- **Original (`CIT-Recrafted`):** Repeatedly baked separate `IBakedModel` instances for duplicate texture/model declarations across rules.
- **Fork (`CIT-Reforged`):** Shares a unified model bake cache (`bakedCache`) across all rules, significantly cutting model baking times, memory usage, and VRAM overhead during game launch.

---

## 📊 Comparison Matrix

| Feature / Subsystem | Vanilla / OptiFine 1.12.2 | Original CIT-Recrafted | CIT Reforged (This Fork) |
| :--- | :---: | :---: | :---: |
| **Standalone (No OptiFine Required)** | ❌ No | ✅ Yes | ✅ **Yes** |
| **CleanroomMC & Modern Modpack Support** | ❌ Incompatible / Crash-prone | ✅ Yes | ✅ **Flawless (Forge + Cleanroom)** |
| **Per-Frame Heap Allocation** | Low | ❌ High (Clones NBT every frame) | ⚡ **Zero-Allocation (ThreadLocal)** |
| **Rule Matching Evaluation** | Linear scan | Linear scan per frame | ⚡ **$O(1)$ LRU Rule Cache** |
| **Bow Pulling Animations (0/1/2)** | ✅ Yes | ❌ Not supported | ✅ **Full 3-stage pulling support** |
| **Fishing Rod Cast State** | ✅ Yes | ❌ Not supported | ✅ **Dynamic cast check** |
| **Enchanted Books (`StoredEnchantments`)** | ✅ Yes | ❌ Broken | ✅ **Fully Supported** |
| **Emissive Textures (`_e.png`)** | ✅ Yes | ❌ Ignored | ✅ **Registered & Proxied** |
| **Animated Textures (`.mcmeta`)** | ✅ Yes | ⚠️ Partial | ✅ **Full support** |
| **Root-Level ZIP Pack Discovery** | ✅ Yes | ❌ Broken | ✅ **Fixed & Supported** |
| **Model Bake Deduplication** | ❌ No | ❌ No | ⚡ **Unified Bake Cache** |

---

## 🛠️ Supported CIT Properties

<details>
<summary><b>Click to expand full CIT property reference</b></summary>

- `type`: (Required) Currently only the `item` type is supported.
- `items` / `matchItems`: (Required) Target item ID(s), registry names, or pattern matchers (`regex:`, `iregex:`, `pattern:`, `ipattern:`).
- `texture` / `tile`: Custom texture path or file name.
- `model`: Custom JSON model path.
- `texture.bow_pulling_0/1/2`, `model.bow_pulling_0/1/2`: Bow pulling stages.
- `texture.bow_standby`, `model.bow_standby`: Bow idle state.
- `texture.fishing_rod_cast`, `model.fishing_rod_cast`: Fishing rod active state.
- `damage`: Explicit values, value ranges (e.g., `5-10`), or percentages (e.g., `50%`, `20%-80%`).
- `damageMask`: Bitwise mask applied to damage values before comparison.
- `stackSize`: Item count matching with support for ranges.
- `enchantments` / `enchantmentIDs`: Enchantment name or ID (works on gear and Enchanted Books).
- `enchantmentLevels`: Matching specific enchantment tiers and ranges.
- `nbt.<path>`: Deep NBT inspection with list wildcards (`*`) and regex matching.
- `weight`: Priority evaluation for overlapping rules.

See the [OptiFine CIT documentation](https://optifine.readthedocs.io/cit.html) for property syntax details.

</details>

---

## 📋 Requirements & Installation

### Requirements
- **Minecraft:** 1.12.2
- **Modloader:** Minecraft Forge (`14.23.5.2847`+) or CleanroomMC (`0.6.x`+)
- **Environment:** Client-side only (not required on dedicated servers)
- **Java:** Java 8 runtime for playing

### Installation
1. Install Minecraft Forge or CleanroomMC for 1.12.2.
2. Download `cit_reforged-1.0.0.jar` and place it into your `.minecraft/mods` folder.
3. Place any CIT-compatible resource pack in your `.minecraft/resourcepacks` folder and enable it in-game.

---

## 🔨 Building from Source

The build compiles modern Java syntax using Jabel to produce Java 8 compatible bytecode:

```bash
./gradlew build
```

The resulting mod jar is output to `build/libs/cit_reforged-1.0.0.jar`.

---

## 📜 Credits & License

**CIT Reforged** is released under the **MIT License**.

- **Original Author:** [EclipsedReaper](https://github.com/EclipsedReaper) for creating [CIT-Recrafted](https://github.com/EclipsedReaper/CIT-Recrafted).
- **Fork Maintainer:** [Brilliafy](https://github.com/Brilliafy) for performance optimizations, dynamic model additions, compatibility fixes, and maintenance.
- **Upstream Repository:** [https://github.com/EclipsedReaper/CIT-Recrafted](https://github.com/EclipsedReaper/CIT-Recrafted)
- **Fork Repository:** [https://github.com/Brilliafy/CITReforged](https://github.com/Brilliafy/CITReforged)

See the [LICENSE](LICENSE) file for complete copyright and license details.
