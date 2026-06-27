/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.fluids.client.gui.widgets;


import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.client.gui.widgets.ITooltip;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.fluids.util.IAEFluidTank;
import appeng.helpers.InventoryAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly(Side.CLIENT)
public class GuiFluidSlotSmall extends GuiCustomSlot implements ITooltip {
    private final IAEFluidTank tank;
    private final int slot;

    public GuiFluidSlotSmall(IAEFluidTank tank, int slot, int id, int x, int y) {
        super(id, x, y);
        this.tank = tank;
        this.slot = slot;
    }

    @Override
    public void drawContent(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        final IAEFluidStack fs = this.getFluidStack();
        if (fs != null && fs.getStackSize() > 0) {
            GlStateManager.disableBlend();
            GlStateManager.disableLighting();

            final Fluid fluid = fs.getFluid();
            mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            float red = (fluid.getColor() >> 16 & 255) / 255.0F;
            float green = (fluid.getColor() >> 8 & 255) / 255.0F;
            float blue = (fluid.getColor() & 255) / 255.0F;
            GlStateManager.color(red, green, blue);

            TextureAtlasSprite sprite = mc.getTextureMapBlocks().getAtlasSprite(fluid.getStill().toString());
            this.drawTexturedModalRect(this.xPos(), this.yPos(), sprite, 16, 16);
        }
    }

    @Override
    public String getMessage() {
        final IAEFluidStack fluid = this.tank.getFluidInSlot(this.slot);
        if (fluid != null && fluid.getStackSize() > 0) {
            String desc = fluid.getFluid().getLocalizedName(fluid.getFluidStack());
            return desc + "\n" + fluid.getStackSize() + "mB";
        }
        return null;
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    public IAEFluidStack getFluidStack() {
        return this.tank.getFluidInSlot(this.slot);
    }

    @Override
    public boolean canClick(final net.minecraft.entity.player.EntityPlayer player) {
        final ItemStack clickStack = player.inventory.getItemStack();
        return clickStack.isEmpty() || clickStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
    }

    @Override
    public void slotClicked(ItemStack clickStack, final int mouseButton) {
        if (getFluidStack() != null && getFluidStack().getStackSize() > 0) {
            NetworkHandler.instance().sendToServer(new PacketInventoryAction(InventoryAction.FILL_ITEM, slot, id));
        } else if (!clickStack.isEmpty()) {
            final FluidStack fluid = FluidUtil.getFluidContained(clickStack);
            if (fluid != null) {
                NetworkHandler.instance().sendToServer(new PacketInventoryAction(InventoryAction.EMPTY_ITEM, slot, id));
            }
        }
    }

    @Override
    public Object getIngredient() {
        return this.getFluidStack() == null ? null : this.getFluidStack().getFluidStack();
    }

}
