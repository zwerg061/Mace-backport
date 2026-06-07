package de.wolfmod.macebackport.registry;

import de.wolfmod.macebackport.MaceBackportMod;
import de.wolfmod.macebackport.item.BackportMaceItem;
import de.wolfmod.macebackport.item.ShurikenItem;
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

    public static final RegistryObject<Item> SHURIKEN = ITEMS.register("shuriken", () ->
            new ShurikenItem(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON).tab(ModCreativeTab.TAB)));

    private ModItems() {
    }
}
