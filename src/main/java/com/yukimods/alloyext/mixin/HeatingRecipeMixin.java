package com.yukimods.alloyext.mixin;

import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 加热熔融配方匹配优化。
 * <p>
 * TFC 的 {@code getRecipe} 返回缓存中第一个匹配，不考虑温度。
 * 当同一物品有多个加热配方（如带 DataComponent 的劣等变体 + 原版标签配方），
 * 应优先返回温度较低的配方。
 */
@Mixin(value = HeatingRecipe.class, remap = false)
public abstract class HeatingRecipeMixin {

    @Accessor("temperature")
    public abstract float getTemperature();

    @Inject(method = "getRecipe(Lnet/minecraft/world/item/ItemStack;)Lnet/dries007/tfc/common/recipes/HeatingRecipe;",
            at = @At("RETURN"), cancellable = true)
    private static void preferLowestTemperature(ItemStack stack, CallbackInfoReturnable<HeatingRecipe> cir) {
        HeatingRecipe first = cir.getReturnValue();
        // 无匹配或仅一个候选，无需处理
        if (first == null) return;

        // 收集所有匹配的配方
        List<HeatingRecipe> matches = new ArrayList<>();
        for (HeatingRecipe candidate : HeatingRecipe.CACHE.getAll(stack.getItem())) {
            if (candidate.matches(stack)) {
                matches.add(candidate);
            }
        }

        if (matches.size() <= 1) return;

        // 按温度升序排列，选最低的
        matches.sort(Comparator.comparingDouble(
                r -> ((HeatingRecipeMixin) (Object) r).getTemperature()));

        HeatingRecipe best = matches.get(0);
        if (best != first) {
            cir.setReturnValue(best);
        }
    }
}
