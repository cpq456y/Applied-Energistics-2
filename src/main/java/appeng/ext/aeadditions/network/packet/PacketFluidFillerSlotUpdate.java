package appeng.ext.aeadditions.network.packet;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.ext.aeadditions.gui.fluid.GuiFluidFiller;

/** Ported from PacketFluidFillerSlotUpdate.kt. */
public class PacketFluidFillerSlotUpdate extends Packet {

    public final Fluid fluid;

    public PacketFluidFillerSlotUpdate(final Fluid fluid) {
        this.fluid = fluid;
    }

    @Override
    public PacketId getPacketId() {
        return PacketId.FLUID_FILLER_SLOT_UPDATE;
    }

    @Override
    public void writeData(final PacketBufferEC data) {
        data.writeFluid(this.fluid);
    }

    /** Mirrors the Kotlin {@code companion object} nesting. */
    public static class Companion {

        @SideOnly(Side.CLIENT)
        public static class HandlerClient implements IPacketHandlerClient {

            @Override
            public void onPacketData(final PacketBufferEC data, final EntityPlayer player) throws IOException {
                final Fluid fluid = data.readFluid();

                final Gui gui = Minecraft.getMinecraft().currentScreen;

                if (gui instanceof GuiFluidFiller) {
                    ((GuiFluidFiller) gui).updateSelectedFluid(fluid);
                }
            }
        }
    }
}
