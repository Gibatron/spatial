package dev.mrturtle.spatial.other;

import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.Random;

public class SpatialUtil {
	public static int colorFromItemStack(ItemStack stack) {
		Random rand = new Random();
		rand.setSeed(stack.getItem().getName().hashCode());
		return Color.HSBtoRGB(rand.nextFloat(), 1.0f, 0.75f);
	}
}
