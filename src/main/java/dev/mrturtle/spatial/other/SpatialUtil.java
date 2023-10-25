package dev.mrturtle.spatial.other;

import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.awt.*;

public class SpatialUtil {
	public static int colorFromItemStack(ItemStack stack) {
		return colorFromItemStack(stack, false);
	}

	public static int colorFromItemStack(ItemStack stack, boolean isMainStack) {
		// Calculate hue from hash of item's english name
		float hue = (float) stack.getItem().getName().hashCode() / Integer.MAX_VALUE;
		return Color.HSBtoRGB(hue, isMainStack ? 1.0f : 0.75f, isMainStack ? 0.75f : 0.65f);
	}

	public static boolean isInvalidInventory(Inventory inventory) {
		if (inventory instanceof PlayerInventory)
			return false;
		if (inventory instanceof ChestBlockEntity)
			return false;
		if (inventory instanceof DoubleInventory)
			return false;
		if (inventory instanceof EnderChestInventory)
			return false;
		if (inventory instanceof BarrelBlockEntity)
			return false;
		if (inventory instanceof ShulkerBoxBlockEntity)
			return false;
		if (inventory instanceof DispenserBlockEntity)
			return false;
		return true;
	}
}
