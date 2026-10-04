package appeng.ext.aeadditions.part.gas;

import java.io.IOException;
import java.util.Random;

import com.google.common.collect.ImmutableList;

import io.netty.buffer.ByteBuf;
import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;

import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.ext.aeadditions.api.gas.IAEGasStack;
import appeng.ext.aeadditions.container.gas.ContainerGasEmitter;
import appeng.ext.aeadditions.gui.gas.GuiGasEmitter;
import appeng.ext.aeadditions.gui.widget.fluid.IFluidSlotListener;
import appeng.ext.aeadditions.integration.mekanism.gas.AEGasStack;
import appeng.ext.aeadditions.models.PartModels;
import appeng.ext.aeadditions.network.packet.other.PacketFluidSlotUpdate;
import appeng.ext.aeadditions.network.packet.part.PacketPartConfig;
import appeng.ext.aeadditions.part.PartECBase;
import appeng.ext.aeadditions.util.GasUtil;
import appeng.ext.aeadditions.util.NetworkUtil;
import appeng.ext.aeadditions.util.PermissionUtil;
import appeng.ext.aeadditions.util.StorageChannels;

/** Ported from PartGasLevelEmitter.kt. */
public class PartGasLevelEmitter extends PartECBase implements IStackWatcherHost, IFluidSlotListener {

    protected Fluid selectedFluid = null;
    private RedstoneMode mode = RedstoneMode.HIGH_SIGNAL;
    private IStackWatcher watcher = null;
    private long wantedAmount = 0;
    protected long currentAmount = 0;
    private boolean previousState = false;
    private boolean clientRedstoneOutput = false;

    @Override
    public float getCableConnectionLength(final AECableType aeCableType) {
        return 16.0f;
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.SMART;
    }

    public void changeWantedAmount(final int modifier, final EntityPlayer player) {
        this.setWantedAmount(this.wantedAmount + modifier, player);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(7.0, 7.0, 11.0, 9.0, 9.0, 16.0);
    }

    @Override
    public Object getClientGuiElement(final EntityPlayer player) {
        return new GuiGasEmitter(this, player);
    }

    @Override
    public double getPowerUsage() {
        return 1.0;
    }

    @Override
    public Object getServerGuiElement(final EntityPlayer player) {
        return new ContainerGasEmitter(this, player);
    }

    private boolean isLevelEmitterOn() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return this.clientRedstoneOutput;
        }
        final IGridNode gridNode = this.getGridNode();
        if (gridNode == null || !gridNode.isActive()) {
            return false;
        }
        switch (this.mode) {
            case LOW_SIGNAL:
                return this.wantedAmount >= this.currentAmount;
            case HIGH_SIGNAL:
                return this.wantedAmount <= this.currentAmount;
            default:
                return false;
        }
    }

    @Override
    public int isProvidingStrongPower() {
        return this.isLevelEmitterOn() ? 15 : 0;
    }

    @Override
    public int isProvidingWeakPower() {
        return this.isProvidingStrongPower();
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    public void notifyTargetBlock(final TileEntity tileEntity, final EnumFacing facing) {
        // note - params are always the same
        tileEntity.getWorld().notifyNeighborsOfStateChange(tileEntity.getPos(), Blocks.AIR, true);
        tileEntity.getWorld().notifyNeighborsOfStateChange(tileEntity.getPos().offset(facing), Blocks.AIR, true);
    }

    @Override
    public boolean onActivate(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, (IPart) this)) {
            return super.onActivate(player, hand, pos);
        }
        return false;
    }

    @Override
    public void onStackChange(final IItemList<?> o, final IAEStack<?> fullStack, final IAEStack<?> diffStack,
            final IActionSource src, final IStorageChannel<?> chan) {
        final IAEGasStack gasFull = fullStack instanceof IAEGasStack ? (IAEGasStack) fullStack : null;

        if (chan == StorageChannels.GAS && gasFull != null
                && gasFull.getGas() == GasUtil.getGas(this.selectedFluid)) {
            this.currentAmount = gasFull.getStackSize();

            final boolean isOn = this.isLevelEmitterOn();

            if (this.previousState != isOn) {

                final IGridNode node = this.getGridNode();

                if (node != null) {

                    this.setActive(node.isActive());
                    if (this.getHost() != null) {
                        this.getHost().markForUpdate();
                    }
                    this.notifyTargetBlock(this.getHostTile(), this.getFacing());
                    this.previousState = isOn;
                }
            }
        }
    }

    @Override
    public void randomDisplayTick(final World world, final BlockPos blockPos, final Random random) {
        if (this.isLevelEmitterOn()) {
            final EnumFacing facing = this.getFacing();
            // TODO: Make sure change works
            final double d0 = facing.getXOffset() * 0.45f + (random.nextFloat() - 0.5f) * 0.2;
            final double d1 = facing.getYOffset() * 0.45f + (random.nextFloat() - 0.5f) * 0.2;
            final double d2 = facing.getZOffset() * 0.45f + (random.nextFloat() - 0.5f) * 0.2;
            world.spawnParticle(EnumParticleTypes.REDSTONE, 0.5 + blockPos.getX() + d0,
                    0.5 + blockPos.getY() + d1, 0.5 + blockPos.getZ() + d2, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.selectedFluid = FluidRegistry.getFluid(data.getString("fluid"));
        this.mode = RedstoneMode.values()[data.getInteger("mode")];
        this.wantedAmount = data.getLong("wantedAmount");
        this.previousState = data.getBoolean("previousState");
        if (this.wantedAmount < 0) {
            this.wantedAmount = 0;
        }
    }

    @Override
    public boolean readFromStream(final ByteBuf data) throws IOException {
        super.readFromStream(data);
        this.clientRedstoneOutput = data.readBoolean();
        this.previousState = data.readBoolean();
        if (this.getHost() != null) {
            this.getHost().markForUpdate();
        }
        return true;
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        if (this.selectedFluid != null) {
            data.setString("fluid", this.selectedFluid.getName());
        } else {
            data.removeTag("fluid");
        }
        data.setInteger("mode", this.mode.ordinal());
        data.setLong("wantedAmount", this.wantedAmount);
        data.setBoolean("previousState", this.previousState);
    }

    @Override
    public void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(this.isLevelEmitterOn());
        data.writeBoolean(this.previousState);
    }

    @Override
    public void setFluid(final int index, final Fluid fluid, final EntityPlayer player) {
        this.selectedFluid = fluid;
        if (this.watcher == null) {
            return;
        }
        this.watcher.reset();
        this.updateWatcher(this.watcher);
        if (this.selectedFluid != null) {
            NetworkUtil.sendToPlayer(new PacketFluidSlotUpdate(ImmutableList.of(this.selectedFluid)), player);
        }
        this.saveData();
    }

    public void setWantedAmount(final long wantedAmount, final EntityPlayer player) {
        this.wantedAmount = wantedAmount;
        if (this.wantedAmount < 0) {
            this.wantedAmount = 0;
        }
        this.notifyTargetBlock(this.getHostTile(), this.getFacing());
        if (this.getHost() != null) {
            this.getHost().markForUpdate();
        }

        this.currentAmount = this.getGridBlock().getGasMonitor().getStorageList()
                .findPrecise(new AEGasStack(new GasStack(GasUtil.getGas(this.selectedFluid), 1000))).getStackSize();

        final boolean isOn = this.isLevelEmitterOn();

        if (this.previousState != isOn) {

            final IGridNode node = this.getGridNode();

            if (node != null) {

                this.setActive(node.isActive());
                if (this.getHost() != null) {
                    this.getHost().markForUpdate();
                }
                this.notifyTargetBlock(this.getHostTile(), this.getFacing());
                this.previousState = isOn;
            }
        }

        this.saveData();
    }

    public void syncClientGui(final EntityPlayer player) {
        NetworkUtil.sendToPlayer(new PacketPartConfig(this, PacketPartConfig.FLUID_EMITTER_MODE,
                this.mode.toString()), player);
        NetworkUtil.sendToPlayer(new PacketPartConfig(this, PacketPartConfig.FLUID_EMITTER_AMOUNT,
                Long.toString(this.wantedAmount)), player);
        if (this.selectedFluid != null) {
            NetworkUtil.sendToPlayer(new PacketFluidSlotUpdate(ImmutableList.of(this.selectedFluid)), player);
        }
    }

    public long getWantedAmount() {
        return this.wantedAmount;
    }

    public void toggleMode(final EntityPlayer player) {
        if (this.mode == RedstoneMode.LOW_SIGNAL) {
            this.mode = RedstoneMode.HIGH_SIGNAL;
        } else {
            this.mode = RedstoneMode.LOW_SIGNAL;
        }
        this.notifyTargetBlock(this.getHostTile(), this.getFacing());
        NetworkUtil.sendToPlayer(new PacketPartConfig(this, PacketPartConfig.FLUID_EMITTER_MODE,
                this.mode.toString()), player);
        if (this.getHost() != null) {
            this.getHost().markForUpdate();
        }
        this.saveData();
    }

    @Override
    public void updateWatcher(final IStackWatcher newWatcher) {
        this.watcher = newWatcher;
        if (this.selectedFluid != null) {
            this.watcher.add(StorageChannels.GAS.createStack(new FluidStack(this.selectedFluid, 1000)));
        }
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return this.isLevelEmitterOn() ? PartModels.EMITTER_ON_HAS_CHANNEL : PartModels.EMITTER_OFF_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return this.isLevelEmitterOn() ? PartModels.EMITTER_ON_ON : PartModels.EMITTER_OFF_ON;
        } else {
            return this.isLevelEmitterOn() ? PartModels.EMITTER_ON_OFF : PartModels.EMITTER_OFF_OFF;
        }
    }
}
