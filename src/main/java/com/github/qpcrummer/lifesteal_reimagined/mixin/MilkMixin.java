package com.github.qpcrummer.lifesteal_reimagined.mixin;

import com.github.qpcrummer.lifesteal_reimagined.effect.InvulnerableStatusEffect;
import com.github.qpcrummer.lifesteal_reimagined.utils.PlayerInvulnerabilityInterface;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MilkBucketItem;
import net.neoforged.neoforge.common.EffectCure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MilkBucketItem.class)
public class MilkMixin {
    @Redirect(method = "finishUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;removeEffectsCuredBy(Lnet/neoforged/neoforge/common/EffectCure;)Z"))
    private boolean lifesteal$preventInvulnerabilityRemoval(LivingEntity instance, EffectCure cure) {
        instance.removeAllEffects();
        if (instance instanceof ServerPlayer player && ((PlayerInvulnerabilityInterface)player).isInvulnerable()) {
            instance.addEffect(new MobEffectInstance(InvulnerableStatusEffect.INVULNERABLE, ((PlayerInvulnerabilityInterface)player).getRemaining(), 0, false, false, true));
        }
        return false;
    }
}