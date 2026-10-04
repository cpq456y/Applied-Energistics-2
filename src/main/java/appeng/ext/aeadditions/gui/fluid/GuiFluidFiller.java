package appeng.ext.aeadditions.gui.fluid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import appeng.ext.aeadditions.Constants;
import appeng.ext.aeadditions.container.fluid.ContainerFluidFiller;
import appeng.ext.aeadditions.gui.GuiBase;
import appeng.ext.aeadditions.gui.ISlotRenderer;
import appeng.ext.aeadditions.gui.SlotUpgradeRenderer;
import appeng.ext.aeadditions.gui.widget.AbstractWidget;
import appeng.ext.aeadditions.gui.widget.fluid.WidgetFluidSlot;
import appeng.ext.aeadditions.network.packet.PacketFluidFillerSyncClient;
import appeng.ext.aeadditions.tileentity.TileEntityFluidFiller;
import appeng.ext.aeadditions.util.FluidHelper;
import appeng.ext.aeadditions.util.NetworkUtil;

/** Ported from GuiFluidFiller.kt. */
public class GuiFluidFiller extends GuiBase<ContainerFluidFiller> {

    public boolean hasNetworkTool = false;

    private final TileEntityFluidFiller tileEntity;

    public GuiFluidFiller(final EntityPlayer player, final TileEntityFluidFiller tileEntity) {
        super(new ResourceLocation(Constants.MOD_ID, "textures/gui/fluidfiller.png"),
                new ContainerFluidFiller(player.inventory, tileEntity));
        this.tileEntity = tileEntity;

        this.hasNetworkTool = this.inventorySlots.getInventory().size() > 39;
        this.xSize = this.hasNetworkTool ? 245 : 211;
        this.ySize = 166;
        this.widgetManager.add(new WidgetFluidSlot(this.widgetManager, tileEntity, 79, 34));
        ((ContainerFluidFiller) this.inventorySlots).gui = this;
        NetworkUtil.sendToServer(new PacketFluidFillerSyncClient(tileEntity));
    }

    public TileEntityFluidFiller getTileEntity() {
        return this.tileEntity;
    }

    @Override
    protected void drawBackground() {
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 175, 166);
        this.drawTexturedModalRect(this.guiLeft + 179, this.guiTop, 179, 0, 32, 68);

        if (this.hasNetworkTool) {
            this.drawTexturedModalRect(this.guiLeft + 177, this.guiTop + 70, 177, 70, 68, 68);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(
                I18n.translateToLocal("tile.appeng.ext.aeadditions.block.fluidfiller.name").replace("ME ", ""), 5, 5,
                0x000000);
    }

    @Override
    public ISlotRenderer getSlotRenderer(final Slot slot) {
        final ItemStack stack = slot.getStack();

        if (stack != null && !stack.isEmpty()) {
            return null;
        }

        final int slotNumber = slot.slotNumber;

        if (slotNumber > 38) {
            return SlotUpgradeRenderer.INSTANCE;
        }
        return null;
    }

    @Override
    public boolean hasSlotRenders() {
        return true;
    }

    public void updateSelectedFluid(final Fluid fluid) {
        for (final AbstractWidget widget : this.widgetManager.getWidgets()) {
            if (widget instanceof WidgetFluidSlot) {
                if (fluid == null) {
                    ((WidgetFluidSlot) widget).setFluid(null);
                    return;
                }

                ((WidgetFluidSlot) widget).setFluid(fluid);
            }
        }
    }

    public boolean shiftClick(final ItemStack itemStack) {
        final FluidStack containerFluid = FluidHelper.getFluidFromContainer(itemStack);
        final Fluid fluid = containerFluid == null ? null : containerFluid.getFluid();
        for (final AbstractWidget widget : this.widgetManager.getWidgets()) {
            if (widget instanceof WidgetFluidSlot) {
                final WidgetFluidSlot fluidSlot = (WidgetFluidSlot) widget;
                if (fluid != null && (fluidSlot.getFluid() == null || fluidSlot.getFluid() == fluid)
                        && fluidSlot.isVisable()) {
                    fluidSlot.handleContainer(itemStack);
                    return true;
                }
            }
        }
        return false;
    }
}
