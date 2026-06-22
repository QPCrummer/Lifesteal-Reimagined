package com.github.qpcrummer.lifesteal.mixin;

import com.github.qpcrummer.lifesteal.block.AltarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractCandleBlock.class)
public class CandleBlockMixin extends Block {
    public CandleBlockMixin(Properties p_49795_) {
        super(p_49795_);
    }

    @Inject(method = "setLit", at = @At("HEAD"))
    private static void lifesteal$playAnimation(LevelAccessor level, BlockState state, BlockPos pos, boolean lit, CallbackInfo ci) {
        AltarBlock.playAnimation(level, pos);
    }
}
