package com.yukimods.alloyext;

import com.yukimods.alloyext.fluid.InferiorAddonMetals;
import com.yukimods.alloyext.fluid.InferiorFirmalifeMetals;
import com.yukimods.alloyext.fluid.InferiorMetalFluids;
import com.yukimods.alloyext.metal.SolderMetal;
import net.dries007.tfc.client.ClientEventHandler;
import net.dries007.tfc.client.extensions.FluidRendererExtension;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.model.DynamicFluidContainerModel;

/**
 * 客户端设置 — 用 TFC 同款机制注册流体颜色和桶贴图。
 */
@EventBusSubscriber(modid = TFCAlloyExt.MOD_ID, value = Dist.CLIENT)
public class ModClientSetup {

    /** 焊锡颜色：银灰色 */
    private static final int SOLDER_COLOR = 0xFFB8B8B8;

    @SubscribeEvent
    static void registerExtensions(RegisterClientExtensionsEvent event) {
        // 劣等合金流体
        for (InferiorMetal metal : InferiorMetal.VALUES) {
            var holder = InferiorMetalFluids.getInferiorFluid(metal.getName());
            if (holder == null) continue;
            event.registerFluidType(
                    new FluidRendererExtension(metal.getColor(),
                            ClientEventHandler.MOLTEN_STILL, ClientEventHandler.MOLTEN_FLOW,
                            null, null),
                    holder.getType());
        }

        // IE Addon 劣等金属（仅当 tfc_ie_addon 存在时）
        if (InferiorAddonMetals.isEnabled()) {
            for (var metal : InferiorAddonMetals.ALL) {
                var holder = InferiorAddonMetals.getFluid(metal.name());
                if (holder == null) continue;
                event.registerFluidType(
                        new FluidRendererExtension(metal.color(),
                                ClientEventHandler.MOLTEN_STILL, ClientEventHandler.MOLTEN_FLOW,
                                null, null),
                        holder.getType());
            }
        }

        // Firmalife 劣等金属（仅当 firmalife 存在时）
        if (InferiorFirmalifeMetals.isEnabled()) {
            var holder = InferiorFirmalifeMetals.getFluid("chromium");
            if (holder != null) {
                event.registerFluidType(
                        new FluidRendererExtension(InferiorFirmalifeMetals.getColor("chromium"),
                                ClientEventHandler.MOLTEN_STILL, ClientEventHandler.MOLTEN_FLOW,
                                null, null),
                        holder.getType());
            }
        }

        // 焊锡流体
        event.registerFluidType(
                new FluidRendererExtension(SOLDER_COLOR,
                        ClientEventHandler.MOLTEN_STILL, ClientEventHandler.MOLTEN_FLOW,
                        null, null),
                SolderMetal.FLUID.getType());
    }

    /**
     * 为所有本模组流体桶注册 ItemColor，确保桶内流体显示正确 tint 颜色。
     * 与 TFC 的 {@code registerColorHandlerItems} 完全一致的模式：
     * 遍历注册表 → 过滤命名空间 → {@code fluid.getBucket()} 拿桶 → 注册 ItemColor。
     */
    @SubscribeEvent
    static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        var colors = new DynamicFluidContainerModel.Colors();

        for (var fluid : BuiltInRegistries.FLUID) {
            var key = BuiltInRegistries.FLUID.getKey(fluid);
            if (key == null || !TFCAlloyExt.MOD_ID.equals(key.getNamespace())) continue;

            var bucket = fluid.getBucket();
            if (bucket != null) {
                event.register(colors, bucket);
            }
        }
    }
}
