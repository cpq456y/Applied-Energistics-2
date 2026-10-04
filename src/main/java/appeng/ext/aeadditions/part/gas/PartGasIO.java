package appeng.ext.aeadditions.part.gas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import appeng.api.AEApi;
import appeng.api.config.RedstoneMode;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.ext.aeadditions.container.IUpgradeable;
import appeng.ext.aeadditions.container.gas.ContainerBusGasIO;
import appeng.ext.aeadditions.gui.gas.GuiBusGasIO;
import appeng.ext.aeadditions.gui.widget.fluid.IFluidSlotListener;
import appeng.ext.aeadditions.inventory.IInventoryListener;
import appeng.ext.aeadditions.inventory.InventoryPlain;
import appeng.ext.aeadditions.inventory.UpgradeInventory;
import appeng.ext.aeadditions.network.packet.other.PacketFluidSlotUpdate;
import appeng.ext.aeadditions.network.packet.part.PacketPartConfig;
import appeng.ext.aeadditions.part.PartECBase;
import appeng.ext.aeadditions.util.NetworkUtil;
import io.netty.buffer.ByteBuf;

/** Ported from PartGasIO.kt. */
public abstract class PartGasIO extends PartECBase
        implements IGridTickable, IInventoryListener, IFluidSlotListener, IUpgradeable {

    public final Fluid[] filterFluids = new Fluid[9];
    public final List<Integer> filterOrder = Arrays.asList(4, 1, 3, 5, 7, 0, 2, 6, 8);

    private final UpgradeInventory upgradeInventory = new UpgradeInventory(this) {
        @Override
        public void onContentsChanged() {
            PartGasIO.this.saveData();
        }
    };

    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
    public boolean lastRedstone = false;
    protected byte filterSize = 0;
    private int speedState = 0;

    public int getSpeedState() {
        return this.speedState;
    }

    protected void setSpeedState(final int speedState) {
        this.speedState = speedState;
    }
    protected boolean redstoneControlled = false;

    public RedstoneMode getRedstoneMode() {
        return this.redstoneMode;
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {
        for (final ItemStack stack : this.upgradeInventory.slots) {
            if (stack == null) {
                continue;
            }
            drops.add(stack);
        }
    }

    @Override
    public ItemStack getItemStack(final PartItemStack type) {
        final ItemStack stack = super.getItemStack(type);
        if (type == PartItemStack.WRENCH) {
            stack.getTagCompound().removeTag("upgradeInventory");
        }
        return stack;
    }

    @Override
    public float getCableConnectionLength(final AECableType aeCableType) {
        return 5.0f;
    }

    protected boolean canDoWork() {
        final boolean redstonePowered = this.isRedstonePowered();

        if (!this.redstoneControlled) {
            return true;
        }

        switch (this.redstoneMode) {
            case IGNORE:
                return true;
            case LOW_SIGNAL:
                return !redstonePowered;
            case HIGH_SIGNAL:
                return redstonePowered;
            case SIGNAL_PULSE:
            default:
                return false;
        }
    }

    public abstract boolean doWork(int rate, int ticksSinceLastCall);

    @Override
    public Object getClientGuiElement(final EntityPlayer player) {
        return new GuiBusGasIO(this, player);
    }

    @Override
    public int getLightLevel() {
        return 0;
    }

    @Override
    public Object getServerGuiElement(final EntityPlayer player) {
        return new ContainerBusGasIO(this, player);
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(1, 20, false, false);
    }

    @Override
    public InventoryPlain getUpgradeInventory() {
        return this.upgradeInventory;
    }

    @Override
    public List<String> getWailaBodey(final NBTTagCompound tag, final List<String> oldList) {
        if (tag.hasKey("speed")) {
            oldList.add(Integer.toString(tag.getInteger("speed")) + "mB/t");
        } else {
            oldList.add("125mB/t");
        }
        return oldList;
    }

    @Override
    public NBTTagCompound getWailaTag(final NBTTagCompound tag) {
        tag.setInteger("speed", this.getMaxAmountToTransfer());
        return tag;
    }

    public void loopRedstoneMode(final EntityPlayer player) {
        if (this.redstoneMode.ordinal() + 1 < RedstoneMode.values().length) {
            this.redstoneMode = RedstoneMode.values()[this.redstoneMode.ordinal() + 1];
        } else {
            this.redstoneMode = RedstoneMode.values()[0];
        }
        NetworkUtil.sendToPlayer(new PacketPartConfig(this, PacketPartConfig.FLUID_IO_REDSTONE_MODE,
                this.redstoneMode.toString()), player);
        this.saveData();
    }

    @Override
    public boolean onActivate(final EntityPlayer player, final EnumHand enumHand, final Vec3d pos) {
        final boolean activate = super.onActivate(player, enumHand, pos);
        this.onInventoryChanged();
        return activate;
    }

    public List<Fluid> getActiveFilters() {
        final List<Fluid> filters = new ArrayList<>();
        for (final Integer order : this.filterOrder) {
            final Fluid filter = this.filterFluids[order];
            if (filter != null) {
                filters.add(filter);
            }
        }
        return filters;
    }

    @Override
    public void onInventoryChanged() {
        this.filterSize = 0;
        this.redstoneControlled = false;
        this.speedState = 0;
        for (int i = 0; i < this.upgradeInventory.getSizeInventory(); i++) {
            final ItemStack currentStack = this.upgradeInventory.getStackInSlot(i);
            if (currentStack != null) {
                if (AEApi.instance().definitions().materials().cardCapacity().isSameAs(currentStack)) {
                    this.filterSize++;
                }
                if (AEApi.instance().definitions().materials().cardRedstone().isSameAs(currentStack)) {
                    this.redstoneControlled = true;
                }
                if (AEApi.instance().definitions().materials().cardSpeed().isSameAs(currentStack)) {
                    this.speedState++;
                }
            }
        }
        final IPartHost host = this.getHost();
        final DimensionalCoord coord = this.getLocation();
        if (host == null || coord == null || coord.getWorld() == null || coord.getWorld().isRemote) {
            return;
        }
        NetworkUtil.sendNetworkPacket(new PacketPartConfig(this, PacketPartConfig.FLUID_IO_FILTER,
                Byte.toString(this.filterSize)), coord.getPos(), coord.getWorld());
        NetworkUtil.sendNetworkPacket(new PacketPartConfig(this, PacketPartConfig.FLUID_IO_REDSTONE,
                Boolean.toString(this.redstoneControlled)), coord.getPos(), coord.getWorld());
        this.saveData();
    }

    @Override
    public void onNeighborChanged(final IBlockAccess var1, final BlockPos var2, final BlockPos var3) {
        super.onNeighborChanged(var1, var2, var3);
        if (this.lastRedstone != this.getHost().hasRedstone(this.getSide())) {
            this.lastRedstone = this.isRedstonePowered();

            if (this.lastRedstone && this.redstoneMode == RedstoneMode.SIGNAL_PULSE) {
                this.doWork(this.getMaxAmountToTransfer(), 1);
            }
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.redstoneMode = RedstoneMode.values()[data.getInteger("redstoneMode")];
        for (int i = 0; i <= 8; i++) {
            this.filterFluids[i] = FluidRegistry.getFluid(data.getString("FilterFluid#" + i));
        }
        this.upgradeInventory.readFromNBT(data.getTagList("upgradeInventory", 10));
        this.onInventoryChanged();
    }

    @Override
    public boolean readFromStream(final ByteBuf data) throws IOException {
        return super.readFromStream(data);
    }

    public void sendInformation(final EntityPlayer player) {
        NetworkUtil.sendToPlayer(new PacketFluidSlotUpdate(Arrays.asList(this.filterFluids)), player);
        NetworkUtil.sendToPlayer(new PacketPartConfig(this, PacketPartConfig.FLUID_IO_FILTER,
                Byte.toString(this.filterSize)), player);
        NetworkUtil.sendToPlayer(new PacketPartConfig(this, PacketPartConfig.FLUID_IO_REDSTONE,
                Boolean.toString(this.redstoneControlled)), player);
        NetworkUtil.sendToPlayer(new PacketPartConfig(this, PacketPartConfig.FLUID_IO_REDSTONE_MODE,
                this.redstoneMode.toString()), player);
    }

    @Override
    public void setFluid(final int index, final Fluid fluid, final EntityPlayer player) {
        this.filterFluids[index] = fluid;
        NetworkUtil.sendToPlayer(new PacketFluidSlotUpdate(Arrays.asList(this.filterFluids)), player);
        this.saveData();
    }

    @Override
    public void setPartHostInfo(final AEPartLocation location, final IPartHost iPartHost,
            final TileEntity tileEntity) {
        super.setPartHostInfo(location, iPartHost, tileEntity);
        this.onInventoryChanged();
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        if (this.canDoWork()) {
            return this.doWork(this.getMaxAmountToTransfer(), ticksSinceLastCall) ? TickRateModulation.FASTER
                    : TickRateModulation.SLOWER;
        }
        return TickRateModulation.SLOWER;
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("redstoneMode", this.redstoneMode.ordinal());
        for (int i = 0; i < this.filterFluids.length; i++) {
            final Fluid fluid = this.filterFluids[i];
            if (fluid != null) {
                data.setString("FilterFluid#" + i, fluid.getName());
            } else {
                data.setString("FilterFluid#" + i, "");
            }
        }
        data.setTag("upgradeInventory", this.upgradeInventory.writeToNBT());
    }

    @Override
    public void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);
    }

    protected int getMaxAmountToTransfer() {
        double amount = 125.00;
        if (this.speedState == 4) {
            amount *= 1.50;
        }
        if (this.speedState >= 3) {
            amount *= 2.0;
        }
        if (this.speedState >= 2) {
            amount *= 4.0;
        }
        if (this.speedState >= 1) {
            amount *= 8.0;
        }
        return (int) amount;
    }
}
