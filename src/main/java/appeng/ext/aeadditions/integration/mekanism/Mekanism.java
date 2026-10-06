package appeng.ext.aeadditions.integration.mekanism;

import appeng.ext.aeadditions.api.AEAApi;

/** Ported from Mekanism.kt. */
public final class Mekanism {

    private Mekanism() {
    }

    public static void init() {
        AEAApi.instance().registerWrenchHandler(WrenchHandler.INSTANCE);
    }
}
