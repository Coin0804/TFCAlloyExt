package com.yukimods.alloyext.metal;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 劣等金属定义 —— 集中管理名称、熔点等元数据。
 * <p>
 * 替代此前散落在各注册类中的硬编码字符串，统一从枚举派生注册 ID。
 * 注册 ID 的派生规则见 {@link MetalRegistration}。
 */
public enum InferiorMetal {

    // (name, meltingTemp°C, 流体颜色ARGB)
    // meltingTemp 与 data/tfc/tfc/fluid_heat/inferior_*.json 的 melt_temperature 对应（单一数据源）
    // color 为 TFC 原版 Metal.getColor() - 0x0F 的略暗色变体
    COPPER("copper", 1026, 0xFFA73118),
    TIN("tin", 219, 0xFF8195AC),
    ZINC("zinc", 399, 0xFFACACB5),
    BISMUTH("bismuth", 257, 0xFF394563),
    GOLD("gold", 1007, 0xFFCDB80C),
    SILVER("silver", 913, 0xFF858B86),
    NICKEL("nickel", 1380, 0xFF3FA22D);

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
