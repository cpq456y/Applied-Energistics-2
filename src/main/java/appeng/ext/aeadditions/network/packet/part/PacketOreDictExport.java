package appeng.ext.aeadditions.network.packet.part;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.ext.aeadditions.container.ContainerOreDictExport;
import appeng.ext.aeadditions.gui.GuiOreDictExport;
import appeng.ext.aeadditions.network.packet.IPacketHandlerClient;
import appeng.ext.aeadditions.network.packet.IPacketHandlerServer;
import appeng.ext.aeadditions.network.packet.Packet;
import appeng.ext.aeadditions.network.packet.PacketBufferEC;
import appeng.ext.aeadditions.network.packet.PacketId;
import appeng.ext.aeadditions.util.GuiUtil;

public class PacketOreDictExport extends Packet {

	private String filter;

	public PacketOreDictExport(String filter) {
		this.filter = filter;
	}

	@Override
	public PacketId getPacketId() {
		return PacketId.EXPORT_ORE;
	}

	@Override
	protected void writeData(PacketBufferEC data) throws IOException {
		data.writeString(filter);
	}

	@SideOnly(Side.CLIENT)
	public static class HandlerClient implements IPacketHandlerClient {
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayer player) throws IOException {
			String filter = data.readString();
			if (filter == null) {
				return;
			}
			GuiOreDictExport.updateFilter(filter);
		}
	}

	public static class HandlerServer implements IPacketHandlerServer {
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayerMP player) throws IOException {
			String filter = data.readString();
			ContainerOreDictExport container = GuiUtil.getContainer(player, ContainerOreDictExport.class);
			if (filter == null || container == null) {
				return;
			}
			container.setFilter(filter);
		}
	}
}
