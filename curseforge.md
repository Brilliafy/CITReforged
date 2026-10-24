# CIT Reforged

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.12.2-success.svg?style=for-the-badge)
![Modloader](https://img.shields.io/badge/Modloader-Forge%20%7C%20Cleanroom-orange.svg?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)

**CIT Reforged** is a high-performance, standalone **Custom Item Textures (CIT)** engine for **Minecraft 1.12.2** (Forge & Cleanroom). It provides 100% native support for OptiFine and MCPatcher CIT resource packs **without requiring OptiFine to be installed**.

If you run modern 1.12.2 optimization mods (such as CensoredASM, VintageFix, Phosphor, FoamFix, or CleanroomMC) where OptiFine causes crashes, visual glitches, or massive memory overhead, **CIT Reforged** is the drop-in replacement that lets you enjoy all your favorite custom weapon, tool, armor, and item skins at maximum frame rates.

---

## 🌟 Why CIT Reforged? (What Makes It Better)

While the original [CIT-Recrafted](https://github.com/EclipsedReaper/CIT-Recrafted) proved that standalone CIT rendering on 1.12.2 was possible, it suffered from severe rendering stutter, missing OptiFine features, and pack compatibility bugs. 

**CIT Reforged** completely overhauls the internal architecture:

### ⚡ 1. Zero-Allocation Rendering Engine (No Micro-Stutters)
- **Garbage Collection Elimination:** The original mod allocated a new `ItemCacheKey` and performed a full deep-copy (`.copy()`) of every item's `NBTTagCompound` on **every single frame** for **every item rendered**. In busy inventories, hotbars, or modpacks with item frames, this caused constant garbage collection pauses.
- **ThreadLocal Reset Lookups:** CIT Reforged replaces this with reusable `ThreadLocal` lookup keys that reset in-place with zero memory allocation and zero NBT cloning.
- **Precomputed Hashes & LRU Rule Cache:** Matching rules are cached in an LRU model cache (`ModelCache(500)`). Once a stack's conditions (durability, NBT, count, enchantments) are verified, subsequent frames look up the model in constant $O(1)$ time.

### 🏹 2. Dynamic Bow Pulling & Fishing Rod Cast Models
- **Full Animation Support:** The original mod only supported static item models; custom bows and fishing rods were completely static or broken.
- **OptiFine Specification Parity:** CIT Reforged introduces full support for:
  - `texture.bow_pulling_0`, `texture.bow_pulling_1`, `texture.bow_pulling_2` (and model variants)
  - `texture.fishing_rod_cast` (and uncast variants)
  - Real-time player animation tracking (hand use duration and active bobber hook state).
  - Proper 3D camera perspective transformations (`PerspectiveModelWrapper`) so models look identical in First-Person, Third-Person, GUI, and on the ground.

### 📚 3. Enchanted Book Support (`StoredEnchantments`)
- Legacy CIT mods only checked the item's `ench` tag. This caused enchantment-based CIT rules to fail on Enchanted Books.
- CIT Reforged natively inspects both `ench` and `StoredEnchantments`, allowing packs that reskin enchanted books based on their specific enchantment and level to work flawlessly.

### 💡 4. Emissive Textures (`_e`) & Animation Metadata (`.mcmeta`)
- Support for OptiFine emissive texture layers (`_e.png`).
- Fully proxies `.mcmeta` texture animation files, ensuring custom animated CIT items cycle frames as intended.

### 📦 5. Universal Resource Pack Compatibility & Path Resolution
- **Root-Level CIT Support:** Fixes a major bug where zip packs placing CIT folders at the root directory (`optifine/cit/...` or `mcpatcher/cit/...`) instead of nested under `assets/minecraft/` failed to load.
- **Universal Path Resolver:** Accurately resolves relative paths (`./`), parent directories (`../`), absolute paths (`~/` expanding to `optifine/` or `mcpatcher/`), and case-insensitive file references.
- **Model Deduplication:** Automatically caches and shares baked models across duplicate rules, dramatically lowering loading times and VRAM footprint in heavy packs (such as Conquest or John Smith).

---

## 📊 Feature Comparison

| Feature | Vanilla / OptiFine 1.12.2 | Original CIT-Recrafted | CIT Reforged (This Mod) |
| :--- | :---: | :---: | :---: |
| **Standalone (No OptiFine Needed)** | ❌ (Requires OptiFine) | ✅ Yes | ✅ **Yes** |
| **Cleanroom & Modern Modpack Support** | ❌ Incompatible / Crash-prone | ✅ Yes | ✅ **Flawless (Forge + Cleanroom)** |
| **Frame Allocation / GC Churn** | Moderate | ❌ High (Clones NBT every frame) | ⚡ **Zero-Allocation (ThreadLocal)** |
| **Rule Matching Speed** | Linear scan | Linear scan per frame | ⚡ **$O(1)$ LRU Rule Cache** |
| **Bow Pulling Animations** | ✅ Yes | ❌ Not supported | ✅ **Full 3-stage pulling support** |
| **Fishing Rod Cast State** | ✅ Yes | ❌ Not supported | ✅ **Supported** |
| **Enchanted Books (`StoredEnchantments`)** | ✅ Yes | ❌ Broken | ✅ **Supported** |
| **Emissive Textures (`_e.png`)** | ✅ Yes | ❌ Ignored | ✅ **Registered & Proxied** |
| **Animated `.mcmeta` Files** | ✅ Yes | ⚠️ Partial | ✅ **Full support** |
| **Root-Folder ZIP Pack Discovery** | ✅ Yes | ❌ Broken | ✅ **Fixed & Supported** |

---

## 🛠️ Supported CIT Properties

CIT Reforged supports standard OptiFine and MCPatcher property configurations:

- `type`: `item` (default)
- `items` / `matchItems`: Target item ID(s), registry names, numeric IDs, or pattern matchers (`regex:`, `iregex:`, `pattern:`, `ipattern:`).
- `texture` / `tile`: Custom texture path or filename.
- `model`: Custom JSON model path.
- `texture.bow_pulling_0/1/2` & `model.bow_pulling_0/1/2`: Bow pulling stages.
- `texture.bow_standby` & `model.bow_standby`: Bow idle state.
- `texture.fishing_rod_cast` & `model.fishing_rod_cast`: Fishing rod active state.
- `damage`: Explicit values, value ranges (e.g., `5-10`), or percentages (e.g., `50%`, `20%-80%`).
- `damageMask`: Bitwise mask applied to damage values.
- `stackSize`: Item count matching with support for ranges.
- `enchantments` / `enchantmentIDs`: Enchantment name or ID (works on gear and Enchanted Books).
- `enchantmentLevels`: Matching specific enchantment tiers and ranges.
- `nbt.<path>`: Deep NBT inspection with list wildcards (`*`) and regex matching.
- `weight`: Priority evaluation for overlapping rules.

---

## 📥 Installation

1. Make sure you have **Minecraft 1.12.2** installed with either:
   - **Minecraft Forge** (version `14.23.5.2847` or newer)
   - **CleanroomMC** (version `0.6.x` or newer)
2. Download `cit_reforged-1.0.0.jar` from the **Files** tab.
3. Place the jar file into your `.minecraft/mods` directory.
4. Put any CIT-compatible resource pack into `.minecraft/resourcepacks` and enable it in the in-game Resource Packs menu.
5. *Note: CIT Reforged is a client-side mod. It does not need to be installed on dedicated servers.*

---

## 📜 Credits & License

**CIT Reforged** is an open-source project distributed under the **MIT License**.

- **Original Author & Mod:** Huge credit and gratitude to [EclipsedReaper](https://github.com/EclipsedReaper) for creating the original [CIT-Recrafted](https://github.com/EclipsedReaper/CIT-Recrafted) mod and laying the foundational architecture for standalone 1.12.2 CIT loading.
- **CIT Reforged Fork & Maintenance:** Developed, optimized, and maintained by [Brilliafy](https://github.com/Brilliafy).
- **Source Code:** [GitHub Repository (Brilliafy/CITReforged)](https://github.com/Brilliafy/CITReforged)
- **Original Source Code:** [GitHub Repository (EclipsedReaper/CIT-Recrafted)](https://github.com/EclipsedReaper/CIT-Recrafted)
