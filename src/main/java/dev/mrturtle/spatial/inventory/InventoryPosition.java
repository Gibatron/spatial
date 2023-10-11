package dev.mrturtle.spatial.inventory;

public class InventoryPosition {
    public int x;
    public int y;

    public InventoryPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getRelativeIndex(int index) {
        int rowWidth = 9; // This will fail on inventories that don't have a row width of 9, maybe use inventory size to calculate row width?
        return index + x + y * rowWidth;
    }
}
