package com.yukimods.alloyext.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.yukimods.alloyext.TFCAlloyExt.MOD_ID;

/**
 * 备用的物品注册中心。
 * <p>
 * 流体桶已迁移至 {@link com.yukimods.alloyext.fluid.InferiorMetalFluids#ITEMS}，
 * 焊锡桶在 {@link com.yukimods.alloyext.metal.SolderMetal#ITEMS}，
 * IE Addon 桶在 {@link com.yukimods.alloyext.fluid.InferiorAddonMetals#ITEMS}。
 */
public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MOD_ID);

    // 未来如果有非桶物品可在此注册
}
