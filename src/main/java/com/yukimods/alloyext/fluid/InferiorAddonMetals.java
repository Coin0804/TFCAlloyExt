package com.yukimods.alloyext.fluid;

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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.yukimods.alloyext.TFCAlloyExt.MOD_ID;

/**
 * IE Addon 金属的劣等合金变体 —— 铝、铅、铀。
 * <p>
 * 仅在 {@code tfc_ie_addon} 模组已安装时激活。
 * 纯金属流体来自 {@code tfc_ie_addon:metal/aluminum|lead|uranium}，
 * 劣等变体注册到 {@code tfc_alloy_ext:metal/inferior_aluminum|lead|uranium}。
 * <p>
 * 使用与 {@link InferiorMetalFluids} 相同的注册模式。
 */
public class InferiorAddonMetals {

    private static final Logger LOG = LoggerFactory.getLogger("TFCAlloyExt:Addon");

    /** tfc_ie_addon 的 mod ID */
    public static final String ADDON_MOD_ID = "tfc_ie_addon";

    // ─── 元数据 ──────────────────────────────────────────

    public record AddonMetal(
            String name,
            int meltingTemp,
            int color,
            String pureFluidId  // "tfc_ie_addon:metal/xxx"
    ) {}

    public static final AddonMetal ALUMINUM = new AddonMetal("aluminum", 1653, 0xFFC8D6E0, "tfc_ie_addon:metal/aluminum");
    public static final AddonMetal LEAD     = new AddonMetal("lead",     314,  0xFF5A5A6E, "tfc_ie_addon:metal/lead");
    public static final AddonMetal URANIUM  = new AddonMetal("uranium",  1074, 0xFF4A5E3C, "tfc_ie_addon:metal/uranium");

    public static final AddonMetal[] ALL = {ALUMINUM, LEAD, URANIUM};

    /** 纯金属名称集合（按注册顺序），供 InferiorAlloyLogic 使用 */
    static final Set<String> PURE_METAL_NAMES;

    static {
        var names = new LinkedHashSet<String>();
        for (var m : ALL) names.add(m.name());
        PURE_METAL_NAMES = Collections.unmodifiableSet(names);
    }

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

    // ─── 注册结果 ──────────────────────────────────────────

    private static final Map<String, FluidHolder<BaseFlowingFluid>> FLUID_MAP = new LinkedHashMap<>();
    private static final Map<String, DeferredHolder<Block, MoltenFluidBlock>> BLOCK_MAP = new LinkedHashMap<>();
    private static final Map<String, DeferredHolder<Item, ? extends Item>> BUCKET_MAP = new LinkedHashMap<>();

    public static final FluidHolder<BaseFlowingFluid> INFERIOR_ALUMINUM = register(ALUMINUM);
    public static final FluidHolder<BaseFlowingFluid> INFERIOR_LEAD     = register(LEAD);
    public static final FluidHolder<BaseFlowingFluid> INFERIOR_URANIUM  = register(URANIUM);

    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_ALUMINUM_BLOCK = getBlock("aluminum");
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_LEAD_BLOCK     = getBlock("lead");
    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_URANIUM_BLOCK  = getBlock("uranium");

    public static final DeferredHolder<Item, Item> INFERIOR_ALUMINUM_BUCKET = getBucket("aluminum");
    public static final DeferredHolder<Item, Item> INFERIOR_LEAD_BUCKET     = getBucket("lead");
    public static final DeferredHolder<Item, Item> INFERIOR_URANIUM_BUCKET  = getBucket("uranium");

    @SuppressWarnings("unchecked")
    private static <T extends Block> DeferredHolder<Block, T> getBlock(String name) {
        return (DeferredHolder<Block, T>) BLOCK_MAP.get(name);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Item> DeferredHolder<Item, T> getBucket(String name) {
        return (DeferredHolder<Item, T>) BUCKET_MAP.get(name);
    }

    // ─── 注册逻辑 ──────────────────────────────────────────

    private static FluidHolder<BaseFlowingFluid> register(AddonMetal metal) {
        String fluidId = "metal/inferior_" + metal.name();
        String flowingId = "metal/flowing_inferior_" + metal.name();
        String blockId = "fluid/metal/inferior_" + metal.name();

        // 1. FluidType
        var typeHolder = FLUID_TYPES.register(fluidId,
                () -> FluidRegistrationHelper.createMoltenFluidType(
                        "fluid." + MOD_ID + ".metal.inferior_" + metal.name()));

        // 2. 数组引用
        BaseFlowingFluid.Properties[] propsRef = new BaseFlowingFluid.Properties[1];

        // 3. Source + Flowing
        var sourceHolder = FLUIDS.register(fluidId,
                () -> new MoltenFluid.Source(propsRef[0]));
        var flowingHolder = FLUIDS.register(flowingId,
                () -> new MoltenFluid.Flowing(propsRef[0]));

        // 4. Block
        var blockHolder = BLOCKS.register(blockId,
                () -> new MoltenFluidBlock(flowingHolder, FLUID_BLOCK_PROPS));
        BLOCK_MAP.put(metal.name(), blockHolder);

        // 5. 桶物品 — 与 ModItems 一致的 BucketItem 模式
        String bucketId = "inferior_" + metal.name() + "_bucket";
        var bucketHolder = ITEMS.register(bucketId,
                () -> new BucketItem(sourceHolder.get(), new Item.Properties().stacksTo(1)));
        BUCKET_MAP.put(metal.name(), bucketHolder);

        // 6. Properties
        propsRef[0] = new BaseFlowingFluid.Properties(typeHolder, sourceHolder, flowingHolder)
                .block(blockHolder).bucket(bucketHolder);
        FluidRegistrationHelper.configureMoltenProperties(propsRef[0]);

        // 7. FluidHolder
        @SuppressWarnings({"rawtypes", "unchecked"})
        FluidHolder<BaseFlowingFluid> holder = new FluidHolder(typeHolder, flowingHolder, sourceHolder);
        FLUID_MAP.put(metal.name(), holder);
        return holder;
    }

    // ─── 工具方法 ──────────────────────────────────────────

    /** 仅在 tfc_ie_addon 已安装时返回 true */
    public static boolean isEnabled() {
        return net.neoforged.fml.ModList.get().isLoaded(ADDON_MOD_ID);
    }

    /** 按基础金属名获取 FluidHolder */
    @Nullable
    public static FluidHolder<BaseFlowingFluid> getFluid(String baseMetal) {
        return FLUID_MAP.get(baseMetal);
    }

    /** 检查流体是否为 IE addon 的劣等变体 */
    public static boolean isInferiorAddonFluid(Fluid fluid) {
        return getBaseMetalFromInferior(fluid) != null;
    }

    /**
     * 反向查找：Fluid → IE addon 基础金属名。
     * 基于注册表 ID 前缀匹配 {@code metal/inferior_}。
     */
    @Nullable
    public static String getBaseMetalFromInferior(Fluid fluid) {
        var key = BuiltInRegistries.FLUID.getKey(fluid);
        if (key == null || !MOD_ID.equals(key.getNamespace())) return null;
        String path = key.getPath();
        if (path.startsWith("metal/inferior_")) {
            int start = "metal/inferior_".length();
            int end = path.indexOf('/', start);
            String name = end < 0 ? path.substring(start) : path.substring(start, end);
            return PURE_METAL_NAMES.contains(name) ? name : null;
        }
        if (path.startsWith("metal/flowing_inferior_")) {
            int start = "metal/flowing_inferior_".length();
            int end = path.indexOf('/', start);
            String name = end < 0 ? path.substring(start) : path.substring(start, end);
            return PURE_METAL_NAMES.contains(name) ? name : null;
        }
        return null;
    }

    /**
     * 从 {@code tfc_ie_addon:metal/xxx} 流体提取纯金属名称（非劣等）,
     * 仅匹配 Al/Pb/U，不匹配 constantan/electrum 等合金。
     */
    @Nullable
    public static String extractAddonPureMetalName(Fluid fluid) {
        if (!isEnabled()) return null;
        var key = BuiltInRegistries.FLUID.getKey(fluid);
        if (key == null || !ADDON_MOD_ID.equals(key.getNamespace())) return null;
        String path = key.getPath();
        if (path.startsWith("metal/")) {
            String name = path.substring("metal/".length());
            // 只接受单质金属，排除 alloy/constantan/electrum
            return PURE_METAL_NAMES.contains(name) ? name : null;
        }
        return null;
    }

    /** 获取对应纯金属流体 ID（"tfc_ie_addon:metal/xxx"） */
    public static String getPureFluidId(String baseMetal) {
        for (var m : ALL) {
            if (m.name().equals(baseMetal)) return m.pureFluidId();
        }
        return null;
    }

    /** 获取金属颜色 */
    public static int getColor(String baseMetal) {
        for (var m : ALL) {
            if (m.name().equals(baseMetal)) return m.color();
        }
        return 0xFFFFFFFF;
    }

    /** IE 物品 ID 映射（锭） */
    public static String getIEIngotId(String baseMetal) {
        return "immersiveengineering:ingot_" + baseMetal;
    }
}
