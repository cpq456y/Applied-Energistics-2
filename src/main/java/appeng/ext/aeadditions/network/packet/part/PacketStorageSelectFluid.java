package appeng.ext.aeadditions.network.packet.part;

import appeng.ext.aeadditions.container.ContainerStorage;
import appeng.ext.aeadditions.network.packet.IPacketHandlerServer;
import appeng.ext.aeadditions.network.packet.Packet;
import appeng.ext.aeadditions.network.packet.PacketBufferEC;
import appeng.ext.aeadditions.network.packet.PacketId;
import appeng.ext.aeadditions.util.GuiUtil;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fluids.Fluid;

import java.io.IOException;

public class PacketStorageSelectFluid extends Packet {
	Fluid fluid;

	public PacketStorageSelectFluid(Fluid fluid) {
		this.fluid = fluid;
	}

	@Override
	public void writeData(PacketBufferEC data) throws IOException {
		data.writeFluid(fluid);
	}

	@Override
	public PacketId getPacketId() {
		return PacketId.STORAGE_SELECT_FLUID;
	}

	public static class Handler implements IPacketHandlerServer {
		@Override
		public void onPacketData(PacketBufferEC data, EntityPlayerMP player) throws IOException {
			Fluid fluid = data.readFluid();
			ContainerStorage containerStorage = GuiUtil.getContainer(player, ContainerStorage.class);
			if (fluid == null || containerStorage == null) {
				return;
			}

			containerStorage.receiveSelectedFluid(fluid);
		}
	}
}
