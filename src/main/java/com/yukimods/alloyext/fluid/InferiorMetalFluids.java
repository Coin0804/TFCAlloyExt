package com.yukimods.alloyext.fluid;

import com.yukimods.alloyext.InferiorMetal;
import com.yukimods.alloyext.util.FluidRegistrationHelper;
import net.dries007.tfc.common.blocks.MoltenFluidBlock;
import net.dries007.tfc.common.fluids.FluidHolder;
import net.dries007.tfc.common.fluids.MoltenFluid;
import net.minecraft.core.registries.BuiltInRegistries;
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

import java.util.*;

import static com.yukimods.alloyext.TFCAlloyExt.MOD_ID;

/**
 * 7 种「熔融劣等X合金」流体的注册与查询。
 * <p>
 * 采用标准 NeoForge DeferredRegister + DeferredHolder 模式。
 * DeferredHolder 实现了 {@link java.util.function.Supplier}，
 * 可安全传递到 {@link BaseFlowingFluid.Properties} 和 {@link MoltenFluidBlock} 中。
 * 所有 {@code get()} 调用在注册事件触发时才解析，不存在运行时顺序问题。
 * <p>
 * 同时持有 {@link #BLOCKS} DeferredRegister 和方块 DeferredHolder，
 * 避免 ModBlocks ↔ InferiorMetalFluids 双向引用。
 */
public class InferiorMetalFluids {

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

    // ─── 内部 map（register() 填充，按 static 声明顺序执行） ─

    private static final Map<String, FluidHolder<BaseFlowingFluid>> FLUID_MAP = new LinkedHashMap<>();
    private static final Map<String, DeferredHolder<Block, MoltenFluidBlock>> BLOCK_MAP = new LinkedHashMap<>();
    private static final Map<String, DeferredHolder<Item, ? extends Item>> BUCKET_MAP = new LinkedHashMap<>();

    // ─── 流体：静态初始化时依次注册 ────────────────────────

    public static final FluidHolder<BaseFlowingFluid> INFERIOR_COPPER  = register(InferiorMetal.COPPER);
    public static final FluidHolder<BaseFlowingFluid> INFERIOR_TIN     = register(InferiorMetal.TIN);
    public static final FluidHolder<BaseFlowingFluid> INFERIOR_ZINC    = register(InferiorMetal.ZINC);
    public static final FluidHolder<BaseFlowingFluid> INFERIOR_BISMUTH = register(InferiorMetal.BISMUTH);
    public static final FluidHolder<BaseFlowingFluid> INFERIOR_GOLD    = register(InferiorMetal.GOLD);
    public static final FluidHolder<BaseFlowingFluid> INFERIOR_SILVER  = register(InferiorMetal.SILVER);
    public static final FluidHolder<BaseFlowingFluid> INFERIOR_NICKEL  = register(InferiorMetal.NICKEL);

    // ─── 方块 ──────────────────────────────────────────

    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_COPPER_BLOCK  = getBlock("copper");
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_TIN_BLOCK     = getBlock("tin");
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_ZINC_BLOCK    = getBlock("zinc");
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_BISMUTH_BLOCK = getBlock("bismuth");
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_GOLD_BLOCK    = getBlock("gold");
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_SILVER_BLOCK  = getBlock("silver");
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_NICKEL_BLOCK  = getBlock("nickel");

    // ─── 桶物品 ────────────────────────────────────────

    public static final DeferredHolder<Item, Item> INFERIOR_COPPER_BUCKET  = getBucket("copper");
    public static final DeferredHolder<Item, Item> INFERIOR_TIN_BUCKET     = getBucket("tin");
    public static final DeferredHolder<Item, Item> INFERIOR_ZINC_BUCKET    = getBucket("zinc");
    public static final DeferredHolder<Item, Item> INFERIOR_BISMUTH_BUCKET = getBucket("bismuth");
    public static final DeferredHolder<Item, Item> INFERIOR_GOLD_BUCKET    = getBucket("gold");
    public static final DeferredHolder<Item, Item> INFERIOR_SILVER_BUCKET  = getBucket("silver");
    public static final DeferredHolder<Item, Item> INFERIOR_NICKEL_BUCKET  = getBucket("nickel");

    @SuppressWarnings("unchecked")
    private static <T extends Block> DeferredHolder<Block, T> getBlock(String name) {
        return (DeferredHolder<Block, T>) BLOCK_MAP.get(name);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Item> DeferredHolder<Item, T> getBucket(String name) {
        return (DeferredHolder<Item, T>) BUCKET_MAP.get(name);
    }

    // ─── 注册逻辑 ──────────────────────────────────────────

    /**
     * 注册一组劣等金属流体及其对应方块。
     *
     * <h3>Properties ↔ Fluid 循环引用</h3>
     * <ul>
     *   <li>FluidType — 独立创建，无上游依赖</li>
     *   <li>Source / Flowing — 需要 Properties（但 Properties 又需要它们作为 Supplier）</li>
     *   <li>Block — 需要 Flowing 的 Supplier</li>
     *   <li>Properties — 需要 FluidType + Source + Flowing + Block 的 Supplier</li>
     * </ul>
     * 使用单元素数组 {@code propsRef} 打破 "effectively final" 限制：
     * DeferredRegister 的 factory lambda 捕获数组引用，数组内容在 Properties 创建后写入。
     * DeferredHolder.get() 仅在注册事件触发时才调用（BLOCK → FLUID_TYPES → FLUID），
     * 不会在类加载期间提前 resolve。
     */
    private static FluidHolder<BaseFlowingFluid> register(InferiorMetal metal) {
        // 1. FluidType
        var typeHolder = FLUID_TYPES.register(metal.getFluidId(),
                () -> FluidRegistrationHelper.createMoltenFluidType(
                        "fluid." + MOD_ID + ".metal.inferior_" + metal.getName()));

        // 2. 数组引用 — 解决 Properties ↔ Fluid 循环依赖
        BaseFlowingFluid.Properties[] propsRef = new BaseFlowingFluid.Properties[1];

        // 3. Source + Flowing DeferredHolder（factory 捕获 propsRef，延迟解析）
        var sourceHolder = FLUIDS.register(metal.getFluidId(),
                () -> new MoltenFluid.Source(propsRef[0]));
        var flowingHolder = FLUIDS.register(metal.getFlowingFluidId(),
                () -> new MoltenFluid.Flowing(propsRef[0]));

        // 4. Block — flowingHolder 是 DeferredHolder，天然实现 Supplier
        var blockHolder = BLOCKS.register(metal.getBlockId(),
                () -> new MoltenFluidBlock(flowingHolder, FLUID_BLOCK_PROPS));
        BLOCK_MAP.put(metal.getName(), blockHolder);

        // 5. 桶物品 — SafeBucketItem（禁用放置交互）
        var bucketHolder = ITEMS.register(metal.getBucketId(),
                () -> new BucketItem(sourceHolder.get(), new Item.Properties().stacksTo(1)));
        @SuppressWarnings({"rawtypes", "unchecked"})
        var bucketEntry = (DeferredHolder<Item, ? extends Item>) (DeferredHolder<?, ?>) bucketHolder;
        BUCKET_MAP.put(metal.getName(), bucketEntry);

        // 6. Properties
        propsRef[0] = new BaseFlowingFluid.Properties(typeHolder, sourceHolder, flowingHolder)
                .block(blockHolder).bucket(bucketHolder);
        FluidRegistrationHelper.configureMoltenProperties(propsRef[0]);

        // 7. FluidHolder
        @SuppressWarnings({"rawtypes", "unchecked"})
        FluidHolder<BaseFlowingFluid> holder = new FluidHolder(typeHolder, flowingHolder, sourceHolder);
        FLUID_MAP.put(metal.getName(), holder);
        return holder;
    }

    // ─── 工具方法 ──────────────────────────────────────────

    /** 基于注册表 ID 前缀判断，O(1) */
    public static boolean isInferiorFluid(Fluid fluid) {
        return getBaseMetalFromInferior(fluid) != null;
    }

    /** 按基础金属名获取 FluidHolder */
    public static FluidHolder<BaseFlowingFluid> getInferiorFluid(String baseMetal) {
        return FLUID_MAP.get(baseMetal);
    }

    /**
     * 反向查找：Fluid → 基础金属名。
     * 基于注册表 ID 前缀匹配，O(1) 字符串操作，替代旧版 O(n) 遍历比较。
     */
    public static String getBaseMetalFromInferior(Fluid fluid) {
        var key = BuiltInRegistries.FLUID.getKey(fluid);
        if (key == null || !MOD_ID.equals(key.getNamespace())) return null;
        String path = key.getPath();
        if (path.startsWith("metal/inferior_")) {
            int start = "metal/inferior_".length();
            int end = path.indexOf('/', start);
            return end < 0 ? path.substring(start) : path.substring(start, end);
        }
        if (path.startsWith("metal/flowing_inferior_")) {
            int start = "metal/flowing_inferior_".length();
            int end = path.indexOf('/', start);
            return end < 0 ? path.substring(start) : path.substring(start, end);
        }
        return null;
    }

    /** 所有已注册的劣等合金流体 */
    public static Collection<FluidHolder<BaseFlowingFluid>> all() {
        return Collections.unmodifiableCollection(FLUID_MAP.values());
    }
}
