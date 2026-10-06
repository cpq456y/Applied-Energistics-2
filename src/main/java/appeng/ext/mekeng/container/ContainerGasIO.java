package appeng.ext.mekeng.container;

import appeng.ext.mekeng.common.me.inventory.IGasInventory;
import appeng.ext.mekeng.common.part.PartSharedGasBus;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerGasIO extends ContainerGasConfigurable<PartSharedGasBus> {

    public ContainerGasIO(InventoryPlayer ip, PartSharedGasBus te) {
        super(ip, te);
    }

    @Override
    public IGasInventory getGasConfigInventory() {
        return this.getUpgradeable().getConfig();
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();
    }

}
