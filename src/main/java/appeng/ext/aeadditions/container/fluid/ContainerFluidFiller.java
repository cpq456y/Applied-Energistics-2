package appeng.ext.aeadditions.container.fluid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import appeng.ext.aeadditions.container.ContainerUpgradeable;
import appeng.ext.aeadditions.gui.fluid.GuiFluidFiller;
import appeng.ext.aeadditions.tileentity.TileEntityFluidFiller;
import appeng.ext.aeadditions.util.FluidHelper;

/** Ported from ContainerFluidFiller.kt. */
public class ContainerFluidFiller extends ContainerUpgradeable {

    private TileEntityFluidFiller tileEntity;

    public GuiFluidFiller gui = null;

    public ContainerFluidFiller(final InventoryPlayer player, final TileEntityFluidFiller tileEntity) {
        this.tileEntity = tileEntity;

        this.bindPlayerInventory(player);
        this.bindUpgradeInventory(tileEntity);
        this.bindNetworkToolInventory(player, tileEntity, 185, 78);
    }

    public TileEntityFluidFiller getTileEntity() {
        return this.tileEntity;
    }

    public void setTileEntity(final TileEntityFluidFiller tileEntity) {
        this.tileEntity = tileEntity;
    }

    @Override
    public boolean canInteractWith(final EntityPlayer entityplayer) {
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer player, final int slotnumber) {
        if (this.gui != null && this.gui.shiftClick(this.getSlot(slotnumber).getStack())) {
            return ItemStack.EMPTY;
        }

        ItemStack transferStack = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(slotnumber);
        if (slot != null && slot.getHasStack()) {
            final ItemStack itemStack = slot.getStack();
            transferStack = itemStack.copy();
            if (FluidHelper.isEmpty(itemStack)) {
                this.tileEntity.containerItem = itemStack.copy();
                this.tileEntity.markDirty();
                return ItemStack.EMPTY;
            } else if (slotnumber < 27) {
                if (!this.mergeItemStack(itemStack, 27, this.inventorySlots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemStack, 0, 27, false)) {
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
}
