package appeng.ext.aeadditions.tileentity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.crafting.ICraftingWatcher;
import appeng.api.networking.crafting.ICraftingWatcherHost;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.core.AELog;
import appeng.ext.aeadditions.api.IECTileEntity;
import appeng.ext.aeadditions.api.inventory.IToggleableSlotsInventory;
import appeng.ext.aeadditions.container.IUpgradeable;
import appeng.ext.aeadditions.container.fluid.ContainerFluidCrafter;
import appeng.ext.aeadditions.crafting.CraftingPattern;
import appeng.ext.aeadditions.gridblock.ECFluidGridBlock;
import appeng.ext.aeadditions.gui.fluid.GuiFluidCrafter;
import appeng.ext.aeadditions.inventory.CraftingUpgradeInventory;
import appeng.ext.aeadditions.inventory.IInventoryListener;
import appeng.ext.aeadditions.network.IGuiProvider;
import appeng.ext.aeadditions.network.packet.PacketCrafterCapacity;
import appeng.ext.aeadditions.network.packet.PacketCrafterDroppedItem;
import appeng.ext.aeadditions.registries.BlockEnum;
import appeng.ext.aeadditions.util.ItemStackUtils;
import appeng.ext.aeadditions.util.MachineSource;
import appeng.ext.aeadditions.util.NetworkUtil;
import appeng.ext.aeadditions.util.StorageChannels;

/** Ported from TileEntityFluidCrafter.kt. */
public class TileEntityFluidCrafter extends TileBase implements IActionHost, ICraftingProvider, ICraftingWatcherHost,
        IECTileEntity, ITickable, IGuiProvider, IInventoryListener, IUpgradeable {

    private int speedState = 0;
    private int capacity = 0;

    public final List<List<Integer>> inventoryOrder = Arrays.asList(
            Arrays.asList(4),
            Arrays.asList(4, 1, 3, 5, 7),
            Arrays.asList(4, 1, 3, 5, 7, 0, 2, 6, 8));

    public final List<List<Integer>> inventoryOrderSeparated = Arrays.asList(
            Arrays.asList(4),
            Arrays.asList(1, 3, 5, 7),
            Arrays.asList(0, 2, 6, 8));

    public final List<Integer> filterOrder = Arrays.asList(4, 1, 3, 5, 7, 0, 2, 6, 8);

    public final List<ICraftingPatternDetails> craftingList = new ArrayList<>();

    public List<List<Integer>> getInventoryOrder() {
        return this.inventoryOrder;
    }

    public List<List<Integer>> getInventoryOrderSeparated() {
        return this.inventoryOrderSeparated;
    }

    public List<Integer> getFilterOrder() {
        return this.filterOrder;
    }

    public List<ICraftingPatternDetails> getCraftingList() {
        return this.craftingList;
    }

    public final CraftingUpgradeInventory upgradeInventory = new CraftingUpgradeInventory(this,
            BlockEnum.FLUIDCRAFTER) {
        @Override
        public void onContentsChanged() {
            TileEntityFluidCrafter.this.saveData();
            super.onContentsChanged();
        }
    };

    private final ECFluidGridBlock gridBlock;
    private IGridNode node = null;
    private List<IAEItemStack> requestedItems = new ArrayList<>();
    private final List<IAEItemStack> removeList = new ArrayList<>();
    private final ItemStack[] oldStack = new ItemStack[9];
    private boolean isBusy = false;
    private ICraftingWatcher watcher = null;
    private boolean isFirstGetGridNode = true;
    public final FluidCrafterInventory inventory;
    private long finishCraftingTime = 0L;
    private ItemStack returnStack = null;
    private ItemStack[] optionalReturnStack = new ItemStack[0];
    private boolean update = false;
    private final TileEntityFluidCrafter instance;

    public TileEntityFluidCrafter() {
        this.gridBlock = new ECFluidGridBlock(this);
        this.inventory = new FluidCrafterInventory();
        this.instance = this;
    }

    public int getSpeedState() {
        return this.speedState;
    }

    public void setSpeedState(final int speedState) {
        this.speedState = speedState;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void setCapacity(final int capacity) {
        this.capacity = capacity;
    }

    @Override
    public IGridNode getActionableNode() {
        if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
            return this.node;
        }
        if (this.node == null) {
            this.node = AEApi.instance().grid().createGridNode(this.gridBlock);
        }
        return this.node;
    }

    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.SMART;
    }

    public IGridNode getGridNode() {
        return this.getGridNode(AEPartLocation.INTERNAL);
    }

    @Override
    public IGridNode getGridNode(final AEPartLocation dir) {
        if (FMLCommonHandler.instance().getSide().isClient()
                && (this.getWorld() == null || this.getWorld().isRemote)) {
            return null;
        }
        if (this.isFirstGetGridNode) {
            this.isFirstGetGridNode = false;
            this.getActionableNode().updateState();
        }
        return this.node;
    }

    public IInventory getInventory() {
        return this.inventory;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    @Override
    public double getPowerUsage() {
        return 0.0;
    }

    @Override
    public boolean isBusy() {
        return this.isBusy;
    }

    @Override
    public void onRequestChange(final ICraftingGrid craftingGrid, final IAEItemStack what) {
        if (craftingGrid.isRequesting(what)) {
            if (!this.requestedItems.contains(what)) {
                this.requestedItems.add(what);
            }
        } else if (this.requestedItems.contains(what)) {
            this.requestedItems.remove(what);
        }
    }

    @Override
    public void provideCrafting(final ICraftingProviderHelper craftingTracker) {
        for (final ICraftingPatternDetails it : this.craftingList) {
            if (it.getCondensedInputs().length == 0) {
                craftingTracker.setEmitable(it.getCondensedOutputs()[0]);
            } else {
                craftingTracker.addCraftingOption(this, it);
            }
        }
        this.updateWatcher();
    }

    @Override
    public boolean pushPattern(final ICraftingPatternDetails patternDetails, final InventoryCrafting table) {
        if (this.isBusy) {
            return false;
        }
        if (patternDetails instanceof CraftingPattern) {
            final CraftingPattern patter = (CraftingPattern) patternDetails;
            final Map<Fluid, Long> fluids = new HashMap<>();
            for (final IAEFluidStack stack : patter.getCondensedFluidInputs()) {
                if (fluids.containsKey(stack.getFluid())) {
                    final long amount = fluids.get(stack.getFluid()) + stack.getStackSize();
                    fluids.remove(stack.getFluid());
                    fluids.put(stack.getFluid(), amount);
                } else {
                    fluids.put(stack.getFluid(), stack.getStackSize());
                }
            }
            if (this.node == null) {
                return false;
            }
            final IGrid grid = this.node.getGrid();
            if (grid == null) {
                return false;
            }
            final IStorageGrid storage = grid.getCache(IStorageGrid.class);
            if (storage == null) {
                return false;
            }
            for (final Fluid fluid : fluids.keySet()) {
                final Long amount = fluids.get(fluid);
                final IAEFluidStack extractFluid = storage.getInventory(StorageChannels.FLUID)
                        .extractItems(
                                StorageChannels.FLUID
                                        .createStack(new FluidStack(fluid, (int) (amount.longValue() + 0))),
                                Actionable.SIMULATE, new MachineSource(this));
                if (extractFluid == null || extractFluid.getStackSize() != amount) {
                    return false;
                }
            }
            for (final Fluid fluid : fluids.keySet()) {
                final Long amount = fluids.get(fluid);
                storage.getInventory(StorageChannels.FLUID).extractItems(
                        StorageChannels.FLUID.createStack(new FluidStack(fluid, (int) (amount.longValue() + 0))),
                        Actionable.MODULATE, new MachineSource(this));
            }
            this.finishCraftingTime = System.currentTimeMillis() + 1000 - (this.speedState * 175);
            this.returnStack = patter.getOutput(table, this.getWorld());
            this.optionalReturnStack = new ItemStack[9];
            for (int i = 0; i <= 8; i++) {
                final ItemStack s = table.getStackInSlot(i);
                if (s != null && s.getItem() != null) {
                    this.optionalReturnStack[i] = s.getItem().getContainerItem(s.copy());
                }
            }
            this.isBusy = true;
        }
        return true;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.onInventoryChanged();
    }

    @Override
    public void readFromNBT(final NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.inventory.readFromNBT(tagCompound);
        if (this.hasWorld()) {
            final IGridNode node = this.getGridNode();
            if (tagCompound.hasKey("nodes") && node != null) {
                node.loadFromNBT("node0", tagCompound.getCompoundTag("nodes"));
                node.updateState();
            }
        }

        this.upgradeInventory.readFromNBT(tagCompound.getTagList("upgradeInventory", 10));
        this.onInventoryChanged();
    }

    @Override
    public void securityBreak() {
    }

    @Override
    public void update() {
        if (this.getWorld() == null || this.getWorld().provider == null) {
            return;
        }
        if (this.update) {
            this.update = false;
            if (this.getGridNode() != null && this.getGridNode().getGrid() != null) {
                this.getGridNode().getGrid()
                        .postEvent(new MENetworkCraftingPatternChange(this.instance, this.getGridNode()));
            }
        }
        if (this.isBusy && this.finishCraftingTime <= System.currentTimeMillis() && this.getWorld() != null
                && !this.getWorld().isRemote) {
            if (this.node == null || this.returnStack == null) {
                return;
            }
            final IGrid grid = this.node.getGrid();
            if (grid == null) {
                return;
            }
            final IStorageGrid storage = grid.getCache(IStorageGrid.class);
            if (storage == null) {
                return;
            }
            storage.getInventory(StorageChannels.ITEM).injectItems(
                    StorageChannels.ITEM.createStack(this.returnStack), Actionable.MODULATE,
                    new MachineSource(this));
            for (final ItemStack s : this.optionalReturnStack) {
                if (s == null || s.isEmpty()) {
                    continue;
                }
                storage.getInventory(StorageChannels.ITEM).injectItems(StorageChannels.ITEM.createStack(s),
                        Actionable.MODULATE, new MachineSource(this));
            }
            this.optionalReturnStack = new ItemStack[0];
            this.isBusy = false;
            this.returnStack = null;
            this.markDirty();
        }
        if (!this.isBusy && this.getWorld() != null && !this.getWorld().isRemote) {
            for (final IAEItemStack stack : this.removeList) {
                this.requestedItems.remove(stack);
            }
            this.removeList.clear();
            if (!this.requestedItems.isEmpty()) {
                for (final IAEItemStack s : this.requestedItems) {
                    if (this.node == null) {
                        break;
                    }
                    final IGrid grid = this.node.getGrid();
                    if (grid == null) {
                        break;
                    }
                    final ICraftingGrid crafting = grid.getCache(ICraftingGrid.class);
                    if (crafting == null) {
                        break;
                    }
                    if (!crafting.isRequesting(s)) {
                        this.removeList.add(s);
                        continue;
                    }
                    for (final ICraftingPatternDetails details : this.craftingList) {
                        if (details.getCondensedOutputs()[0].equals(s)) {
                            final CraftingPattern patter = (CraftingPattern) details;
                            final Map<Fluid, Long> fluids = new HashMap<>();
                            for (final IAEFluidStack stack : patter.getCondensedFluidInputs()) {
                                if (fluids.containsKey(stack.getFluid())) {
                                    final long amount = fluids.get(stack.getFluid()) + stack.getStackSize();
                                    fluids.remove(stack.getFluid());
                                    fluids.put(stack.getFluid(), amount);
                                } else {
                                    fluids.put(stack.getFluid(), stack.getStackSize());
                                }
                            }
                            final IStorageGrid storage = grid.getCache(IStorageGrid.class);
                            if (storage == null) {
                                break;
                            }
                            boolean doBreak = false;
                            for (final Fluid fluid : fluids.keySet()) {
                                final Long amount = fluids.get(fluid);
                                final IAEFluidStack extractFluid = storage.getInventory(StorageChannels.FLUID)
                                        .extractItems(
                                                StorageChannels.FLUID.createStack(
                                                        new FluidStack(fluid, (int) (amount.longValue() + 0))),
                                                Actionable.SIMULATE, new MachineSource(this));
                                if (extractFluid == null || extractFluid.getStackSize() != amount) {
                                    doBreak = true;
                                    break;
                                }
                            }
                            if (doBreak) {
                                break;
                            }
                            for (final Fluid fluid : fluids.keySet()) {
                                final Long amount = fluids.get(fluid);
                                storage.getInventory(StorageChannels.FLUID).extractItems(
                                        StorageChannels.FLUID.createStack(
                                                new FluidStack(fluid, (int) (amount.longValue() + 0))),
                                        Actionable.MODULATE, new MachineSource(this));
                            }
                            this.finishCraftingTime = System.currentTimeMillis() + 1000 - (this.speedState * 175);
                            this.returnStack = patter.getCondensedOutputs()[0].createItemStack();
                            this.isBusy = true;
                            this.markDirty();
                            return;
                        }
                    }
                }
            }
        }
    }

    private void updateWatcher() {
        this.requestedItems = new ArrayList<>();
        IGrid grid = null;
        final IGridNode node = this.getGridNode();
        ICraftingGrid crafting = null;
        if (node != null) {
            grid = node.getGrid();
            if (grid != null) {
                crafting = grid.getCache(ICraftingGrid.class);
            }
        }
        for (final ICraftingPatternDetails patter : this.craftingList) {
            this.watcher.reset();
            if (patter.getCondensedInputs().length == 0) {
                this.watcher.add(patter.getCondensedOutputs()[0]);
                if (crafting != null) {
                    if (crafting.isRequesting(patter.getCondensedOutputs()[0])) {
                        this.requestedItems.add(patter.getCondensedOutputs()[0]);
                    }
                }
            }
        }
    }

    @Override
    public void updateWatcher(final ICraftingWatcher newWatcher) {
        this.watcher = newWatcher;
        this.updateWatcher();
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        this.inventory.writeToNBT(tagCompound);
        tagCompound.setTag("upgradeInventory", this.upgradeInventory.writeToNBT());
        if (!this.hasWorld()) {
            return tagCompound;
        }
        final IGridNode node = this.getGridNode();
        if (node != null) {
            final NBTTagCompound nodeTag = new NBTTagCompound();
            node.saveToNBT("node0", nodeTag);
            tagCompound.setTag("nodes", nodeTag);
        }
        return tagCompound;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public GuiContainer getClientGuiElement(final EntityPlayer player, final Object... args) {
        return new GuiFluidCrafter(player.inventory, this);
    }

    @Override
    public Container getServerGuiElement(final EntityPlayer player, final Object... args) {
        return new ContainerFluidCrafter(player.inventory, this);
    }

    @Override
    public IInventory getUpgradeInventory() {
        return this.upgradeInventory;
    }

    @Override
    public void onInventoryChanged() {
        if (!this.hasWorld()) {
            return;
        }

        final int oldCapacity = this.capacity;
        this.speedState = 0;
        this.capacity = 0;
        for (int i = 0; i < this.upgradeInventory.getSizeInventory(); i++) {
            final ItemStack currentStack = this.upgradeInventory.getStackInSlot(i);
            if (currentStack != null) {
                if (AEApi.instance().definitions().materials().cardSpeed().isSameAs(currentStack)) {
                    this.speedState++;
                }
                if (AEApi.instance().definitions().materials().cardCapacity().isSameAs(currentStack)) {
                    this.capacity++;
                }
            }
        }

        if (this.capacity < oldCapacity) {
            final List<Integer> indexes = new ArrayList<>();
            // Drop other Patterns
            if (oldCapacity == 2 && this.capacity == 1) {
                indexes.addAll(this.inventoryOrderSeparated.get(2));
            }

            if (oldCapacity == 2 && this.capacity == 0) {
                indexes.addAll(this.inventoryOrderSeparated.get(1));
            }

            if (oldCapacity == 1 && this.capacity == 0) {
                indexes.addAll(this.inventoryOrderSeparated.get(1));
            }

            for (final Integer index : indexes) {
                this.dropItem(index);
            }
        }

        for (final Map.Entry<Integer, Boolean> entry : this.inventory.getEnabledSlots().entrySet()) {
            if (entry.getKey() != 4) {
                this.inventory.getEnabledSlots().put(entry.getKey(), false);
            }
        }

        switch (this.capacity) {
            case 0:
            case 1:
            case 2:
                for (final Integer it : this.inventoryOrder.get(this.capacity)) {
                    if (!this.inventory.getEnabledSlots().containsKey(it)) {
                        this.inventory.getEnabledSlots().put(it, true);
                    }

                    this.inventory.getEnabledSlots().put(it, true);
                }
                break;
            default:
                break;
        }

        if (this.getGridNode(AEPartLocation.INTERNAL) == null) {
            return;
        }

        final DimensionalCoord coord = this.getLocation();
        if (coord == null || coord.getWorld() == null || coord.getWorld().isRemote) {
            return;
        }

        this.updateCraftingList();

        NetworkUtil.sendNetworkPacket(new PacketCrafterCapacity(this, this.capacity), coord.getPos(),
                coord.getWorld());

        this.saveData();
    }

    public void updateCraftingList() {
        if (this.getGridNode(AEPartLocation.INTERNAL) == null || !this.hasWorld()) {
            return;
        }

        this.craftingList.clear();

        for (final ItemStack it : this.inventory.inv) {
            if (!ItemStackUtils.isEmpty(it) && it != null && it.getItem() != null
                    && it.getItem() instanceof ICraftingPatternItem) {
                final CraftingPattern pattern = new CraftingPattern(
                        ((ICraftingPatternItem) it.getItem()).getPatternForItem(it, this.getWorld()));

                this.craftingList.add(pattern);
            }
        }

        try {
            this.getGridNode(AEPartLocation.INTERNAL).getGrid()
                    .postEvent(new MENetworkCraftingPatternChange(this, this.getGridNode(AEPartLocation.INTERNAL)));
        } catch (final Throwable e) {
            AELog.error(e);
        }
    }

    public void removeSlot(final int index) {
        final ItemStack item = this.inventory.getStackInSlot(index);
        if (item != null && item.getCount() > 0) {
            item.setCount(0);
        }
    }

    public void postUpdateEvent() {
        if (this.getGridNode(AEPartLocation.INTERNAL) != null
                && this.getGridNode(AEPartLocation.INTERNAL).getGrid() != null) {
            this.getGridNode(AEPartLocation.INTERNAL).getGrid()
                    .postEvent(new MENetworkCraftingPatternChange(this, this.getGridNode(AEPartLocation.INTERNAL)));
        }
    }

    private void dropItem(final int index) {
        final Random rand = new Random();
        final ItemStack item = this.inventory.getStackInSlot(index);
        if (item != null && item.getCount() > 0) {
            if (this.world.isRemote) {
                return;
            }
            final float rx = rand.nextFloat() * 0.8f + 0.1f;
            final float ry = rand.nextFloat() * 0.8f + 0.1f;
            final float rz = rand.nextFloat() * 0.8f + 0.1f;
            final EntityItem entityItem = new EntityItem(this.world, this.pos.getX() + rx, this.pos.getY() + ry,
                    this.pos.getZ() + rz, item.copy());
            if (item.hasTagCompound()) {
                entityItem.getItem().setTagCompound(item.getTagCompound().copy());
            }
            final float factor = 0.05f;
            entityItem.motionX = rand.nextGaussian() * factor;
            entityItem.motionY = rand.nextGaussian() * factor + 0.2f;
            entityItem.motionZ = rand.nextGaussian() * factor;
            this.world.spawnEntity(entityItem);
            item.setCount(0);

            NetworkUtil.sendNetworkPacket(new PacketCrafterDroppedItem(this, index), this.pos, this.world);
        }
    }

    /** Inventory backing the crafter's pattern slots. */
    public class FluidCrafterInventory implements IToggleableSlotsInventory {

        public final ItemStack[] inv = new ItemStack[9];

        private final Map<Integer, Boolean> enabledSlots = new HashMap<>();

        public FluidCrafterInventory() {
            this.enabledSlots.put(4, true);

            for (int i = 0; i < this.inv.length; i++) {
                this.inv[i] = ItemStack.EMPTY;
            }
        }

        @Override
        public Map<Integer, Boolean> getEnabledSlots() {
            return this.enabledSlots;
        }

        @Override
        public void closeInventory(final EntityPlayer player) {
        }

        @Override
        public ItemStack decrStackSize(final int slot, final int amt) {
            ItemStack stack = this.getStackInSlot(slot);
            if (stack != null && !stack.isEmpty()) {
                if (stack.getCount() <= amt) {
                    this.setInventorySlotContents(slot, ItemStack.EMPTY);
                } else {
                    stack = stack.splitStack(amt);
                    if (stack.getCount() == 0) {
                        this.setInventorySlotContents(slot, ItemStack.EMPTY);
                    }
                }
            }
            TileEntityFluidCrafter.this.update = true;
            this.onContentsChanged();
            return stack;
        }

        @Override
        public String getName() {
            return "inventory.fluidCrafter";
        }

        @Override
        public int getInventoryStackLimit() {
            return 1;
        }

        @Override
        public int getSizeInventory() {
            return this.inv.length;
        }

        @Override
        public boolean isEmpty() {
            // NOTE: upstream AE-Additions had this inverted (it reported "empty" while holding items);
            // corrected to match the IInventory contract.
            for (int i = 0; i < this.inv.length; i++) {
                if (this.inv[i] != null && !this.inv[i].isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getStackInSlot(final int slot) {
            return this.inv[slot];
        }

        @Override
        public ItemStack removeStackFromSlot(final int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean hasCustomName() {
            return false;
        }

        @Override
        public boolean isItemValidForSlot(final int slot, final ItemStack stack) {
            if (stack.getItem() instanceof ICraftingPatternItem) {
                final ICraftingPatternDetails details = ((ICraftingPatternItem) stack.getItem())
                        .getPatternForItem(stack, TileEntityFluidCrafter.this.getWorld());
                return details != null && details.isCraftable();
            }
            return false;
        }

        @Override
        public boolean isUsableByPlayer(final EntityPlayer player) {
            return true;
        }

        @Override
        public void markDirty() {
        }

        @Override
        public void openInventory(final EntityPlayer player) {
        }

        public void readFromNBT(final NBTTagCompound tagCompound) {
            final NBTTagList tagList = tagCompound.getTagList("Inventory", 10);
            for (int i = 0; i < tagList.tagCount(); i++) {
                final NBTTagCompound tag = tagList.getCompoundTagAt(i);
                final byte slot = tag.getByte("Slot");
                if (slot >= 0 && slot < this.inv.length) {
                    this.inv[slot] = new ItemStack(tag);
                }
            }
        }

        @Override
        public void setInventorySlotContents(final int slot, final ItemStack stack) {
            this.inv[slot] = stack;
            if (stack != null && stack.getCount() > this.getInventoryStackLimit()) {
                stack.setCount(this.getInventoryStackLimit());
            }
            this.onContentsChanged();
            TileEntityFluidCrafter.this.update = true;
        }

        public void writeToNBT(final NBTTagCompound tagCompound) {
            final NBTTagList itemList = new NBTTagList();
            for (int i = 0; i < this.inv.length; i++) {
                final ItemStack stack = this.inv[i];
                if (stack != null) {
                    final NBTTagCompound tag = new NBTTagCompound();
                    tag.setByte("Slot", (byte) i);
                    stack.writeToNBT(tag);
                    itemList.appendTag(tag);
                }
            }
            tagCompound.setTag("Inventory", itemList);
        }

        @Override
        public int getField(final int id) {
            return 0;
        }

        @Override
        public void setField(final int id, final int value) {
        }

        @Override
        public int getFieldCount() {
            return 0;
        }

        @Override
        public void clear() {
        }

        @Override
        public ITextComponent getDisplayName() {
            return new TextComponentString(this.getName());
        }

        protected void onContentsChanged() {
            TileEntityFluidCrafter.this.saveData();
            TileEntityFluidCrafter.this.onInventoryChanged();
            if (TileEntityFluidCrafter.this.hasWorld()) {
                TileEntityFluidCrafter.this.updateBlock();
            }

            TileEntityFluidCrafter.this.postUpdateEvent();
        }
    }
}
