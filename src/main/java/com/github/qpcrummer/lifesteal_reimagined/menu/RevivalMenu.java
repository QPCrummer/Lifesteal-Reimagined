package com.github.qpcrummer.lifesteal_reimagined.menu;

import com.github.qpcrummer.lifesteal_reimagined.data.DeathData;
import com.github.qpcrummer.lifesteal_reimagined.gamerules.LifeStealGamerules;
import com.github.qpcrummer.lifesteal_reimagined.utils.PlayerUtils;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ResolvableProfile;
import java.util.UUID;

public class RevivalMenu extends AbstractContainerMenu {
    private final Container container;
    private final InteractionHand hand;

    public RevivalMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, InteractionHand.MAIN_HAND);
    }

    public RevivalMenu(int containerId, Inventory playerInventory, InteractionHand hand) {
        super(ModMenu.REVIVAL_MENU.get(), containerId);
        this.container = new SimpleContainer(54);
        this.hand = hand;

        for (int row = 0; row < 6; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public Container getContainer() {
        return this.container;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId < 0 || slotId >= 54 || !(player instanceof ServerPlayer serverPlayer)) {
            super.clicked(slotId, button, clickType, player);
            return;
        }

        ItemStack clickedItem = this.container.getItem(slotId);

        if (clickedItem.is(Items.PLAYER_HEAD)) {
            ResolvableProfile profile = clickedItem.get(DataComponents.PROFILE);
            if (profile != null && profile.id().isPresent()) {
                UUID deadPlayerUuid = profile.id().get();
                if (DeathData.isPlayerDead(deadPlayerUuid, serverPlayer.level().getGameRules().getRule(LifeStealGamerules.AUTO_REVIVAL).get())) {
                    PlayerUtils.revive(deadPlayerUuid, serverPlayer, true, this.hand);
                    serverPlayer.closeContainer();
                }
                return;
            }
        }

        super.clicked(slotId, button, clickType, player);
    }
}