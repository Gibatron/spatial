package dev.mrturtle.spatial.mixin;

import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(ScreenHandler.class)
public class ScreenHandlerMixin {
    @ModifyArgs(method = "updateToClient", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandlerSyncHandler;updateState(Lnet/minecraft/screen/ScreenHandler;Lnet/minecraft/util/collection/DefaultedList;Lnet/minecraft/item/ItemStack;[I)V"))
    public void updateToClient(Args args) {

    }
}
