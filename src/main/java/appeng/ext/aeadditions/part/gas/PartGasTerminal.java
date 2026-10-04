package appeng.ext.aeadditions.part.gas;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.MutablePair;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AECableType;
import appeng.ext.aeadditions.container.ContainerTerminal;
import appeng.ext.aeadditions.container.StorageType;
import appeng.ext.aeadditions.gui.GuiTerminal;
import appeng.ext.aeadditions.gridblock.ECBaseGridBlock;
import appeng.ext.aeadditions.integration.Integration;
import appeng.ext.aeadditions.inventory.IInventoryListener;
import appeng.ext.aeadditions.inventory.InventoryPlain;
import appeng.ext.aeadditions.models.PartModels;
import appeng.ext.aeadditions.network.packet.part.PacketTerminalSelectFluidClient;
import appeng.ext.aeadditions.part.PartECBase;
import appeng.ext.aeadditions.util.GasUtil;
import appeng.ext.aeadditions.util.MachineSource;
import appeng.ext.aeadditions.util.NetworkUtil;
import appeng.ext.aeadditions.util.PermissionUtil;
import appeng.ext.aeadditions.util.StorageChannels;

/** Ported from PartGasTerminal.kt. */
public class PartGasTerminal extends PartECBase implements IGridTickable, IInventoryListener {

    private final List<Object> containers = new ArrayList<>();

    private final InventoryPlain inventory = new InventoryPlain("appeng.ext.aeadditions.part.gas.terminal", 2, 64,
            this) {
        @Override
        public boolean isItemValidForSlot(final int i, final ItemStack itemstack) {
            return PartGasTerminal.this.isItemValidForInputSlot(i, itemstack);
        }

        @Override
        public void onContentsChanged() {
            PartGasTerminal.this.saveData();
        }
    };

    private Fluid currentFluid = null;

    protected MachineSource machineSource = new MachineSource(this);

    public InventoryPlain getInventory() {
        return this.inventory;
    }

    public Fluid getCurrentFluid() {
        return this.currentFluid;
    }

    public void setCurrentFluid(final Fluid value) {
        this.currentFluid = value;
        this.sendCurrentFluid();
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {
        for (final ItemStack stack : this.inventory.slots) {
            if (stack == null) {
                continue;
            }
            drops.add(stack);
        }
    }

    public boolean fillSecondSlot(final ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }
        final ItemStack secondSlot = this.inventory.getStackInSlot(1);
        if (secondSlot == null || secondSlot.isEmpty()) {
            this.inventory.setInventorySlotContents(1, itemStack);
            return true;
        } else {
            if (!secondSlot.isItemEqual(itemStack) || !ItemStack.areItemStackTagsEqual(itemStack, secondSlot)) {
                return false;
            }
            this.inventory.incrStackSize(1, itemStack.getCount());
            return true;
        }
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0);
        bch.addBox(4.0, 4.0, 13.0, 12.0, 12.0, 14.0);
        bch.addBox(5.0, 5.0, 12.0, 11.0, 11.0, 13.0);
    }

    @Override
    public double getPowerUsage() {
        return 0.5;
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(1, 20, false, false);
    }

    @Override
    public boolean onActivate(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (this.isActive() && (PermissionUtil.hasPermission(player, SecurityPermissions.INJECT, (IPart) this)
                || PermissionUtil.hasPermission(player, SecurityPermissions.EXTRACT, (IPart) this))) {
            return super.onActivate(player, hand, pos);
        }
        return false;
    }

    @Override
    public void onInventoryChanged() {
        this.saveData();
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.inventory.readFromNBT(data.getTagList("inventory", 10));
    }

    public void removeContainer(final ContainerTerminal containerTerminalFluid) {
        if (containerTerminalFluid != null) {
            this.containers.remove(containerTerminalFluid);
        }
    }

    public void addContainer(final ContainerTerminal containerTerminalFluid) {
        if (containerTerminalFluid != null) {
            this.containers.add(containerTerminalFluid);
            this.sendCurrentFluid();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IPartModel getStaticModels() {
        if (this.isActive()) {
            return PartModels.TERMINAL_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return PartModels.TERMINAL_ON;
        } else {
            return PartModels.TERMINAL_OFF;
        }
    }

    public void sendCurrentFluid() {
        for (final Object containerFluidTerminal : this.containers) {
            this.sendCurrentFluid(containerFluidTerminal);
        }
    }

    public void sendCurrentFluid(final Object container) {
        if (container instanceof ContainerTerminal) {
            NetworkUtil.sendToPlayer(new PacketTerminalSelectFluidClient(this.currentFluid),
                    ((ContainerTerminal) container).getPlayer());
        }
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        this.doWork();

        return TickRateModulation.FASTER;
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("inventory", this.inventory.writeToNBT());
    }

    @Override
    public float getCableConnectionLength(final AECableType aeCableType) {
        return 1.0f;
    }

    public void decreaseFirstSlot() {
        final ItemStack slot = this.inventory.getStackInSlot(0);
        slot.setCount(slot.getCount() - 1);
        if (slot.getCount() <= 0) {
            this.inventory.setInventorySlotContents(0, ItemStack.EMPTY);
        }
    }

    @Override
    public ItemStack getItemStack(final PartItemStack type) {
        final ItemStack stack = super.getItemStack(type);
        if (type == PartItemStack.WRENCH) {
            stack.getTagCompound().removeTag("inventory");
        }
        return stack;
    }

    public boolean isMekanismLoaded() {
        return Integration.Mods.MEKANISMGAS.isEnabled();
    }

    public boolean doNextFill = false;

    public boolean isItemValidForInputSlot(final int i, final ItemStack itemStack) {
        return GasUtil.isGasContainer(itemStack);
    }

    public void doWork() {
        if (this.isMekanismLoaded()) {
            this.doWorkGas();
        }
    }

    @Optional.Method(modid = "mekanism")
    public void doWorkGas() {
        final ItemStack secondSlot = this.inventory.getStackInSlot(1);

        if (secondSlot != null && !secondSlot.isEmpty() && secondSlot.getCount() >= secondSlot.getMaxStackSize()) {
            return;
        }

        ItemStack container = this.inventory.getStackInSlot(0);

        if (container == null || container.isEmpty()) {
            this.doNextFill = false;
        }

        if (!GasUtil.isGasContainer(container) || container == null) {
            return;
        }

        container = container.copy();
        container.setCount(1);

        final ECBaseGridBlock gridBlock = this.getGridBlock();
        if (gridBlock == null) {
            return;
        }
        final appeng.api.storage.IMEMonitor<appeng.ext.aeadditions.api.gas.IAEGasStack> monitor = gridBlock
                .getGasMonitor();
        if (monitor == null) {
            return;
        }

        final GasStack gasStack = GasUtil.getGasFromContainer(container);

        if (GasUtil.isEmpty(container) || (gasStack.amount < GasUtil.getCapacity(container)
                && GasUtil.getFluidStack(gasStack).getFluid() == this.currentFluid && this.doNextFill)) {
            if (this.currentFluid == null) {
                return;
            }

            final int capacity = GasUtil.getCapacity(container);

            final appeng.ext.aeadditions.api.gas.IAEGasStack result = monitor.extractItems(
                    StorageChannels.GAS.createStack(new GasStack(GasUtil.getGas(this.currentFluid), capacity)),
                    Actionable.SIMULATE, this.machineSource);

            int proposedAmount = 0;
            if (result == null) {
                proposedAmount = 0;
            } else if (gasStack == null) {
                proposedAmount = Math.min(capacity, (int) result.getStackSize());
            } else {
                proposedAmount = Math.min(capacity - gasStack.amount, (int) result.getStackSize());
            }

            final MutablePair<Integer, ItemStack> filledContainer = GasUtil.fillStack(container,
                    GasUtil.getGasStack(new FluidStack(this.currentFluid, proposedAmount)));

            final ItemStack filledContainerItemStack = filledContainer.getRight();

            final GasStack gasStack2 = GasUtil.getGasFromContainer(filledContainerItemStack);
            if (gasStack2 == null) {
                this.doNextFill = false;
            } else if (container.getCount() == 1
                    && gasStack2.amount < GasUtil.getCapacity(filledContainerItemStack)) {
                this.inventory.setInventorySlotContents(0, filledContainerItemStack);
                monitor.extractItems(
                        StorageChannels.GAS.createStack(
                                new GasStack(GasUtil.getGas(this.currentFluid), filledContainer.getLeft())),
                        Actionable.MODULATE, this.machineSource);
                this.doNextFill = true;
            } else if (this.fillSecondSlot(filledContainerItemStack)) {
                monitor.extractItems(
                        StorageChannels.GAS.createStack(
                                new GasStack(GasUtil.getGas(this.currentFluid), filledContainer.getLeft())),
                        Actionable.MODULATE, this.machineSource);
                this.decreaseFirstSlot();
                this.doNextFill = false;
            }
        } else {
            final GasStack containerGas = GasUtil.getGasFromContainer(container);

            final MutablePair<Integer, ItemStack> drainedContainer = GasUtil.drainStack(container.copy(), containerGas);
            final GasStack drainedGasStack = containerGas.copy();

            drainedGasStack.amount = drainedContainer.getLeft();

            final appeng.ext.aeadditions.api.gas.IAEGasStack notInjected = monitor.injectItems(
                    StorageChannels.GAS.createStack(drainedGasStack), Actionable.SIMULATE, this.machineSource);

            if (notInjected != null) {
                return;
            }

            final ItemStack emptyContainer = drainedContainer.getRight();

            if (emptyContainer != null && !emptyContainer.isEmpty()
                    && GasUtil.getGasFromContainer(emptyContainer) != null && emptyContainer.getCount() == 1) {
                monitor.injectItems(StorageChannels.GAS.createStack(drainedGasStack), Actionable.MODULATE,
                        this.machineSource);
                this.inventory.setInventorySlotContents(0, emptyContainer);
            } else if (emptyContainer == null || emptyContainer.isEmpty()
                    || this.fillSecondSlot(emptyContainer)) {
                monitor.injectItems(StorageChannels.GAS.createStack(containerGas), Actionable.MODULATE,
                        this.machineSource);
                this.decreaseFirstSlot();
            }
        }
    }

    @Override
    public Object getServerGuiElement(final EntityPlayer player) {
        if (this.isMekanismLoaded()) {
            return new ContainerTerminal(this, player, StorageType.GAS);
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(final EntityPlayer player) {
        if (this.isMekanismLoaded()) {
            return new GuiTerminal(this, player, StorageType.GAS);
        }

        return null;
    }
}
