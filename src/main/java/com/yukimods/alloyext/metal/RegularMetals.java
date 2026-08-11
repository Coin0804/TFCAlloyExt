package com.yukimods.alloyext.metal;

import com.yukimods.extapi.metal.MetalRegistration;
import net.dries007.tfc.common.blocks.MoltenFluidBlock;
import net.dries007.tfc.common.fluids.FluidHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import static com.yukimods.alloyext.TFCAlloyExt.MOD_ID;

/**
 * 自有金属（常规金属）统一注册 —— 枚举驱动的流体 + 方块 + 桶 + 锭注册工作流。
 * <p>
 * 覆盖 {@link RegularMetal} 全部枚举值；带前置 mod 要求的金属（如铬铁需 firmalife）
 * 在静态初始化时按 {@link RegularMetal#isEnabled()} 过滤，主类只需无条件注册四个
 * DeferredRegister。注册逻辑委托 {@link MetalRegistration#registerRegular}。
 */
public class RegularMetals {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, MOD_ID);
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MOD_ID);

    // ─── 内部 map（register() 填充，按枚举声明顺序执行） ────

    private static final Map<RegularMetal, FluidHolder<BaseFlowingFluid>> FLUID_MAP =
            new EnumMap<>(RegularMetal.class);
    private static final Map<RegularMetal, DeferredHolder<Block, MoltenFluidBlock>> BLOCK_MAP =
            new EnumMap<>(RegularMetal.class);
    private static final Map<RegularMetal, DeferredHolder<Item, Item>> INGOT_MAP =
            new EnumMap<>(RegularMetal.class);
    private static final Map<RegularMetal, DeferredHolder<Item, ? extends Item>> BUCKET_MAP =
            new EnumMap<>(RegularMetal.class);

    static {
        for (RegularMetal metal : RegularMetal.values()) {
            if (metal.isEnabled()) {
                register(metal);
            }
        }
    }

    private static void register(RegularMetal metal) {
        MetalRegistration.RegularResult result = MetalRegistration.registerRegular(
                FLUID_TYPES, FLUIDS, BLOCKS, ITEMS, MOD_ID, metal.getName());
        FLUID_MAP.put(metal, result.fluid());
        BLOCK_MAP.put(metal, result.block());
        INGOT_MAP.put(metal, result.ingot());
        BUCKET_MAP.put(metal, result.bucket());
    }

    // ─── 查询 ────────────────────────────────────────────

    /** 已注册的金属（按 isEnabled 过滤后的实际注册集合） */
    public static Collection<RegularMetal> getRegistered() {
        return FLUID_MAP.keySet();
    }

    public static FluidHolder<BaseFlowingFluid> getFluid(RegularMetal metal) {
        return FLUID_MAP.get(metal);
    }

    public static DeferredHolder<Block, MoltenFluidBlock> getBlock(RegularMetal metal) {
        return BLOCK_MAP.get(metal);
    }

    public static DeferredHolder<Item, Item> getIngot(RegularMetal metal) {
        return INGOT_MAP.get(metal);
    }

    public static DeferredHolder<Item, ? extends Item> getBucket(RegularMetal metal) {
        return BUCKET_MAP.get(metal);
    }
}
