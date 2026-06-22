package com.github.qpcrummer.lifesteal.world;

import com.github.qpcrummer.lifesteal.Lifesteal;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class Ores {

    public static final ResourceKey<PlacedFeature> HEART_ORE_PLACED_KEY = ResourceKey.create(
            Registries.PLACED_FEATURE,
            ResourceLocation.fromNamespaceAndPath(Lifesteal.MOD_ID, "heart_ore")
    );

    public static void initOres() {}
}