package com.yukimods.alloyext;

import com.yukimods.alloyext.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 劣等合金创造模式标签页，包含全部 7 种合金锭和 7 种流体桶。
 */
public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TFCAlloyExt.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            CREATIVE_TABS.register("tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tfc_alloy_ext"))
                    .icon(() -> new ItemStack(ModItems.INFERIOR_COPPER_INGOT.get()))
                    .displayItems((params, output) -> {
                        // 劣等合金锭
                        output.accept(ModItems.INFERIOR_COPPER_INGOT.get());
                        output.accept(ModItems.INFERIOR_TIN_INGOT.get());
                        output.accept(ModItems.INFERIOR_ZINC_INGOT.get());
                        output.accept(ModItems.INFERIOR_BISMUTH_INGOT.get());
                        output.accept(ModItems.INFERIOR_GOLD_INGOT.get());
                        output.accept(ModItems.INFERIOR_SILVER_INGOT.get());
                        output.accept(ModItems.INFERIOR_NICKEL_INGOT.get());
                        // 流体桶
                        output.accept(ModItems.INFERIOR_COPPER_BUCKET.get());
                        output.accept(ModItems.INFERIOR_TIN_BUCKET.get());
                        output.accept(ModItems.INFERIOR_ZINC_BUCKET.get());
                        output.accept(ModItems.INFERIOR_BISMUTH_BUCKET.get());
                        output.accept(ModItems.INFERIOR_GOLD_BUCKET.get());
                        output.accept(ModItems.INFERIOR_SILVER_BUCKET.get());
                        output.accept(ModItems.INFERIOR_NICKEL_BUCKET.get());
                    })
                    .build());
}
