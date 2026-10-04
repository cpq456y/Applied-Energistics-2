package appeng.ext.aeadditions.network.packet.part;

import appeng.ext.aeadditions.container.ContainerTerminal;
import appeng.ext.aeadditions.network.packet.IPacketHandlerServer;
import appeng.ext.aeadditions.network.packet.Packet;
import appeng.ext.aeadditions.network.packet.PacketBufferEC;
import appeng.ext.aeadditions.network.packet.PacketId;
import appeng.ext.aeadditions.part.gas.PartGasTerminal;
import appeng.ext.aeadditions.util.GuiUtil;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.IOException;

public class PacketTerminalOpenContainer extends Packet {
	PartGasTerminal terminalFluid;

	public PacketTerminalOpenContainer(PartGasTerminal terminalFluid) {
		this.terminalFluid = terminalFluid;
	}

	@Override
	public void writeData(PacketBufferEC data) throws IOException {
		data.writePart(terminalFluid);
	}

	@Override
	public PacketId getPacketId() {
		return PacketId.TERMINAL_OPEN_CONTAINER;
	}

	public static class Handler implements IPacketHandlerServer {
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayerMP player) throws IOException {
			PartGasTerminal terminalFluid = data.readPart(player.world);
			ContainerTerminal containerTerminal = GuiUtil.getContainer(player, ContainerTerminal.class);
			if (terminalFluid == null) {
				return;
			}

			containerTerminal.forceFluidUpdate();
			terminalFluid.sendCurrentFluid(containerTerminal);
		}
	}
}
