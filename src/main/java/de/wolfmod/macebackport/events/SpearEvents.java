package de.wolfmod.macebackport.events;

import de.wolfmod.macebackport.item.SpearItem;
import de.wolfmod.macebackport.registry.ModEnchantments;
import de.wolfmod.macebackport.registry.ModItemTags;
import de.wolfmod.macebackport.registry.ModSounds;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SpearEvents {
    private static final int COLLISION_HIT_COOLDOWN_TICKS = 12;
    private static final int MAX_COLLISION_TARGETS = 8;
    private static final double MIN_COLLISION_SPEED = 0.12D;
    private static final double COLLISION_REACH = 1.15D;
    private static final java.util.Map<UUID, Long> LAST_COLLISION_HIT = new ConcurrentHashMap<>();
    private static final java.util.Map<UUID, Long> LAST_EMPTY_LUNGE = new ConcurrentHashMap<>();
    private static final Set<UUID> ACTIVE_COLLISION_ATTACKS = ConcurrentHashMap.newKeySet();

    private SpearEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        if (player.level.isClientSide) {
            return;
        }

        if (!player.isUsingItem()) {
            return;
        }

        ItemStack weapon = player.getUseItem();
        if (!weapon.is(ModItemTags.SPEARS) || !(weapon.getItem() instanceof SpearItem spear)) {
            return;
        }

        int chargeTicks = SpearItem.getChargeTicks(player);
        if (chargeTicks < SpearItem.MIN_USE_TICKS || !canCollisionStrike(player)) {
            return;
        }

        List<LivingEntity> targets = findCollisionTargets(player);
        if (targets.isEmpty()) {
            return;
        }

        long gameTime = player.level.getGameTime();
        Long lastHit = LAST_COLLISION_HIT.get(player.getUUID());
        if (lastHit != null && (gameTime - lastHit) < COLLISION_HIT_COOLDOWN_TICKS) {
            return;
        }

        boolean anyHit = false;
        ACTIVE_COLLISION_ATTACKS.add(player.getUUID());
        try {
            for (LivingEntity target : targets) {
                float speedDamage = spear.computeKineticDamageBonus(player, target, chargeTicks);
                float damage = (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE) + speedDamage;
                if (target.hurt(DamageSource.playerAttack(player), damage)) {
                    anyHit = true;
                    player.level.playSound(null, target.getX(), target.getY(), target.getZ(), spear.getHitSound(), SoundSource.PLAYERS, 0.9F, 1.0F);
                }
            }
        } finally {
            ACTIVE_COLLISION_ATTACKS.remove(player.getUUID());
        }

        if (anyHit) {
            LAST_COLLISION_HIT.put(player.getUUID(), gameTime);
            weapon.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(player.getUsedItemHand()));
            player.stopUsingItem();
            player.level.playSound(null, player.getX(), player.getY(), player.getZ(), spear.getAttackSound(), SoundSource.PLAYERS, 1.0F, 0.9F + (0.2F * SpearItem.getChargeScale(chargeTicks)));
        }
    }

    @SubscribeEvent
    public static void onSpearHit(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        if (attacker.level.isClientSide) {
            return;
        }

        if (ACTIVE_COLLISION_ATTACKS.contains(attacker.getUUID())) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        if (!weapon.is(ModItemTags.SPEARS)) {
            return;
        }

        applySpearHit(attacker, event.getEntity(), weapon, event);
        applyLungeIfAvailable(attacker, weapon);
    }

    private static void applySpearHit(Player attacker, LivingEntity target, ItemStack weapon, LivingHurtEvent event) {
        if (!(weapon.getItem() instanceof SpearItem spear)) {
            return;
        }

        float bonus = spear.computeKineticDamageBonus(attacker, target);
        if (bonus > 0.0F) {
            event.setAmount(event.getAmount() + bonus);
            attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), spear.getAttackSound(), SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        attacker.level.playSound(null, target.getX(), target.getY(), target.getZ(), spear.getHitSound(), SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    private static void applyLungeIfAvailable(Player attacker, ItemStack weapon) {
        int lungeLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LUNGE.get(), weapon);
        if (lungeLevel <= 0 || !canUseLunge(attacker)) {
            return;
        }

        applyLunge(attacker, weapon, lungeLevel);
    }

    public static void tryLungeFromEmptyAttack(ServerPlayer attacker) {
        if (attacker.level.isClientSide || attacker.isSpectator() || attacker.getAttackStrengthScale(0.5F) < 0.9F) {
            return;
        }

        long gameTime = attacker.level.getGameTime();
        Long lastLunge = LAST_EMPTY_LUNGE.get(attacker.getUUID());
        if (lastLunge != null && (gameTime - lastLunge) < 6L) {
            return;
        }

        ItemStack weapon = attacker.getMainHandItem();
        int lungeLevel = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.LUNGE.get(), weapon);
        if (!weapon.is(ModItemTags.SPEARS) || lungeLevel <= 0 || !canUseLunge(attacker)) {
            return;
        }

        LAST_EMPTY_LUNGE.put(attacker.getUUID(), gameTime);
        applyLunge(attacker, weapon, lungeLevel);
        attacker.resetAttackStrengthTicker();
        attacker.swing(net.minecraft.world.InteractionHand.MAIN_HAND, true);
    }

    private static boolean canUseLunge(Player player) {
        if (player.isPassenger() || player.isFallFlying() || player.isInWaterOrBubble()) {
            return false;
        }

        if (player.isCreative()) {
            return true;
        }

        return player.getFoodData().getFoodLevel() >= 7;
    }

    private static boolean canCollisionStrike(Player player) {
        if (player.isPassenger() || player.isFallFlying() || player.isSpectator()) {
            return false;
        }

        Vec3 movement = player.getDeltaMovement();
        return horizontalSpeed(movement) >= MIN_COLLISION_SPEED || player.isSprinting();
    }

    private static List<LivingEntity> findCollisionTargets(Player player) {
        Vec3 movement = player.getDeltaMovement();
        Vec3 look = player.getLookAngle();
        Vec3 direction = horizontalSpeed(movement) > 0.0001D ? new Vec3(movement.x, 0.0D, movement.z).normalize() : new Vec3(look.x, 0.0D, look.z).normalize();
        AABB area = player.getBoundingBox()
                .inflate(0.35D, 0.25D, 0.35D)
                .expandTowards(direction.x * COLLISION_REACH, 0.0D, direction.z * COLLISION_REACH);

        List<LivingEntity> targets = new ArrayList<>();
        for (LivingEntity candidate : player.level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (!canBeCollisionTarget(player, candidate)) {
                continue;
            }

            Vec3 toCandidate = candidate.position().subtract(player.position());
            Vec3 horizontalToCandidate = new Vec3(toCandidate.x, 0.0D, toCandidate.z);
            if (horizontalToCandidate.lengthSqr() > 0.0001D && horizontalToCandidate.normalize().dot(direction) < 0.25D) {
                continue;
            }

            targets.add(candidate);
        }

        targets.sort(Comparator.comparingDouble(player::distanceToSqr));
        if (targets.size() > MAX_COLLISION_TARGETS) {
            return new ArrayList<>(targets.subList(0, MAX_COLLISION_TARGETS));
        }

        return targets;
    }

    private static boolean canBeCollisionTarget(Player attacker, LivingEntity candidate) {
        if (candidate == attacker || !candidate.isAlive() || candidate.isSpectator()) {
            return false;
        }

        if (attacker.isAlliedTo(candidate)) {
            return false;
        }

        if (candidate instanceof ArmorStand armorStand && armorStand.isMarker()) {
            return false;
        }

        if (candidate instanceof TamableAnimal tamable && tamable.isTame() && tamable.isOwnedBy(attacker)) {
            return false;
        }

        if (candidate instanceof Player player && player.isCreative() && player.getAbilities().flying) {
            return false;
        }

        return true;
    }

    private static double horizontalSpeed(Vec3 movement) {
        return Math.sqrt((movement.x * movement.x) + (movement.z * movement.z));
    }

    private static void applyLunge(Player attacker, ItemStack weapon, int level) {
        double magnitude = 0.458D * level;
        Vec3 look = attacker.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() > 0.0001D) {
            Vec3 impulse = horizontal.normalize().scale(magnitude);
            attacker.setDeltaMovement(attacker.getDeltaMovement().add(impulse.x, 0.0D, impulse.z));
            attacker.hurtMarked = true;

            if (attacker instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            }
        }

        attacker.causeFoodExhaustion(4.0F * level);
        weapon.hurtAndBreak(1, attacker, p -> p.broadcastBreakEvent(attacker.getUsedItemHand()));

        SoundEvent sound = switch (Mth.clamp(level, 1, 3)) {
            case 1 -> ModSounds.SPEAR_LUNGE_1.get();
            case 2 -> ModSounds.SPEAR_LUNGE_2.get();
            default -> ModSounds.SPEAR_LUNGE_3.get();
        };

        attacker.level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), sound, SoundSource.PLAYERS, 1.0F, 1.0F);
    }
}
