package appeng.ext.aeadditions.registries;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import appeng.ext.aeadditions.Constants;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.config.Upgrades;
import appeng.ext.aeadditions.integration.Integration;
import appeng.ext.aeadditions.part.PartBattery;
import appeng.ext.aeadditions.part.PartDrive;
import appeng.ext.aeadditions.part.PartECBase;
import appeng.ext.aeadditions.part.PartOreDictExporter;
import appeng.ext.aeadditions.part.fluid.PartFluidInterface;

public enum PartEnum {
	DRIVE("drive", PartDrive.class),
	BATTERY("battery", PartBattery.class),
	INTERFACE("interface", PartFluidInterface.class),
	OREDICTEXPORTBUS("oredict.export", PartOreDictExporter.class);

	private Integration.Mods mod;


	private static Pair<Upgrades, Integer> generatePair(Upgrades _upgrade, int integer) {
		return Pair.of(_upgrade, integer);
	}

	public static int getPartID(Class<? extends PartECBase> partClass) {
		for (int i = 0; i < values().length; i++) {
			if (values()[i].getPartClass() == partClass) {
				return i;
			}
		}
		return -1;
	}

	public static ItemStack getPartByName(String name){
		for(int i = 0; i < values().length; i++){
			if (values()[i].name == name)
				return new ItemStack(ItemEnum.PARTITEM.getItem(), 1, i);
		}
		return null;
	}

	public static int getPartID(PartECBase partECBase) {
		return getPartID(partECBase.getClass());
	}

	private String unlocalizedName;
	private String name;
	private Class<? extends PartECBase> partClass;
	private String groupName;
	@SideOnly(Side.CLIENT)
	private Optional<ModelResourceLocation> itemModel;
	private Map<Upgrades, Integer> upgrades = new HashMap<Upgrades, Integer>();

	PartEnum(String name, Class<? extends PartECBase> partClass) {
		this(name, partClass, null, (Integration.Mods) null);
	}

	PartEnum(String name, Class<? extends PartECBase> partClass, Integration.Mods mod) {
		this(name, partClass, null, mod);
	}

	PartEnum(String name, Class<? extends PartECBase> partClass, String groupName) {
		this(name, partClass, groupName, (Integration.Mods) null);
	}

	PartEnum(String name, Class<? extends PartECBase> partClass, String groupName, Integration.Mods mod) {
		this.unlocalizedName = "appeng.ext.aeadditions.part." + name;
		this.name = name;
		this.partClass = partClass;
		this.groupName = groupName == null || groupName.isEmpty() ? null : "appeng.ext.aeadditions." + groupName;
		this.mod = mod;
		if (FMLCommonHandler.instance().getSide().isClient()) {
			itemModel = Optional.of(new ModelResourceLocation(Constants.MOD_ID + ":part/" + name.replace(".", "_")));
		}
	}

	PartEnum(String _unlocalizedName, Class<? extends PartECBase> _partClass, String _groupName, Pair<Upgrades, Integer>... _upgrades) {
		this(_unlocalizedName, _partClass, _groupName, (Integration.Mods) null);
		for (Pair<Upgrades, Integer> pair : _upgrades) {
			this.upgrades.put(pair.getKey(), pair.getValue());
		}
	}

	PartEnum(String _unlocalizedName, Class<? extends PartECBase> _partClass, String _groupName, Integration.Mods _mod, Pair<Upgrades, Integer>... _upgrades) {
		this(_unlocalizedName, _partClass, _groupName, _mod);
		for (Pair<Upgrades, Integer> pair : _upgrades) {
			this.upgrades.put(pair.getKey(), pair.getValue());
		}
	}

	public String getGroupName() {
		return this.groupName;
	}

	public Class<? extends PartECBase> getPartClass() {
		return this.partClass;
	}

	public String getStatName() {
		return I18n.translateToLocal(this.unlocalizedName + ".name");
	}

	public String getTranslationKey() {
		return this.unlocalizedName;
	}

	@SuppressWarnings("unchecked")
	public Map<Upgrades, Integer> getUpgrades() {
		return this.upgrades;
	}

	public PartECBase newInstance(ItemStack partStack)
		throws IllegalAccessException, InstantiationException {
		PartECBase partECBase = this.partClass.newInstance();
		partECBase.initializePart(partStack);
		return partECBase;
	}

	@SideOnly(Side.CLIENT)
	public Optional<ModelResourceLocation> getItemModel() {
		return itemModel;
	}

	public Integration.Mods getMod() {
		return mod;
	}
}
