package com.yukimods.alloyext.item;

import com.yukimods.alloyext.InferiorMetal;
import com.yukimods.alloyext.fluid.InferiorMetalFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.yukimods.alloyext.TFCAlloyExt.MOD_ID;

/**
 * 本模组物品注册中心 —— 7 种流体桶。
 * <p>
 * 劣等锭不再独立注册，改为通过浇铸产 TFC 原生锭 + {@code InferiorOrigin} 组件区分。
 */
public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MOD_ID);

    public static final DeferredHolder<Item, Item> INFERIOR_COPPER_BUCKET =
            bucket(InferiorMetal.COPPER, InferiorMetalFluids.INFERIOR_COPPER);
    public static final DeferredHolder<Item, Item> INFERIOR_TIN_BUCKET =
            bucket(InferiorMetal.TIN, InferiorMetalFluids.INFERIOR_TIN);
    public static final DeferredHolder<Item, Item> INFERIOR_ZINC_BUCKET =
            bucket(InferiorMetal.ZINC, InferiorMetalFluids.INFERIOR_ZINC);
    public static final DeferredHolder<Item, Item> INFERIOR_BISMUTH_BUCKET =
            bucket(InferiorMetal.BISMUTH, InferiorMetalFluids.INFERIOR_BISMUTH);
    public static final DeferredHolder<Item, Item> INFERIOR_GOLD_BUCKET =
            bucket(InferiorMetal.GOLD, InferiorMetalFluids.INFERIOR_GOLD);
    public static final DeferredHolder<Item, Item> INFERIOR_SILVER_BUCKET =
            bucket(InferiorMetal.SILVER, InferiorMetalFluids.INFERIOR_SILVER);
    public static final DeferredHolder<Item, Item> INFERIOR_NICKEL_BUCKET =
            bucket(InferiorMetal.NICKEL, InferiorMetalFluids.INFERIOR_NICKEL);

    private static DeferredHolder<Item, Item> bucket(InferiorMetal metal, net.dries007.tfc.common.fluids.FluidHolder<?> fluid) {
        return ITEMS.register(metal.getBucketId(), () ->
                new SafeBucketItem(fluid.getSource(), new Item.Properties().stacksTo(1)));
    }
}
