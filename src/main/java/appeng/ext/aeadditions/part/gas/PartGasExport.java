package appeng.ext.aeadditions.part.gas;

import java.util.List;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.ITubeConnection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.util.AECableType;
import appeng.ext.aeadditions.api.gas.IAEGasStack;
import appeng.ext.aeadditions.integration.Integration;
import appeng.ext.aeadditions.integration.mekanism.gas.Capabilities;
import appeng.ext.aeadditions.models.PartModels;
import appeng.ext.aeadditions.util.PermissionUtil;
import appeng.ext.aeadditions.util.StorageChannels;

/** Ported from PartGasExport.kt. */
@Optional.Interface(iface = "mekanism.api.gas.ITubeConnection", modid = "mekanism", striprefs = true)
public class PartGasExport extends PartGasIO implements ITubeConnection {

    @Override
    public float getCableConnectionLength(final AECableType aeCableType) {
        return 5.0f;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(6.0, 6.0, 12.0, 10.0, 10.0, 13.0);
        bch.addBox(4.0, 4.0, 13.0, 12.0, 12.0, 14.0);
        bch.addBox(5.0, 5.0, 14.0, 11.0, 11.0, 15.0);
        bch.addBox(6.0, 6.0, 15.0, 10.0, 10.0, 16.0);
        bch.addBox(6.0, 6.0, 11.0, 10.0, 10.0, 12.0);
    }

    @Override
    public double getPowerUsage() {
        return 1.0;
    }

    @Override
    public boolean onActivate(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, (IPart) this)) {
            return super.onActivate(player, hand, pos);
        }
        return false;
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return PartModels.EXPORT_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return PartModels.EXPORT_ON;
        }
        return PartModels.EXPORT_OFF;
    }

    private final boolean isMekanismEnabled = Integration.Mods.MEKANISMGAS.isEnabled();

    @Override
    public boolean doWork(final int rate, final int ticksSinceLastCall) {
        if (this.isMekanismEnabled) {
            return this.work(rate, ticksSinceLastCall);
        }

        return false;
    }

    protected boolean work(final int rate, final int ticksSinceLastCall) {
        final IGasHandler facingTank = this.getFacingGasTank();
        if (facingTank == null) {
            return false;
        }

        if (!this.isActive()) {
            return false;
        }

        final List<Fluid> activeFilters = this.getActiveFilters();

        for (final Fluid fluid : activeFilters) {
            if (fluid != null) {
                final IAEGasStack stack = this.extractGas(
                        StorageChannels.GAS.createStack(new FluidStack(fluid, rate * ticksSinceLastCall)),
                        Actionable.SIMULATE);

                if (stack != null) {
                    final GasStack gasStack = (GasStack) stack.getGasStack();

                    if (gasStack != null && facingTank.canReceiveGas(this.getFacing().getOpposite(), gasStack.getGas())) {
                        final int filled = facingTank.receiveGas(this.getFacing().getOpposite(), gasStack, true);

                        if (filled > 0) {
                            this.extractGas(StorageChannels.GAS.createStack(new FluidStack(fluid, filled)),
                                    Actionable.MODULATE);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Optional.Method(modid = "mekanism")
    @Override
    public boolean hasCapability(final Capability<?> capabilityClass) {
        return capabilityClass == Capabilities.TUBE_CONNECTION_CAPABILITY;
    }

    @Optional.Method(modid = "mekanism")
    @Override
    public <T> T getCapability(final Capability<T> capabilityClass) {
        if (capabilityClass == Capabilities.TUBE_CONNECTION_CAPABILITY) {
            return Capabilities.TUBE_CONNECTION_CAPABILITY.cast(this);
        }

        return super.getCapability(capabilityClass);
    }

    @Override
    public boolean canTubeConnect(final EnumFacing enumFacing) {
        return enumFacing != null && enumFacing == this.getSide().getFacing();
    }
}
