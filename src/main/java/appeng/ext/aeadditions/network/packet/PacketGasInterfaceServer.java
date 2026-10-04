package appeng.ext.aeadditions.network.packet;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

import appeng.ext.aeadditions.tileentity.TileEntityGasInterface;

/** Ported from PacketGasInterfaceServer.kt. */
public class PacketGasInterfaceServer extends Packet {

    public final TileEntityGasInterface tileEntity;

    public PacketGasInterfaceServer(final TileEntityGasInterface tileEntity) {
        this.tileEntity = tileEntity;
    }

    @Override
    public PacketId getPacketId() {
        return PacketId.GAS_INTERFACE_SERVER;
    }

    @Override
    public void writeData(final PacketBufferEC data) {
        data.writeTile(this.tileEntity);
    }

    public static class HandlerServer implements IPacketHandlerServer {

        @Override
        public void onPacketData(final PacketBufferEC data, final EntityPlayerMP player) throws IOException {
            final TileEntity tile = data.readTile(player.world);

            if (tile instanceof TileEntityGasInterface) {
                ((TileEntityGasInterface) tile).syncClientGui(player);
            }
        }
    }
}
