package appeng.ext.aeadditions.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.util.DimensionalCoord;
import appeng.ext.aeadditions.container.slot.SlotNetworkTool;
import appeng.ext.aeadditions.container.slot.SlotRespective;

/** Ported from ContainerUpgradeable.kt. */
public abstract class ContainerUpgradeable extends ContainerBase {

    public void bindUpgradeInventory(final IUpgradeable upgradeable) {
        final IInventory upgradeInventory = upgradeable.getUpgradeInventory();
        for (int i = 0; i < upgradeInventory.getSizeInventory(); i++) {
            this.addSlotToContainer(new SlotRespective(upgradeInventory, i, 187, i * 18 + 8));
        }
    }

    public void bindNetworkToolInventory(final InventoryPlayer inv, final IUpgradeable upgradeable) {
        this.bindNetworkToolInventory(inv, upgradeable, 187, 102);
    }

    public void bindNetworkToolInventory(final InventoryPlayer inv, final IUpgradeable upgradeable, final int x,
            final int y) {
        if (inv == null) {
            return;
        }

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            final ItemStack stack = inv.getStackInSlot(i);
            if (stack != null && AEApi.instance().definitions().items().networkTool().isSameAs(stack)) {
                this.lockPlayerInventorySlot(i);
                final DimensionalCoord coord = upgradeable.getLocation();
                final IGuiItem guiItem = (IGuiItem) stack.getItem();
                final INetworkTool networkTool = (INetworkTool) guiItem.getGuiObject(stack, coord.getWorld(),
                        coord.getPos());
                for (int j = 0; j <= 2; j++) {
                    for (int k = 0; k <= 2; k++) {
                        this.addSlotToContainer(
                                new SlotNetworkTool(networkTool, k + j * 3, x + k * 18, j * 18 + y));
                    }
                }
                return;
            }
        }
    }
}
