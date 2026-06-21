package com.github.qpcrummer.lifesteal_reimagined.commands;

import com.github.qpcrummer.lifesteal_reimagined.Lifesteal;
import com.github.qpcrummer.lifesteal_reimagined.data.DeathData;
import com.github.qpcrummer.lifesteal_reimagined.gamerules.LifeStealGamerules;
import com.github.qpcrummer.lifesteal_reimagined.gamerules.ReviveMethod;
import com.github.qpcrummer.lifesteal_reimagined.items.HeartItem;
import com.github.qpcrummer.lifesteal_reimagined.utils.LifeStealText;
import com.github.qpcrummer.lifesteal_reimagined.utils.PlayerUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ReviveCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("revive")
                        .requires(CommandSourceStack::isPlayer)
                        .then(Commands.argument("player", StringArgumentType.string())
                                .suggests(ReviveCommand::suggestPlayers)
                                .executes(ReviveCommand::revive))
        );
    }

    private static CompletableFuture<Suggestions> suggestPlayers(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        MinecraftServer server = context.getSource().getServer();

        for (UUID playerId : Lifesteal.DEAD_PLAYERS.keySet()) {
            Optional<GameProfile> optionalGameProfile = server.getProfileCache().get(playerId);
            optionalGameProfile.ifPresent(profile -> {
                if (DeathData.isPlayerDead(profile.getId(), context.getSource().getLevel().getGameRules().getInt(LifeStealGamerules.AUTO_REVIVAL))) {
                    builder.suggest(profile.getName());
                }
            });
        }

        return builder.buildFuture();
    }

    private static int revive(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        CommandSourceStack source = context.getSource();

        if (source.getLevel().getGameRules().getRule(LifeStealGamerules.REVIVE_METHOD).get() == ReviveMethod.COMMAND) {
            ItemStack holding = source.getPlayer().getMainHandItem();
            if (!(holding.getItem() instanceof HeartItem)) {
                source.sendFailure(LifeStealText.REVIVE_HOLD);
                return 0;
            }

            String name = StringArgumentType.getString(context, "player");
            Optional<GameProfile> optionalGameProfile = server.getProfileCache().get(name);
            if (optionalGameProfile.isPresent()) {
                GameProfile profile = optionalGameProfile.get();
                if (DeathData.isPlayerDead(profile.getId(), 0)) {
                    UseOnContext usageContext = new UseOnContext(source.getPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(source.getPlayer().position(), Direction.DOWN, source.getPlayer().blockPosition(), true));
                    PlayerUtils.revive(profile.getId(), server, source.getLevel(), source.getPlayer().blockPosition(), source.getPlayer(), usageContext);
                } else {
                    source.sendFailure(LifeStealText.playerIsAlive(Component.nullToEmpty(profile.getName())));
                    return 0;
                }
            } else {
                source.sendFailure(LifeStealText.notFound(name));
                return 0;
            }
            return 1;
        } else {
            source.sendFailure(LifeStealText.REVIVE_COMMAND_DISABLED);
            return 0;
        }
    }
}
