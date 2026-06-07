package de.wolfmod.macebackport.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;

public final class DensityEnchantment extends MaceOnlyEnchantment {
    public DensityEnchantment() {
        super(Rarity.RARE);
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
        return 5;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        if (!super.checkCompatibility(other)) {
            return false;
        }

        String id = other.getDescriptionId();
        return other != de.wolfmod.macebackport.registry.ModEnchantments.BREACH.get()
                && !"enchantment.minecraft.sharpness".equals(id)
                && !"enchantment.minecraft.smite".equals(id)
                && !"enchantment.minecraft.bane_of_arthropods".equals(id)
                && !"enchantment.minecraft.impaling".equals(id);
    }
}
