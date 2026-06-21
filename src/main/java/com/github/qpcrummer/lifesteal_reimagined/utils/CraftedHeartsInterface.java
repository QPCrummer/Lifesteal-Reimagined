package com.github.qpcrummer.lifesteal_reimagined.utils;

import com.github.qpcrummer.lifesteal_reimagined.gamerules.LifeStealGamerules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;

public interface CraftedHeartsInterface {
    int getHeartsCrafted();
    void resetHeartsCrafted();
    void incrementHeartsCrafted();

    ServerPlayer getInstance();

    /**
     * If the player can craft a new heart item
     * @return If the player can crate a heart
     */
    default boolean canCraftHeart() {
        ServerPlayer player = getInstance();
        GameRules gameRules = player.level().getGameRules();
        int amount = gameRules.getInt(LifeStealGamerules.LIMITED_CRAFTING_AMOUNT);
        switch (gameRules.getRule(LifeStealGamerules.LIMITED_CRAFTING_TYPE).get()) {
            case FOREVER, UNTIL_BANNED -> {
                return getHeartsCrafted() < amount;
            }
            case HEART_BASED -> {
                return amount > PlayerUtils.getMaxHearts(player);
            }
            default -> {
                return true;
            }
        }
    }
}
