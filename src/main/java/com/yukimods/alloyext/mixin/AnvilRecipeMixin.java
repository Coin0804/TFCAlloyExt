package com.yukimods.alloyext.mixin;

import com.yukimods.alloyext.InferiorOrigin;
import com.yukimods.alloyext.TFCAlloyExt;
import com.yukimods.alloyext.util.InferiorMetalHelper;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 砧锻造时：输入是劣等合金锭 → 产物携带 {@link InferiorOrigin} 标记。
 */
@Mixin(value = AnvilRecipe.class, remap = false)
public abstract class AnvilRecipeMixin {

    @Inject(method = "assemble(Lnet/dries007/tfc/common/recipes/AnvilRecipe$Inventory;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"), cancellable = true)
    private void tagInferiorOrigin(AnvilRecipe.Inventory inventory, HolderLookup.Provider provider,
                                   CallbackInfoReturnable<ItemStack> cir) {
        ItemStack input = inventory.getItem();
        ItemStack output = cir.getReturnValue();
        if (output.isEmpty()) return;

        String baseMetal = InferiorMetalHelper.getBaseMetalFromInferiorItem(input);
        if (baseMetal != null) {
            output.set(TFCAlloyExt.INFERIOR_ORIGIN.get(), new InferiorOrigin(baseMetal));
        }
    }
}
