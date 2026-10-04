package appeng.ext.aeadditions.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import appeng.ext.aeadditions.container.slot.ToggleableSlot;

/** Ported from SlotToggleableRenderer.kt (Kotlin {@code object}, kept as a singleton). */
public class SlotToggleableRenderer implements ISlotRenderer {

    public static final SlotToggleableRenderer INSTANCE = new SlotToggleableRenderer();

    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("appliedenergistics2",
            "textures/guis/states.png");

    private SlotToggleableRenderer() {
    }

    @Override
    public void renderBackground(final Slot slot, final GuiBase gui, final int mouseX, final int mouseY) {
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        if (slot instanceof ToggleableSlot && !((ToggleableSlot) slot).isSlotEnabled()) {
            Gui.drawRect(slot.xPos, slot.yPos, slot.xPos + 16, slot.yPos + 16, 0x99000000);
        } else {
            GlStateManager.color(1f, 1f, 1f, 0.5f);
            gui.mc.getTextureManager().bindTexture(TEXTURE_LOCATION);
            gui.drawTexturedModalRect(slot.xPos, slot.yPos, 240, 112, 16, 16);
        }
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
    }
}
