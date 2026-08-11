package com.yukimods.alloyext;

import com.mojang.serialization.MapCodec;
import com.yukimods.alloyext.client.ModCreativeTab;
import com.yukimods.alloyext.condition.ConfigEnabledCondition;
import com.yukimods.alloyext.config.ModConfig;
import com.yukimods.alloyext.metal.InferiorMetals;
import com.yukimods.alloyext.metal.InferiorOrigin;
import com.yukimods.alloyext.metal.RegularMetals;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 群峦合金扩展 —— 主类。
 * <ul>
 *   <li>11 种熔融劣等合金流体 + 方块（TFC 7 种 + IE Addon 铝/铅/铀 + Firmalife 铬，前置 mod 条件注册）</li>
 *   <li>自有金属（焊锡/镍铁/铬铁，铬铁需 firmalife）</li>
 *   <li>可配置阈值（默认75%）/ 污染扩散 / 黑箱沉没 合金逻辑</li>
 *   <li>PM 熔铸炉 TFC 集成（Mixin，可选依赖）</li>
 *   <li>劣等合金配方替代（DataComponent 标记）</li>
 * </ul>
 */
@Mod(TFCAlloyExt.MOD_ID)
public class TFCAlloyExt {

    public static final String MOD_ID = "tfc_alloy_ext";
    public static final Logger LOGGER = LoggerFactory.getLogger("TFC Alloy Extension");

    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD_ID);

    /** 配方条件 codec 注册表（neoforge:condition_codecs）—— 自定义条件在此注册 */
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITIONS =
            DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS, MOD_ID);

    /** 标记物品的劣等金属来源，如 "copper"。锻造/浇铸时写入，熔融时读取。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<InferiorOrigin>> INFERIOR_ORIGIN =
            DATA_COMPONENTS.register("inferior_origin",
                    () -> DataComponentType.<InferiorOrigin>builder()
                            .persistent(InferiorOrigin.CODEC)
                            .networkSynchronized(InferiorOrigin.STREAM_CODEC)
                            .build());

    /** 配置开关条件：配方 JSON 中 { "type": "tfc_alloy_ext:config_enabled", "config": "..." } */
    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigEnabledCondition>> CONFIG_ENABLED_CONDITION =
            CONDITIONS.register("config_enabled", () -> ConfigEnabledCondition.CODEC);

    public TFCAlloyExt(IEventBus modEventBus, ModContainer container) {
        LOGGER.info("TFC Alloy Extension 初始化");

        // 铁开关在 COMMON：配方条件在数据包重载时读取，早于 SERVER 配置加载（见 ModConfig 注释）
        container.registerConfig(Type.SERVER, ModConfig.SERVER_SPEC);
        container.registerConfig(Type.COMMON, ModConfig.COMMON_SPEC);

        DATA_COMPONENTS.register(modEventBus);
        CONDITIONS.register(modEventBus);

        // 劣等合金（11 种：TFC 7 种始终注册，铝/铅/铀与铬按前置 mod 条件——条件在 InferiorMetals 内部过滤）
        InferiorMetals.FLUID_TYPES.register(modEventBus);
        InferiorMetals.FLUIDS.register(modEventBus);
        InferiorMetals.BLOCKS.register(modEventBus);
        InferiorMetals.ITEMS.register(modEventBus);

        // 自有金属（焊锡/镍铁/铬铁）— 枚举驱动统一注册，前置 mod 条件在 RegularMetals 内部过滤
        RegularMetals.FLUID_TYPES.register(modEventBus);
        RegularMetals.FLUIDS.register(modEventBus);
        RegularMetals.BLOCKS.register(modEventBus);
        RegularMetals.ITEMS.register(modEventBus);

        // 创造模式标签页（桶/锭物品均注册于各金属注册类）
        ModCreativeTab.CREATIVE_TABS.register(modEventBus);

        LOGGER.info("已注册 11 种熔融劣等合金流体 + 自有金属（焊锡/镍铁/铬铁）+ 流体桶 + 液体方块 + 劣等溯源组件");
    }
}
