package com.vh99999.sorceryageaddon;

import com.mojang.logging.LogUtils;
import com.vh99999.sorceryageaddon.registry.AddonAbilities;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
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
            GameRules.Category.MOBS, GameRules.BooleanValue.create(false));

    // Gamerule para ativar Damage Transfer binding vow
    public static final GameRules.Key<GameRules.BooleanValue> GETO_DMG_TRANSFER = GameRules.register("getoDmgTransfer",
            GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));

    public SorceryAgeAddon() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        AddonAbilities.register();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

        if (Config.logDirtBlock) {
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
        }

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach(item ->
                LOGGER.info("ITEM >> {}", item)
        );
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // adicionar itens ao creative tab se quiser
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(
            modid = MODID,
            bus = Mod.EventBusSubscriber.Bus.MOD,
            value = Dist.CLIENT
    )
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
}
