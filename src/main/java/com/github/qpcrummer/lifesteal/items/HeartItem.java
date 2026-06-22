package com.github.qpcrummer.lifesteal.items;

import com.github.qpcrummer.lifesteal.gamerules.LifeStealGamerules;
import com.github.qpcrummer.lifesteal.utils.PlayerUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class HeartItem extends Item {

    public HeartItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if(world.isClientSide() || user.isShiftKeyDown()) {
            return super.use(world, user, hand);
        }

        final ItemStack stack = user.getItemInHand(hand);

        if (PlayerUtils.incrementHearts((ServerPlayer) user)) {
            stack.shrink(1);
            return InteractionResultHolder.pass(stack);
        } else {
            return InteractionResultHolder.fail(stack);
        }
    }

    @Nullable
    public static String getCustomName(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            throw new AssertionError("stack is empty");
        }

        if (hasCustomName(stack)) {
            return stack.getHoverName().getString();
        }

        return null;
    }

    private static boolean hasCustomName(ItemStack stack) {
        return stack.get(DataComponents.CUSTOM_NAME) != null;
    }

    @Override
    public int getDefaultMaxStackSize() {
        return LifeStealGamerules.getStatic(LifeStealGamerules.HEART_STACK_SIZE, GameRules.IntegerValue.create(1).createRule()).get();
    }
}
