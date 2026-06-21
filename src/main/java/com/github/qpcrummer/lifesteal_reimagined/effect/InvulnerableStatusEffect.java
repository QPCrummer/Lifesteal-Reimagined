package com.github.qpcrummer.lifesteal_reimagined.effect;

import com.github.qpcrummer.lifesteal_reimagined.Lifesteal;
import com.github.qpcrummer.lifesteal_reimagined.utils.EffectEndEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.checkerframework.checker.nullness.qual.NonNull;

public class InvulnerableStatusEffect extends MobEffect implements EffectEndEvent {

    public InvulnerableStatusEffect() {
        super(MobEffectCategory.BENEFICIAL, 16262179, ParticleTypes.MYCELIUM);
    }


    @Override
    public boolean isBeneficial() {
        return true;
    }
    @Override
    public MobEffectCategory getCategory() {
        return MobEffectCategory.BENEFICIAL;
    }

    @Override
    public void onEffectStarted(@NonNull LivingEntity entity, int amplifier) {
        super.onEffectStarted(entity, amplifier);
    }

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Lifesteal.MOD_ID);

    public static final DeferredHolder<MobEffect, InvulnerableStatusEffect> INVULNERABLE =
            MOB_EFFECTS.register(ResourceLocation.fromNamespaceAndPath(Lifesteal.MOD_ID, "invulnerability").toString(), InvulnerableStatusEffect::new);

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }

    @Override
    public void onEffectFinished(ServerPlayer effectedPlayer) {
        Scoreboard scoreboard = effectedPlayer.level().getServer().getScoreboard();
        if (Lifesteal.invulnerableTeam.getPlayers().contains(effectedPlayer.getScoreboardName())) {
            scoreboard.removePlayerFromTeam(effectedPlayer.getScoreboardName(), Lifesteal.invulnerableTeam);
        }
    }
}