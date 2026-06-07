package de.wolfmod.macebackport.entity;

import de.wolfmod.macebackport.registry.ModItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class ShurikenEntity extends ThrowableItemProjectile {
    private static final float DAMAGE = 2.0F;

    public ShurikenEntity(EntityType<? extends ShurikenEntity> type, Level level) {
        super(type, level);
    }

    public ShurikenEntity(EntityType<? extends ShurikenEntity> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    public ShurikenEntity(EntityType<? extends ShurikenEntity> type, double x, double y, double z, Level level) {
        super(type, x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SHURIKEN.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level.isClientSide) {
            return;
        }

        Entity owner = this.getOwner();
        result.getEntity().hurt(DamageSource.thrown(this, owner == null ? this : owner), DAMAGE);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level.isClientSide) {
            this.discard();
        }
    }

    @Override
    protected float getGravity() {
        return 0.03F;
    }
}
