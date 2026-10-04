package appeng.ext.aeadditions.gridblock;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.util.AEColor;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.ext.aeadditions.api.gas.IAEGasStack;
import appeng.ext.aeadditions.integration.Integration;
import appeng.ext.aeadditions.tileentity.TileEntityGasInterface;
import appeng.ext.aeadditions.util.StorageChannels;

/** Ported from AEGridBlockGasInterface.kt. */
public class AEGridBlockGasInterface implements IGridBlock {

    private final TileEntityGasInterface host;

    protected IGrid grid = null;
    protected int usedChannels = 0;

    public AEGridBlockGasInterface(final TileEntityGasInterface host) {
        this.host = host;
    }

    public TileEntityGasInterface getHost() {
        return this.host;
    }

    @Override
    public double getIdlePowerUsage() {
        return this.host.getPowerUsage();
    }

    @Override
    public EnumSet<GridFlags> getFlags() {
        return EnumSet.noneOf(GridFlags.class);
    }

    @Override
    public boolean isWorldAccessible() {
        return true;
    }

    @Override
    public DimensionalCoord getLocation() {
        return this.host.getLocation();
    }

    @Override
    public AEColor getGridColor() {
        return AEColor.TRANSPARENT;
    }

    @Override
    public void onGridNotification(final GridNotification notification) {
        // Do nothing
    }

    @Override
    public void setNetworkStatus(final IGrid grid, final int usedChannels) {
        this.grid = grid;
        this.usedChannels = usedChannels;
    }

    @Override
    public EnumSet<EnumFacing> getConnectableSides() {
        return EnumSet.allOf(EnumFacing.class);
    }

    @Override
    public IGridHost getMachine() {
        return this.host;
    }

    @Override
    public void gridChanged() {
        // Do nothing
    }

    @Override
    public ItemStack getMachineRepresentation() {
        final DimensionalCoord location = this.getLocation();
        final net.minecraft.block.state.IBlockState blockState = location.getWorld().getBlockState(location.getPos());

        return new ItemStack(blockState.getBlock(), 1,
                blockState.getBlock().getMetaFromState(blockState));
    }

    public IMEMonitor<IAEGasStack> getGasMonitor() {
        if (!Integration.Mods.MEKANISMGAS.isEnabled()) {
            return null;
        }
        final appeng.api.networking.IGridNode node = this.host.getGridNode(AEPartLocation.INTERNAL);
        if (node == null) {
            return null;
        }
        final IGrid nodeGrid = node.getGrid();
        if (nodeGrid == null) {
            return null;
        }
        final IStorageGrid storageGrid = nodeGrid.getCache(IStorageGrid.class);
        if (storageGrid == null) {
            return null;
        }
        return storageGrid.getInventory(StorageChannels.GAS);
    }
}
