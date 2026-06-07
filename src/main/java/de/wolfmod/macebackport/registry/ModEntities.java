package de.wolfmod.macebackport.registry;

import de.wolfmod.macebackport.MaceBackportMod;
import de.wolfmod.macebackport.entity.ShurikenEntity;
import de.wolfmod.macebackport.entity.WindChargeEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MaceBackportMod.MODID);

    public static final RegistryObject<EntityType<WindChargeEntity>> WIND_CHARGE = ENTITIES.register("wind_charge", () ->
            EntityType.Builder.<WindChargeEntity>of(WindChargeEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(new ResourceLocation(MaceBackportMod.MODID, "wind_charge").toString()));

    public static final RegistryObject<EntityType<ShurikenEntity>> SHURIKEN = ENTITIES.register("shuriken", () ->
            EntityType.Builder.<ShurikenEntity>of(ShurikenEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(8)
                    .updateInterval(10)
                    .build(new ResourceLocation(MaceBackportMod.MODID, "shuriken").toString()));

    private ModEntities() {
    }
}
