package com.yukimods.alloyext.metal;

import com.yukimods.alloyext.config.ModConfig;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.dries007.tfc.common.fluids.FluidHolder;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.FluidAlloy;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 劣等合金核心逻辑。
 * <p>
 * 在 TFC 合金系统无法匹配已知配方（结果为 UNKNOWN）时被 Mixin 调用，
 * 按规则判断是否应生成「熔融劣等X合金」。
 */
public class InferiorAlloyLogic {

    /** 能产生劣等变体的纯金属名称集合（由 {@link InferiorMetal} 枚举驱动） */
    private static final Set<String> PURE_METALS = InferiorMetal.NAMES;

    /** 铁劣等系统的纯金属名和劣等金属名 */
    private static final String WROUGHT_IRON = "wrought_iron";
    private static final String CAST_IRON = "cast_iron";

    /**
     * 解析流体混合物，判断是否应替换为劣等合金。
     * <p>
     * 已知合金（见 {@link AlloyTypicalRatios}）按典型比例分解为纯金属后
     * 参与判定，避免整炉合金流体直接黑箱沉没为 UNKNOWN。
     *
     * @param alloy 坩埚/小缸中的流体混合物 (FluidAlloy)
     * @param recipeManager 当前配方管理器（由调用方传入，调用时配方已完整加载）
     * @return 替换后的 FluidStack，或 null 表示保持 UNKNOWN 不变
     */
    @Nullable
    public static FluidStack resolve(FluidAlloy alloy, RecipeManager recipeManager) {
        Object2DoubleMap<Fluid> content = alloy.getContent();
        if (content.size() < 2) return null; // 单流体不处理

        int total = alloy.getAmount();
        if (total <= 0) return null;

        // ── 第一步：分类流体 ──────────────────────────────
        List<Fluid> inferiorFluids = new ArrayList<>();
        Map<String, Double> pureAmounts = new LinkedHashMap<>();

        for (var entry : content.object2DoubleEntrySet()) {
            Fluid fluid = entry.getKey();
            double amount = entry.getDoubleValue();

            if (InferiorMetalFluids.isInferiorFluid(fluid) ||
                    InferiorAddonMetals.isInferiorAddonFluid(fluid) ||
                    InferiorFirmalifeMetals.isInferiorFirmalifeFluid(fluid) ||
                    isCastIronFluid(fluid)) {
                inferiorFluids.add(fluid);
            } else {
                String metalName = getComponentMetalName(fluid);
                if (metalName == null && isWroughtIronFluid(fluid)) {
                    metalName = WROUGHT_IRON;
                }
                if (metalName != null && (PURE_METALS.contains(metalName) ||
                        InferiorAddonMetals.PURE_METAL_NAMES.contains(metalName) ||
                        InferiorFirmalifeMetals.PURE_METAL_NAMES.contains(metalName) ||
                        WROUGHT_IRON.equals(metalName))) {
                    pureAmounts.merge(metalName, amount, Double::sum);
                } else {
                    // 合金流体：按 TFC 合金配方典型比例分解为纯金属（运行时读取，无硬编码）
                    Map<Fluid, Double> ratios = AlloyTypicalRatios.getRatios(recipeManager, fluid);
                    if (ratios != null) {
                        for (var ratio : ratios.entrySet()) {
                            String name = getComponentMetalName(ratio.getKey());
                            if (name != null) {
                                pureAmounts.merge(name, amount * ratio.getValue(), Double::sum);
                            }
                        }
                    } else {
                        // 非目标流体（水、未知合金等）→ 保持 UNKNOWN
                        return null;
                    }
                }
            }
        }

        // ── 第二步：劣等流体规则 ──────────────────────────
        if (!inferiorFluids.isEmpty()) {
            // 劣等X合金 + 同族纯X → 更多劣等X合金（污染扩散）
            if (inferiorFluids.size() == 1 && pureAmounts.size() == 1) {
                String infMetal = getBaseMetalFromAnyInferior(inferiorFluids.get(0));
                String pureMetal = pureAmounts.keySet().iterator().next();
                if (infMetal != null && infMetal.equals(pureMetal)) {
                    var holder = getInferiorFluidHolder(infMetal);
                    if (holder != null) return new FluidStack(holder.getSource(), total);
                }
            }
            // 劣等X + 异族纯Y / 劣等X + 劣等Y → 黑箱沉没 → UNKNOWN
            return null;
        }

        // ── 第三步：阈值判定（全纯金属混合物）─────────────
        String dominant = null;
        double maxAmount = 0;
        for (var entry : pureAmounts.entrySet()) {
            if (entry.getValue() > maxAmount) {
                maxAmount = entry.getValue();
                dominant = entry.getKey();
            }
        }

        if (dominant != null && maxAmount / total >= ModConfig.INFERIOR_ALLOY_THRESHOLD.get()) {
            var holder = getInferiorFluidHolder(dominant);
            if (holder != null) return new FluidStack(holder.getSource(), total);
        }

        // 低于阈值 → UNKNOWN（不变）
        return null;
    }

    // ─── 铁劣等系统辅助 ────────────────────────────────────

    private static boolean isWroughtIronFluid(Fluid fluid) {
        if (!ModConfig.ENABLE_IRON_INFERIOR_SYSTEM.get()) return false;
        var key = BuiltInRegistries.FLUID.getKey(fluid);
        return key != null && "tfc".equals(key.getNamespace())
                && ("metal/" + WROUGHT_IRON).equals(key.getPath());
    }

    private static boolean isCastIronFluid(Fluid fluid) {
        if (!ModConfig.ENABLE_IRON_INFERIOR_SYSTEM.get()) return false;
        var key = BuiltInRegistries.FLUID.getKey(fluid);
        return key != null && "tfc".equals(key.getNamespace())
                && ("metal/" + CAST_IRON).equals(key.getPath());
    }

    // ─── 通用辅助 ──────────────────────────────────────────

    /** 从流体提取纯金属名（TFC → IE Addon → Firmalife 链），非纯金属返回 null */
    @Nullable
    private static String getComponentMetalName(Fluid fluid) {
        String name = extractPureMetalName(fluid);
        if (name == null) {
            name = InferiorAddonMetals.extractAddonPureMetalName(fluid);
        }
        if (name == null) {
            name = InferiorFirmalifeMetals.extractFirmalifePureMetalName(fluid);
        }
        return name;
    }

    /**
     * 从 TFC 纯金属流体 ID 提取金属名称。
     * 例如 "tfc:metal/copper" → "copper"，"tfc:metal/unknown" → null。
     */
    @Nullable
    private static String extractPureMetalName(Fluid fluid) {
        var key = BuiltInRegistries.FLUID.getKey(fluid);
        if (key == null) return null;
        String path = key.getPath();
        if (key.getNamespace().equals("tfc") && path.startsWith("metal/")) {
            String name = path.substring("metal/".length());
            return name.equals("unknown") ? null : name;
        }
        return null;
    }

    /** 从 TFC / IE Addon / Firmalife / Iron 劣等流体中提取基础金属名 */
    @Nullable
    private static String getBaseMetalFromAnyInferior(Fluid fluid) {
        String name = InferiorMetalFluids.getBaseMetalFromInferior(fluid);
        if (name != null) return name;
        name = InferiorAddonMetals.getBaseMetalFromInferior(fluid);
        if (name != null) return name;
        name = InferiorFirmalifeMetals.getBaseMetalFromInferior(fluid);
        if (name != null) return name;
        // 铁劣等：cast_iron 基础金属映射为 "wrought_iron"（纯铁），才能与 pureAmounts 匹配
        if (isCastIronFluid(fluid)) return WROUGHT_IRON;
        return null;
    }

    /** 查找劣等 FluidHolder（先查 TFC，再查 IE Addon，再查 Firmalife，铁返回 TFC 原生 cast_iron） */
    @Nullable
    private static FluidHolder<BaseFlowingFluid> getInferiorFluidHolder(String baseMetal) {
        // 铁劣等：wrought_iron 的劣等变体是 TFC 原生 cast_iron
        if (WROUGHT_IRON.equals(baseMetal) && ModConfig.ENABLE_IRON_INFERIOR_SYSTEM.get()) {
            return TFCFluids.METALS.get(Metal.CAST_IRON);
        }
        var holder = InferiorMetalFluids.getInferiorFluid(baseMetal);
        if (holder != null) return holder;
        holder = InferiorAddonMetals.getFluid(baseMetal);
        if (holder != null) return holder;
        return InferiorFirmalifeMetals.getFluid(baseMetal);
    }
}
