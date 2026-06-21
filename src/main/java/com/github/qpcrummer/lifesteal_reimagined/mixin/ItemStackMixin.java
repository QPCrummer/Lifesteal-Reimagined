package com.github.qpcrummer.lifesteal_reimagined.mixin;

import com.github.qpcrummer.lifesteal_reimagined.items.ModItems;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DataComponentHolder, MutableDataComponentHolder, IItemStackExtension {

    @Shadow
    public abstract boolean is(Item item);

    @Shadow
    public abstract Item getItem();

    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void lifesteal$redirectMaxStackSize(CallbackInfoReturnable<Integer> cir) {
        if (this.is(ModItems.HEART)) {
            Item item = this.getItem();
            // TODO Is this correct?
            cir.setReturnValue(item.getDefaultMaxStackSize());
        }
    }
}
