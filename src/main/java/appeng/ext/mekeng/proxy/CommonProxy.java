package appeng.ext.mekeng.proxy;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.ext.mekeng.MekEng;
import appeng.ext.mekeng.common.ItemAndBlocks;
import appeng.ext.mekeng.common.RegistryHandler;
import appeng.ext.mekeng.common.me.storage.IGasStorageChannel;
import appeng.ext.mekeng.common.me.storage.impl.GasCellGuiHandler;
import appeng.ext.mekeng.common.me.storage.impl.GasStorageChannel;
import appeng.ext.mekeng.network.Packets;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class CommonProxy {

    public final RegistryHandler regHandler = createRegistryHandler();
    public final SimpleNetworkWrapper netHandler = NetworkRegistry.INSTANCE.newSimpleChannel(appeng.core.AppEng.MOD_ID);

    public RegistryHandler createRegistryHandler() {
        return new RegistryHandler();
    }

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(regHandler);
        ItemAndBlocks.init(regHandler);
        AEApi.instance().storage().registerStorageChannel(IGasStorageChannel.class, GasStorageChannel.INSTANCE);
        Packets.init();
    }

    public void init(FMLInitializationEvent event) {
        regHandler.onInit();
        AEApi.instance().registries().cell().addCellGuiHandler(GasCellGuiHandler.INSTANCE);
        AEApi.instance().registries().wireless().registerWirelessHandler(ItemAndBlocks.WIRELESS_GAS_TERMINAL);
    }

    public void postInit(FMLPostInitializationEvent event) {
        // No GUI handler is registered here on purpose: AE2 registers its own handler for the
        // "appliedenergistics2" key during postInit, and a second registration for the same key would replace
        // it (which silently broke every AE2 and AE-Additions GUI). MkE's GUIs are routed through
        // appeng.core.sync.GuiBridge instead, which dispatches ids >= MkEGuis.GUI_ID_BASE back to GuiHandler.
        this.loadP2P();
        this.loadUpgrades();
    }

    private void loadP2P() {
        AEApi.instance().registries().p2pTunnel().addNewAttunement(new ItemStack(MekanismBlocks.GasTank), MekEng.GAS);
        AEApi.instance().registries().p2pTunnel().addNewAttunement(new ItemStack(MekanismItems.Flamethrower), MekEng.GAS);
        AEApi.instance().registries().p2pTunnel().addNewAttunement(new ItemStack(MekanismItems.GaugeDropper), MekEng.GAS);
        AEApi.instance().registries().p2pTunnel().addNewAttunement(new ItemStack(MekanismItems.Jetpack), MekEng.GAS);
        AEApi.instance().registries().p2pTunnel().addNewAttunement(new ItemStack(MekanismItems.ScubaTank), MekEng.GAS);
    }

    private void loadUpgrades() {
        Upgrades.INVERTER.registerItem(new ItemStack(ItemAndBlocks.GAS_CELL_1k), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlocks.GAS_CELL_1k), 1);
        Upgrades.INVERTER.registerItem(new ItemStack(ItemAndBlocks.GAS_CELL_4k), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlocks.GAS_CELL_4k), 1);
        Upgrades.INVERTER.registerItem(new ItemStack(ItemAndBlocks.GAS_CELL_16k), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlocks.GAS_CELL_16k), 1);
        Upgrades.INVERTER.registerItem(new ItemStack(ItemAndBlocks.GAS_CELL_64k), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlocks.GAS_CELL_64k), 1);
        Upgrades.INVERTER.registerItem(new ItemStack(ItemAndBlocks.PORTABLE_GAS_CELL), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlocks.PORTABLE_GAS_CELL), 1);
        Upgrades.CAPACITY.registerItem(new ItemStack(ItemAndBlocks.GAS_IMPORT_BUS), 2);
        Upgrades.REDSTONE.registerItem(new ItemStack(ItemAndBlocks.GAS_IMPORT_BUS), 1);
        Upgrades.SPEED.registerItem(new ItemStack(ItemAndBlocks.GAS_IMPORT_BUS), 4);
        Upgrades.CAPACITY.registerItem(new ItemStack(ItemAndBlocks.GAS_EXPORT_BUS), 2);
        Upgrades.REDSTONE.registerItem(new ItemStack(ItemAndBlocks.GAS_EXPORT_BUS), 1);
        Upgrades.SPEED.registerItem(new ItemStack(ItemAndBlocks.GAS_EXPORT_BUS), 4);
        Upgrades.CAPACITY.registerItem(new ItemStack(ItemAndBlocks.GAS_INTERFACE), 2);
        Upgrades.CAPACITY.registerItem(new ItemStack(ItemAndBlocks.GAS_INTERFACE_PART), 2);
        Upgrades.INVERTER.registerItem(new ItemStack(ItemAndBlocks.GAS_STORAGE_BUS), 1);
        Upgrades.CAPACITY.registerItem(new ItemStack(ItemAndBlocks.GAS_STORAGE_BUS), 5);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlocks.GAS_STORAGE_BUS), 1);
        Upgrades.MAGNET.registerItem(new ItemStack(ItemAndBlocks.WIRELESS_GAS_TERMINAL), 1);
    }

}
