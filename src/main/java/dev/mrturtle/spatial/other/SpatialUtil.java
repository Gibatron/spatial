package dev.mrturtle.spatial.other;

import net.minecraft.item.ItemStack;

import java.awt.*;

public class SpatialUtil {
	public static int colorFromItemStack(ItemStack stack) {
		// Calculate hue from hash of item's english name
		float hue = (float) stack.getItem().getName().hashCode() / Integer.MAX_VALUE;
		return Color.HSBtoRGB(hue, 1.0f, 0.75f);
	}
}
