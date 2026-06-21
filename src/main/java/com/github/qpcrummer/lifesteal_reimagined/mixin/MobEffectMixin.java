package com.github.qpcrummer.lifesteal_reimagined.mixin;

import com.github.qpcrummer.lifesteal_reimagined.utils.EffectEndEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MobEffect.class)
public class MobEffectMixin implements EffectEndEvent {
    @Override
    public void onEffectFinished(ServerPlayer effectedPlayer) {}
}
