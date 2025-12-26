package com.vh99999.sorceryageaddon.geto;

// ...existing code...

import com.vh99999.sorceryageaddon.SorceryAgeAddon;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.AbsorbedCurse;
import radon.jujutsu_kaisen.item.CursedSpiritOrbItem;
import radon.jujutsu_kaisen.item.JJKItems;
import radon.jujutsu_kaisen.entity.curse.base.CursedSpirit;
import radon.jujutsu_kaisen.util.EntityUtil;

/**
 * When a summoned cursed spirit (owned by a player via Geto) kills another curse, and the gamerule
 * GETO_SPIRITS_CAN_ABSORB is true, give the owner a Cursed Spirit Orb as if they had killed it.
 */
@Mod.EventBusSubscriber(modid = SorceryAgeAddon.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GetoAbsorbHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetoAbsorbHandler.class);

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        Level level = victim.level();

        if (level.isClientSide) return;

        // Only care about cursed spirits being killed
        if (!(victim instanceof CursedSpirit)) return;

        Entity direct = event.getSource().getEntity();

        if (!(direct instanceof CursedSpirit)) return;

        CursedSpirit killerCurse = (CursedSpirit) direct;

        LivingEntity owner = killerCurse.getOwner();

        if (owner == null) return;

        // Check gamerule on the world of the owner
        if (!owner.level().getGameRules().getBoolean(SorceryAgeAddon.GETO_SPIRITS_CAN_ABSORB)) return;

        try {
            // Try to build the orb from the victim's sorcerer capability
            if (!victim.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return;

            ISorcererData victimCap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

            ItemStack stack = new ItemStack(JJKItems.CURSED_SPIRIT_ORB.get());

            if (victim instanceof Player playerVictim) {
                CursedSpiritOrbItem.setAbsorbed(stack, new AbsorbedCurse(victim.getName(), victim.getType(), victimCap.serializeNBT(), playerVictim.getGameProfile()));
            } else {
                CursedSpiritOrbItem.setAbsorbed(stack, new AbsorbedCurse(victim.getName(), victim.getType(), victimCap.serializeNBT()));
            }

            // Give the item to the owner
            if (owner instanceof ServerPlayer serverPlayer) {
                serverPlayer.addItem(stack);
            } else {
                owner.setItemSlot(EquipmentSlot.MAINHAND, stack);
            }

            // Particles / feedback
            EntityUtil.makePoofParticles(victim);

            LOGGER.debug("Geto absorb: gave cursed spirit orb of {} to {}", victim.getType(), owner.getName());
        } catch (Exception e) {
            LOGGER.error("Error while handling Geto absorb on death", e);
        }
    }
}

