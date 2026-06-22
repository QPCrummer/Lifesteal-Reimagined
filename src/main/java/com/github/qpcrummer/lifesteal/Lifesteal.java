package com.github.qpcrummer.lifesteal;

import com.github.qpcrummer.lifesteal.block.ModBlocks;
import com.github.qpcrummer.lifesteal.commands.*;
import com.github.qpcrummer.lifesteal.data.DeathData;
import com.github.qpcrummer.lifesteal.effect.InvulnerableStatusEffect;
import com.github.qpcrummer.lifesteal.effect.ParticleAnimation;
import com.github.qpcrummer.lifesteal.gamerules.LifeStealGamerules;
import com.github.qpcrummer.lifesteal.items.ModItems;
import com.github.qpcrummer.lifesteal.menu.ModMenu;
import com.github.qpcrummer.lifesteal.utils.PlayerInvulnerabilityInterface;
import com.github.qpcrummer.lifesteal.utils.PlayerUtils;
import com.github.qpcrummer.lifesteal.world.Ores;
import net.minecraft.ChatFormatting;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.nio.file.Path;
import java.util.*;

@Mod(Lifesteal.MOD_ID)
public class Lifesteal {
    public static final String MOD_ID = "lifesteal";
    private static final Path CONFIG_DIR = net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
    public static final Map<UUID, DeathData> DEAD_PLAYERS = new HashMap<>();
    public static Path DEAD_PLAYERS_FILE_PATH;
    public static final List<ParticleAnimation> ANIMATIONS = new ArrayList<>();
    public static PlayerTeam invulnerableTeam;

    public Lifesteal(IEventBus modEventBus, ModContainer modContainer) {
        DEAD_PLAYERS_FILE_PATH = Path.of(CONFIG_DIR.resolve("lifesteal-deaths.json").toString());
        modEventBus.addListener(this::commonSetup);
        ModBlocks.register(modEventBus);
        InvulnerableStatusEffect.register(modEventBus);
        ModMenu.MENUS.register(modEventBus);
        modEventBus.addListener(this::registerScreens);
        ModItems.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        DeathData.loadDeathDataFromFile(); // Load DeathData
        Ores.initOres();
        LifeStealGamerules.init();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        AdminReviveCommand.register(event.getDispatcher());
        ReviveCommand.register(event.getDispatcher());
        GiftCommand.register(event.getDispatcher());
        WithdrawCommand.register(event.getDispatcher());
        AdminCancelInvulnerabilityCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LifeStealGamerules.serverInstance = event.getServer();
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        MinecraftServer minecraftServer = event.getServer();
        boolean containsTeam = minecraftServer.getScoreboard().getPlayerTeams().stream()
                .anyMatch(team -> team.getName().equals("invulnerable"));

        if (containsTeam) {
            invulnerableTeam = minecraftServer.getScoreboard().getPlayerTeam("invulnerable");
        } else {
            invulnerableTeam = minecraftServer.getScoreboard().addPlayerTeam("invulnerable");
            invulnerableTeam.setColor(ChatFormatting.DARK_RED);
            invulnerableTeam.setNameTagVisibility(Team.Visibility.ALWAYS);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player oldPlayer = event.getOriginal();
            Player newPlayer = event.getEntity();

            if (((PlayerInvulnerabilityInterface) oldPlayer).isInvulnerable()) {

                int remainingTime = ((PlayerInvulnerabilityInterface) oldPlayer).getRemaining();
                newPlayer.addEffect(new MobEffectInstance(
                        InvulnerableStatusEffect.INVULNERABLE,
                        remainingTime,
                        0,
                        false,
                        false,
                        true
                ));

                if (newPlayer instanceof ServerPlayer serverPlayer) {
                    serverPlayer.getServer().getScoreboard().addPlayerToTeam(
                            serverPlayer.getScoreboardName(),
                            invulnerableTeam
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public void onLevelStartTick(LevelTickEvent.Pre event) {
        Level level = event.getLevel();
        if (level instanceof ServerLevel serverLevel) {
            Iterator<ParticleAnimation> iterator = ANIMATIONS.iterator();
            while (iterator.hasNext()) {
                ParticleAnimation anim = iterator.next();
                anim.tick(serverLevel);
                if (anim.isDone()) {
                    anim.onDone(serverLevel);
                    iterator.remove();
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerUtils.handlePlayerJoin(serverPlayer);
        }
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenu.REVIVAL_MENU.get(), ModMenu.RevivalScreen::new);
    }
}
