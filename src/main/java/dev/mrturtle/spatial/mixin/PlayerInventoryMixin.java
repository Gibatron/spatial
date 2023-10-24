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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Shadow @Final public DefaultedList<ItemStack> main;

    @Shadow public abstract int getOccupiedSlotWithRoomForStack(ItemStack stack);

    @Shadow public int selectedSlot;

    @Redirect(method = "offer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getOccupiedSlotWithRoomForStack(Lnet/minecraft/item/ItemStack;)I"))
    public int offerGetOccupiedSlotWithRoomForStackRedirect(PlayerInventory instance, ItemStack stack) {
        int index = getOccupiedSlotWithRoomForStackRedirect(instance, stack);
        if (index != -1)
            attemptPlacement(stack, index);
        return index;
    }

    @Redirect(method = "offer", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getEmptySlot()I"))
    public int offerGetEmptySlotRedirect(PlayerInventory instance, ItemStack stack) {
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

    @Redirect(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getEmptySlot()I"))
    public int addStackGetEmptySlotRedirect(PlayerInventory instance) {
        int index = instance.getEmptySlot();
        if (index != 4 && index <= 8)
            return -1;
        return index;
    }

    @Inject(method = "insertStack(ILnet/minecraft/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getEmptySlot()I", shift = At.Shift.AFTER), cancellable = true)
    public void insertStack(int slot, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        int index = getOccupiedSlotWithRoomForStackRedirect(getOccupiedSlotWithRoomForStack(stack), stack);
        // This is not ideal, but I'm not sure how to do it better at the moment
        if (index != -1) {
            attemptPlacement(stack, index);
            main.set(index, stack.copyAndEmpty());
            main.get(index).setBobbingAnimationTime(5);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "swapSlotWithHotbar", at = @At("HEAD"), cancellable = true)
    public void swapSlotWithHotbar(int slot, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "scrollInHotbar", at = @At("HEAD"), cancellable = true)
    public void scrollInHotbar(double scrollAmount, CallbackInfo ci) {
        selectedSlot = 4;
        ci.cancel();
    }

    @Unique
    public void attemptPlacement(ItemStack stack, int index) {
        if (stack.isEmpty())
            return;
        if (stack.hasNbt())
            if (stack.getOrCreateNbt().getBoolean("isSpatialCopy"))
                return;
        // Don't run on mainhand or offhand
        if (index == 4 || index == 40)
            return;
        InventoryShape shape = Spatial.getShape(stack);
        shape.placeAt((Inventory) this, index, stack);
    }

    @Unique
    public int getOccupiedSlotWithRoomForStackRedirect(PlayerInventory instance, ItemStack stack) {
        int index = instance.getOccupiedSlotWithRoomForStack(stack);
        return getOccupiedSlotWithRoomForStackRedirect(index, stack);
    }

    @Unique
    public int getOccupiedSlotWithRoomForStackRedirect(int index, ItemStack stack) {
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
