package com.yukimods.alloyext;

import java.util.*;

/**
 * 劣等金属定义 —— 集中管理名称、熔点等元数据。
 * <p>
 * 替代此前散落在 ModItems、ModBlocks、InferiorMetalFluids、InferiorAlloyLogic
 * 中的硬编码字符串，统一从枚举派生注册 ID。
 */
public enum InferiorMetal {

    COPPER("copper", 1084, 0xFFA73118),
    TIN("tin", 232, 0xFF8195AC),
    ZINC("zinc", 420, 0xFFACACB5),
    BISMUTH("bismuth", 271, 0xFF394563),
    GOLD("gold", 1063, 0xFFCDB80C),
    SILVER("silver", 961, 0xFF858B86),
    NICKEL("nickel", 1455, 0xFF3FA22D);

    private final String name;
    private final int meltingTemp;
    private final int color;

    InferiorMetal(String name, int meltingTemp, int color) {
        this.name = name;
        this.meltingTemp = meltingTemp;
        this.color = color;
    }

    /** 金属基础名称，如 "copper" */
    public String getName() { return name; }

    /** 金属熔点（摄氏度） */
    public int getMeltingTemp() { return meltingTemp; }

    /** 流体 tint 颜色（RRGGBB），配合 lava-like 渲染使用 */
    public int getColor() { return color; }

    // ─── 注册 ID 派生 ──────────────────────────────────────

    /** 静止流体 ID，如 "metal/inferior_copper" */
    public String getFluidId() { return "metal/inferior_" + name; }

    /** 流动流体 ID，如 "metal/flowing_inferior_copper" */
    public String getFlowingFluidId() { return "metal/flowing_inferior_" + name; }

    /** 流体方块 ID，如 "fluid/metal/inferior_copper" */
    public String getBlockId() { return "fluid/metal/inferior_" + name; }

    /** 合金锭物品 ID，如 "inferior_copper_ingot" */
    public String getIngotId() { return "inferior_" + name + "_ingot"; }

    /** 流体桶物品 ID，如 "inferior_copper_bucket" */
    public String getBucketId() { return "inferior_" + name + "_bucket"; }

    // ─── 静态工具 ──────────────────────────────────────────

    /** 所有枚举值的数组，避免重复调用 values() */
    public static final InferiorMetal[] VALUES = values();

    /** 金属名称集合（保持插入顺序），替代 InferiorAlloyLogic 中硬编码的 PURE_METALS */
    public static final Set<String> NAMES =
            Collections.unmodifiableSet(new LinkedHashSet<>(
                    Arrays.stream(VALUES)
                            .map(InferiorMetal::getName)
                            .toList()));
}
