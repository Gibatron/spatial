package dev.mrturtle.spatial.inventory;

import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.PacketByteBuf;

public class InventoryPosition {
    public int x;
    public int y;

    public InventoryPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getRelativeIndex(Inventory inventory, int index, InventoryPosition originPos) {
        int modifiedX = x - originPos.x;
        int modifiedY = y - originPos.y;
        return index + modifiedX + modifiedY * getRowWidth(inventory);
    }

    public void writeTo(PacketByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
    }

    public static InventoryPosition readFrom(PacketByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        return new InventoryPosition(x, y);
    }

    public static int getRowWidth(Inventory inventory) {
        if (inventory instanceof HopperBlockEntity)
            return 5;
        if (inventory instanceof DispenserBlockEntity)
            return 3;
        return 9;
    }
}
