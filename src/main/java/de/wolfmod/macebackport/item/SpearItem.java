package de.wolfmod.macebackport.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import de.wolfmod.macebackport.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;
import java.util.UUID;

public final class SpearItem extends Item {
    private static final UUID SPEAR_ATTACK_RANGE_UUID = UUID.fromString("2a7f0368-3277-47d1-83c7-f9c20cdbba35");
    public static final int MIN_USE_TICKS = 10;
    public static final int FULL_CHARGE_TICKS = 45;
    private static final double ATTACK_RANGE_BONUS = 1.5D;

    private final SpearStats stats;
    private final Multimap<Attribute, AttributeModifier> defaultModifiers;

    public SpearItem(SpearStats stats, Properties properties) {
        super(properties);
        this.stats = stats;

        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", stats.attackDamageBonus(), AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", stats.attackSpeedBonus(), AttributeModifier.Operation.ADDITION));
        builder.put(ForgeMod.ATTACK_RANGE.get(), new AttributeModifier(SPEAR_ATTACK_RANGE_UUID, "Spear range modifier", ATTACK_RANGE_BONUS, AttributeModifier.Operation.ADDITION));
        this.defaultModifiers = builder.build();
    }

    public SpearStats getStats() {
        return this.stats;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        // Charge is consumed while holding and colliding, not after releasing.
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.model.HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand, ItemStack stack) {
                if (entity.isUsingItem() && entity.getUsedItemHand() == hand && entity.getUseItem().is(stack.getItem())) {
                    return net.minecraft.client.model.HumanoidModel.ArmPose.THROW_SPEAR;
                }

                return null;
            }
        });
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.defaultModifiers : super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return this.stats.enchantmentValue();
    }

    public float computeKineticDamageBonus(Player attacker, LivingEntity target) {
        double attackerSpeed = horizontalSpeed(attacker.getDeltaMovement().x, attacker.getDeltaMovement().z);
        double relativeSpeed = horizontalSpeed(attacker.getDeltaMovement().x - target.getDeltaMovement().x, attacker.getDeltaMovement().z - target.getDeltaMovement().z);

        float bonus = 0.0F;

        if (attackerSpeed >= 0.18D) {
            bonus += this.stats.movingDamageBonus() * (float) Math.min(2.0D, attackerSpeed / 0.32D);
        }

        if (relativeSpeed >= 0.35D) {
            bonus += this.stats.relativeDamageBonus() * (float) Math.min(1.5D, relativeSpeed / 0.6D);
        }

        return bonus;
    }

    public float computeKineticDamageBonus(Player attacker, LivingEntity target, int chargeTicks) {
        double attackerSpeed = horizontalSpeed(attacker.getDeltaMovement().x, attacker.getDeltaMovement().z);
        double relativeSpeed = horizontalSpeed(attacker.getDeltaMovement().x - target.getDeltaMovement().x, attacker.getDeltaMovement().z - target.getDeltaMovement().z);

        float chargeScale = getChargeScale(chargeTicks);
        float speedScale = (float) Math.min(1.75D, Math.max(0.0D, attackerSpeed - 0.08D) / 0.42D);
        float relativeScale = (float) Math.min(1.5D, Math.max(0.0D, relativeSpeed - 0.12D) / 0.55D);

        return (this.stats.primedDamageBonus() * chargeScale)
                + (this.stats.movingDamageBonus() * speedScale * chargeScale)
                + (this.stats.relativeDamageBonus() * relativeScale);
    }

    public static int getChargeTicks(Player player) {
        return Math.max(0, player.getTicksUsingItem());
    }

    public static float getChargeScale(int chargeTicks) {
        if (chargeTicks < MIN_USE_TICKS) {
            return 0.0F;
        }

        return Math.min(1.0F, (chargeTicks - MIN_USE_TICKS) / (float) (FULL_CHARGE_TICKS - MIN_USE_TICKS));
    }

    public SoundEvent getAttackSound() {
        return this.stats.wooden() ? ModSounds.WOODEN_SPEAR_ATTACK.get() : ModSounds.SPEAR_ATTACK.get();
    }

    public SoundEvent getHitSound() {
        return this.stats.wooden() ? ModSounds.WOODEN_SPEAR_HIT.get() : ModSounds.SPEAR_HIT.get();
    }

    public int getCooldownTicks() {
        return this.stats.cooldownTicks();
    }

    private static double horizontalSpeed(double x, double z) {
        return Math.sqrt((x * x) + (z * z));
    }

    public record SpearStats(
            double attackDamageBonus,
            double attackSpeedBonus,
            float primedDamageBonus,
            float movingDamageBonus,
            float relativeDamageBonus,
            int cooldownTicks,
            int enchantmentValue,
            boolean wooden
    ) {
        public static SpearStats of(float attackDamageBonus, float swingSeconds, float primedDamageBonus,
                                    float movingDamageBonus, float relativeDamageBonus, int enchantmentValue,
                                    boolean wooden) {
            return new SpearStats(attackDamageBonus, (1.0F / swingSeconds) - 4.0F,
                    primedDamageBonus, movingDamageBonus, relativeDamageBonus,
                    Math.max(1, Math.round(swingSeconds * 20.0F)), enchantmentValue, wooden);
        }
    }
}
