package com.vh99999.sorceryageaddon.mixin;

import com.vh99999.sorceryageaddon.Config;
import com.vh99999.sorceryageaddon.SorceryAgeAddon;
import com.vh99999.sorceryageaddon.accessor.IBindingVowListWidgetEntry;
import com.vh99999.sorceryageaddon.capability.IAddonSorcererData;
import com.vh99999.sorceryageaddon.client.ClientRuleCache;
import com.vh99999.sorceryageaddon.network.AddonPacketHandler;
import com.vh99999.sorceryageaddon.network.ToggleDamageTransferC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import radon.jujutsu_kaisen.capability.data.sorcerer.BindingVow;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.client.gui.screen.tab.BindingVowTab;
import radon.jujutsu_kaisen.client.gui.screen.widget.BindingVowListWidget;

import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(value = BindingVowTab.class)
public abstract class BindingVowTabMixin {
    @Shadow(remap = false) private BindingVowListWidget.Entry vow;
    @Shadow(remap = false) private Button add;
    @Shadow(remap = false) private Button remove;
    @Shadow(remap = false) private MultiLineTextWidget description;
    // Removed minecraft shadow as it's not in the target class

    @SuppressWarnings("rawtypes")
    @Inject(method = "buildBindingVowList", at = @At("TAIL"), remap = false)
    private void sorceryageaddon$injectDamageTransfer(Consumer consumer, Function result, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap -> {
                if (cap.hasTechnique(CursedTechnique.CURSE_MANIPULATION)) {
                    Object entry = result.apply(null);
                    if (entry instanceof IBindingVowListWidgetEntry listEntry) {
                        listEntry.sorceryageaddon$setDamageTransfer(true);
                    }
                    consumer.accept(entry);
                }
            });
        }
    }

    @Inject(method = "setSelectedBindingVow", at = @At("HEAD"), cancellable = true, remap = false)
    private void sorceryageaddon$setSelectedBindingVow(BindingVowListWidget.Entry entry, CallbackInfo ci) {
        if (entry instanceof IBindingVowListWidgetEntry listEntry && listEntry.sorceryageaddon$isDamageTransfer()) {
            this.vow = entry;
            Component desc = Component.translatable("binding_vow.jujutsu_kaisen.damage_transfer.description");
            
            boolean gameRuleEnabled = Config.getoDmgTransfer || ClientRuleCache.getoDmgTransfer;
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                gameRuleEnabled |= mc.level.getGameRules().getBoolean(SorceryAgeAddon.GETO_DMG_TRANSFER);
            }

            if (!gameRuleEnabled) {
                desc = desc.copy().append("\n\n").append(Component.translatable("gui.sorceryageaddon.binding_vow.disabled_by_config"));
            }
            this.description.setMessage(desc);
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("TAIL"), remap = false)
    private void sorceryageaddon$tick(CallbackInfo ci) {
        if (this.vow instanceof IBindingVowListWidgetEntry listEntry && listEntry.sorceryageaddon$isDamageTransfer()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            ISorcererData cap = mc.player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
            
            if (!cap.hasTechnique(CursedTechnique.CURSE_MANIPULATION)) {
                this.add.active = false;
                this.remove.active = false;
                return;
            }

            boolean active = false;
            if (cap instanceof IAddonSorcererData addonCap) {
                active = addonCap.sorceryageaddon$isDamageTransferActive();
            }

            boolean gameRuleEnabled = Config.getoDmgTransfer || ClientRuleCache.getoDmgTransfer;
            if (mc.level != null) {
                gameRuleEnabled |= mc.level.getGameRules().getBoolean(SorceryAgeAddon.GETO_DMG_TRANSFER);
            }

            this.add.active = gameRuleEnabled && !active;
            this.remove.active = gameRuleEnabled && active;
        }
    }

    @Redirect(method = "addWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;", remap = true), remap = false)
    private Button.Builder sorceryageaddon$wrapOnPress(Component label, Button.OnPress originalOnPress) {
        return Button.builder(label, (button) -> {
            if (this.vow instanceof IBindingVowListWidgetEntry listEntry && listEntry.sorceryageaddon$isDamageTransfer()) {
                Player player = Minecraft.getInstance().player;
                if (player == null) return;
                ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
                if (!cap.hasTechnique(CursedTechnique.CURSE_MANIPULATION)) return;

                if (label.getContents() instanceof TranslatableContents tc) {
                    if (tc.getKey().endsWith(".add")) {
                        AddonPacketHandler.INSTANCE.sendToServer(new ToggleDamageTransferC2SPacket(true));
                        cap.addBindingVow(null);
                        button.active = false;
                        this.remove.active = true;
                        return;
                    } else if (tc.getKey().endsWith(".remove")) {
                        AddonPacketHandler.INSTANCE.sendToServer(new ToggleDamageTransferC2SPacket(false));
                        cap.removeBindingVow(null);
                        button.active = false;
                        this.add.active = true;
                        return;
                    }
                }
            }
            originalOnPress.onPress(button);
        });
    }
}
