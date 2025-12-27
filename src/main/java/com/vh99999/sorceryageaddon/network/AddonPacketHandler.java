package com.vh99999.sorceryageaddon.network;

import com.vh99999.sorceryageaddon.SorceryAgeAddon;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class AddonPacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(SorceryAgeAddon.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        INSTANCE.registerMessage(packetId++, ToggleDamageTransferC2SPacket.class, ToggleDamageTransferC2SPacket::encode, ToggleDamageTransferC2SPacket::new, ToggleDamageTransferC2SPacket::handle);
        INSTANCE.registerMessage(packetId++, SyncAddonRulesS2CPacket.class, SyncAddonRulesS2CPacket::encode, SyncAddonRulesS2CPacket::new, SyncAddonRulesS2CPacket::handle);
    }
}
