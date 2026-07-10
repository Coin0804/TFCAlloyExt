package com.yukimods.alloyext;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 服务端配置 —— 坩埚倾倒速率等。
 */
public class ModConfig {

    public static final ModConfigSpec SERVER_SPEC;

    /** 坩埚每次 tick 传输量倍率，默认 4（即 4mB/tick，100mB 锭约 1.25 秒），范围 1~100 */
    public static final ModConfigSpec.IntValue CRUCIBLE_POUR_SPEED_MULTIPLIER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("crucible");
        CRUCIBLE_POUR_SPEED_MULTIPLIER = builder
                .comment("Multiplier for how much fluid (in mB) the crucible transfers per tick when pouring into molds or output slots.",
                        "Default TFC: 1mB/tick. Setting this to 4 means 4mB/tick, filling a 100mB ingot in ~1.25s instead of ~5s (fast pour) or ~20s (normal).")
                .defineInRange("pourSpeedMultiplier", 4, 1, 100);
        builder.pop();

        SERVER_SPEC = builder.build();
    }
}
