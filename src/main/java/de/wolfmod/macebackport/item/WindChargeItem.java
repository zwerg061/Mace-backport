package de.wolfmod.macebackport.item;

import de.wolfmod.macebackport.entity.WindChargeEntity;
import de.wolfmod.macebackport.registry.ModEntities;
import de.wolfmod.macebackport.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class WindChargeItem extends Item {
    private static final int USE_COOLDOWN_TICKS = 10;

    public WindChargeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.WIND_CHARGE_THROW.get(),
                SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide) {
            WindChargeEntity projectile = new WindChargeEntity(ModEntities.WIND_CHARGE.get(), player, level);
            projectile.setPos(player.getX(), player.getEyeY(), player.getZ());
            ItemStack thrownStack = stack.copy();
            thrownStack.setCount(1);
            projectile.setItem(thrownStack);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(projectile);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, USE_COOLDOWN_TICKS);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
