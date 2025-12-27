package com.vh99999.sorceryageaddon.registry;

import com.vh99999.sorceryageaddon.ability.AutoConsumeAbility;
import net.minecraftforge.registries.RegistryObject;
import radon.jujutsu_kaisen.ability.JJKAbilities;
import radon.jujutsu_kaisen.ability.base.Ability;

public class AddonAbilities {
    public static final RegistryObject<Ability> AUTO_CONSUME = JJKAbilities.ABILITIES.register("auto_consume", AutoConsumeAbility::new);

    public static void register() {
        // Just to trigger class loading and static initialization
    }
}
