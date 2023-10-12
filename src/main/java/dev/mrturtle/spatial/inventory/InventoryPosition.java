package dev.mrturtle.spatial.inventory;

import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;

public class InventoryPosition {
    public int x;
    public int y;

    public InventoryPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getRelativeIndex(Inventory inventory, int index) {
        return index + x + y * getRowWidth(inventory);
    }

    private int getRowWidth(Inventory inventory) {
        if (inventory instanceof HopperBlockEntity)
            return 5;
        if (inventory instanceof DispenserBlockEntity)
            return 3;
        return 9;
    }
}
