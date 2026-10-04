package appeng.ext.aeadditions.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.ext.aeadditions.container.slot.SlotRespective;
import appeng.ext.aeadditions.tileentity.TileEntityHardMeDrive;

/** Ported from ContainerHardMEDrive.kt. */
public class ContainerHardMEDrive extends Container {

    private final InventoryPlayer playerInventory;
    private final TileEntityHardMeDrive tile;

    public ContainerHardMEDrive(final InventoryPlayer inventory, final TileEntityHardMeDrive tile) {
        this.playerInventory = inventory;
        this.tile = tile;

        for (int i = 0; i <= 2; i++) {
            final int slotIndex = i;
            this.addSlotToContainer(new SlotRespective(tile.getInventory(), slotIndex, 80, 17 + slotIndex * 18) {
                @Override
                public boolean isItemValid(final ItemStack itemstack) {
                    return AEApi.instance().registries().cell().isCellHandled(itemstack);
                }
            });
        }

        this.bindPlayerInventory();
    }

    /** Named differently from {@link Container#getInventory()} to avoid an incompatible override. */
    public InventoryPlayer getPlayerInventory() {
        return this.playerInventory;
    }

    public TileEntityHardMeDrive getTile() {
        return this.tile;
    }

    protected void bindPlayerInventory() {
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 8; j++) {
                this.addSlotToContainer(new Slot(this.playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int i = 0; i <= 8; i++) {
            this.addSlotToContainer(new Slot(this.playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer playerIn, final int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(0);

        if (slot != null && slot.getHasStack()) {
            final ItemStack itemStack1 = slot.getStack();

            itemStack = itemStack1.copy();

            if (AEApi.instance().registries().cell().isCellHandled(itemStack)) {
                if (index < 3) {
                    if (!this.mergeItemStack(itemStack1, 3, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.mergeItemStack(itemStack1, 0, 3, false)) {
                    return ItemStack.EMPTY;
                }

                if (itemStack1.getCount() == 0) {
                    slot.putStack(ItemStack.EMPTY);
                } else {
                    slot.onSlotChanged();
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

        return itemStack;
    }

    @Override
    public boolean canInteractWith(final EntityPlayer player) {
        if (this.tile.hasWorld()) {
            return this.tile.isUseableByPlayer(player);
        }
        return false;
    }
}
