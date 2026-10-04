package appeng.ext.aeadditions.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.ext.aeadditions.item.ItemFluid;
import appeng.ext.aeadditions.registries.ItemEnum;

/** Ported from ECFluidFilterInventory.kt. */
public class ECFluidFilterInventory extends InventoryPlain {

    private final ItemStack cellItem;

    public ECFluidFilterInventory(final String customName, final int size, final ItemStack cellItem) {
        super(customName, size, 1);
        this.cellItem = cellItem;

        if (cellItem.hasTagCompound() && cellItem.getTagCompound().hasKey("filter")) {
            this.readFromNBT(cellItem.getTagCompound().getTagList("filter", 10));
        }
    }

    private static String fluidNameOf(final ItemStack stack) {
        final FluidStack fluidStack = ItemFluid.getFluid(stack);
        if (fluidStack == null || fluidStack.getFluid() == null) {
            return "";
        }
        return fluidStack.getFluid().getName();
    }

    @Override
    public boolean isItemValidForSlot(final int index, final ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return false;
        }
        if (itemStack.getItem() == ItemEnum.FLUIDITEM.getItem()) {
            final String fluidName = fluidNameOf(itemStack);
            for (final ItemStack slotStack : this.slots) {
                if (slotStack == null || slotStack.isEmpty()) {
                    continue;
                }
                final String itemFluidName = fluidNameOf(slotStack);
                if (itemFluidName.equals(fluidName)) {
                    return false;
                }
            }
            return true;
        }
        final FluidStack stack = FluidUtil.getFluidContained(itemStack);
        if (stack == null || stack.getFluid() == null) {
            return false;
        }
        final String fluidName = stack.getFluid().getName();
        for (final ItemStack slotStack : this.slots) {
            if (slotStack == null || slotStack.isEmpty()) {
                continue;
            }
            final String itemFluidName = fluidNameOf(slotStack);
            if (itemFluidName.equals(fluidName)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void markDirty() {
        if (!this.cellItem.hasTagCompound()) {
            this.cellItem.setTagCompound(new NBTTagCompound());
        }

        this.cellItem.getTagCompound().setTag("filter", this.writeToNBT());
    }

    @Override
    public void setInventorySlotContents(final int index, final ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            super.setInventorySlotContents(index, ItemStack.EMPTY);
            return;
        }
        FluidStack fluidStack;
        if (itemStack.getItem() == ItemEnum.FLUIDITEM.getItem()) {
            fluidStack = ItemFluid.getFluid(itemStack);
            if (fluidStack == null) {
                return;
            }
        } else {
            if (!this.isItemValidForSlot(index, itemStack)) {
                return;
            }
            fluidStack = FluidUtil.getFluidContained(itemStack);
            if (fluidStack == null) {
                super.setInventorySlotContents(index, ItemStack.EMPTY);
                return;
            }
            if (fluidStack.getFluid() == null) {
                super.setInventorySlotContents(index, ItemStack.EMPTY);
                return;
            }
        }
        if (fluidStack == null) {
            super.setInventorySlotContents(index, ItemStack.EMPTY);
            return;
        }
        final ItemStack stack = new ItemStack(ItemEnum.FLUIDITEM.getItem());
        ItemFluid.setFluid(stack, fluidStack);
        super.setInventorySlotContents(index, stack);
    }
}
