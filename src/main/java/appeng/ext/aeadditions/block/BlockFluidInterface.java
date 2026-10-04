package appeng.ext.aeadditions.block;

import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridNode;
import appeng.api.util.AEPartLocation;
import appeng.ext.aeadditions.api.IWrenchHandler;
import appeng.ext.aeadditions.models.ModelManager;
import appeng.ext.aeadditions.network.GuiHandler;
import appeng.ext.aeadditions.tileentity.IListenerTile;
import appeng.ext.aeadditions.tileentity.TileEntityFluidInterface;
import appeng.ext.aeadditions.util.PermissionUtil;
import appeng.ext.aeadditions.util.WrenchUtil;

/** Ported from BlockFluidInterface.kt. */
public class BlockFluidInterface extends BlockAE {

    public BlockFluidInterface() {
        super(Material.IRON, 2.0f, 10.0f);
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta) {
        return new TileEntityFluidInterface();
    }

    @Override
    public void registerModel(final Item item, final ModelManager manager) {
        if (manager != null) {
            manager.registerItemModel(item, 0, "fluid_interface");
        }
    }

    private void dropPatterns(final World world, final BlockPos pos, final TileEntityFluidInterface tileEntity) {
        final Random rand = new Random();
        final int x = pos.getX();
        final int y = pos.getY();
        final int z = pos.getZ();

        final net.minecraft.inventory.IInventory inventory = tileEntity.inventory;

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

        if (tile instanceof TileEntityFluidInterface) {
            if (!PermissionUtil.hasPermission(player, SecurityPermissions.BUILD,
                    ((TileEntityFluidInterface) tile).getGridNode(AEPartLocation.INTERNAL))) {
                return false;
            }
        }

        final ItemStack current = player.getHeldItem(hand);
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
        if (world.isRemote) {
            return;
        }

        final TileEntity tile = world.getTileEntity(pos);

        if (tile != null) {
            if (tile instanceof TileEntityFluidInterface) {
                final IGridNode node = ((TileEntityFluidInterface) tile).getGridNode(AEPartLocation.INTERNAL);

                if (entity != null && entity instanceof EntityPlayer && node != null) {
                    node.setPlayerID(AEApi.instance().registries().players().getID((EntityPlayer) entity));
                }

                if (node != null) {
                    node.updateState();
                }
            }

            if (tile instanceof IListenerTile) {
                ((IListenerTile) tile).registerListener();
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

        if (tile instanceof TileEntityFluidInterface) {
            this.dropPatterns(world, pos, (TileEntityFluidInterface) tile);

            final IGridNode node = ((TileEntityFluidInterface) tile).getGridNode(AEPartLocation.INTERNAL);

            if (node != null) {
                node.destroy();
            }
        }

        super.breakBlock(world, pos, state);
    }
}
