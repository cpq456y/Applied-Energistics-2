package appeng.ext.aeadditions.integration.buildcraft.tools;

import appeng.ext.aeadditions.api.AEAApi;

/** Ported from BuildcraftTools.kt. */
public final class BuildcraftTools {

    private BuildcraftTools() {
    }

    public static void init() {
        AEAApi.instance().registerWrenchHandler(WrenchHandler.INSTANCE);
    }
}
