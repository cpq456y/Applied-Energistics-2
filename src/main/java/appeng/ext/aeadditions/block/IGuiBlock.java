package appeng.ext.aeadditions.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Ported from IGuiBlock.kt. */
public interface IGuiBlock {

    @SideOnly(Side.CLIENT)
    default Object getClientGuiElement(final EntityPlayer player, final World world, final BlockPos pos) {
        return null;
    }

    default Object getServerGuiElement(final EntityPlayer player, final World world, final BlockPos pos) {
        return null;
    }
}
