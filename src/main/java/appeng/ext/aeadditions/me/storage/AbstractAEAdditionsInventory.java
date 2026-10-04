package appeng.ext.aeadditions.me.storage;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.ext.aeadditions.api.IAEAdditionsStorageCell;
import appeng.util.Platform;

/** Ported from AbstractAEAdditionsInventory.kt. */
public abstract class AbstractAEAdditionsInventory<T extends IAEStack<T>> implements ICellInventory<T> {

    protected static final String ITEM_TYPE_TAG = "it";
    protected static final String ITEM_COUNT_TAG = "ic";
    protected static final String ITEM_SLOT = "#";
    protected static final String ITEM_SLOT_COUNT = "@";
    protected static final String ITEM_PRE_FORMATTED_COUNT = "PF";
    protected static final String ITEM_PRE_FORMATTED_SLOT = "PF#";
    protected static final String ITEM_PRE_FORMATTED_NAME = "PN";
    protected static final String ITEM_PRE_FORMATTED_FUZZY = "FP";

    protected ISaveProvider container;
    private int maxItemTypes = 500;
    private short storedItems = 0;
    private long storedItemCount = 0L;
    private final ItemStack i;
    protected IAEAdditionsStorageCell<T> cellType;
    protected int itemsPerByte = 0;
    private boolean isPersisted = true;
    private final NBTTagCompound tagCompound;
    protected IItemList<T> cellItems;

    protected AbstractAEAdditionsInventory(final IAEAdditionsStorageCell<T> cellType, final ItemStack o,
            final ISaveProvider container) {
        this.i = o;
        this.cellType = cellType;
        this.itemsPerByte = this.cellType.getChannel().getUnitsPerByte();
        this.maxItemTypes = this.cellType.getTotalTypes(this.i);
        if (this.maxItemTypes < 1) {
            this.maxItemTypes = 1;
        }
        this.container = container;
        this.tagCompound = Platform.openNbtData(o);
        this.storedItems = this.tagCompound.getShort(ITEM_TYPE_TAG);
        this.storedItemCount = this.tagCompound.getLong(ITEM_COUNT_TAG);
        this.cellItems = null;
    }

    /** Replaces the Kotlin lazy {@code cellItems} property. */
    protected IItemList<T> getCellItems() {
        if (this.cellItems == null) {
            this.cellItems = this.getChannel().createList();
            this.loadCellItems();
        }
        return this.cellItems;
    }

    @Override
    public void persist() {
        if (this.isPersisted) {
            return;
        }
        long itemCount = 0L;

        int x = 0;
        int index = 0;
        for (final T element : this.getCellItems()) {
            itemCount += element.getStackSize();
            final NBTTagCompound g = new NBTTagCompound();
            element.writeToNBT(g);
            this.tagCompound.setTag(ITEM_SLOT + index, g);
            this.tagCompound.setLong(ITEM_SLOT_COUNT + index, element.getStackSize());
            x = index;
            index++;
        }
        x++;
        final short oldStoredItems = this.storedItems;
        this.storedItems = (short) this.getCellItems().size();
        if (this.getCellItems().isEmpty()) {
            this.tagCompound.removeTag(ITEM_TYPE_TAG);
        } else {
            this.tagCompound.setShort(ITEM_TYPE_TAG, this.storedItems);
        }
        this.storedItemCount = itemCount;
        if (itemCount == 0L) {
            this.tagCompound.removeTag(ITEM_COUNT_TAG);
        } else {
            this.tagCompound.setLong(ITEM_COUNT_TAG, itemCount);
        }

        while (x < oldStoredItems && x < this.maxItemTypes) {
            this.tagCompound.removeTag(ITEM_SLOT + x);
            this.tagCompound.removeTag(ITEM_SLOT_COUNT + x);
            x++;
        }
        this.isPersisted = true;
    }

    protected void saveChanges() {
        this.storedItems = (short) this.getCellItems().size();
        this.storedItemCount = 0;
        for (final T v : this.getCellItems()) {
            this.storedItemCount += v.getStackSize();
        }
        this.isPersisted = false;
        if (this.container != null) {
            this.container.saveChanges(this);
        } else {
            this.persist();
        }
    }

    protected void loadCellItems() {
        if (this.cellItems == null) {
            this.cellItems = this.getChannel().createList();
        }
        this.cellItems.resetStatus();
        final int types = (int) this.getStoredItemTypes();
        boolean needsUpdate = false;
        for (int slot = 0; slot < types; slot++) {
            final NBTTagCompound compoundTag = this.tagCompound.getCompoundTag(ITEM_SLOT + slot);
            final long stackSize = this.tagCompound.getLong(ITEM_SLOT_COUNT + slot);
            needsUpdate = needsUpdate | !this.loadCellItem(compoundTag, stackSize);
        }
        if (needsUpdate) {
            this.saveChanges();
        }
    }

    protected abstract boolean loadCellItem(NBTTagCompound compoundTag, long stackSize);

    @Override
    public IItemList<T> getAvailableItems(final IItemList<T> out) {
        for (final T item : this.getCellItems()) {
            out.add(item);
        }
        return out;
    }

    @Override
    public ItemStack getItemStack() {
        return this.i;
    }

    @Override
    public double getIdleDrain() {
        return this.cellType.getIdleDrain();
    }

    @Override
    public FuzzyMode getFuzzyMode() {
        return this.cellType.getFuzzyMode(this.i);
    }

    @Override
    public IItemHandler getConfigInventory() {
        return this.cellType.getConfigInventory(this.i);
    }

    @Override
    public IItemHandler getUpgradesInventory() {
        return this.cellType.getUpgradesInventory(this.i);
    }

    @Override
    public int getBytesPerType() {
        return this.cellType.getBytesPerType(this.i);
    }

    @Override
    public boolean canHoldNewItem() {
        final long bytesFree = this.getFreeBytes();
        return (bytesFree > this.getBytesPerType()
                || bytesFree == this.getBytesPerType() && this.getUnusedItemCount() > 0)
                && this.getRemainingItemTypes() > 0;
    }

    @Override
    public long getTotalBytes() {
        return this.cellType.getBytes(this.i);
    }

    @Override
    public long getFreeBytes() {
        return this.getTotalBytes() - this.getUsedBytes();
    }

    @Override
    public long getTotalItemTypes() {
        return this.maxItemTypes;
    }

    @Override
    public long getStoredItemCount() {
        return this.storedItemCount;
    }

    @Override
    public long getStoredItemTypes() {
        return this.storedItems;
    }

    @Override
    public long getRemainingItemTypes() {
        final long basedOnStorage = this.getFreeBytes() / this.getBytesPerType();
        final long baseOnTotal = this.getTotalItemTypes() - this.getStoredItemTypes();
        return basedOnStorage > baseOnTotal ? baseOnTotal : basedOnStorage;
    }

    @Override
    public long getUsedBytes() {
        final long bytesForItemCount = (this.getStoredItemCount() + this.getUnusedItemCount()) / this.itemsPerByte;
        return this.getStoredItemTypes() * this.getBytesPerType() + bytesForItemCount;
    }

    @Override
    public long getRemainingItemCount() {
        final long remaining = this.getFreeBytes() * this.itemsPerByte + this.getUnusedItemCount();
        return remaining > 0 ? remaining : 0;
    }

    @Override
    public int getUnusedItemCount() {
        final int div = (int) (this.getStoredItemCount() % 8);
        return div == 0 ? 0 : this.itemsPerByte - div;
    }

    @Override
    public int getStatusForCell() {
        if (this.canHoldNewItem()) {
            return 1;
        }
        return this.getRemainingItemCount() > 0 ? 2 : 3;
    }
}
