package com.yukimods.alloyext.metal;

import net.dries007.tfc.common.recipes.AlloyRecipe;
import net.dries007.tfc.common.recipes.TFCRecipeTypes;
import net.dries007.tfc.util.AlloyRange;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 合金典型比例分解 —— 运行时读取 TFC 合金配方（单一数据源），无硬编码。
 * <p>
 * 配方成分带范围 (min, max)，按中点 (min+max)/2 分解为纯金属占比。
 * 当存在不确定性时，次等金属无法推断，只分解确定的基础金属（主要金属）：
 * <ol>
 *   <li>配方唯一且无 min=0 成分 → 全部成分按中点分解</li>
 *   <li>配方唯一但有 min=0 成分 → 仅基础金属按中点分解</li>
 *   <li>配方不唯一 → 仅各配方的基础金属按中点分解，同一金属取所有配方中的最小中点</li>
 * </ol>
 * 基础金属 = 配方必选成分（min>0）中中点最大者。
 * <p>
 * 不做静态缓存：调用发生在游戏内混合物判定时，此时配方已完整加载；
 * 每次查询走 RecipeManager 索引（约 30 个合金配方），开销可忽略，且 /reload 后新配方立即生效。
 */
public final class AlloyTypicalRatios {

    private AlloyTypicalRatios() {}

    /** 按流体查其合金配方的典型分解比例；非已知合金返回 null */
    @Nullable
    public static Map<Fluid, Double> getRatios(RecipeManager recipeManager, Fluid fluid) {
        List<AlloyRecipe> recipes = findRecipes(recipeManager, fluid);
        if (recipes.isEmpty()) return null;

        if (recipes.size() == 1) {
            return decomposeSingle(recipes.get(0));
        }
        // 规则 3：多个配方 → 只取各配方的基础金属，同一金属取最小中点
        Map<Fluid, Double> ratios = new LinkedHashMap<>();
        for (AlloyRecipe recipe : recipes) {
            AlloyRange main = mainMetalOf(recipe);
            if (main != null) {
                ratios.merge(main.fluid(), midpoint(main), Math::min);
            }
        }
        return ratios.isEmpty() ? null : ratios;
    }

    /** 收集所有产出该流体的合金配方 */
    private static List<AlloyRecipe> findRecipes(RecipeManager recipeManager, Fluid result) {
        List<AlloyRecipe> recipes = new ArrayList<>();
        for (RecipeHolder<AlloyRecipe> holder : recipeManager.getAllRecipesFor(TFCRecipeTypes.ALLOY.get())) {
            AlloyRecipe recipe = holder.value();
            if (recipe.result() == result) {
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    /**
     * 唯一配方分解。
     * 规则 1：无 min=0 → 全部成分按中点分解；
     * 规则 2：有 min=0 → 次等金属无法推断，只分解基础金属。
     */
    private static Map<Fluid, Double> decomposeSingle(AlloyRecipe recipe) {
        boolean hasOptional = hasOptionalComponent(recipe);
        AlloyRange main = mainMetalOf(recipe);
        if (hasOptional && main == null) return null;  // 全部成分均可选，无从推断

        Map<Fluid, Double> ratios = new LinkedHashMap<>();
        for (AlloyRange range : recipe.contents()) {
            if (hasOptional) {
                if (range == main) {
                    ratios.put(range.fluid(), midpoint(range));
                }
            } else {
                ratios.put(range.fluid(), midpoint(range));
            }
        }
        return ratios;
    }

    /** 配方是否含可选成分（min=0） */
    private static boolean hasOptionalComponent(AlloyRecipe recipe) {
        for (AlloyRange range : recipe.contents()) {
            if (range.min() <= 0) return true;
        }
        return false;
    }

    /** 基础金属 = 必选成分（min>0）中中点最大者 */
    @Nullable
    private static AlloyRange mainMetalOf(AlloyRecipe recipe) {
        AlloyRange main = null;
        double best = -1;
        for (AlloyRange range : recipe.contents()) {
            if (range.min() <= 0) continue;
            double mid = midpoint(range);
            if (mid > best) {
                best = mid;
                main = range;
            }
        }
        return main;
    }

    private static double midpoint(AlloyRange range) {
        return (range.min() + range.max()) / 2;
    }
}
