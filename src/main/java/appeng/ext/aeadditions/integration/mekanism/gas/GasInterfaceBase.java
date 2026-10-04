package appeng.ext.aeadditions.integration.mekanism.gas;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Optional;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.ext.aeadditions.gui.widget.fluid.IFluidSlotListener;
import appeng.ext.aeadditions.integration.Integration;
import appeng.ext.aeadditions.part.PartECBase;

/** Ported from GasInterfaceBase.kt. */
@Optional.InterfaceList({
        @Optional.Interface(iface = "mekanism.api.gas.IGasHandler", modid = "mekanism", striprefs = true),
        @Optional.Interface(iface = "mekanism.api.gas.ITubeConnection", modid = "mekanism", striprefs = true) })
public abstract class GasInterfaceBase extends PartECBase
        implements IGasHandler, ITubeConnection, IPowerChannelState, IActionHost, IFluidSlotListener {

    public boolean isMekanismLoaded() {
        return Integration.Mods.MEKANISMGAS.isEnabled();
    }

    @Optional.Method(modid = "mekanism")
    public abstract GasTank getGasTank(EnumFacing side);

    @Optional.Method(modid = "mekanism")
    @Override
    public int receiveGas(final EnumFacing side, final GasStack gasStack, final boolean doTransfer) {
        return this.getGasTank(side).receive(gasStack, doTransfer);
    }

    @Optional.Method(modid = "mekanism")
    @Override
    public boolean canReceiveGas(final EnumFacing side, final Gas gasStack) {
        return side != null && !this.hasFilter(AEPartLocation.fromFacing(side))
                && this.getGasTank(side).canReceive(gasStack);
    }

    @Optional.Method(modid = "mekanism")
    @Override
    public GasStack drawGas(final EnumFacing side, final int amount, final boolean doTransfer) {
        return this.getGasTank(side).draw(amount, doTransfer);
    }

    @Optional.Method(modid = "mekanism")
    @Override
    public boolean canDrawGas(final EnumFacing side, final Gas gasStack) {
        return this.getGasTank(side).canDraw(gasStack);
    }

    @Optional.Method(modid = "mekanism")
    @Override
    public boolean canTubeConnect(final EnumFacing side) {
        return this.isMekanismLoaded();
    }

    public abstract String getFilter(AEPartLocation side);

    public void setFilter(final AEPartLocation side, final Fluid fluid) {
        if (fluid == null) {
            this.setFilter(side, "");

            return;
        }

        this.setFilter(side, fluid.getName());
    }

    public abstract void setFilter(AEPartLocation side, String field);

    public boolean hasFilter(final AEPartLocation side) {
        return !this.getFilter(side).equals("");
    }

    @Optional.Method(modid = "mekanism")
    public int exportGas(final EnumFacing side, final GasStack gas, final DimensionalCoord pos) {
        if (gas == null || pos == null || side == null) {
            return 0;
        }

        this.getGasTank(side);

        final World world = pos.getWorld();
        if (world == null) {
            return 0;
        }

        final TileEntity tile = world.getTileEntity(pos.getPos().offset(side));
        if (tile == null) {
            return 0;
        }

        if (!(tile instanceof IGasHandler)) {
            return 0;
        }

        if (((IGasHandler) tile).canReceiveGas(side.getOpposite(), gas.getGas())) {
            return ((IGasHandler) tile).receiveGas(side.getOpposite(), gas, true);
        }

        return 0;
    }

    @Override
    public void setFluid(final int index, final Fluid fluid, final EntityPlayer player) {
        this.setFilter(AEPartLocation.fromOrdinal(index), fluid);
    }
}
