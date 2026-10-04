package appeng.ext.aeadditions.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import appeng.api.networking.security.IActionSource;
import appeng.ext.aeadditions.api.IExternalGasStorageHandler;

/** Ported from GasStorageRegistry.kt. */
public final class GasStorageRegistry {

    private static final List<IExternalGasStorageHandler> HANDLERS = new ArrayList<>();

    private GasStorageRegistry() {
    }

    public static void addExternalStorageInterface(final IExternalGasStorageHandler gasHandler) {
        HANDLERS.add(gasHandler);
    }

    public static IExternalGasStorageHandler getHandler(final TileEntity te, final EnumFacing opposite,
            final IActionSource mySrc) {
        for (final IExternalGasStorageHandler handler : HANDLERS) {
            if (handler.canHandle(te, opposite, mySrc)) {
                return handler;
            }
        }
        return null;
    }
}
