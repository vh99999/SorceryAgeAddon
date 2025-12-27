package com.vh99999.sorceryageaddon.mixin;

import com.vh99999.sorceryageaddon.registry.AddonAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.curse_manipulation.CurseAbsorption;
import radon.jujutsu_kaisen.capability.data.sorcerer.AbsorbedCurse;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;
import radon.jujutsu_kaisen.util.EntityUtil;

@Mixin(value = CurseAbsorption.class, remap = false)
public abstract class CurseAbsorptionMixin {

    @Inject(method = "check", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;<init>(Lnet/minecraft/world/level/ItemLike;)V"), cancellable = true)
    private static void autoConsumeCheck(LivingEntity victim, DamageSource source, CallbackInfo ci) {
        if (!(source.getEntity() instanceof LivingEntity attacker)) return;
        
        if (JJKAbilities.hasToggled(attacker, AddonAbilities.AUTO_CONSUME.get())) {
            ISorcererData victimCap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            ISorcererData attackerCap = attacker.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            attacker.swing(InteractionHand.MAIN_HAND, true);

            AbsorbedCurse absorbedCurse;
            if (victim instanceof Player player) {
                absorbedCurse = new AbsorbedCurse(victim.getName(), victim.getType(), victimCap.serializeNBT(), player.getGameProfile());
            } else {
                absorbedCurse = new AbsorbedCurse(victim.getName(), victim.getType(), victimCap.serializeNBT());
            }

            attackerCap.addCurse(absorbedCurse);
            
            if (attacker instanceof ServerPlayer serverPlayer) {
                PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(attackerCap.serializeNBT()), serverPlayer);
            }

            EntityUtil.makePoofParticles(victim);

            if (!(victim instanceof Player)) {
                victim.discard();
            } else {
                if (!victim.isDeadOrDying()) {
                    victim.kill();
                }
            }
            ci.cancel();
        }
    }
}
