package appeng.ext.aeadditions.network.packet;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.ext.aeadditions.gui.fluid.GuiFluidCrafter;
import appeng.ext.aeadditions.tileentity.TileEntityFluidCrafter;
import appeng.ext.aeadditions.util.GuiUtil;

/** Ported from PacketCrafterCapacity.kt. */
public class PacketCrafterCapacity extends Packet {

    public final TileEntity tileEntity;
    public final int capacity;

    public PacketCrafterCapacity(final TileEntity tileEntity, final int capacity) {
        this.tileEntity = tileEntity;
        this.capacity = capacity;
    }

    @Override
    public PacketId getPacketId() {
        return PacketId.FLUID_CRAFTER_CAPACITY;
    }

    @Override
    public void writeData(final PacketBufferEC data) {
        data.writeTile(this.tileEntity);
        data.writeInt(this.capacity);
    }

    /** Mirrors the Kotlin {@code companion object} nesting, so {@code Companion.HandlerClient} still resolves. */
    public static class Companion {

        @SideOnly(Side.CLIENT)
        public static class HandlerClient implements IPacketHandlerClient {

            @Override
            public void onPacketData(final PacketBufferEC data, final EntityPlayer player) throws IOException {
                final TileEntityFluidCrafter tile = (TileEntityFluidCrafter) data.readTile(player.world);
                final int capacity = data.readInt();

                tile.setCapacity(capacity);

                final GuiFluidCrafter gui = GuiUtil.getGui(GuiFluidCrafter.class);
                if (gui == null) {
                    return;
                }

                gui.onCapacityChanged();
            }
        }
    }
}
