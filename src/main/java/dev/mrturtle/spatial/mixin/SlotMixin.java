package dev.mrturtle.spatial.mixin;

import dev.mrturtle.spatial.Spatial;
import dev.mrturtle.spatial.inventory.InventoryShape;
import dev.mrturtle.spatial.other.SpatialUtil;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
	@Shadow @Final public Inventory inventory;

	@Shadow @Final private int index;

	@Shadow public abstract int getMaxItemCount();

	@Inject(method = "canInsert", at = @At("RETURN"), cancellable = true)
	public void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue())
			return;
		if (stack.hasNbt())
			if (stack.getOrCreateNbt().getBoolean("isSpatialCopy"))
				return;
		if (SpatialUtil.isInvalidInventory(inventory))
			return;
		// Don't run on mainhand or offhand
		if (inventory instanceof PlayerInventory)
			if (index == 4 || index == 40)
				return;
		InventoryShape shape = Spatial.getShape(stack);
		cir.setReturnValue(shape.canPlaceAt(inventory, index));
	}

	@Inject(method = "setStack(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)V", at = @At("HEAD"))
	public void setStack(ItemStack stack, ItemStack previousStack, CallbackInfo ci) {
		if (previousStack.isEmpty())
			return;
		if (previousStack.hasNbt())
			if (previousStack.getOrCreateNbt().getBoolean("isSpatialCopy"))
				return;
		if (SpatialUtil.isInvalidInventory(inventory))
			return;
		if (inventory instanceof PlayerInventory) {
			// Don't run on mainhand or offhand
			if (index == 4 || index == 40)
				return;
			// Don't run on armor slots
			if (getMaxItemCount() == 1)
				return;
		}
		InventoryShape shape = Spatial.getShape(previousStack);
		shape.removeAt(inventory, index);
	}

	@Inject(method = "setStackNoCallbacks", at = @At("RETURN"))
	public void setStackNoCallbacks(ItemStack stack, CallbackInfo ci) {
		if (stack.isEmpty())
			return;
		if (stack.hasNbt())
			if (stack.getOrCreateNbt().getBoolean("isSpatialCopy"))
				return;
		if (SpatialUtil.isInvalidInventory(inventory))
			return;
		if (inventory instanceof PlayerInventory) {
			// Don't run on mainhand or offhand
			if (index == 4 || index == 40)
				return;
			// Don't run on armor slots
			if (getMaxItemCount() == 1)
				return;
		}
		InventoryShape shape = Spatial.getShape(stack);
		shape.placeAt(inventory, index, stack);
	}

	@Inject(method = "insertStack(Lnet/minecraft/item/ItemStack;I)Lnet/minecraft/item/ItemStack;", at = @At("RETURN"))
	public void insertStack(ItemStack stack, int count, CallbackInfoReturnable<ItemStack> cir) {
		if (stack == cir.getReturnValue())
			return;
		if (stack.hasNbt())
			if (stack.getOrCreateNbt().getBoolean("isSpatialCopy"))
				return;
		if (SpatialUtil.isInvalidInventory(inventory))
			return;
		InventoryShape shape = Spatial.getShape(stack);
		shape.placeAt(inventory, index, stack);
	}

	@Inject(method = "takeStack", at = @At("HEAD"), cancellable = true)
	public void takeStack(int amount, CallbackInfoReturnable<ItemStack> cir) {
		ItemStack stack = inventory.getStack(index);
		if (!stack.hasNbt())
			return;
		if (!stack.getOrCreateNbt().getBoolean("isSpatialCopy"))
			return;
		if (SpatialUtil.isInvalidInventory(inventory))
			return;
		cir.setReturnValue(ItemStack.EMPTY);
	}

	@Redirect(method = "canTakePartial", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;canInsert(Lnet/minecraft/item/ItemStack;)Z"))
	public boolean canInsertRedirect(Slot instance, ItemStack stack) {
		return true;
	}

	@Inject(method = "canBeHighlighted", at = @At("RETURN"), cancellable = true)
	public void canBeHighlighted(CallbackInfoReturnable<Boolean> cir) {
		if (!(inventory instanceof PlayerInventory))
			return;
		if (index == 4 || index == 40)
			return;
		if (index > 8)
			return;
		cir.setReturnValue(false);
	}
}
