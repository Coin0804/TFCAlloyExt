package com.yukimods.alloyext.mixin;

import com.yukimods.alloyext.InferiorOrigin;
import com.yukimods.alloyext.TFCAlloyExt;
import com.yukimods.alloyext.util.InferiorMetalHelper;
import net.dries007.tfc.common.recipes.WeldingRecipe;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 焊接配方：输入有 {@link InferiorOrigin} 组件 → 产物继承。
 */
@Mixin(value = WeldingRecipe.class, remap = false)
public abstract class WeldingRecipeMixin {

    @Inject(method = "assemble(Lnet/dries007/tfc/common/recipes/WeldingRecipe$Inventory;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"), cancellable = true)
    private void propagateInferiorOrigin(WeldingRecipe.Inventory inventory,
                                         CallbackInfoReturnable<ItemStack> cir) {
        ItemStack output = cir.getReturnValue();
        if (output.isEmpty()) return;

        InferiorOrigin origin = null;

        // 优先取主手
        origin = inventory.getMain().get(TFCAlloyExt.INFERIOR_ORIGIN.get());
        if (origin == null) {
            origin = inventory.getSecondary().get(TFCAlloyExt.INFERIOR_ORIGIN.get());
        }
        // 如果两者都无 origin，检查是否为劣等锭（新铸造的）
        if (origin == null) {
            String metal = InferiorMetalHelper.getBaseMetalFromInferiorItem(inventory.getMain());
            if (metal == null) {
                metal = InferiorMetalHelper.getBaseMetalFromInferiorItem(inventory.getSecondary());
            }
            if (metal != null) {
                origin = new InferiorOrigin(metal);
            }
        }

        if (origin != null) {
            output.set(TFCAlloyExt.INFERIOR_ORIGIN.get(), origin);
        }
    }
}
