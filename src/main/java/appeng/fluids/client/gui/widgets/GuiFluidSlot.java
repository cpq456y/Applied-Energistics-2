package appeng.fluids.client.gui.widgets;


import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.container.slot.IJEITargetSlot;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketFluidSlot;
import appeng.fluids.util.AEFluidStack;
import appeng.fluids.util.IAEFluidTank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import java.util.Collections;


public class GuiFluidSlot extends GuiCustomSlot implements IJEITargetSlot {
    private final IAEFluidTank fluids;
    private final int slot;
    private final boolean isConfigSlot;

    public GuiFluidSlot(final IAEFluidTank fluids, final int slot, final int id, final int x, final int y) {
        this(fluids, slot, id, x, y, id >= 1000 && id < 2000);
    }

    public GuiFluidSlot(final IAEFluidTank fluids, final int slot, final int id, final int x, final int y, final boolean isConfigSlot) {
        super(id, x, y);
        this.fluids = fluids;
        this.slot = slot;
        this.isConfigSlot = isConfigSlot;
    }

    @Override
    public void drawContent(final Minecraft mc, final int mouseX, final int mouseY, final float partialTicks) {
        final IAEFluidStack fs = this.getFluidStack();
        if (fs != null) {
            GlStateManager.disableLighting();
            GlStateManager.disableBlend();
            final Fluid fluid = fs.getFluid();
            mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            final TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(fluid.getStill().toString());

            // Set color for dynamic fluids
            // Convert int color to RGB
            final float red = (fluid.getColor() >> 16 & 255) / 255.0F;
            final float green = (fluid.getColor() >> 8 & 255) / 255.0F;
            final float blue = (fluid.getColor() & 255) / 255.0F;
            GlStateManager.color(red, green, blue);

            this.drawTexturedModalRect(this.xPos(), this.yPos(), sprite, this.getWidth(), this.getHeight());
            
            // Draw fluid amount text
            GlStateManager.disableLighting();
            GlStateManager.disableBlend();
            final long amountInB = fs.getStackSize() / Fluid.BUCKET_VOLUME;
            final String text = amountInB + "B";
            final float scaleFactor = 0.5f;
            final float inverseScaleFactor = 1.0f / scaleFactor;
            final int offset = -1;
            
            // Disable unicode rendering to match item stack size rendering
            final boolean unicodeFlag = mc.fontRenderer.getUnicodeFlag();
            mc.fontRenderer.setUnicodeFlag(false);
            
            GlStateManager.pushMatrix();
            GlStateManager.scale(scaleFactor, scaleFactor, scaleFactor);
            final int X = (int) (((float) this.xPos() + offset + 16.0f - mc.fontRenderer.getStringWidth(text) * scaleFactor) * inverseScaleFactor);
            final int Y = (int) (((float) this.yPos() + offset + 16.0f - 7.0f * scaleFactor) * inverseScaleFactor);
            mc.fontRenderer.drawStringWithShadow(text, X, Y, 16777215);
            GlStateManager.popMatrix();
            
            // Restore unicode flag
            mc.fontRenderer.setUnicodeFlag(unicodeFlag);
        }
    }

    @Override
    public boolean canClick(final EntityPlayer player) {
        // Storage slots (non-config) are read-only
        if (!this.isConfigSlot) {
            return false;
        }
        final ItemStack mouseStack = player.inventory.getItemStack();
        return mouseStack.isEmpty() || mouseStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    }

    @Override
    public void slotClicked(final ItemStack clickStack, int mouseButton) {
        // Storage slots (non-config) are read-only
        if (!this.isConfigSlot) {
            return;
        }
        if (clickStack.isEmpty() || mouseButton == 1) {
            this.setFluidStack(null);
        } else if (mouseButton == 0) {
            final FluidStack fluid = FluidUtil.getFluidContained(clickStack);
            if (fluid != null) {
                this.setFluidStack(AEFluidStack.fromFluidStack(fluid));
            }
        }
    }

    @Override
    public String getMessage() {
        final IAEFluidStack fluid = this.getFluidStack();
        if (fluid != null) {
            return fluid.getFluidStack().getLocalizedName();
        }
        return null;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    public IAEFluidStack getFluidStack() {
        return this.fluids.getFluidInSlot(this.slot);
    }

    public void setFluidStack(final IAEFluidStack stack) {
        this.fluids.setFluidInSlot(this.slot, stack);
        NetworkHandler.instance().sendToServer(new PacketFluidSlot(Collections.singletonMap(this.getId(), this.getFluidStack())));
    }

    @Override
    public boolean needAccept() {
        return this.getFluidStack() == null;
    }

    @Override
    public Object getIngredient() {
        return this.getFluidStack() == null ? null : this.getFluidStack().getFluidStack();
    }

    public boolean isSlotEnabled() {
        return true;
    }

}
