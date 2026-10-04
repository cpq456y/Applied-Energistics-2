package appeng.ext.aeadditions.util;

import java.util.HashMap;
import java.util.Map;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Optional;

import appeng.ext.aeadditions.integration.Integration;
import buildcraft.api.fuels.BuildcraftFuelRegistry;
import buildcraft.api.fuels.IFuel;

/** Ported from FuelBurnTime.kt. */
public final class FuelBurnTime {

    private static final Map<Fluid, Integer> FLUID_BURN_TIMES = new HashMap<>();

    private FuelBurnTime() {
    }

    public static void registerFuel(final Fluid fluid, final int burnTime) {
        FLUID_BURN_TIMES.putIfAbsent(fluid, burnTime);
    }

    public static int getBurnTime(final Fluid fluid) {
        final Integer known = FLUID_BURN_TIMES.get(fluid);
        if (known != null) {
            return known;
        }
        if (Integration.Mods.BCFUEL.isEnabled()) {
            return getBCBurnTime(fluid);
        }
        return 0;
    }

    @Optional.Method(modid = "BuildCraftAPI|fuels")
    private static int getBCBurnTime(final Fluid fluid) {
        if (BuildcraftFuelRegistry.fuel == null) {
            return 0;
        }

        for (final IFuel fuel : BuildcraftFuelRegistry.fuel.getFuels()) {
            if (fuel.getFluid().getFluid() == fluid) {
                return fuel.getTotalBurningTime();
            }
        }
        return 0;
    }
}
