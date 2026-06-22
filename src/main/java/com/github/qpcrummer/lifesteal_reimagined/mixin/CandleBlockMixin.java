package com.github.qpcrummer.lifesteal_reimagined.mixin;

import com.github.qpcrummer.lifesteal_reimagined.Lifesteal;
import com.github.qpcrummer.lifesteal_reimagined.block.AltarBlock;
import com.github.qpcrummer.lifesteal_reimagined.block.ModBlocks;
import com.github.qpcrummer.lifesteal_reimagined.effect.AltarRitualAnimation;
import com.github.qpcrummer.lifesteal_reimagined.gamerules.LifeStealGamerules;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractCandleBlock.class)
public class CandleBlockMixin {
    @Inject(method = "setLit", at = @At("HEAD"))
    private static void lifesteal$playAnimation(LevelAccessor level, BlockState state, BlockPos pos, boolean lit, CallbackInfo ci) {
        GameRules gameRules = level.getServer().getGameRules();
        if (level instanceof ServerLevel serverLevel && gameRules.getBoolean(LifeStealGamerules.DO_ALTAR_ANIMATIONS)) {
            BlockPos altarPos = isNextToAltar(pos, serverLevel);
            if (altarPos != null && AltarBlock.isAltarComplete(serverLevel, altarPos)) {
                Lifesteal.ANIMATIONS.add(new AltarRitualAnimation(altarPos, serverLevel));
            }
        }
    }

    private static @Nullable BlockPos isNextToAltar(BlockPos pos, ServerLevel level) {
        BlockPos north = pos.north();
        if (level.getBlockState(north).is(ModBlocks.ALTAR)) {
            return north;
        }

        BlockPos east = pos.east();
        if (level.getBlockState(east).is(ModBlocks.ALTAR)) {
            return east;
        }

        BlockPos south = pos.south();
        if (level.getBlockState(south).is(ModBlocks.ALTAR)) {
            return south;
        }

        BlockPos west = pos.west();
        if (level.getBlockState(west).is(ModBlocks.ALTAR)) {
            return west;
        }

        return null;
    }
}
