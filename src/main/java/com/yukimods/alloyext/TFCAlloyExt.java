package com.yukimods.alloyext;

import com.yukimods.alloyext.fluid.InferiorAddonMetals;
import com.yukimods.alloyext.fluid.InferiorMetalFluids;
import com.yukimods.alloyext.metal.SolderMetal;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 群峦合金扩展 —— 主类。
 * <ul>
 *   <li>7 种熔融劣等合金流体 + 方块</li>
 *   <li>3 种 IE Addon 劣等金属（铝/铅/铀，需 tfc_ie_addon）</li>
 *   <li>可配置阈值（默认75%）/ 污染扩散 / 黑箱沉没 合金逻辑</li>
 *   <li>焊锡金属（Sn-Pb-Bi 合金，非劣等）</li>
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

    /** 标记物品的劣等金属来源，如 "copper"。锻造/浇铸时写入，熔融时读取。 */
    public static final net.neoforged.neoforge.registries.DeferredHolder<DataComponentType<?>, DataComponentType<InferiorOrigin>> INFERIOR_ORIGIN =
            DATA_COMPONENTS.register("inferior_origin",
                    () -> DataComponentType.<InferiorOrigin>builder()
                            .persistent(InferiorOrigin.CODEC)
                            .networkSynchronized(InferiorOrigin.STREAM_CODEC)
                            .build());

    public TFCAlloyExt(IEventBus modEventBus, ModContainer container) {
        LOGGER.info("TFC Alloy Extension 初始化");

        container.registerConfig(Type.SERVER, ModConfig.SERVER_SPEC);

        DATA_COMPONENTS.register(modEventBus);

        // TFC 劣等合金（7 种，始终注册）
        InferiorMetalFluids.FLUID_TYPES.register(modEventBus);
        InferiorMetalFluids.FLUIDS.register(modEventBus);
        InferiorMetalFluids.BLOCKS.register(modEventBus);
        InferiorMetalFluids.ITEMS.register(modEventBus);

        // IE Addon 劣等合金（3 种：铝/铅/铀，仅当 tfc_ie_addon 存在时注册）
        if (InferiorAddonMetals.isEnabled()) {
            InferiorAddonMetals.FLUID_TYPES.register(modEventBus);
            InferiorAddonMetals.FLUIDS.register(modEventBus);
            InferiorAddonMetals.BLOCKS.register(modEventBus);
            InferiorAddonMetals.ITEMS.register(modEventBus);
            LOGGER.info("已激活 IE Addon 劣等金属：铝、铅、铀");
        }

        // 焊锡金属 — 非劣等合金
        SolderMetal.FLUID_TYPES.register(modEventBus);
        SolderMetal.FLUIDS.register(modEventBus);
        SolderMetal.BLOCKS.register(modEventBus);
        SolderMetal.ITEMS.register(modEventBus);

        // ModItems 目前无物品（桶已迁移至各流体注册类），保留占位
        ModCreativeTab.CREATIVE_TABS.register(modEventBus);

        LOGGER.info("已注册 7+3 种熔融劣等合金流体 + 焊锡金属 + 7 种流体桶 + 液体方块 + 劣等溯源组件");
    }
}
