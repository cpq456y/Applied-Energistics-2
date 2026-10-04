package appeng.ext.aeadditions.network.packet;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;

import appeng.ext.aeadditions.tileentity.TileEntityFluidFiller;

/** Ported from PacketFluidFillerSyncClient.kt. */
public class PacketFluidFillerSyncClient extends Packet {

    public final TileEntityFluidFiller tileEntity;

    public PacketFluidFillerSyncClient(final TileEntityFluidFiller tileEntity) {
        this.tileEntity = tileEntity;
    }

    @Override
    public PacketId getPacketId() {
        return PacketId.FLUID_FILLER_SYNC_CLIENT;
    }

    @Override
    public void writeData(final PacketBufferEC data) {
        data.writeTile(this.tileEntity);
    }

    public static class HandlerServer implements IPacketHandlerServer {

        @Override
        public void onPacketData(final PacketBufferEC data, final EntityPlayerMP player) throws IOException {
            final net.minecraft.tileentity.TileEntity tile = data.readTile(player.world);

            if (tile instanceof TileEntityFluidFiller) {
                ((TileEntityFluidFiller) tile).syncClientGui(player);
            }
        }
    }
}
