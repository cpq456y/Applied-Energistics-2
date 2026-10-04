package appeng.ext.aeadditions.container.fluid;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.EnumHand;

import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.ext.aeadditions.api.IPortableFluidStorageCell;
import appeng.ext.aeadditions.util.StorageChannels;
import appeng.me.helpers.MEMonitorHandler;

/**
 * Portable fluid cell container in AE2's GuiBridge shape: the bridge looks up a two argument constructor
 * {@code (InventoryPlayer, hostObject)} reflectively.
 *
 * <p>The GUI class name is derived from this class name by {@code GuiBridge#getGui}, hence the package and the
 * {@code Container}/{@code Gui} naming: appeng.ext.aeadditions.container.fluid.ContainerPortableFluidCell
 * maps to appeng.ext.aeadditions.client.gui.fluid.GuiPortableFluidCell.
 */
public class ContainerPortableFluidCell extends ContainerFluidStorage {

    public ContainerPortableFluidCell(final InventoryPlayer ip, final IGuiItemObject host) {
        super(monitorOf(host), ip.player, (IPortableFluidStorageCell) host.getItemStack().getItem(),
                EnumHand.MAIN_HAND);
    }

    private static IMEMonitor<IAEFluidStack> monitorOf(final IGuiItemObject host) {
        final ICellInventoryHandler<IAEFluidStack> handler = AEApi.instance()
                .registries()
                .cell()
                .getCellInventory(host.getItemStack(), null, StorageChannels.FLUID);

        return handler == null ? null : new MEMonitorHandler<>(handler, StorageChannels.FLUID);
    }
}
