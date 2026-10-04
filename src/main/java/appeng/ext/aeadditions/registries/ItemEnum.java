package appeng.ext.aeadditions.registries;

import appeng.ext.aeadditions.item.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

import appeng.ext.aeadditions.integration.Integration;
import appeng.ext.aeadditions.util.CreativeTabEC;

public enum ItemEnum {
	PARTITEM("part.base", new ItemPartECBase()),
	FLUIDPATTERN("pattern.fluid", new ItemFluidPattern()),
	FLUIDITEM("fluid.item", new ItemFluid(), null, null, true), // Internal EC Item
	CRAFTINGPATTERN("pattern.crafting", new ItemInternalCraftingPattern(), null, null, true),// Internal EC Item
	GASWIRELESSTERMINAL("terminal.gas.wireless", ItemWirelessTerminalGas.INSTANCE, Integration.Mods.MEKANISMGAS),
	OCUPGRADE("oc.upgrade", ItemOCUpgrade.INSTANCE, Integration.Mods.OPENCOMPUTERS, false),
	GASITEM("gas.item", ItemGas.INSTANCE, Integration.Mods.MEKANISMGAS, null, true); //Internal EC Item

	private final String internalName;
	private Item item;
	private Integration.Mods mod;
	private Boolean enabled = true;

	ItemEnum(String internalName, Item item) {
		this(internalName, item, null);
	}

	ItemEnum(String internalName, Item item, Integration.Mods mod) {
		this(internalName, item, mod, CreativeTabEC.INSTANCE);
	}

	ItemEnum(String internalName, Item item, Integration.Mods mod, Boolean enabled) {
		this(internalName, item, mod, CreativeTabEC.INSTANCE);
		this.enabled = enabled;
	}

	ItemEnum(String internalName, Item item, Integration.Mods mod, CreativeTabs creativeTab) {
		this.internalName = internalName;
		this.item = item;
		this.item.setTranslationKey("appeng.ext.aeadditions." + this.internalName);
		this.item.setRegistryName(appeng.ext.aeadditions.Constants.MOD_ID, this.internalName);
		this.mod = mod;
		if ((creativeTab != null) && (mod == null || mod.isEnabled())) {
			this.item.setCreativeTab(creativeTab);
		}
	}

	ItemEnum(String internalName, Item item, Integration.Mods mod, CreativeTabs creativeTab, Boolean enabled) {
		this.internalName = internalName;
		this.item = item;
		this.item.setTranslationKey("appeng.ext.aeadditions." + this.internalName);
		this.item.setRegistryName(appeng.ext.aeadditions.Constants.MOD_ID, this.internalName);
		this.mod = mod;
		if ((creativeTab != null) && (mod == null || mod.isEnabled())) {
			this.item.setCreativeTab(creativeTab);
		}
		this.enabled = enabled;
	}

	public ItemStack getDamagedStack(int damage) {
		return new ItemStack(this.item, 1, damage);
	}

	public String getInternalName() {
		return this.internalName;
	}

	public Item getItem() {
		return this.item;
	}

	public ItemStack getSizedStack(int size) {
		return new ItemStack(this.item, size);
	}

	public String getStatName() {
		return I18n.translateToLocal(this.item.getTranslationKey());
	}

	public Integration.Mods getMod() {
		return mod;
	}

	public boolean shouldRegister() {
		return mod == null || mod.isEnabled();
	}

	public Boolean isEnabled() {
		return enabled;
	}
}
