package appeng.ext.aeadditions.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumBlockRenderType;

import appeng.ext.aeadditions.models.IItemModelRegister;
import appeng.ext.aeadditions.models.ModelManager;
import appeng.ext.aeadditions.util.CreativeTabEC;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public abstract class BlockAE extends BlockContainer implements IItemModelRegister {

	protected BlockAE(Material material, float hardness, float resistance) {
		super(material);
		setHardness(hardness);
		setResistance(resistance);
		setCreativeTab(CreativeTabEC.INSTANCE);
	}

	protected BlockAE(Material material) {
		super(material);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public void registerModel(Item item, ModelManager manager) {
		manager.registerItemModel(item, 0);
	}
}
