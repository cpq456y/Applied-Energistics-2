package appeng.ext.aeadditions.tileentity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasHandler;
import mekanism.common.util.GasUtils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.ext.aeadditions.api.IECTileEntity;
import appeng.ext.aeadditions.api.gas.IAEGasStack;
import appeng.ext.aeadditions.container.IContainerListener;
import appeng.ext.aeadditions.container.gas.ContainerGasInterface;
import appeng.ext.aeadditions.gridblock.AEGridBlockGasInterface;
import appeng.ext.aeadditions.gui.gas.GuiGasInterface;
import appeng.ext.aeadditions.gui.widget.fluid.IFluidSlotListener;
import appeng.ext.aeadditions.integration.mekanism.gas.Capabilities;
import appeng.ext.aeadditions.integration.waila.IWailaTile;
import appeng.ext.aeadditions.network.IGuiProvider;
import appeng.ext.aeadditions.network.packet.PacketGasInterface;
import appeng.ext.aeadditions.util.GasUtil;
import appeng.ext.aeadditions.util.MachineSource;
import appeng.ext.aeadditions.util.NetworkUtil;
import appeng.ext.aeadditions.util.StorageChannels;

/** Ported from TileEntityGasInterface.kt. */
public class TileEntityGasInterface extends TileBase implements IECTileEntity, IActionHost, IGridTickable,
        IGuiProvider, IGasHandler, IFluidSlotListener, IWailaTile {

    public List<IContainerListener> listeners = new ArrayList<>();

    private IGridNode node = null;
    private boolean isFirstGetGridNode = true;
    private boolean doUpdate = false;
    private AEGridBlockGasInterface gridBlock = new AEGridBlockGasInterface(this);
    private final List<GasTank> gasTanks = new ArrayList<>();
    private final List<Gas> gasConfig = new ArrayList<>();
    public boolean useSides = false;

    public List<GasTank> getGasTanks() {
        return this.gasTanks;
    }

    public List<Gas> getGasConfig() {
        return this.gasConfig;
    }

    private Gas previousGas = null;
    private Integer previousGasIndex = null;

    public TileEntityGasInterface() {
        for (int i = 0; i < 6; i++) {
            this.gasTanks.add(i, new GasTank(10000));
        }
        for (int i = 0; i < 6; i++) {
            this.gasConfig.add(i, null);
        }
    }

    @Override
    public IGridNode getGridNode(final AEPartLocation dir) {
        if (FMLCommonHandler.instance().getSide().isClient()
                && (this.getWorld() == null || this.getWorld().isRemote)) {
            return null;
        }
        if (this.isFirstGetGridNode) {
            this.isFirstGetGridNode = false;
            this.getActionableNode().updateState();
        }
        return this.node;
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation p0) {
        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public int receiveGas(final EnumFacing direction, final GasStack gas, final boolean doTransfer) {
        if (gas == null) {
            return 0;
        }

        final int originalAmount = gas.amount;

        int gasAmount = gas.amount;

        final Actionable action = doTransfer ? Actionable.MODULATE : Actionable.SIMULATE;

        final List<Integer> gasTankIndexesForGas = new ArrayList<>();

        for (int index = 0; index < this.gasConfig.size(); index++) {
            final Gas item = this.gasConfig.get(index);
            if (gas.getGas().equals(item)) {
                gasTankIndexesForGas.add(index);
            }
        }

        for (final Integer gasTankIndex : gasTankIndexesForGas) {
            final GasTank tank = this.gasTanks.get(gasTankIndex);

            if (tank.canReceive(gas.getGas())) {
                final int amtReceived = tank.receive(gas, doTransfer);

                gasAmount -= amtReceived;

                if (gasAmount <= 0) {
                    break;
                }
            }
        }

        if (gasAmount <= 0) {
            return gas.amount;
        }

        gas.amount = gasAmount;

        final IAEGasStack aeGasStack = StorageChannels.GAS.createStack(gas);
        if (aeGasStack == null) {
            return 0;
        }

        final IAEGasStack notInjected = this.injectGas(aeGasStack, action);
        if (notInjected == null) {
            return originalAmount;
        }

        boolean didFillGasTanks = false;

        for (final GasTank tank : this.gasTanks) {
            final int amountInserted = tank.receive((GasStack) notInjected.getGasStack(), doTransfer);

            if (amountInserted != 0) {
                didFillGasTanks = true;
            }

            notInjected.setStackSize(notInjected.getStackSize() - amountInserted);

            if (notInjected.getStackSize() <= 0) {
                break;
            }
        }

        if (didFillGasTanks) {
            this.doUpdate = true;
        }

        return (int) (originalAmount - notInjected.getStackSize());
    }

    @Override
    public GasStack drawGas(final EnumFacing direction, final int amount, final boolean doDrain) {
        if (direction == null) {
            return null;
        }

        final int sideIndex = direction.ordinal();

        final GasTank gasTank = this.gasTanks.get(sideIndex);

        final Gas gas = gasTank.getGasType();

        if (!gasTank.canDraw(gas)) {
            return null;
        }

        final Actionable action = doDrain ? Actionable.MODULATE : Actionable.SIMULATE;

        final GasStack stack = gasTank.draw(amount, doDrain);

        // Attempt to fill from network

        final int gasTankDiff = gasTank.getNeeded();

        if (gasTankDiff > 0) {
            final IAEGasStack gasStack = StorageChannels.GAS.createStack(gas);
            if (gasStack != null) {
                gasStack.setStackSize(gasTankDiff);
                final IAEGasStack extracted = this.extractGas(gasStack, action);

                if (extracted != null) {
                    gasTank.receive((GasStack) extracted.getGasStack(), doDrain);

                    if ((int) extracted.getStackSize() != gasTankDiff) {
                        this.doUpdate = true;
                    }
                }
            }
        }

        this.previousGas = null;
        this.previousGasIndex = null;

        return stack;
    }

    private GasTank getGasTankForGas(final Gas gas) {
        Integer tankIndex = null;
        for (int index = 0; index < this.gasConfig.size(); index++) {
            final Gas gasInTank = this.gasConfig.get(index);
            if (gasInTank == gas) {
                tankIndex = index;
            }
        }

        if (tankIndex == null) {
            return null;
        }

        return this.gasTanks.get(tankIndex);
    }

    private GasTank getFirstTankWithGas() {
        for (final GasTank gasTank : this.gasTanks) {
            if (gasTank.getGasType() != null && gasTank.getStored() != 0) {
                return gasTank;
            }
        }

        return null;
    }

    @Override
    public boolean canReceiveGas(final EnumFacing direction, final Gas gas) {
        boolean canReceive = false;

        for (final GasTank it : this.gasTanks) {
            if (it.getGasType() == null) {
                canReceive = true;

                continue;
            }

            if (it.getGasType().equals(gas) && it.canReceive(gas)) {
                canReceive = true;
            }
        }

        return canReceive;
    }

    @Override
    public boolean canDrawGas(final EnumFacing direction, final Gas gas) {
        if (direction == null) {
            return false;
        }

        final int sideIndex = direction.ordinal();

        final GasTank tank = this.gasTanks.get(sideIndex);

        return tank.getGasType() != null;
    }

    private int getAmountOfGasInConfig() {
        int count = 0;
        for (final Gas gas : this.gasConfig) {
            if (gas != null) {
                count++;
            }
        }
        return count;
    }

    @Override
    public double getPowerUsage() {
        return 1.0;
    }

    @Override
    public IGridNode getActionableNode() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return this.node;
        }
        if (this.node == null) {
            this.node = AEApi.instance().grid().createGridNode(this.gridBlock);
        }
        return this.node;
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode p0) {
        return new TickingRequest(1, 20, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (this.doUpdate) {
            this.forceUpdate();
        }

        // 1. Attempt to fill config slots
        // 2. Empty gas tanks that aren't attached to a config or if the gas doesn't match.
        // Example: Config is hydrogen and there is oxygen, we want to empty the oxygen

        final appeng.ext.aeadditions.api.gas.IGasStorageChannel storageChannel = StorageChannels.GAS;

        {
            boolean didFillTank = false;
            for (int index = 0; index < this.gasConfig.size(); index++) {
                final Gas gas = this.gasConfig.get(index);
                if (gas == null) {
                    continue;
                }

                final GasTank tank = this.gasTanks.get(index);

                if (!tank.canReceive(gas)) {
                    continue;
                }

                final IAEGasStack stack = storageChannel.createStack(gas);
                if (stack == null) {
                    continue;
                }

                int amountToExtract = 1500;

                if (tank.getNeeded() < amountToExtract) {
                    amountToExtract = tank.getNeeded();
                }

                stack.setStackSize(amountToExtract);

                final IAEGasStack extracted = this.extractGas(stack, Actionable.MODULATE);

                if (extracted != null) {
                    final int amt = tank.receive((GasStack) extracted.getGasStack(), true);

                    if (amt > 0) {
                        didFillTank = true;
                    }
                }
            }

            if (didFillTank) {
                this.doUpdate = true;
            }
        }

        {
            boolean didEmptyTank = false;

            for (int index = 0; index < this.gasTanks.size(); index++) {
                final GasTank gasTank = this.gasTanks.get(index);
                if (gasTank.getStored() == 0) {
                    continue;
                }

                final Gas config = this.gasConfig.get(index);

                if (config == null || !gasTank.getGasType().equals(config)) {
                    final IAEGasStack stack = storageChannel.createStack(gasTank.stored);
                    if (stack == null) {
                        continue;
                    }

                    final IAEGasStack notInjected = this.injectGas(stack, Actionable.MODULATE);

                    int toDraw = (int) stack.getStackSize();

                    if (notInjected != null) {
                        toDraw -= (int) notInjected.getStackSize();
                    }

                    if (toDraw > 0) {
                        final GasStack amt = gasTank.draw(toDraw, true);

                        if (amt != null && amt.amount > 0) {
                            didEmptyTank = true;
                        }
                    }
                }

                if (gasTank.getStored() > 0) {
                    final GasStack stack = gasTank.stored;

                    final EnumSet<EnumFacing> set = EnumSet.of(EnumFacing.byIndex(index));

                    set.addAll(this.getSidesWithGasConduits());

                    final GasStack drained = gasTank.draw(GasUtils.emit(stack, this, set), true);

                    if (drained != null && drained.amount > 0) {
                        didEmptyTank = true;
                    }
                }
            }

            if (didEmptyTank) {
                this.doUpdate = true;
            }
        }

        return TickRateModulation.FASTER;
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        super.writeToNBT(compound);
        for (int i = 0; i < 6; i++) {
            final GasTank tank = this.gasTanks.get(i);
            final Gas config = this.gasConfig.get(i);

            compound.setTag("tank#" + i, tank.write(new NBTTagCompound()));

            if (config != null) {
                compound.setString("gasConfig#" + i, config.getName());
            }
        }

        return compound;
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        super.readFromNBT(compound);

        for (int i = 0; i < 6; i++) {
            if (compound.hasKey("tank#" + i)) {
                final GasTank newTank = GasTank.readFromNBT(compound.getCompoundTag("tank#" + i));
                this.gasTanks.get(i).setGas(newTank.getGas());
            }

            if (compound.hasKey("gasConfig#" + i)) {
                this.gasConfig.set(i, GasRegistry.getGas(compound.getString("gasConfig#" + i)));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public GuiContainer getClientGuiElement(final EntityPlayer player, final Object... args) {
        return new GuiGasInterface(player, this);
    }

    @Override
    public Container getServerGuiElement(final EntityPlayer player, final Object... args) {
        return new ContainerGasInterface(player, this);
    }

    @Override
    public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
        return capability == Capabilities.GAS_HANDLER_CAPABILITY;
    }

    @Override
    public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
        if (capability == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.cast(this);
        }

        return super.getCapability(capability, facing);
    }

    protected IAEGasStack extractGas(final IAEGasStack toExtract, final Actionable action) {
        if (this.gridBlock == null) {
            return null;
        }
        final IMEMonitor<IAEGasStack> monitor = this.gridBlock.getGasMonitor();
        if (monitor == null) {
            return null;
        }
        try {
            return AEApi.instance().storage().poweredExtraction(this.getEnergy(), monitor, toExtract,
                    new MachineSource(this), action);
        } catch (final GridAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected IAEGasStack injectGas(final IAEGasStack toInject, final Actionable action) {
        if (this.gridBlock == null) {
            return toInject;
        }
        final IMEMonitor<IAEGasStack> monitor = this.gridBlock.getGasMonitor();
        if (monitor == null) {
            return toInject;
        }
        try {
            return AEApi.instance().storage().poweredInsert(this.getEnergy(), monitor, toInject,
                    new MachineSource(this), action);
        } catch (final GridAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public IEnergyGrid getEnergy() throws GridAccessException {
        final IGrid grid = this.node == null ? null : this.node.getGrid();
        if (grid == null) {
            throw new GridAccessException();
        }
        final IEnergyGrid cache = grid.getCache(IEnergyGrid.class);
        if (cache == null) {
            throw new GridAccessException();
        }
        return cache;
    }

    public void registerListener(final IContainerListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(final IContainerListener listener) {
        this.listeners.remove(listener);
    }

    private void forceUpdate() {
        this.updateBlock();
        for (final IContainerListener listener : this.listeners) {
            listener.updateContainer();
        }
        this.saveData();
        this.doUpdate = false;
    }

    @Override
    public void setFluid(final int index, final Fluid fluid, final EntityPlayer player) {
        this.gasConfig.set(index, GasUtil.getGas(fluid));

        this.doUpdate = true;
    }

    public void syncClientGui(final EntityPlayer player) {
        NetworkUtil.sendToPlayer(new PacketGasInterface(this.gasTanks, this.gasConfig), player);
    }

    @Override
    public List<String> getWailaBody(final List<String> list, final NBTTagCompound tag, final EnumFacing side) {
        if (side == null) {
            return list;
        }

        final int sideIndex = side.ordinal();

        list.add(I18n.translateToLocal("appeng.ext.aeadditions.tooltip.direction." + sideIndex));

        final GasTank tank = GasTank.readFromNBT(tag.getCompoundTag("tank#" + sideIndex));

        if (tag.hasKey("gasConfig#" + sideIndex)) {
            final Gas gas = GasRegistry.getGas(tag.getString("gasConfig#" + sideIndex));

            list.add("Selected Gas: " + gas.getLocalizedName());
            list.add("Amount: " + tank.getStored() + " / " + tank.getMaxGas());
        } else {
            if (tank.getGasType() != null) {
                list.add("Filled Gas: " + tank.getGasType().getLocalizedName());
                list.add("Amount: " + tank.getStored() + " / " + tank.getMaxGas());
            } else {
                list.add("Tank Empty");
            }
        }

        return list;
    }

    @Override
    public NBTTagCompound getWailaTag(final NBTTagCompound tag) {
        for (int i = 0; i < 6; i++) {
            final GasTank tank = this.gasTanks.get(i);
            final Gas gas = this.gasConfig.get(i);

            tag.setTag("tank#" + i, tank.write(new NBTTagCompound()));

            if (gas != null) {
                tag.setString("gasConfig#" + i, gas.getName());
            }
        }

        return tag;
    }

    public EnumSet<EnumFacing> getSidesWithGasConduits() {
        // EnderIO / Gas Conduits interop is intentionally deferred: those mavens are not
        // reachable from this build, and the gas channel itself does not depend on them.
        return EnumSet.noneOf(EnumFacing.class);
    }
}
