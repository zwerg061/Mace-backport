package de.wolfmod.macebackport.entity;

import de.wolfmod.macebackport.registry.ModParticles;
import de.wolfmod.macebackport.registry.ModItems;
import de.wolfmod.macebackport.registry.ModSounds;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class WindChargeEntity extends ThrowableItemProjectile {
    private static final double VANILLA_EXPLOSION_RADIUS = 1.2D;
    private static final double BACKPORT_PUSH_RADIUS = 2.35D;
    private static final double BLOCK_HIT_OFFSET = 0.25D;
    private static final double BOUNDING_BOX_Y_OFFSET = 0.15D;

    public WindChargeEntity(EntityType<? extends WindChargeEntity> type, Level level) {
        super(type, level);
    }

    public WindChargeEntity(EntityType<? extends WindChargeEntity> type, LivingEntity shooter, Level level) {
        super(type, shooter, level);
    }

    public WindChargeEntity(EntityType<? extends WindChargeEntity> type, double x, double y, double z, Level level) {
        super(type, x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.WIND_CHARGE.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level.isClientSide) {
            return;
        }

        Entity owner = this.getOwner();
        result.getEntity().hurt(DamageSource.thrown(this, owner == null ? this : owner), 1.0F);
        burst(this.position());
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (this.level.isClientSide) {
            return;
        }

        Vec3 pushOut = Vec3.atLowerCornerOf(result.getDirection().getNormal()).scale(BLOCK_HIT_OFFSET);
        burst(result.getLocation().add(pushOut));
        this.discard();
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
        return 0.0F;
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return !(entity instanceof WindChargeEntity) && super.canCollideWith(entity);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity instanceof WindChargeEntity || entity.getType() == EntityType.END_CRYSTAL) {
            return false;
        }

        return super.canHitEntity(entity);
    }

    @Override
    protected AABB makeBoundingBox() {
        AABB box = super.makeBoundingBox();
        return box.move(0.0D, -BOUNDING_BOX_Y_OFFSET, 0.0D);
    }

    @Override
    public void push(double x, double y, double z) {
        // Vanilla wind charges ignore entity pushes.
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide && this.getBlockY() > this.level.getMaxBuildHeight() + 30) {
            burst(this.position());
            this.discard();
            return;
        }

        Vec3 beforeTickVelocity = this.getDeltaMovement();
        super.tick();
        if (beforeTickVelocity.lengthSqr() > 0.0001D && this.getDeltaMovement().lengthSqr() > 0.0001D) {
            this.setDeltaMovement(this.getDeltaMovement().normalize().scale(beforeTickVelocity.length()));
        }
    }

    private void burst(Vec3 center) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(ModParticles.GUST_EMITTER_SMALL.get(), center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        serverLevel.sendParticles(ModParticles.GUST_EMITTER_LARGE.get(), center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);

        final double pushRadius = BACKPORT_PUSH_RADIUS;
        AABB area = new AABB(center, center).inflate(pushRadius);

        for (LivingEntity target : this.level.getEntitiesOfClass(LivingEntity.class, area)) {
            Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            double distance = targetCenter.distanceTo(center);
            if (distance > pushRadius) {
                continue;
            }

            Vec3 delta = targetCenter.subtract(center);
            Vec3 direction;
            if (delta.lengthSqr() < 0.0001D) {
                Vec3 travel = this.getDeltaMovement();
                direction = travel.lengthSqr() < 0.0001D ? new Vec3(0.0D, 1.0D, 0.0D) : travel.normalize();
            } else {
                direction = delta.normalize();
            }

            boolean owner = target == this.getOwner();
            double distanceScale = Math.max(0.0D, 1.0D - (distance / pushRadius));
            double strength = (owner ? 1.18D : 0.92D) * distanceScale;
            double yBoost = Math.min(owner ? 0.88D : 0.62D,
                    Math.max(owner ? 0.42D : 0.16D, (direction.y * strength) + (owner ? 0.44D : 0.20D)));
            target.push(direction.x * strength, yBoost, direction.z * strength);
            target.hurtMarked = true;

            if (target instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            }
        }

        this.level.playSound(null, this.blockPosition(), ModSounds.WIND_CHARGE_BURST.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
