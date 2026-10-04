package appeng.ext.aeadditions.integration.opencomputers;

import net.minecraft.item.ItemStack;

/** Ported from ResultWrapper.kt. */
public final class ResultWrapper {

    private ResultWrapper() {
    }

    public static Object[] result(final Object... args) {
        final Object[] out = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            out[i] = unwrap(args[i]);
        }
        return out;
    }

    private static Object unwrap(final Object arg) {
        if (arg instanceof ItemStack) {
            return ((ItemStack) arg).isEmpty() ? null : arg;
        }
        return arg;
    }
}
