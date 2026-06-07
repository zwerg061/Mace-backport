package de.wolfmod.macebackport.enchantment;

import net.minecraft.world.item.enchantment.Enchantment;

public final class BreachEnchantment extends MaceOnlyEnchantment {
    public BreachEnchantment() {
        super(Rarity.RARE);
    }

    @Override
    public int getMinCost(int level) {
        return 15 + ((level - 1) * 9);
    }

    @Override
    public int getMaxCost(int level) {
        return 65 + ((level - 1) * 9);
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        if (!super.checkCompatibility(other)) {
            return false;
        }

        String id = other.getDescriptionId();
        return other != de.wolfmod.macebackport.registry.ModEnchantments.DENSITY.get()
                && !"enchantment.minecraft.sharpness".equals(id)
                && !"enchantment.minecraft.smite".equals(id)
                && !"enchantment.minecraft.bane_of_arthropods".equals(id)
                && !"enchantment.minecraft.impaling".equals(id);
    }
}
