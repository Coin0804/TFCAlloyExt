package com.yukimods.alloyext;

import com.yukimods.alloyext.fluid.InferiorMetalFluids;
import net.dries007.tfc.client.ClientEventHandler;
import net.dries007.tfc.client.extensions.FluidRendererExtension;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * 客户端设置 — 用 TFC 同款机制注册流体颜色和纹理。
 * <p>
 * 参照 {@code ClientEventHandler.registerExtensions()}：
 * 使用 {@link RegisterClientExtensionsEvent#registerFluidType} +
 * TFC 的 {@link FluidRendererExtension} + {@code MOLTEN_STILL/FLOW} 纹理。
 */
@EventBusSubscriber(modid = TFCAlloyExt.MOD_ID, value = Dist.CLIENT)
public class ModClientSetup {

    @SubscribeEvent
    static void registerExtensions(RegisterClientExtensionsEvent event) {
        for (InferiorMetal metal : InferiorMetal.VALUES) {
            var holder = InferiorMetalFluids.getInferiorFluid(metal.getName());
            if (holder == null) continue;

            // 与 TFC 金属流体完全一致的构造方式：
            // FluidRendererExtension(tintColor, MOLTEN_STILL, MOLTEN_FLOW, null, null)
            var ext = new FluidRendererExtension(
                    metal.getColor(),
                    ClientEventHandler.MOLTEN_STILL,
                    ClientEventHandler.MOLTEN_FLOW,
                    null,  // overlay
                    null   // renderOverlay
            );
            event.registerFluidType(ext, holder.getType());
        }
    }
}
