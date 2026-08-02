package com.yukimods.alloyext.event;

import com.yukimods.alloyext.TFCAlloyExt;
import com.yukimods.alloyext.metal.InferiorOrigin;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * 客户端事件 — 劣等合金 tooltip 显示。
 */
@EventBusSubscriber(modid = TFCAlloyExt.MOD_ID, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        InferiorOrigin origin = event.getItemStack().get(TFCAlloyExt.INFERIOR_ORIGIN.get());
        if (origin != null) {
            event.getToolTip().add(Component.translatable("component.tfc_alloy_ext.inferior_origin"));
        }
    }
}
