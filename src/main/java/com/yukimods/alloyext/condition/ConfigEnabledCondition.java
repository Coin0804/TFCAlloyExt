package com.yukimods.alloyext.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yukimods.alloyext.config.ModConfig;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * 配置开关配方条件 —— 读取 {@link ModConfig} 的布尔配置项，为 true 时配方才加载。
 * <p>
 * 与 neoforge:mod_loaded 等同属 {@link ICondition} 体系，codec 注册于
 * {@code neoforge:condition_codecs} 注册表（见 TFCAlloyExt.CONDITIONS）。
 * 用于让铁类修改配方随「铁劣等系统」开关整体启停。
 * <p>
 * 时序：配方条件在数据包加载（进入世界 / reload）时求值，晚于 mod 加载与
 * 配置读入，读取 {@link ModConfig} 安全。
 * <p>
 * JSON 用法：{ "type": "tfc_alloy_ext:config_enabled", "config": "enableIronInferiorSystem" }
 */
public record ConfigEnabledCondition(String configPath) implements ICondition {

    public static final MapCodec<ConfigEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("config").forGetter(ConfigEnabledCondition::configPath)
    ).apply(i, ConfigEnabledCondition::new));

    @Override
    public boolean test(IContext context) {
        // 配置路径 = ModConfig 中的 define 名（单一数据源，见 ModConfig）
        return switch (configPath) {
            case "enableIronInferiorSystem" -> ModConfig.ENABLE_IRON_INFERIOR_SYSTEM.get();
            default -> false;
        };
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
