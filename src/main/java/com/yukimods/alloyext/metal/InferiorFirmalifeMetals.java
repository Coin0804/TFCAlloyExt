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

import java.util.*;

import static com.yukimods.alloyext.TFCAlloyExt.MOD_ID;

/**
 * Firmalife 金属的劣等合金变体 —— 铬。
 * <p>
 * 仅在 {@code firmalife} 模组已安装时激活。
 * 纯金属流体来自 {@code firmalife:metal/chromium}。
 * 不锈钢是合金，不产生劣等变体。
 */
public class InferiorFirmalifeMetals {

    public static final String ADDON_MOD_ID = "firmalife";

    // ─── 元数据 ──────────────────────────────────────────

    record FirmalifeMetal(String name, int meltingTemp, int color, String pureFluidId) {}

    // Chromium: 1250°C * 0.95 = 1187.5 → 1188; color -0x0F0F0F from 0xF5F6FF → 0xE6E7F0
    public static final FirmalifeMetal CHROMIUM = new FirmalifeMetal("chromium", 1188, 0xFFE6E7F0, "firmalife:metal/chromium");

    public static final Set<String> PURE_METAL_NAMES =
            Collections.unmodifiableSet(new LinkedHashSet<>(Set.of(CHROMIUM.name())));

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

    public static final FluidHolder<BaseFlowingFluid> INFERIOR_CHROMIUM = register(CHROMIUM);

    public static final DeferredHolder<Block, MoltenFluidBlock> INFERIOR_CHROMIUM_BLOCK = getBlock("chromium");
    public static final DeferredHolder<Item, Item> INFERIOR_CHROMIUM_BUCKET = getBucket("chromium");

    @SuppressWarnings("unchecked")
    private static <T extends Block> DeferredHolder<Block, T> getBlock(String name) {
        return (DeferredHolder<Block, T>) BLOCK_MAP.get(name);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Item> DeferredHolder<Item, T> getBucket(String name) {
        return (DeferredHolder<Item, T>) BUCKET_MAP.get(name);
    }

    // ─── 注册逻辑 ──────────────────────────────────────────

    private static FluidHolder<BaseFlowingFluid> register(FirmalifeMetal metal) {
        String fluidId = "metal/inferior_" + metal.name();
        String flowingId = "metal/flowing_inferior_" + metal.name();
        String blockId = "fluid/metal/inferior_" + metal.name();
        String bucketId = "inferior_" + metal.name() + "_bucket";

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

        // 5. 桶物品
        var bucketHolder = ITEMS.register(bucketId,
                () -> new BucketItem(sourceHolder.get(), new Item.Properties().stacksTo(1)));
        @SuppressWarnings({"unchecked"})
        var bucketEntry = (DeferredHolder<Item, ? extends Item>) (DeferredHolder<?, ?>) bucketHolder;
        BUCKET_MAP.put(metal.name(), bucketEntry);

        // 6. Properties
        propsRef[0] = new BaseFlowingFluid.Properties(typeHolder, sourceHolder, flowingHolder)
                .block(blockHolder).bucket(bucketEntry);
        FluidRegistrationHelper.configureMoltenProperties(propsRef[0]);

        // 7. FluidHolder
        @SuppressWarnings({"rawtypes", "unchecked"})
        FluidHolder<BaseFlowingFluid> holder = new FluidHolder(typeHolder, flowingHolder, sourceHolder);
        FLUID_MAP.put(metal.name(), holder);
        return holder;
    }

    // ─── 工具方法 ──────────────────────────────────────────

    public static boolean isEnabled() {
        return net.neoforged.fml.ModList.get().isLoaded(ADDON_MOD_ID);
    }

    @Nullable
    public static FluidHolder<BaseFlowingFluid> getFluid(String baseMetal) {
        return FLUID_MAP.get(baseMetal);
    }

    public static boolean isInferiorFirmalifeFluid(Fluid fluid) {
        return getBaseMetalFromInferior(fluid) != null;
    }

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

    @Nullable
    public static String extractFirmalifePureMetalName(Fluid fluid) {
        if (!isEnabled()) return null;
        var key = BuiltInRegistries.FLUID.getKey(fluid);
        if (key == null || !ADDON_MOD_ID.equals(key.getNamespace())) return null;
        String path = key.getPath();
        if (path.startsWith("metal/")) {
            String name = path.substring("metal/".length());
            return PURE_METAL_NAMES.contains(name) ? name : null;
        }
        return null;
    }

    public static int getColor(String baseMetal) {
        return CHROMIUM.color();
    }
}
