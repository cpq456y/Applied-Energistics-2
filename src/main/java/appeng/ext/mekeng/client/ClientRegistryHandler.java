package appeng.ext.mekeng.client;

import appeng.api.AEApi;
import appeng.ext.mekeng.MekEng;
import appeng.ext.mekeng.client.model.SpecialModel;
import appeng.ext.mekeng.client.render.DummyGasModel;
import appeng.ext.mekeng.common.RegistryHandler;
import appeng.ext.mekeng.common.part.PartGasExportBus;
import appeng.ext.mekeng.common.part.PartGasImportBus;
import appeng.ext.mekeng.common.part.PartGasInterface;
import appeng.ext.mekeng.common.part.PartGasInterfaceConfigurationTerminal;
import appeng.ext.mekeng.common.part.PartGasStorageBus;
import appeng.ext.mekeng.common.part.PartGasTerminal;
import appeng.ext.mekeng.common.part.p2p.PartP2PGases;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

public class ClientRegistryHandler extends RegistryHandler {

    @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new DummyGasModel.Loader());
        for (Pair<String, Block> entry : blocks) {
            registerModel(entry.getLeft(), Item.getItemFromBlock(entry.getRight()));
        }
        for (Pair<String, Item> entry : items) {
            registerModel(entry.getLeft(), entry.getRight());
        }
        // Items that live in a registration slot are not in 'items', but they need models just the same.
        for (Pair<String, Item> entry : slottedItems) {
            registerModel(entry.getLeft(), entry.getRight());
        }
        AEApi.instance().registries().partModels().registerModels(PartGasTerminal.MODEL_ON);
        AEApi.instance().registries().partModels().registerModels(PartGasTerminal.MODEL_OFF);
        AEApi.instance().registries().partModels().registerModels(PartGasImportBus.MODELS_ON.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasImportBus.MODELS_OFF.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasImportBus.MODELS_HAS_CHANNEL.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasExportBus.MODELS_ON.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasExportBus.MODELS_OFF.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasExportBus.MODELS_HAS_CHANNEL.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasInterface.MODELS_ON.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasInterface.MODELS_OFF.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasInterface.MODELS_HAS_CHANNEL.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasStorageBus.MODELS_ON.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasStorageBus.MODELS_OFF.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasStorageBus.MODELS_HAS_CHANNEL.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasInterfaceConfigurationTerminal.MODEL_ON);
        AEApi.instance().registries().partModels().registerModels(PartGasInterfaceConfigurationTerminal.MODEL_OFF);
        AEApi.instance().registries().partModels().registerModels(PartP2PGases.getModels());
    }


    private static void registerModel(String key, Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item instanceof SpecialModel ? ((SpecialModel) item).getModelPath() : MekEng.id(key), "inventory"));
    }

}
