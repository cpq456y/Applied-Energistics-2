package appeng.ext.aeadditions.util;

import appeng.api.AEApi;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;

/** Ported from StorageChannels.kt. */
public final class StorageChannels {

    public static final IItemStorageChannel ITEM = AEApi.instance().storage()
            .getStorageChannel(IItemStorageChannel.class);

    public static final IFluidStorageChannel FLUID = AEApi.instance().storage()
            .getStorageChannel(IFluidStorageChannel.class);

    // The gas channel is no longer provided here: it is the strong-typed one from the merged
    // Mekanism Energistics layer (appeng.ext.mekeng.common.me.storage.IGasStorageChannel).

    private StorageChannels() {
    }
}
