package com.github.qpcrummer.lifesteal_reimagined.utils;

import com.github.qpcrummer.lifesteal_reimagined.Lifesteal;
import com.github.qpcrummer.lifesteal_reimagined.data.DeathData;
import com.github.qpcrummer.lifesteal_reimagined.gamerules.DeathAction;
import com.github.qpcrummer.lifesteal_reimagined.gamerules.DeathCriteria;
import com.github.qpcrummer.lifesteal_reimagined.gamerules.LifeStealGamerules;
import com.github.qpcrummer.lifesteal_reimagined.gamerules.LimitedCraftingType;
import com.github.qpcrummer.lifesteal_reimagined.items.ModItems;
import com.github.qpcrummer.lifesteal_reimagined.mixin.ServerPlayerServerAccessor;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public final class PlayerUtils {

    /**
     * Handles how the server should process a dead player.
     * Options include banning them, immediately reviving them, or putting them in spectator
     * @param player ServerPlayerEntity that died
     * @param data The player's DeathData
     */
    public static void handleDeadPlayerAction(ServerPlayer player, DeathData data) {
        GameRules gameRules = player.level().getGameRules();
        DeathAction action = gameRules.getRule(LifeStealGamerules.DEATH_ACTION).get();
        switch (action) {
            case BAN -> {
                if (gameRules.getInt(LifeStealGamerules.AUTO_REVIVAL) == 0) {
                    player.connection.disconnect(LifeStealText.DEATH);
                } else {
                    player.connection.disconnect(LifeStealText.deathTime((int) (data.deathTime + gameRules.getInt(LifeStealGamerules.AUTO_REVIVAL) - (System.currentTimeMillis() * 0.001))));
                }
            }
            case REVIVE -> {
                setMaxHearts(player, gameRules.getInt(LifeStealGamerules.MIN_PLAYER_HEARTS));
                DeathData.removeFromDeathDataList(player.getUUID()); // I know this is a waste of processing power... but I don't care
                ((PlayerReviveData)player).setNewlyRevived(true); // Prevent heart duplication
            }
            case SPECTATOR -> {
                player.setGameMode(GameType.SPECTATOR);
                ((PlayerGameModeInterface)(player.gameMode)).setPreviousGameMode(GameType.SPECTATOR);
            }
        }
    }

    /**
     * Handles the player joining the server.
     * It determines if the player should be revived or not
     * @param player ServerPlayerEntity that joined
     */
    public static void handlePlayerJoin(ServerPlayer player) {
        // Set new player invulnerability
        int playTimeSecs = (int) (player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) * 0.05);
        int newPlayerInvulnerabilitySecs = player.level().getGameRules().getInt(LifeStealGamerules.NEW_PLAYER_INVULNERABILITY);
        if (playTimeSecs < newPlayerInvulnerabilitySecs) {
            int invulnerabilityTicks = (newPlayerInvulnerabilitySecs - playTimeSecs) * 20;
            ((PlayerInvulnerabilityInterface)(player)).setInvulnerability(invulnerabilityTicks);
        }

        DeathData data = Lifesteal.DEAD_PLAYERS.get(player.getUUID());
        if (data != null) {
            // A reviver takes highest priority
            if (data.reviverPlayerID == null) {
                if (DeathData.shouldAutoRevive(data, player.level().getGameRules().getInt(LifeStealGamerules.AUTO_REVIVAL))) {
                    // Autorevival timer up
                    handlePostRevival(data, player, true);
                } else {
                    // Still dead
                    handleDeadPlayerAction(player, data);
                }
            } else {
                // Reviver found
                handlePostRevival(data, player, false);
            }
        }
    }

    /**
     * Handles the revival of a player when they log back in
     * @param data DeathData of the player
     * @param player ServerPlayerEntity that was revived
     * @param autoRevived If the player was revived due to the automatic revival system
     */
    private static void handlePostRevival(DeathData data, ServerPlayer player, boolean autoRevived) {
        GameRules gameRules = player.level().getGameRules();
        if (!autoRevived) {
            player.sendSystemMessage(LifeStealText.onRevivalText(data, ((ServerPlayerServerAccessor) player).getServer()));
        } else {
            // Autorevived players shouldn't be exempted from the antiHeartDupe
            ((PlayerReviveData)player).setNewlyRevived(true);
        }
        // Check if invulnerability should be applied
        // Doesn't apply invulnerability if they already are invulnerable
        if (!((PlayerInvulnerabilityInterface)player).isInvulnerable()
                && gameRules.getInt(LifeStealGamerules.RESPAWN_INVULNERABILITY) != 0) {
            ((PlayerInvulnerabilityInterface)player).setReviveInvulnerability();
        }
        DeathData.removeFromDeathDataList(player.getUUID());
    }

    /**
     * Handles when a player dies in any circumstance (player or no player)
     * @param killed The player that died
     * @param attacker The player who killed the player who died (or null if there was none).
     */
    public static void handleDeath(ServerPlayer killed, @Nullable ServerPlayer attacker) {
        GameRules gameRules = killed.level().getGameRules();

        // Don't do anything if this gamerule is enabled
        if (gameRules.getRule(LifeStealGamerules.DEATH_CRITERIA).get() == DeathCriteria.PLAYER_ONLY && attacker == null) {
            return;
        }

        int killedOldHearts = getMaxHearts(killed);
        int killedNewHearts = killedOldHearts - gameRules.getInt(LifeStealGamerules.STEAL_AMOUNT);
        boolean isDead = killedNewHearts < gameRules.getInt(LifeStealGamerules.MIN_PLAYER_HEARTS);

        if (isDead) {
            // Considered "banned"
            if (gameRules.getRule(LifeStealGamerules.LIMITED_CRAFTING_TYPE).get() == LimitedCraftingType.UNTIL_BANNED) {
                ((CraftedHeartsInterface)killed).resetHeartsCrafted();
            }
            DeathData data = new DeathData(killed.getUUID());
            data.addToDeathDataList();
            handleDeadPlayerAction(killed, data);
        } else {
            setMaxHearts(killed, killedNewHearts);
        }

        // Check to see if heart should be rewarded
        if (((PlayerReviveData)killed).newlyRevived() && gameRules.getBoolean(LifeStealGamerules.ANTI_HEART_DUPE)) {
            return;
        }

        int heartsAwarded = Math.min(killedOldHearts, gameRules.getInt(LifeStealGamerules.STEAL_AMOUNT));
        if (attacker != null) {
            int attackerNewHearts = getMaxHearts(attacker) + heartsAwarded;
            if (attackerNewHearts > gameRules.getInt(LifeStealGamerules.MAX_PLAYER_HEARTS)) {
                // They can't get more health, but they can still get an item to prevent heart deletion
                attacker.sendSystemMessage(LifeStealText.MAX_HEALTH, true);
                givePlayerHeart(attacker, heartsAwarded);
            } else {
                setMaxHearts(attacker, attackerNewHearts);
            }
        } else if (gameRules.getRule(LifeStealGamerules.DEATH_CRITERIA).get() == DeathCriteria.ANY_DEATH_DROP_HEART) {
            dropHearts(killed, heartsAwarded);
        }
    }

    /**
     * Gets the maximum number of hearts that can be accepted by a player
     * @param currentHearts Player's current max hearts
     * @param hearts Hearts requested
     * @param gameRules GameRules instance
     * @return The maximum number of hearts that the player can accept within their requested amount
     */
    public static int maxHeartsAccepted(int currentHearts, int hearts, GameRules gameRules) {
        if (hearts < 0) {
            return 0;
        }
        int newHearts = Math.min(gameRules.getInt(LifeStealGamerules.MAX_PLAYER_HEARTS), hearts + currentHearts);
        return newHearts - currentHearts;
    }

    /**
     * Gets the maximum number of hearts that can be removed from a player
     * @param player Player removing hearts
     * @param hearts Hearts requested
     * @return The maximum number of hearts that the player can remove within their requested amount
     */
    public static int maxHeartsRemoved(ServerPlayer player, int hearts) {
        if (hearts < 0) {
            player.sendSystemMessage(LifeStealText.INVALID_HEART_AMOUNT, true);
            return -1;
        }
        GameRules gameRules = player.level().getGameRules();
        int maxHearts = getMaxHearts(player);
        int newHearts = Math.max(gameRules.getInt(LifeStealGamerules.MIN_PLAYER_HEARTS), maxHearts - hearts);
        return maxHearts - newHearts;
    }

    /**
     * Sets the new max hearts for a Player
     * @param player Player to change the max base hearts
     * @param maxHearts Max number of hearts
     */
    public static void setMaxHearts(ServerPlayer player, int maxHearts) {
        ((PlayerMaxHealthInterface)player).setBaseMaxHealth(maxHearts * 2);
    }

    /**
     * Gets the max number of hearts of a player
     * @param player The player to query
     * @return Gets the max base hearts of the ServerPlayer
     */
    public static int getMaxHearts(ServerPlayer player) {
        return (int) ((PlayerMaxHealthInterface)player).getBaseMaxHealth() / 2;
    }

    /**
     * Withdraws the specified number of hearts from the player
     * @param player The player that is withdrawing hearts
     * @param hearts The number of requested hearts
     * @param giveHeart Whether to give a heart
     * @return If the withdrawal was successful
     */
    public static boolean handleWithdraw(ServerPlayer player, int hearts, boolean giveHeart) {
        switch (canWithdraw(player, hearts)) {
            case 1 -> {
                player.sendSystemMessage(LifeStealText.INVALID_HEART_AMOUNT, true);
                return false;
            }
            case 2 -> {
                // Not enough hearts
                player.sendSystemMessage(LifeStealText.LOW_HEALTH, true);
                return false;
            }
            default -> {
                executeWithdraw(player, hearts, giveHeart);
                return true;
            }
        }
    }

    /**
     * 0 -> Yes
     * 1 -> Invalid amount
     * 2 -> Not enough hearts
     * @param player The player that is withdrawing hearts
     * @param hearts The number of requested hearts
     * @return The int code above.
     */
    private static int canWithdraw(ServerPlayer player, int hearts) {
        if (hearts <= 0) {
            return 1;
        }
        GameRules gameRules = player.level().getGameRules();
        int playerMaxHearts = getMaxHearts(player);
        if (playerMaxHearts == gameRules.getInt(LifeStealGamerules.MIN_PLAYER_HEARTS)) {
            return 2;
        } else {
            return 0;
        }
    }

    /**
     * Converts a physical heart to a heart item. This method does not check if the operation is safe.
     * @param player The player doing the conversion
     * @param hearts The number of hearts to withdraw
     * @param giveHeart If the heart should be given as an item
     */
    public static void executeWithdraw(ServerPlayer player, int hearts, boolean giveHeart) {
        GameRules gameRules = player.level().getGameRules();
        int playerMaxHearts = getMaxHearts(player);
        int heartsToWithdraw = Math.min(hearts, playerMaxHearts - gameRules.getInt(LifeStealGamerules.MIN_PLAYER_HEARTS));
        int heartsAfterWithdraw = playerMaxHearts - heartsToWithdraw;
        setMaxHearts(player, heartsAfterWithdraw);
        if (giveHeart) {
            givePlayerHeart(player, heartsToWithdraw);
        }
        player.sendSystemMessage(LifeStealText.withdrawnHealth(heartsToWithdraw), true);
    }

    /**
     * Gives the player the specified number of hearts
     * @param player ServerPlayerEntity that gets the hearts
     * @param hearts Number of hearts
     */
    private static void givePlayerHeart(ServerPlayer player, int hearts) {
        final ItemStack heartStack = new ItemStack(ModItems.HEART, 1);
        for (int i = 0; i < hearts; i++) {
            if (!player.addItem(heartStack.copy())) {
                // Quick path for dropping the rest of the hearts to avoid unnecessary checks
                for (int j = i; j < hearts; j++) {
                    player.drop(heartStack.copy(), false, true);
                }
                break;
            }
        }
    }

    /**
     * Drops the heart items where the player died
     * @param deadPlayer The player that died
     * @param hearts The number of heart items to drop
     */
    private static void dropHearts(ServerPlayer deadPlayer, int hearts) {
        final ItemStack heartStack = new ItemStack(ModItems.HEART, 1);
        for (int i = 0; i < hearts; i++) {
            deadPlayer.drop(heartStack.copy(), false, true);
        }
    }

    /**
     * Increments the Player's hearts by 1 if possible
     * @param player Player to increment health by 1
     * @return If the player's health was incremented
     */
    public static boolean incrementHearts(ServerPlayer player) {
        GameRules gameRules = player.level().getGameRules();
        int playerNewMaxHearts = getMaxHearts(player) + 1;
        if (playerNewMaxHearts > gameRules.getInt(LifeStealGamerules.MAX_PLAYER_HEARTS)) {
            player.sendSystemMessage(LifeStealText.MAX_HEALTH, true);
            return false;
        } else {
            setMaxHearts(player, playerNewMaxHearts);
            return true;
        }
    }

    /**
     * Revives a player at the location of the reviver
     * @param reviveeId The UUID of the player being revived
     * @param reviver The player doing the reviving
     * @param fromItem If an item was used.
     * @param hand The hand used. Null if not used
     * @return 0 if success, 1 if error, and 2 if the player was not found
     */
    public static byte revive(UUID reviveeId, ServerPlayer reviver, boolean fromItem, @Nullable InteractionHand hand) {
        return revive(reviveeId, reviver.level().getServer(), (ServerLevel) reviver.level(), reviver.blockPosition(), reviver, fromItem, hand);
    }

    /**
     * Revives a player at a specified location
     * @param playerName The player's name that is being revived
     * @param server Instance of MinecraftServer
     * @param world World that the revived player should spawn in
     * @param pos The position the revived player should spawn at
     * @param reviver The player doing the reviving
     * @param fromItem If an item was used.
     * @param hand The hand used. Null if not used
     * @return 0 if success, 1 if error, and 2 if the player was not found
     */
    public static byte revive(String playerName, MinecraftServer server, ServerLevel world, BlockPos pos, ServerPlayer reviver, boolean fromItem, @Nullable InteractionHand hand) {
        return revive(server.getPlayerList().getPlayerByName(playerName), null, playerName, server, world, pos, reviver, fromItem, hand);
    }

    /**
     * Revives a player at a specified location
     * @param reviveeId The player's UUID that is being revived
     * @param server Instance of MinecraftServer
     * @param world World that the revived player should spawn in
     * @param pos The position the revived player should spawn at
     * @param reviver The player doing the reviving
     * @param fromHeartItem If an item was used
     * @param hand The hand used. Null if not used
     * @return 0 if success, 1 if error, and 2 if the player was not found
     */
    public static byte revive(UUID reviveeId, MinecraftServer server, ServerLevel world, BlockPos pos, ServerPlayer reviver, boolean fromHeartItem, @Nullable InteractionHand hand) {
        return revive(server.getPlayerList().getPlayer(reviveeId), reviveeId, null, server, world, pos, reviver, fromHeartItem, hand);
    }

    private static byte revive(ServerPlayer revivee, @Nullable UUID reviveeId, @Nullable String reviveeName, MinecraftServer server, ServerLevel world, BlockPos pos, ServerPlayer reviver, boolean fromHeartItem, @Nullable InteractionHand hand) {
        if (revivee != null) {
            if (reviveOnline(revivee, world, pos, reviver, fromHeartItem)) {
                revived(reviver, revivee.getDisplayName(), world, pos, hand);
                return 0;
            }
            failed(reviver, pos, revivee.getDisplayName());
            return 1;
        }

        Optional<GameProfile> profile;
        if (reviveeId != null) {
            profile = server.getProfileCache().get(reviveeId);
        } else if (reviveeName != null) {
            profile = server.getProfileCache().get(reviveeName);
        } else {
            profile = Optional.empty();
        }

        if (profile.isPresent()) {
            if (reviveOffline(profile.get(), world, pos, reviver, fromHeartItem)) {
                revived(reviver, Component.nullToEmpty(profile.get().getName()), world, pos, hand);
                return 0;
            }
            failed(reviver, pos, Component.nullToEmpty(profile.get().getName()));
            return 1;
        }
        return 2;
    }

    private static boolean reviveOnline(ServerPlayer player, ServerLevel world, BlockPos alter, Player reviver, boolean fromHeartItem) {
        if (!DeathData.isPlayerDead(player.getUUID(), world.getGameRules().getInt(LifeStealGamerules.AUTO_REVIVAL))) {
            return false;
        }
        teleport(player, world, alter);
        player.setGameMode(GameType.SURVIVAL);

        player.sendSystemMessage(LifeStealText.onRevivalText(reviver.getDisplayName()));
        PlayerUtils.setMaxHearts(player, world.getGameRules().getInt(LifeStealGamerules.MIN_PLAYER_HEARTS));
        // These players are not newly revived if a heart wasn't consumed to revive them
        ((PlayerReviveData)player).setNewlyRevived(!fromHeartItem);
        return true;
    }

    private static boolean reviveOffline(GameProfile profile, ServerLevel world, BlockPos alter, Player reviver, boolean fromHeartItem) {
        if (!DeathData.isPlayerDead(profile.getId(), world.getGameRules().getInt(LifeStealGamerules.AUTO_REVIVAL))) {
            return false;
        }

        MinecraftServer server = world.getServer();
        OfflinePlayerData playerData = OfflinePlayerData.getOfflinePlayerData(server, profile);
        if (playerData == null) {
            return false;
        }
        playerData.setPosition(world, Vec3.atCenterOf(alter.above()));
        playerData.setGamemode(GameType.SURVIVAL);
        playerData.setMaxHearts(world.getGameRules().getInt(LifeStealGamerules.MIN_PLAYER_HEARTS));
        // These players are not newly revived if a heart was consumed to revive them
        playerData.setNewlyRevived(!fromHeartItem);
        playerData.save();

        DeathData.setReviver(profile.getId(), reviver.getUUID());
        return true;
    }

    private static void revived(ServerPlayer reviver, Component revived, ServerLevel level, BlockPos pos, @Nullable InteractionHand hand) {
        if (hand != null) {
            successSound(level, pos);
            reviver.getItemInHand(hand).shrink(1);
            reviver.sendSystemMessage(LifeStealText.revived(revived), true);
        }
    }

    private static void successSound(Level world, BlockPos alter) {
        world.playSound(null, alter, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 16.f, 1);
    }

    private static void failed(ServerPlayer reviver, BlockPos alter, Component revived) {
        failedSound(reviver.level(), alter);
        reviver.sendSystemMessage(LifeStealText.playerIsAlive(revived), true);
    }

    /**
     * The sound that should play if a revive fails
     * @param world The world to play the sound in
     * @param pos The position to play it at
     */
    public static void failedSound(Level world, BlockPos pos) {
        world.playSound(null, pos, SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.PLAYERS, 16.f, 1);
    }

    private static void teleport(Player player, ServerLevel target, BlockPos alterPos) {
        Vec3 pos = Vec3.atCenterOf(alterPos.above());
        player.teleportTo(target, pos.x, pos.y, pos.z, RelativeMovement.ALL, player.getYRot(), player.getXRot());
    }
}