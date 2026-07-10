package com.yukimods.alloyext.item;

import com.yukimods.alloyext.InferiorMetal;
import com.yukimods.alloyext.fluid.InferiorMetalFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.yukimods.alloyext.TFCAlloyExt.MOD_ID;

/**
 * 本模组物品注册中心 —— 7 种劣等合金锭 + 7 种流体桶。
 */
public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MOD_ID);

    // ─── 劣等合金锭 ────────────────────────────────────────

    public static final DeferredHolder<Item, Item> INFERIOR_COPPER_INGOT =
            ITEMS.register(InferiorMetal.COPPER.getIngotId(), () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFERIOR_TIN_INGOT =
            ITEMS.register(InferiorMetal.TIN.getIngotId(), () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFERIOR_ZINC_INGOT =
            ITEMS.register(InferiorMetal.ZINC.getIngotId(), () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFERIOR_BISMUTH_INGOT =
            ITEMS.register(InferiorMetal.BISMUTH.getIngotId(), () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFERIOR_GOLD_INGOT =
            ITEMS.register(InferiorMetal.GOLD.getIngotId(), () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFERIOR_SILVER_INGOT =
            ITEMS.register(InferiorMetal.SILVER.getIngotId(), () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> INFERIOR_NICKEL_INGOT =
            ITEMS.register(InferiorMetal.NICKEL.getIngotId(), () -> new Item(new Item.Properties()));

    // ─── 流体桶（SafeBucketItem：有流体渲染，无交互） ──────

    public static final DeferredHolder<Item, Item> INFERIOR_COPPER_BUCKET =
            ITEMS.register(InferiorMetal.COPPER.getBucketId(), () ->
                    new SafeBucketItem(InferiorMetalFluids.INFERIOR_COPPER.getSource(),
                            new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> INFERIOR_TIN_BUCKET =
            ITEMS.register(InferiorMetal.TIN.getBucketId(), () ->
                    new SafeBucketItem(InferiorMetalFluids.INFERIOR_TIN.getSource(),
                            new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> INFERIOR_ZINC_BUCKET =
            ITEMS.register(InferiorMetal.ZINC.getBucketId(), () ->
                    new SafeBucketItem(InferiorMetalFluids.INFERIOR_ZINC.getSource(),
                            new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> INFERIOR_BISMUTH_BUCKET =
            ITEMS.register(InferiorMetal.BISMUTH.getBucketId(), () ->
                    new SafeBucketItem(InferiorMetalFluids.INFERIOR_BISMUTH.getSource(),
                            new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> INFERIOR_GOLD_BUCKET =
            ITEMS.register(InferiorMetal.GOLD.getBucketId(), () ->
                    new SafeBucketItem(InferiorMetalFluids.INFERIOR_GOLD.getSource(),
                            new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> INFERIOR_SILVER_BUCKET =
            ITEMS.register(InferiorMetal.SILVER.getBucketId(), () ->
                    new SafeBucketItem(InferiorMetalFluids.INFERIOR_SILVER.getSource(),
                            new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> INFERIOR_NICKEL_BUCKET =
            ITEMS.register(InferiorMetal.NICKEL.getBucketId(), () ->
                    new SafeBucketItem(InferiorMetalFluids.INFERIOR_NICKEL.getSource(),
                            new Item.Properties().stacksTo(1)));
}
