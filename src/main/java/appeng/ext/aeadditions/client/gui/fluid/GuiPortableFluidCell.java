package appeng.ext.aeadditions.client.gui.fluid;

import net.minecraft.entity.player.InventoryPlayer;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.ext.aeadditions.container.fluid.ContainerPortableFluidCell;
import appeng.ext.aeadditions.gui.GuiStorage;

/**
 * Client GUI for the portable fluid cell. The name is fixed by {@code GuiBridge#getGui}: the container
 * appeng.ext.aeadditions.container.fluid.ContainerPortableFluidCell maps to
 * appeng.ext.aeadditions.client.gui.fluid.GuiPortableFluidCell.
 */
public class GuiPortableFluidCell extends GuiStorage {

    public GuiPortableFluidCell(final InventoryPlayer ip, final IGuiItemObject host) {
        super(new ContainerPortableFluidCell(ip, host), "appeng.ext.aeadditions.item.storage.fluid.portable.name");
    }
}
