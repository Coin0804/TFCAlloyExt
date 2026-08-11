package com.yukimods.alloyext.client;

import com.yukimods.alloyext.TFCAlloyExt;
import com.yukimods.alloyext.metal.InferiorMetal;
import com.yukimods.alloyext.metal.InferiorMetals;
import com.yukimods.alloyext.metal.RegularMetal;
import com.yukimods.alloyext.metal.RegularMetals;
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

    @SubscribeEvent
    public static void registerExtensions(RegisterClientExtensionsEvent event) {
        // 劣等金属流体（11 种——铝/铅/铀与铬按前置 mod 条件在 InferiorMetals 内部过滤）
        for (InferiorMetal metal : InferiorMetals.getRegistered()) {
            var holder = InferiorMetals.getFluid(metal);
            if (holder == null) continue;
            event.registerFluidType(
                    new FluidRendererExtension(metal.getColor(),
                            ClientEventHandler.MOLTEN_STILL, ClientEventHandler.MOLTEN_FLOW,
                            null, null),
                    holder.getType());
        }

        // 自有金属流体（焊锡/镍铁/铬铁——铬铁仅当 firmalife 存在时已注册）
        for (RegularMetal metal : RegularMetals.getRegistered()) {
            var holder = RegularMetals.getFluid(metal);
            if (holder == null) continue;
            event.registerFluidType(
                    new FluidRendererExtension(metal.getColor(),
                            ClientEventHandler.MOLTEN_STILL, ClientEventHandler.MOLTEN_FLOW,
                            null, null),
                    holder.getType());
        }
    }

    /**
     * 为所有本模组流体桶注册 ItemColor，确保桶内流体显示正确 tint 颜色。
     * 与 TFC 的 {@code registerColorHandlerItems} 完全一致的模式：
     * 遍历注册表 → 过滤命名空间 → {@code fluid.getBucket()} 拿桶 → 注册 ItemColor。
     */
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
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
