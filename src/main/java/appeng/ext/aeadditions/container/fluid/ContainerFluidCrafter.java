package appeng.ext.aeadditions.container.fluid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import appeng.ext.aeadditions.container.ContainerUpgradeable;
import appeng.ext.aeadditions.container.slot.ToggleableSlot;
import appeng.ext.aeadditions.tileentity.TileEntityFluidCrafter;

/** Ported from ContainerFluidCrafter.kt. */
public class ContainerFluidCrafter extends ContainerUpgradeable {

    private final TileEntityFluidCrafter tileEntity;

    public ContainerFluidCrafter(final InventoryPlayer player, final TileEntityFluidCrafter tileEntity) {
        this.tileEntity = tileEntity;

        this.bindInventory();
        this.bindPlayerInventory(player, 8, 100);
        this.bindUpgradeInventory(tileEntity);
        this.bindNetworkToolInventory(player, tileEntity, 185, 114);
    }

    public TileEntityFluidCrafter getTileEntity() {
        return this.tileEntity;
    }

    @Override
    public boolean canInteractWith(final EntityPlayer entityplayer) {
        return true;
    }

    @Override
    public void onContainerClosed(final EntityPlayer entityplayer) {
        super.onContainerClosed(entityplayer);
    }

    public void onCapacityChanged() {
//        bindInventory()
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer player, final int slotnumber) {
        ItemStack transferStack = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(slotnumber);
        if (slot != null && slot.getHasStack()) {
            final ItemStack itemStack = slot.getStack();
            transferStack = itemStack.copy();
            if (slotnumber < 9) {
                if (!this.mergeItemStack(itemStack, 9, this.inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotnumber < 36) {
                if (!this.mergeItemStack(itemStack, 0, 9, false)
                        && !this.mergeItemStack(itemStack, 36, this.inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemStack, 0, 9, false)
                    && !this.mergeItemStack(itemStack, 9, 36, false)) {
                return ItemStack.EMPTY;
            }
            if (itemStack.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }
        return transferStack;
    }

    private void bindInventory() {
        for (final Integer it : this.tileEntity.getFilterOrder()) {
            final int row = (int) Math.ceil((it + 1) / 3.0) - 1;
            final int column = it % 3;

            this.addSlotToContainer(
                    new ToggleableSlot(this.tileEntity.inventory, it, 62 + column * 18, 27 + row * 18));
        }
    }
}
