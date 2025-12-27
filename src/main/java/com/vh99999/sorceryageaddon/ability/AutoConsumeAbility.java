package com.vh99999.sorceryageaddon.ability;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import radon.jujutsu_kaisen.ability.base.Ability;
import radon.jujutsu_kaisen.capability.data.sorcerer.CursedTechnique;
import radon.jujutsu_kaisen.capability.data.sorcerer.ISorcererData;
import radon.jujutsu_kaisen.capability.data.sorcerer.SorcererDataHandler;
import radon.jujutsu_kaisen.item.CursedSpiritOrbItem;
import radon.jujutsu_kaisen.network.PacketHandler;
import radon.jujutsu_kaisen.network.packet.s2c.SyncSorcererDataS2CPacket;

public class AutoConsumeAbility extends Ability implements Ability.IToggled {
    @Override
    public boolean isScalable(LivingEntity owner) {
        return false;
    }

    @Override
    public boolean shouldTrigger(PathfinderMob owner, @Nullable LivingEntity target) {
        return false;
    }

    @Override
    public ActivationType getActivationType(LivingEntity owner) {
        return ActivationType.TOGGLED;
    }

    @Override
    public void run(LivingEntity owner) {
    }

    @Override
    public float getCost(LivingEntity owner) {
        return 0;
    }

    @Override
    public void onEnabled(LivingEntity owner) {
        if (owner instanceof Player player && !owner.level().isClientSide) {
            consumeOrbsFromInventory(player);
        }
    }

    private void consumeOrbsFromInventory(Player player) {
        ISorcererData cap = player.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        boolean changed = false;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof CursedSpiritOrbItem) {
                cap.addCurse(CursedSpiritOrbItem.getAbsorbed(stack));
                stack.setCount(0);
                changed = true;
            }
        }

        if (changed && player instanceof ServerPlayer serverPlayer) {
            PacketHandler.sendToClient(new SyncSorcererDataS2CPacket(cap.serializeNBT()), serverPlayer);
        }
    }

    @Override
    public void onDisabled(LivingEntity owner) {
    }

    @Override
    public boolean isUnlockable() {
        return false;
    }

    @Override
    public boolean isTechnique() {
        return false;
    }

    @Override
    public boolean isValid(LivingEntity owner) {
        if (owner == null || (owner instanceof Player player && player.isSpectator())) return false;
        if (!owner.getCapability(SorcererDataHandler.INSTANCE).isPresent()) return false;
        
        ISorcererData cap = owner.getCapability(SorcererDataHandler.INSTANCE).resolve().orElseThrow();
        return cap.hasTechnique(CursedTechnique.CURSE_MANIPULATION);
    }
}
