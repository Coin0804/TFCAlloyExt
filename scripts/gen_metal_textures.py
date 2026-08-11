#!/usr/bin/env python3
"""生成铁合金锭纹理 —— 线性/函数拼接（用户定稿 2026-08-09）。

将两种锭纹理沿对角线拼合：左下角为 A 锭、右上角为 B 锭。
对像素 (X, Y)：
  LB（左下角度）= (X - Y + 16) / 32，值域约 (0,1)
    X=0,Y=15（左下）→ LB≈0 → 几乎纯 A；X=15,Y=0（右上）→ LB≈1 → 几乎纯 B
  RU = 1 - LB
  线性拼接：color = LB·A + RU·B（f(x)=x）
  函数拼接：color = f(LB)·A + (1-f(LB))·B
    f 满足 f(0)=0、f(1)=1、关于 (0.5,0.5) 中心对称（f(x)+f(1-x)=1），例如 5 阶阶跃

用法：python scripts/gen_metal_textures.py
"""
import os

from PIL import Image

# 脚本所在目录 → 模组根 → 模组包根（单一数据源：脚本相对路径解析，独立克隆可用）
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PACK = os.path.dirname(os.path.dirname(ROOT))
FORK = os.path.join(PACK, ".projectmods", ".fork")

OUT_DIR = os.path.join(ROOT, "src/main/resources/assets/tfc_alloy_ext/textures/item/metal/ingot")

# 拼合源：A = 左下锭，B = 右上锭（相对 .fork 的路径）
SOURCES = {
    "ferronickel": (
        "TerraFirmaCraft/src/main/resources/assets/tfc/textures/item/metal/ingot/nickel.png",
        "TerraFirmaCraft/src/main/resources/assets/tfc/textures/item/metal/ingot/cast_iron.png",
    ),
    "ferrochromium": (
        "firmalife/src/main/resources/assets/firmalife/textures/item/metal/ingot/chromium.png",
        "TerraFirmaCraft/src/main/resources/assets/tfc/textures/item/metal/ingot/cast_iron.png",
    ),
}


def blend_linear(lb):
    """线性拼接（默认）：f(x) = x"""
    return lb


def blend_step5(lb):
    """5 阶阶跃函数拼接（可选复杂化）：f(0)=0、f(1)=1、中心对称 f(x)+f(1-x)=1"""
    if lb < 0.2:
        return 0.0
    if lb < 0.4:
        return 0.25
    if lb < 0.6:
        return 0.5
    if lb < 0.8:
        return 0.75
    return 1.0


# 当前使用的拼接函数（换 blend_step5 即切换为函数拼接）
BLEND = blend_linear


def main():
    for name, (rel_a, rel_b) in SOURCES.items():
        img_a = Image.open(os.path.join(FORK, rel_a)).convert("RGBA")
        img_b = Image.open(os.path.join(FORK, rel_b)).convert("RGBA")
        assert img_a.size == img_b.size == (16, 16), f"{name}: 源纹理尺寸不一致"
        out = Image.new("RGBA", (16, 16))
        for y in range(16):
            for x in range(16):
                lb = (x - y + 16) / 32  # 左下角度（用户定稿公式）
                t = BLEND(lb)
                pa = img_a.getpixel((x, y))
                pb = img_b.getpixel((x, y))
                out.putpixel((x, y), tuple(int(t * pa[i] + (1 - t) * pb[i]) for i in range(4)))
        dst = os.path.join(OUT_DIR, f"{name}.png")
        out.save(dst)
        print(f"生成: {dst}（{name}: A={os.path.basename(rel_a)} 左下, B={os.path.basename(rel_b)} 右上）")


if __name__ == "__main__":
    main()
