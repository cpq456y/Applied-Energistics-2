package appeng.ext.aeadditions.integration.cofh.item;

import appeng.ext.aeadditions.api.AEAApi;

/** Ported from CofhItem.kt. */
public final class CofhItem {

    private CofhItem() {
    }

    public static void init() {
        AEAApi.instance().registerWrenchHandler(WrenchHandler.INSTANCE);
    }
}
