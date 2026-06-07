package de.wolfmod.macebackport.registry;

import de.wolfmod.macebackport.MaceBackportMod;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles {
    public static final DeferredRegister<net.minecraft.core.particles.ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MaceBackportMod.MODID);

    public static final RegistryObject<SimpleParticleType> GUST =
            PARTICLE_TYPES.register("gust", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> SMALL_GUST =
            PARTICLE_TYPES.register("small_gust", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> GUST_EMITTER_SMALL =
            PARTICLE_TYPES.register("gust_emitter_small", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> GUST_EMITTER_LARGE =
            PARTICLE_TYPES.register("gust_emitter_large", () -> new SimpleParticleType(true));

    private ModParticles() {
    }
}
