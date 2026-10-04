package appeng.ext.aeadditions.util;

import appeng.api.AEApi;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.ext.aeadditions.api.gas.IGasStorageChannel;
import appeng.ext.aeadditions.integration.Integration;

/** Ported from StorageChannels.kt. */
public final class StorageChannels {

    public static final IItemStorageChannel ITEM = AEApi.instance().storage()
            .getStorageChannel(IItemStorageChannel.class);

    public static final IFluidStorageChannel FLUID = AEApi.instance().storage()
            .getStorageChannel(IFluidStorageChannel.class);

    public static final IGasStorageChannel GAS = Integration.Mods.MEKANISMGAS.isEnabled()
            ? AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class)
            : null;

    private StorageChannels() {
    }
}
