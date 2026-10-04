package appeng.ext.aeadditions.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import appeng.ext.aeadditions.Constants;
import appeng.ext.aeadditions.container.ContainerHardMEDrive;
import appeng.ext.aeadditions.registries.BlockEnum;
import appeng.ext.aeadditions.tileentity.TileEntityHardMeDrive;

/** Ported from GuiHardMEDrive.kt. */
public class GuiHardMEDrive extends GuiContainer {

    private final InventoryPlayer playerInventory;
    private final TileEntityHardMeDrive tile;

    private final ResourceLocation guiTexture = new ResourceLocation(Constants.MOD_ID,
            "textures/gui/hardmedrive.png");

    public GuiHardMEDrive(final InventoryPlayer inventory, final TileEntityHardMeDrive tile) {
        super(new ContainerHardMEDrive(inventory, tile));
        this.playerInventory = inventory;
        this.tile = tile;
    }

    public InventoryPlayer getPlayerInventory() {
        return this.playerInventory;
    }

    public TileEntityHardMeDrive getTile() {
        return this.tile;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        this.drawDefaultBackground();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.guiTexture);
        final int posX = (this.width - this.xSize) / 2;
        final int posY = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(posX, posY, 0, 0, this.xSize, this.ySize);

        for (final Object s : this.inventorySlots.inventorySlots) {
            this.renderBackground((Slot) s);
        }
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(BlockEnum.BLASTRESISTANTMEDRIVE.getStatName(), 5, 5, 0x000000);
    }

    private void renderBackground(final Slot slot) {
        if ((slot.getStack() == null || slot.getStack().isEmpty()) && slot.slotNumber < 3) {
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
            this.mc.getTextureManager()
                    .bindTexture(new ResourceLocation("appliedenergistics2", "textures/guis/states.png"));
            this.drawTexturedModalRect(this.guiLeft + slot.xPos, this.guiTop + slot.yPos, 240, 0, 16, 16);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
        }
    }
}
