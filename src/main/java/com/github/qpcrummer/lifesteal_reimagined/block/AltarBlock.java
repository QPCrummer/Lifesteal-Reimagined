package com.github.qpcrummer.lifesteal_reimagined.block;

import com.github.qpcrummer.lifesteal_reimagined.Lifesteal;
import com.github.qpcrummer.lifesteal_reimagined.effect.ReviveRitualAnimation;
import com.github.qpcrummer.lifesteal_reimagined.effect.WithdrawRitualAnimation;
import com.github.qpcrummer.lifesteal_reimagined.gamerules.LifeStealGamerules;
import com.github.qpcrummer.lifesteal_reimagined.gamerules.ReviveMethod;
import com.github.qpcrummer.lifesteal_reimagined.gamerules.WithdrawMethod;
import com.github.qpcrummer.lifesteal_reimagined.items.HeartItem;
import com.github.qpcrummer.lifesteal_reimagined.items.ModItems;
import com.github.qpcrummer.lifesteal_reimagined.utils.LifeStealText;
import com.github.qpcrummer.lifesteal_reimagined.utils.PlayerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class AltarBlock extends Block {
    public AltarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer) {
            GameRules gameRules = level.getGameRules();
            if (gameRules.getRule(LifeStealGamerules.WITHDRAW_METHOD).get() == WithdrawMethod.ALTAR
                    && serverPlayer.isShiftKeyDown()
                    && isAltarComplete(level, hitResult.getBlockPos())) {
                if (gameRules.getBoolean(LifeStealGamerules.DO_ALTAR_ANIMATIONS)) {
                    if (PlayerUtils.handleWithdraw(serverPlayer, 1, false)) {
                        com.github.qpcrummer.lifesteal_reimagined.Lifesteal.ANIMATIONS.add(new WithdrawRitualAnimation(serverPlayer, hitResult.getBlockPos()));
                    }
                } else {
                    PlayerUtils.handleWithdraw(serverPlayer, 1, true);
                }
            }
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        if (itemStack.is(ModItems.HEART)) {
            if (!(level instanceof ServerLevel world)) {
                return super.useItemOn(itemStack, blockState, level, pos, player, hand, blockHitResult);
            }
            final MinecraftServer server = world.getServer();
            GameRules gameRules = world.getGameRules();

            if (gameRules.getRule(LifeStealGamerules.REVIVE_METHOD).get() != ReviveMethod.ALTAR) {
                return super.useItemOn(itemStack, blockState, level, pos, player, hand, blockHitResult);
            }

            String playerName = HeartItem.getCustomName(itemStack);
            if (playerName == null) {
                return super.useItemOn(itemStack, blockState, level, pos, player, hand, blockHitResult);
            }

            if (player.isShiftKeyDown() && isAltarComplete(world, pos)) {
                // Can't revive yourself
                if (playerName.equalsIgnoreCase(player.getDisplayName().getString())) {
                    ((ServerPlayer) player).sendSystemMessage(LifeStealText.noSelfReviving(player.getName()), true);
                    PlayerUtils.failedSound(world, pos);
                    return ItemInteractionResult.FAIL;
                }

                byte val = PlayerUtils.revive(playerName, server, world, pos, (ServerPlayer) player, true, hand);

                switch (val) {
                    case 0 -> {
                        if (gameRules.getBoolean(LifeStealGamerules.DO_ALTAR_ANIMATIONS)) {
                            Lifesteal.ANIMATIONS.add(new ReviveRitualAnimation(pos, world));
                        }
                        return ItemInteractionResult.SUCCESS;
                    }
                    case 1 -> {
                        return ItemInteractionResult.FAIL;
                    }
                    default -> {
                        ((ServerPlayer) player).sendSystemMessage(LifeStealText.notFound(playerName), true);
                        PlayerUtils.failedSound(world, pos);
                        return ItemInteractionResult.FAIL;
                    }
                }
            }
        }

        return super.useItemOn(itemStack, blockState, level, pos, player, hand, blockHitResult);
    }

    public static boolean isAltarComplete(Level world, BlockPos pos) {
        BlockState north = world.getBlockState(pos.north());
        BlockState east = world.getBlockState(pos.east());
        BlockState south = world.getBlockState(pos.south());
        BlockState west = world.getBlockState(pos.west());

        return north.is(BlockTags.CANDLES) && north.getValue(CandleBlock.LIT)
                && east.is(BlockTags.CANDLES) && east.getValue(CandleBlock.LIT)
                && south.is(BlockTags.CANDLES) && south.getValue(CandleBlock.LIT)
                && west.is(BlockTags.CANDLES) && west.getValue(CandleBlock.LIT);
    }
}
