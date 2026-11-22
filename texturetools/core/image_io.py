# SPDX-License-Identifier: GPL-3.0-only
"""Image load and save shared by every engine: float32 RGBA arrays in, raw RGBA PNG out (no gAMA,
sRGB or ICC chunks). From combine_tex.py, behavior unchanged (plan_texture_tools_gui.md P0).
"""

import numpy as np
from PIL import Image


def load_rgba_array(path):
	img = Image.open(path).convert("RGBA")
	return np.array(img).astype(np.float32)


def save_raw(result_arr, output):
	"""Write raw pixel data only -- no gAMA/sRGB/ICC chunks get carried over,
	so colors stay exactly as computed."""
	result_arr = np.clip(result_arr, 0, 255).astype(np.uint8)
	result_img = Image.new("RGBA", (result_arr.shape[1], result_arr.shape[0]))
	result_img.putdata([tuple(p) for p in result_arr.reshape(-1, 4)])
	result_img.save(output)
