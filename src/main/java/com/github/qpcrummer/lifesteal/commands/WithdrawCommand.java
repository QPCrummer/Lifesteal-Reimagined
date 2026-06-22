package com.github.qpcrummer.lifesteal.commands;

import com.github.qpcrummer.lifesteal.gamerules.LifeStealGamerules;
import com.github.qpcrummer.lifesteal.gamerules.WithdrawMethod;
import com.github.qpcrummer.lifesteal.utils.LifeStealText;
import com.github.qpcrummer.lifesteal.utils.PlayerUtils;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

public final class WithdrawCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("withdraw")
                        .requires(CommandSourceStack::isPlayer)
                        .then(Commands.argument("hearts", IntegerArgumentType.integer(1))
                                .executes(WithdrawCommand::withdraw))
        );
    }

    private static int withdraw(CommandContext<CommandSourceStack> context) {
        final CommandSourceStack source = context.getSource();
        final GameRules gameRules = source.getLevel().getGameRules();

        if (source.isPlayer() && gameRules.getRule(LifeStealGamerules.WITHDRAW_METHOD).get() == WithdrawMethod.COMMAND) {
            ServerPlayer player = source.getPlayer();
            PlayerUtils.handleWithdraw(player, IntegerArgumentType.getInteger(context, "hearts"), true);
            return 1;
        } else {
            source.sendFailure(LifeStealText.WITHDRAW_COMMAND_DISABLED);
            return 0;
        }
    }
}
