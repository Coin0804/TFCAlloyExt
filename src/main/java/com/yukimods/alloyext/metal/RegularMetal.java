package com.yukimods.alloyext.metal;

import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

/**
 * 本模组自有金属（含锭物品的常规金属）枚举 —— 焊锡与铁合金的元数据。
 * <p>
 * 新金属在此追加枚举值即可获得统一注册工作流（{@link RegularMetals}）：
 * 熔融流体 + 方块 + 桶 + 锭物品 + 客户端颜色 + 创造标签页条目，全部由枚举驱动。
 * 与劣等金属的枚举模式（{@link InferiorMetal} + {@link InferiorMetals}）对齐。
 */
public enum RegularMetal {

    /** 焊锡（Sn-Pb-Bi 合金）—— 非劣等合金，TFC IE Addon 铅 + TFC 锡/铋 的合金产物 */
    SOLDER("solder", 183, 0xFFB8B8B8, null),

    /** 镍铁 FeNi（含镍 15-35%，粗）—— 原料 TFC 原版硅镁镍矿，始终注册 */
    FERRONICKEL("ferronickel", 1440, 0xFF525241, null),

    /** 铬铁 FeCr（高碳，粗）—— 原料 firmalife 铬铁矿，firmalife 存在时注册 */
    FERROCHROMIUM("ferrochromium", 1550, 0xFFF2FBFC, "firmalife");

    private final String name;
    private final int meltingTemp;
    private final int color;
    @Nullable
    private final String requiredModId;

    RegularMetal(String name, int meltingTemp, int color, @Nullable String requiredModId) {
        this.name = name;
        this.meltingTemp = meltingTemp;
        this.color = color;
        this.requiredModId = requiredModId;
    }

    /** 金属名（注册 ID 派生的唯一数据源） */
    public String getName() {
        return name;
    }

    /** 熔点 °C —— 贴近现实液相线（焊锡 183、镍铁 1430-1450、高碳铬铁约 1550） */
    public int getMeltingTemp() {
        return meltingTemp;
    }

    /** 流体颜色 —— 镍铁/铬铁为镍/铬流体色向铁色 0xFF989897 微偏移（5%/3%），焊锡为银灰 */
    public int getColor() {
        return color;
    }

    /** 前置 mod 缺失时不注册（null = 始终注册） */
    public boolean isEnabled() {
        return requiredModId == null || ModList.get().isLoaded(requiredModId);
    }
}
