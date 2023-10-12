package dev.mrturtle.spatial.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeMatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeMatcher.class)
public class RecipeMatcherMixin {
    @Inject(method = "addInput(Lnet/minecraft/item/ItemStack;I)V", at = @At("HEAD"), cancellable = true)
    public void addInput(ItemStack stack, int maxCount, CallbackInfo ci) {
        if (stack.isEmpty())
            return;
        if (!stack.hasNbt())
            return;
        if (!stack.getOrCreateNbt().getBoolean("isSpatialCopy"))
            return;
        ci.cancel();
    }
}
