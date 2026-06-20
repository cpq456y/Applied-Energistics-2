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


import appeng.api.config.ActionItems;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.me.ItemRepo;
import appeng.container.implementations.ContainerWirelessCraftingTerminal;
import appeng.container.slot.SlotCraftingMatrix;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.ext.wut.ItemWirelessUniversalTerminal;
import appeng.ext.wut.WUTPlugin;
import appeng.ext.wut.client.CycleTerminalButton;
import appeng.ext.wut.network.WUTNetworkHandler;
import appeng.helpers.InventoryAction;
import appeng.helpers.WirelessTerminalGuiObject;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.io.IOException;


public class GuiWirelessCraftingTerminal extends GuiMEMonitorable {

    private GuiImgButton clearBtn;
    private CycleTerminalButton cycleTerminalBtn;
    private boolean isWUT = false;
    private ItemStack wutStack;

    public GuiWirelessCraftingTerminal(final InventoryPlayer inventoryPlayer, final WirelessTerminalGuiObject te) {
        super(inventoryPlayer, te, new ContainerWirelessCraftingTerminal(inventoryPlayer, te));
        this.setReservedSpace(73);
        
        // 检查是否是WUT
        ItemStack heldItem = inventoryPlayer.getCurrentItem();
        if (heldItem.getItem() instanceof ItemWirelessUniversalTerminal) {
            isWUT = true;
            wutStack = heldItem;
        }
    }

    @Override
    protected void actionPerformed(final GuiButton btn) {
        if (btn == cycleTerminalBtn) {
            // 发送切换终端的网络包
            WUTPlugin.NET_CHANNEL.sendToServer(new WUTNetworkHandler.CycleTerminalMessage((byte) 1));
            return;
        }
        
        super.actionPerformed(btn);

        if (this.clearBtn == btn) {
            Slot s = null;
            final Container c = this.inventorySlots;
            for (final Object j : c.inventorySlots) {
                if (j instanceof SlotCraftingMatrix) {
                    s = (Slot) j;
                }
            }

            if (s != null) {
                final PacketInventoryAction p = new PacketInventoryAction(InventoryAction.MOVE_REGION, s.slotNumber, 0);
                NetworkHandler.instance().sendToServer(p);
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.add(this.clearBtn = new GuiImgButton(this.guiLeft + 92, this.guiTop + this.ySize - 156, Settings.ACTIONS, ActionItems.STASH));
        this.clearBtn.setHalfSize(true);
        
        // 如果是WUT，添加切换按钮
        if (isWUT && wutStack != null) {
            int btnX = this.guiLeft - 18;
            int btnY = this.guiTop + 8 + jeiOffset + 100; // 在左侧按钮区域下方
            cycleTerminalBtn = new CycleTerminalButton(
                1001, 
                btnX, 
                btnY
            );
            this.buttonList.add(cycleTerminalBtn);
        }
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
        this.fontRenderer.drawString(GuiText.CraftingTerminal.getLocal(), 8, this.ySize - 96 + 1 - this.getReservedSpace(), 4210752);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture("guis/wirelessupgrades.png");
        Gui.drawModalRectWithCustomSizedTexture(offsetX + 198, offsetY + 127, 0, 0, 32, 32, 32, 32);
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    protected String getBackground() {
        return "guis/crafting.png";
    }
}
