package com.yukimods.alloyext;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Game 总线事件监听 — 合成追溯等。
 */
@EventBusSubscriber(modid = TFCAlloyExt.MOD_ID)
public class ModEvents {

    /**
     * 合成产物继承原料的 {@link InferiorOrigin} 组件。
     */
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack output = event.getCrafting();
        if (output.isEmpty()) return;

        // 遍历合成格中的所有原料
        var inventory = event.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack ingredient = inventory.getItem(i);
            if (ingredient.isEmpty()) continue;
            InferiorOrigin origin = ingredient.get(TFCAlloyExt.INFERIOR_ORIGIN.get());
            if (origin != null) {
                output.set(TFCAlloyExt.INFERIOR_ORIGIN.get(), origin);
                return;
            }
        }
    }
}
