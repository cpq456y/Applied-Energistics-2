package appeng.ext.aeadditions.proxy;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import appeng.ext.aeadditions.models.ModelManager;
import appeng.ext.aeadditions.models.PartModels;
import appeng.ext.aeadditions.models.blocks.ModelCertusTank;
import appeng.ext.aeadditions.network.PacketHandler;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {

	public ClientProxy() {
		super();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void registerRenderers() {
		//ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWalrus.class, new TileEntityRendererWalrus());
		ModelManager.registerItemAndBlockColors();
	}

	@SubscribeEvent
	public void onBakeModels(ModelBakeEvent event) {
		ModelCertusTank.onBakeModels(event);
		//ModelWalrus.onBakeModels(event);
		ModelManager.onBakeModels(event);
	}

	@Override
	public void registerModels() {
		ModelManager.init();
		PartModels.registerModels();
		ModelManager.registerModels();
	}

	@Override
	public void registerBlock(Block block) {
		super.registerBlock(block);
		ModelManager.registerBlockClient(block);
	}

	@Override
	public void registerItem(Item item) {
		super.registerItem(item);
		ModelManager.registerItemClient(item);
	}

	@Override
	public boolean isClient() {
		return true;
	}

	@Override
	public boolean isServer() {
		return false;
	}

	@Override
	public void registerPackets() {
		super.registerPackets();
		PacketHandler.registerClientPackets();
	}
}
