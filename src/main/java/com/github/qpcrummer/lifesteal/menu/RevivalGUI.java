package com.github.qpcrummer.lifesteal.menu;

import com.github.qpcrummer.lifesteal.data.DeathData;
import com.github.qpcrummer.lifesteal.utils.DeadPlayerIdentification;
import com.github.qpcrummer.lifesteal.utils.LifeStealText;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.ResolvableProfile;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.List;
import java.util.UUID;

public class RevivalGUI {
    private static final int GUI_SIZE = 45;
    private static final String ARROW_RIGHT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19";
    private static final String ARROW_LEFT = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==";

    public static void openGUI(ServerPlayer player, InteractionHand hand) {
        List<DeadPlayerIdentification> deadList = DeathData.getDeadPlayers(player.level().getServer());
        int[] currentPage = {0};
        int pages = 1 + deadList.size() / GUI_SIZE;

        player.openMenu(new SimpleMenuProvider((containerId, playerInv, p) -> {
            RevivalMenu menu = new RevivalMenu(containerId, playerInv, hand);
            refreshPage(menu, currentPage[0], pages, deadList);
            return menu;
        }, LifeStealText.TITLE));
    }

    // Refresh elements inside the screen dynamically
    public static void refreshPage(RevivalMenu menu, int page, int totalPages, List<DeadPlayerIdentification> deadList) {
        net.minecraft.world.Container container = menu.getContainer();
        container.clearContent();

        // Populate heads
        for (int i = 0; i < GUI_SIZE; i++) {
            int index = page * GUI_SIZE + i;
            if (index >= deadList.size()) break;

            DeadPlayerIdentification deadPlayer = deadList.get(index);
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);
            head.set(DataComponents.PROFILE, new ResolvableProfile(new GameProfile(deadPlayer.uuid(), deadPlayer.name())));
            head.set(DataComponents.CUSTOM_NAME, Component.literal(deadPlayer.name()));
            container.setItem(i, head);
        }

        // Gray filler borders
        ItemStack filler = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        filler.set(DataComponents.CUSTOM_NAME, Component.empty());
        for (int i = GUI_SIZE; i < 54; i++) {
            container.setItem(i, filler);
        }

        // Back arrow button item configuration
        if (page > 0) {
            ItemStack backArrow = createCustomTextureHead(ARROW_LEFT, LifeStealText.BACK);
            container.setItem(GUI_SIZE, backArrow);
        }

        // Next arrow button item configuration
        if (page < totalPages - 1) {
            ItemStack nextArrow = createCustomTextureHead(ARROW_RIGHT, LifeStealText.NEXT);
            container.setItem(53, nextArrow);
        }
    }

    private static ItemStack createCustomTextureHead(String base64, Component name) {
        ItemStack head = new ItemStack(Items.PLAYER_HEAD);
        GameProfile profile = new GameProfile(UUID.randomUUID(), "");
        profile.getProperties().put("textures", new Property("textures", base64));
        head.set(DataComponents.PROFILE, new ResolvableProfile(profile));
        head.set(DataComponents.CUSTOM_NAME, name);
        return head;
    }
}
