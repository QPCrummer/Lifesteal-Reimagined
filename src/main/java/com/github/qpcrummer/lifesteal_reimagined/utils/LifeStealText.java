package com.github.qpcrummer.lifesteal_reimagined.utils;

import com.github.qpcrummer.lifesteal_reimagined.data.DeathData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public final class LifeStealText {
    public static final Component
            FAILURE_UNKNOWN = Component.translatable("lifesteal_reimagined.failure.unknown"), // For debug purposes
            DEATH = Component.translatable("lifesteal_reimagined.gameplay.death"),
            MAX_HEALTH = Component.translatable("lifesteal_reimagined.gameplay.max_health"),
            LOW_HEALTH = Component.translatable("lifesteal_reimagined.gameplay.low_health"),
            INVALID_HEART_AMOUNT = Component.translatable("lifesteal_reimagined.gameplay.invalid_heart_amount"),
            GIFT_COMMAND_DISABLED = Component.translatable("lifesteal_reimagined.command.gift.disabled"),
            REVIVE_COMMAND_DISABLED = Component.translatable("lifesteal_reimagined.command.revive.disabled"),
            WITHDRAW_COMMAND_DISABLED = Component.translatable("lifesteal_reimagined.command.withdraw.disabled"),
            GIFT_NONE = Component.translatable("lifesteal_reimagined.gift.none"),
            GIFT_MULTIPLE = Component.translatable("lifesteal_reimagined.gift.multiple"),
            PREVENT_ATTACK = Component.translatable("lifesteal_reimagined.gameplay.prevent_attack"),
            REVIVE_HOLD = Component.translatable("lifesteal_reimagined.revive.holding"),
            BACK = Component.translatable("lifesteal_reimagined.gui.back"),
            NEXT = Component.translatable("lifesteal_reimagined.gui.next"),
            TITLE = Component.translatable("lifesteal_reimagined.gui.title");

    private static final String
            HEART_WITHDRAWN = "lifesteal_reimagined.withdraw.heart",
            HEART_WITHDRAWN_SINGLE = "lifesteal_reimagined.withdraw.heart.single",
            GIFT_RECEIVER_MAX_HEALTH = "lifesteal_reimagined.gift.receiver.max_health",
            GIFT_SUCCESS = "lifesteal_reimagined.gift.success",
            RECEIVE_SUCCESS = "lifesteal_reimagined.gift.received",
            PLAYER_IS_ALIVE = "lifesteal_reimagined.player.alive",
            PLAYER_IS_DEAD = "lifesteal_reimagined.player.dead",
            PLAYER_IS_YOU = "lifesteal_reimagined.gift.self",
            PLAYER_DOES_NOT_EXIST = "lifesteal_reimagined.player.not_found",
            REVIVEE = "lifesteal_reimagined.player.revived.receiver",
            REVIVER = "lifesteal_reimagined.player.revived.sender",
            SELF_REVIVE = "lifesteal_reimagined.revive.self",
            DEATH_TIME = "lifesteal_reimagined.gameplay.death_time",
            PREVENT_DAMAGE = "lifesteal_reimagined.gameplay.prevent_damage",
            ADMIN_REVIVE = "lifesteal_reimagined.admin.revive";

    public static Component onRevivalText(DeathData data, MinecraftServer server) {
        return Component.translatable(REVIVEE, server.getProfileCache().get(data.reviverPlayerID).get().getName());
    }

    public static Component notFound(String playerName) {
        return Component.translatable(PLAYER_DOES_NOT_EXIST, playerName);
    }

    public static Component onRevivalText(Component reviver) {
        return Component.translatable(REVIVEE, reviver);
    }

    public static Component revived(Component revived) {
        return Component.translatable(REVIVER, revived);
    }

    public static Component playerIsAlive(Component player) {
        return Component.translatable(PLAYER_IS_ALIVE, player);
    }

    public static Component withdrawnHealth(int hearts) {
        if (hearts == 1) {
            return Component.translatable(HEART_WITHDRAWN_SINGLE);
        }
        return Component.translatable(HEART_WITHDRAWN, hearts);
    }

    public static Component receiverTooMuchHealth(Component receiver) {
        return Component.translatable(GIFT_RECEIVER_MAX_HEALTH, receiver);
    }

    public static Component giftSuccess(double health, Component receiver) {
        return Component.translatable(GIFT_SUCCESS, health, receiver);
    }

    public static Component receiveGift(double health, Component sender) {
        return Component.translatable(RECEIVE_SUCCESS, health, sender);
    }

    public static Component noSelfGifting(Component name) {
        return Component.translatable(PLAYER_IS_YOU, name);
    }

    public static Component noSelfReviving(Component name) {
        return Component.translatable(SELF_REVIVE, name);
    }

    public static Component isDead(Component name) {
        return Component.translatable(PLAYER_IS_DEAD, name);
    }

    public static Component adminRevive(String player) {
        return Component.translatable(ADMIN_REVIVE, player);
    }

    public static Component deathTime(int seconds) {
        return Component.translatable(DEATH_TIME, seconds);
    }

    public static Component preventDamage(Component player) {
        return Component.translatable(PREVENT_DAMAGE, player);
    }
}
