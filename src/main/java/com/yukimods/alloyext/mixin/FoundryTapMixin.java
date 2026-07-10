package com.yukimods.alloyext.mixin;

import cy.jdkdigital.productivemetalworks.common.block.entity.FoundryTapBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

/**
 * PM 熔铸炉龙头 —— TFC 合金逻辑注入点。
 * <p>
 * 在龙头浇铸前，读取熔铸炉 MultiFluidTank 中的 TFC 金属流体，
 * 通过 InferiorAlloyLogic 判定是否应替换为劣等合金/正常合金。
 * <p>
 * TODO: 需要在 IDEA 中反编译 FoundryTapBlockEntity.serverTick 完整逻辑后确定具体注入位置。
 *       当前仅做类引用占位，避免编译错误。
 */
@Mixin(value = FoundryTapBlockEntity.class, remap = false)
public abstract class FoundryTapMixin {

    // @Inject 将在反编译确认 serverTick 逻辑后添加
    // 预期注入点：龙头激活开始浇铸时，对目标槽位的流体应用 TFC 合金计算
}
