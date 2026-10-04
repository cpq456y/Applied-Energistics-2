package appeng.ext.aeadditions.gui.gas;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import appeng.ext.aeadditions.Constants;
import appeng.ext.aeadditions.container.gas.ContainerGasInterface;
import appeng.ext.aeadditions.gui.GuiBase;
import appeng.ext.aeadditions.gui.widget.WidgetGasTank;
import appeng.ext.aeadditions.gui.widget.fluid.WidgetFluidSlot;
import appeng.ext.aeadditions.network.packet.PacketGasInterfaceServer;
import appeng.ext.aeadditions.tileentity.TileEntityGasInterface;
import appeng.ext.aeadditions.util.NetworkUtil;

/** Ported from GuiGasInterface.kt. */
public class GuiGasInterface extends GuiBase<ContainerGasInterface> {

    private final EntityPlayer player;
    private final TileEntityGasInterface gasInterface;

    private final WidgetFluidSlot[] filter = new WidgetFluidSlot[6];

    public GuiGasInterface(final EntityPlayer player, final TileEntityGasInterface gasInterface) {
        super(new ResourceLocation(Constants.MOD_ID, "textures/gui/gasinterface.png"),
                new ContainerGasInterface(player, gasInterface));
        this.player = player;
        this.gasInterface = gasInterface;

        ((ContainerGasInterface) this.inventorySlots).gui = this;
        this.ySize = 231;
        for (int i = 0; i < 6; i++) {
            final int xPos = i * 18 + 35;

            this.widgetManager
                    .add(new WidgetGasTank(this.widgetManager, gasInterface.getGasTanks().get(i), xPos, 53, i));

            final WidgetFluidSlot slot = new WidgetFluidSlot(this.widgetManager, gasInterface, i, xPos - 1, 35);

            this.widgetManager.add(slot);

            this.filter[i] = slot;
        }

        NetworkUtil.sendToServer(new PacketGasInterfaceServer(gasInterface));
    }

    public WidgetFluidSlot[] getFilter() {
        return this.filter;
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }

    public TileEntityGasInterface getGasInterface() {
        return this.gasInterface;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(
                I18n.translateToLocal("tile.appeng.ext.aeadditions.block.gas_interface.name").replace("ME ", ""), 5, 5,
                0x000000);
    }
}
