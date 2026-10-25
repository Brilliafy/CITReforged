<div align="center">

  <img src="logo.png" alt="CIT Reforged Logo" width="160" />

  # CIT Reforged

  <p><strong>A high-performance, zero-allocation Custom Item Textures (CIT) engine for Minecraft 1.12.2.</strong></p>
  <p><em>Native OptiFine and MCPatcher CIT resource pack support — completely standalone, no OptiFine required.</em></p>

  <p>
    <a href="https://github.com/Brilliafy/CITReforged/releases"><img src="https://img.shields.io/github/v/release/Brilliafy/CITReforged?color=00e5ff&style=for-the-badge&logo=github&label=Release" alt="Latest Release" /></a>
    <img src="https://img.shields.io/badge/Minecraft-1.12.2-22c55e?style=for-the-badge&logo=curseforge&logoColor=white" alt="Minecraft 1.12.2" />
    <img src="https://img.shields.io/badge/Loader-Forge%20%7C%20Cleanroom-f59e0b?style=for-the-badge" alt="Forge & Cleanroom" />
    <a href="LICENSE"><img src="https://img.shields.io/badge/License-MIT-3b82f6?style=for-the-badge" alt="MIT License" /></a>
  </p>

  <p>
    <a href="#-about-the-project">About</a> •
    <a href="#-key-features">Features</a> •
    <a href="#-fork-improvements--benefits">Why CIT Reforged?</a> •
    <a href="#-comparison-matrix">Comparison</a> •
    <a href="#-supported-properties">Supported Properties</a> •
    <a href="#-installation">Installation</a> •
    <a href="#-credits--acknowledgments">Credits</a>
  </p>

</div>

---

> [!NOTE]
> **Why Standalone CIT?**
> OptiFine 1.12.2 frequently crashes or causes rendering artifacts when paired with modern optimization mods (such as **CleanroomMC**, **CensoredASM**, **VintageFix**, **Phosphor**, and **FoamFix**). **CIT Reforged** is an optimized, drop-in replacement that lets you render custom weapons, tools, armors, and items from OptiFine/MCPatcher packs with smooth framerates and zero garbage collection overhead.

---

## 📖 About the Project

**CIT Reforged** is an optimized, active continuation of [CIT-Recrafted](https://github.com/EclipsedReaper/CIT-Recrafted) originally authored by **EclipsedReaper**. 

While the original project proved that standalone CIT loading was possible on 1.12.2, it suffered from severe frame allocation churn (NBT cloning every tick), static-only bow/rod states, and pack discovery issues. **CIT Reforged** completely overhauls the internal caching and baking pipelines to deliver seamless OptiFine feature parity at maximum performance.

---

## ✨ Key Features

<table>
  <tr>
    <td width="50%" valign="top">
      <h3>🚀 Zero-Allocation Rendering</h3>
      <p>Replaces heavy per-frame memory allocations with recyclable <code>ThreadLocal</code> lookup keys. Caches evaluated rules in an LRU model cache (<code>ModelCache</code>) for instantaneous <b>O(1)</b> lookups.</p>
    </td>
    <td width="50%" valign="top">
      <h3>🏹 Dynamic Bows & Rods</h3>
      <p>Full animation support! Seamlessly transitions custom bows through drawing stages (<code>pulling_0</code>, <code>pulling_1</code>, <code>pulling_2</code>) and handles fishing rod active/cast models.</p>
    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">
      <h3>📚 Enchanted Book Support</h3>
      <p>Natively inspects both standard <code>ench</code> tags and <code>StoredEnchantments</code> tags, allowing custom enchanted books to display their unique reskins based on enchantment and level.</p>
    </td>
    <td width="50%" valign="top">
      <h3>✨ Emissives & Animations</h3>
      <p>Registers OptiFine emissive <code>_e.png</code> textures for glowing details with modern shaders, and accurately proxies <code>.mcmeta</code> files for animated items.</p>
    </td>
  </tr>
  <tr>
    <td width="50%" valign="top">
      <h3>📁 Universal ZIP Pack Discovery</h3>
      <p>Correctly discovers CIT rules placed at the root of a ZIP pack (legacy MCPatcher style) or nested inside <code>assets/minecraft/</code>, with robust <code>~/</code>, <code>./</code>, and <code>../</code> path resolution.</p>
    </td>
    <td width="50%" valign="top">
      <h3>🧩 Baked Model Deduplication</h3>
      <p>Shares identical texture and model baking results across rules via <code>bakedCache</code>, drastically cutting initial launch times and saving memory in large packs (e.g. Conquest or John Smith).</p>
    </td>
  </tr>
</table>

---

## ⚡ Fork Improvements & Benefits

Here is a detailed breakdown of how **CIT Reforged** improves upon the original [CIT-Recrafted](https://github.com/EclipsedReaper/CIT-Recrafted) codebase:

### 1. Zero Garbage Collection Stutter
* **The Issue:** Upstream instantiated a `new ItemCacheKey` and called `stack.getTagCompound().copy()` on **every frame for every rendered item**. In complex scenes with full inventories, modded chests, or item frames, this allocated millions of temporary objects and caused frequent GC freeze frames.
* **The Solution:** CIT Reforged introduces a static `ThreadLocal<ItemCacheKey.LookupKey>` that resets in-place without memory allocation and without cloning NBT.
* **Benefit:** Eliminates rendering micro-stutter and lowers heap pressure to near zero.

### 2. Full Dynamic Animation States (Bows & Fishing Rods)
* **The Issue:** Upstream only handled static item models. Bows and fishing rods never updated when drawn or cast.
* **The Solution:** CIT Reforged checks player draw duration and active bobber hook state during model evaluation, dynamically swapping into `texture.bow_pulling_0/1/2`, `model.bow_pulling_0/1/2`, or `texture.fishing_rod_cast`.
* **Perspective Safety:** Wraps models in `PerspectiveModelWrapper` so 3D camera transforms (First Person, Third Person, GUI, Ground, Fixed) are strictly preserved during dynamic state transitions.

### 3. Enchanted Books Compatibility
* **The Issue:** Upstream inspected only `stack.getTagCompound().getTagList("ench", 10)`. Minecraft stores Enchanted Book data inside `StoredEnchantments`, causing all enchantment-based rules on books to silently fail.
* **The Solution:** CIT Reforged inspects both `ench` and `StoredEnchantments`.

### 4. OptiFine Emissive Glow & Animation Metadata
* **The Issue:** Emissive `_e.png` companion textures were skipped during texture stitching, and `.mcmeta` files could fail to load through the proxy pack.
* **The Solution:** `CITRegistry.registerTexture` automatically registers and proxies all `_e.png` variants and `.mcmeta` descriptors.

### 5. Universal Pack Directory Discovery
* **The Issue:** Packs placing CIT configuration files directly in the ZIP root (`optifine/cit/...` or `mcpatcher/cit/...`) failed to load because the parser expected a rigid `assets/` structure.
* **The Solution:** Both `CITParser` and `CITProxyPack` scan and resolve resources from pack roots and nested folders alike.

---

## 📊 Comparison Matrix

<table>
  <thead>
    <tr>
      <th align="left">Capability / Feature</th>
      <th align="center">Vanilla / OptiFine 1.12.2</th>
      <th align="center">Original CIT-Recrafted</th>
      <th align="center">CIT Reforged (This Fork)</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><b>Standalone (No OptiFine)</b></td>
      <td align="center">❌ No</td>
      <td align="center">✅ Yes</td>
      <td align="center">✅ <b>Yes</b></td>
    </tr>
    <tr>
      <td><b>CleanroomMC & Modern Modpack Support</b></td>
      <td align="center">❌ Crash-prone</td>
      <td align="center">✅ Supported</td>
      <td align="center">⚡ <b>Flawless</b></td>
    </tr>
    <tr>
      <td><b>Per-Frame Heap Allocation</b></td>
      <td align="center">Moderate</td>
      <td align="center">❌ High (Clones NBT every tick)</td>
      <td align="center">⚡ <b>Zero-Allocation (ThreadLocal)</b></td>
    </tr>
    <tr>
      <td><b>Rule Evaluation Performance</b></td>
      <td align="center">Linear Scan</td>
      <td align="center">Linear scan per frame</td>
      <td align="center">⚡ <b>O(1) LRU Rule Cache</b></td>
    </tr>
    <tr>
      <td><b>Bow Pulling Animations (0 / 1 / 2)</b></td>
      <td align="center">✅ Yes</td>
      <td align="center">❌ Static only</td>
      <td align="center">✅ <b>Full 3-stage pulling</b></td>
    </tr>
    <tr>
      <td><b>Fishing Rod Cast Models</b></td>
      <td align="center">✅ Yes</td>
      <td align="center">❌ Static only</td>
      <td align="center">✅ <b>Dynamic Hook Check</b></td>
    </tr>
    <tr>
      <td><b>Enchanted Books (<code>StoredEnchantments</code>)</b></td>
      <td align="center">✅ Yes</td>
      <td align="center">❌ Broken</td>
      <td align="center">✅ <b>Fully Supported</b></td>
    </tr>
    <tr>
      <td><b>Emissive Textures (<code>_e.png</code>)</b></td>
      <td align="center">✅ Yes</td>
      <td align="center">❌ Ignored</td>
      <td align="center">✅ <b>Registered & Proxied</b></td>
    </tr>
    <tr>
      <td><b>Animated Textures (<code>.mcmeta</code>)</b></td>
      <td align="center">✅ Yes</td>
      <td align="center">⚠️ Partial</td>
      <td align="center">✅ <b>Full Support</b></td>
    </tr>
    <tr>
      <td><b>Root-Level ZIP Discovery</b></td>
      <td align="center">✅ Yes</td>
      <td align="center">❌ Broken</td>
      <td align="center">✅ <b>Fixed & Auto-Discovered</b></td>
    </tr>
    <tr>
      <td><b>Model Baking Deduplication</b></td>
      <td align="center">❌ No</td>
      <td align="center">❌ No</td>
      <td align="center">⚡ <b>Unified Bake Cache</b></td>
    </tr>
  </tbody>
</table>

---

## 🛠️ Supported Properties

<details>
<summary><b>Click to expand full CIT property reference</b></summary>

<br />

| Property | Description | Example |
| :--- | :--- | :--- |
| `type` | Rule target type (currently `item`). | `type=item` |
| `items` / `matchItems` | Target item names, IDs, or pattern matchers. | `items=diamond_sword pattern:*_axe` |
| `texture` / `tile` | Custom texture path or filename. | `texture=ruby_sword.png` |
| `model` | Custom JSON model path. | `model=models/item/ruby_sword.json` |
| `texture.bow_pulling_0/1/2` | Bow draw stage textures. | `texture.bow_pulling_0=bow_pull_0.png` |
| `model.bow_pulling_0/1/2` | Bow draw stage 3D models. | `model.bow_pulling_0=bow_pull_0.json` |
| `texture.bow_standby` | Bow idle texture. | `texture.bow_standby=bow_standby.png` |
| `texture.fishing_rod_cast` | Cast fishing rod texture. | `texture.fishing_rod_cast=rod_cast.png` |
| `damage` | Damage values, value ranges, or percentages. | `damage=0-50` or `damage=75%` |
| `damageMask` | Bitwise mask applied to damage values. | `damageMask=15` |
| `stackSize` | Item count in stack, supports ranges. | `stackSize=1-16` |
| `enchantments` | Enchantment names or numeric IDs. | `enchantments=sharpness fire_aspect` |
| `enchantmentLevels` | Enchantment level matching with ranges. | `enchantmentLevels=1-3` |
| `nbt.<path>` | Deep NBT inspection with regex and wildcards. | `nbt.display.Name=regex:(?i)Excalibur` |
| `weight` | Rule priority over other matches. | `weight=10` |

Refer to the official [OptiFine CIT Documentation](https://optifine.readthedocs.io/cit.html) for full property syntax specifications.

</details>

---

## 📥 Installation

1. Ensure **Minecraft 1.12.2** is installed with either:
   - **Minecraft Forge** (`14.23.5.2847` or newer)
   - **CleanroomMC** (`0.6.x` or newer)
2. Download `cit_reforged-1.0.0.jar` from [Releases](https://github.com/Brilliafy/CITReforged/releases).
3. Place the `.jar` into your `.minecraft/mods` directory.
4. Drop your CIT resource packs into `.minecraft/resourcepacks` and enable them in-game.

> [!TIP]
> CIT Reforged is a client-side mod. It does **not** need to be installed on dedicated servers.

---

## 🔨 Building from Source

This project targets Java 8 bytecode using Jabel to compile modern Java syntax. A JDK 17+ or JDK 25 environment is recommended for building:

```bash
# Clone the repository
git clone https://github.com/Brilliafy/CITReforged.git
cd CITReforged

# Build the project
./gradlew build
```

The compiled mod jar will be located at `build/libs/cit_reforged-1.0.0.jar`.

---

## 🤝 Credits & Acknowledgments

**CIT Reforged** is proudly open-source under the **MIT License**.

* **Original Creator:** Massive credit to [EclipsedReaper](https://github.com/EclipsedReaper) for designing the original [CIT-Recrafted](https://github.com/EclipsedReaper/CIT-Recrafted) mod and proving the concept of standalone 1.12.2 CIT parsing.
* **Fork Maintainer:** [Brilliafy](https://github.com/Brilliafy) — performance architecture, zero-allocation memory pipeline, dynamic animations, bug fixes, and maintenance.
* **Upstream Repository:** [https://github.com/EclipsedReaper/CIT-Recrafted](https://github.com/EclipsedReaper/CIT-Recrafted)
* **Fork Repository:** [https://github.com/Brilliafy/CITReforged](https://github.com/Brilliafy/CITReforged)

<div align="center">
  <sub>MIT License © 2026 EclipsedReaper & Brilliafy</sub>
</div>
