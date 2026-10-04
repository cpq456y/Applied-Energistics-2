package appeng.ext.aeadditions.util;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import appeng.ext.aeadditions.registries.ItemEnum;

public class CreativeTabEC extends CreativeTabs {

	public static final CreativeTabs INSTANCE = new CreativeTabEC();

	public CreativeTabEC() {
		super("AE_Additions");
	}

	@Override
	public ItemStack createIcon() {
		return ItemEnum.FLUIDPATTERN.getSizedStack(1);
	}
}
