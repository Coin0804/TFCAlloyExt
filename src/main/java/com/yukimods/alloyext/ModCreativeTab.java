package com.yukimods.alloyext;

import com.yukimods.alloyext.fluid.InferiorAddonMetals;
import com.yukimods.alloyext.fluid.InferiorFirmalifeMetals;
import com.yukimods.alloyext.fluid.InferiorMetalFluids;
import com.yukimods.alloyext.metal.SolderMetal;
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
                    .icon(() -> new ItemStack(InferiorMetalFluids.INFERIOR_COPPER_BUCKET.get()))
                    .displayItems((params, output) -> {
                        // 劣等合金桶（7 种，来自 InferiorMetalFluids）
                        output.accept(InferiorMetalFluids.INFERIOR_COPPER_BUCKET.get());
                        output.accept(InferiorMetalFluids.INFERIOR_TIN_BUCKET.get());
                        output.accept(InferiorMetalFluids.INFERIOR_ZINC_BUCKET.get());
                        output.accept(InferiorMetalFluids.INFERIOR_BISMUTH_BUCKET.get());
                        output.accept(InferiorMetalFluids.INFERIOR_GOLD_BUCKET.get());
                        output.accept(InferiorMetalFluids.INFERIOR_SILVER_BUCKET.get());
                        output.accept(InferiorMetalFluids.INFERIOR_NICKEL_BUCKET.get());
                        // IE Addon 劣等合金桶（仅当 tfc_ie_addon 存在时）
                        if (InferiorAddonMetals.isEnabled()) {
                            output.accept(InferiorAddonMetals.INFERIOR_ALUMINUM_BUCKET.get());
                            output.accept(InferiorAddonMetals.INFERIOR_LEAD_BUCKET.get());
                            output.accept(InferiorAddonMetals.INFERIOR_URANIUM_BUCKET.get());
                        }
                        // Firmalife 劣等合金桶（仅当 firmalife 存在时）
                        if (InferiorFirmalifeMetals.isEnabled()) {
                            output.accept(InferiorFirmalifeMetals.INFERIOR_CHROMIUM_BUCKET.get());
                        }
                        // 焊锡
                        output.accept(SolderMetal.INGOT.get());
                        output.accept(SolderMetal.BUCKET.get());
                    })
                    .build());
}
