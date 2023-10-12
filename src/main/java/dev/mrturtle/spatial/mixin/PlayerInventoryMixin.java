package dev.mrturtle.spatial.mixin;

import dev.mrturtle.spatial.Spatial;
import dev.mrturtle.spatial.inventory.InventoryShape;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Shadow @Final public DefaultedList<ItemStack> main;

    @Redirect(method = "offer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getOccupiedSlotWithRoomForStack(Lnet/minecraft/item/ItemStack;)I"))
    public int offerGetOccupiedSlotWithRoomForStackRedirect(PlayerInventory instance, ItemStack stack) {
        int index = getOccupiedSlotWithRoomForStackRedirect(instance, stack);
        if (index != -1)
            attemptPlacement(stack, index);
        return index;
    }

    @Redirect(method = "addStack(Lnet/minecraft/item/ItemStack;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getOccupiedSlotWithRoomForStack(Lnet/minecraft/item/ItemStack;)I"))
    public int addStackGetOccupiedSlotWithRoomForStackRedirect(PlayerInventory instance, ItemStack stack) {
        int index = getOccupiedSlotWithRoomForStackRedirect(instance, stack);
        if (index != -1)
            attemptPlacement(stack, index);
        return index;
    }

    @Redirect(method = "addStack(Lnet/minecraft/item/ItemStack;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getEmptySlot()I"))
    public int addStackGetEmptySlotRedirect(PlayerInventory instance, ItemStack stack) {
        int index = instance.getEmptySlot();
        if (index != 4 && index <= 8)
            return -1;
        return index;
    }

    public void attemptPlacement(ItemStack stack, int index) {
        if (stack.isEmpty())
            return;
        if (stack.hasNbt())
            if (stack.getOrCreateNbt().getBoolean("isSpatialCopy"))
                return;
        // Don't run on mainhand or offhand
        if (index == 4 || index == 40)
            return;
        Spatial.LOGGER.info("Add Stack - Placing shape of " + stack);
        InventoryShape shape = Spatial.getShape(stack);
        shape.placeAt((Inventory) this, index, stack);
    }

    public int getOccupiedSlotWithRoomForStackRedirect(PlayerInventory instance, ItemStack stack) {
        int index = instance.getOccupiedSlotWithRoomForStack(stack);
        if (index != -1)
            return index;
        for (int i = 0; i < main.size(); i++) {
            ItemStack slotStack = main.get(i);
            if (!slotStack.isEmpty())
                continue;
            InventoryShape shape = Spatial.getShape(stack);
            if (shape.canPlaceAt((Inventory) this, i) || i == 4 || i == 40)
                return i;
        }
        return -1;
    }
}
