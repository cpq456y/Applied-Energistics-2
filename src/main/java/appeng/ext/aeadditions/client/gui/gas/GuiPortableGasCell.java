package appeng.ext.aeadditions.client.gui.gas;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.ext.aeadditions.container.gas.ContainerPortableGasCell;
import appeng.ext.aeadditions.gui.GuiStorage;

/**
 * Client GUI for the portable gas cell; the name is fixed by {@code GuiBridge#getGui}.
 */
public class GuiPortableGasCell extends GuiStorage {

    public GuiPortableGasCell(final InventoryPlayer ip, final IGuiItemObject host) {
        super(new ContainerPortableGasCell(ip, host), "appeng.ext.aeadditions.item.storage.gas.portable.name");
    }
}
