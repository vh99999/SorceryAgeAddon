package com.vh99999.sorceryageaddon;

import com.mojang.logging.LogUtils;
import com.vh99999.sorceryageaddon.network.AddonPacketHandler;
import com.vh99999.sorceryageaddon.registry.AddonAbilities;
import com.vh99999.sorceryageaddon.network.SyncAddonRulesS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import radon.jujutsu_kaisen.ability.base.Ability;
import java.util.Set;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import radon.jujutsu_kaisen.ability.JJKAbilities;

@Mod(SorceryAgeAddon.MODID)
public class SorceryAgeAddon {

    public static final String MODID = "sorceryageaddon";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final GameRules.Key<GameRules.BooleanValue> GETO_SPIRITS_CAN_ABSORB = GameRules.register("getoSpiritsCanAbsorb",
            GameRules.Category.MOBS, GameRules.BooleanValue.create(false, (server, value) -> syncRules(server)));

    // Gamerule para ativar Damage Transfer binding vow
    public static final GameRules.Key<GameRules.BooleanValue> GETO_DMG_TRANSFER = GameRules.register("getoDmgTransfer",
            GameRules.Category.PLAYER, GameRules.BooleanValue.create(false, (server, value) -> syncRules(server)));

    private static void syncRules(net.minecraft.server.MinecraftServer server) {
        AddonPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(),
                new SyncAddonRulesS2CPacket(
                        server.getGameRules().getBoolean(GETO_DMG_TRANSFER),
                        server.getGameRules().getBoolean(GETO_SPIRITS_CAN_ABSORB)
                )
        );
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !player.level().isClientSide) {
            AddonPacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                    new SyncAddonRulesS2CPacket(
                            player.level().getGameRules().getBoolean(GETO_DMG_TRANSFER),
                            player.level().getGameRules().getBoolean(GETO_SPIRITS_CAN_ABSORB)
                    )
            );
        }
    }

    public SorceryAgeAddon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        AddonAbilities.register();
        AddonPacketHandler.register();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(
            modid = MODID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}
