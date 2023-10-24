package dev.mrturtle.spatial.mixin;

import dev.mrturtle.spatial.Spatial;
import dev.mrturtle.spatial.inventory.InventoryPosition;
import dev.mrturtle.spatial.inventory.InventoryShape;
import dev.mrturtle.spatial.other.SpatialUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> {
	@Shadow protected abstract void drawItem(DrawContext context, ItemStack stack, int x, int y, String amountText);

	@Shadow protected int x;

	@Shadow protected int y;

	@Shadow @Final protected T handler;

	@Shadow @Nullable protected abstract Slot getSlotAt(double x, double y);

	@Shadow
	public static void drawSlotHighlight(DrawContext context, int x, int y, int z) {
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawItem(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", shift = At.Shift.AFTER, ordinal = 0))
	public void renderCursorStack(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		ItemStack stack = handler.getCursorStack();
		if (stack.isEmpty())
			return;
		InventoryShape shape = Spatial.getShape(stack);
		for (InventoryPosition pos : shape.shape) {
			if (pos.x == 0 && pos.y == 0)
				continue;
			int i = pos.x * 18 + mouseX - x - 8;
			int j = pos.y * 18 + mouseY - y - 8;
			context.fill(RenderLayer.getGuiOverlay(), i - 1, j - 1, i + 17, j + 17, SpatialUtil.colorFromItemStack(stack));
			drawItem(context, stack.copyWithCount(1), i, j, null);
		}
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlotHighlight(Lnet/minecraft/client/gui/DrawContext;III)V", shift = At.Shift.AFTER, ordinal = 0))
	public void renderHighlightedSlots(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		ItemStack stack = handler.getCursorStack();
		if (stack.isEmpty())
			return;
		InventoryShape shape = Spatial.getShape(stack);
		for (InventoryPosition pos : shape.shape) {
			if (pos.x == 0 && pos.y == 0)
				continue;
			int i = pos.x * 18 + mouseX;
			int j = pos.y * 18 + mouseY;
			Slot slot = getSlotAt(i, j);
			if (slot == null)
				continue;
			if (!slot.canBeHighlighted())
				continue;
			drawSlotHighlight(context, slot.x, slot.y, 0);
		}
	}

	@Inject(method = "getTooltipFromItem", at = @At("RETURN"), cancellable = true)
	public void getTooltipFromItem(ItemStack stack, CallbackInfoReturnable<List<Text>> cir) {
		if (!stack.hasNbt())
			return;
		if (!stack.getOrCreateNbt().getBoolean("isSpatialCopy"))
			return;
		cir.setReturnValue(List.of());
	}
}
