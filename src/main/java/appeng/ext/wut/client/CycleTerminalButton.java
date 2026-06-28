package appeng.ext.wut.client;

import appeng.client.gui.widgets.ITooltip;
import appeng.ext.wut.ItemWirelessUniversalTerminal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Terminal switch button for cycling between terminal modes in WUT GUI
 */
@SideOnly(Side.CLIENT)
public class CycleTerminalButton extends GuiButton implements ITooltip {

    public CycleTerminalButton(int buttonId, int x, int y) {
        super(buttonId, x, y, 16, 16, "");
    }

    private ItemStack getWutStack() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        if (player == null) return ItemStack.EMPTY;
        ItemStack held = player.getHeldItemMainhand();
        if (!held.isEmpty() && held.getItem() instanceof ItemWirelessUniversalTerminal) {
            return held;
        }
        return ItemStack.EMPTY;
    }

    private ItemWirelessUniversalTerminal getWut() {
        ItemStack stack = getWutStack();
        if (!stack.isEmpty()) {
            return (ItemWirelessUniversalTerminal) stack.getItem();
        }
        return null;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }

        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

        if (this.enabled) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
        }

        ItemWirelessUniversalTerminal wut = getWut();
        ItemStack wutStack = getWutStack();
        if (wut != null && !wutStack.isEmpty()) {
            byte currentMode = wut.getCurrentMode(wutStack);
            ItemStack modeIcon = getModeIcon(currentMode);

            if (!modeIcon.isEmpty()) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(this.x, this.y, 0);

                RenderItem itemRender = mc.getRenderItem();
                itemRender.renderItemAndEffectIntoGUI(modeIcon, 0, 0);

                GlStateManager.popMatrix();
            }
        }

        this.mouseDragged(mc, mouseX, mouseY);
    }

    private ItemStack getModeIcon(byte mode) {
        switch (mode) {
            case ItemWirelessUniversalTerminal.MODE_ITEM:
                return appeng.api.AEApi.instance().definitions().items().wirelessTerminal().maybeStack(1).orElse(ItemStack.EMPTY);
            case ItemWirelessUniversalTerminal.MODE_CRAFTING:
                return appeng.api.AEApi.instance().definitions().items().wirelessCraftingTerminal().maybeStack(1).orElse(ItemStack.EMPTY);
            case ItemWirelessUniversalTerminal.MODE_FLUID:
                return appeng.api.AEApi.instance().definitions().items().wirelessFluidTerminal().maybeStack(1).orElse(ItemStack.EMPTY);
            case ItemWirelessUniversalTerminal.MODE_PATTERN:
                return appeng.api.AEApi.instance().definitions().items().wirelessPatternTerminal().maybeStack(1).orElse(ItemStack.EMPTY);
            case ItemWirelessUniversalTerminal.MODE_INTERFACE:
                return appeng.api.AEApi.instance().definitions().items().wirelessInterfaceTerminal().maybeStack(1).orElse(ItemStack.EMPTY);
            default:
                return ItemStack.EMPTY;
        }
    }

    @Override
    public String getMessage() {
        ItemWirelessUniversalTerminal wut = getWut();
        ItemStack wutStack = getWutStack();
        if (wut != null && !wutStack.isEmpty()) {
            return "切换终端\n当前: " + ItemWirelessUniversalTerminal.getModeName(wut.getCurrentMode(wutStack)) + "\n点击切换到下一个终端";
        }
        return "切换终端";
    }

    @Override
    public int xPos() {
        return this.x;
    }

    @Override
    public int yPos() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }
}
