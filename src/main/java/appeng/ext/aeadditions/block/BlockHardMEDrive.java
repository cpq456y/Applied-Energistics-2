package appeng.ext.aeadditions.block;

import java.util.Random;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridNode;
import appeng.api.util.AEPartLocation;
import appeng.ext.aeadditions.api.IWrenchHandler;
import appeng.ext.aeadditions.block.properties.PropertyDrive;
import appeng.ext.aeadditions.models.drive.DriveSlotsState;
import appeng.ext.aeadditions.network.GuiHandler;
import appeng.ext.aeadditions.tileentity.TileEntityHardMeDrive;
import appeng.ext.aeadditions.util.PermissionUtil;
import appeng.ext.aeadditions.util.TileUtil;
import appeng.ext.aeadditions.util.WrenchUtil;

/** Ported from BlockHardMEDrive.kt. */
public class BlockHardMEDrive extends BlockAE {

    private PropertyDirection _facing = null;

    public BlockHardMEDrive() {
        super(Material.ROCK, 2.0f, 1000000f);
    }

    public PropertyDirection getFacing() {
        if (this._facing == null) {
            this._facing = BlockHorizontal.FACING;
        }

        return this._facing;
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta) {
        return new TileEntityHardMeDrive();
    }

    @Override
    public IBlockState getStateForPlacement(final World world, final BlockPos pos, final EnumFacing side,
            final float hitX, final float hitY, final float hitZ, final int meta, final EntityLivingBase placer,
            final EnumHand hand) {
        return this.getDefaultState().withProperty(this.getFacing(), placer.getHorizontalFacing().getOpposite());
    }

    private void dropItems(final World world, final BlockPos pos, final TileEntityHardMeDrive tileEntity) {
        final Random rand = new Random();
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        final IInventory inventory = tileEntity.getInventory();

        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            final ItemStack itemStack = inventory.getStackInSlot(i);

            if (itemStack != null && itemStack.getCount() > 0) {
                final float rx = rand.nextFloat() * 0.8f + .1f;
                final float ry = rand.nextFloat() * 0.8f + .1f;
                final float rz = rand.nextFloat() * 0.8f + .1f;

                final EntityItem entityItem = new EntityItem(world, x + rx, y + ry, z + rz, itemStack.copy());

                if (itemStack.hasTagCompound()) {
                    entityItem.getItem().setTagCompound(itemStack.getTagCompound().copy());
                }

                final float factor = 0.05f;

                entityItem.motionX = rand.nextGaussian() * factor;
                entityItem.motionY = rand.nextGaussian() * factor + 0.2f;
                entityItem.motionZ = rand.nextGaussian() * factor;

                world.spawnEntity(entityItem);

                itemStack.setCount(0);
            }
        }
    }

    @Override
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state,
            final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY,
            final float hitZ) {
        if (world.isRemote) {
            return true;
        }

        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        final TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileEntityHardMeDrive) {
            if (!PermissionUtil.hasPermission(player, SecurityPermissions.BUILD,
                    ((TileEntityHardMeDrive) tile).getGridNode(AEPartLocation.INTERNAL))) {
                return false;
            }
        }

        final ItemStack current = player.getHeldItem(hand);
        if (player.isSneaking()) {
            final RayTraceResult rayTraceResult = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos);

            final IWrenchHandler wrenchHandler = WrenchUtil.getHandler(current, player, rayTraceResult, hand);
            if (wrenchHandler != null) {
                dropBlockAsItem(world, pos, state, 1);

                world.setBlockToAir(pos);

                wrenchHandler.wrenchUsed(current, player, rayTraceResult, hand);
                return true;
            }
        }

        GuiHandler.launchGui(0, player, world, x, y, z);

        return true;
    }

    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final IBlockState state,
            final EntityLivingBase entity, final ItemStack stack) {
        if (entity == null) {
            return;
        }

        super.onBlockPlacedBy(world, pos, state, entity, stack);

        world.setBlockState(pos, state.withProperty(this.getFacing(), entity.getHorizontalFacing().getOpposite()), 2);

        if (world.isRemote) {
            return;
        }

        final TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileEntityHardMeDrive) {
            final IGridNode node = ((TileEntityHardMeDrive) tile).getGridNode(AEPartLocation.INTERNAL);

            if (entity instanceof EntityPlayer && node != null) {
                node.setPlayerID(AEApi.instance().registries().players().getID((EntityPlayer) entity));
            }

            if (node != null) {
                node.updateState();
            }
        }
    }

    @Override
    public void breakBlock(final World world, final BlockPos pos, final IBlockState state) {
        if (world.isRemote) {
            super.breakBlock(world, pos, state);
            return;
        }

        final TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileEntityHardMeDrive) {
            this.dropItems(world, pos, (TileEntityHardMeDrive) tile);

            final IGridNode node = ((TileEntityHardMeDrive) tile).getGridNode(AEPartLocation.INTERNAL);

            if (node != null) {
                node.destroy();
            }
        }

        super.breakBlock(world, pos, state);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[] { this.getFacing() },
                new IUnlistedProperty[] { PropertyDrive.INSTANCE });
    }

    @Override
    public IBlockState getExtendedState(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
        final TileEntityHardMeDrive tileEntity = TileUtil.getTile(world, pos, TileEntityHardMeDrive.class);
        if (tileEntity == null) {
            return super.getExtendedState(state, world, pos);
        }

        final IExtendedBlockState extendedState = (IExtendedBlockState) super.getExtendedState(state, world, pos);

        return extendedState.withProperty(PropertyDrive.INSTANCE, DriveSlotsState.createState(tileEntity));
    }

    @Override
    public IBlockState getStateFromMeta(final int meta) {
        EnumFacing facing = EnumFacing.VALUES[meta];

        if (facing.getAxis() == EnumFacing.Axis.Y) {
            facing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(this.getFacing(), facing);
    }

    @Override
    public int getMetaFromState(final IBlockState state) {
        return state.getValue(this.getFacing()).getIndex();
    }

    @Override
    public IBlockState withRotation(final IBlockState state, final Rotation rot) {
        return state.withProperty(this.getFacing(), rot.rotate(state.getValue(this.getFacing())));
    }

    @Override
    public IBlockState withMirror(final IBlockState state, final Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(this.getFacing())));
    }
}
