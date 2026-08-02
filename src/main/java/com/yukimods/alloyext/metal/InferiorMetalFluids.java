package com.yukimods.alloyext.metal;

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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.yukimods.alloyext.TFCAlloyExt.MOD_ID;

/**
 * 7 种「熔融劣等X合金」流体的注册与查询。
 * <p>
 * 注册逻辑委托 {@link MetalRegistration#registerInferior}（ID 派生规则见该类），
 * 本类只持有各金属的 DeferredRegister 与查询用 MAP。
 * 劣等金属无自有锭：铸造/加热产物复用 TFC 原版金属物品。
 */
public class InferiorMetalFluids {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, MOD_ID);
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MOD_ID);

    // ─── 内部 map（register() 填充，按 static 声明顺序执行） ─

    private static final Map<String, FluidHolder<BaseFlowingFluid>> FLUID_MAP = new LinkedHashMap<>();
    private static final Map<String, DeferredHolder<Block, MoltenFluidBlock>> BLOCK_MAP = new LinkedHashMap<>();
    private static final Map<String, DeferredHolder<Item, ? extends Item>> BUCKET_MAP = new LinkedHashMap<>();

    // ─── 流体：静态初始化时依次注册 ────────────────────────

    public static final FluidHolder<BaseFlowingFluid> INFERIOR_COPPER  = register(InferiorMetal.COPPER);
    public static final FluidHolder<BaseFlowingFluid> INFERIOR_TIN     = register(InferiorMetal.TIN);
    public static final FluidHolder<BaseFlowingFluid> INFERIOR_ZINC    = register(InferiorMetal.ZINC);
    public static final FluidHolder<BaseFlowingFluid> INFERIOR_BISMUTH = register(InferiorMetal.BISMUTH);
    public static final FluidHolder<BaseFlowingFluid> INFERIOR_GOLD    = register(InferiorMetal.GOLD);
    public static final FluidHolder<BaseFlowingFluid> INFERIOR_SILVER  = register(InferiorMetal.SILVER);
    public static final FluidHolder<BaseFlowingFluid> INFERIOR_NICKEL  = register(InferiorMetal.NICKEL);

    // ─── 方块 ──────────────────────────────────────────

    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_COPPER_BLOCK  = getBlock("copper");
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_TIN_BLOCK     = getBlock("tin");
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_ZINC_BLOCK    = getBlock("zinc");
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_BISMUTH_BLOCK = getBlock("bismuth");
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_GOLD_BLOCK    = getBlock("gold");
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_SILVER_BLOCK  = getBlock("silver");
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_NICKEL_BLOCK  = getBlock("nickel");

    // ─── 桶物品 ────────────────────────────────────────

    public static final DeferredHolder<Item, Item> INFERIOR_COPPER_BUCKET  = getBucket("copper");
    public static final DeferredHolder<Item, Item> INFERIOR_TIN_BUCKET     = getBucket("tin");
    public static final DeferredHolder<Item, Item> INFERIOR_ZINC_BUCKET    = getBucket("zinc");
    public static final DeferredHolder<Item, Item> INFERIOR_BISMUTH_BUCKET = getBucket("bismuth");
    public static final DeferredHolder<Item, Item> INFERIOR_GOLD_BUCKET    = getBucket("gold");
    public static final DeferredHolder<Item, Item> INFERIOR_SILVER_BUCKET  = getBucket("silver");
    public static final DeferredHolder<Item, Item> INFERIOR_NICKEL_BUCKET  = getBucket("nickel");

    @SuppressWarnings("unchecked")
    private static <T extends Block> DeferredHolder<Block, T> getBlock(String name) {
        return (DeferredHolder<Block, T>) BLOCK_MAP.get(name);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Item> DeferredHolder<Item, T> getBucket(String name) {
        return (DeferredHolder<Item, T>) BUCKET_MAP.get(name);
    }

    // ─── 注册逻辑 ──────────────────────────────────────────

    private static FluidHolder<BaseFlowingFluid> register(InferiorMetal metal) {
        MetalRegistration.Result result = MetalRegistration.registerInferior(
                FLUID_TYPES, FLUIDS, BLOCKS, ITEMS, metal.getName());
        FLUID_MAP.put(metal.getName(), result.fluid());
        BLOCK_MAP.put(metal.getName(), result.block());
        BUCKET_MAP.put(metal.getName(), result.bucket());
        return result.fluid();
    }

    // ─── 工具方法 ──────────────────────────────────────────

    /** 基于注册表 ID 前缀判断，O(1) */
    public static boolean isInferiorFluid(Fluid fluid) {
        return getBaseMetalFromInferior(fluid) != null;
    }

    /** 按基础金属名获取 FluidHolder */
    public static FluidHolder<BaseFlowingFluid> getInferiorFluid(String baseMetal) {
        return FLUID_MAP.get(baseMetal);
    }

    /**
     * 反向查找：Fluid → 基础金属名。
     * 基于注册表 ID 前缀匹配 + 本类金属白名单，O(1) 字符串操作。
     */
    public static String getBaseMetalFromInferior(Fluid fluid) {
        return MetalRegistration.getBaseMetalFromInferior(fluid, InferiorMetal.NAMES);
    }

    /** 所有已注册的劣等合金流体 */
    public static Collection<FluidHolder<BaseFlowingFluid>> all() {
        return Collections.unmodifiableCollection(FLUID_MAP.values());
    }
}
