package appeng.ext.aeadditions.container.fluid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.util.AEPartLocation;
import appeng.ext.aeadditions.gui.fluid.GuiFluidInterface;
import appeng.ext.aeadditions.api.IFluidInterface;
import appeng.ext.aeadditions.container.IContainerListener;
import appeng.ext.aeadditions.container.slot.SlotRespective;
import appeng.ext.aeadditions.network.packet.part.PacketFluidInterface;
import appeng.ext.aeadditions.part.fluid.PartFluidInterface;
import appeng.ext.aeadditions.tileentity.TileEntityFluidInterface;
import appeng.ext.aeadditions.util.NetworkUtil;

/** Ported from ContainerFluidInterface.kt. */
public class ContainerFluidInterface extends Container implements IContainerListener {

    private EntityPlayer player;

    public IFluidInterface fluidInterface;

    @SideOnly(Side.CLIENT)
    public GuiFluidInterface gui = null;

    public ContainerFluidInterface(final EntityPlayer player, final IFluidInterface fluidInterface) {
        this.player = player;
        this.fluidInterface = fluidInterface;

        for (int j = 0; j <= 8; j++) {
            this.addSlotToContainer(new SlotRespective(fluidInterface.getPatternInventory(), j, 8 + j * 18, 115));
        }
        this.bindPlayerInventory(player.inventory);
        if (fluidInterface instanceof TileEntityFluidInterface) {
            ((TileEntityFluidInterface) fluidInterface).registerListener(this);
        } else if (fluidInterface instanceof PartFluidInterface) {
            ((PartFluidInterface) fluidInterface).registerListener(this);
        }
        if (fluidInterface instanceof TileEntityFluidInterface) {
            ((TileEntityFluidInterface) fluidInterface).doNextUpdate = true;
        } else if (fluidInterface instanceof PartFluidInterface) {
            ((PartFluidInterface) fluidInterface).doNextUpdate = true;
        }
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }

    public void setPlayer(final EntityPlayer player) {
        this.player = player;
    }

    protected void bindPlayerInventory(final IInventory inventoryPlayer) {
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 8; j++) {
                this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, i * 18 + 149));
            }
        }
        for (int i = 0; i <= 8; i++) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 207)); // 173
        }
    }

    @Override
    public boolean canInteractWith(final EntityPlayer entityplayer) {
        return true;
    }

    private String getFluidName(final AEPartLocation side) {
        final net.minecraftforge.fluids.Fluid fluid = this.fluidInterface.getFilter(side);
        return fluid == null ? "" : fluid.getName();
    }

    @Override
    public void onContainerClosed(final EntityPlayer player) {
        super.onContainerClosed(player);
        if (this.fluidInterface instanceof TileEntityFluidInterface) {
            ((TileEntityFluidInterface) this.fluidInterface).removeListener(this);
        } else if (this.fluidInterface instanceof PartFluidInterface) {
            ((PartFluidInterface) this.fluidInterface).removeListener(this);
        }
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

    @Override
    public void updateContainer() {
        final FluidStack[] fluidStacks = new FluidStack[6];
        final String[] fluidNames = new String[6];
        for (int i = 0; i <= 5; i++) {
            final AEPartLocation location = AEPartLocation.fromOrdinal(i);
            fluidStacks[i] = this.fluidInterface.getFluidTank(location).getFluid();
            fluidNames[i] = this.getFluidName(location);
        }
        NetworkUtil.sendToPlayer(new PacketFluidInterface(fluidStacks, fluidNames), this.player);
    }
}
