/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.client.gui.implementations;


import appeng.api.config.LockCraftingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiImgLabel;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.GuiToggleButton;
import appeng.container.implementations.ContainerInterface;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.fluids.client.gui.widgets.GuiFluidSlot;
import appeng.fluids.client.gui.widgets.GuiFluidTank;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fluids.Fluid;
import org.lwjgl.input.Mouse;

import java.io.IOException;


public class GuiInterface extends GuiUpgradeable {

    private GuiTabButton priority;
    private GuiImgButton UnlockMode;
    private GuiImgButton BlockMode;
    private GuiToggleButton interfaceMode;
    private GuiImgLabel lockReason;

    public GuiInterface(final InventoryPlayer inventoryPlayer, final IInterfaceHost te) {
        super(new ContainerInterface(inventoryPlayer, te));
        this.ySize = 256;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.addLabel();
        this.updateSlotVisibility();
    }

    private void updateSlotVisibility() {
        this.guiSlots.removeIf(slot -> slot instanceof GuiFluidSlot);

        final ContainerInterface container = (ContainerInterface) this.cvb;
        final IInterfaceHost host = (IInterfaceHost) container.getTarget();
        final DualityInterface duality = host.getInterfaceDuality();

        for (int i = 0; i < DualityInterface.NUMBER_OF_STORAGE_SLOTS; i++) {
            final boolean hasFluidConfig = duality.getFluidConfig().getFluidInSlot(i) != null;

            if (hasFluidConfig) {
                this.guiSlots.add(new GuiFluidSlot(duality.getFluidConfig(), i, 1000 + i, 8 + 18 * i, 35));
                this.guiSlots.add(new GuiFluidSlot(duality.getFluidStorage(), i, 2000 + i, 8 + 18 * i, 53));
            }
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.updateSlotVisibility();
    }

    @Override
    protected void addButtons() {
        this.priority = new GuiTabButton(this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), this.itemRender);
        this.buttonList.add(this.priority);

        this.BlockMode = new GuiImgButton(this.guiLeft - 18, this.guiTop + 8, Settings.BLOCK, YesNo.NO);
        this.buttonList.add(this.BlockMode);

        this.UnlockMode = new GuiImgButton(this.guiLeft - 18, this.guiTop + 26, Settings.UNLOCK, LockCraftingMode.NONE);
        this.buttonList.add(this.UnlockMode);

        this.interfaceMode = new GuiToggleButton(this.guiLeft - 18, this.guiTop + 44, 84, 85, GuiText.InterfaceTerminal.getLocal(), GuiText.InterfaceTerminalHint.getLocal());
        this.buttonList.add(this.interfaceMode);
    }

    protected void addLabel() {
        if (lockReason != null) {
            labelList.remove(this.lockReason);
        }
        this.lockReason = new GuiImgLabel(this.fontRenderer, guiLeft + 40, guiTop + 12, Settings.UNLOCK, LockCraftingMode.NONE);
        this.lockReason.setVisibility(false);
        labelList.add(lockReason);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        if (this.BlockMode != null) {
            this.BlockMode.set(((ContainerInterface) this.cvb).getBlockingMode());
        }

        if (this.UnlockMode != null) {
            this.UnlockMode.set(((ContainerInterface) this.cvb).getUnlockMode());

            if (this.lockReason != null) {
                if (this.UnlockMode.getCurrentValue() == LockCraftingMode.NONE) {
                    this.lockReason.setVisibility(false);
                } else {
                    this.lockReason.setVisibility(true);
                    this.lockReason.set(((ContainerInterface) this.cvb).getCraftingLockedReason());
                }
            }
        }

        if (this.interfaceMode != null) {
            this.interfaceMode.setState(((ContainerInterface) this.cvb).getInterfaceTerminalMode() == YesNo.YES);
        }

        this.fontRenderer.drawString(this.getGuiDisplayName(GuiText.Interface.getLocal()), 8, 6, 4210752);

        this.fontRenderer.drawString(GuiText.Config.getLocal(), 8, 6 + 11 + 7, 4210752);
        this.fontRenderer.drawString(GuiText.StoredItems.getLocal(), 8, 6 + 60 + 7, 4210752);
        this.fontRenderer.drawString(GuiText.Patterns.getLocal(), 8, 6 + 73 + 7, 4210752);

    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) throws IOException {
        if (btn == 1) {
            final net.minecraft.inventory.Slot slot = this.getSlotAtPosition(xCoord, yCoord);
            if (slot instanceof appeng.container.slot.SlotFakeFluidConfig) {
                final appeng.container.slot.SlotFakeFluidConfig fluidConfigSlot = (appeng.container.slot.SlotFakeFluidConfig) slot;
                fluidConfigSlot.handleRightClick(this.mc.player);
                return;
            }
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    protected void handleMouseClick(final net.minecraft.inventory.Slot slot, final int slotIdx, final int mouseButton, final net.minecraft.inventory.ClickType clickType) {
        if (mouseButton == 1 && slot instanceof appeng.container.slot.SlotFakeFluidConfig) {
            return;
        }
        
        if (mouseButton == 0 && slot instanceof appeng.container.slot.SlotFakeFluidConfig) {
            final appeng.container.slot.SlotFakeFluidConfig fluidConfigSlot = (appeng.container.slot.SlotFakeFluidConfig) slot;
            final int slotIndex = fluidConfigSlot.getFluidSlotIndex();
            
            final ContainerInterface container = (ContainerInterface) this.cvb;
            final IInterfaceHost host = (IInterfaceHost) container.getTarget();
            final DualityInterface duality = host.getInterfaceDuality();
            
            final net.minecraft.item.ItemStack mouseItem = this.mc.player.inventory.getItemStack();
            
            if (mouseItem.isEmpty()) {
                ((appeng.tile.inventory.AppEngInternalAEInventory) duality.getConfig()).setStackInSlot(slotIndex, net.minecraft.item.ItemStack.EMPTY);
                duality.getFluidConfig().setFluidInSlot(slotIndex, null);
                NetworkHandler.instance().sendToServer(new appeng.core.sync.packets.PacketItemConfigSlot(
                    java.util.Collections.singletonMap(slotIndex, null)));
                NetworkHandler.instance().sendToServer(new appeng.core.sync.packets.PacketFluidSlot(
                    java.util.Collections.singletonMap(slotIndex, null)));
            } else {
                final net.minecraft.item.ItemStack configItem = mouseItem.copy();
                configItem.setCount(1);
                ((appeng.tile.inventory.AppEngInternalAEInventory) duality.getConfig()).setStackInSlot(slotIndex, configItem);
                duality.getFluidConfig().setFluidInSlot(slotIndex, null);
                final appeng.api.storage.data.IAEItemStack aeItemStack = appeng.util.item.AEItemStack.fromItemStack(configItem);
                NetworkHandler.instance().sendToServer(new appeng.core.sync.packets.PacketItemConfigSlot(
                    java.util.Collections.singletonMap(slotIndex, aeItemStack)));
                NetworkHandler.instance().sendToServer(new appeng.core.sync.packets.PacketFluidSlot(
                    java.util.Collections.singletonMap(slotIndex, null)));
            }
            
            return;
        }
        
        super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
    }

    @Override
    protected void mouseWheelEvent(final int x, final int y, final int wheel) {
        final int relativeX = x - this.guiLeft;
        final int relativeY = y - this.guiTop;
        
        if (relativeY >= 35 && relativeY < 51) {
            for (int i = 0; i < DualityInterface.NUMBER_OF_CONFIG_SLOTS; i++) {
                int slotX = 8 + 18 * i;
                if (relativeX >= slotX && relativeX < slotX + 16) {
                    final ContainerInterface container = (ContainerInterface) this.cvb;
                    final IInterfaceHost host = (IInterfaceHost) container.getTarget();
                    final DualityInterface duality = host.getInterfaceDuality();
                    
                    final IAEFluidStack fluidConfig = duality.getFluidConfig().getFluidInSlot(i);
                    if (fluidConfig != null) {
                        if (appeng.fluids.util.FluidScrollHelper.applyScrollAdjustment(
                                duality.getFluidConfig(), i, wheel, DualityInterface.TANK_CAPACITY)) {
                            final IAEFluidStack updatedFluid = duality.getFluidConfig().getFluidInSlot(i);
                            if (updatedFluid != null) {
                                NetworkHandler.instance().sendToServer(new appeng.core.sync.packets.PacketFluidSlot(
                                    java.util.Collections.singletonMap(i, updatedFluid)));
                            }
                        }
                    } else {
                        final net.minecraft.item.ItemStack itemConfig = duality.getConfig().getStackInSlot(i);
                        if (!itemConfig.isEmpty() && net.minecraft.client.gui.GuiScreen.isShiftKeyDown()) {
                            long currentCount = itemConfig.getCount();
                            long newCount;
                            
                            if (net.minecraft.client.gui.GuiScreen.isCtrlKeyDown()) {
                                if (wheel > 0) {
                                    newCount = currentCount * 2;
                                } else {
                                    newCount = currentCount / 2;
                                }
                            } else {
                                if (wheel > 0) {
                                    newCount = currentCount + 1;
                                } else {
                                    newCount = currentCount - 1;
                                }
                            }
                            
                            newCount = Math.max(1, Math.min(512, newCount));
                            
                            if (newCount != currentCount) {
                                final net.minecraft.item.ItemStack newItem = itemConfig.copy();
                                newItem.setCount((int) newCount);
                                ((appeng.tile.inventory.AppEngInternalAEInventory) duality.getConfig()).setStackInSlot(i, newItem);
                                
                                final appeng.api.storage.data.IAEItemStack aeItemStack = appeng.util.item.AEItemStack.fromItemStack(newItem);
                                NetworkHandler.instance().sendToServer(new appeng.core.sync.packets.PacketItemConfigSlot(
                                    java.util.Collections.singletonMap(i, aeItemStack)));
                            }
                        }
                    }
                    return;
                }
            }
        }
        super.mouseWheelEvent(x, y, wheel);
    }

    @Override
    protected String getBackground() {
        int upgrades = ((ContainerInterface) this.cvb).getPatternUpgrades();
        if (upgrades == 0) {
            return "guis/newinterface.png";
        } else {
            return "guis/newinterface" + upgrades + ".png";
        }
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        super.actionPerformed(btn);

        final boolean backwards = Mouse.isButtonDown(1);

        if (btn == this.priority) {
            NetworkHandler.instance().sendToServer(new PacketSwitchGuis(GuiBridge.GUI_PRIORITY));
        }

        if (btn == this.interfaceMode) {
            NetworkHandler.instance().sendToServer(new PacketConfigButton(Settings.INTERFACE_TERMINAL, backwards));
        }

        if (btn == this.BlockMode) {
            NetworkHandler.instance().sendToServer(new PacketConfigButton(this.BlockMode.getSetting(), backwards));
        }

        if (btn == this.UnlockMode) {
            NetworkHandler.instance.sendToServer(new PacketConfigButton(this.UnlockMode.getSetting(), backwards));
        }
    }

}
