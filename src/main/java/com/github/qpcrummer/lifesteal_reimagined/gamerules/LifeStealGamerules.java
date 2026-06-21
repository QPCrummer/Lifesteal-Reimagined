package com.github.qpcrummer.lifesteal_reimagined.gamerules;

import mc.recraftors.unruled_api.UnruledApi;
import mc.recraftors.unruled_api.rules.EnumRule;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;

public final class LifeStealGamerules {
    public static MinecraftServer serverInstance;
    public static void init() {}

    /**
     * What criteria must be met in order for hearts to be removed from the player
     */
    public static final GameRules.@NotNull Key<EnumRule<DeathCriteria>> DEATH_CRITERIA = UnruledApi.registerEnumRule("death_criteria", GameRules.Category.PLAYER, DeathCriteria.class, DeathCriteria.PLAYER_ONLY);

    /**
     * The action to take when the player goes below the allowed minimum health as defined by {@link #MIN_PLAYER_HEARTS}
     */
    public static final GameRules.@NotNull Key<EnumRule<DeathAction>> DEATH_ACTION = UnruledApi.registerEnumRule("death_action", GameRules.Category.PLAYER, DeathAction.class, DeathAction.BAN);

    /**
     * The method required to revive a player
     */
    public static final GameRules.@NotNull Key<EnumRule<ReviveMethod>> REVIVE_METHOD = UnruledApi.registerEnumRule("revive_method", GameRules.Category.PLAYER, ReviveMethod.class, ReviveMethod.ALTAR);

    /**
     * The method required to gift a heart
     */
    public static final GameRules.@NotNull Key<EnumRule<GiftMethod>> GIFT_METHOD = UnruledApi.registerEnumRule("gift_method", GameRules.Category.PLAYER, GiftMethod.class, GiftMethod.MANUAL);

    /**
     * The method required to withdraw a heart to a heart item
     */
    public static final GameRules.@NotNull Key<EnumRule<WithdrawMethod>> WITHDRAW_METHOD = UnruledApi.registerEnumRule("withdraw_method", GameRules.Category.PLAYER, WithdrawMethod.class, WithdrawMethod.ALTAR);

    /**
     * Whether to disable getting "free" hearts from killing people with the minimum HP.
     * This can prevent spawn camping and harvesting tons of hearts from teammates
     */
    public static final GameRules.@NotNull Key<GameRules.BooleanValue> ANTI_HEART_DUPE = UnruledApi.registerBoolean("enable_anti_heart_dupe", GameRules.Category.DROPS, true);

    /**
     * The number of hearts "stolen" from players when other players kill them.
     */
    public static final GameRules.Key<GameRules.IntegerValue> STEAL_AMOUNT = UnruledApi.registerInt("steal_amount", GameRules.Category.PLAYER, 1, ((server, integerValue) -> integerValue.set(Math.max(0, integerValue.get()), server)));

    /**
     * This value determines the threshold for being considered "dead".
     * If a player reaches lower than this value, they will be categorized as dead
     */
    public static final GameRules.Key<GameRules.IntegerValue> MIN_PLAYER_HEARTS = UnruledApi.registerInt("min_player_hearts", GameRules.Category.PLAYER, 1, ((server, integerValue) -> integerValue.set(Math.max(1, integerValue.get()), server)));

    /**
     * The max amount of hearts a player can obtain
     */
    public static final GameRules.Key<GameRules.IntegerValue> MAX_PLAYER_HEARTS = UnruledApi.registerInt("max_player_hearts", GameRules.Category.PLAYER, 10, ((server, integerValue) -> integerValue.set(Math.max(1, integerValue.get()), server)));

    /**
     * The amount of seconds until the player is automatically revived
     * Setting this to 0 will disable auto-revival
     */
    public static final GameRules.Key<GameRules.IntegerValue> AUTO_REVIVAL = UnruledApi.registerInt("auto_revival_seconds", GameRules.Category.PLAYER, 0, ((server, integerValue) -> integerValue.set(Math.max(0, integerValue.get()), server)));

    /**
     * The amount of time a player is invulnerable after being revived in seconds
     * The default value is 0 seconds, which disables the feature
     */
    public static final GameRules.Key<GameRules.IntegerValue> RESPAWN_INVULNERABILITY = UnruledApi.registerInt("revival_invulnerability_seconds", GameRules.Category.PLAYER, 0, ((server, integerValue) -> integerValue.set(Math.max(0, integerValue.get()), server)));

    /**
     * The amount of time a player is invulnerable since they started playing
     * The default value is 0 seconds, which disables the feature
     */
    public static final GameRules.Key<GameRules.IntegerValue> NEW_PLAYER_INVULNERABILITY = UnruledApi.registerInt("new_player_invulnerability_seconds", GameRules.Category.PLAYER, 0, ((server, integerValue) -> integerValue.set(Math.max(0, integerValue.get()), server)));

    /**
     * The maximum stack size of the heart item
     */
    public static final GameRules.Key<GameRules.IntegerValue> HEART_STACK_SIZE = UnruledApi.registerInt("heart_stack_size", GameRules.Category.MISC, 1, ((server, integerValue) -> integerValue.set(Math.clamp(integerValue.get(), 1, 64), server)));

    /**
     * If a heart can be crafted in a crafter
     */
    public static final GameRules.Key<GameRules.BooleanValue> HEART_CRAFT_IN_CRAFTER = UnruledApi.registerBoolean("heart_craft_in_crafter", GameRules.Category.DROPS, false);

    /**
     * The type of limited crafting of hearts
     */
    public static final GameRules.Key<EnumRule<LimitedCraftingType>> LIMITED_CRAFTING_TYPE = UnruledApi.registerEnumRule("limited_heart_crafting_type", GameRules.Category.PLAYER, LimitedCraftingType.class, LimitedCraftingType.NONE);

    /**
     * The amount of hearts that can be crafted before limited
     */
    public static final GameRules.Key<GameRules.IntegerValue> LIMITED_CRAFTING_AMOUNT = UnruledApi.registerInt("limited_heart_crafting_amount", GameRules.Category.MISC, 0, ((server, integerValue) -> integerValue.set(Math.max(0, integerValue.get()), server)));

    /**
     * Whether to do fancy animations
     */
    public static final GameRules.Key<GameRules.BooleanValue> DO_ALTAR_ANIMATIONS = UnruledApi.registerBoolean("altar_animations", GameRules.Category.MISC, true);

    public static <T extends GameRules.Value<T>> T getStatic(GameRules.Key<T> gameRule, T fallback) {
        return serverInstance == null ? fallback : serverInstance.getGameRules().getRule(gameRule);
    }
}