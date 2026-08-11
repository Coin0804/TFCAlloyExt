package com.yukimods.alloyext.metal;

import com.yukimods.extapi.metal.MetalRegistration;
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
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.yukimods.alloyext.TFCAlloyExt.MOD_ID;

/**
 * 劣等金属统一注册 —— 枚举驱动的流体 + 方块 + 桶注册工作流。
 * <p>
 * 覆盖 {@link InferiorMetal} 全部枚举值；带前置 mod 要求的金属（铝/铅/铀需
 * tfc_ie_addon、铬需 firmalife）在静态初始化时按 {@link InferiorMetal#isEnabled()}
 * 过滤，主类只需无条件注册四个 DeferredRegister。注册逻辑委托
 * {@link MetalRegistration#registerInferior}（劣等金属无自有锭）。
 */
public class InferiorMetals {

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, MOD_ID);
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MOD_ID);

    // ─── 内部 map（register() 填充，按枚举声明顺序执行） ────

    private static final Map<InferiorMetal, FluidHolder<BaseFlowingFluid>> FLUID_MAP =
            new EnumMap<>(InferiorMetal.class);
    private static final Map<InferiorMetal, DeferredHolder<Block, MoltenFluidBlock>> BLOCK_MAP =
            new EnumMap<>(InferiorMetal.class);
    private static final Map<InferiorMetal, DeferredHolder<Item, ? extends Item>> BUCKET_MAP =
            new EnumMap<>(InferiorMetal.class);

    /** 实际注册的金属名集合（白名单，供流体反向查找） */
    private static final Set<String> REGISTERED_NAMES = new LinkedHashSet<>();

    static {
        for (InferiorMetal metal : InferiorMetal.values()) {
            if (metal.isEnabled()) {
                register(metal);
            }
        }
    }

    private static void register(InferiorMetal metal) {
        MetalRegistration.Result result = MetalRegistration.registerInferior(
                FLUID_TYPES, FLUIDS, BLOCKS, ITEMS, MOD_ID, metal.getName());
        FLUID_MAP.put(metal, result.fluid());
        BLOCK_MAP.put(metal, result.block());
        BUCKET_MAP.put(metal, result.bucket());
        REGISTERED_NAMES.add(metal.getName());
    }

    // ─── 查询 ────────────────────────────────────────────

    /** 已注册的金属（按 isEnabled 过滤后的实际注册集合） */
    public static Collection<InferiorMetal> getRegistered() {
        return FLUID_MAP.keySet();
    }

    public static FluidHolder<BaseFlowingFluid> getFluid(InferiorMetal metal) {
        return FLUID_MAP.get(metal);
    }

    public static DeferredHolder<Block, MoltenFluidBlock> getBlock(InferiorMetal metal) {
        return BLOCK_MAP.get(metal);
    }

    public static DeferredHolder<Item, ? extends Item> getBucket(InferiorMetal metal) {
        return BUCKET_MAP.get(metal);
    }

    /** 按基础金属名获取 FluidHolder（未注册返回 null） */
    @Nullable
    public static FluidHolder<BaseFlowingFluid> getFluidByName(String baseMetal) {
        InferiorMetal metal = InferiorMetal.getByName(baseMetal);
        return metal == null ? null : getFluid(metal);
    }

    // ─── 流体识别 ──────────────────────────────────────────

    /** 检查流体是否为任一劣等金属变体（基于注册表 ID 前缀判断，O(1)） */
    public static boolean isInferiorFluid(Fluid fluid) {
        return getBaseMetalFromInferior(fluid) != null;
    }

    /** 反向查找：Fluid → 基础金属名（白名单 = 实际注册集合） */
    @Nullable
    public static String getBaseMetalFromInferior(Fluid fluid) {
        return MetalRegistration.getBaseMetalFromInferior(fluid, REGISTERED_NAMES);
    }

    /**
     * 从外部纯金属流体提取金属名称（非劣等），仅匹配 IE Addon / Firmalife 的单质金属。
     * 例如 {@code tfc_ie_addon:metal/aluminum} → "aluminum"；namespace 不匹配或
     * 非白名单（如 stainless_steel）返回 null。
     */
    @Nullable
    public static String extractPureMetalName(Fluid fluid) {
        var key = BuiltInRegistries.FLUID.getKey(fluid);
        if (key == null) return null;
        if (!("tfc_ie_addon".equals(key.getNamespace()) || "firmalife".equals(key.getNamespace()))) {
            return null;
        }
        String path = key.getPath();
        if (path.startsWith("metal/")) {
            String name = path.substring("metal/".length());
            return InferiorMetal.NAMES.contains(name) ? name : null;
        }
        return null;
    }

    /** 按基础金属名获取颜色（未注册返回默认白色） */
    public static int getColor(String baseMetal) {
        InferiorMetal metal = InferiorMetal.getByName(baseMetal);
        return metal == null ? 0xFFFFFFFF : metal.getColor();
    }
}
