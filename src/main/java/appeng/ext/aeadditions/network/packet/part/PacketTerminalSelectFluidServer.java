package appeng.ext.aeadditions.network.packet.part;

import appeng.ext.aeadditions.network.packet.IPacketHandlerServer;
import appeng.ext.aeadditions.network.packet.Packet;
import appeng.ext.aeadditions.network.packet.PacketBufferEC;
import appeng.ext.aeadditions.network.packet.PacketId;
import appeng.ext.aeadditions.part.gas.PartGasTerminal;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fluids.Fluid;

import java.io.IOException;

public class PacketTerminalSelectFluidServer extends Packet {
	Fluid fluid;
	PartGasTerminal terminalFluid;

	public PacketTerminalSelectFluidServer(Fluid fluid, PartGasTerminal terminalFluid) {
		this.fluid = fluid;
		this.terminalFluid = terminalFluid;
	}

	@Override
	public void writeData(PacketBufferEC data) throws IOException {
		data.writePart(terminalFluid);
		data.writeFluid(fluid);
	}

	@Override
	public PacketId getPacketId() {
		return PacketId.TERMINAL_SELECT_FLUID;
	}

	public static class Handler implements IPacketHandlerServer {
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayerMP player) throws IOException {
			PartGasTerminal terminalFluid = data.readPart(player.world);
			Fluid fluid = data.readFluid();
			if (fluid == null || terminalFluid == null) {
				return;
			}

			terminalFluid.setCurrentFluid(fluid);
		}
	}
}
