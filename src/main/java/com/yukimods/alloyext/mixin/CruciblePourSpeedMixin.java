package com.yukimods.alloyext.mixin;

import com.yukimods.alloyext.ModConfig;
import net.dries007.tfc.common.blockentities.CrucibleBlockEntity;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 坩埚倾倒速度加速 Mixin。
 * <p>
 * TFC 默认每次 tick 仅转移 1mB 流体，填满一个锭模具（100mB）
 * 需要 100 ticks（5 秒，fast pour 模式）或 400 ticks（20 秒，普通模式）。
 * 此 Mixin 将每次传输量乘以配置中的倍率。
 */
@Mixin(value = CrucibleBlockEntity.class, remap = false)
public abstract class CruciblePourSpeedMixin {

    /**
     * 拦截坩埚→模具的流体传输，放大每次传输量。
     * <p>
     * 原始调用：{@code FluidHelpers.transferExact(crucible, mold, 1)}
     */
    @Redirect(method = "serverTick",
              at = @At(value = "INVOKE",
                       target = "Lnet/dries007/tfc/common/fluids/FluidHelpers;transferExact(Lnet/neoforged/neoforge/fluids/capability/IFluidHandler;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler;I)Z"))
    private static boolean fasterTransferToMold(IFluidHandler source, IFluidHandler target, int amount) {
        return FluidHelpers.transferExact(source, target, amount * ModConfig.CRUCIBLE_POUR_SPEED_MULTIPLIER.get());
    }

    /**
     * 拦截坩埚→输出槽的 drain 操作，放大每次排出量。
     * <p>
     * 原始调用：SIMULATE 和 EXECUTE 两处均硬编码 {@code iconst_1}，
     * 两者的倍率必须一致才能保持流体平衡。
     */
    @Redirect(method = "serverTick",
              at = @At(value = "INVOKE",
                       target = "Lnet/dries007/tfc/common/blockentities/CrucibleBlockEntity$CrucibleInventory;drain(ILnet/neoforged/neoforge/fluids/capability/IFluidHandler$FluidAction;)Lnet/neoforged/neoforge/fluids/FluidStack;"))
    private static net.neoforged.neoforge.fluids.FluidStack fasterDrain(
            net.dries007.tfc.common.blockentities.CrucibleBlockEntity.CrucibleInventory inventory,
            int amount,
            net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction action) {
        return inventory.drain(amount * ModConfig.CRUCIBLE_POUR_SPEED_MULTIPLIER.get(), action);
    }
}
