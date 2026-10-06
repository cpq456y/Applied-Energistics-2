package appeng.ext.aeadditions.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.api.features.INetworkEncodable;

/**
 * Wireless fluid terminal handler.
 *
 * <p>This used to extend {@code IWirelessGasFluidTermHandler}, which was removed together with the rest of the
 * AE-Additions gas implementation (the merged Mekanism Energistics layer owns gases now). The members it
 * inherited are declared here directly so the fluid wireless terminal keeps working unchanged.
 */
public interface IWirelessFluidTermHandler extends INetworkEncodable {

	boolean canHandle(ItemStack is);

	boolean hasPower(EntityPlayer player, double amount, ItemStack is);

	boolean isItemNormalWirelessTermToo(ItemStack is);

	boolean usePower(EntityPlayer player, double amount, ItemStack is);
}
