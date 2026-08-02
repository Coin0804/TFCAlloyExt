package com.yukimods.alloyext.mixin;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import com.yukimods.alloyext.TFCAlloyExt;
import com.yukimods.alloyext.metal.InferiorOrigin;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * IE 多方块打印输出：从 inputItems 读取 origin → 写到输出。
 */
@Mixin(value = MultiblockProcessInWorld.class, remap = false)
public abstract class IEOutputItemMixin {

    private static final Logger LOG = LoggerFactory.getLogger("TFCAlloyExt:IE");

    @SuppressWarnings("rawtypes")
    @Inject(method = "outputItem(Lblusunrize/immersiveengineering/common/blocks/multiblocks/process/ProcessContext$ProcessContextInWorld;Lnet/minecraft/world/item/ItemStack;Lblusunrize/immersiveengineering/api/multiblocks/blocks/env/IMultiblockLevel;)V",
            at = @At("HEAD"))
    private void propagateOrigin(ProcessContext.ProcessContextInWorld ctx, ItemStack output,
                                  IMultiblockLevel level,
                                  CallbackInfo ci) {
        if (output.isEmpty()) return;
        MultiblockProcessInWorld<?> self = (MultiblockProcessInWorld<?>) (Object) this;
        for (int i = 0; i < self.inputItems.size(); i++) {
            InferiorOrigin origin = self.inputItems.get(i).get(TFCAlloyExt.INFERIOR_ORIGIN.get());
            if (origin != null) {
                output.set(TFCAlloyExt.INFERIOR_ORIGIN.get(), origin);
                LOG.info("outputItem: propagated origin={} to output", origin.baseMetal());
                return;
            }
        }
    }
}
