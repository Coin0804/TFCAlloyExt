package com.yukimods.alloyext.mixin;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import com.yukimods.alloyext.TFCAlloyExt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 合金窑配方匹配：劣等输入 → 跳过正常配方，优先 contaminated。
 */
@Mixin(value = AlloyRecipe.class, remap = false)
public abstract class IEAlloyRecipeMixin {

    @Inject(method = "findRecipe(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lblusunrize/immersiveengineering/api/crafting/AlloyRecipe;)Lblusunrize/immersiveengineering/api/crafting/AlloyRecipe;",
            at = @At("RETURN"), cancellable = true)
    private static void preferContaminated(Level level, ItemStack input0, ItemStack input1,
                                           AlloyRecipe hint, CallbackInfoReturnable<AlloyRecipe> cir) {
        AlloyRecipe first = cir.getReturnValue();
        if (first == null) return;
        if (!hasInferiorOrigin(input0) && !hasInferiorOrigin(input1)) return;

        boolean foundFirst = false;
        for (RecipeHolder<AlloyRecipe> h : AlloyRecipe.RECIPES.getRecipes(level)) {
            AlloyRecipe r = h.value();
            if (!foundFirst) {
                if (r == first) foundFirst = true;
                continue;
            }
            if (r.matches(input0, input1)) {
                cir.setReturnValue(r);
                return;
            }
        }
    }

    private static boolean hasInferiorOrigin(ItemStack s) {
        return s.get(TFCAlloyExt.INFERIOR_ORIGIN.get()) != null;
    }
}
