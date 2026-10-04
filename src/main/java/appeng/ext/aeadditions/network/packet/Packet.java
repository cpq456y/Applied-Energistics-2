package appeng.ext.aeadditions.network.packet;

import java.io.IOException;

import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import appeng.ext.aeadditions.network.PacketHandler;
import appeng.ext.aeadditions.util.Log;
import io.netty.buffer.Unpooled;

public abstract class Packet implements IPacket {
	@Override
	public final FMLProxyPacket getPacket() {
		PacketBufferEC data = new PacketBufferEC(Unpooled.buffer());

		try {
			PacketId id = getPacketId();
			data.writeByte(id.ordinal());
			writeData(data);
		} catch (IOException e) {
			Log.error("Failed to write packet.", e);
		}

		return new FMLProxyPacket(data, PacketHandler.CHANNEL_ID);
	}

	protected abstract void writeData(PacketBufferEC data) throws IOException;
}
