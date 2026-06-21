package com.github.qpcrummer.lifesteal_reimagined.items;

import com.github.qpcrummer.lifesteal_reimagined.Lifesteal;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {

    // Heart
    private static final ResourceKey<Item> key_heart = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Lifesteal.MOD_ID, "heart"));
    public static final Item HEART = register(
            new HeartItem(new Item.Properties().stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)),
            key_heart
    );

    // Totem
    private static final ResourceKey<Item> key_totem = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Lifesteal.MOD_ID, "totem_of_rebirth"));
    public static final Item TOTEM = register(
            new TotemOfRebirth(new Item.Properties().stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)),
            key_totem
    );

    // Other Items
    public static void initialize() {
        // Dust
        ResourceKey<Item> key_heart_dust = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Lifesteal.MOD_ID, "heart_dust"));
        register(
                new Item(new Item.Properties()),
                key_heart_dust
        );

        // Crystal
        ResourceKey<Item> key_heart_crystal = ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Lifesteal.MOD_ID, "heart_crystal"));
        register(
                new Item(new Item.Properties()),
                key_heart_crystal
        );
    }

    public static Item register(Item item, ResourceKey<Item> key) {
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }
}
