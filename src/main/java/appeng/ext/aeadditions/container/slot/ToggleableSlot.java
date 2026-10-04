package appeng.ext.aeadditions.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import appeng.ext.aeadditions.api.inventory.IToggleableSlotsInventory;

/** Ported from ToggleableSlot.kt. */
public class ToggleableSlot extends Slot {

    public final IToggleableSlotsInventory toggleAbleSlotInventory;

    public ToggleableSlot(final IToggleableSlotsInventory toggleAbleSlotInventory, final int slotIndex, final int x,
            final int y) {
        super(toggleAbleSlotInventory, slotIndex, x, y);
        this.toggleAbleSlotInventory = toggleAbleSlotInventory;
    }

    public boolean isSlotEnabled() {
        final Boolean enabled = this.toggleAbleSlotInventory.getEnabledSlots().get(this.slotNumber);
        return enabled != null && enabled;
    }

    @Override
    public boolean canTakeStack(final EntityPlayer playerIn) {
        return this.isSlotEnabled();
    }

    @Override
    public boolean isItemValid(final ItemStack itemstack) {
        return this.isSlotEnabled() && this.inventory.isItemValidForSlot(this.slotNumber, itemstack);
    }
}
