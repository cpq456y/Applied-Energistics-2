package appeng.ext.aeadditions.item.storage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import appeng.ext.aeadditions.api.gas.IAEGasStack;
import appeng.ext.aeadditions.inventory.ECGasFilterInventory;
import appeng.ext.aeadditions.util.StorageChannels;
import appeng.items.materials.MaterialType;
import appeng.items.storage.AbstractStorageCell;
import appeng.util.InventoryAdaptor;

/**
 * Gas storage cell, mirroring {@code BasicFluidStorageCell} so that the gas tiers behave exactly like the item
 * and fluid ones (same upgrade slots, same idle drain progression, same bytes-per-type progression).
 *
 * <p>No Mekanism type is referenced here: {@link IAEGasStack} only exposes {@link Object} accessors, and
 * {@link StorageChannels#GAS} is null when the integration is disabled.
 */
public final class BasicGasStorageCell extends AbstractStorageCell<IAEGasStack> {

    private final int perType;
    private final double idleDrain;

    public BasicGasStorageCell(final MaterialType whichCell, final int kilobytes) {
        super(whichCell, kilobytes);
        switch (whichCell) {
            case GAS_CELL1K_PART:
                this.idleDrain = 0.5;
                this.perType = 8;
                break;
            case GAS_CELL4K_PART:
                this.idleDrain = 1.0;
                this.perType = 32;
                break;
            case GAS_CELL16K_PART:
                this.idleDrain = 1.5;
                this.perType = 128;
                break;
            case GAS_CELL64K_PART:
                this.idleDrain = 2.0;
                this.perType = 512;
                break;
            case GAS_CELL256K_PART:
                this.idleDrain = 2.5;
                this.perType = 2048;
                break;
            case GAS_CELL1024K_PART:
                this.idleDrain = 3.0;
                this.perType = 8192;
                break;
            default:
                this.idleDrain = 3.5;
                this.perType = 32768;
        }
    }

    @Override
    public int getBytesPerType(final ItemStack cellItem) {
        return this.perType;
    }

    @Override
    public double getIdleDrain() {
        return this.idleDrain;
    }

    @Override
    public IStorageChannel<IAEGasStack> getChannel() {
        return StorageChannels.GAS;
    }

    @Override
    public int getTotalTypes(final ItemStack cellItem) {
        return 5;
    }

    @Override
    public IItemHandler getConfigInventory(final ItemStack is) {
        return new InvWrapper(new ECGasFilterInventory("configFluidCell", 63, is));
    }

    @Override
    protected void dropEmptyStorageCellCase(final InventoryAdaptor ia, final EntityPlayer player) {
        AEApi.instance().definitions().materials().emptyStorageCell().maybeStack(1).ifPresent(is ->
        {
            final ItemStack extraA = ia.addItems(is);
            if (!extraA.isEmpty()) {
                player.dropItem(extraA, false);
            }
        });
    }
}
