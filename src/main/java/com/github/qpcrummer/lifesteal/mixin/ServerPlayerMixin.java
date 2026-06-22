package com.github.qpcrummer.lifesteal.mixin;

import com.github.qpcrummer.lifesteal.effect.InvulnerableStatusEffect;
import com.github.qpcrummer.lifesteal.gamerules.LifeStealGamerules;
import com.github.qpcrummer.lifesteal.utils.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements PlayerReviveData, PlayerInvulnerabilityInterface, PlayerMaxHealthInterface, CraftedHeartsInterface {

    @Shadow
    public abstract void sendSystemMessage(Component message, boolean overlay);

    private boolean newlyRevived;
    private int invulnerableTicks = 0;
    private int heartsCrafted = 0;

    public ServerPlayerMixin(Level p_250508_, BlockPos p_250289_, float p_251702_, GameProfile p_252153_) {
        super(p_250508_, p_250289_, p_251702_, p_252153_);
    }

    @Inject(method = "die", at = @At("TAIL"))
    private void lifesteal$onDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayer attacker = this.getKillCredit() instanceof ServerPlayer ? (ServerPlayer) this.getKillCredit() : null;
        PlayerUtils.handleDeath((ServerPlayer) (Object) this, attacker);
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void lifesteal$copyNewlyRevived(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        this.setNewlyRevived(((PlayerReviveData)oldPlayer).newlyRevived());
        this.invulnerableTicks = ((PlayerInvulnerabilityInterface)oldPlayer).getRemaining();
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void lifesteal$readRevivedData(CompoundTag tag, CallbackInfo ci) {
        this.setNewlyRevived(tag.getBoolean("newly_revived"));
        this.invulnerableTicks = tag.getInt("invulnerability_ticks");
        this.heartsCrafted = tag.getInt("hearts_crafted");
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void lifesteal$writeRevivedData(CompoundTag tag, CallbackInfo ci) {
        tag.putBoolean("newly_revived", this.newlyRevived);
        tag.putInt("invulnerability_ticks", this.invulnerableTicks);
        tag.putInt("hearts_crafted", this.heartsCrafted);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void lifesteal$tickInvulnerability(CallbackInfo ci) {
        if (this.isInvulnerable()) {
            invulnerableTicks--;
        }
    }

    // You cannot be killed by players if invulnerable
    @Inject(method = "canHarmPlayer", at = @At("HEAD"), cancellable = true)
    private void lifesteal$checkInvulnerability(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player instanceof ServerPlayer serverPlayer && this.isInvulnerable()) {
            serverPlayer.sendSystemMessage(LifeStealText.preventDamage(this.getName()), true);
            cir.setReturnValue(false);
        }
    }

    // You cannot kill players if invulnerable either
    @WrapMethod(method = "attack")
    private void lifesteal$cancelAttacking(Entity targetEntity, Operation<Void> original) {
        if (this.isInvulnerable() && targetEntity instanceof ServerPlayer) {
            this.sendSystemMessage(LifeStealText.PREVENT_ATTACK, true);
        } else {
            original.call(targetEntity);
        }
    }

    @Override
    public boolean newlyRevived() {
        return newlyRevived;
    }

    @Override
    public void setNewlyRevived(boolean set) {
        this.newlyRevived = set;
    }

    @Override
    public void setReviveInvulnerability() {
        this.setInvulnerability(this.level().getGameRules().getInt(LifeStealGamerules.RESPAWN_INVULNERABILITY) * 20);
    }

    @Override
    public void setInvulnerability(int ticks) {
        this.invulnerableTicks = ticks;
        this.addEffect(new MobEffectInstance(InvulnerableStatusEffect.INVULNERABLE, this.getRemaining(), 0, false, false, true));
    }

    @Override
    public boolean isInvulnerable() {
        return invulnerableTicks != 0;
    }

    @Override
    public int getRemaining() {
        return invulnerableTicks;
    }

    @Override
    public void setVulnerable() {
        this.invulnerableTicks = 1;
    }

    @Override
    public double getBaseMaxHealth() {
        return this.getAttributeBaseValue(Attributes.MAX_HEALTH);
    }

    @Override
    public void setBaseMaxHealth(double value) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(value);
    }

    @Override
    public int getHeartsCrafted() {
        return this.heartsCrafted;
    }

    @Override
    public void resetHeartsCrafted() {
        this.heartsCrafted = 0;
    }

    @Override
    public ServerPlayer getInstance() {
        return (ServerPlayer) (Object) this;
    }

    @Override
    public void incrementHeartsCrafted() {
        this.heartsCrafted++;
    }
}
