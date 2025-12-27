package com.vh99999.sorceryageaddon.network;

import com.vh99999.sorceryageaddon.client.ClientRuleCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncAddonRulesS2CPacket {
    private final boolean getoDmgTransfer;
    private final boolean getoSpiritsCanAbsorb;

    public SyncAddonRulesS2CPacket(boolean getoDmgTransfer, boolean getoSpiritsCanAbsorb) {
        this.getoDmgTransfer = getoDmgTransfer;
        this.getoSpiritsCanAbsorb = getoSpiritsCanAbsorb;
    }

    public SyncAddonRulesS2CPacket(FriendlyByteBuf buf) {
        this.getoDmgTransfer = buf.readBoolean();
        this.getoSpiritsCanAbsorb = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(getoDmgTransfer);
        buf.writeBoolean(getoSpiritsCanAbsorb);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientRuleCache.getoDmgTransfer = getoDmgTransfer;
            ClientRuleCache.getoSpiritsCanAbsorb = getoSpiritsCanAbsorb;
        });
        ctx.get().setPacketHandled(true);
    }
}
