package appeng.ext.aeadditions.integration.jei;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.IModRegistry;
import net.minecraftforge.fluids.FluidStack;

/** Ported from Jei.kt. */
public final class Jei {

    public static final List<FluidStack> fluidBlackList = new ArrayList<>();

    public static IModRegistry registry = null;

    private Jei() {
    }

    public static void addFluidToBlacklist(final FluidStack fluidStack) {
        if (registry != null) {
            registry.getJeiHelpers().getIngredientBlacklist().addIngredientToBlacklist(fluidStack);
        } else {
            fluidBlackList.add(fluidStack);
        }
    }
}
