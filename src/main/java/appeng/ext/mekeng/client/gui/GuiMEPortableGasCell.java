package appeng.ext.mekeng.client.gui;

import appeng.ext.mekeng.container.ContainerMEPortableGasCell;
import appeng.ext.mekeng.common.me.storage.IPortableGasCell;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiMEPortableGasCell extends GuiGasTerminal {

    public GuiMEPortableGasCell(final InventoryPlayer inventoryPlayer, final IPortableGasCell te) {
        super(inventoryPlayer, te, new ContainerMEPortableGasCell(inventoryPlayer, te));
    }

    @Override
    protected String getName() {
        return "gui.mekeng.gas_portable_cell";
    }

}
