package com.yukimods.alloyext.fluid;

import com.yukimods.alloyext.InferiorMetal;
import com.yukimods.alloyext.ModConfig;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import net.dries007.tfc.common.fluids.FluidHolder;
import net.dries007.tfc.util.FluidAlloy;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

import java.util.*;

/**
 * 劣等合金核心逻辑。
 * <p>
 * 在 TFC 合金系统无法匹配已知配方（结果为 UNKNOWN）时被 Mixin 调用，
 * 按规则判断是否应生成「熔融劣等X合金」。
 */
public class InferiorAlloyLogic {

    /** 能产生劣等变体的纯金属名称集合（由 {@link InferiorMetal} 枚举驱动） */
    private static final Set<String> PURE_METALS = InferiorMetal.NAMES;

    /**
     * 解析流体混合物，判断是否应替换为劣等合金。
     *
     * @param alloy 坩埚/小缸中的流体混合物 (FluidAlloy)
     * @return 替换后的 FluidStack，或 null 表示保持 UNKNOWN 不变
     */
    @Nullable
    public static FluidStack resolve(FluidAlloy alloy) {
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
                    InferiorAddonMetals.isInferiorAddonFluid(fluid)) {
                inferiorFluids.add(fluid);
            } else {
                String metalName = extractPureMetalName(fluid);
                if (metalName == null) {
                    metalName = InferiorAddonMetals.extractAddonPureMetalName(fluid);
                }
                if (metalName != null && (PURE_METALS.contains(metalName) ||
                        InferiorAddonMetals.PURE_METAL_NAMES.contains(metalName))) {
                    pureAmounts.merge(metalName, amount, Double::sum);
                } else {
                    // 非目标金属（水、TFC合金流体等）→ 保持 UNKNOWN
                    return null;
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

    /** 从 TFC 或 IE Addon 劣等流体中提取基础金属名 */
    @Nullable
    private static String getBaseMetalFromAnyInferior(Fluid fluid) {
        String name = InferiorMetalFluids.getBaseMetalFromInferior(fluid);
        return name != null ? name : InferiorAddonMetals.getBaseMetalFromInferior(fluid);
    }

    /** 查找劣等 FluidHolder（先查 TFC，再查 IE Addon） */
    @Nullable
    private static FluidHolder<BaseFlowingFluid> getInferiorFluidHolder(String baseMetal) {
        var holder = InferiorMetalFluids.getInferiorFluid(baseMetal);
        if (holder != null) return holder;
        return InferiorAddonMetals.getFluid(baseMetal);
    }
}
