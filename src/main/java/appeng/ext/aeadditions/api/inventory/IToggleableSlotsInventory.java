package appeng.ext.aeadditions.api.inventory;

import java.util.Map;

import net.minecraft.inventory.IInventory;

/** Ported from IToggleableSlotsInventory.kt. */
public interface IToggleableSlotsInventory extends IInventory {

    Map<Integer, Boolean> getEnabledSlots();
}
