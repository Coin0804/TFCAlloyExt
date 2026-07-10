package com.yukimods.alloyext.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

/**
 * 安全流体桶 —— 继承 BucketItem 获得流体渲染，但禁用放置/回收交互。
 * <p>
 * 用于创造标签页和 JEI 显示，不参与生存玩法。
 */
public class SafeBucketItem extends BucketItem {

    public SafeBucketItem(Fluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        // 完全禁用交互：不倒出、不回收流体
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}
