package com.yukimods.alloyext.mixin;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import com.yukimods.alloyext.TFCAlloyExt;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 电弧炉配方匹配：劣等输入 → 跳过正常配方，优先 contaminated。
 */
@Mixin(value = ArcFurnaceRecipe.class, remap = false)
public abstract class IEArcFurnaceRecipeMixin {

    @Inject(method = "findRecipe(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/NonNullList;)Lnet/minecraft/world/item/crafting/RecipeHolder;",
            at = @At("RETURN"), cancellable = true)
    private static void preferContaminated(Level level, ItemStack input, NonNullList<ItemStack> additives,
                                           CallbackInfoReturnable<RecipeHolder<ArcFurnaceRecipe>> cir) {
        RecipeHolder<ArcFurnaceRecipe> first = cir.getReturnValue();
        if (first == null) return;

        if (!hasInferiorOrigin(input)) {
            boolean found = false;
            for (ItemStack add : additives) {
                if (hasInferiorOrigin(add)) { found = true; break; }
            }
            if (!found) return;
        }

        boolean foundFirst = false;
        for (RecipeHolder<ArcFurnaceRecipe> h : ArcFurnaceRecipe.RECIPES.getRecipes(level)) {
            ArcFurnaceRecipe r = h.value();
            if (!foundFirst) {
                if (h == first) foundFirst = true;
                continue;
            }
            if (r.matches(input, additives)) {
                cir.setReturnValue(h);
                return;
            }
        }
    }

    private static boolean hasInferiorOrigin(ItemStack s) {
        return s.get(TFCAlloyExt.INFERIOR_ORIGIN.get()) != null;
    }
}
