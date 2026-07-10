# 群峦金属扩展模组 — 实现方案 v3

**独立发布模组**，全部功能在模组内部实现，不依赖 KubeJS。

---

## 命名规范

| 概念 | 中文 | 注册 ID |
|------|------|---------|
| 流体 | 熔融劣等铜合金 | `modid:metal/inferior_copper` |
| 锭 | 劣等铜合金锭 | `modid:inferior_copper_ingot` |
| 方块 | — | — |

---

## 一、模组架构

```
src/main/
├── java/com/xxx/modid/
│   ├── ModMain.java                    # 主类 + 注册总线
│   ├── fluid/
│   │   ├── InferiorMetalFluids.java    # 7 种熔融劣等合金流体注册
│   │   └── InferiorAlloyLogic.java     # 55%阈值 + 污染扩散 + 黑箱
│   ├── item/
│   │   └── ModItems.java              # 7 种劣等合金锭注册
│   └── mixin/
│       ├── FluidAlloyMixin.java        # [A] TFC getResult() 注入劣等逻辑
│       └── FoundryTapMixin.java        # [B] PM 龙头 TFC 合金
│
└── resources/
    ├── modid.mixins.json
    ├── META-INF/neoforge.mods.toml
    ├── assets/modid/
    │   ├── lang/zh_cn.json
    │   ├── blockstates/fluid/metal/inferior_*.json  ×7
    │   ├── models/item/inferior_*_ingot.json         ×7
    │   └── textures/...
    └── data/modid/
        ├── recipe/
        │   ├── casting/              # 浇铸：熔融劣等合金 → 劣等合金锭 ×7
        │   └── heating/              # 熔化：劣等合金锭 → 熔融劣等合金 ×7
        ├── tfc/
        │   └── item_heat/            # 劣等合金锭 heat 定义 ×7
        └── tags/item/                # 劣等合金锭标签
```

---

## 二、依赖

| 依赖 | 类型 |
|------|------|
| TFC 4.1.3 | required — 流体基类 MoltenFluid + 合金系统 |
| Productive Metalworks 1.15.0 | optional — 功能 B 需要 |

---

## 三、核心实现

### 3.1 流体注册

`InferiorMetalFluids.java` — 用 `DeferredRegister` 注册 7 种熔融劣等合金流体：

```java
public class InferiorMetalFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
        DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, MODID);
    public static final DeferredRegister<Fluid> FLUIDS =
        DeferredRegister.create(Registries.FLUID, MODID);

    public static final FluidHolder<BaseFlowingFluid> INFERIOR_COPPER =
        register("inferior_copper", 0xFF_FF8B45); // 橙铜色，比纯铜暗
    // ... ×7

    private static FluidHolder<BaseFlowingFluid> register(String name, int color) {
        // FluidType: lava-like (density=3000, viscosity=6000, temperature=1300, light=15)
        // MoltenFluid.Source + MoltenFluid.Flowing
    }

    // 工具方法
    public static boolean isInferiorFluid(Fluid f);
    public static Fluid getInferiorFluid(String baseMetal);    // "copper" → 劣等铜流体
    public static String getBaseMetalFromInferior(Fluid f);     // 反向提取
}
```

MoltenFluid 继承关系：
```
BaseFlowingFluid
  └── MoltenFluid (TFC 提供，lava-like 粒子 + 发光)
       ├── Source (静止)
       └── Flowing (流动)
```

### 3.2 物品注册

`ModItems.java`：

```java
public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(Registries.ITEM, MODID);

    // 7 种劣等合金锭
    public static final DeferredHolder<Item, Item> INFERIOR_COPPER_INGOT =
        ITEMS.register("inferior_copper_ingot", () -> new Item(new Item.Properties()));
    // ... ×7
}
```

### 3.3 合金逻辑（核心）

`InferiorAlloyLogic.java`：独立的静态逻辑类，与 Mixin 解耦，可被两处注入点共用。

```java
public class InferiorAlloyLogic {
    // TFC 单质金属 — 只有这 7 种能产生劣等变体
    private static final Set<String> PURE_METALS = Set.of(
        "copper", "tin", "zinc", "bismuth", "gold", "silver", "nickel"
    );

    /**
     * 解析流体混合物。
     * @return 如果应替换为劣等合金，返回新 FluidStack；否则返回 null（保持原样）
     */
    @Nullable
    public static FluidStack resolve(FluidAlloy alloy) { ... }
}
```

逻辑流程见下文 Mixin 注入点。

### 3.4 Mixin A：FluidAlloyMixin（劣等合金核心）

```java
@Mixin(value = FluidAlloy.class, remap = false)
public abstract class FluidAlloyMixin {

    @Inject(method = "getResult(Lnet/minecraft/world/item/crafting/RecipeManager;)" +
                      "Lnet/neoforged/neoforge/fluids/FluidStack;",
            at = @At("RETURN"), cancellable = true)
    private void modid$onGetResult(RecipeManager rm, CallbackInfoReturnable<FluidStack> cir) {
        FluidStack result = cir.getReturnValue();
        // 只拦截 UNKNOWN
        Fluid unknown = TFCFluids.METALS.get(Metal.UNKNOWN).getSource();
        if (result.getFluid() != unknown) return;

        FluidAlloy self = (FluidAlloy)(Object)this;
        FluidStack inferior = InferiorAlloyLogic.resolve(self);
        if (inferior != null) cir.setReturnValue(inferior);
    }
}
```

逻辑流程：
```
getResult(RecipeManager)
  ├─ content.size() == 0 → EMPTY (不变)
  ├─ content.size() == 1 → 返回该流体 (不变)
  └─ content.size() >= 2
       ├─ AlloyRecipe 匹配 → 正常合金 (不变)
       └─ UNKNOWN → [我们的注入点]
            ├─ 劣等流体存在？
            │   ├─ 劣等X + 纯X → 熔融劣等X合金 (污染扩散)
            │   └─ 劣等X + 异族/劣等Y → UNKNOWN (黑箱沉没)
            └─ 全纯金属？
                ├─ 最高占比 ≥ 55% → 熔融劣等X合金
                └─ 最高占比 < 55% → UNKNOWN (不变)
```

### 3.5 Mixin B：FoundryTapMixin（PM 集成）

```java
@Mixin(value = FoundryTapBlockEntity.class, remap = false)
public abstract class FoundryTapMixin {
    // 在龙头浇铸逻辑中注入 TFC 合金计算
    // 具体注入点需在 IDEA 中反编译确认 FoundryTapBlockEntity.serverTick 完整逻辑
}
```

---

## 四、Datapack 资源（配方 + 标签）

所有配方以 JSON 形式打包在 `data/modid/` 下。

### 4.1 浇铸配方（7 个）

`data/modid/recipe/casting/inferior_copper_ingot.json`：
```json
{
  "type": "tfc:casting",
  "mold": { "item": "tfc:ceramic/ingot_mold" },
  "fluid": { "fluid": "modid:metal/inferior_copper", "amount": 100 },
  "result": { "item": "modid:inferior_copper_ingot" },
  "break_chance": 0.1
}
```

### 4.2 熔化配方（7 个）

`data/modid/recipe/heating/inferior_copper_ingot.json`：
```json
{
  "type": "tfc:heating",
  "ingredient": { "item": "modid:inferior_copper_ingot" },
  "result_fluid": { "fluid": "modid:metal/inferior_copper", "amount": 100 },
  "temperature": 1084
}
```

### 4.3 Heat 定义（7 个）

`data/modid/tfc/item_heat/inferior_copper_ingot.json`：
```json
{
  "ingredient": { "item": "modid:inferior_copper_ingot" },
  "heat_capacity": 0.5
}
```

### 4.4 PM 熔化配方（7+ 个，功能 B）

`data/modid/productivemetalworks/recipe/melting/tfc_copper_ingot.json`：
```json
{
  "type": "productivemetalworks:item_melting",
  "ingredient": { "item": "tfc:metal/ingot/copper" },
  "minimum_temperature": 1084,
  "maximum_temperature": 0,
  "result": [{ "amount": 100, "id": "tfc:metal/copper" }]
}
```

同样为劣等合金锭添加回收配方（熔融劣等 → 劣等锭在 PM 铸造台）。

### 4.5 标签

`data/modid/tags/item/inferior_alloy_ingots.json`：
```json
{
  "values": [
    "modid:inferior_copper_ingot",
    "modid:inferior_tin_ingot",
    ...
  ]
}
```

---

## 五、资源文件

### 流体 Blockstate（×7）

`assets/modid/blockstates/fluid/metal/inferior_copper.json`：
```json
{
  "variants": {
    "": { "model": "modid:block/fluid/inferior_copper" }
  }
}
```

### 流体模型 + 纹理

复用 TFC 金属流体的模型结构，用 darker/tinted 纹理区分劣等金属。

### 语言文件

`assets/modid/lang/zh_cn.json`：
```json
{
  "block.modid.fluid.metal.inferior_copper": "熔融劣等铜合金",
  "item.modid.inferior_copper_ingot": "劣等铜合金锭"
}
```

---

## 六、执行顺序

### 第一阶段：模组骨架（1-2 hr）
1. 创建 `.projectmods/` 新目录 + build.gradle（复用 BackToTFCCore 模板）
2. `ModMain.java` — 主类 + 事件总线
3. `modid.mixins.json` + `neoforge.mods.toml`

### 第二阶段：流体 + 物品 + 材质（1-2 hr）
4. `InferiorMetalFluids.java` — 7 种流体
5. `ModItems.java` — 7 种锭
6. 全部 datapack JSON（浇铸/熔化/heat/tag/PM 配方）
7. 资源文件（blockstate/模型/纹理/语言文件）

### 第三阶段：核心 Mixin（2-3 hr）
8. `InferiorAlloyLogic.java` — 合金逻辑
9. `FluidAlloyMixin.java` — TFC getResult 注入
10. 编译 → 放入 mods 目录 → 启动游戏测试
11. 在 TFC 坩埚中验证全部劣等合金场景

### 第四阶段：PM 集成（1-2 hr）
12. `FoundryTapMixin.java` — PM 龙头注入
13. PM 熔化配方 JSON
14. 编译测试 — 熔铸炉 + TFC 金属联动

---

## 七、测试清单

- [ ] TFC 坩埚：55% Cu + 45% Sn → drain → 熔融劣等铜合金
- [ ] TFC 坩埚：45% Cu + 55% Sn → drain → 熔融劣等锡合金
- [ ] TFC 坩埚：50% Cu + 50% Sn → 未知金属
- [ ] TFC 坩埚：88% Cu + 12% Sn → 青铜（不受影响）
- [ ] TFC 坩埚：熔融劣等铜合金 + 纯铜 → 熔融劣等铜合金（污染扩散）
- [ ] TFC 坩埚：熔融劣等铜合金 + 纯锡 → 未知金属（黑箱）
- [ ] TFC 坩埚：60% Cu + 25% Sn + 15% Zn → 熔融劣等铜合金（三元≥55%）
- [ ] 浇铸：熔融劣等铜合金 + 锭模具 → 劣等铜合金锭
- [ ] 熔化：劣等铜合金锭 → 篝火/锻铁炉 → 熔融劣等铜合金
- [ ] PM 熔铸炉：TFC 铜锭 → 熔化 → 铜流体 → 浇铸 → TFC 合金判断
- [ ] 配方替代：劣等铜合金锭 → 铜块（datapack recipe）
