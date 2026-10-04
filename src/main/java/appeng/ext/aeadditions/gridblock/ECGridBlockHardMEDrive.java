package appeng.ext.aeadditions.gridblock;

import java.util.EnumSet;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.ext.aeadditions.tileentity.TileEntityHardMeDrive;

/** Ported from ECGridBlockHardMEDrive.kt. */
public class ECGridBlockHardMEDrive implements IGridBlock {

    private final TileEntityHardMeDrive host;

    protected IGrid grid = null;
    protected int usedChannels = 0;

    public ECGridBlockHardMEDrive(final TileEntityHardMeDrive host) {
        this.host = host;
    }

    public TileEntityHardMeDrive getHost() {
        return this.host;
    }

    @Override
    public double getIdlePowerUsage() {
        return this.host.getPowerUsage();
    }

    @Override
    public EnumSet<GridFlags> getFlags() {
        return EnumSet.of(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY);
    }

    @Override
    public boolean isWorldAccessible() {
        return true;
    }

    @Override
    public DimensionalCoord getLocation() {
        return this.host.getLocation();
    }

    @Override
    public AEColor getGridColor() {
        return AEColor.TRANSPARENT;
    }

    @Override
    public void onGridNotification(final GridNotification notification) {
        // Do nothing
    }

    @Override
    public void setNetworkStatus(final IGrid grid, final int usedChannels) {
        this.grid = grid;
        this.usedChannels = usedChannels;
    }

    @Override
    public EnumSet<EnumFacing> getConnectableSides() {
        return EnumSet.of(EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH,
                EnumFacing.WEST);
    }

    @Override
    public IGridHost getMachine() {
        return this.host;
    }

    @Override
    public void gridChanged() {
        // Do nothing
    }

    @Override
    public ItemStack getMachineRepresentation() {
        final DimensionalCoord location = this.getLocation();
        final IBlockState blockState = location.getWorld().getBlockState(location.getPos());

        return new ItemStack(blockState.getBlock(), 1, 0);
    }
}
