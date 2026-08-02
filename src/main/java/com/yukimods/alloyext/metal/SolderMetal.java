package com.yukimods.alloyext.metal;

import net.dries007.tfc.common.blocks.MoltenFluidBlock;
import net.dries007.tfc.common.fluids.FluidHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static com.yukimods.alloyext.TFCAlloyExt.MOD_ID;

/**
 * 焊锡 (Solder) 金属注册 —— 流体 + 方块 + 锭 + 桶。
 * <p>
 * 焊锡是 TFC IE Addon 铅金属 + TFC 锡/铋 的合金产物。
 * 非劣等合金，是独立的合金金属。
 * 注册逻辑委托 {@link MetalRegistration#registerRegular}（ID 派生规则见该类）。
 */
public class SolderMetal {

    /** 焊锡金属名（ID 派生的唯一数据源） */
    public static final String METAL_NAME = "solder";

    // ─── DeferredRegister ─────────────────────────────────

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, MOD_ID);
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MOD_ID);

    // ─── 注册 Holder ──────────────────────────────────────

    public static final FluidHolder<BaseFlowingFluid> FLUID;
    public static final DeferredHolder<Block, MoltenFluidBlock> FLUID_BLOCK;
    public static final DeferredHolder<Item, Item> INGOT;
    public static final DeferredHolder<Item, ? extends Item> BUCKET;

    static {
        MetalRegistration.RegularResult result = MetalRegistration.registerRegular(
                FLUID_TYPES, FLUIDS, BLOCKS, ITEMS, METAL_NAME);
        FLUID = result.fluid();
        FLUID_BLOCK = result.block();
        INGOT = result.ingot();
        BUCKET = result.bucket();
    }

    // ─── 工具方法 ──────────────────────────────────────────

    /** 检查流体是否为焊锡（源或流动） */
    public static boolean isSolderFluid(Fluid fluid) {
        var key = BuiltInRegistries.FLUID.getKey(fluid);
        if (key == null || !MOD_ID.equals(key.getNamespace())) return false;
        String path = key.getPath();
        return path.equals("metal/" + METAL_NAME) || path.equals("metal/flowing_" + METAL_NAME);
    }
}
