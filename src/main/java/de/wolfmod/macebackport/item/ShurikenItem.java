package de.wolfmod.macebackport.item;

import de.wolfmod.macebackport.entity.ShurikenEntity;
import de.wolfmod.macebackport.registry.ModEntities;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ShurikenItem extends Item {
    private static final int USE_COOLDOWN_TICKS = 4;

    public ShurikenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW,
                SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!level.isClientSide) {
            ShurikenEntity projectile = new ShurikenEntity(ModEntities.SHURIKEN.get(), player, level);
            projectile.setPos(player.getX(), player.getEyeY() - 0.1D, player.getZ());
            ItemStack thrownStack = stack.copy();
            thrownStack.setCount(1);
            projectile.setItem(thrownStack);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 0.6F);
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
