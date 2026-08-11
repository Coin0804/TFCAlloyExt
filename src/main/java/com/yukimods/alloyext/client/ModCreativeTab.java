package com.yukimods.alloyext.client;

import com.yukimods.alloyext.TFCAlloyExt;
import com.yukimods.alloyext.metal.InferiorMetal;
import com.yukimods.alloyext.metal.InferiorMetals;
import com.yukimods.alloyext.metal.RegularMetal;
import com.yukimods.alloyext.metal.RegularMetals;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 合金创造模式标签页，包含 7 种劣等流体桶 + 焊锡锭和桶 + IE Addon 桶。
 */
public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TFCAlloyExt.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            CREATIVE_TABS.register("tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tfc_alloy_ext"))
                    .icon(() -> new ItemStack(InferiorMetals.getBucket(InferiorMetal.COPPER).get()))
                    .displayItems((params, output) -> {
                        // 劣等合金桶（11 种——铝/铅/铀与铬按前置 mod 条件在 InferiorMetals 内部过滤）
                        for (InferiorMetal metal : InferiorMetals.getRegistered()) {
                            output.accept(InferiorMetals.getBucket(metal).get());
                        }
                        // 自有金属锭与桶（焊锡/镍铁/铬铁——铬铁仅当 firmalife 存在时已注册）
                        for (RegularMetal metal : RegularMetals.getRegistered()) {
                            output.accept(RegularMetals.getIngot(metal).get());
                            output.accept(RegularMetals.getBucket(metal).get());
                        }
                    })
                    .build());
}
