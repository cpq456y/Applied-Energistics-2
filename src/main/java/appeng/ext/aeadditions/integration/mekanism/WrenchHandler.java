package appeng.ext.aeadditions.integration.mekanism;

import mekanism.api.IMekWrench;
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
        if (itemStack == null) {
            return false;
        }

        final net.minecraft.item.Item item = itemStack.getItem();

        if (item == null || !(item instanceof IMekWrench)) {
            return false;
        }

        return ((IMekWrench) item).canUseWrench(user, hand, itemStack, rayTraceResult);
    }

    @Override
    public void wrenchUsed(final ItemStack itemStack, final EntityPlayer user, final RayTraceResult rayTraceResult,
            final EnumHand hand) {
        // NOTE: upstream AE-Additions left this as TODO("Not yet implemented"), which throws as soon as a
        // Mekanism wrench is actually used. IMekWrench only offers canUseWrench, so a no-op is correct.
    }
}
