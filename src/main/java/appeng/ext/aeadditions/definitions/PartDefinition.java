package appeng.ext.aeadditions.definitions;

import appeng.api.definitions.IItemDefinition;
import appeng.ext.aeadditions.api.definitions.IPartDefinition;
import appeng.ext.aeadditions.registries.ItemEnum;
import appeng.ext.aeadditions.registries.PartEnum;

public class PartDefinition implements IPartDefinition {

	public static final PartDefinition instance = new PartDefinition();

	@Override
	public IItemDefinition partBattery() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(),
			PartEnum.BATTERY.ordinal());
	}

	@Override
	public IItemDefinition partDrive() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(),
			PartEnum.DRIVE.ordinal());
	}

	@Override
	public IItemDefinition partOreDictExportBus() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(),
			PartEnum.OREDICTEXPORTBUS.ordinal());
	}

}
