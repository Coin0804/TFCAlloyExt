package com.yukimods.alloyext.metal;

import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 劣等金属定义 —— 集中管理名称、熔点、颜色与前置 mod 条件。
 * <p>
 * 统一注册工作流（{@link InferiorMetals}）：枚举值在静态初始化时按
 * {@link #isEnabled()} 过滤后注册（TFC 7 种始终注册；铝/铅/铀需
 * tfc_ie_addon；铬需 firmalife），一个循环完成全部注册。
 * 注册 ID 的派生规则见群峦扩展 API 的 MetalRegistration（Lib）。
 */
public enum InferiorMetal {

    // (name, meltingTemp°C, 流体颜色ARGB, 前置modId null=始终注册)
    // meltingTemp 与 data/tfc/tfc/fluid_heat/inferior_*.json 的 melt_temperature 对应（单一数据源）
    // color 为 TFC 原版 Metal.getColor() - 0x0F 的略暗色变体
    COPPER("copper", 1026, 0xFFA73118, null),
    TIN("tin", 219, 0xFF8195AC, null),
    ZINC("zinc", 399, 0xFFACACB5, null),
    BISMUTH("bismuth", 257, 0xFF394563, null),
    GOLD("gold", 1007, 0xFFCDB80C, null),
    SILVER("silver", 913, 0xFF858B86, null),
    NICKEL("nickel", 1380, 0xFF3FA22D, null),

    // IE Addon 金属（tfc_ie_addon 存在时注册）
    ALUMINUM("aluminum", 1653, 0xFFC8D6E0, "tfc_ie_addon"),
    LEAD("lead", 314, 0xFF5A5A6E, "tfc_ie_addon"),
    URANIUM("uranium", 1074, 0xFF4A5E3C, "tfc_ie_addon"),

    // Firmalife 金属（firmalife 存在时注册）
    CHROMIUM("chromium", 1188, 0xFFE6E7F0, "firmalife");

    private final String name;
    private final int meltingTemp;
    private final int color;
    @Nullable
    private final String requiredModId;

    InferiorMetal(String name, int meltingTemp, int color, @Nullable String requiredModId) {
        this.name = name;
        this.meltingTemp = meltingTemp;
        this.color = color;
        this.requiredModId = requiredModId;
    }

    /** 金属基础名称，如 "copper" */
    public String getName() {
        return name;
    }

    /** 金属熔点（摄氏度） */
    public int getMeltingTemp() {
        return meltingTemp;
    }

    /** 流体 tint 颜色（RRGGBB），配合 lava-like 渲染使用 */
    public int getColor() {
        return color;
    }

    /** 前置 mod 缺失时不注册（null = 始终注册） */
    public boolean isEnabled() {
        return requiredModId == null || ModList.get().isLoaded(requiredModId);
    }

    // ─── 静态工具 ──────────────────────────────────────────

    /** 所有枚举值的数组，避免重复调用 values() */
    public static final InferiorMetal[] VALUES = values();

    /** 金属名称集合（保持插入顺序），替代 InferiorAlloyLogic 中硬编码的 PURE_METALS */
    public static final Set<String> NAMES =
            Collections.unmodifiableSet(new LinkedHashSet<>(
                    Arrays.stream(VALUES)
                            .map(InferiorMetal::getName)
                            .toList()));

    /** 按名称查找枚举（未命中返回 null） */
    @Nullable
    public static InferiorMetal getByName(String name) {
        for (InferiorMetal metal : VALUES) {
            if (metal.getName().equals(name)) {
                return metal;
            }
        }
        return null;
    }
}
