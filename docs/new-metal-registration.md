# 新金属注册完整工作流

> 本文档说明如何在 TFCAlloyExt 中注册一种新金属（熔融流体 + 方块 + 桶 + 锭物品）。
> 参考实现：镍铁/铬铁（自有金属，2026-08-09 S2）、焊锡（自有金属）、11 种劣等金属。

---

## 1. 两级工作流总览

| 类型 | 枚举 | 注册类 | 委托方法 | 锭物品 | 适用 |
|------|------|--------|----------|--------|------|
| **自有金属**（常规） | `metal.RegularMetal` | `metal.RegularMetals` | `MetalRegistration.registerRegular` | 有自有锭 | 焊锡、镍铁、铬铁、未来的新合金 |
| **劣等金属** | `metal.InferiorMetal` | `metal.InferiorMetals` | `MetalRegistration.registerInferior` | 无（复用 TFC 原版锭物品） | 劣等铜/锡/…、IE Addon 铝/铅/铀、Firmalife 铬 |

**核心机制**：枚举驱动。枚举值追加后，注册、客户端颜色、创造标签页全部自动（遍历 `getRegistered()`）；带前置 mod 的金属在静态初始化时按 `isEnabled()` 过滤。

---

## 2. 步骤清单（以自有金属为例，劣等金属略去锭相关项）

### Step 1 · 枚举加值

[`metal/RegularMetal.java`](../src/main/java/com/yukimods/alloyext/metal/RegularMetal.java)

```java
MYMETAL("mymetal", 熔融熔点°C, 0xFF流体颜色, 前置modId或null),
```

- `name` = 注册 ID 派生唯一数据源（流体 `metal/mymetal`、锭 `metal/ingot/mymetal` 等，派生规则见 `MetalRegistration` 注释）
- `meltingTemp`：贴近现实液相线；与 `fluid_heat` 数据文件的 `melt_temperature` 对应（单一数据源，见 Step 4）
- `color`：流体渲染色（ARGB）；自有金属如有"基色→铁色"渐变语义，按偏移比例取色
- `requiredModId`：非 null 时仅该 mod 装载才注册（如铬铁 = `"firmalife"`）

劣等金属改加在 [`metal/InferiorMetal.java`](../src/main/java/com/yukimods/alloyext/metal/InferiorMetal.java)（字段同构）。

### Step 2 · 资产（资源包）

模板：`src/main/resources/assets/tfc_alloy_ext/` 下已有 `solder`/`ferronickel` 全套可复制：

| 文件 | 路径 | 要点 |
|------|------|------|
| 流体块 blockstate | `blockstates/fluid/metal/{name}.json` | 引用 `tfc:block/fluid/metal/copper` 模型（颜色由 FluidType tint 决定） |
| 桶模型 | `models/item/metal/{name}_bucket.json` | `neoforge:fluid_container` loader，`fluid` 字段填 `tfc_alloy_ext:metal/{name}` |
| 锭模型 | `models/item/metal/ingot/{name}.json` | `item/generated` + 锭纹理 |
| 锭纹理 | `textures/item/metal/ingot/{name}.png` | 16×16；**多金属拼合纹理用脚本生成**（见下） |

**拼合纹理脚本**：[`scripts/gen_metal_textures.py`](../scripts/gen_metal_textures.py)（线性/函数拼接，用户定稿 2026-08-09）
- 对角拼合：左下 A 锭、右上 B 锭；像素色 = `LB·A + (1−LB)·B`，`LB = (X−Y+16)/32`
- 在 `SOURCES` 映射表加一行（A = 左下源锭、B = 右上源锭，路径相对 `.fork/`）
- 默认 `BLEND = blend_linear`；可切换 `blend_step5`（5 阶阶跃，f(0)=0、f(1)=1、中心对称）
- 运行 `python scripts/gen_metal_textures.py` 生成

### Step 3 · 语言文件

`assets/tfc_alloy_ext/lang/en_us.json` + `zh_cn.json`，各加 3 键：

```
fluid.tfc_alloy_ext.metal.{name}          → "Molten {Name}" / "熔融{中文名}"
item.tfc_alloy_ext.metal.ingot.{name}     → "{Name} Ingot" / "{中文名}锭"
item.tfc_alloy_ext.metal.{name}_bucket    → "Molten {Name} Bucket" / "熔融{中文名}桶"
```

### Step 4 · 数据（加热熔融 + 浇筑，参照焊锡 5 件套）

路径 `src/main/resources/data/`，数值参考铸铁/同族金属：

| 文件 | 路径 | 要点 |
|------|------|------|
| 熔融液熔点 | `tfc/tfc/fluid_heat/{name}.json` | `melt_temperature`（= 枚举 meltingTemp）、`specific_heat_capacity`（铸铁 0.008571429） |
| 锭锻造/焊接 | `tfc/tfc/item_heat/{name}/ingot.json` | forging/welding 温度 + heat_capacity（铸铁 921/1228/2.857 参考）；ingredient 用 `item`（自有金属无 c: tag 时） |
| 浇筑·锭模 | `tfc_alloy_ext/recipe/casting/{name}_ingot.json` | `tfc:casting`，`ceramic/ingot_mold`，break 0.1 |
| 浇筑·耐火锭模 | `tfc_alloy_ext/recipe/casting/{name}_fire_ingot.json` | `ceramic/fire_ingot_mold`，break 0.01 |
| 加热熔化 | `tfc_alloy_ext/recipe/heating/metal/ingot/{name}.json` | `tfc:heating`，锭 → 熔融液 100mB，`temperature` = 熔点 |

### Step 5 · 标签（参照焊锡）

| tag | 内容 |
|-----|------|
| `c/tags/fluid/molten_metal.json` | 追加 `tfc_alloy_ext:metal/{name}` + `metal/flowing_{name}` |
| `tfc/tags/fluid/molten_metals.json` | 追加 `tfc_alloy_ext:metal/{name}`（仅 source） |
| `c/tags/item/ingots/{name}.json` | 新建：锭物品 |

专用 tag（如焊锡的 `c:molten_solder`/`tfc_alloy_ext:solder_fluid`）仅在存在对应消费点时建（IE 合金炉污染配方、焊接逻辑等）。

### Step 6 · 合金配方（消费点，可选）

`data/tfc_alloy_ext/recipe/alloy/{name}.json`，`tfc:alloy` 类型：
- 组分比例按**成分守恒矩阵**设计（如不锈钢 = 镍铁 40% + 铬铁 31% + 铸铁 29%，解 x·镍铁+y·铬铁+z·铸铁=目标成分）
- 范围留容差（TFC 全量精确匹配：组分在 [min,max]、总和 100%、未列流体出现即失败）
- 依赖其他 mod 时加 `neoforge:conditions`（`neoforge:mod_loaded`；读配置用 `tfc_alloy_ext:config_enabled`，见下）

### Step 7 · 自动获得（无需代码）

枚举值追加后，以下自动生效：
- 注册：`RegularMetals`/`InferiorMetals` 静态块遍历（`isEnabled()` 过滤）
- 客户端流体颜色：`ModClientSetup` 遍历 `getRegistered()` 注册 `FluidRendererExtension`
- 创造标签页：`ModCreativeTab` 遍历输出锭 + 桶
- 查询 API：`getFluid/getIngot/getBucket/getBlock/getFluidByName`

---

## 3. 配置条件（可选）

自定义 `ICondition`（`condition/ConfigEnabledCondition`，注册于 `neoforge:condition_codecs`）：

```json
{ "type": "tfc_alloy_ext:config_enabled", "config": "enableIronInferiorSystem" }
```

- **时序约束**：条件在数据包重载时求值（KubeJS discoverRecipes 早于 SERVER 配置加载）——配置必须放 COMMON 类型（2026-08-09 踩坑：SERVER 类型读配置抛 IllegalStateException 导致进世界失败）
- `neoforge:conditions` 数组为 AND 语义，可并列多个条件

---

## 4. 验证清单

- [ ] 游戏启动无注册冲突；日志无配方/条件解析错误
- [ ] 创造标签页出现锭 + 桶；中文本地化正确
- [ ] 桶倒出熔融液、流体颜色正确
- [ ] 锭加热熔化（heating）→ 熔融液 → 铸造（casting）回锭 全链可跑
- [ ] 锭纹理拼接方向正确（左下 A / 右上 B 渐变）
- [ ] JEI 显示加热/铸造/合金配方；条件配方随配置联动
