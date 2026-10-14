# Changelog

## [1.0.0] - 2026-10-14

First release of CIT Reforged, an optimized continuation of CIT Recrafted.

### Changed

- Renamed the project, mod, jar, mixin config and access transformer to CIT Reforged (`cit_reforged`).
- Moved the source tree to the `com.brilliafy.citreforged` package.
- Added an icon, description, author and original-author credit to the mod metadata.

### Added

- Per-model LRU cache for matched rules, so a stack that has already been resolved does not re-run
  the rule matcher on every model lookup.
- Baked model cache in the bake listener, including bow pulling stages and the fishing rod cast
  state, so identical texture and model combinations are baked once and reused.
- OptiFine and MCPatcher path resolution for texture, model and sub-model properties.
- Enchanted book support for the `enchantments` condition.
- Proxy pack result caching and `.mcmeta` awareness, plus support for OptiFine emissive `_e`
  textures.

### Fixed

- CIT rules inside a zip pack are now found when the pack places them at the pack root instead of
  under `assets/`.
- Proxy pack lookups no longer fall through to a null pack on every resource query.

## [0.1.0] - Original CIT Recrafted

See [CIT Recrafted](https://github.com/EclipsedReaper/CIT-Recrafted) for the original project history.
