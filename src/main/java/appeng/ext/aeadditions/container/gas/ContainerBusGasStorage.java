package appeng.ext.aeadditions.container.gas;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import appeng.ext.aeadditions.container.ContainerUpgradeable;
import appeng.ext.aeadditions.container.slot.SlotRespective;
import appeng.ext.aeadditions.gui.IFluidSlotGuiTransfer;
import appeng.ext.aeadditions.part.gas.PartGasStorage;

/** Ported from ContainerBusGasStorage.kt. */
public class ContainerBusGasStorage extends ContainerUpgradeable {

    private IFluidSlotGuiTransfer guiBusFluidStorage = null;
    public PartGasStorage part;

    public ContainerBusGasStorage(final PartGasStorage part, final EntityPlayer player) {
        this.addSlotToContainer(new SlotRespective(part.getUpgradeInventory(), 0, 187, 8));
        this.part = part;
        this.bindPlayerInventory(player.inventory, 8, 140);
        this.bindUpgradeInventory(part);
        this.bindNetworkToolInventory(player.inventory, part);
    }

    protected void bindPlayerInventory(final IInventory inventoryPlayer) {
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 8; j++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, i * 18 + 140));
            }
        }
        for (int i = 0; i <= 8; i++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 198));
        }
    }

    @Override
    public boolean canInteractWith(final EntityPlayer entityplayer) {
        return this.part.isValid();
    }

    public void setGui(final IFluidSlotGuiTransfer guiBusFluidStorage) {
        this.guiBusFluidStorage = guiBusFluidStorage;
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer player, final int slotnumber) {
        if (this.guiBusFluidStorage != null) {
            this.guiBusFluidStorage.shiftClick(this.getSlot(slotnumber).getStack());
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
                return ItemStack.EMPTY;
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
