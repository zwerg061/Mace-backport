package de.wolfmod.macebackport.enchantment;

import de.wolfmod.macebackport.registry.ModItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public final class LungeEnchantment extends Enchantment {
    public LungeEnchantment() {
        super(Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 5 + ((level - 1) * 8);
    }

    @Override
    public int getMaxCost(int level) {
        return 25 + ((level - 1) * 8);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.is(ModItemTags.SPEARS);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return stack.is(ModItemTags.SPEARS);
    }
}
