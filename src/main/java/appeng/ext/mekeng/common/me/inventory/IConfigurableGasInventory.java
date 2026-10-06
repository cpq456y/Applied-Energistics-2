package appeng.ext.mekeng.common.me.inventory;

public interface IConfigurableGasInventory {

    default IGasInventory getGasInventoryByName(String name) {
        return null;
    }

}
