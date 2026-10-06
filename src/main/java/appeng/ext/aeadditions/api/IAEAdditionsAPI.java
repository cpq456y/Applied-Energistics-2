package appeng.ext.aeadditions.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import net.minecraftforge.fluids.Fluid;

import appeng.ext.aeadditions.api.definitions.IBlockDefinition;
import appeng.ext.aeadditions.api.definitions.IPartDefinition;

public interface IAEAdditionsAPI {

	void addFluidToShowBlacklist(Class<? extends Fluid> clazz);

	void addFluidToShowBlacklist(Fluid fluid);

	void addFluidToStorageBlacklist(Class<? extends Fluid> clazz);

	void addFluidToStorageBlacklist(Fluid fluid);

	IBlockDefinition blocks();

	boolean canFluidSeeInTerminal(Fluid fluid);

	boolean canStoreFluid(Fluid fluid);

	String getVersion();

	boolean isWirelessFluidTerminal(ItemStack is);

	ItemStack openPortableFluidCellGui(EntityPlayer player, EnumHand hand, World world);

	IPartDefinition parts();

	@Deprecated
	void registerWirelessFluidTermHandler(IWirelessFluidTermHandler handler);

	/**
	 * @deprecated incorrect spelling
	 */
	@Deprecated
	void registryWirelessFluidTermHandler(IWirelessFluidTermHandler handler);

	void registerFuelBurnTime(Fluid fuel, int burnTime);

	void registerWrenchHandler(IWrenchHandler wrenchHandler);
}
