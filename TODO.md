# TODO — TFC Alloy Extension

## 近期完成 (2026-08-03)

- [x] **劣等合金工具耐久减益（游戏内验证通过）** — 劣等工具头合成的工具（工具/鱼竿）出生即损 `inferiorToolDurabilityPenalty`（默认 10%，范围 0~0.9）最大耐久。纯事件实现（ModEvents.onItemCrafted 在组件追溯后补刀），零 Mixin；TFC 无修复配方故减益持续整个工具寿命；砧锻劣等头保留 TFC 锻造加成（用户决策，加成与减益共存）

## 近期完成 (2026-08-02)

- [x] **按功能分目录重构** — 根包仅留主类；新建 config/event/client/ 包，全部金属相关收进 metal/；删除空壳 block/ModBlocks、item/ModItems
- [x] **MetalRegistration 共享注册器** — registerRegular（常规金属含锭）/ registerInferior（劣等金属复用原版物品）两级抽象，四份重复注册逻辑合并
- [x] **合金分解改为运行时读 TFC 配方** — 消除硬编码表（AlloyTypicalRatios 重写）：唯一配方无 min=0 → 全量中点；唯一配方含 min=0 → 仅基础金属；多配方 → 各配方基础金属取最小中点。/reload 后新配方立即生效
- [x] **游戏内验证通过 (0.2.0-beta)** — ①1600 铜+100 锡+100 焊锡 → 88.9% → 劣等铜 ②900 青铜+100 焊锡 → 81% → 劣等铜（青铜 5 配方命中、KubeJS 四元测试配方被动态遍历、规则 3 正确）
- [x] **审美修复（B2）** — public 修饰符（含 ModClientSetup 两个 @SubscribeEvent）、7 处完全限定名、B2.4 链式调用、通配符 import
- [x] 版本 0.2.0-beta

## 近期完成 (2026-07-18/19)

- [x] 启动无 crash，Mixins 全部加载
- [x] 7 种熔融劣等合金流体 + 锭 + 桶 + 创造标签页
- [x] 55% 阈值 / 污染扩散 / 黑箱沉没 / 三元混合 全部通过
- [x] 浇铸配方（锭 + 工具头 14 个）带 `inferior_origin` 组件
- [x] 砧锻造 (AnvilRecipeMixin) + 焊接 (WeldingRecipeMixin) 组件传递
- [x] 加热熔融 (HeatingRecipeMixin) 温度最低优先
- [x] 坩埚倾倒加速 (CruciblePourSpeedMixin，配置可控，默认 4×)
- [x] Tooltip — ModClientEvents 显示"劣等合金"
- [x] 铁劣等系统 — config 开关 + 坩埚逻辑 + 34 个锻铁加热覆写 + 锻铁浇铸
- [x] 焊锡金属 — 流体/锭/桶/浇铸/火锭 + alloy 配方
- [x] 铜部件 `neoforge:components` 加热 ×59 + 其他 6 金属 ×39
- [x] IE 合金窑/电弧炉 contaminated 配方 ×30
- [x] IE 配方匹配优先级 Mixin ×2 + 冲压/粉碎组件继承 Mixin ×2
- [x] IE/PM/tfc_ie_addon/firmalife 可选依赖 + Mixin 条件加载
- [x] 合金阈值可配置（默认 75%）
- [x] 合成追溯（PlayerEvent.ItemCraftedEvent）
- [x] 流体方块可被方块取代（`.replaceable()`）
- [x] tfc_ie_addon 熔融流体译名覆写
- [x] README + LICENSE

---

## Phase 5: PM 熔铸炉集成

- [ ] 反编译确认 FoundryTapBlockEntity.serverTick 注入位置
- [ ] 实现 FoundryTapMixin
- [ ] TFC 金属 PM 熔化配方

## Phase 6: 打磨

- [ ] 劣等合金专用纹理（目前复用 TFC 原版）
- [ ] JEI tooltip 信息
- [x] 桶颜色偏白 — 已解决（此前修复，非 0.2.0-beta 变更）

## 可选依赖测试（未测）

- [ ] **tfc_ie_addon** — 铅青铜合金配方（Cu+Pb→青铜、Cu+Pb+Sn→青铜）
- [ ] **tfc_ie_addon** — 焊锡合金配方（Sn+Bi、Sn+Pb、Sn+Pb+Bi→焊锡）
- [ ] **tfc_ie_addon** — IE Addon 劣等金属（Al/Pb/U 劣等合金生成+浇铸+加热+颜色）
- [ ] **tfc_ie_addon** — 无 addon 时启动不崩溃
- [ ] **IE** — 劣等铝/铅/铀锭加热回熔（-5% 温度）
- [ ] **IE** — Contaminated 配方
- [ ] **IE** — 无 IE 时启动不崩溃
- [ ] **PM** — 无 PM 时启动不崩溃
- [ ] **Firmalife** — 铬劣等合金测试

## 暂缓

- [ ] 铁类配方适配（铸铁/锻铁/生铁/钢体系，待设计）
