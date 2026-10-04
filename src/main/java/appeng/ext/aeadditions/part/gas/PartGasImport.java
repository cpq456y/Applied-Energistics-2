package appeng.ext.aeadditions.part.gas;

import java.util.List;

import mekanism.api.gas.Gas;
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
import appeng.ext.aeadditions.integration.mekanism.gas.MekanismGas;
import appeng.ext.aeadditions.models.PartModels;
import appeng.ext.aeadditions.util.GasUtil;
import appeng.ext.aeadditions.util.PermissionUtil;
import appeng.ext.aeadditions.util.StorageChannels;

/** Ported from PartGasImport.kt. */
@Optional.InterfaceList({
        @Optional.Interface(iface = "mekanism.api.gas.IGasHandler", modid = "mekanism", striprefs = true),
        @Optional.Interface(iface = "mekanism.api.gas.ITubeConnection", modid = "mekanism", striprefs = true) })
public class PartGasImport extends PartGasIO implements IGasHandler, ITubeConnection {

    @Override
    public float getCableConnectionLength(final AECableType aeCableType) {
        return 5.0f;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(4.0, 4.0, 14.0, 12.0, 12.0, 16.0);
        bch.addBox(5.0, 5.0, 13.0, 11.0, 11.0, 14.0);
        bch.addBox(6.0, 6.0, 12.0, 10.0, 10.0, 13.0);
        bch.addBox(6.0, 6.0, 11.0, 10.0, 10.0, 12.0);
    }

    @Override
    public double getPowerUsage() {
        return 1.0;
    }

    @Override
    public boolean onActivate(final EntityPlayer player, final EnumHand enumHand, final Vec3d pos) {
        return PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, (IPart) this)
                && super.onActivate(player, enumHand, pos);
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return PartModels.IMPORT_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return PartModels.IMPORT_ON;
        } else {
            return PartModels.IMPORT_OFF;
        }
    }

    private final boolean isMekanismEnabled = Integration.Mods.MEKANISMGAS.isEnabled();

    @Override
    public boolean doWork(final int rate, final int ticksSinceLastCall) {
        if (!this.isMekanismEnabled || this.getFacingGasTank() == null || !this.isActive()) {
            return false;
        }

        boolean empty = true;
        final List<Fluid> activeFilters = this.getActiveFilters();

        for (final Fluid fluid : activeFilters) {
            if (fluid != null) {
                empty = false;

                if (this.fillToNetwork(fluid, rate * ticksSinceLastCall)) {
                    return true;
                }
            }
        }

        return empty && this.fillToNetwork(null, rate * ticksSinceLastCall);
    }

    @Optional.Method(modid = "mekanism")
    public boolean fillToNetwork(final Fluid fluid, final int toDrain) {
        GasStack drained = null;
        final IGasHandler facingTank = this.getFacingGasTank();
        final EnumFacing side = this.getFacing();

        final Gas gasType;
        if (fluid == null) {
            gasType = null;
        } else {
            final GasStack gasStack = GasUtil.getGasStack(new FluidStack(fluid, toDrain));

            gasType = gasStack == null ? null : gasStack.getGas();
        }

        if (gasType == null) {
            drained = facingTank.drawGas(side.getOpposite(), toDrain, false);
        } else if (facingTank.canDrawGas(side.getOpposite(), gasType)) {
            drained = facingTank.drawGas(side.getOpposite(), toDrain, false);
        }

        if (drained == null || drained.amount <= 0 || drained.getGas() == null) {
            return false;
        }

        final IAEGasStack toFill = StorageChannels.GAS.createStack(drained);
        if (toFill == null) {
            return false;
        }

        final IAEGasStack notInjected = this.injectGas(toFill, Actionable.MODULATE);

        if (notInjected != null) {
            final int amount = (int) (toFill.getStackSize() - notInjected.getStackSize());

            if (amount > 0) {
                facingTank.drawGas(side.getOpposite(), amount, true);

                return true;
            }

            return false;
        } else {
            final Object gasStack = toFill.getGasStack();
            if (gasStack instanceof GasStack) {
                facingTank.drawGas(side.getOpposite(), toDrain, true);

                return true;
            }
        }

        return false;
    }

    @Optional.Method(modid = "mekanism")
    @Override
    public int receiveGas(final EnumFacing side, final GasStack stack, final boolean doTransfer) {
        if (!this.canDoWork()) {
            return 0;
        }

        if (stack == null || stack.amount <= 0 || !this.canReceiveGas(side, stack.getGas()) || !this.isActive()) {
            return 0;
        }

        final Actionable action = doTransfer ? Actionable.MODULATE : Actionable.SIMULATE;

        // Workaround to fix duplicate gas when the gas tank auto ejects
        final int amount = Math.min(stack.amount, this.getMaxAmountToTransfer());

        final IAEGasStack gasStack = StorageChannels.GAS.createStack(new GasStack(stack.getGas(), amount));

        final IAEGasStack notInjected = this.injectGas(gasStack, action);
        if (notInjected == null) {
            return amount;
        }

        return amount - (int) notInjected.getStackSize();
    }

    @Optional.Method(modid = "mekanism")
    @Override
    public GasStack drawGas(final EnumFacing p0, final int p1, final boolean p2) {
        return null;
    }

    @Optional.Method(modid = "mekanism")
    @Override
    public boolean canReceiveGas(final EnumFacing side, final Gas gas) {
        if (!this.canDoWork()) {
            return false;
        }

        final Fluid fluid = MekanismGas.fluidGas.get(gas);

        boolean isEmpty = true;

        for (final Fluid filter : this.filterFluids) {
            if (filter != null) {
                isEmpty = false;
                if (filter.equals(fluid)) {
                    return true;
                }
            }
        }

        return isEmpty;
    }

    @Optional.Method(modid = "mekanism")
    @Override
    public boolean canDrawGas(final EnumFacing p0, final Gas p1) {
        return false;
    }

    @Optional.Method(modid = "mekanism")
    @Override
    public boolean hasCapability(final Capability<?> capabilityClass) {
        return capabilityClass == Capabilities.GAS_HANDLER_CAPABILITY
                || capabilityClass == Capabilities.TUBE_CONNECTION_CAPABILITY;
    }

    @Optional.Method(modid = "mekanism")
    @Override
    public <T> T getCapability(final Capability<T> capabilityClass) {
        if (capabilityClass == Capabilities.GAS_HANDLER_CAPABILITY) {
            return Capabilities.GAS_HANDLER_CAPABILITY.cast(this);
        } else if (capabilityClass == Capabilities.TUBE_CONNECTION_CAPABILITY) {
            return Capabilities.TUBE_CONNECTION_CAPABILITY.cast(this);
        }

        return super.getCapability(capabilityClass);
    }

    @Override
    public boolean canTubeConnect(final EnumFacing enumFacing) {
        return enumFacing != null && enumFacing == this.getSide().getFacing();
    }
}
