package appeng.client.gui.implementations;

import appeng.ext.wut.ItemWirelessUniversalTerminal;
import appeng.ext.wut.WUTPlugin;
import appeng.ext.wut.client.CycleTerminalButton;
import appeng.ext.wut.network.WUTNetworkHandler;
import appeng.helpers.WirelessTerminalGuiObject;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import java.io.IOException;

public class GuiWirelessInterfaceTerminal extends GuiInterfaceTerminal {

    private CycleTerminalButton cycleTerminalBtn;
    private boolean isWUT = false;
    private ItemStack wutStack;

    public GuiWirelessInterfaceTerminal(InventoryPlayer inventoryPlayer, final WirelessTerminalGuiObject te) {
        super(inventoryPlayer, te);
        
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
        
        // 如果是WUT，添加切换按钮（紧跟在接口终端的按钮下方）
        if (isWUT && wutStack != null) {
            int btnX = this.guiLeft - 18;
            // 接口终端有4个按钮，每个间隔20像素：
            // terminalStyleBox(8 + jeiButtonPadding), guiButtonBrokenRecipes(+20), guiButtonHideFull(+20), guiButtonAssemblersOnly(+20)
            // 切换按钮放在第五个位置，需要加上JEI偏移
            int jeiPadding = getJEIPadding();
            int btnY = this.guiTop + 88 + jeiPadding;
            cycleTerminalBtn = new CycleTerminalButton(
                1001, 
                btnX, 
                btnY
            );
            this.buttonList.add(cycleTerminalBtn);
        }
    }
    
    private int getJEIPadding() {
        try {
            java.lang.reflect.Field field = GuiInterfaceTerminal.class.getDeclaredField("jeiButtonPadding");
            field.setAccessible(true);
            return (int) field.get(this);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button == cycleTerminalBtn) {
            // 发送切换终端的网络包
            WUTPlugin.NET_CHANNEL.sendToServer(new WUTNetworkHandler.CycleTerminalMessage((byte) 1));
            return;
        }
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // 动态检查是否是WUT（每次渲染都检查，避免物品切换后状态不同步）
        EntityPlayer player = net.minecraft.client.Minecraft.getMinecraft().player;
        if (player != null) {
            ItemStack held = player.getHeldItemMainhand();
            if (!held.isEmpty() && held.getItem() instanceof ItemWirelessUniversalTerminal) {
                isWUT = true;
                wutStack = held;
            } else {
                isWUT = false;
                wutStack = null;
            }
        }
        
        // 如果是WUT，确保按钮存在
        if (isWUT && wutStack != null) {
            if (cycleTerminalBtn == null) {
                int btnX = this.guiLeft - 18;
                int jeiPadding = getJEIPadding();
                int btnY = this.guiTop + 88 + jeiPadding;
                cycleTerminalBtn = new CycleTerminalButton(1001, btnX, btnY);
            }
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);

        // 父类drawScreen会清空buttonList并渲染，需要在之后重新添加并手动渲染切换按钮
        if (isWUT && cycleTerminalBtn != null) {
            this.buttonList.add(cycleTerminalBtn);
            // 手动渲染按钮
            cycleTerminalBtn.drawButton(mc, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture("guis/wirelessupgrades.png");
        Gui.drawModalRectWithCustomSizedTexture(offsetX + 189, offsetY + 165, 0, 0, 32, 32, 32, 32);
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
    }
}
