# CIT Reforged

![Minecraft 1.12.2](https://img.shields.io/badge/Minecraft-1.12.2-22c55e?style=flat-square)
![Forge and Cleanroom](https://img.shields.io/badge/Modloader-Forge%20%7C%20Cleanroom-orange?style=flat-square)
![MIT License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

**CIT Reforged** brings Custom Item Textures (CIT) to Minecraft 1.12.2 without requiring OptiFine.

If you play modern 1.12 modpacks with mods like Cleanroom, VintageFix, Phosphor, or CensoredASM, you probably already know that OptiFine causes constant crashes and visual bugs. But dropping OptiFine usually means losing your custom weapon skins, renamed tools, and pack-specific models.

CIT Reforged fixes that. It loads your existing OptiFine and MCPatcher CIT resource packs natively on Forge and Cleanroom, completely standalone.

---

### What's new compared to CIT Recrafted?

This mod is an optimized continuation of [CIT-Recrafted](https://github.com/EclipsedReaper/CIT-Recrafted) by EclipsedReaper. The original proved standalone CIT on 1.12 was possible, but had noticeable performance hiccups and missing features in actual gameplay. Here is what has been improved:

* **Eliminated rendering lag & micro-stutter**  
  The original mod cloned every item's entire NBT tag on every single render frame. If you looked at an item frame wall or opened a full chest, Java's garbage collector had to clean up thousands of throwaway objects every second. CIT Reforged switches to pooled thread-local lookups and an LRU cache, bringing memory allocations down to zero during rendering.

* **Working bows and fishing rods**  
  Custom bows now smoothly transition through pulling stages (`pulling_0`, `pulling_1`, `pulling_2`) when drawing an arrow. Fishing rods also swap correctly between cast and uncast models based on your bobber hook.

* **Enchanted book support**  
  Minecraft stores enchantment data on books under `StoredEnchantments` instead of the usual `ench` tag. The original mod missed this, so book reskins never showed up. CIT Reforged checks both tags.

* **Emissive textures & animations**  
  OptiFine glowing `_e.png` companion textures are now registered properly alongside custom models, and animated `.mcmeta` files play as intended.

* **Loads root-level ZIP packs**  
  Older packs often put their CIT files right at the root of the zip archive (`optifine/cit/...` or `mcpatcher/cit/...`) instead of nesting them inside `assets/`. CIT Reforged detects both layouts without making you unpack and rearrange the files yourself.

* **Shared model baking**  
  Identical models and textures are baked once and reused rather than re-baked for every matching rule, saving both startup time and VRAM.

---

### Comparison

| Feature | OptiFine 1.12.2 | Original CIT-Recrafted | CIT Reforged |
| :--- | :---: | :---: | :---: |
| Standalone (No OptiFine needed) | No | Yes | **Yes** |
| Cleanroom & Modern Fixes Support | Often breaks | Works | **Fully supported** |
| Per-frame memory allocations | Low | High (clones NBT every tick) | **Zero (ThreadLocal cache)** |
| Rule lookup speed | Linear scan | Linear scan | **O(1) LRU cache** |
| Bow pulling animations | Yes | No (static only) | **Yes (3 draw stages)** |
| Fishing rod cast state | Yes | No (static only) | **Yes** |
| Enchanted books | Yes | Broken | **Works** |
| Emissive `_e.png` textures | Yes | Ignored | **Supported** |
| Root-level ZIP packs | Yes | Broken | **Fixed** |

---

### Quick Setup

* **Requirements:** Minecraft 1.12.2 with Forge (recommended `14.23.5.2847`+) or CleanroomMC (`0.6.x`+).
* **Install:** Drop `cit_reforged-1.0.0.jar` into your `.minecraft/mods` folder.
* **Usage:** Put any CIT-compatible resource pack in `.minecraft/resourcepacks` and turn it on in the in-game menu.
* **Side:** Client-side only. You can join vanilla, Forge, or Paper servers without having the mod installed server-side.

---

### Credits & Thanks

Huge thanks to **EclipsedReaper** for creating the original [CIT-Recrafted](https://github.com/EclipsedReaper/CIT-Recrafted) and establishing standalone 1.12 CIT loading. CIT Reforged continues that work under the original MIT license.

* Upstream project: [EclipsedReaper/CIT-Recrafted](https://github.com/EclipsedReaper/CIT-Recrafted)
* Source & issue tracker: [Brilliafy/CITReforged](https://github.com/Brilliafy/CITReforged)
