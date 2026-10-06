package appeng.ext.mekeng.common;

import appeng.api.AEApi;
import appeng.bootstrap.RegistrationSlots;
import appeng.items.materials.MaterialType;
import appeng.ext.mekeng.MekEng;
import appeng.ext.mekeng.common.block.BlockGasInterface;
import appeng.ext.mekeng.common.item.ItemDummyGas;
import appeng.ext.mekeng.common.item.ItemGasCell;
import appeng.ext.mekeng.common.item.ItemMkEPart;
import appeng.ext.mekeng.common.item.ItemNormal;
import appeng.ext.mekeng.common.item.ItemPortableGasCell;
import appeng.ext.mekeng.common.item.ItemWirelessGasTerminal;
import appeng.ext.mekeng.common.part.PartGasExportBus;
import appeng.ext.mekeng.common.part.PartGasImportBus;
import appeng.ext.mekeng.common.part.PartGasInterface;
import appeng.ext.mekeng.common.part.PartGasInterfaceConfigurationTerminal;
import appeng.ext.mekeng.common.part.PartGasLevelEmitter;
import appeng.ext.mekeng.common.part.PartGasStorageBus;
import appeng.ext.mekeng.common.part.PartGasTerminal;
import appeng.ext.mekeng.common.part.p2p.PartP2PGases;
import appeng.ext.mekeng.common.part.reporting.PartGasConversionMonitor;
import appeng.ext.mekeng.common.part.reporting.PartGasStorageMonitor;
import appeng.ext.mekeng.common.tile.TileGasInterface;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemAndBlocks {

    public static final CreativeTabs TAB = new CreativeTabs(appeng.core.AppEng.MOD_ID) {
        @Nonnull
        @Override
        public ItemStack createIcon() {
            return new ItemStack(GAS_INTERFACE);
        }
    };

    public static ItemDummyGas DUMMY_GAS;
    public static ItemGasCell GAS_CELL_1k;
    public static ItemGasCell GAS_CELL_4k;
    public static ItemGasCell GAS_CELL_16k;
    public static ItemGasCell GAS_CELL_64k;
    public static ItemGasCell GAS_CELL_256k;
    public static ItemGasCell GAS_CELL_1024k;
    public static ItemGasCell GAS_CELL_4096k;
    public static ItemGasCell GAS_CELL_16384k;
    public static ItemPortableGasCell PORTABLE_GAS_CELL;
    public static ItemMkEPart<PartGasTerminal> GAS_TERMINAL;
    public static ItemMkEPart<PartGasImportBus> GAS_IMPORT_BUS;
    public static ItemMkEPart<PartGasExportBus> GAS_EXPORT_BUS;
    public static BlockGasInterface GAS_INTERFACE;
    public static ItemMkEPart<PartGasInterface> GAS_INTERFACE_PART;
    public static ItemMkEPart<PartGasStorageBus> GAS_STORAGE_BUS;
    public static ItemMkEPart<PartGasLevelEmitter> GAS_LEVEL_EMITTER;
    public static ItemMkEPart<PartGasInterfaceConfigurationTerminal> GAS_INTERFACE_TERMINAL;
    public static ItemWirelessGasTerminal WIRELESS_GAS_TERMINAL;
    public static ItemMkEPart<PartP2PGases> GAS_P2P;
    public static ItemMkEPart<PartGasStorageMonitor> GAS_STORAGE_MONITOR;
    public static ItemMkEPart<PartGasConversionMonitor> GAS_CONVERSION_MONITOR;

    /** Gas storage components are AE2 materials (appliedenergistics2:material) so that they share the meta
     *  numbering of the item/fluid/spatial components. See MaterialType.GAS_CELL*_PART. */
    private static ItemStack gasPart(MaterialType type) {
        return type.stack(1);
    }

    public static void init(RegistryHandler regHandler) {
        ItemStack casing = AEApi.instance().definitions().materials().emptyStorageCell().maybeStack(1).orElse(null);
        regHandler.item("dummy_gas", DUMMY_GAS = new ItemDummyGas());

        // The gas cells are handed to the slot that ApiItems placed directly behind AE2's fluid storage cells
        // instead of to the regular handler: Forge assigns item ids in registration order and JEI (like the
        // creative tabs) lists items in that same order, so registering them last would strand them behind
        // every single AE2 item.
        RegistrationSlots.AFTER_FLUID_CELLS.add(regHandler.deferredItem("gas_cell_1k", GAS_CELL_1k = new ItemGasCell(gasPart(MaterialType.GAS_CELL1K_PART), casing, 1, 0.5, 8)));
        RegistrationSlots.AFTER_FLUID_CELLS.add(regHandler.deferredItem("gas_cell_4k", GAS_CELL_4k = new ItemGasCell(gasPart(MaterialType.GAS_CELL4K_PART), casing, 4, 1.0, 32)));
        RegistrationSlots.AFTER_FLUID_CELLS.add(regHandler.deferredItem("gas_cell_16k", GAS_CELL_16k = new ItemGasCell(gasPart(MaterialType.GAS_CELL16K_PART), casing, 16, 1.5, 128)));
        RegistrationSlots.AFTER_FLUID_CELLS.add(regHandler.deferredItem("gas_cell_64k", GAS_CELL_64k = new ItemGasCell(gasPart(MaterialType.GAS_CELL64K_PART), casing, 64, 2.0, 512)));
        RegistrationSlots.AFTER_FLUID_CELLS.add(regHandler.deferredItem("gas_cell_256k", GAS_CELL_256k = new ItemGasCell(gasPart(MaterialType.GAS_CELL256K_PART), casing, 256, 2.5, 2048)));
        RegistrationSlots.AFTER_FLUID_CELLS.add(regHandler.deferredItem("gas_cell_1024k", GAS_CELL_1024k = new ItemGasCell(gasPart(MaterialType.GAS_CELL1024K_PART), casing, 1024, 3.0, 8192)));
        RegistrationSlots.AFTER_FLUID_CELLS.add(regHandler.deferredItem("gas_cell_4096k", GAS_CELL_4096k = new ItemGasCell(gasPart(MaterialType.GAS_CELL4096K_PART), casing, 4096, 3.5, 32768)));
        RegistrationSlots.AFTER_FLUID_CELLS.add(regHandler.deferredItem("gas_cell_16384k", GAS_CELL_16384k = new ItemGasCell(gasPart(MaterialType.GAS_CELL16384K_PART), casing, 16384, 4.0, 131072)));

        // Behind the portable fluid cell, same reasoning as the storage cells above.
        RegistrationSlots.AFTER_PORTABLE_CELLS.add(regHandler.deferredItem("portable_gas_cell", PORTABLE_GAS_CELL = new ItemPortableGasCell()));
        regHandler.item("gas_terminal", GAS_TERMINAL = new ItemMkEPart<>(PartGasTerminal::new));
        regHandler.item("gas_import_bus", GAS_IMPORT_BUS = new ItemMkEPart<>(PartGasImportBus::new));
        regHandler.item("gas_export_bus", GAS_EXPORT_BUS = new ItemMkEPart<>(PartGasExportBus::new));
        regHandler.item("gas_storage_bus", GAS_STORAGE_BUS = new ItemMkEPart<>(PartGasStorageBus::new));
        regHandler.item("gas_level_emitter", GAS_LEVEL_EMITTER = new ItemMkEPart<>(PartGasLevelEmitter::new));
        regHandler.item("gas_interface_terminal", GAS_INTERFACE_TERMINAL = new ItemMkEPart<>(PartGasInterfaceConfigurationTerminal::new));
        regHandler.item("wireless_gas_terminal", WIRELESS_GAS_TERMINAL = new ItemWirelessGasTerminal());
        regHandler.item("gas_p2p", GAS_P2P = new ItemMkEPart<>(PartP2PGases::new));
        regHandler.item("gas_storage_monitor", GAS_STORAGE_MONITOR = new ItemMkEPart<>(PartGasStorageMonitor::new));
        regHandler.item("gas_conversion_monitor", GAS_CONVERSION_MONITOR = new ItemMkEPart<>(PartGasConversionMonitor::new));
        regHandler.block("gas_interface", GAS_INTERFACE = new BlockGasInterface(), TileGasInterface.class);

        // The gas interface mirrors AE2's fluid interface, so both of its items go into the slot that
        // ApiBlocks placed directly behind the fluid interface.
        RegistrationSlots.AFTER_FLUID_INTERFACE.add(regHandler.deferredBlockItem("gas_interface", GAS_INTERFACE));
        RegistrationSlots.AFTER_FLUID_INTERFACE.add(regHandler.deferredItem("gas_interface_part", GAS_INTERFACE_PART = new ItemMkEPart<>(PartGasInterface::new)));
    }

}
