package com.github.qpcrummer.lifesteal.menu;

import com.github.qpcrummer.lifesteal.Lifesteal;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenu {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Lifesteal.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<RevivalMenu>> REVIVAL_MENU =
            MENUS.register("revival_menu", () -> new MenuType<>(RevivalMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static class RevivalScreen extends AbstractContainerScreen<RevivalMenu> {
        private static final ResourceLocation CHEST_GUI_TEXTURE =
                ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");

        public RevivalScreen(RevivalMenu menu, Inventory inventory, Component title) {
            super(menu, inventory, title);
            this.imageWidth = 176;
            this.imageHeight = 222;
            this.inventoryLabelY = this.imageHeight - 94;
        }

        @Override
        protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
            int x = (this.width - this.imageWidth) / 2;
            int y = (this.height - this.imageHeight) / 2;
            guiGraphics.blit(CHEST_GUI_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
            super.render(guiGraphics, mouseX, mouseY, partialTick);
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }
}
