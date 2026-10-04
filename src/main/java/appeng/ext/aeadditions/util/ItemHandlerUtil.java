package appeng.ext.aeadditions.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

/** Ported from ItemHandlerUtil.kt. */
public final class ItemHandlerUtil {

    private ItemHandlerUtil() {
    }

    public static ItemStack insertItemStack(final IItemHandler itemHandler, final ItemStack itemStack,
            final boolean simulate) {
        if (itemHandler == null) {
            return itemStack;
        }

        ItemStack itemStackRemaining = itemStack.copy();

        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemStackRemaining = itemHandler.insertItem(i, itemStackRemaining, simulate);
            if (itemStackRemaining.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return itemStackRemaining;
    }
}
