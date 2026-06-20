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


import appeng.api.implementations.guiobjects.IPortableCell;
import appeng.container.implementations.ContainerWirelessTerm;
import appeng.ext.wut.ItemWirelessUniversalTerminal;
import appeng.ext.wut.WUTPlugin;
import appeng.ext.wut.client.CycleTerminalButton;
import appeng.ext.wut.network.WUTNetworkHandler;
import appeng.helpers.WirelessTerminalGuiObject;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import java.io.IOException;


public class GuiWirelessTerm extends GuiMEMonitorable {

    private CycleTerminalButton cycleTerminalBtn;
    private boolean isWUT = false;
    private ItemStack wutStack;

    public GuiWirelessTerm(final InventoryPlayer inventoryPlayer, final WirelessTerminalGuiObject te) {
        super(inventoryPlayer, te, new ContainerWirelessTerm(inventoryPlayer, te));
        
        // 检查是否是WUT
        ItemStack heldItem = inventoryPlayer.getCurrentItem();
        if (heldItem.getItem() instanceof ItemWirelessUniversalTerminal) {
            isWUT = true;
            wutStack = heldItem;
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        
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
    protected void actionPerformed(GuiButton button) {
        if (button == cycleTerminalBtn) {
            // 发送切换终端的网络包
            WUTPlugin.NET_CHANNEL.sendToServer(new WUTNetworkHandler.CycleTerminalMessage((byte) 1));
            return;
        }
        super.actionPerformed(button);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture("guis/wirelessupgrades.png");
        Gui.drawModalRectWithCustomSizedTexture(offsetX + 198, offsetY + 127, 0, 0, 32, 32, 32, 32);
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
    }
}
