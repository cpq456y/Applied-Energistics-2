package appeng.ext.aeadditions.container.gas;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import appeng.ext.aeadditions.container.ContainerUpgradeable;
import appeng.ext.aeadditions.gui.IFluidSlotGuiTransfer;
import appeng.ext.aeadditions.part.gas.PartGasIO;

/** Ported from ContainerBusGasIO.kt. */
public class ContainerBusGasIO extends ContainerUpgradeable {

    private final PartGasIO part;
    private IFluidSlotGuiTransfer guiBusFluidIO = null;

    public ContainerBusGasIO(final PartGasIO part, final EntityPlayer player) {
        this.part = part;

        this.bindPlayerInventory(player.inventory, 8, 102);
        this.bindUpgradeInventory(part);
        this.bindNetworkToolInventory(player.inventory, part);
    }

    @Override
    public boolean canInteractWith(final EntityPlayer entityplayer) {
        return this.part.isValid();
    }

    public void setGui(final IFluidSlotGuiTransfer guiBusFluidIO) {
        this.guiBusFluidIO = guiBusFluidIO;
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer player, final int slotnumber) {
        // TODO: remove the gui from this
        if (this.guiBusFluidIO != null && this.guiBusFluidIO.shiftClick(this.getSlot(slotnumber).getStack())) {
            return ItemStack.EMPTY;
        }
        ItemStack itemstack = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(slotnumber);
        if (slot != null && slot.getHasStack()) {
            final ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (slotnumber < 36) {
                if (!this.mergeItemStack(itemstack1, 36, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, 36, false)) {
                return itemstack1;
            }
            if (itemstack1.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        return itemstack;
    }
}
