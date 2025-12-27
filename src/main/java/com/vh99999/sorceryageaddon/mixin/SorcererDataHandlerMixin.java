package com.vh99999.sorceryageaddon.mixin;

import com.vh99999.sorceryageaddon.capability.IAddonSorcererData;
import com.vh99999.sorceryageaddon.registry.AddonAbilities;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;

@Mixin(value = SorcererDataHandler.class, remap = false)
public abstract class SorcererDataHandlerMixin {

    @Inject(method = "onPlayerClone", at = @At(value = "INVOKE", target = "Lradon/jujutsu_kaisen/capability/data/sorcerer/ISorcererData;clearToggled()V", shift = At.Shift.AFTER), remap = false)
    private static void sorceryageaddon$restoreToggledAbilities(PlayerEvent.Clone event, CallbackInfo ci) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(SorcererDataHandler.INSTANCE).ifPresent(oldCap -> {
                event.getEntity().getCapability(SorcererDataHandler.INSTANCE).ifPresent(newCap -> {
                    for (Ability ability : oldCap.getToggled()) {
                        if (ability == JJKAbilities.CURSE_ABSORPTION.get() || ability == AddonAbilities.AUTO_CONSUME.get()) {
                            if (!newCap.hasToggled(ability)) {
                                newCap.toggle(ability);
                            }
                        }
                    }

                    if (oldCap instanceof IAddonSorcererData oldAddonCap && newCap instanceof IAddonSorcererData addonCap) {
                        addonCap.sorceryageaddon$setDamageTransferActive(oldAddonCap.sorceryageaddon$isDamageTransferActive());
                    }
                });
            });
        }
    }
}
