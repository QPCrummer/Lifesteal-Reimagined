package com.github.qpcrummer.lifesteal.commands;

import com.github.qpcrummer.lifesteal.data.DeathData;
import com.github.qpcrummer.lifesteal.gamerules.LifeStealGamerules;
import com.github.qpcrummer.lifesteal.utils.LifeStealText;
import com.github.qpcrummer.lifesteal.utils.PlayerUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;

import java.util.Collection;

public class AdminReviveCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("admin-revive")
                        .requires(CommandSourceStack::isPlayer)
                        .requires(source -> source.hasPermission(2)) // Level 2 permission matching GameModeCommand requirements
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .executes(AdminReviveCommand::reset)));
    }

    public static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();
        final Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "player");
        if (profiles.size() != 1) {
            // Error
            return 0;
        }
        final GameProfile receiver = profiles.iterator().next();
        if (!DeathData.isPlayerDead(receiver.getId(), source.getLevel().getGameRules().getInt(LifeStealGamerules.AUTO_REVIVAL))) {
            // Error
            source.sendFailure(LifeStealText.playerIsAlive(Component.nullToEmpty(receiver.getName())));
            return 0;
        }
        PlayerUtils.revive(receiver.getName(), source.getServer(), source.getLevel(), source.getPlayer().blockPosition(), source.getPlayer(), false, null);
        context.getSource().sendSuccess(() -> LifeStealText.adminRevive(receiver.getName()), true);
        return 1;
    }
}
