package appeng.ext.aeadditions.me.storage;

import net.minecraft.item.ItemStack;

import appeng.api.implementations.guiobjects.IGuiItemObject;

/**
 * Host object handed to AE2's GuiBridge for the portable fluid/gas cells. AE2 resolves an item GUI through
 * {@code IGuiItem#getGuiObject}, whose result is then matched against the bridge entry's host class and passed
 * to the two argument container/GUI constructors.
 */
public class PortableCellGuiObject implements IGuiItemObject {

    private final ItemStack stack;

    public PortableCellGuiObject(final ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ItemStack getItemStack() {
        return this.stack;
    }
}
