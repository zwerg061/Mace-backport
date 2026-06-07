package de.wolfmod.macebackport.registry;

import de.wolfmod.macebackport.MaceBackportMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MaceBackportMod.MODID);

    public static final RegistryObject<SoundEvent> MACE_SMASH_GROUND = register("mace_smash_ground");
    public static final RegistryObject<SoundEvent> MACE_SMASH_GROUND_HEAVY = register("mace_smash_ground_heavy");
    public static final RegistryObject<SoundEvent> MACE_SMASH_AIR = register("mace_smash_air");
    public static final RegistryObject<SoundEvent> WIND_CHARGE_THROW = register("wind_charge_throw");
    public static final RegistryObject<SoundEvent> WIND_CHARGE_BURST = register("wind_charge_burst");
    public static final RegistryObject<SoundEvent> SPEAR_LUNGE_1 = register("spear_lunge_1");
    public static final RegistryObject<SoundEvent> SPEAR_LUNGE_2 = register("spear_lunge_2");
    public static final RegistryObject<SoundEvent> SPEAR_LUNGE_3 = register("spear_lunge_3");
    public static final RegistryObject<SoundEvent> SPEAR_ATTACK = register("spear_attack");
    public static final RegistryObject<SoundEvent> SPEAR_HIT = register("spear_hit");
    public static final RegistryObject<SoundEvent> SPEAR_USE = register("spear_use");
    public static final RegistryObject<SoundEvent> WOODEN_SPEAR_ATTACK = register("wooden_spear_attack");
    public static final RegistryObject<SoundEvent> WOODEN_SPEAR_HIT = register("wooden_spear_hit");
    public static final RegistryObject<SoundEvent> WOODEN_SPEAR_USE = register("wooden_spear_use");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation id = new ResourceLocation(MaceBackportMod.MODID, name);
        return SOUND_EVENTS.register(name, () -> new SoundEvent(id));
    }
}
