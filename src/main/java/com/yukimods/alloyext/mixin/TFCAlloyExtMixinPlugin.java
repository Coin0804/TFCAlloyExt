package com.yukimods.alloyext.mixin;

import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin 插件：根据已安装的模组条件性加载 IE 和 PM 相关的 Mixin。
 * <p>
 * TFC 是硬依赖 → TFC mixin 始终加载。
 * Immersive Engineering / Productive Metalworks 是软依赖 → 仅在已安装时加载对应 mixin。
 * <p>
 * 此插件在 Mixin 解析配置阶段运行，早于类加载，
 * {@code shouldApplyMixin} 返回 {@code false} 的 mixin 不会被加载，
 * 因此对应的目标类不需要存在于 classpath 上。
 */
public class TFCAlloyExtMixinPlugin implements IMixinConfigPlugin {

    private static final Logger LOG = LoggerFactory.getLogger("TFCAlloyExt:MixinPlugin");

    private static final String IE_PREFIX = "com.yukimods.alloyext.mixin.IE";
    private static final String PM_CLASS = "com.yukimods.alloyext.mixin.FoundryTapMixin";

    private boolean ieLoaded;
    private boolean pmLoaded;

    @Override
    public void onLoad(String mixinPackage) {
        ieLoaded = isModLoaded("immersiveengineering");
        pmLoaded = isModLoaded("productivemetalworks");

        LOG.info("MixinPlugin: IE={}, PM={}", ieLoaded ? "loaded" : "missing",
                pmLoaded ? "loaded" : "missing");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // IE mixins — only when immersiveengineering is installed
        if (mixinClassName.startsWith(IE_PREFIX)) {
            if (!ieLoaded) {
                LOG.info("Skipping IE mixin {} — immersiveengineering not installed", mixinClassName);
                return false;
            }
            return true;
        }

        // PM mixin — only when productivemetalworks is installed
        if (mixinClassName.equals(PM_CLASS)) {
            if (!pmLoaded) {
                LOG.info("Skipping PM mixin FoundryTapMixin — productivemetalworks not installed");
                return false;
            }
            return true;
        }

        // TFC core mixins — always apply (TFC is required)
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass,
                         String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass,
                          String mixinClassName, IMixinInfo mixinInfo) {
    }

    /**
     * 检查指定 modId 是否在加载列表中。
     * 在 Mixin 解析阶段，需使用 {@link FMLLoader#getLoadingModList()} 而非
     * {@code ModList.get()}，因为后者可能在此时尚未完全初始化。
     */
    private static boolean isModLoaded(String modId) {
        for (ModFileInfo modFile : FMLLoader.getLoadingModList().getModFiles()) {
            if (modFile.getMods().stream().anyMatch(m -> m.getModId().equals(modId))) {
                return true;
            }
        }
        return false;
    }
}
