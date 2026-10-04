package appeng.ext.aeadditions.block;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import appeng.api.config.SecurityPermissions;
import appeng.api.util.AEPartLocation;
import appeng.ext.aeadditions.api.IECTileEntity;
import appeng.ext.aeadditions.api.IWrenchHandler;
import appeng.ext.aeadditions.container.IUpgradeable;
import appeng.ext.aeadditions.network.GuiHandler;
import appeng.ext.aeadditions.tileentity.IListenerTile;
import appeng.ext.aeadditions.tileentity.TileEntityFluidFiller;
import appeng.ext.aeadditions.tileentity.TileEntityFluidInterface;
import appeng.ext.aeadditions.util.PermissionUtil;
import appeng.ext.aeadditions.util.TileUtil;
import appeng.ext.aeadditions.util.WrenchUtil;

/** Ported from BlockFluidFiller.kt. */
public class BlockFluidFiller extends BlockAE {

    public BlockFluidFiller() {
        super(Material.IRON, 2.0f, 10.0f);
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta) {
        return new TileEntityFluidFiller();
    }

    @Override
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state,
            final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY,
            final float hitZ) {
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        final ItemStack current = player.getHeldItem(hand);

        if (world.isRemote) {
            return true;
        }

        final TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof IECTileEntity) {
            if (!PermissionUtil.hasPermission(player, SecurityPermissions.BUILD,
                    ((IECTileEntity) tile).getGridNode(AEPartLocation.INTERNAL))) {
                return false;
            }
        }

        if (player.isSneaking()) {
            final RayTraceResult rayTraceResult = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos);

            final IWrenchHandler wrenchHandler = WrenchUtil.getHandler(current, player, rayTraceResult, hand);
            if (wrenchHandler != null) {
                final ItemStack block = new ItemStack(this, 1, 0);

                if (tile instanceof TileEntityFluidInterface) {
                    block.setTagCompound(((TileEntityFluidInterface) tile).writeFilter(new NBTTagCompound()));
                }

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
        super.onBlockPlacedBy(world, pos, state, entity, stack);

        if (world.isRemote) {
            return;
        }

        TileUtil.setOwner(world, pos, entity);

        final TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof IListenerTile) {
            ((IListenerTile) tile).registerListener();
        }
    }

    @Override
    public void breakBlock(final World world, final BlockPos pos, final IBlockState state) {
        if (world.isRemote) {
            super.breakBlock(world, pos, state);
            return;
        }

        this.dropItems(world, pos);

        TileUtil.destroy(world, pos);

        final TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof IListenerTile) {
            ((IListenerTile) tile).removeListener();
        }

        if (tile instanceof TileEntityFluidFiller) {
            ((TileEntityFluidFiller) tile).finishCrafting();
        }

        super.breakBlock(world, pos, state);
    }

    public void dropItems(final World world, final BlockPos pos) {
        final Random rand = new Random();
        final TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityFluidFiller)) {
            return;
        }
        final TileEntityFluidFiller tileEntity = (TileEntityFluidFiller) te;

        for (int i = 0; i < ((IUpgradeable) tileEntity).getUpgradeInventory().getSizeInventory(); i++) {
            final ItemStack item = ((IUpgradeable) tileEntity).getUpgradeInventory().getStackInSlot(i);
            dropItem(item, rand, world, pos);
        }
    }

    private void dropItem(final ItemStack item, final Random rand, final World world, final BlockPos pos) {
        if (item != null && item.getCount() > 0) {
            final float rx = rand.nextFloat() * 0.8f + 0.1f;
            final float ry = rand.nextFloat() * 0.8f + 0.1f;
            final float rz = rand.nextFloat() * 0.8f + 0.1f;
            final EntityItem entityItem = new EntityItem(world, pos.getX() + rx, pos.getY() + ry, pos.getZ() + rz,
                    item.copy());
            if (item.hasTagCompound()) {
                entityItem.getItem().setTagCompound(item.getTagCompound().copy());
            }
            final float factor = 0.05f;
            entityItem.motionX = rand.nextGaussian() * factor;
            entityItem.motionY = rand.nextGaussian() * factor + 0.2f;
            entityItem.motionZ = rand.nextGaussian() * factor;
            world.spawnEntity(entityItem);
            item.setCount(0);
        }
    }
}
