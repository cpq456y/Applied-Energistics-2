package appeng.ext.aeadditions.item;

import net.minecraft.item.Item;

import appeng.ext.aeadditions.models.IItemModelRegister;
import appeng.ext.aeadditions.models.ModelManager;
import appeng.ext.aeadditions.util.CreativeTabEC;

public class ItemECBase extends Item implements IItemModelRegister {
	public ItemECBase() {
		setCreativeTab(CreativeTabEC.INSTANCE);
	}

	@Override
	public void registerModel(Item item, ModelManager manager) {
		manager.registerItemModel(item);
	}
}
