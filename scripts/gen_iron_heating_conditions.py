#!/usr/bin/env python3
"""给锻铁加热配方批量添加「铁劣等系统」配置条件（用户定稿 2026-08-09）。

范围：
- data/tfc/recipe/heating/metal/**/wrought_iron.json   （锻铁物品加热）
- data/tfc/recipe/heating/wrought_iron_grill.json      （锻铁烤架）
- data/tfcsuperhammer/recipe/heating/wrought_iron_*    （跨模组锻铁加热）
- data/precisionprospecting/recipe/heating/metal/wrought_iron/*

铸造配方（wrought_iron_ingot/casting）不挂。
条件：{ "type": "tfc_alloy_ext:config_enabled", "config": "enableIronInferiorSystem" }
配置关闭时配方不加载，锻铁加热保持 TFC 原版行为。

用法：python scripts/gen_iron_heating_conditions.py
"""
import json
import os
import glob

# 脚本所在目录 → 模组根（单一数据源：脚本相对路径解析，独立克隆可用）
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src/main/resources/data")

CONDITION = {
    "type": "tfc_alloy_ext:config_enabled",
    "config": "enableIronInferiorSystem",
}

# 目标 glob（与用户确认的范围一一对应）
GLOBS = [
    os.path.join(DATA, "tfc/recipe/heating/metal/**/wrought_iron.json"),
    os.path.join(DATA, "tfc/recipe/heating/wrought_iron_grill.json"),
    os.path.join(DATA, "tfcsuperhammer/recipe/heating/wrought_iron_*.json"),
    os.path.join(DATA, "precisionprospecting/recipe/heating/metal/wrought_iron/*.json"),
]


def main():
    targets = sorted({p for g in GLOBS for p in glob.glob(g, recursive=True)})
    changed = 0
    skipped = []
    for path in targets:
        with open(path, encoding="utf-8") as f:
            data = json.load(f)
        existing = data.get("neoforge:conditions", [])
        if any(c.get("type") == CONDITION["type"] for c in existing):
            skipped.append(os.path.relpath(path, ROOT))
            continue
        existing.append(CONDITION)
        data["neoforge:conditions"] = existing
        with open(path, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, separators=(",", ":"))
        changed += 1
        print(f"已添加条件: {os.path.relpath(path, ROOT)}")
    print(f"\n完成: {changed} 个配方添加条件, {len(skipped)} 个已存在跳过")
    for s in skipped:
        print(f"  跳过: {s}")


if __name__ == "__main__":
    main()
