package appeng.ext.aeadditions.network;

import com.google.common.base.Preconditions;

import java.io.IOException;

import appeng.ext.aeadditions.Constants;
import appeng.ext.aeadditions.network.packet.*;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.IThreadListener;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientCustomPacketEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.ext.aeadditions.network.packet.other.PacketFluidContainerSlot;
import appeng.ext.aeadditions.network.packet.other.PacketFluidSlotSelect;
import appeng.ext.aeadditions.network.packet.other.PacketFluidSlotUpdate;
import appeng.ext.aeadditions.network.packet.part.PacketFluidInterface;
import appeng.ext.aeadditions.network.packet.part.PacketOreDictExport;
import appeng.ext.aeadditions.network.packet.part.PacketStorageOpenContainer;
import appeng.ext.aeadditions.network.packet.part.PacketStorageSelectFluid;
import appeng.ext.aeadditions.network.packet.part.PacketStorageUpdateFluid;
import appeng.ext.aeadditions.network.packet.part.PacketStorageUpdateState;
import appeng.ext.aeadditions.util.Log;

public class PacketHandler {

	public static final String CHANNEL_ID = Constants.MOD_ID;
	private final FMLEventChannel channel;

	public PacketHandler() {
		channel = NetworkRegistry.INSTANCE.newEventDrivenChannel(CHANNEL_ID);
		channel.register(this);
	}

	public static void registerServerPackets() {
		PacketId.FLUID_SLOT.registerHandler(new PacketFluidSlotSelect.Handler());
		PacketId.FLUID_CONTAINER_SLOT.registerHandler(new PacketFluidContainerSlot.Handler());
		PacketId.EXPORT_ORE.registerHandler(new PacketOreDictExport.HandlerServer());
		PacketId.STORAGE_OPEN_CONTAINER.registerHandler(new PacketStorageOpenContainer.Handler());
		PacketId.STORAGE_SELECT_FLUID.registerHandler(new PacketStorageSelectFluid.Handler());
		PacketId.FLUID_FILLER_SYNC_CLIENT.registerHandler(new PacketFluidFillerSyncClient.HandlerServer());
	}

	@SideOnly(Side.CLIENT)
	public static void registerClientPackets() {
		PacketId.FLUID_SLOT.registerHandler(new PacketFluidSlotUpdate.Handler());
		PacketId.FLUID_INTERFACE.registerHandler(new PacketFluidInterface.Handler());
		PacketId.EXPORT_ORE.registerHandler(new PacketOreDictExport.HandlerClient());
		PacketId.STORAGE_UPDATE_FLUID.registerHandler(new PacketStorageUpdateFluid.Handler());
		PacketId.STORAGE_UPDATE_STATE.registerHandler(new PacketStorageUpdateState.Handler());
		PacketId.FLUID_CRAFTER_CAPACITY.registerHandler(new PacketCrafterCapacity.Companion.HandlerClient());
		PacketId.FLUID_CRAFTER_DROPPED_ITEM.registerHandler(new PacketCrafterDroppedItem.Companion.HandlerClient());
		PacketId.FLUID_FILLER_SLOT_UPDATE.registerHandler(new PacketFluidFillerSlotUpdate.Companion.HandlerClient());
	}

	@SubscribeEvent
	public void onPacket(ServerCustomPacketEvent event) {
		PacketBufferEC data = new PacketBufferEC(event.getPacket().payload());
		EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).player;

		byte packetIdOrdinal = data.readByte();
		PacketId packetId = PacketId.values()[packetIdOrdinal];
		IPacketHandlerServer packetHandler = packetId.getHandlerServer();
		checkThreadAndEnqueue(packetHandler, data, player, player.getServerWorld());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onPacket(ClientCustomPacketEvent event) {
		PacketBufferEC data = new PacketBufferEC(event.getPacket().payload());

		byte packetIdOrdinal = data.readByte();
		PacketId packetId = PacketId.values()[packetIdOrdinal];
		IPacketHandlerClient packetHandler = packetId.getHandlerClient();
		checkThreadAndEnqueue(packetHandler, data, Minecraft.getMinecraft());
	}

	public void sendPacket(FMLProxyPacket packet, EntityPlayerMP player) {
		channel.sendTo(packet, player);
	}

	@SideOnly(Side.CLIENT)
	private static void checkThreadAndEnqueue(final IPacketHandlerClient packet, final PacketBufferEC data, IThreadListener threadListener) {
		if (!threadListener.isCallingFromMinecraftThread()) {
			data.retain();
			threadListener.addScheduledTask(() -> {
				try {
					EntityPlayer player = Minecraft.getMinecraft().player;
					Preconditions.checkNotNull(player, "Tried to send data to client before the player exists.");
					packet.onPacketData(data, player);
				} catch (IOException e) {
					Log.error("Network Error", e);
				} finally {
					data.release();
				}
			});
		}
	}

	private static void checkThreadAndEnqueue(final IPacketHandlerServer packet, final PacketBufferEC data, final EntityPlayerMP player, IThreadListener threadListener) {
		if (!threadListener.isCallingFromMinecraftThread()) {
			data.retain();
			threadListener.addScheduledTask(() -> {
				try {
					packet.onPacketData(data, player);
				} catch (IOException e) {
					Log.error("Network Error", e);
				} finally {
					data.release();
				}
			});
		}
	}
}
