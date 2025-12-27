package com.vh99999.sorceryageaddon.event;

import com.vh99999.sorceryageaddon.SorceryAgeAddon;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.capability.data.sorcerer.AbsorbedCurse;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.JujutsuType;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.capability.data.sorcerer.Trait;
import radon.jujutsu_kaisen.entity.base.ISorcerer;
import radon.jujutsu_kaisen.item.CursedSpiritOrbItem;
import radon.jujutsu_kaisen.item.JJKItems;
import radon.jujutsu_kaisen.util.EntityUtil;
import radon.jujutsu_kaisen.util.HelperMethods;

@Mod.EventBusSubscriber(modid = SorceryAgeAddon.MODID)
public class CurseAbsorptionEvents {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        check(event.getEntity(), event.getSource());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        check(event.getEntity(), event.getSource());
    }

    private static void check(LivingEntity victim, DamageSource source) {
        if (victim.level().isClientSide) return;
        if (!victim.level().getGameRules().getBoolean(SorceryAgeAddon.GETO_SPIRITS_CAN_ABSORB)) return;
        
        if (!HelperMethods.isMelee(source)) return;

        if (!(source.getEntity() instanceof LivingEntity attacker)) return;
        
        LivingEntity owner = null;
        if (attacker instanceof TamableAnimal tamable && attacker instanceof ISorcerer sorcerer) {
            if (sorcerer.getJujutsuType() == JujutsuType.CURSE) {
                owner = tamable.getOwner();
            }
        }
        
        if (owner == null) return;

        if (!canAbsorb(owner, victim)) return;

        if (!JJKAbilities.hasToggled(owner, JJKAbilities.CURSE_ABSORPTION.get())) return;

        ISorcererData victimCap = victim.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        attacker.swing(InteractionHand.MAIN_HAND, true);

        ItemStack stack = new ItemStack(JJKItems.CURSED_SPIRIT_ORB.get());

        if (victim instanceof Player player) {
            CursedSpiritOrbItem.setAbsorbed(stack, new AbsorbedCurse(victim.getName(), victim.getType(), victimCap.serializeNBT(), player.getGameProfile()));
        } else {
            CursedSpiritOrbItem.setAbsorbed(stack, new AbsorbedCurse(victim.getName(), victim.getType(), victimCap.serializeNBT()));
        }

        if (owner instanceof Player player) {
            if (!player.addItem(stack)) {
                player.drop(stack, false);
            }
        } else {
            owner.setItemSlot(EquipmentSlot.MAINHAND, stack);
        }
        EntityUtil.makePoofParticles(victim);

        if (!(victim instanceof Player)) {
            victim.discard();
        } else {
            if (!victim.isDeadOrDying()) {
                victim.kill();
            }
        }
    }

    private static boolean canAbsorb(LivingEntity owner, LivingEntity target) {
        if (!owner.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return false;
        if (!target.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return false;

        ISorcererData ownerCap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        ISorcererData targetCap = target.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();

        return (targetCap.getType() == JujutsuType.CURSE && !targetCap.hasTrait(Trait.DEATH_PAINTING) && (!(target instanceof TamableAnimal tamable) || !tamable.isTame())) &&
                (ownerCap.getExperience() / targetCap.getExperience() >= 2 || target.isDeadOrDying());
    }
}
