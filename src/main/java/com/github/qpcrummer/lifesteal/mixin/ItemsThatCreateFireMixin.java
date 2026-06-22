package com.github.qpcrummer.lifesteal.mixin;

import com.github.qpcrummer.lifesteal.block.AltarBlock;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net.minecraft.world.item.FireChargeItem", "net.minecraft.world.item.FlintAndSteelItem"})
public class ItemsThatCreateFireMixin {
    @Inject(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;gameEvent(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/core/BlockPos;)V"))
    private void lifesteal$checkForAnimation(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir, @Local(name = "level") Level level, @Local(ordinal = 0) BlockPos pos, @Local(ordinal = 0) BlockState state) {
        if (!level.isClientSide() && state.is(BlockTags.CANDLES) && !state.getValue(CandleBlock.LIT)) {
            AltarBlock.playAnimation(level, pos);
        }
    }
}
