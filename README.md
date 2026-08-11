# TFC Alloy Extension / 群峦合金扩展

[English](#english) | [中文](#中文)

---

## English

An expansion to TerraFirmaCraft's alloy system. When your crucible mix doesn't match any known alloy recipe, you no longer have to accept useless Unknown metal. You can still get a usable, though inferior, alloy.

### Inferior Alloy System

When melting metals in a crucible, if the ratios don't match any known alloy recipe, this mod checks whether one metal dominates the mix. If the dominant metal exceeds a configurable threshold (default 75%), a usable "inferior" variant is produced — impure, but still functional.

**Default pure metals:** Copper, Tin, Zinc, Bismuth, Gold, Silver, Nickel

**Optional addon metals:**
- TFC-IE-Crossover (optional): Aluminum, Lead, Uranium
- Firmalife (optional): Chromium

**Alloy decomposition:** When a known alloy (e.g. bronze) is part of an impure mix, it is decomposed into its pure metal components using TFC's own alloy recipes, read at runtime — no hardcoded tables. When a decomposition is uncertain (multiple recipes produce the same alloy, or a recipe has optional ingredients with `min = 0`), only the certain base metal is counted, so unidentifiable secondary metals never distort the judgement.

### Iron Inferior System

In TFC, wrought iron is purified through repeated forging, while cast iron is the brittle, high-carbon form. This system treats **wrought iron as pure iron** and **cast iron as inferior iron**.

Two changes are always active, regardless of config:

- **Wrought iron items melt into wrought iron fluid** (overriding TFC's default behavior of producing cast iron). Pure stays pure.
- **Wrought iron fluid can be directly cast into ingots** via new casting recipes, a feature absent from vanilla TFC.

The `alloy.enableIronInferiorSystem` config toggle (disabled by default) additionally enables crucible alloy logic: when wrought iron is the dominant metal in an impure crucible mix, it produces **cast iron fluid** as the inferior output — just like copper/tin/zinc produce their inferior variants.

### Solder

A new metal added by this mod — a tin-lead-bismuth alloy with its own fluid, ingots, buckets, and casting recipes.

### Crafting Propagation

Items crafted with inferior alloy materials automatically inherit the "inferior" tag. This tag persists through TFC forging, welding, and vanilla crafting, ensuring correct fluid recovery when the item is eventually melted down. Compatible with Immersive Engineering multiblock machines — the tag is preserved through Arc Furnace and Alloy Smelter processing.

### Ferroalloys: Ferronickel & Ferrochromium (0.3.0-beta)

New iron-alloy metals with full registration (fluid + block + bucket + ingot), melt/cast recipes and tags:

- **Ferronickel** (FeNi, 15–35% Ni) — always registered; alloyed from nickel 20–30% + cast iron 70–80%
- **Ferrochromium** (FeCr, high-carbon) — registered when Firmalife is present; alloyed from chromium 60–70% + cast iron 30–40%

**Alloy consumption routes** (mass-conservation matrix, vanilla routes preserved):
- Stainless steel (ferro route): ferronickel + ferrochromium + cast iron
- Weak steel (ferro route): ferronickel + black bronze

### Crucible Pour Speed

Configurable pour speed multiplier (default 4×). Accelerates fluid transfer from the crucible into molds.

### Dependencies

| Mod | Type |
|-----|------|
| TerraFirmaCraft 4.1.3+ | Required |
| TFC Extensions API (tfc_ext_api) 0.3.0+ | Required |
| TFC-IE-Crossover 2.1+ | Optional |
| Immersive Engineering 12.0+ | Optional |
| Productive Metalworks | Optional |
| Firmalife 3.0+ | Optional |

### Configuration

`config/tfc_alloy_ext-server.toml`

| Setting | Default | Description |
|---------|---------|-------------|
| `crucible.pourSpeedMultiplier` | 4 | Crucible pour speed multiplier |
| `alloy.inferiorAlloyThreshold` | 0.75 | Dominant metal threshold (50%~100%) |
| `alloy.enableIronInferiorSystem` | false | Enable iron inferior system |

### Build

```bash
./gradlew jar
# Output: build/libs/tfc_alloy_ext-neoforge-0.2.0-beta.jar
```

### License

MIT — see [LICENSE](LICENSE)

---

## 中文

群峦传说合金系统扩展模组。当你在坩埚里把金属比例搞砸了，不会只剩一堆不可用的"未知"废料——只要有一种金属占比够高，你仍然能得到可用的劣等合金。

### 劣等合金系统

在坩埚中混合金属时，如果配比无法匹配任何已知合金配方，本模组会检查是否有某种金属占了绝大多数。如果主导金属的占比超过可配置阈值（默认 75%），就会产出一个可用的"劣等"版本——纯度不够，但照样能用。

**默认支持的纯金属：** 铜、锡、锌、铋、金、银、镍

**可选模组扩展：**
- 安装 TFC-IE-Crossover 后：铝、铅、铀
- 安装 Firmalife 后：铬

**合金分解：** 当混合物中含有已知合金（如青铜）时，会按 TFC 自身的合金配方（运行时读取，无硬编码表）分解为纯金属成分参与判定。当分解存在不确定性时（同一合金有多个配方、或配方含 `min=0` 的可选成分），只计入确定的基础金属，避免无法推断的次等金属扭曲判定结果。

### 铁劣等系统

在群峦中，锻铁是经过反复锻打纯化得到的金属，铸铁则是含碳较高的脆性形态。本系统将**锻铁视为纯铁**、**铸铁视为劣等铁**。

以下两项始终生效，不受配置开关影响：

- **锻铁物品加热熔融后产出锻铁流体**（覆写了 TFC 原版产出铸铁流体的行为）。纯的归纯的。
- **锻铁流体可直接浇铸成锭**，原版群峦没有这个配方。

`alloy.enableIronInferiorSystem` 配置开关（默认关闭）额外启用坩埚合金逻辑：当锻铁在坩埚中占比不足产生合金时，主导金属锻铁会产出**铸铁流体**作为劣等变体——和铜/锡/锌等完全一致的劣等逻辑。

### 焊锡

本模组新增的金属——一种锡-铅-铋三元合金。拥有完整的流体、锭、桶和浇铸配方。

### 合成追溯

使用劣等合金材料合成物品时，产物会自动继承"劣等"标记。这个标记在群峦锻造、焊接、原版合成中持续传递，也兼容沉浸工程的多方块机器——电弧炉和合金窑处理过程中标记不会丢失，确保最终回熔时能正确返回劣等流体。

### 铁合金：镍铁与铬铁（0.3.0-beta）

新铁合金金属，完整注册（流体+方块+桶+锭）、熔铸配方与标签：

- **镍铁**（FeNi，含镍 15-35%）——始终注册；镍 20-30% + 铸铁 70-80% 合金化
- **铬铁**（FeCr，高碳）——Firmalife 存在时注册；铬 60-70% + 铸铁 30-40% 合金化

**合金消费路线**（成分守恒矩阵，原配方保留）：
- 不锈钢（ferro 路线）：镍铁 + 铬铁 + 铸铁
- 脆钢（ferro 路线）：镍铁 + 黑青铜

### 坩埚倾倒加速

可配置的倾倒速度倍率（默认 4×），加速坩埚向模具排出流体。

### 依赖

| 模组 | 类型 |
|------|------|
| TerraFirmaCraft 4.1.3+ | 必需 |
| 群峦扩展 API（tfc_ext_api）0.3.0+ | 必需 |
| TFC-IE-Crossover 2.1+ | 可选 |
| Immersive Engineering 12.0+ | 可选 |
| Productive Metalworks | 可选 |
| Firmalife 3.0+ | 可选 |

### 配置

配置文件：`config/tfc_alloy_ext-server.toml`（倾倒/阈值/耐久减益）+ `config/tfc_alloy_ext-common.toml`（铁劣等开关——配方条件在数据包重载时读取，早于 SERVER 配置加载，故独立 COMMON）

| 设置项 | 默认值 | 说明 |
|--------|--------|------|
| `crucible.pourSpeedMultiplier` | 4 | 坩埚倾倒速度倍率 |
| `alloy.inferiorAlloyThreshold` | 0.75 | 劣等合金判定阈值（50%~100%） |
| `alloy.enableIronInferiorSystem` | false | 铁劣等系统开关（COMMON 配置） |

### 构建

```bash
./gradlew jar
# 输出：build/libs/tfc_alloy_ext-neoforge-0.3.0-beta.jar
```

### 许可

MIT 协议 —— 详见 [LICENSE](LICENSE)
