package com.vh99999.sorceryageaddon.event;

import com.vh99999.sorceryageaddon.SorceryAgeAddon;
import com.vh99999.sorceryageaddon.capability.IAddonSorcererData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.entity.base.ISorcerer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = SorceryAgeAddon.MODID)
public class DamageTransferEvents {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            boolean enabled = com.vh99999.sorceryageaddon.Config.getoDmgTransfer || player.level().getGameRules().getBoolean(SorceryAgeAddon.GETO_DMG_TRANSFER);
            if (!enabled) return;

            player.getCapability(SorcererDataHandler.INSTANCE).ifPresent(cap -> {
                if (cap.hasTechnique(CursedTechnique.CURSE_MANIPULATION) && cap instanceof IAddonSorcererData addonCap && addonCap.sorceryageaddon$isDamageTransferActive()) {
                    LivingEntity weakest = getWeakestCurse(player);
                    if (weakest != null) {
                        weakest.hurt(event.getSource(), event.getAmount());
                        event.setAmount(0);
                        event.setCanceled(true);
                    }
                }
            });
        }
    }

    public static LivingEntity getWeakestCurse(Player owner) {
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        List<Entity> summons = cap.getSummons();
        if (summons.isEmpty()) return null;

        List<LivingEntity> curses = summons.stream()
                .filter(e -> e instanceof LivingEntity le && le.isAlive())
                .map(e -> (LivingEntity) e)
                .filter(le -> le.getCapability(SorcererDataHandler.INSTANCE)
                        .map(c -> c.getType() == JujutsuType.CURSE)
                        .orElse(false))
                .collect(Collectors.toList());

        if (curses.isEmpty()) return null;

        // Shuffle once to handle the "random" part of the requirement if Max Health and XP are equal
        if (curses.size() > 1) {
            Collections.shuffle(curses);
        }

        return curses.stream().min((e1, e2) -> {
            float mh1 = e1.getMaxHealth();
            float mh2 = e2.getMaxHealth();
            if (mh1 != mh2) return Float.compare(mh1, mh2);

            float xp1 = 0, xp2 = 0;
            ISorcererData c1 = e1.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
            ISorcererData c2 = e2.getCapability(SorcererDataHandler.INSTANCE).resolve().orElse(null);
            if (c1 != null) xp1 = c1.getExperience();
            if (c2 != null) xp2 = c2.getExperience();

            return Float.compare(xp1, xp2);
        }).orElse(null);
    }
}
