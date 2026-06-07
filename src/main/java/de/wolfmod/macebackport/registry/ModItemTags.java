package de.wolfmod.macebackport.registry;

import de.wolfmod.macebackport.MaceBackportMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {
    public static final TagKey<Item> SPEARS = ItemTags.create(new ResourceLocation(MaceBackportMod.MODID, "spears"));

    private ModItemTags() {
    }
}
