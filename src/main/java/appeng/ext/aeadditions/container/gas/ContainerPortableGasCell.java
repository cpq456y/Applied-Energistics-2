package appeng.ext.aeadditions.container.gas;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumHand;

import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.ext.aeadditions.api.IPortableGasStorageCell;
import appeng.ext.aeadditions.api.gas.IAEGasStack;
import appeng.ext.aeadditions.integration.mekanism.gas.MEMonitorFluidGasWrapper;
import appeng.ext.aeadditions.util.StorageChannels;
import appeng.me.helpers.MEMonitorHandler;

/**
 * Portable gas cell container in AE2's GuiBridge shape: the bridge looks up a two argument constructor
 * {@code (InventoryPlayer, hostObject)} reflectively. The GUI class name is derived from this name, hence
 * appeng.ext.aeadditions.container.gas.ContainerPortableGasCell maps to
 * appeng.ext.aeadditions.client.gui.gas.GuiPortableGasCell.
 */
public class ContainerPortableGasCell extends ContainerGasStorage {

    public ContainerPortableGasCell(final InventoryPlayer ip, final IGuiItemObject host) {
        super(new MEMonitorFluidGasWrapper(monitorOf(host)), ip.player,
                (IPortableGasStorageCell) host.getItemStack().getItem(), EnumHand.MAIN_HAND);
    }

    private static IMEMonitor<IAEGasStack> monitorOf(final IGuiItemObject host) {
        final ICellInventoryHandler<IAEGasStack> handler = AEApi.instance()
                .registries()
                .cell()
                .getCellInventory(host.getItemStack(), null, StorageChannels.GAS);

        return handler == null ? null : new MEMonitorHandler<>(handler, StorageChannels.GAS);
    }
}
