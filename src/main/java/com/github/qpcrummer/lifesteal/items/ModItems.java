package com.github.qpcrummer.lifesteal.items;

import com.github.qpcrummer.lifesteal.Lifesteal;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Lifesteal.MOD_ID);

    public static final DeferredHolder<Item, Item> HEART = ITEMS.register("heart", () ->
            new HeartItem(new Item.Properties().stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));

    public static final DeferredHolder<Item, Item> TOTEM = ITEMS.register("totem_of_rebirth", () ->
            new TotemOfRebirth(new Item.Properties().stacksTo(1).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));

    public static final DeferredHolder<Item, Item> HEART_DUST = ITEMS.register("heart_dust", () ->
            new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> HEART_CRYSTAL = ITEMS.register("heart_crystal", () ->
            new Item(new Item.Properties()));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}