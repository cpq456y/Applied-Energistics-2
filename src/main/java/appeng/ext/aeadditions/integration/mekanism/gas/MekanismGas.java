package appeng.ext.aeadditions.integration.mekanism.gas;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.AEApi;
import appeng.ext.aeadditions.api.AEAApi;
import appeng.ext.aeadditions.api.gas.IGasStorageChannel;
import appeng.ext.aeadditions.integration.Integration;
import appeng.ext.aeadditions.integration.jei.Jei;

/** Ported from MekanismGas.kt. */
public final class MekanismGas {

    public static final Map<Gas, Fluid> fluidGas = new HashMap<>();

    private MekanismGas() {
    }

    public static void preInit() {
        AEApi.instance().storage().registerStorageChannel(IGasStorageChannel.class, new GasStorageChannel());
    }

    public static void init() {
        // Do Nothing
    }

    public static void postInit() {
        final Iterator<Gas> iterator = GasRegistry.getRegisteredGasses().iterator();

        while (iterator.hasNext()) {
            final Gas gas = iterator.next();

            final Fluid fluid = new GasFluid(gas);

            if (!FluidRegistry.isFluidRegistered(fluid) && FluidRegistry.registerFluid(fluid)) {
                fluidGas.put(gas, fluid);

                if (Integration.Mods.JEI.isEnabled()) {
                    Jei.addFluidToBlacklist(new FluidStack(fluid, 1000));
                }
            }
        }

        AEAApi.instance().addFluidToShowBlacklist(GasFluid.class);
        AEAApi.instance().addFluidToStorageBlacklist(GasFluid.class);
    }

    public static ResourceLocation getGasResourceLocation(final String gasName) {
        return GasRegistry.getGas(gasName).getIcon();
    }

    public static class GasFluid extends Fluid {

        public final Gas gas;

        public GasFluid(final Gas gas) {
            super("ec.internal." + gas.getName(), gas.getIcon(), gas.getIcon());
            this.gas = gas;
        }

        @Override
        public String getLocalizedName(final FluidStack stack) {
            return this.gas.getLocalizedName();
        }
    }
}
