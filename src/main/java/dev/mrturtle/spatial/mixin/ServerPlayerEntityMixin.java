package dev.mrturtle.spatial.mixin;

import dev.mrturtle.spatial.Spatial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net/minecraft/server/network/ServerPlayerEntity$1")
public class ServerPlayerEntityMixin {
    @ModifyVariable(method = "updateState", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    public DefaultedList<ItemStack> updateState(DefaultedList<ItemStack> stacks) {
        Spatial.LOGGER.info(stacks.toString());
        for (int i = 0; i < stacks.size(); i++) {
            stacks.set(i, new ItemStack(Items.KELP, stacks.get(i).getCount()));
        }
        return stacks;
    }

    @ModifyVariable(method = "updateState", at = @At(value = "HEAD"), ordinal = 0, argsOnly = true)
    public ItemStack updateSlot(ItemStack stack) {
        return new ItemStack(Items.KELP, stack.getCount());
    }
}
