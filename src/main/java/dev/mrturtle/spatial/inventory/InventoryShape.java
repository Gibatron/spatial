package dev.mrturtle.spatial.inventory;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.HashMap;
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
		if (inventory instanceof PlayerInventory playerInventory)
			if (playerInventory.player.isCreative())
				return true;
		int rowWidth = InventoryPosition.getRowWidth(inventory);
		int row = index / rowWidth;
		for (InventoryPosition pos : shape) {
			int posIndex = pos.getRelativeIndex(inventory, index, shape.get(0));
			if (posIndex < 0 || posIndex >= inventory.size())
				return false;
			int posRow = posIndex / rowWidth;
			if (posRow != row + pos.y)
				return false;
			if (inventory instanceof PlayerInventory) {
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
		if (inventory instanceof PlayerInventory playerInventory)
			if (playerInventory.player.isCreative())
				return;
		for (InventoryPosition pos : shape) {
			int posIndex = pos.getRelativeIndex(inventory, index, shape.get(0));
			if (posIndex == index)
				continue;
			ItemStack newStack = stack.copyWithCount(1);
			newStack.setCustomName(Text.empty());
			NbtCompound nbt = newStack.getOrCreateNbt();
			nbt.putBoolean("isSpatialCopy", true);
			nbt.putInt("spatialOwnerIndex", index);
			newStack.setNbt(nbt);
			inventory.setStack(posIndex, newStack);
		}
		// This might not actually be needed...
		if (inventory instanceof PlayerInventory playerInventory) {
			playerInventory.player.playerScreenHandler.updateToClient();
		}
	}

	public void removeAt(Inventory inventory, int index) {
		if (inventory instanceof PlayerInventory playerInventory)
			if (playerInventory.player.isCreative())
				return;
		for (InventoryPosition pos : shape) {
			int posIndex = pos.getRelativeIndex(inventory, index, shape.get(0));
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

	public void writeTo(PacketByteBuf buf) {
		buf.writeCollection(shape, (bufx, pos) -> pos.writeTo(bufx));
		buf.writeInt(width);
		buf.writeInt(height);
	}

	public static InventoryShape readFrom(PacketByteBuf buf) {
		List<InventoryPosition> shape = buf.readCollection(ArrayList::new, InventoryPosition::readFrom);
		int width = buf.readInt();
		int height = buf.readInt();
		return new InventoryShape(shape, width, height);
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

	public static InventoryShape fromString(String[] input) {
		int width = 0;
		int height = input.length;
		int j = 0;
		List<InventoryPosition> shape = new ArrayList<>();
		for (String line : input) {
			int lineWidth = line.length();
			if (width < lineWidth)
				width = lineWidth;
			int i = 0;
			for (char c : line.toCharArray()) {
				if (c != ' ')
					shape.add(new InventoryPosition(i, j));
				i += 1;
			}
			j += 1;
		}
		return new InventoryShape(shape, width, height);
	}
}
