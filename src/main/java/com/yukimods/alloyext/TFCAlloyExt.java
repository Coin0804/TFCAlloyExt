package com.yukimods.alloyext;

import com.yukimods.alloyext.fluid.InferiorMetalFluids;
import com.yukimods.alloyext.item.ModItems;
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
 *   <li>7 种熔融劣等合金流体 + 方块 + 合金锭</li>
 *   <li>55% 阈值 / 污染扩散 / 黑箱沉没 合金逻辑</li>
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

        // 先触发 InferiorMetalFluids 类加载（ModBlocks 依赖其 BLOCKS DeferredRegister）
        InferiorMetalFluids.FLUID_TYPES.register(modEventBus);
        InferiorMetalFluids.FLUIDS.register(modEventBus);
        InferiorMetalFluids.BLOCKS.register(modEventBus);

        // ModBlocks.BLOCKS 是 InferiorMetalFluids.BLOCKS 的别名，无需重复注册
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTab.CREATIVE_TABS.register(modEventBus);

        LOGGER.info("已注册 7 种熔融劣等合金流体 + 7 种合金锭 + 7 种流体桶 + 液体方块 + 劣等溯源组件");
    }
}
