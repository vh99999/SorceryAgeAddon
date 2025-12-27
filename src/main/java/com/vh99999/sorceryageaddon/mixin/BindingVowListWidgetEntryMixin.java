package com.vh99999.sorceryageaddon.mixin;

import com.vh99999.sorceryageaddon.accessor.IBindingVowListWidgetEntry;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import radon.jujutsu_kaisen.capability.data.sorcerer.BindingVow;
import radon.jujutsu_kaisen.client.gui.screen.widget.BindingVowListWidget;

@Mixin(value = BindingVowListWidget.Entry.class)
public abstract class BindingVowListWidgetEntryMixin implements IBindingVowListWidgetEntry {
    @Shadow(remap = false) private BindingVow vow;
    @Unique
    private boolean sorceryageaddon$isDamageTransfer;

    @Override
    public boolean sorceryageaddon$isDamageTransfer() {
        return sorceryageaddon$isDamageTransfer;
    }

    @Override
    public void sorceryageaddon$setDamageTransfer(boolean isDamageTransfer) {
        this.sorceryageaddon$isDamageTransfer = isDamageTransfer;
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lradon/jujutsu_kaisen/capability/data/sorcerer/BindingVow;getName()Lnet/minecraft/network/chat/Component;", remap = false))
    private Component sorceryageaddon$redirectGetName(BindingVow instance) {
        if (this.sorceryageaddon$isDamageTransfer) {
            return Component.translatable("binding_vow.jujutsu_kaisen.damage_transfer");
        }
        return instance.getName();
    }

    @Redirect(method = "getNarration", at = @At(value = "INVOKE", target = "Lradon/jujutsu_kaisen/capability/data/sorcerer/BindingVow;getName()Lnet/minecraft/network/chat/Component;", remap = false))
    private Component sorceryageaddon$redirectGetNarration(BindingVow instance) {
        if (this.sorceryageaddon$isDamageTransfer) {
            return Component.translatable("binding_vow.jujutsu_kaisen.damage_transfer");
        }
        return instance.getName();
    }
}
