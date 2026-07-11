# TODO — TFC Alloy Extension

## 验证通过

- [x] 启动无 crash，Mixins 全部加载
- [x] 7 种熔融劣等合金流体 + 锭 + 桶 + 创造标签页
- [x] 55% 阈值 / 污染扩散 / 黑箱沉没 / 三元混合 全部通过
- [x] 浇铸配方（锭 + 工具头 14 个）带 `inferior_origin` 组件
- [x] 砧锻造 → AnvilRecipeMixin 传递组件
- [x] 焊接 → WeldingRecipeMixin 传递组件
- [x] 加热熔融 → HeatingRecipeMixin 温度最低优先
- [x] 坩埚倾倒加速 → CruciblePourSpeedMixin（配置可控，默认 4×）
- [x] Tooltip → ModClientEvents 显示"劣等合金"
- [x] 铜部件 `neoforge:components` 加热配方 ×59
- [x] 其他 6 金属加热配方 ×39
- [x] IE 合金窑/电弧炉 contaminated 配方 ×30
- [x] IE 配方匹配优先级 Mixin ×2
- [x] IE 冲压/粉碎组件继承 Mixin ×2

## Phase 5: PM 熔铸炉集成

- [ ] 反编译确认 FoundryTapBlockEntity.serverTick 注入位置
- [ ] 实现 FoundryTapMixin
- [ ] TFC 金属 PM 熔化配方

## Phase 6: 打磨

- [ ] 劣等合金专用纹理（目前复用原版 TFC）
- [ ] JEI tooltip 信息
- [ ] 桶颜色偏白修复
- [ ] 铁类配方适配（铸铁/锻铁/生铁/钢体系，待设计）
