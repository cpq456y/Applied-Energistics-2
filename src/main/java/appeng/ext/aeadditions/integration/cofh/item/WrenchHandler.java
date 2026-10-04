package appeng.ext.aeadditions.integration.cofh.item;

import cofh.api.item.IToolHammer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

import appeng.ext.aeadditions.api.IWrenchHandler;

/** Ported from WrenchHandler.kt (Kotlin {@code object}, kept as a singleton). */
public final class WrenchHandler implements IWrenchHandler {

    public static final WrenchHandler INSTANCE = new WrenchHandler();

    private WrenchHandler() {
    }

    @Override
    public boolean canWrench(final ItemStack itemStack, final EntityPlayer user, final RayTraceResult rayTraceResult,
            final EnumHand hand) {
        if (itemStack == null || rayTraceResult == null) {
            return false;
        }
        final net.minecraft.item.Item item = itemStack.getItem();

        if (!(item instanceof IToolHammer)) {
            return false;
        }

        return ((IToolHammer) item).isUsable(itemStack, user, rayTraceResult.getBlockPos());
    }

    @Override
    public void wrenchUsed(final ItemStack itemStack, final EntityPlayer user, final RayTraceResult rayTraceResult,
            final EnumHand hand) {
        if (itemStack == null || rayTraceResult == null) {
            return;
        }
        final net.minecraft.item.Item item = itemStack.getItem();

        if (item instanceof IToolHammer) {
            ((IToolHammer) item).toolUsed(itemStack, user, rayTraceResult.getBlockPos());
        }
    }
}
