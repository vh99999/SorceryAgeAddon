package com.vh99999.sorceryageaddon.network;

import com.vh99999.sorceryageaddon.Config;
import com.vh99999.sorceryageaddon.SorceryAgeAddon;
import com.vh99999.sorceryageaddon.capability.IAddonSorcererData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;

import java.util.function.Supplier;

public class ToggleDamageTransferC2SPacket {
    private final boolean active;

    public ToggleDamageTransferC2SPacket(boolean active) {
        this.active = active;
    }

    public ToggleDamageTransferC2SPacket(FriendlyByteBuf buf) {
        this.active = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.active);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                boolean enabled = Config.getoDmgTransfer || player.level().getGameRules().getBoolean(SorceryAgeAddon.GETO_DMG_TRANSFER);
                if (!enabled) return;
                player.getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap -> {
                    if (cap.hasTechnique(CursedTechnique.CURSE_MANIPULATION) && cap instanceof IAddonSorcererData addonCap) {
                        addonCap.sorceryageaddon$setDamageTransferActive(active);
                    }
                });
            }
        });
        ctx.setPacketHandled(true);
    }
}
