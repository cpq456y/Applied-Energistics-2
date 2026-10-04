package appeng.ext.aeadditions.network.packet.part;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayerMP;

import appeng.ext.aeadditions.container.ContainerStorage;
import appeng.ext.aeadditions.network.packet.IPacketHandlerServer;
import appeng.ext.aeadditions.network.packet.Packet;
import appeng.ext.aeadditions.network.packet.PacketBufferEC;
import appeng.ext.aeadditions.network.packet.PacketId;
import appeng.ext.aeadditions.util.GuiUtil;

public class PacketStorageOpenContainer extends Packet {

	@Override
	public void writeData(PacketBufferEC data) throws IOException {
	}

	@Override
	public PacketId getPacketId() {
		return PacketId.STORAGE_OPEN_CONTAINER;
	}

	public static class Handler implements IPacketHandlerServer {
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayerMP player) throws IOException {
			ContainerStorage containerStorage = GuiUtil.getContainer(player, ContainerStorage.class);
			if (containerStorage == null) {
				return;
			}

			containerStorage.forceFluidUpdate();
			containerStorage.doWork();
		}
	}
}
