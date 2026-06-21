package com.github.qpcrummer.lifesteal_reimagined.block;

import com.github.qpcrummer.lifesteal_reimagined.Lifesteal;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Lifesteal.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Lifesteal.MOD_ID);

    public static final DeferredHolder<Block, Block> DEEPSLATE_HEART_ORE = register(
            "deepslate_heart_ore",
            SoundType.DEEPSLATE
    );

    public static final DeferredHolder<Block, Block> HEART_ORE = register(
            "heart_ore",
            SoundType.STONE
    );

    private static DeferredHolder<Block, Block> register(String name, SoundType soundGroup) {
        DeferredHolder<Block, Block> blockHolder = BLOCKS.register(name, () -> new Block(
                BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE)
                        .requiresCorrectToolForDrops()
                        .strength(6.0f, 6.0f)
                        .sound(soundGroup)
        ));


        ITEMS.register(name, () -> new BlockItem(
                blockHolder.get(),
                new Item.Properties()
        ));

        return blockHolder;
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }
}
