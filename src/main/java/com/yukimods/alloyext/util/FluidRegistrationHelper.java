package com.yukimods.alloyext.util;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * 流体注册工具 — 与 TFC / tfc_ie_addon 参数完全对齐。
 */
public class FluidRegistrationHelper {

    /**
     * 创建熔融金属 FluidType，属性与 TFC 的 {@code lavaLike()} 完全一致。
     */
    public static FluidType createMoltenFluidType(String descriptionId) {
        return new FluidType(FluidType.Properties.create()
                .adjacentPathType(PathType.LAVA)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY_LAVA)
                .lightLevel(15).density(3000).viscosity(6000).temperature(1300)
                .canConvertToSource(false).canDrown(false).canExtinguish(false)
                .canHydrate(false).canPushEntity(false).canSwim(false).supportsBoating(false)
                .descriptionId(descriptionId));
    }

    /**
     * 配置熔融金属流体 Properties，与 TFC / tfc_ie_addon 参数完全一致。
     */
    public static void configureMoltenProperties(BaseFlowingFluid.Properties props) {
        props.explosionResistance(100f).slopeFindDistance(4).tickRate(30);
    }
}
