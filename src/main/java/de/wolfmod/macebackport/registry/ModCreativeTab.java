package de.wolfmod.macebackport.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTab {
    public static final CreativeModeTab TAB = new CreativeModeTab("macebackport") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModItems.MACE.get());
        }
    };

    private ModCreativeTab() {
    }
}
