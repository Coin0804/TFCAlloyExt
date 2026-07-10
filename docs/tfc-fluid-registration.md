# TFC 流体 + 方块注册机制分析

## 核心问题

在 NeoForge 的 `DeferredRegister` 体系中，`DeferredHolder.get()` 不能在类静态初始化阶段调用 —— 此时注册表尚未构建，会抛出 `NullPointerException: Trying to access unbound value`。

注册熔融金属流体时存在鸡生蛋问题：
- 流体的 `BaseFlowingFluid.Properties` 需要引用一个 `LiquidBlock`（通过 `.block()`）
- `LiquidBlock` 构造函数需要 `FlowingFluid` 引用
- 两者都通过 `DeferredRegister` 注册，在静态初始化时都不可用

## TFC 的解决方案

### 1. MoltenFluidBlock —— 延迟 FlowingFluid 解析

TFC 使用 `MoltenFluidBlock`（继承 `LiquidBlock`），其构造函数接收 `Supplier<? extends FlowingFluid>`：

```java
public class MoltenFluidBlock extends LiquidBlock {
    public MoltenFluidBlock(Supplier<? extends FlowingFluid> supplier, Properties props) {
        super(supplier.get(), props); // get() 在构造函数中调用
    }
}
```

关键：`supplier.get()` 在 `MoltenFluidBlock` **构造函数**中被调用，该构造函数又被包在延迟执行的 lambda 中：

```java
// TFCBlocks.METAL_FLUIDS 条目
BLOCKS.register("fluid/metal/copper",
    () -> new MoltenFluidBlock(              // ← 注册表构建时才执行
        () -> TFCFluids.METALS.get(Metal.COPPER).getFlowing(),  // ← getFlowing() 在 MoltenFluidBlock 构造时才调用
        props));
```

执行时间线：
1. **类加载阶段**：`BLOCKS.register(name, supplier)` — 只存储 supplier，不执行
2. **注册表构建阶段**：supplier 执行 → `new MoltenFluidBlock(...)` → `supplier.get()` → `getFlowing()` → `DeferredHolder.get()` — 此时注册表已准备好

### 2. 类加载顺序 —— 避免循环依赖

TFC 使用 **Map + Enum key** 而非**独立静态字段**：

```java
// TFCFluids: 所有流体存在一个 Map 中
public static final Map<Metal, FluidHolder<BaseFlowingFluid>> METALS;

// TFCBlocks: 通过 Metal enum 引用
public static final Map<Metal, Id<LiquidBlock>> METAL_FLUIDS;
```

这样 TFCBlocks 在静态初始化时不需要访问 `TFCFluids.COPPER` 这种独立字段，只需通过 `Metal.COPPER`（enum 常量，始终可用）作为 key 查找 map。

### 3. 本模组的实现

类加载顺序（在 `TFCAlloyExt` 构造函数中）：
1. `ModBlocks.BLOCKS.register(modEventBus)` → ModBlocks 类加载
2. `InferiorMetalFluids.FLUID_TYPES.register(modEventBus)` → InferiorMetalFluids 类加载

ModBlocks 的静态字段引用 `InferiorMetalFluids.getInferiorFluid(name)`，
但调用发生在延迟 lambda 内（注册表构建时），不在类加载阶段。

InferiorMetalFluids 通过 `ModBlocks.getBlockSupplier(name)` 懒查找方块，
但由于 ModBlocks 已先加载，此调用在注册表构建时是安全的。

### 4. 本模组的 InferiorMetalFluids.getBaseMetalFromInferior()

也不能在静态初始化时调用 `FluidHolder.getSource()` / `getFlowing()`，
改为运行时遍历 `INFERIOR_FLUIDS` map 并用 `==` 比较 Fluid 引用：

```java
public static String getBaseMetalFromInferior(Fluid fluid) {
    for (var entry : INFERIOR_FLUIDS.entrySet()) {
        var holder = entry.getValue();
        if (holder.getSource() == fluid || holder.getFlowing() == fluid) {
            return entry.getKey();
        }
    }
    return null;
}
```

## 总结：安全规则

| 操作 | 类加载阶段 | 注册表构建阶段 | 游戏运行阶段 |
|------|:---:|:---:|:---:|
| `DeferredRegister.register(name, supplier)` | ✅ | ✅ | ❌ |
| `DeferredHolder.get()` | ❌ NPE | ✅ | ✅ |
| `new FluidHolder(...)` 存储 | ✅ | — | — |
| `FluidHolder.getSource()` / `getFlowing()` | ❌ NPE | ✅ | ✅ |
| `new BaseFlowingFluid.Properties(...).block(supplier)` | ✅ 只存 supplier | ✅ | — |
