package com.yukimods.alloyext.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 模组配置 —— 拆分为 SERVER + COMMON 两个 ModConfigSpec。
 * <p>
 * <b>为什么铁开关单独放 COMMON</b>（时序原因，2026-08-09 踩坑）：
 * 配方条件（{@code ConfigEnabledCondition}）在数据包重载时求值——KubeJS
 * discoverRecipes 的触发早于 SERVER 配置加载（ModConfigEvent.Loading），
 * SERVER 类型读配置会抛 IllegalStateException 导致进世界失败。COMMON
 * 配置在 mod 加载早期读入，早于任何数据包重载，条件读取安全。
 */
public class ModConfig {

    /** 服务端配置：坩埚倾倒速率、劣等合金阈值、劣等工具耐久减益 */
    public static final ModConfigSpec SERVER_SPEC;

    /** 公共配置：铁劣等系统开关（配方条件读取，需早于数据包重载加载） */
    public static final ModConfigSpec COMMON_SPEC;

    /** 坩埚每次 tick 传输量倍率，默认 4（即 4mB/tick，100mB 锭约 1.25 秒），范围 1~100 */
    public static final ModConfigSpec.IntValue CRUCIBLE_POUR_SPEED_MULTIPLIER;

    /**
     * 劣等合金阈值：混合物中最高占比金属需 ≥ 此值才会生成劣等合金（而非 UNKNOWN）。
     * 默认 0.75（75%），范围 0.50~1.00。
     */
    public static final ModConfigSpec.DoubleValue INFERIOR_ALLOY_THRESHOLD;

    /**
     * 劣等合金工具耐久减益：劣等工具头合成的可损伤产物（工具/鱼竿），
     * 出生即损失该比例的最大耐久。默认 0.10（出生 90% 耐久），范围 0~0.90。
     */
    public static final ModConfigSpec.DoubleValue INFERIOR_TOOL_DURABILITY_PENALTY;

    /**
     * 铁劣等合金系统开关（COMMON —— 配方条件读取，需早于数据包重载加载）。
     * 开启后，坩埚中锻铁 (wrought_iron) 被识别为纯铁，
     * 占比达阈值时产出铸铁 (cast_iron) 作为劣等变体；
     * 同时加载 39 个锻铁加热配方（铁类修改，条件见 ConfigEnabledCondition）。
     */
    public static final ModConfigSpec.BooleanValue ENABLE_IRON_INFERIOR_SYSTEM;

    static {
        ModConfigSpec.Builder server = new ModConfigSpec.Builder();

        server.push("crucible");
        CRUCIBLE_POUR_SPEED_MULTIPLIER = server
                .comment("Multiplier for how much fluid (in mB) the crucible transfers per tick when pouring into molds or output slots.",
                        "Default TFC: 1mB/tick. Setting this to 4 means 4mB/tick, filling a 100mB ingot in ~1.25s instead of ~5s (fast pour) or ~20s (normal).")
                .defineInRange("pourSpeedMultiplier", 4, 1, 100);
        server.pop();

        server.push("alloy");
        INFERIOR_ALLOY_THRESHOLD = server
                .comment("Minimum fraction of the dominant metal in a crucible mix required to produce an inferior alloy instead of Unknown.",
                        "Range: 0.50 (50%) to 1.00 (100%). Default: 0.75 (75%).",
                        "Lower values = more forgiving (more inferior alloys, less waste).",
                        "Higher values = stricter (less inferior alloys, more Unknown/waste).")
                .defineInRange("inferiorAlloyThreshold", 0.75, 0.50, 1.00);
        INFERIOR_TOOL_DURABILITY_PENALTY = server
                .comment("Durability penalty for tools crafted from inferior alloy heads.",
                        "The crafted tool starts with this fraction of its max durability already damaged.",
                        "TFC has no tool repair recipes, so the penalty lasts the tool's whole life.",
                        "Range: 0 (no penalty) to 0.90. Default: 0.10 (start at 90% durability).")
                .defineInRange("inferiorToolDurabilityPenalty", 0.10, 0.0, 0.90);
        server.pop();

        SERVER_SPEC = server.build();

        ModConfigSpec.Builder common = new ModConfigSpec.Builder();
        common.push("alloy");
        ENABLE_IRON_INFERIOR_SYSTEM = common
                .comment("Enable the iron inferior alloy system. Stored in COMMON config because recipe conditions",
                        "(tfc_alloy_ext:config_enabled) read it during datapack reload, which happens before SERVER config loads.",
                        "When true, wrought_iron in an impure crucible mix produces cast_iron as the inferior output,",
                        "and the 39 wrought_iron heating recipes (iron-line modifications) are loaded.",
                        "No new fluids/items are registered; uses existing TFC cast_iron and wrought_iron.",
                        "Default: false (disabled).")
                .define("enableIronInferiorSystem", false);
        common.pop();

        COMMON_SPEC = common.build();
    }
}
