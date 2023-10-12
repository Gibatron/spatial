package dev.mrturtle.spatial.inventory;

import dev.mrturtle.spatial.Spatial;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

public class InventoryShape {
	public List<InventoryPosition> shape;
	public int width;
	public int height;

	public InventoryShape() {
		shape = List.of(new InventoryPosition(0, 0));
		width = 1;
		height = 1;
	}

	public InventoryShape(List<InventoryPosition> shape, int width, int height) {
		this.shape = shape;
		this.width = width;
		this.height = height;
	}

	public boolean canPlaceAt(Inventory inventory, int index) {
		int row = index / 9;
		for (InventoryPosition pos : shape) {
			int posIndex = pos.getRelativeIndex(inventory, index);
			if (posIndex < 0 || posIndex >= inventory.size())
				return false;
			if (inventory instanceof PlayerInventory) {
				int posRow = posIndex / 9;
				if (pos.y == 0 && posRow != row)
					return false;
				// Prevent placing in armor slots
				if (posIndex >= 36 && posIndex <= 39)
					return false;
				// Prevent placing in offhand
				if (posIndex == 40)
					return false;
				// Prevent placing in hotbar
				if (posIndex <= 8)
					return false;
			}
			if (!inventory.getStack(posIndex).isEmpty())
				return false;
		}
		return true;
	}

	public void placeAt(Inventory inventory, int index, ItemStack stack) {
		for (InventoryPosition pos : shape) {
			int posIndex = pos.getRelativeIndex(inventory, index);
			if (posIndex == index)
				continue;
			ItemStack newStack = stack.copyWithCount(1);
			NbtCompound nbt = newStack.getOrCreateNbt();
			nbt.putBoolean("isSpatialCopy", true);
			nbt.putInt("spatialOwnerIndex", index);
			newStack.setNbt(nbt);
			inventory.setStack(posIndex, newStack);
		}
		if (inventory instanceof PlayerInventory playerInventory) {
			playerInventory.player.playerScreenHandler.updateToClient();
			Spatial.LOGGER.info("updating player inventory");
		}
	}

	public void removeAt(Inventory inventory, int index) {
		for (InventoryPosition pos : shape) {
			int posIndex = pos.getRelativeIndex(inventory, index);
			if (posIndex == index)
				continue;
			ItemStack slotStack = inventory.getStack(posIndex);
			if (!slotStack.hasNbt())
				continue;
			if (!slotStack.getOrCreateNbt().getBoolean("isSpatialCopy"))
				continue;
			if (slotStack.getOrCreateNbt().getInt("spatialOwnerIndex") != index)
				continue;
			inventory.removeStack(posIndex);
		}
	}

	public static InventoryShape fromIngredientList(DefaultedList<Ingredient> ingredients, int width, int height) {
		List<InventoryPosition> shape = new ArrayList<>();
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				Ingredient ingredient = ingredients.get(i + j * width);
				if (!ingredient.isEmpty())
					shape.add(new InventoryPosition(i, j));
			}
		}
		return new InventoryShape(shape, width, height);
	}
}
