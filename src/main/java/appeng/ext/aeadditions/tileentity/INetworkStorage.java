package appeng.ext.aeadditions.tileentity;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;
import appeng.ext.aeadditions.util.StorageChannels;

/** Ported from INetworkStorage.kt. */
public interface INetworkStorage {

    default IStorageGrid getStorageGrid(final AEPartLocation side) {
        if (!(this instanceof IGridHost)) {
            return null;
        }

        final IGridHost host = (IGridHost) this;

        if (host.getGridNode(side) == null) {
            return null;
        }

        final IGrid grid = host.getGridNode(side).getGrid();

        if (grid == null) {
            return null;
        }

        return grid.getCache(IStorageGrid.class);
    }

    default IMEMonitor<IAEFluidStack> getFluidInventory(final AEPartLocation side) {
        final IStorageGrid storageGrid = this.getStorageGrid(side);
        if (storageGrid == null) {
            return null;
        }

        return storageGrid.getInventory(StorageChannels.FLUID);
    }

    default IMEMonitor<IAEItemStack> getItemInventory(final AEPartLocation side) {
        final IStorageGrid storageGrid = this.getStorageGrid(side);
        if (storageGrid == null) {
            return null;
        }

        return storageGrid.getInventory(StorageChannels.ITEM);
    }
}
