package com.github.qpcrummer.lifesteal_reimagined.commands;

import com.github.qpcrummer.lifesteal_reimagined.data.DeathData;
import com.github.qpcrummer.lifesteal_reimagined.gamerules.GiftMethod;
import com.github.qpcrummer.lifesteal_reimagined.gamerules.LifeStealGamerules;
import com.github.qpcrummer.lifesteal_reimagined.utils.LifeStealText;
import com.github.qpcrummer.lifesteal_reimagined.utils.OfflinePlayerData;
import com.github.qpcrummer.lifesteal_reimagined.utils.PlayerUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

import java.util.Collection;

public final class GiftCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("gift")
                        .requires(CommandSourceStack::isPlayer)
                        .then(Commands.argument("player", GameProfileArgument.gameProfile())
                                .then(Commands.argument("hearts", IntegerArgumentType.integer(1))
                                        .executes(GiftCommand::gift)))
        );
    }

    private static int gift(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final CommandSourceStack source = context.getSource();
        final MinecraftServer server = source.getServer();
        final GameRules gameRules = source.getLevel().getGameRules();


        if (gameRules.getRule(LifeStealGamerules.GIFT_METHOD).get() == GiftMethod.COMMAND && source.isPlayer()) {
            final ServerPlayer player = source.getPlayer();
            int amount = PlayerUtils.maxHeartsRemoved(player, IntegerArgumentType.getInteger(context, "hearts"));
            if (amount > 0) {
                // Check if the target is valid
                final Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(context, "player");
                if (profiles.isEmpty()) {
                    source.sendFailure(LifeStealText.GIFT_NONE);
                    return 0;
                }
                if (profiles.size() > 1) {
                    source.sendFailure(LifeStealText.GIFT_MULTIPLE);
                    return 0;
                }

                final GameProfile receiver = profiles.iterator().next();

                if (receiver.getId() == player.getUUID()) {
                    // Can't gift to yourself
                    source.sendFailure(LifeStealText.noSelfGifting(player.getName()));
                    return 0;
                }

                if (DeathData.isPlayerDead(receiver.getId(), gameRules.getInt(LifeStealGamerules.AUTO_REVIVAL))) {
                    // Can't gift to a dead guy
                    source.sendFailure(LifeStealText.isDead(Component.nullToEmpty(receiver.getName())));
                    return 0;
                }

                ServerPlayer receiverPlayer = server.getPlayerList().getPlayer(receiver.getId());

                if (receiverPlayer != null) {
                    // Online
                    int maxPlayerHearts = PlayerUtils.getMaxHearts(receiverPlayer);
                    amount = PlayerUtils.maxHeartsAccepted(maxPlayerHearts, amount, gameRules);

                    if (amount > 0) {
                        PlayerUtils.setMaxHearts(receiverPlayer, maxPlayerHearts + amount);
                        PlayerUtils.setMaxHearts(player, PlayerUtils.getMaxHearts(player) - amount);
                        player.sendSystemMessage(LifeStealText.giftSuccess(amount, receiverPlayer.getName()));
                        receiverPlayer.sendSystemMessage(LifeStealText.receiveGift(amount, player.getName()));
                    } else {
                        source.sendFailure(LifeStealText.receiverTooMuchHealth(receiverPlayer.getName()));
                        return 0;
                    }
                } else {
                    // Offline
                    OfflinePlayerData offlinePlayerData = OfflinePlayerData.getOfflinePlayerData(server, receiver);
                    int maxHeartsOffline = offlinePlayerData.getMaxHearts();
                    amount = PlayerUtils.maxHeartsAccepted(maxHeartsOffline, amount, gameRules);
                    Component receiverName = Component.nullToEmpty(receiver.getName());

                    if (amount > 0) {
                        offlinePlayerData.setMaxHearts(maxHeartsOffline + amount);
                        PlayerUtils.setMaxHearts(player, PlayerUtils.getMaxHearts(player) - amount);
                        player.sendSystemMessage(LifeStealText.giftSuccess(amount, receiverName));
                    } else {
                        source.sendFailure(LifeStealText.receiverTooMuchHealth(receiverName));
                        return 0;
                    }
                }
                return 1;
            } else if (amount == 0) {
                source.sendFailure(LifeStealText.LOW_HEALTH);
            }
            return 1;
        } else {
            source.sendFailure(LifeStealText.GIFT_COMMAND_DISABLED);
            return 0;
        }
    }
}
