package com.github.qpcrummer.lifesteal_reimagined.items;

import com.github.qpcrummer.lifesteal_reimagined.gamerules.LifeStealGamerules;
import com.github.qpcrummer.lifesteal_reimagined.gamerules.ReviveMethod;
import com.github.qpcrummer.lifesteal_reimagined.menu.RevivalGUI;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TotemOfRebirth extends Item {

    public TotemOfRebirth(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (user instanceof ServerPlayer serverPlayer && world.getGameRules().getRule(LifeStealGamerules.REVIVE_METHOD).get() == ReviveMethod.TOTEM) {
            RevivalGUI.openGUI(serverPlayer, hand);
        }
        return super.use(world, user, hand);
    }

    @Override
    public int getDefaultMaxStackSize() {
        return 1;
    }
}
