package appeng.ext.aeadditions;

import java.util.ArrayList;
import java.util.List;

import appeng.api.storage.*;

import appeng.ext.aeadditions.api.*;
import appeng.ext.aeadditions.util.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fluids.Fluid;


import appeng.api.AEApi;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AEPartLocation;
import appeng.ext.aeadditions.api.definitions.IBlockDefinition;
import appeng.ext.aeadditions.api.definitions.IPartDefinition;
import appeng.ext.aeadditions.definitions.BlockDefinition;
import appeng.ext.aeadditions.definitions.PartDefinition;
import appeng.ext.aeadditions.network.GuiHandler;
import appeng.ext.aeadditions.wireless.WirelessTermRegistry;

public class AEAdditionsApi implements IAEAdditionsAPI {

	public static final IAEAdditionsAPI instance = new AEAdditionsApi();

	private final List<Class<? extends Fluid>> blacklistShowClass = new ArrayList<Class<? extends Fluid>>();
	private final List<Fluid> blacklistShowFluid = new ArrayList<Fluid>();
	private final List<Class<? extends Fluid>> blacklistStorageClass = new ArrayList<Class<? extends Fluid>>();
	private final List<Fluid> blacklistStorageFluid = new ArrayList<Fluid>();


	@Override
	public void addFluidToShowBlacklist(Class<? extends Fluid> clazz) {
		if (clazz == null || clazz == Fluid.class) {
			return;
		}
		this.blacklistShowClass.add(clazz);
	}

	@Override
	public void addFluidToShowBlacklist(Fluid fluid) {
		if (fluid == null) {
			return;
		}
		this.blacklistShowFluid.add(fluid);
	}

	@Override
	public void addFluidToStorageBlacklist(Class<? extends Fluid> clazz) {
		if (clazz == null || clazz == Fluid.class) {
			return;
		}
		this.blacklistStorageClass.add(clazz);
	}

	@Override
	public void addFluidToStorageBlacklist(Fluid fluid) {
		if (fluid == null) {
			return;
		}
		this.blacklistStorageFluid.add(fluid);
	}

	@Override
	public IBlockDefinition blocks() {
		return BlockDefinition.instance;
	}

	@Override
	public boolean canFluidSeeInTerminal(Fluid fluid) {
		if (fluid == null) {
			return false;
		}
		if (this.blacklistShowFluid.contains(fluid)) {
			return false;
		}
		for (Class<? extends Fluid> clazz : this.blacklistShowClass) {
			if (clazz.isInstance(fluid)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean canStoreFluid(Fluid fluid) {
		if (fluid == null) {
			return false;
		}
		if (this.blacklistStorageFluid.contains(fluid)) {
			return false;
		}
		for (Class<? extends Fluid> clazz : this.blacklistStorageClass) {
			if (clazz.isInstance(fluid)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getVersion() {
		return Constants.VERSION;
	}

	@Override
	public boolean isWirelessFluidTerminal(ItemStack is) {
		return WirelessTermRegistry.isWirelessItem(is);
	}

	@Override
	public ItemStack openPortableFluidCellGui(EntityPlayer player, EnumHand hand, World world) {
		ItemStack stack = player.getHeldItem(hand);
		if (world.isRemote || stack == null || stack.getItem() == null) {
			return stack;
		}
		Item item = stack.getItem();
		if (!(item instanceof IPortableFluidStorageCell)) {
			return stack;
		}
		ICellInventoryHandler<IAEFluidStack> handler = AEApi.instance().registries().cell().getCellInventory(stack, null, StorageChannels.FLUID);
		if (handler == null)
			return stack;
		IMEMonitor<IAEFluidStack> fluidInventory = new MEMonitorHandler<>(handler, StorageChannels.FLUID);
		GuiHandler.launchGui(GuiHandler.getGuiId(3), player, hand, new Object[]{fluidInventory, item});
		return stack;
	}

	private ItemStack openWirelessTerminal(EntityPlayer player, ItemStack itemStack, World world, BlockPos pos, Long key, int guiId, EnumHand hand, IStorageChannel channel) {
		if (world.isRemote) {
			return itemStack;
		}
		IGridHost securityTerminal = (IGridHost) AEApi.instance().registries().locatable().getLocatableBy(key);
		if (securityTerminal == null) {
			return itemStack;
		}
		IGridNode gridNode = securityTerminal
			.getGridNode(AEPartLocation.INTERNAL);
		if (gridNode == null) {
			return itemStack;
		}
		IGrid grid = gridNode.getGrid();
		if (grid == null) {
			return itemStack;
		}
		for (IGridNode node : grid.getMachines((Class<? extends IGridHost>) AEApi.instance().definitions().blocks().wirelessAccessPoint().maybeEntity().get())) {
			IWirelessAccessPoint accessPoint = (IWirelessAccessPoint) node
				.getMachine();
			BlockPos distance = accessPoint.getLocation().getPos().subtract(pos);
			int squaredDistance = distance.getX() * distance.getX() + distance.getY() * distance.getY() + distance.getZ() * distance.getZ();
			if (squaredDistance <= accessPoint.getRange() * accessPoint.getRange()) {
				IStorageGrid gridCache = grid.getCache(IStorageGrid.class);
				if (gridCache != null) {
					IMEMonitor fluidInventory = gridCache.getInventory(channel);
					if (fluidInventory != null) {
						GuiHandler.launchGui(GuiHandler.getGuiId(guiId), player, hand, new Object[]{fluidInventory, WirelessTermRegistry.getWirelessTermHandler(itemStack)});
					}
				}
			}
		}
		return itemStack;
	}

	@Override
	public IPartDefinition parts() {
		return PartDefinition.instance;
	}

	@Override
	public void registerWirelessFluidTermHandler(IWirelessFluidTermHandler handler) {
		WirelessTermRegistry.registerWirelessTermHandler(handler);
	}

	/**
	 * @deprecated Incorrect spelling
	 */
	@Override
	@Deprecated
	public void registryWirelessFluidTermHandler(IWirelessFluidTermHandler handler) {
		registerWirelessFluidTermHandler(handler);
	}

	@Override
	public void registerFuelBurnTime(Fluid fuel, int burnTime) {
		FuelBurnTime.registerFuel(fuel, burnTime);
	}

	@Override
	public void registerWrenchHandler(IWrenchHandler wrenchHandler) {
		WrenchUtil.addWrenchHandler(wrenchHandler);
	}

}
