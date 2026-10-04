package appeng.ext.aeadditions.api;

import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import net.minecraft.item.ItemStack;

/** Ported from IAEAdditionsStorageCell.kt. */
public interface IAEAdditionsStorageCell<T extends IAEStack<T>> extends ICellWorkbenchItem {

    int getBytes(ItemStack itemStack);

    int getBytesPerType(ItemStack itemStack);

    int getTotalTypes(ItemStack itemStack);

    boolean isBlackListed(ItemStack itemStack, T var2);

    boolean storableInStorageCell();

    boolean isStorageCell(ItemStack itemStack);

    double getIdleDrain();

    IStorageChannel<T> getChannel();
}
