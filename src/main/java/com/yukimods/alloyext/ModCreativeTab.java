package com.yukimods.alloyext;

import com.yukimods.alloyext.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 劣等合金创造模式标签页，包含 7 种流体桶。
 */
public class ModCreativeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TFCAlloyExt.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            CREATIVE_TABS.register("tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.tfc_alloy_ext"))
                    .icon(() -> new ItemStack(ModItems.INFERIOR_COPPER_BUCKET.get()))
                    .displayItems((params, output) -> {
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
