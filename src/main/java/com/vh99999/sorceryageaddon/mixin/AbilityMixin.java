package com.vh99999.sorceryageaddon.mixin;

import com.vh99999.sorceryageaddon.capability.IAddonSorcererData;
import com.vh99999.sorceryageaddon.registry.AddonAbilities;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;

@Mixin(value = Ability.class)
public abstract class AbilityMixin {
    @Inject(method = "getStatus", at = @At("HEAD"), cancellable = true, remap = false)
    private void sorceryageaddon$restrictAbilities(LivingEntity owner, CallbackInfoReturnable<Ability.Status> cir) {
        owner.getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap -> {
            if (cap.hasTechnique(CursedTechnique.CURSE_MANIPULATION) && cap instanceof IAddonSorcererData addonCap && addonCap.sorceryageaddon$isDamageTransferActive()) {
                Ability ability = (Ability) (Object) this;

                // Allow melee
                if (ability.isMelee()) return;

                // Allow CURSE_ABSORPTION
                if (ability == JJKAbilities.CURSE_ABSORPTION.get()) return;

                // Allow AUTO_CONSUME
                if (ability == AddonAbilities.AUTO_CONSUME.get()) return;

                // Disable others
                cir.setReturnValue(Ability.Status.DISABLE);
            }
        });
    }
}
