package appeng.ext.aeadditions.gui.fluid;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import appeng.ext.aeadditions.Constants;
import appeng.ext.aeadditions.container.fluid.ContainerFluidCrafter;
import appeng.ext.aeadditions.gui.GuiBase;
import appeng.ext.aeadditions.gui.ISlotRenderer;
import appeng.ext.aeadditions.gui.SlotToggleableRenderer;
import appeng.ext.aeadditions.gui.SlotUpgradeRenderer;
import appeng.ext.aeadditions.registries.BlockEnum;
import appeng.ext.aeadditions.tileentity.TileEntityFluidCrafter;

/** Ported from GuiFluidCrafter.kt. */
public class GuiFluidCrafter extends GuiBase<ContainerFluidCrafter> {

    public boolean hasNetworkTool = false;

    public GuiFluidCrafter(final InventoryPlayer player, final TileEntityFluidCrafter tileEntity) {
        super(new ResourceLocation(Constants.MOD_ID, "textures/gui/fluidcrafter.png"),
                new ContainerFluidCrafter(player, tileEntity));

        this.hasNetworkTool = this.inventorySlots.getInventory().size() > 50;
        this.xSize = this.hasNetworkTool ? 245 : 211;
        this.ySize = 182;
    }

    @Override
    protected void drawBackground() {
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 176, 182);
        this.drawTexturedModalRect(this.guiLeft + 179, this.guiTop, 179, 0, 32, 104);

        if (this.hasNetworkTool) {
            this.drawTexturedModalRect(this.guiLeft + 176, this.guiTop + 106, 176, 106, 68, 68);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(BlockEnum.FLUIDCRAFTER.getStatName(), 5, 5, 0x000000);
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
    }

    @Override
    public ISlotRenderer getSlotRenderer(final Slot slot) {
        final net.minecraft.item.ItemStack stack = slot.getStack();

        if (stack != null && !stack.isEmpty()) {
            return null;
        }

        final int slotNumber = slot.slotNumber;

        if (slotNumber > 44) {
            return SlotUpgradeRenderer.INSTANCE;
        }
        if (slotNumber < 9) {
            return SlotToggleableRenderer.INSTANCE;
        }
        return null;
    }

    @Override
    public boolean hasSlotRenders() {
        return true;
    }

    public void onCapacityChanged() {
//        container.onCapacityChanged()
    }
}
