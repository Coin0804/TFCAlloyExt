package com.yukimods.alloyext.mixin;

import com.yukimods.alloyext.metal.InferiorAlloyLogic;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.FluidAlloy;
import net.dries007.tfc.util.Metal;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 在 TFC 的 FluidAlloy.getResult() 中注入劣等合金判定。
 * <p>
 * 仅拦截返回 UNKNOWN 的情况，已有合金配方完全不受影响。
 */
@Mixin(value = FluidAlloy.class, remap = false)
public abstract class FluidAlloyMixin {

    /** 懒初始化的 UNKNOWN 流体引用缓存，volatile 保证多线程可见性 */
    @Unique
    private static volatile Fluid tfcAlloyExt$cachedUnknown = null;

    @Inject(
            method = "getResult(Lnet/minecraft/world/item/crafting/RecipeManager;)" +
                     "Lnet/neoforged/neoforge/fluids/FluidStack;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void tfcAlloyExt$onGetResult(RecipeManager recipeManager, CallbackInfoReturnable<FluidStack> cir) {
        FluidStack result = cir.getReturnValue();

        // 只拦截 "未知金属"，不碰已有合金配方
        Fluid unknown = tfcAlloyExt$cachedUnknown;
        if (unknown == null) {
            unknown = TFCFluids.METALS.get(Metal.UNKNOWN).getSource();
            tfcAlloyExt$cachedUnknown = unknown;
        }
        if (result.getFluid() != unknown) return;

        // 执行劣等合金规则（RecipeManager 来自 getResult 参数，调用时配方已加载）
        FluidAlloy self = (FluidAlloy) (Object) this;
        FluidStack inferior = InferiorAlloyLogic.resolve(self, recipeManager);
        if (inferior != null) {
            cir.setReturnValue(inferior);
        }
    }
}
