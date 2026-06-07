package de.wolfmod.macebackport.enchantment;

public final class WindBurstEnchantment extends MaceOnlyEnchantment {
    public WindBurstEnchantment() {
        super(Rarity.VERY_RARE);
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
        return 3;
    }
}
