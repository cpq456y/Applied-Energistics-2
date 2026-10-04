package appeng.ext.aeadditions.gui.widget;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.Fluid;

import appeng.ext.aeadditions.integration.mekanism.gas.MekanismGas;

/** Ported from WidgetGasTank.kt. */
public class WidgetGasTank extends AbstractWidget {

    private GasTank tank;
    private int index = 0;

    public WidgetGasTank(final WidgetManager widgetManager, final GasTank tank, final int xPos, final int yPos) {
        this(widgetManager, tank, xPos, yPos, 0);
    }

    public WidgetGasTank(final WidgetManager widgetManager, final GasTank tank, final int xPos, final int yPos,
            final int index) {
        super(widgetManager, xPos, yPos);
        this.tank = tank;
        this.index = index;

        this.width = 16;
        this.height = 68;
    }

    public GasTank getTank() {
        return this.tank;
    }

    public void setTank(final GasTank tank) {
        this.tank = tank;
    }

    public int getIndex() {
        return this.index;
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    @Override
    public void draw(final int mouseX, final int mouseY) {
        if (this.tank == null) {
            return;
        }
        final net.minecraft.client.renderer.texture.TextureManager textureManager = this.manager.mc
                .getTextureManager();
        GlStateManager.disableLighting();

        GlStateManager.color(1.0f, 1.0f, 1.0f);

        final GasStack gas = this.tank.getGas();
        if (gas != null && gas.amount > 0) {
            textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            final Fluid fluid = MekanismGas.fluidGas.get(gas.getGas());
            final ResourceLocation still = fluid == null ? null : fluid.getStill();
            final net.minecraft.client.renderer.texture.TextureAtlasSprite sprite = Minecraft.getMinecraft()
                    .getTextureMapBlocks().getAtlasSprite(String.valueOf(still));

            final int color = gas.getGas().getTint();

            GlStateManager.color(this.getRed(color), this.getGreen(color), this.getBlue(color));

            final int scaledHeight = (int) (this.height
                    * ((float) this.tank.getStored() / (float) this.tank.getMaxGas()));

            final int iconHeightRemainder = scaledHeight % 16;

            if (iconHeightRemainder > 0) {
                this.manager.gui.drawTexturedModalRect(this.xPos, this.yPos + this.height - iconHeightRemainder,
                        sprite, 16, iconHeightRemainder);
            }
            for (int i = 0; i < scaledHeight / 16; i++) {
                this.manager.gui.drawTexturedModalRect(this.xPos,
                        this.yPos + this.height - iconHeightRemainder - (i + 1) * 16, sprite, 16, 16);
            }
        }
        GlStateManager.enableLighting();
    }

    @Override
    public List<String> getToolTip(final int mouseX, final int mouseY) {
        final List<String> description = new ArrayList<>();
        if (this.tank == null || this.tank.getGas() == null) {
            description.add(I18n.translateToLocal("appeng.ext.aeadditions.tooltip.empty1"));
            description.add("Side: " + EnumFacing.byIndex(this.index).name());
        } else {
            if (this.tank.getGas().amount > 0 && this.tank.getGas() != null) {
                final String amountToText = this.tank.getGas().amount + "mB";
                description.add(this.tank.getGas().getGas().getLocalizedName());
                description.add(amountToText);
                description.add("Side: " + EnumFacing.byIndex(this.index).name());
            }
        }
        return description;
    }

    private float getRed(final int color) {
        return (color >> 16 & 0xFF) / 255.0f;
    }

    private float getGreen(final int color) {
        return (color >> 8 & 0xFF) / 255.0f;
    }

    private float getBlue(final int color) {
        return (color & 0xFF) / 255.0f;
    }
}
