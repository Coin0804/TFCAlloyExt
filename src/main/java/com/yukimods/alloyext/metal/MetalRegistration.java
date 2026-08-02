package com.yukimods.alloyext.metal;

import com.yukimods.alloyext.util.FluidRegistrationHelper;
import net.dries007.tfc.common.blocks.MoltenFluidBlock;
import net.dries007.tfc.common.fluids.FluidHolder;
import net.dries007.tfc.common.fluids.MoltenFluid;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import static com.yukimods.alloyext.TFCAlloyExt.MOD_ID;

/**
 * 金属注册公共逻辑 —— 常规金属与劣等金属共用的流体注册骨架。
 * <p>
 * 劣等金属（inferior_*）无自有锭物品：铸造/加热产物复用 TFC 原版金属物品
 * （如 {@code tfc:metal/ingot/copper} + InferiorOrigin 组件）。
 * 常规金属（如焊锡）拥有自己的锭物品。
 * 两种注册的 ID 派生规则各自在此类集中定义（单一数据源）。
 */
public final class MetalRegistration {

    /** 注册结果（劣等金属：无自有锭，对应原版金属物品） */
    public record Result(
            FluidHolder<BaseFlowingFluid> fluid,
            DeferredHolder<Block, MoltenFluidBlock> block,
            DeferredHolder<Item, ? extends Item> bucket) {}

    /** 注册结果（常规金属：含锭物品） */
    public record RegularResult(
            FluidHolder<BaseFlowingFluid> fluid,
            DeferredHolder<Block, MoltenFluidBlock> block,
            DeferredHolder<Item, ? extends Item> bucket,
            DeferredHolder<Item, Item> ingot) {}

    private static final BlockBehaviour.Properties FLUID_BLOCK_PROPS = BlockBehaviour.Properties.of()
            .liquid().noCollission().strength(100f).noLootTable().replaceable();

    private MetalRegistration() {}

    /**
     * 常规金属注册：流体 + 方块 + 桶 + 自有锭物品。
     * <p>
     * ID 派生（如焊锡）：source {@code metal/solder}、flowing {@code metal/flowing_solder}、
     * block {@code fluid/metal/solder}、bucket {@code metal/solder_bucket}、
     * ingot {@code metal/ingot/solder}。
     */
    public static RegularResult registerRegular(
            DeferredRegister<FluidType> fluidTypes, DeferredRegister<Fluid> fluids,
            DeferredRegister<Block> blocks, DeferredRegister<Item> items,
            String name) {
        Result core = registerFluidCore(fluidTypes, fluids, blocks, items,
                "metal/" + name, "metal/flowing_" + name,
                "fluid/metal/" + name, "metal/" + name + "_bucket",
                "fluid." + MOD_ID + ".metal." + name);
        DeferredHolder<Item, Item> ingot = items.register("metal/ingot/" + name,
                () -> new Item(new Item.Properties()));
        return new RegularResult(core.fluid(), core.block(), core.bucket(), ingot);
    }

    /**
     * 劣等金属注册：流体 + 方块 + 桶，无自有锭（锭复用 TFC 原版金属物品）。
     * <p>
     * ID 派生：source {@code metal/inferior_x}、flowing {@code metal/flowing_inferior_x}、
     * block {@code fluid/metal/inferior_x}、bucket {@code inferior_x_bucket}。
     */
    public static Result registerInferior(
            DeferredRegister<FluidType> fluidTypes, DeferredRegister<Fluid> fluids,
            DeferredRegister<Block> blocks, DeferredRegister<Item> items,
            String name) {
        return registerFluidCore(fluidTypes, fluids, blocks, items,
                "metal/inferior_" + name, "metal/flowing_inferior_" + name,
                "fluid/metal/inferior_" + name, "inferior_" + name + "_bucket",
                "fluid." + MOD_ID + ".metal.inferior_" + name);
    }

    /**
     * 流体注册核心：FluidType + Source/Flowing + 方块 + 桶 + Properties。
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
    private static Result registerFluidCore(
            DeferredRegister<FluidType> fluidTypes, DeferredRegister<Fluid> fluids,
            DeferredRegister<Block> blocks, DeferredRegister<Item> items,
            String fluidId, String flowingId, String blockId, String bucketId,
            String descriptionId) {
        // 1. FluidType
        var typeHolder = fluidTypes.register(fluidId,
                () -> FluidRegistrationHelper.createMoltenFluidType(descriptionId));

        // 2. 数组引用 — 解决 Properties ↔ Fluid 循环依赖
        BaseFlowingFluid.Properties[] propsRef = new BaseFlowingFluid.Properties[1];

        // 3. Source + Flowing DeferredHolder（factory 捕获 propsRef，延迟解析）
        var sourceHolder = fluids.register(fluidId,
                () -> new MoltenFluid.Source(propsRef[0]));
        var flowingHolder = fluids.register(flowingId,
                () -> new MoltenFluid.Flowing(propsRef[0]));

        // 4. Block — flowingHolder 是 DeferredHolder，天然实现 Supplier
        var blockHolder = blocks.register(blockId,
                () -> new MoltenFluidBlock(flowingHolder, FLUID_BLOCK_PROPS));

        // 5. 桶物品 — 桶内流体渲染由 neoforge:fluid_container 模型 loader 提供
        var bucketHolder = items.register(bucketId,
                () -> new BucketItem(sourceHolder.get(), new Item.Properties().stacksTo(1)));

        // 6. Properties
        propsRef[0] = new BaseFlowingFluid.Properties(typeHolder, sourceHolder, flowingHolder)
                .block(blockHolder).bucket(bucketHolder);
        FluidRegistrationHelper.configureMoltenProperties(propsRef[0]);

        // 7. FluidHolder
        @SuppressWarnings({"rawtypes", "unchecked"})
        FluidHolder<BaseFlowingFluid> holder = new FluidHolder(typeHolder, flowingHolder, sourceHolder);
        return new Result(holder, blockHolder, bucketHolder);
    }

    /**
     * 从劣等流体注册表 ID 提取基础金属名，白名单过滤。
     * 例如 "tfc_alloy_ext:metal/inferior_copper" → "copper"（仅当白名单包含）。
     */
    @Nullable
    public static String getBaseMetalFromInferior(Fluid fluid, Set<String> allowedNames) {
        var key = BuiltInRegistries.FLUID.getKey(fluid);
        if (key == null || !MOD_ID.equals(key.getNamespace())) return null;
        String path = key.getPath();
        String name = matchInferiorPrefix(path, "metal/inferior_");
        if (name == null) {
            name = matchInferiorPrefix(path, "metal/flowing_inferior_");
        }
        return name != null && allowedNames.contains(name) ? name : null;
    }

    /** 匹配 "前缀/xxx[/子路径]" 并提取金属名 */
    @Nullable
    private static String matchInferiorPrefix(String path, String prefix) {
        if (!path.startsWith(prefix)) return null;
        int start = prefix.length();
        int end = path.indexOf('/', start);
        return end < 0 ? path.substring(start) : path.substring(start, end);
    }
}
