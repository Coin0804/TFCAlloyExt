package com.yukimods.alloyext.metal;

import com.yukimods.alloyext.util.FluidRegistrationHelper;
import net.dries007.tfc.common.blocks.MoltenFluidBlock;
import net.dries007.tfc.common.fluids.FluidHolder;
import net.dries007.tfc.common.fluids.MoltenFluid;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static com.yukimods.alloyext.TFCAlloyExt.MOD_ID;

/**
 * 焊锡 (Solder) 金属注册 —— 流体 + 方块 + 物品。
 * <p>
 * 焊锡是 TFC IE Addon 铅金属 + TFC 锡/铋 的合金产物。
 * 非劣等合金，是独立的合金金属。
 */
public class SolderMetal {

    // ─── ID 常量 ──────────────────────────────────────────

    public static final String METAL_NAME = "solder";
    public static final String FLUID_ID = "metal/solder";
    public static final String FLOWING_FLUID_ID = "metal/flowing_solder";
    public static final String BLOCK_ID = "fluid/metal/solder";
    public static final String BUCKET_ID = "metal/solder_bucket";
    public static final String INGOT_ID = "metal/ingot/solder";

    // ─── DeferredRegister ─────────────────────────────────

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, MOD_ID);
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, MOD_ID);

    private static final BlockBehaviour.Properties FLUID_BLOCK_PROPS = BlockBehaviour.Properties.of()
            .liquid().noCollission().strength(100f).noLootTable().replaceable();

    // ─── 注册 Holder ──────────────────────────────────────

    public static final DeferredHolder<FluidType, FluidType> FLUID_TYPE;
    public static final FluidHolder<BaseFlowingFluid> FLUID;
    public static final DeferredHolder<Block, MoltenFluidBlock> FLUID_BLOCK;
    public static final DeferredHolder<Item, Item> INGOT;
    public static final DeferredHolder<Item, Item> BUCKET;

    static {
        // 1. FluidType
        FLUID_TYPE = FLUID_TYPES.register(FLUID_ID,
                () -> FluidRegistrationHelper.createMoltenFluidType("fluid." + MOD_ID + ".metal.solder"));

        // 2. 数组引用 — 解决 Properties ↔ Fluid 循环依赖
        BaseFlowingFluid.Properties[] propsRef = new BaseFlowingFluid.Properties[1];

        // 3. Source + Flowing
        var sourceHolder = FLUIDS.register(FLUID_ID,
                () -> new MoltenFluid.Source(propsRef[0]));
        var flowingHolder = FLUIDS.register(FLOWING_FLUID_ID,
                () -> new MoltenFluid.Flowing(propsRef[0]));

        // 4. Block
        FLUID_BLOCK = BLOCKS.register(BLOCK_ID,
                () -> new MoltenFluidBlock(flowingHolder, FLUID_BLOCK_PROPS));

        // 5. 物品（必须在 Properties 之前注册，因为 .bucket() 需要引用）
        INGOT = ITEMS.register(INGOT_ID,
                () -> new Item(new Item.Properties()));
        BUCKET = ITEMS.register(BUCKET_ID,
                () -> new BucketItem(
                        sourceHolder.get(), new Item.Properties().stacksTo(1)));

        // 6. Properties
        propsRef[0] = new BaseFlowingFluid.Properties(FLUID_TYPE, sourceHolder, flowingHolder)
                .block(FLUID_BLOCK).bucket(BUCKET);
        FluidRegistrationHelper.configureMoltenProperties(propsRef[0]);

        // 7. FluidHolder
        @SuppressWarnings({"rawtypes", "unchecked"})
        FluidHolder<BaseFlowingFluid> holder = new FluidHolder(FLUID_TYPE, flowingHolder, sourceHolder);
        FLUID = holder;
    }

    // ─── 工具方法 ──────────────────────────────────────────

    /** 检查流体是否为焊锡（源或流动） */
    public static boolean isSolderFluid(Fluid fluid) {
        var key = net.minecraft.core.registries.BuiltInRegistries.FLUID.getKey(fluid);
        if (key == null || !MOD_ID.equals(key.getNamespace())) return false;
        String path = key.getPath();
        return path.equals(FLUID_ID) || path.equals(FLOWING_FLUID_ID);
    }
}
