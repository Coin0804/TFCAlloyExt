package com.yukimods.alloyext.util;

import com.yukimods.alloyext.TFCAlloyExt;
import com.yukimods.alloyext.metal.InferiorMetal;
import com.yukimods.alloyext.metal.InferiorOrigin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Mixin 共用的劣等金属识别工具方法。
 */
public class InferiorMetalHelper {

    /**
     * 从物品提取劣等金属的基体名。按优先级：
     * <ol>
     *   <li>检查 {@link InferiorOrigin} DataComponent（前一步已标记）</li>
     *   <li>检查物品 ID 是否为劣等合金锭</li>
     * </ol>
     * 这样劣等锭→板→斧头的链条上每一级产物都能追溯到原始金属。
     */
    @Nullable
    public static String getBaseMetalFromInferiorItem(ItemStack stack) {
        // 优先检查 DataComponent（传递链）
        InferiorOrigin origin = stack.get(TFCAlloyExt.INFERIOR_ORIGIN.get());
        if (origin != null) return origin.baseMetal();

        // 检查物品 ID
        var key = stack.getItemHolder().getKey();
        if (key == null) return null;
        ResourceLocation location = key.location();
        String ns = location.getNamespace();
        String path = location.getPath();
        if (!"tfc_alloy_ext".equals(ns) || !path.startsWith("inferior_") || !path.endsWith("_ingot")) {
            return null;
        }
        String middle = path.substring("inferior_".length(), path.length() - "_ingot".length());
        return InferiorMetal.NAMES.contains(middle) ? middle : null;
    }
}
