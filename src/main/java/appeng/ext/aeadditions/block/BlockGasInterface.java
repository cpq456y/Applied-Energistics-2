package appeng.ext.aeadditions.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
import appeng.ext.aeadditions.integration.Integration;
import appeng.ext.aeadditions.network.GuiHandler;
import appeng.ext.aeadditions.util.PermissionUtil;
import appeng.ext.aeadditions.util.TileUtil;
import appeng.ext.aeadditions.util.WrenchUtil;

/** Ported from BlockGasInterface.kt. */
public class BlockGasInterface extends BlockAE {

    public BlockGasInterface() {
        super(Material.IRON, 2.0f, 10.0f);
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta) {
        if (!Integration.Mods.MEKANISMGAS.isEnabled()) {
            return null;
        }

        // Created reflectively on purpose: TileEntityGasInterface implements mekanism.api.gas.IGasHandler, so a
        // direct reference here would force the JVM to define that class (and its missing interface) while
        // BlockEnum is being initialised, which crashes the game when Mekanism is not installed.
        try {
            return (TileEntity) Class.forName("appeng.ext.aeadditions.tileentity.TileEntityGasInterface")
                    .newInstance();
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void breakBlock(final World world, final BlockPos pos, final IBlockState state) {
        if (!world.isRemote) {
            TileUtil.destroy(world, pos);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean onBlockActivated(final World world, final BlockPos pos, final IBlockState state,
            final EntityPlayer player, final EnumHand hand, final EnumFacing side, final float hitX, final float hitY,
            final float hitZ) {
        if (world.isRemote || !Integration.Mods.MEKANISMGAS.isEnabled()) {
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
