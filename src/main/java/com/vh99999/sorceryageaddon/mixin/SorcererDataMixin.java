package com.vh99999.sorceryageaddon.mixin;

import com.vh99999.sorceryageaddon.capability.IAddonSorcererData;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import radon.jujutsu_kaisen.capability.data.sorcerer.BindingVow;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererData;

@Mixin(value = SorcererData.class)
public abstract class SorcererDataMixin implements IAddonSorcererData {
    @Shadow(remap = false) protected net.minecraft.world.entity.LivingEntity owner;
    @Unique
    private boolean sorceryageaddon$damageTransferActive;

    @Shadow(remap = false)
    public abstract void sync();

    @Shadow(remap = false)
    public abstract net.minecraft.nbt.CompoundTag serializeNBT();

    @Override
    public boolean sorceryageaddon$isDamageTransferActive() {
        return sorceryageaddon$damageTransferActive;
    }

    @Override
    public void sorceryageaddon$setDamageTransferActive(boolean active) {
        this.sorceryageaddon$damageTransferActive = active;
        this.sync();
        if (this.owner instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            radon.jujutsu_kaisen.network.PacketHandler.sendToClient(new radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket(this.serializeNBT()), serverPlayer);
        }
    }

    @Inject(method = "addBindingVow", at = @At("HEAD"), cancellable = true, remap = false)
    private void sorceryageaddon$onAddBindingVow(BindingVow vow, CallbackInfo ci) {
        if (vow == null) {
            this.sorceryageaddon$setDamageTransferActive(true);
            ci.cancel();
        }
    }

    @Inject(method = "removeBindingVow", at = @At("HEAD"), cancellable = true, remap = false)
    private void sorceryageaddon$onRemoveBindingVow(BindingVow vow, CallbackInfo ci) {
        if (vow == null) {
            this.sorceryageaddon$setDamageTransferActive(false);
            ci.cancel();
        }
    }

    @Inject(method = "hasBindingVow", at = @At("HEAD"), cancellable = true, remap = false)
    private void sorceryageaddon$onHasBindingVow(BindingVow vow, CallbackInfoReturnable<Boolean> cir) {
        if (vow == null) {
            cir.setReturnValue(this.sorceryageaddon$damageTransferActive);
        }
    }

    @Inject(method = "isCooldownDone(Lradon/jujutsu_kaisen/capability/data/sorcerer/BindingVow;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void sorceryageaddon$onIsCooldownDone(BindingVow vow, CallbackInfoReturnable<Boolean> cir) {
        if (vow == null) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "serializeNBT", at = @At("RETURN"), remap = false)
    private void sorceryageaddon$serializeNBT(CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().putBoolean("sorceryageaddon$damageTransferActive", this.sorceryageaddon$damageTransferActive);
    }

    @Inject(method = "deserializeNBT", at = @At("RETURN"), remap = false)
    private void sorceryageaddon$deserializeNBT(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("sorceryageaddon$damageTransferActive")) {
            this.sorceryageaddon$damageTransferActive = nbt.getBoolean("sorceryageaddon$damageTransferActive");
        }
    }
}
