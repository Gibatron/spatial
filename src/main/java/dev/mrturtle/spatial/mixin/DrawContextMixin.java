package dev.mrturtle.spatial.mixin;

import dev.mrturtle.spatial.other.SpatialUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
	@Shadow @Final private MatrixStack matrices;

	@Shadow public abstract void fill(RenderLayer layer, int x1, int y1, int x2, int y2, int color);

	@Inject(method = "drawItemInSlot(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V", at = @At("HEAD"))
	public void drawItemInSlot(TextRenderer textRenderer, ItemStack stack, int x, int y, String countOverride, CallbackInfo ci) {
		if (!stack.hasNbt())
			return;
		if (!stack.getOrCreateNbt().getBoolean("isSpatialCopy"))
			return;
		matrices.push();
		fill(RenderLayer.getGui(), x - 1, y - 1, x + 17, y + 17, SpatialUtil.colorFromItemStack(stack));
		matrices.pop();
	}
}
