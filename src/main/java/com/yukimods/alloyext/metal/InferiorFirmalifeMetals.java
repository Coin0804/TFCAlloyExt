package com.yukimods.alloyext.metal;

import net.dries007.tfc.common.blocks.MoltenFluidBlock;
import net.dries007.tfc.common.fluids.FluidHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.yukimods.alloyext.TFCAlloyExt.MOD_ID;

/**
 * Firmalife 金属的劣等合金变体 —— 铬。
 * <p>
 * 仅在 {@code firmalife} 模组已安装时激活。
 * 纯金属流体来自 {@code firmalife:metal/chromium}。
 * 不锈钢是合金，不产生劣等变体。
 * <p>
 * 注册逻辑委托 {@link MetalRegistration#registerInferior}，与 {@link InferiorMetalFluids} 相同模式。
 */
public class InferiorFirmalifeMetals {

    public static final String ADDON_MOD_ID = "firmalife";

    // ─── 元数据 ──────────────────────────────────────────
    // (name, meltingTemp°C, 流体颜色ARGB, 纯流体ID)
    // meltingTemp 与 data/tfc/tfc/fluid_heat/inferior_chromium.json 的 melt_temperature 对应（单一数据源）

    public record FirmalifeMetal(
            String name,
            int meltingTemp,
            int color,
            String pureFluidId
    ) {}

    // Chromium: 1250°C * 0.95 = 1187.5 → 1188; color -0x0F0F0F from 0xF5F6FF → 0xE6E7F0
    public static final FirmalifeMetal CHROMIUM = new FirmalifeMetal("chromium", 1188, 0xFFE6E7F0, "firmalife:metal/chromium");

    public static final Set<String> PURE_METAL_NAMES =
            Collections.unmodifiableSet(new LinkedHashSet<>(Set.of(CHROMIUM.name())));

    // ─── DeferredRegister ─────────────────────────────────

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, MOD_ID);
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MOD_ID);

    // ─── 注册结果 ──────────────────────────────────────────

    private static final Map<String, FluidHolder<BaseFlowingFluid>> FLUID_MAP = new LinkedHashMap<>();
    private static final Map<String, DeferredHolder<Block, MoltenFluidBlock>> BLOCK_MAP = new LinkedHashMap<>();
    private static final Map<String, DeferredHolder<Item, ? extends Item>> BUCKET_MAP = new LinkedHashMap<>();

    public static final FluidHolder<BaseFlowingFluid> INFERIOR_CHROMIUM = register(CHROMIUM);

    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_CHROMIUM_BLOCK = getBlock("chromium");
    public static final DeferredHolder<Item, Item> INFERIOR_CHROMIUM_BUCKET = getBucket("chromium");

    @SuppressWarnings("unchecked")
    private static <T extends Block> DeferredHolder<Block, T> getBlock(String name) {
        return (DeferredHolder<Block, T>) BLOCK_MAP.get(name);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Item> DeferredHolder<Item, T> getBucket(String name) {
        return (DeferredHolder<Item, T>) BUCKET_MAP.get(name);
    }

    // ─── 注册逻辑 ──────────────────────────────────────────

    private static FluidHolder<BaseFlowingFluid> register(FirmalifeMetal metal) {
        MetalRegistration.Result result = MetalRegistration.registerInferior(
                FLUID_TYPES, FLUIDS, BLOCKS, ITEMS, metal.name());
        FLUID_MAP.put(metal.name(), result.fluid());
        BLOCK_MAP.put(metal.name(), result.block());
        BUCKET_MAP.put(metal.name(), result.bucket());
        return result.fluid();
    }

    // ─── 工具方法 ──────────────────────────────────────────

    public static boolean isEnabled() {
        return ModList.get().isLoaded(ADDON_MOD_ID);
    }

    @Nullable
    public static FluidHolder<BaseFlowingFluid> getFluid(String baseMetal) {
        return FLUID_MAP.get(baseMetal);
    }

    public static boolean isInferiorFirmalifeFluid(Fluid fluid) {
        return getBaseMetalFromInferior(fluid) != null;
    }

    /** 反向查找：Fluid → Firmalife 基础金属名（白名单过滤） */
    @Nullable
    public static String getBaseMetalFromInferior(Fluid fluid) {
        return MetalRegistration.getBaseMetalFromInferior(fluid, PURE_METAL_NAMES);
    }

    /** 从 {@code firmalife:metal/xxx} 流体提取纯金属名称（非劣等），仅匹配铬 */
    @Nullable
    public static String extractFirmalifePureMetalName(Fluid fluid) {
        if (!isEnabled()) return null;
        var key = BuiltInRegistries.FLUID.getKey(fluid);
        if (key == null || !ADDON_MOD_ID.equals(key.getNamespace())) return null;
        String path = key.getPath();
        if (path.startsWith("metal/")) {
            String name = path.substring("metal/".length());
            return PURE_METAL_NAMES.contains(name) ? name : null;
        }
        return null;
    }

    public static int getColor(String baseMetal) {
        return CHROMIUM.color();
    }
}
