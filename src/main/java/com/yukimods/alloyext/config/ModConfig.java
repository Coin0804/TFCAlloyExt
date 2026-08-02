package com.yukimods.alloyext.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 服务端配置 —— 坩埚倾倒速率等。
 */
public class ModConfig {

    public static final ModConfigSpec SERVER_SPEC;

    /** 坩埚每次 tick 传输量倍率，默认 4（即 4mB/tick，100mB 锭约 1.25 秒），范围 1~100 */
    public static final ModConfigSpec.IntValue CRUCIBLE_POUR_SPEED_MULTIPLIER;

    /**
     * 劣等合金阈值：混合物中最高占比金属需 ≥ 此值才会生成劣等合金（而非 UNKNOWN）。
     * 默认 0.75（75%），范围 0.50~1.00。
     */
    public static final ModConfigSpec.DoubleValue INFERIOR_ALLOY_THRESHOLD;

    /**
     * 铁劣等合金系统开关。
     * 开启后，坩埚中锻铁 (wrought_iron) 被识别为纯铁，
     * 占比达阈值时产出铸铁 (cast_iron) 作为劣等变体。
     */
    public static final ModConfigSpec.BooleanValue ENABLE_IRON_INFERIOR_SYSTEM;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("crucible");
        CRUCIBLE_POUR_SPEED_MULTIPLIER = builder
                .comment("Multiplier for how much fluid (in mB) the crucible transfers per tick when pouring into molds or output slots.",
                        "Default TFC: 1mB/tick. Setting this to 4 means 4mB/tick, filling a 100mB ingot in ~1.25s instead of ~5s (fast pour) or ~20s (normal).")
                .defineInRange("pourSpeedMultiplier", 4, 1, 100);
        builder.pop();

        builder.push("alloy");
        INFERIOR_ALLOY_THRESHOLD = builder
                .comment("Minimum fraction of the dominant metal in a crucible mix required to produce an inferior alloy instead of Unknown.",
                        "Range: 0.50 (50%) to 1.00 (100%). Default: 0.75 (75%).",
                        "Lower values = more forgiving (more inferior alloys, less waste).",
                        "Higher values = stricter (less inferior alloys, more Unknown/waste).")
                .defineInRange("inferiorAlloyThreshold", 0.75, 0.50, 1.00);
        ENABLE_IRON_INFERIOR_SYSTEM = builder
                .comment("Enable the iron inferior alloy system.",
                        "When true, wrought_iron in an impure crucible mix produces cast_iron as the inferior output.",
                        "No new fluids/items are registered; uses existing TFC cast_iron and wrought_iron.",
                        "Default: false (disabled).")
                .define("enableIronInferiorSystem", false);
        builder.pop();

        SERVER_SPEC = builder.build();
    }
}
