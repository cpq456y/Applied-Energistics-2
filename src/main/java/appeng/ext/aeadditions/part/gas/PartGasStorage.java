package appeng.ext.aeadditions.part.gas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.util.AECableType;
import appeng.ext.aeadditions.api.gas.IAEGasStack;
import appeng.ext.aeadditions.container.IUpgradeable;
import appeng.ext.aeadditions.container.gas.ContainerBusGasStorage;
import appeng.ext.aeadditions.gui.gas.GuiBusGasStorage;
import appeng.ext.aeadditions.gui.widget.fluid.IFluidSlotListener;
import appeng.ext.aeadditions.integration.Integration;
import appeng.ext.aeadditions.inventory.IInventoryListener;
import appeng.ext.aeadditions.inventory.InventoryPlain;
import appeng.ext.aeadditions.inventory.cell.HandlerPartStorageGas;
import appeng.ext.aeadditions.inventory.cell.IHandlerPartBase;
import appeng.ext.aeadditions.models.PartModels;
import appeng.ext.aeadditions.network.packet.other.PacketFluidSlotUpdate;
import appeng.ext.aeadditions.network.packet.part.PacketPartConfig;
import appeng.ext.aeadditions.part.PartECBase;
import appeng.ext.aeadditions.util.NetworkUtil;
import appeng.ext.aeadditions.util.PermissionUtil;
import appeng.ext.aeadditions.util.StorageChannels;

/** Ported from PartGasStorage.kt. */
public class PartGasStorage extends PartECBase
        implements ICellContainer, IInventoryListener, IFluidSlotListener, IUpgradeable {

    private final Map<GasStack, Integer> gasList = new HashMap<>();
    private final Fluid[] filterGases = new Fluid[54];

    private final InventoryPlain upgradeInventory = new InventoryPlain("", 1, 1, this) {
        @Override
        public boolean isItemValidForSlot(final int i, final ItemStack itemstack) {
            return itemstack != null
                    && AEApi.instance().definitions().materials().cardInverter().isSameAs(itemstack);
        }

        @Override
        public void onContentsChanged() {
            PartGasStorage.this.saveData();
        }
    };

    private int _priority = 0;
    public final HandlerPartStorageGas handler = new HandlerPartStorageGas(this);
    protected AccessRestriction access = AccessRestriction.READ_WRITE;
    protected final IStorageChannel<IAEGasStack> channel = StorageChannels.GAS;
    private final boolean isMekanismGasEnabled = Integration.Mods.MEKANISMGAS.isEnabled();

    @Override
    public IInventory getUpgradeInventory() {
        return this.upgradeInventory;
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {
        for (final ItemStack stack : this.upgradeInventory.slots) {
            if (stack == null) {
                continue;
            }

            drops.add(stack);
        }
    }

    @Override
    public ItemStack getItemStack(final PartItemStack type) {
        final ItemStack stack = super.getItemStack(type);

        if (PartItemStack.WRENCH.equals(type)) {
            stack.getTagCompound().removeTag("upgradeInventory");
        }

        return stack;
    }

    @Override
    public void blinkCell(final int p0) {
        // Do nothing
    }

    @Override
    public float getCableConnectionLength(final AECableType p0) {
        return 3.0f;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(2.0, 2.0, 15.0, 14.0, 14.0, 16.0);
        bch.addBox(4.0, 4.0, 14.0, 12.0, 12.0, 15.0);
        bch.addBox(5.0, 5.0, 13.0, 11.0, 11.0, 14.0);
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(final IStorageChannel<?> channel) {
        final List<IMEInventoryHandler> list = new ArrayList<>();
        if (channel == this.channel) {
            list.add(this.handler);
        }

        this.updateNeighbor();

        return list;
    }

    @Override
    public Object getClientGuiElement(final EntityPlayer player) {
        return new GuiBusGasStorage(this, player);
    }

    @Override
    public int getLightLevel() {
        return 0;
    }

    @Override
    public double getPowerUsage() {
        return 0.0;
    }

    @Override
    public int getPriority() {
        return this._priority;
    }

    @Override
    public Object getServerGuiElement(final EntityPlayer player) {
        return new ContainerBusGasStorage(this, player);
    }

    @Override
    public boolean onActivate(final EntityPlayer player, final EnumHand enumHand, final Vec3d pos) {
        return PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, (IPart) this)
                && super.onActivate(player, enumHand, pos);
    }

    @Override
    public void onInventoryChanged() {
        final boolean isCurrentlyInverted = this.handler.isInverted();
        this.handler.setInverted(AEApi.instance().definitions().materials().cardInverter()
                .isSameAs(this.upgradeInventory.getStackInSlot(0)));

        this.saveData();

        if (isCurrentlyInverted != this.handler.isInverted()
                && !FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            this.getGridNode().getGrid().postEvent(new MENetworkCellArrayUpdate());
        }
    }

    @Override
    public void onNeighborChanged(final IBlockAccess var1, final BlockPos var2, final BlockPos var3) {
        this.handler.onNeighborChange();
        final IGridNode node = this.getGridNode();

        if (node != null) {
            final IGrid grid = node.getGrid();
            if (grid != null && this.wasChanged()) {
                grid.postEvent(new MENetworkCellArrayUpdate());
                grid.postEvent(new MENetworkStorageEvent(this.getGridBlock().getFluidMonitor(), StorageChannels.GAS));
                grid.postEvent(new MENetworkCellArrayUpdate());
            }
            this.getHost().markForUpdate();
        }

        super.onNeighborChanged(null, null, null);
    }

    @MENetworkEventSubscribe
    public void powerChange(final MENetworkPowerStatusChange event) {
        final IGridNode node = this.getGridNode();
        if (node != null) {
            final boolean isNowActive = node.isActive();
            if (isNowActive != this.isActive()) {
                this.setActive(isNowActive);
                this.onNeighborChanged();
                this.getHost().markForUpdate();
            }
            node.getGrid().postEvent(new MENetworkStorageEvent(this.getGridBlock().getFluidMonitor(),
                    StorageChannels.FLUID));
            node.getGrid().postEvent(new MENetworkCellArrayUpdate());
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this._priority = data.getInteger("priority");
        for (int i = 0; i <= 8; i++) {
            this.filterGases[i] = FluidRegistry.getFluid(data.getString("FilterFluid#" + i));
        }
        if (data.hasKey("access")) {
            try {
                this.access = AccessRestriction.valueOf(data.getString("access"));
            } catch (final Throwable e) {
                // Do nothing
            }
        }
        this.upgradeInventory.readFromNBT(data.getTagList("upgradeInventory", 10));
        this.onInventoryChanged();
        this.onNeighborChanged();
        this.handler.setPrioritizedFluids(this.filterGases);
        this.handler.setAccessRestriction(this.access);
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return PartModels.STORAGE_BUS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return PartModels.STORAGE_BUS_ON;
        } else {
            return PartModels.STORAGE_BUS_OFF;
        }
    }

    @Override
    public void saveChanges(final ICellInventory<?> cellInventory) {
        this.saveData();
    }

    public void sendInformation(final EntityPlayer player) {
        NetworkUtil.sendToPlayer(new PacketFluidSlotUpdate(Arrays.asList(this.filterGases)), player);
        NetworkUtil.sendToPlayer(
                new PacketPartConfig(this, PacketPartConfig.FLUID_STORAGE_ACCESS, this.access.toString()), player);
    }

    @Override
    public void setFluid(final int index, final Fluid fluid, final EntityPlayer player) {
        this.filterGases[index] = fluid;
        this.handler.setPrioritizedFluids(this.filterGases);
        this.sendInformation(player);
        this.saveData();
        this.updateNeighbor();
    }

    public void updateAccess(final AccessRestriction access) {
        this.access = access;
        this.handler.setAccessRestriction(access);
        this.onNeighborChanged();
    }

    @MENetworkEventSubscribe
    public void updateChannels(final MENetworkChannelsChanged channel) {
        final IGridNode node = this.getGridNode();
        if (node != null) {
            final boolean isNowActive = node.isActive();
            if (isNowActive != this.isActive()) {
                this.setActive(isNowActive);
                this.onNeighborChanged();
                this.getHost().markForUpdate();
            }
        }
        node.getGrid()
                .postEvent(new MENetworkStorageEvent(this.getGridBlock().getFluidMonitor(), StorageChannels.FLUID));
        node.getGrid().postEvent(new MENetworkCellArrayUpdate());
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("priority", this.getPriority());
        for (int i = 0; i < this.filterGases.length; i++) {
            final Fluid fluid = this.filterGases[i];
            if (fluid != null) {
                data.setString("FilterFluid#" + i, fluid.getName());
            } else {
                data.setString("FilterFluid#" + i, "");
            }
        }
        data.setTag("upgradeInventory", this.upgradeInventory.writeToNBT());
        data.setString("access", this.access.name());
    }

    public void updateNeighbor() {
        if (this.isMekanismGasEnabled) {
            this.updateNeighborGases();
        }
    }

    @Optional.Method(modid = "mekanism")
    private void updateNeighborGases() {
        boolean changed = false;
        final Map<Gas, Integer> oldList = new HashMap<>();
        for (final Map.Entry<GasStack, Integer> entry : this.gasList.entrySet()) {
            oldList.put(entry.getKey().getGas(), entry.getValue());
        }
        this.gasList.clear();

        if (this.access == AccessRestriction.READ || this.access == AccessRestriction.READ_WRITE) {
            for (final IAEGasStack stack : ((IHandlerPartBase<IAEGasStack>) this.handler)
                    .getAvailableItems(StorageChannels.GAS.createList())) {
                final GasStack gasStack = (GasStack) stack.getGasStack();

                this.gasList.put(gasStack, gasStack.amount);

                final Integer old = oldList.get(gasStack.getGas());
                if (old == null || old != gasStack.amount) {
                    changed = true;
                }
            }
        }

        if (changed || oldList.size() != this.gasList.size()) {
            this.getGridNode().getGrid().postEvent(new MENetworkCellArrayUpdate());
        }
    }

    @Optional.Method(modid = "mekanism")
    public boolean wasChanged() {
        final Map<GasStack, Integer> fluids = new HashMap<>();

        for (final IAEGasStack stack : ((IHandlerPartBase<IAEGasStack>) this.handler)
                .getAvailableItems(StorageChannels.GAS.createList())) {
            final GasStack gasStack = (GasStack) stack.getGasStack();

            fluids.put(gasStack, gasStack.amount);
        }

        return !fluids.equals(this.gasList);
    }
}
