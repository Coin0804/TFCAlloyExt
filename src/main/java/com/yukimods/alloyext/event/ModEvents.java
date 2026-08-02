package com.yukimods.alloyext.event;

import com.yukimods.alloyext.TFCAlloyExt;
import com.yukimods.alloyext.config.ModConfig;
import com.yukimods.alloyext.metal.InferiorOrigin;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Game 总线事件监听 — 合成追溯与劣等减益等。
 */
@EventBusSubscriber(modid = TFCAlloyExt.MOD_ID)
public class ModEvents {

    /**
     * 合成产物继承原料的 {@link InferiorOrigin} 组件；
     * 可损伤产物（工具/鱼竿）额外获得出生耐久减益 ——
     * 劣等工具头质量差，合成出的工具出生即损失
     * {@link ModConfig#INFERIOR_TOOL_DURABILITY_PENALTY} 比例的最大耐久。
     * TFC 无修复配方，减益持续整个工具寿命。
     */
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack output = event.getCrafting();
        if (output.isEmpty()) return;

        // 遍历合成格中的所有原料，找到劣等出身
        InferiorOrigin origin = null;
        var inventory = event.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack ingredient = inventory.getItem(i);
            if (ingredient.isEmpty()) continue;
            InferiorOrigin candidate = ingredient.get(TFCAlloyExt.INFERIOR_ORIGIN.get());
            if (candidate != null) {
                origin = candidate;
                break;
            }
        }
        if (origin == null) return;

        // 产物继承劣等出身
        output.set(TFCAlloyExt.INFERIOR_ORIGIN.get(), origin);

        // 可损伤产物出生即带耐久减益（至少 1 点损伤，不超最大耐久 - 1）
        if (output.isDamageableItem()) {
            int maxDamage = output.getMaxDamage();
            int startDamage = Math.round(maxDamage * ModConfig.INFERIOR_TOOL_DURABILITY_PENALTY.get().floatValue());
            if (startDamage > 0) {
                output.setDamageValue(Math.min(startDamage, maxDamage - 1));
            }
        }
    }
}
