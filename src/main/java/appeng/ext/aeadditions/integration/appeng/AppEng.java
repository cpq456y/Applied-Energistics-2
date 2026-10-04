package appeng.ext.aeadditions.integration.appeng;

import appeng.ext.aeadditions.api.AEAApi;

/** Ported from AppEng.kt. */
public final class AppEng {

    private AppEng() {
    }

    public static void init() {
        AEAApi.instance().registerWrenchHandler(WrenchHandler.INSTANCE);
    }
}
