package com.yukimods.alloyext.block;

import com.yukimods.alloyext.fluid.InferiorMetalFluids;
import net.dries007.tfc.common.blocks.MoltenFluidBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 劣等合金流体方块的公开引用。
 * <p>
 * 实际注册逻辑和方块 DeferredHolder 均由 {@link InferiorMetalFluids} 持有
 * （需要 Fluid ↔ Block 之间共享 DeferredHolder 以避免循环依赖）。
 * 本类仅提供命名清晰的公共字段作为 API 兼容层。
 */
public class ModBlocks {

    /** 方块 DeferredRegister（与 InferiorMetalFluids.BLOCKS 是同一个实例） */
    public static final DeferredRegister<Block> BLOCKS = InferiorMetalFluids.BLOCKS;

    // ─── 7 种劣等流体方块 ─────────────────────────────────

    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_COPPER_FLUID  = InferiorMetalFluids.INFERIOR_COPPER_BLOCK;
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_TIN_FLUID     = InferiorMetalFluids.INFERIOR_TIN_BLOCK;
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_ZINC_FLUID    = InferiorMetalFluids.INFERIOR_ZINC_BLOCK;
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_BISMUTH_FLUID = InferiorMetalFluids.INFERIOR_BISMUTH_BLOCK;
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_GOLD_FLUID    = InferiorMetalFluids.INFERIOR_GOLD_BLOCK;
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_SILVER_FLUID  = InferiorMetalFluids.INFERIOR_SILVER_BLOCK;
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_NICKEL_FLUID  = InferiorMetalFluids.INFERIOR_NICKEL_BLOCK;
}
