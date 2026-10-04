package appeng.ext.aeadditions.part.gas;

import mekanism.api.gas.GasTank;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Optional;

import appeng.api.parts.IPartCollisionHelper;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.ext.aeadditions.integration.mekanism.gas.GasInterfaceBase;

/** Ported from PartGasInterface.kt. */
public class PartGasInterface extends GasInterfaceBase {

    public String fluidFilter = "";

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public float getCableConnectionLength(final AECableType cable) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getFilter(final AEPartLocation side) {
        return this.fluidFilter;
    }

    @Override
    public void setFilter(final AEPartLocation side, final String fluid) {
        this.fluidFilter = fluid;
    }

    @Optional.Method(modid = "mekanism")
    @Override
    public GasTank getGasTank(final EnumFacing side) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
