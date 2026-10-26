#!/usr/bin/env python3
"""Render PNG launcher icons (API 21-25) from the existing vector design.

Source vectors:
  ic_launcher_background.xml  -> solid #0F141C 108x108
  ic_launcher_foreground.xml  -> shield #00E676 + bolt #0F141C on 108 viewport
"""
import os
from PIL import Image, ImageDraw

BG = (0x0F, 0x14, 0x1C, 255)
GREEN = (0x00, 0xE6, 0x76, 255)

# Shield outline in the 108x108 viewport, flattened from the cubic curve.
SHIELD = [(54, 20), (78, 32), (78, 54), (54, 88), (30, 54), (30, 32)]
# Lightning bolt, straight from the vector path.
BOLT = [(55, 36), (43, 53), (53, 53), (51, 70), (64, 51), (54, 51)]

SIZES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}
SS = 8  # supersample factor for smooth edges


def scale(points, factor):
    return [(x * factor, y * factor) for x, y in points]


def render(size, round_icon):
    w = size * SS
    img = Image.new("RGBA", (w, w), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    if round_icon:
        d.ellipse([0, 0, w - 1, w - 1], fill=BG)
    else:
        d.rectangle([0, 0, w, w], fill=BG)

    # Foreground is drawn at 108-viewport scale, centred.
    f = w / 108.0
    d.polygon(scale(SHIELD, f), fill=GREEN)
    d.polygon(scale(BOLT, f), fill=BG)

    return img.resize((size, size), Image.LANCZOS)


def main():
    res_root = "app/src/main/res"
    for bucket, size in SIZES.items():
        out_dir = os.path.join(res_root, f"mipmap-{bucket}")
        os.makedirs(out_dir, exist_ok=True)
        for name, is_round in (("ic_launcher", False), ("ic_launcher_round", True)):
            path = os.path.join(out_dir, f"{name}.png")
            render(size, is_round).save(path, "PNG", optimize=True)
            print(f"  {path}  ({size}x{size})")


if __name__ == "__main__":
    main()
