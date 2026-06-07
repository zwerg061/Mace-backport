package de.wolfmod.macebackport.registry;

import de.wolfmod.macebackport.MaceBackportMod;
import de.wolfmod.macebackport.enchantment.BreachEnchantment;
import de.wolfmod.macebackport.enchantment.DensityEnchantment;
import de.wolfmod.macebackport.enchantment.WindBurstEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, MaceBackportMod.MODID);

    public static final RegistryObject<Enchantment> DENSITY = ENCHANTMENTS.register("density", DensityEnchantment::new);
    public static final RegistryObject<Enchantment> BREACH = ENCHANTMENTS.register("breach", BreachEnchantment::new);
    public static final RegistryObject<Enchantment> WIND_BURST = ENCHANTMENTS.register("wind_burst", WindBurstEnchantment::new);

    private ModEnchantments() {
    }
}
