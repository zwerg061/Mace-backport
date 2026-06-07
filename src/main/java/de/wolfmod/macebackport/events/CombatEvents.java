package de.wolfmod.macebackport.events;

import de.wolfmod.macebackport.registry.ModEnchantments;
import de.wolfmod.macebackport.registry.ModItems;
import de.wolfmod.macebackport.registry.ModParticles;
import de.wolfmod.macebackport.registry.ModSounds;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CombatEvents {
    private static final float SMASH_ATTACK_FALL_THRESHOLD = 1.5F;
    private static final float SMASH_ATTACK_HEAVY_THRESHOLD = 5.0F;
    private static final double SMASH_ATTACK_KNOCKBACK_RADIUS = 2.5D;
    private static final double WIND_BURST_RADIUS = 3.5D;

    private static final Map<UUID, Float> RECENT_ATTACK_FALL = new ConcurrentHashMap<>();
    private static final Map<UUID, Long> RECENT_ATTACK_TIME = new ConcurrentHashMap<>();

    private CombatEvents() {
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (!player.isCreative() || !player.getMainHandItem().is(ModItems.MACE.get())) {
            return;
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player attacker = event.getEntity();
        if (attacker.level.isClientSide) {
            return;
        }

        if (!attacker.getMainHandItem().is(ModItems.MACE.get())) {
            return;
        }

        RECENT_ATTACK_FALL.put(attacker.getUUID(), computeRawSmashDistance(attacker));
        RECENT_ATTACK_TIME.put(attacker.getUUID(), attacker.level.getGameTime());
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        if (attacker.level.isClientSide) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        if (!weapon.is(ModItems.MACE.get())) {
            return;
        }

        LivingEntity target = event.getEntity();
        float rawSmashDistance = getCapturedSmashDistance(attacker);
        boolean canSmashAttack = rawSmashDistance > SMASH_ATTACK_FALL_THRESHOLD && !attacker.isFallFlying();
        float fallBonusDamage = canSmashAttack ? computeSmashDamageBonus(rawSmashDistance) : 0.0F;
        int densityLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DENSITY.get(), weapon);
        int breachLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.BREACH.get(), weapon);
        int windBurstLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.WIND_BURST.get(), weapon);

        float totalBonus = fallBonusDamage;

        // Vanilla: density adds smash_damage_per_fallen_block (0.5 per level) * fallen distance.
        if (densityLevel > 0 && canSmashAttack) {
            totalBonus += rawSmashDistance * (0.5F * densityLevel);
        }

        if (breachLevel > 0) {
            totalBonus += computeBreachDamageBonus(event.getAmount() + totalBonus, target, breachLevel);
        }

        if (totalBonus > 0.0F) {
            event.setAmount(event.getAmount() + totalBonus);
        }

        if (canSmashAttack) {
            triggerSmashEffects(attacker, target);
            dampenSmashFall(attacker);
        }

        if (windBurstLevel > 0 && canSmashAttack) {
            triggerWindBurst(attacker, windBurstLevel);
        }
    }

    private static float getCapturedSmashDistance(Player attacker) {
        UUID id = attacker.getUUID();
        Float cached = RECENT_ATTACK_FALL.remove(id);
        Long tick = RECENT_ATTACK_TIME.remove(id);

        if (cached == null || tick == null) {
            return computeRawSmashDistance(attacker);
        }

        if ((attacker.level.getGameTime() - tick) > 10L) {
            return computeRawSmashDistance(attacker);
        }

        return cached;
    }

    private static float computeRawSmashDistance(Player attacker) {
        return Math.max(0.0F, attacker.fallDistance);
    }

    // Matches vanilla MaceItem#getAttackDamageBonus piecewise curve.
    private static float computeSmashDamageBonus(float fallDistance) {
        if (fallDistance <= 3.0F) {
            return 4.0F * fallDistance;
        }

        if (fallDistance <= 8.0F) {
            return 12.0F + (2.0F * (fallDistance - 3.0F));
        }

        return 22.0F + (fallDistance - 8.0F);
    }

    // Vanilla breach reduces armor effectiveness by 15% per level.
    private static float computeBreachDamageBonus(float currentDamage, LivingEntity target, int breachLevel) {
        float armorEffectiveness = Math.min(0.8F, Math.max(0.0F, target.getArmorValue() / 25.0F));
        if (armorEffectiveness <= 0.0F) {
            return 0.0F;
        }

        float reductionFactor = Math.min(0.6F, 0.15F * breachLevel);
        float denom = 1.0F - armorEffectiveness;
        if (denom <= 0.001F) {
            return 0.0F;
        }

        float multiplierDelta = (armorEffectiveness * reductionFactor) / denom;
        return currentDamage * multiplierDelta;
    }

    private static double getWindBurstKnockbackMultiplier(int windBurstLevel) {
        return switch (windBurstLevel) {
            case 1 -> 1.2D;
            case 2 -> 1.9D;
            default -> 3.0D;
        };
    }

    private static void triggerSmashEffects(Player attacker, LivingEntity target) {
        SoundEvent smashSound;
        if (target.isOnGround()) {
            smashSound = attacker.fallDistance > SMASH_ATTACK_HEAVY_THRESHOLD
                    ? ModSounds.MACE_SMASH_GROUND_HEAVY.get()
                    : ModSounds.MACE_SMASH_GROUND.get();
        } else {
            smashSound = ModSounds.MACE_SMASH_AIR.get();
        }

        attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), smashSound, SoundSource.PLAYERS, 1.0F, 1.0F);
        applySmashKnockback(attacker, target);
    }

    private static void dampenSmashFall(Player attacker) {
        attacker.setDeltaMovement(attacker.getDeltaMovement().with(Direction.Axis.Y, 0.01D));
        attacker.resetFallDistance();
        attacker.hurtMarked = true;

        if (attacker instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
        }
    }

    private static void applySmashKnockback(Player attacker, LivingEntity impactTarget) {
        attacker.level.levelEvent(2013, impactTarget.getOnPos(), 750);
        AABB area = impactTarget.getBoundingBox().inflate(SMASH_ATTACK_KNOCKBACK_RADIUS);

        for (LivingEntity nearby : attacker.level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!canBeKnockedBackBySmash(attacker, impactTarget, nearby)) {
                continue;
            }

            Vec3 delta = nearby.position().subtract(impactTarget.position());
            double distance = delta.length();
            if (distance <= 0.0001D || distance > SMASH_ATTACK_KNOCKBACK_RADIUS) {
                continue;
            }

            double strength = computeSmashKnockbackPower(attacker, nearby, distance);
            if (strength <= 0.0D) {
                continue;
            }

            Vec3 impulse = delta.normalize().scale(strength);
            nearby.push(impulse.x, 0.7D, impulse.z);
            nearby.hurtMarked = true;

            if (nearby instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            }
        }
    }

    private static boolean canBeKnockedBackBySmash(Player attacker, LivingEntity impactTarget, LivingEntity nearby) {
        if (nearby == attacker || nearby == impactTarget || nearby.isSpectator()) {
            return false;
        }

        if (attacker.isAlliedTo(nearby)) {
            return false;
        }

        if (nearby instanceof TamableAnimal tamable && tamable.isTame() && tamable.isOwnedBy(attacker)) {
            return false;
        }

        if (nearby instanceof ArmorStand armorStand && armorStand.isMarker()) {
            return false;
        }

        if (impactTarget.distanceToSqr(nearby) > (SMASH_ATTACK_KNOCKBACK_RADIUS * SMASH_ATTACK_KNOCKBACK_RADIUS)) {
            return false;
        }

        if (nearby instanceof Player player && player.isCreative() && player.getAbilities().flying) {
            return false;
        }

        return true;
    }

    private static double computeSmashKnockbackPower(Player attacker, LivingEntity target, double distance) {
        double power = (SMASH_ATTACK_KNOCKBACK_RADIUS - distance) * 0.7D;
        if (attacker.fallDistance > SMASH_ATTACK_HEAVY_THRESHOLD) {
            power *= 2.0D;
        }

        power *= (1.0D - target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        return Math.max(0.0D, power);
    }

    private static void triggerWindBurst(Player attacker, int windBurstLevel) {
        double burst = getWindBurstKnockbackMultiplier(windBurstLevel);
        attacker.setDeltaMovement(attacker.getDeltaMovement().with(Direction.Axis.Y, 0.55D + (0.42D * burst)));
        attacker.hurtMarked = true;
        spawnWindBurstParticles(attacker, windBurstLevel);

        if (attacker instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
        }

        AABB area = attacker.getBoundingBox().inflate(WIND_BURST_RADIUS);

        for (LivingEntity nearby : attacker.level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (nearby == attacker) {
                continue;
            }

            Vec3 direction = nearby.position().subtract(attacker.position());
            if (direction.lengthSqr() < 0.0001D) {
                continue;
            }

            Vec3 normalized = direction.normalize();
            double distance = nearby.position().distanceTo(attacker.position());
            double distanceScale = Math.max(0.0D, 1.0D - (distance / WIND_BURST_RADIUS));
            double strength = burst * distanceScale * 0.82D;
            nearby.push(normalized.x * strength, 0.24D + (0.08D * windBurstLevel), normalized.z * strength);
            nearby.hurtMarked = true;

            if (nearby instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            }
        }
    }

    private static void spawnWindBurstParticles(Player attacker, int windBurstLevel) {
        if (!(attacker.level instanceof ServerLevel serverLevel)) {
            return;
        }

        double centerY = attacker.getY() + 0.15D;
        serverLevel.sendParticles(ModParticles.GUST_EMITTER_SMALL.get(), attacker.getX(), centerY, attacker.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        if (windBurstLevel >= 2) {
            serverLevel.sendParticles(ModParticles.GUST_EMITTER_LARGE.get(), attacker.getX(), centerY, attacker.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        int gusts = 6 + (windBurstLevel * 3);
        for (int i = 0; i < gusts; i++) {
            double angle = (Math.PI * 2.0D * i) / gusts;
            double radius = 0.45D + (0.22D * windBurstLevel);
            double x = attacker.getX() + (Math.cos(angle) * radius);
            double z = attacker.getZ() + (Math.sin(angle) * radius);
            serverLevel.sendParticles(ModParticles.SMALL_GUST.get(), x, centerY + 0.15D, z, 1, 0.0D, 0.06D, 0.0D, 0.0D);
        }
    }
}
