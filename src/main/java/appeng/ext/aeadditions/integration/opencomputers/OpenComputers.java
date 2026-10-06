package appeng.ext.aeadditions.integration.opencomputers;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.Converter;
import li.cil.oc.api.driver.DriverBlock;
import li.cil.oc.api.driver.DriverItem;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.InventoryProvider;

import appeng.ext.aeadditions.integration.Integration;

/** Ported from OpenComputers.kt. */
public final class OpenComputers {

    private OpenComputers() {
    }

    public static void init() {
        add(new DriverOreDictExportBus());
        add(new DriverFluidInterface());

        // TODO: Re-enable when fixed
//        add(ItemOCUpgrade)
//        AEApi.instance().registries().wireless().registerWirelessHandler(WirelessHandlerUpgradeAE)
        AEAdditionsPathProvider.init();
    }

    public static void add(final Object provider) {
        if (provider instanceof EnvironmentProvider) {
            Driver.add((EnvironmentProvider) provider);
        } else if (provider instanceof DriverItem) {
            Driver.add((DriverItem) provider);
        } else if (provider instanceof DriverBlock) {
            Driver.add((DriverBlock) provider);
        } else if (provider instanceof Converter) {
            Driver.add((Converter) provider);
        } else if (provider instanceof InventoryProvider) {
            Driver.add((InventoryProvider) provider);
        }
    }
}
