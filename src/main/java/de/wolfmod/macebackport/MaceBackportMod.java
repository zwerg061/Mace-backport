package de.wolfmod.macebackport;

import de.wolfmod.macebackport.events.CombatEvents;
import de.wolfmod.macebackport.network.ModNetworking;
import de.wolfmod.macebackport.registry.ModEnchantments;
import de.wolfmod.macebackport.registry.ModEntities;
import de.wolfmod.macebackport.registry.ModItems;
import de.wolfmod.macebackport.registry.ModParticles;
import de.wolfmod.macebackport.registry.ModSounds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.client.event.EntityRenderersEvent;

@Mod(MaceBackportMod.MODID)
public final class MaceBackportMod {
    public static final String MODID = "macebackport";

    public MaceBackportMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modBus);
        ModEnchantments.ENCHANTMENTS.register(modBus);
        ModEntities.ENTITIES.register(modBus);
        ModParticles.PARTICLE_TYPES.register(modBus);
        ModSounds.SOUND_EVENTS.register(modBus);

        modBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(CombatEvents.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetworking::register);
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ClientEvents {
        private ClientEvents() {
        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
        }

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.WIND_CHARGE.get(), de.wolfmod.macebackport.client.renderer.WindChargeRenderer::new);
            event.registerEntityRenderer(ModEntities.SHURIKEN.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        }

        @SubscribeEvent
        public static void registerParticles(RegisterParticleProvidersEvent event) {
            event.register(ModParticles.GUST.get(), de.wolfmod.macebackport.particle.GustParticle.Provider::new);
            event.register(ModParticles.SMALL_GUST.get(), de.wolfmod.macebackport.particle.SmallGustParticle.Provider::new);
            event.register(ModParticles.GUST_EMITTER_SMALL.get(), new de.wolfmod.macebackport.particle.GustSeedParticle.Provider(0.25D, 7, 1));
            event.register(ModParticles.GUST_EMITTER_LARGE.get(), new de.wolfmod.macebackport.particle.GustSeedParticle.Provider(1.0D, 15, 2));
        }
    }

}
