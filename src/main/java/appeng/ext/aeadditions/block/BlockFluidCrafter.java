package appeng.ext.aeadditions.block;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import appeng.api.config.SecurityPermissions;
import appeng.ext.aeadditions.api.IWrenchHandler;
import appeng.ext.aeadditions.container.IUpgradeable;
import appeng.ext.aeadditions.network.GuiHandler;
import appeng.ext.aeadditions.tileentity.TileEntityFluidCrafter;
import appeng.ext.aeadditions.util.PermissionUtil;
import appeng.ext.aeadditions.util.TileUtil;
import appeng.ext.aeadditions.util.WrenchUtil;

/** Ported from BlockFluidCrafter.kt. */
public class BlockFluidCrafter extends BlockAE {

    public BlockFluidCrafter() {
        super(Material.IRON, 2.0f, 10.0f);
    }

    @Override
    public void breakBlock(final World world, final BlockPos pos, final IBlockState state) {
        this.dropItems(world, pos);
        if (!world.isRemote) {
            TileUtil.destroy(world, pos);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public TileEntity createNewTileEntity(final World world, final int meta) {
        return new TileEntityFluidCrafter();
    }

    private void dropItems(final World world, final BlockPos pos) {
        final Random rand = new Random();
        final TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityFluidCrafter)) {
            return;
        }
        final TileEntityFluidCrafter tileEntity = (TileEntityFluidCrafter) te;
        final IInventory inventory = tileEntity.getInventory();
        for (int i = 0; i < inventory.getSizeInventory(); i++) {
            final ItemStack item = inventory.getStackInSlot(i);
            dropItem(item, rand, world, pos);
        }

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

    @Override
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state,
            final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY,
            final float hitZ) {
        if (world.isRemote) {
            return true;
        }
        final ItemStack current = player.getHeldItem(hand);
        if (player.isSneaking()) {
            final TileEntity tile = world.getTileEntity(pos);
            if (!PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, tile)) {
                return false;
            }
            final RayTraceResult rayTraceResult = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos);
            final IWrenchHandler wrenchHandler = WrenchUtil.getHandler(current, player, rayTraceResult, hand);
            if (wrenchHandler != null) {
                spawnAsEntity(world, pos, new ItemStack(this));
                world.setBlockToAir(pos);
                wrenchHandler.wrenchUsed(current, player, rayTraceResult, hand);
                return true;
            }
        }
        GuiHandler.launchGui(0, player, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @Override
    public void onBlockPlacedBy(final World world, final BlockPos pos, final IBlockState state,
            final EntityLivingBase placer, final ItemStack stack) {
        if (world.isRemote) {
            return;
        }
        TileUtil.setOwner(world, pos, placer);
    }
}
