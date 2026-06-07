package de.wolfmod.macebackport.registry;

import de.wolfmod.macebackport.MaceBackportMod;
import de.wolfmod.macebackport.item.BackportMaceItem;
import de.wolfmod.macebackport.item.SpearItem;
import de.wolfmod.macebackport.item.SpearItem.SpearStats;
import de.wolfmod.macebackport.item.WindChargeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MaceBackportMod.MODID);

    public static final RegistryObject<Item> MACE = ITEMS.register("mace", () ->
            new BackportMaceItem(new Item.Properties().stacksTo(1).durability(500).rarity(Rarity.RARE).tab(ModCreativeTab.TAB)));

    public static final RegistryObject<Item> WIND_CHARGE = ITEMS.register("wind_charge", () ->
            new WindChargeItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON).tab(ModCreativeTab.TAB)));

    public static final RegistryObject<Item> WOODEN_SPEAR = ITEMS.register("wooden_spear", () ->
            new SpearItem(SpearStats.of(0.0F, 0.65F, 1.5F, 0.75F, 0.75F, 15, true),
                    new Item.Properties().stacksTo(1).durability(59).rarity(Rarity.COMMON).tab(ModCreativeTab.TAB)));

    public static final RegistryObject<Item> STONE_SPEAR = ITEMS.register("stone_spear", () ->
            new SpearItem(SpearStats.of(1.0F, 0.75F, 2.0F, 1.0F, 1.0F, 5, false),
                    new Item.Properties().stacksTo(1).durability(131).rarity(Rarity.COMMON).tab(ModCreativeTab.TAB)));

    public static final RegistryObject<Item> COPPER_SPEAR = ITEMS.register("copper_spear", () ->
            new SpearItem(SpearStats.of(1.0F, 0.85F, 2.25F, 1.15F, 1.1F, 10, false),
                    new Item.Properties().stacksTo(1).durability(190).rarity(Rarity.UNCOMMON).tab(ModCreativeTab.TAB)));

    public static final RegistryObject<Item> IRON_SPEAR = ITEMS.register("iron_spear", () ->
            new SpearItem(SpearStats.of(2.0F, 0.95F, 2.75F, 1.35F, 1.25F, 14, false),
                    new Item.Properties().stacksTo(1).durability(250).rarity(Rarity.UNCOMMON).tab(ModCreativeTab.TAB)));

    public static final RegistryObject<Item> GOLDEN_SPEAR = ITEMS.register("golden_spear", () ->
            new SpearItem(SpearStats.of(0.0F, 0.95F, 2.25F, 1.25F, 1.1F, 22, false),
                    new Item.Properties().stacksTo(1).durability(32).rarity(Rarity.UNCOMMON).tab(ModCreativeTab.TAB)));

    public static final RegistryObject<Item> DIAMOND_SPEAR = ITEMS.register("diamond_spear", () ->
            new SpearItem(SpearStats.of(3.0F, 1.05F, 3.25F, 1.55F, 1.4F, 10, false),
                    new Item.Properties().stacksTo(1).durability(1561).rarity(Rarity.RARE).tab(ModCreativeTab.TAB)));

    public static final RegistryObject<Item> NETHERITE_SPEAR = ITEMS.register("netherite_spear", () ->
            new SpearItem(SpearStats.of(4.0F, 1.15F, 3.75F, 1.75F, 1.6F, 15, false),
                    new Item.Properties().stacksTo(1).durability(2031).rarity(Rarity.EPIC).tab(ModCreativeTab.TAB).fireResistant()));

    private ModItems() {
    }
}
