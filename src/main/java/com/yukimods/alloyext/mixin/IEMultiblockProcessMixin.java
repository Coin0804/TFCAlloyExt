package com.yukimods.alloyext.mixin;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import com.yukimods.alloyext.metal.InferiorOrigin;
import com.yukimods.alloyext.TFCAlloyExt;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * IE 多方块机器：劣等组件从输入 → 产物。
 */
@Mixin(value = IMultiblockRecipe.class, remap = false)
public interface IEMultiblockProcessMixin {

    /** getDisplayStack 注入：捕获实际输入中的 origin，写入 display stack */
    @Inject(method = "getDisplayStack(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"), cancellable = true)
    default void captureOriginInDisplay(ItemStack input, CallbackInfoReturnable<ItemStack> cir) {
        InferiorOrigin origin = input.get(TFCAlloyExt.INFERIOR_ORIGIN.get());
        TFCAlloyExt.LOGGER.info("[IE] getDisplayStack input={} hasOrigin={}", input, origin != null);
        if (origin != null) {
            ItemStack display = cir.getReturnValue();
            if (!display.isEmpty()) {
                display.set(TFCAlloyExt.INFERIOR_ORIGIN.get(), origin);
                TFCAlloyExt.LOGGER.info("[IE] getDisplayStack wrote origin={} to display", origin.baseMetal());
            }
        }
    }
}
