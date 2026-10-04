package appeng.ext.aeadditions.inventory;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IMaterials;
import appeng.ext.aeadditions.registries.BlockEnum;

/** Ported from CraftingUpgradeInventory.kt. */
public class CraftingUpgradeInventory extends InventoryPlain {

    /** Kotlin called this {@code enum}, which is a Java keyword. */
    private final BlockEnum blockEnum;

    private final Map<Upgrades, Integer> installedUpgrades = new HashMap<>();

    public CraftingUpgradeInventory(final IInventoryListener listener, final BlockEnum blockEnum, final int size) {
        super("", size, 1, listener);
        this.blockEnum = blockEnum;
    }

    public CraftingUpgradeInventory(final IInventoryListener listener, final BlockEnum blockEnum) {
        this(listener, blockEnum, 5);
    }

    public BlockEnum getBlockEnum() {
        return this.blockEnum;
    }

    public Map<Upgrades, Integer> getInstalledUpgrades() {
        return this.installedUpgrades;
    }

    @Override
    public void onContentsChanged() {
        this.installedUpgrades.clear();
        super.onContentsChanged();
    }

    @Override
    public boolean isItemValidForSlot(final int i, final ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        if (this.installedUpgrades.isEmpty()) {
            for (int j = 0; j < this.getSizeInventory(); j++) {
                final ItemStack currentStack = this.getStackInSlot(j);

                if (currentStack != null) {
                    if (AEApi.instance().definitions().materials().cardSpeed().isSameAs(currentStack)) {
                        this.installedUpgrades.put(Upgrades.SPEED,
                                this.installedUpgrades.getOrDefault(Upgrades.SPEED, 0) + 1);
                    }
                    if (AEApi.instance().definitions().materials().cardCapacity().isSameAs(currentStack)) {
                        this.installedUpgrades.put(Upgrades.CAPACITY,
                                this.installedUpgrades.getOrDefault(Upgrades.CAPACITY, 0) + 1);
                    }
                }
            }
        }

        final IMaterials materials = AEApi.instance().definitions().materials();

        if (materials.cardSpeed().isSameAs(itemStack)) {
            return this.installedUpgrades.getOrDefault(Upgrades.SPEED, 0) < this.blockEnum.getUpgrades()
                    .getOrDefault(Upgrades.SPEED, 0);
        }

        if (materials.cardCapacity().isSameAs(itemStack)) {
            return this.installedUpgrades.getOrDefault(Upgrades.CAPACITY, 0) < this.blockEnum.getUpgrades()
                    .getOrDefault(Upgrades.CAPACITY, 0);
        }

        return false;
    }
}
