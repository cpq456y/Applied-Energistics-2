package appeng.ext.aeadditions.integration.mekanism;

import appeng.ext.aeadditions.api.AEAApi;
import appeng.ext.aeadditions.integration.Integration;

/** Ported from Mekanism.kt. */
public final class Mekanism {

    private Mekanism() {
    }

    public static void init() {
        if (Integration.Mods.MEKANISMGAS.isEnabled()) {
            AEAApi.instance().addExternalStorageInterface(HandlerMekanismGasTank.INSTANCE);
        }

        AEAApi.instance().registerWrenchHandler(WrenchHandler.INSTANCE);
    }
}
