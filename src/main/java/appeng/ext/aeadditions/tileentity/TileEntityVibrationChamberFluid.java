package appeng.ext.aeadditions.tileentity;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.ext.aeadditions.api.IECTileEntity;
import appeng.ext.aeadditions.container.ContainerVibrationChamberFluid;
import appeng.ext.aeadditions.gridblock.ECGridBlockVibrantChamber;
import appeng.ext.aeadditions.gui.GuiVibrationChamberFluid;
import appeng.ext.aeadditions.network.IGuiProvider;
import appeng.ext.aeadditions.util.FuelBurnTime;

/** Ported from TileEntityVibrationChamberFluid.kt. */
public class TileEntityVibrationChamberFluid extends TileBase
        implements IECTileEntity, IActionHost, IPowerStorage, ITickable, IGuiProvider {

    private boolean isFirstGridNode = true;
    private final ECGridBlockVibrantChamber gridBlock = new ECGridBlockVibrantChamber(this);
    private IGridNode node = null;

    public int burnTime = 0;
    private int burnTimeTotal = 0;
    private int timer = 0;
    private int timerEnergy = 0;
    private double energyLeft = .0;

    private final FluidTank tank = new FluidTank(16000) {
        @Override
        public FluidTank readFromNBT(final NBTTagCompound nbt) {
            if (nbt == null) {
                return this;
            }

            if (!nbt.hasKey("Empty")) {
                final FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt);
                this.setFluid(fluid);
            } else {
                this.setFluid(null);
            }

            return this;
        }
    };

    public FluidTank getTank() {
        return this.tank;
    }

    public final FluidHandler fluidHandler = new FluidHandler();

    @Override
    public IGridNode getGridNode(final AEPartLocation p0) {
        if (this.isFirstGridNode && this.hasWorld() && !this.world.isRemote) {
            this.isFirstGridNode = false;

            try {
                this.node = AEApi.instance().grid().createGridNode(this.gridBlock);
                this.node.updateState();
            } catch (final Exception e) {
                this.isFirstGridNode = true;
            }
        }

        return this.node;
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation p0) {
        return AECableType.SMART;
    }

    @Override
    public void securityBreak() {
        // Do Nothing
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public double getPowerUsage() {
        return 0.00;
    }

    @Override
    public IGridNode getActionableNode() {
        return this.getGridNode(AEPartLocation.INTERNAL);
    }

    @Override
    public void update() {
        // NOTE: upstream AE-Additions had this guard inverted (it returned when a world was present),
        // which made the chamber never tick; corrected here.
        if (!this.hasWorld()) {
            return;
        }
        FluidStack fluidStack1 = this.getTank().getFluid();
        if (fluidStack1 != null) {
            fluidStack1 = fluidStack1.copy();
        }
        if (this.getWorld().isRemote) {
            return;
        }
        if (this.burnTime == this.burnTimeTotal) {
            if (this.timer >= 40) {
                this.updateBlock();
                final FluidStack fluidStack = this.getTank().getFluid();
                int localBurnTime = 0;
                if (fluidStack != null) {
                    localBurnTime = FuelBurnTime.getBurnTime(fluidStack.getFluid());
                } else {
                    localBurnTime = 0;
                }
                if (fluidStack != null && localBurnTime > 0) {
                    if (fluidStack.amount >= 250) {
                        if (this.energyLeft <= 0) {
                            this.burnTime = 0;
                            this.burnTimeTotal = localBurnTime / 4;
                            this.getTank().drain(250, true);
                        }
                    }
                }

                this.timer = 0;
            } else {
                this.timer += 1;
            }
        } else {
            this.burnTime += 1;
            if (this.timerEnergy == 4) {
                if (this.energyLeft == 0.00) {
                    final IEnergyGrid energy = this.getGridNode(AEPartLocation.INTERNAL).getGrid()
                            .getCache(IEnergyGrid.class);
                    this.energyLeft = energy.injectPower(24.00, Actionable.MODULATE);
                } else {
                    final IEnergyGrid energy = this.getGridNode(AEPartLocation.INTERNAL).getGrid()
                            .getCache(IEnergyGrid.class);
                    this.energyLeft = energy.injectPower(this.energyLeft, Actionable.MODULATE);
                }
                this.timerEnergy = 0;
            } else {
                this.timerEnergy += 1;
            }
        }
        if (fluidStack1 == null && this.getTank().getFluid() == null) {
            return;
        }
        if (fluidStack1 == null || this.getTank().getFluid() == null) {
            this.updateBlock();
            return;
        }

        if (!fluidStack1.equals(this.getTank().getFluid())) {
            this.updateBlock();
            return;
        }

        if (fluidStack1.amount == this.getTank().getFluid().amount) {
            this.updateBlock();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        super.writeToNBT(compound);
        this.writePowerToNBT(compound);
        compound.setInteger("burnTime", this.burnTime);
        compound.setInteger("burnTimeTotal", this.burnTimeTotal);
        compound.setInteger("timer", this.timer);
        compound.setInteger("timerEnergy", this.timerEnergy);
        compound.setDouble("energyLeft", this.energyLeft);
        this.getTank().writeToNBT(compound);
        return compound;
    }

    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.readPowerFromNBT(compound);
        if (compound.hasKey("burnTime")) {
            this.burnTime = compound.getInteger("burnTime");
        }
        if (compound.hasKey("burnTimeTotal")) {
            this.burnTimeTotal = compound.getInteger("burnTimeTotal");
        }
        if (compound.hasKey("timer")) {
            this.timer = compound.getInteger("timer");
        }
        if (compound.hasKey("timerEnergy")) {
            this.timerEnergy = compound.getInteger("timerEnergy");
        }
        if (compound.hasKey("energyLeft")) {
            this.energyLeft = compound.getDouble("energyLeft");
        }
        this.getTank().readFromNBT(compound);
    }

    public int getBurntTimeScaled(final int scale) {
        return this.burnTime != 0 ? this.burnTime * scale / this.burnTimeTotal : 0;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public GuiContainer getClientGuiElement(final EntityPlayer player, final Object... args) {
        return new GuiVibrationChamberFluid(player, this);
    }

    @Override
    public Container getServerGuiElement(final EntityPlayer player, final Object... args) {
        return new ContainerVibrationChamberFluid(player.inventory, this);
    }

    @Override
    public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.fluidHandler);
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }

        return super.hasCapability(capability, facing);
    }

    public class FluidHandler implements IFluidHandler {

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return TileEntityVibrationChamberFluid.this.getTank().getTankProperties();
        }

        @Override
        public int fill(final FluidStack resource, final boolean doFill) {
            if (resource == null || resource.getFluid() == null
                    || FuelBurnTime.getBurnTime(resource.getFluid()) == 0) {
                return 0;
            }

            final int filled = TileEntityVibrationChamberFluid.this.getTank().fill(resource, doFill);

            if (filled != 0 && TileEntityVibrationChamberFluid.this.hasWorld()) {
                TileEntityVibrationChamberFluid.this.updateBlock();
            }

            return filled;
        }

        @Override
        public FluidStack drain(final FluidStack resource, final boolean doDrain) {
            return null;
        }

        @Override
        public FluidStack drain(final int maxDrain, final boolean doDrain) {
            return null;
        }
    }
}
