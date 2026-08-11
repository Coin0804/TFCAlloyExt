# Changelog — TFC Alloy Extension

## 0.3.0-beta

### Additions

- **Unified registration workflow**: new metals are registered via enum-driven classes (`RegularMetal` / `RegularMetals` for owned metals — solder, ferronickel, ferrochromium; `InferiorMetal` / `InferiorMetals` unified for all 11 inferior metals incl. IE-Addon Al/Pb/U and Firmalife Cr). Registration delegates to the new **TFC Extensions API** (`MetalRegistration`), now a required dependency.
- **Ferroalloy metals — Ferronickel & Ferrochromium**: molten fluid + block + bucket + ingot, with melt/cast recipes (fluid_heat / item_heat / casting / heating), tags (`c:molten_metal`, `tfc:molten_metals`, `c:ingots`), linear-blend ingot textures (nickel/chromium lower-left × cast iron upper-right).
- **Mass-conservation alloy recipes** (matrix-solved, user-finalized): ferronickel (nickel 20–30% + cast iron 70–80%), ferrochromium (chromium 60–70% + cast iron 30–40%, firmalife-gated), stainless steel ferro route (ferronickel 35–45% + ferrochromium 27–35% + cast iron 24–33%), weak steel ferro route (ferronickel 75–85% + black bronze 15–25%). Vanilla routes preserved.
- **Config-gated iron-line recipes**: custom recipe condition `tfc_alloy_ext:config_enabled` (reads `enableIronInferiorSystem`) applied to 39 wrought-iron heating recipes; the flag lives in a COMMON config (datapack reload evaluates conditions before SERVER config loads — 2026-08-09 timing pitfall).

### Changes

- Registration workflow (`MetalRegistration` / `FluidRegistrationHelper`) moved to TFC Extensions API — local copies removed, `tfc_ext_api` added as required dependency.

## 0.2.0-beta

### Additions

- **Dynamic alloy decomposition**: Known alloys (bronze, solder, etc.) in an impure crucible mix no longer sink into useless Unknown metal — they are decomposed into pure metal components using TFC's own alloy recipes, read at runtime (no hardcoded tables). New alloy recipes added by datapacks or KubeJS are picked up automatically; `/reload` applies them immediately.
  - Unique recipe without optional ingredients (`min = 0`) → full decomposition at range midpoints
  - Unique recipe with optional ingredients → only the certain base metal is decomposed
  - Multiple recipes producing the same alloy → only the base metal, smallest midpoint across all recipes
- **Inferior tool durability penalty**: tools (and fishing rods) assembled from inferior-alloy heads start with a configurable fraction of their max durability already damaged (`inferiorToolDurabilityPenalty`, default 10%, range 0–90%). Implemented purely via `PlayerEvent.ItemCraftedEvent` (the existing origin-tracing handler) — no mixins. TFC has no tool repair recipes, so the penalty lasts the tool's whole life. Anvil-forged inferior heads keep their vanilla forging bonus by design (bonus and penalty coexist).

### Changes

- Reorganized source packages by feature: root package now holds only the main class; new `config/`, `event/`, `client/` packages; all metal-related code consolidated into `metal/`.
- Registration deduplication: new `MetalRegistration` shared registrar (two-tier `registerRegular` / `registerInferior`), merging four copies of near-identical fluid registration code; ID derivation rules centralized.
- Removed dead code: `ModBlocks` (pure forwarding shell), `ModItems` (empty placeholder), unused helper methods.
- Code cleanup: explicit access modifiers (including `ModClientSetup` event handlers), fully qualified names → imports, chain-call extraction, wildcard imports removed.

---

## 0.1.0-beta

### Additions

- Inferior alloy system: 7 molten inferior metal fluids (copper/tin/zinc/bismuth/gold/silver/nickel) + blocks + buckets, with configurable threshold (default 75%), pollution spread, black-box sinking, and ternary-mix judgement.
- Iron inferior system: wrought iron treated as pure iron, cast iron as its inferior variant; includes wrought iron heating overrides and casting recipes (config-gated, default off).
- Solder metal: a tin-lead-bismuth alloy with full fluid, ingot, bucket, and casting recipe support.
- Crafting propagation: the inferior tag persists through TFC forging, welding, and vanilla crafting.
- Immersive Engineering integration: Arc Furnace / Alloy Smelter contaminated recipes + multiblock process component propagation (optional dependency).
- Crucible pour speed: configurable multiplier (default 4×).
- Optional dependency support: tfc_ie_addon (aluminum/lead/uranium inferior metals), Firmalife (chromium inferior metal), Productive Metalworks (placeholder).
