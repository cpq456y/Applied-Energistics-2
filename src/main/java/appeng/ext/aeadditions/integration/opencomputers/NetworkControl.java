package appeng.ext.aeadditions.integration.opencomputers;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import li.cil.oc.api.internal.Agent;
import li.cil.oc.api.internal.Database;
import li.cil.oc.api.internal.Drone;
import li.cil.oc.api.internal.Robot;
import li.cil.oc.api.internal.MultiTank;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Component;
import li.cil.oc.api.network.Connector;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.WorldCoord;
import appeng.ext.aeadditions.registries.ItemEnum;
import appeng.ext.aeadditions.util.FluidHelper;
import appeng.ext.aeadditions.util.MachineSource;
import appeng.ext.aeadditions.util.StorageChannels;

/** Ported from NetworkControl.kt. */
public abstract class NetworkControl<T extends TileEntity & IActionHost & IGridHost>
        extends AbstractManagedEnvironment {

    public boolean isActive = false;

    public abstract T tile();

    public abstract EnvironmentHost host();

    public Robot getRobot() {
        final EnvironmentHost h = this.host();
        return h instanceof Robot ? (Robot) h : null;
    }

    public Drone getDrone() {
        final EnvironmentHost h = this.host();
        return h instanceof Drone ? (Drone) h : null;
    }

    public Agent getAgent() {
        final EnvironmentHost h = this.host();
        return h instanceof Agent ? (Agent) h : null;
    }

    public ItemStack getComponent() {
        final Robot robot = this.getRobot();
        if (robot != null) {
            return robot.getStackInSlot(robot.componentSlot(this.node().address()));
        } else {
            final Drone drone = this.getDrone();
            if (drone != null) {
                final Iterator<ItemStack> iterator = drone.internalComponents().iterator();

                while (iterator.hasNext()) {
                    final ItemStack item = iterator.next();
                    if (item != null && item.getItem() == ItemEnum.OCUPGRADE.getItem()) {
                        return item;
                    }
                }
            }
        }

        return null;
    }

    public IGridHost getSecurity() {
        if (this.host().world().isRemote) {
            return null;
        }

        final ItemStack component = this.getComponent();
        if (component == null) {
            return null;
        }
        final IGridHost security = (IGridHost) AEApi.instance().registries().locatable()
                .getLocatableBy(this.getAEKey(component));

        if (this.checkRange(component, security)) {
            return security;
        }

        return null;
    }

    public boolean checkRange(final ItemStack itemStack, final IGridHost security) {
        if (itemStack == null || security == null) {
            return false;
        }

        final IGridNode gridNode = security.getGridNode(AEPartLocation.INTERNAL);
        if (gridNode == null) {
            return false;
        }

        final IGrid grid = gridNode.getGrid();
        if (grid == null) {
            return false;
        }

        switch (itemStack.getItemDamage()) {
            case 0: {
                final Class<? extends IGridHost> wirelessAccessPoint = (Class<? extends IGridHost>) (Class<?>) AEApi.instance().definitions().blocks()
                        .wirelessAccessPoint().maybeEntity().get();

                return grid.getMachines(wirelessAccessPoint).iterator().hasNext();
            }
            case 1: {
                final IGridBlock gridBlock = gridNode.getGridBlock();
                if (gridBlock == null) {
                    return false;
                }

                final DimensionalCoord location = gridBlock.getLocation();
                if (location == null) {
                    return false;
                }

                final Class<? extends IGridHost> accessPoints = (Class<? extends IGridHost>) (Class<?>) AEApi.instance().definitions().blocks()
                        .wirelessAccessPoint().maybeEntity().get();

                for (final Object node : grid.getMachines(accessPoints)) {
                    final IWirelessAccessPoint accessPoint = (IWirelessAccessPoint) node;

                    final WorldCoord distance = accessPoint.getLocation().subtract(
                            (int) this.getAgent().xPosition(), (int) this.getAgent().yPosition(), (int) this.getAgent().zPosition());
                    final int squaredDistance = distance.x * distance.x + distance.y * distance.y
                            + distance.z * distance.z;

                    final double range = accessPoint.getRange();

                    if (squaredDistance <= range * range) {
                        return true;
                    }
                }

                return false;
            }
            default: {
                final IGridBlock gridBlock = gridNode.getGridBlock();
                if (gridBlock == null) {
                    return false;
                }

                final DimensionalCoord location = gridBlock.getLocation();
                if (location == null) {
                    return false;
                }

                final Class<? extends IGridHost> accessPoints = (Class<? extends IGridHost>) (Class<?>) AEApi.instance().definitions().blocks()
                        .wirelessAccessPoint().maybeEntity().get();

                for (final Object node : grid.getMachines(accessPoints)) {
                    final IWirelessAccessPoint accessPoint = (IWirelessAccessPoint) node;

                    final WorldCoord distance = accessPoint.getLocation().subtract(
                            (int) this.getAgent().xPosition(), (int) this.getAgent().yPosition(), (int) this.getAgent().zPosition());
                    final int squaredDistance = distance.x * distance.x + distance.y * distance.y
                            + distance.z * distance.z;

                    final double range = accessPoint.getRange() / 2;

                    if (squaredDistance <= range * range) {
                        return true;
                    }
                }

                return false;
            }
        }
    }

    public IGrid getGrid() {
        if (this.host().world().isRemote) {
            return null;
        }

        final IGridHost security = this.getSecurity();
        if (security == null) {
            return null;
        }

        final IGridNode node = security.getGridNode(AEPartLocation.INTERNAL);
        return node == null ? null : node.getGrid();
    }

    public long getAEKey(final ItemStack itemStack) {
        try {
            return Long.parseLong(WirelessHandlerUpgradeAE.INSTANCE.getEncryptionKey(itemStack));
        } catch (final Throwable e) {
            // Do nothing
        }

        return 0L;
    }

    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        final IGrid grid = this.getGrid();
        if (grid == null) {
            return null;
        }

        final IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
        if (storageGrid == null) {
            return null;
        }

        return storageGrid.getInventory(StorageChannels.FLUID);
    }

    public IMEMonitor<IAEItemStack> getItemInventory() {
        final IGrid grid = this.getGrid();
        if (grid == null) {
            return null;
        }

        final IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
        if (storageGrid == null) {
            return null;
        }

        return storageGrid.getInventory(StorageChannels.ITEM);
    }

    @Callback(doc = "function([number:amount]):number -- Transfer selected items to your ae system.")
    public List<Object> sendItems(final Context context, final Arguments args) {
        final int selected = this.getAgent().selectedSlot();
        final IInventory invRobot = this.getAgent().mainInventory();
        if (invRobot.getSizeInventory() <= 0) {
            return Collections.<Object>singletonList(0);
        }

        final ItemStack itemStack = invRobot.getStackInSlot(selected);
        final IMEMonitor<IAEItemStack> inventory = this.getItemInventory();

        if (itemStack == null || itemStack.isEmpty() || inventory == null) {
            return Collections.<Object>singletonList(0);
        }

        final int amount = Math.min(args.optInteger(0, 64), itemStack.getCount());
        final ItemStack itemStack2 = itemStack.copy();

        itemStack2.setCount(amount);

        final IAEItemStack notInjected = inventory.injectItems(StorageChannels.ITEM.createStack(itemStack2),
                Actionable.MODULATE, new MachineSource(this.tile()));

        if (notInjected == null) {
            itemStack.setCount(itemStack.getCount() - amount);

            if (itemStack.getCount() <= 0) {
                invRobot.setInventorySlotContents(selected, ItemStack.EMPTY);
            } else {
                invRobot.setInventorySlotContents(selected, itemStack);
            }

            return Collections.<Object>singletonList(amount);
        } else {
            itemStack.setCount(itemStack.getCount() - amount + (int) notInjected.getStackSize());

            if (itemStack.getCount() <= 0) {
                invRobot.setInventorySlotContents(selected, ItemStack.EMPTY);
            } else {
                invRobot.setInventorySlotContents(selected, itemStack);
            }

            return Collections.<Object>singletonList(itemStack2.getCount() - notInjected.getStackSize());
        }
    }

    @Callback(doc = "function(database:address, entry:number[, number:amount]):number -- Get items from your ae system.")
    public List<Object> requestItems(final Context context, final Arguments args) {
        final String address = args.checkString(0);
        final int entry = args.checkInteger(1);
        final int amount = args.optInteger(2, 64);

        final int selected = this.getAgent().selectedSlot();
        final IInventory invRobot = this.getAgent().mainInventory();

        if (invRobot.getSizeInventory() <= 0) {
            return Collections.<Object>singletonList(0);
        }

        final IMEMonitor<IAEItemStack> inventory = this.getItemInventory();
        if (inventory == null) {
            return Collections.<Object>singletonList(0);
        }

        final Node node = this.node().network().node(address);
        if (node == null) {
            throw new IllegalArgumentException("no such component");
        }

        if (!(node instanceof Component)) {
            throw new IllegalArgumentException("no such component");
        }

        final Environment environment = ((Component) node).host();

        if (!(environment instanceof Database)) {
            throw new IllegalArgumentException("not a database");
        }

        final ItemStack robotSelected = invRobot.getStackInSlot(selected);

        final int inSlot = robotSelected == null ? 0 : robotSelected.getCount();

        final int maxSize = robotSelected == null ? 64 : robotSelected.getMaxStackSize();

        final ItemStack itemStack = ((Database) environment).getStackInSlot(entry - 1);
        if (itemStack == null) {
            return Collections.<Object>singletonList(0);
        }

        itemStack.setCount(Math.min(amount, maxSize - inSlot));

        final ItemStack itemStack2 = itemStack.copy();

        itemStack2.setCount(1);

        final ItemStack selected2;
        if (robotSelected != null) {
            final ItemStack selected3 = robotSelected.copy();
            selected3.setCount(1);
            selected2 = selected3;
        } else {
            selected2 = null;
        }
        // TODO: Null check for selected2
        if (robotSelected != null && !ItemStack.areItemStacksEqual(selected2, itemStack2) && !selected2.isEmpty()) {
            return Collections.<Object>singletonList(0);
        }

        final IAEItemStack extracted = inventory.extractItems(StorageChannels.ITEM.createStack(itemStack),
                Actionable.MODULATE, new MachineSource(this.tile()));
        if (extracted == null) {
            return Collections.<Object>singletonList(0);
        }

        final int stackSize = (int) extracted.getStackSize();

        itemStack.setCount(inSlot + stackSize);

        invRobot.setInventorySlotContents(selected, itemStack);

        return Collections.<Object>singletonList(stackSize);
    }

    @Callback(doc = "function([number:amount]):number -- Transfer selected fluid to your ae system.")
    public List<Object> sendFluids(final Context context, final Arguments args) {
        final int selected = this.getAgent().selectedTank();
        final MultiTank tanks = this.getAgent().tank();

        if (tanks.tankCount() <= 0) {
            return Collections.<Object>singletonList(0);
        }

        final IFluidTank tank = tanks.getFluidTank(selected);

        final IMEMonitor<IAEFluidStack> inventory = this.getFluidInventory();

        final FluidStack fluid = tank.getFluid();

        if (tank == null || inventory == null || fluid == null) {
            return Collections.<Object>singletonList(0);
        }

        final int amount = Math.min(args.optInteger(0, tank.getCapacity()), tank.getFluidAmount());

        final FluidStack fluid2 = fluid.copy();

        fluid2.amount = amount;

        final IAEFluidStack notInjected = inventory.injectItems(StorageChannels.FLUID.createStack(fluid2),
                Actionable.MODULATE, new MachineSource(this.tile()));

        if (notInjected == null) {
            tank.drain(amount, true);

            return Collections.<Object>singletonList(amount);
        }

        tank.drain(amount - (int) notInjected.getStackSize(), true);

        return Collections.<Object>singletonList(amount - notInjected.getStackSize());
    }

    @Callback(doc = "function(database:address, entry:number[, number:amount]):number -- Get fluid from your ae system.")
    public List<Object> requestFluids(final Context context, final Arguments args) {
        final String address = args.checkString(0);
        final int entry = args.checkInteger(1);
        final int amount = args.optInteger(2, 64);

        final int selected = this.getAgent().selectedSlot();
        final MultiTank tanks = this.getAgent().tank();

        if (tanks.tankCount() <= 0) {
            return Collections.<Object>singletonList(0);
        }

        final IFluidTank tank = tanks.getFluidTank(selected);

        final IMEMonitor<IAEFluidStack> inventory = this.getFluidInventory();

        if (tank == null || inventory == null) {
            return Collections.<Object>singletonList(0);
        }

        final Node node = this.node().network().node(address);
        if (node == null) {
            throw new IllegalArgumentException("no such component");
        }

        if (!(node instanceof Component)) {
            throw new IllegalArgumentException("no such component");
        }

        final Environment environment = ((Component) node).host();

        if (!(environment instanceof Database)) {
            throw new IllegalArgumentException("not a database");
        }

        final FluidStack fluid = FluidHelper.getFluidFromContainer(((Database) environment).getStackInSlot(entry - 1));

        fluid.amount = amount;

        final FluidStack fluid2 = fluid.copy();

        fluid2.amount = tank.fill(fluid, false);

        if (fluid2.amount == 0) {
            return Collections.<Object>singletonList(0);
        }

        final IAEFluidStack extracted = inventory.extractItems(StorageChannels.FLUID.createStack(fluid2),
                Actionable.MODULATE, new MachineSource(this.tile()));

        // TODO: Make sure this works
        if (extracted.getFluidStack().amount == 0) {
            return Collections.<Object>singletonList(0);
        }

        return Collections.<Object>singletonList(tank.fill(extracted.getFluidStack(), true));
    }

    @Callback(doc = "function():boolean -- Return true if the card is linked to your ae network.")
    public List<Object> isLinked(final Context context, final Arguments args) {
        return Collections.<Object>singletonList(this.getGrid() != null);
    }

    @Override
    public void update() {
        super.update();

        if ((int) (this.host().world().getTotalWorldTime() % 10) == 0 && this.isActive) {
            final Connector node = (Connector) this.node();
            // Check if enough energy
            if (node.tryChangeBuffer(-this.getEnergy())) {
                this.isActive = false;
            }
        }
    }

    public double getEnergy() {
        final ItemStack c = this.getComponent();
        if (c == null) {
            return .0;
        }

        switch (c.getItemDamage()) {
            case 0:
                return .6;
            case 1:
                return .3;
            default:
                return .05;
        }
    }

    @Override
    public void onMessage(final Message message) {
        super.onMessage(message);

        if (message.name().equals("computer.stopped")) {
            this.isActive = false;
        } else if (message.name().equals("computer.started")) {
            this.isActive = true;
        }
    }
}
