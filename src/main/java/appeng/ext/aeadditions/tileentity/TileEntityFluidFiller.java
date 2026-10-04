package appeng.ext.aeadditions.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.tuple.Pair;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.ext.aeadditions.api.IECTileEntity;
import appeng.ext.aeadditions.container.IUpgradeable;
import appeng.ext.aeadditions.container.fluid.ContainerFluidFiller;
import appeng.ext.aeadditions.gridblock.ECFluidGridBlock;
import appeng.ext.aeadditions.gui.fluid.GuiFluidFiller;
import appeng.ext.aeadditions.gui.widget.fluid.IFluidSlotListener;
import appeng.ext.aeadditions.inventory.CraftingUpgradeInventory;
import appeng.ext.aeadditions.inventory.IInventoryListener;
import appeng.ext.aeadditions.network.IGuiProvider;
import appeng.ext.aeadditions.network.packet.PacketFluidFillerSlotUpdate;
import appeng.ext.aeadditions.registries.BlockEnum;
import appeng.ext.aeadditions.util.AEUtils;
import appeng.ext.aeadditions.util.FluidHelper;
import appeng.ext.aeadditions.util.MachineSource;
import appeng.ext.aeadditions.util.NetworkUtil;
import appeng.ext.aeadditions.util.StorageChannels;

/** Ported from TileEntityFluidFiller.kt. */
public class TileEntityFluidFiller extends TileBase implements IActionHost, ICraftingProvider, IECTileEntity,
        IMEMonitorHandlerReceiver<IAEFluidStack>, IListenerTile, ITickable, IGuiProvider, IInventoryListener,
        IFluidSlotListener, IUpgradeable {

    private final ECFluidGridBlock gridBlock;
    private IGridNode node = null;
    public List<Fluid> fluids = new ArrayList<>();

    public boolean beingBroken = false;
    public int speedState = 0;
    public Fluid selectedFluid = null;
    public CraftingUpgradeInventory upgradeInventory = new CraftingUpgradeInventory(this, BlockEnum.FLUIDCRAFTER, 3) {
        @Override
        public void onContentsChanged() {
            TileEntityFluidFiller.this.saveData();
            super.onContentsChanged();
        }
    };

    public ItemStack containerItem = new ItemStack(Items.BUCKET);
    public ItemStack returnStack = null;
    public int ticksToFinish = 0;
    private boolean isFirstGetGridNode = true;
    private final Item encodedPattern = AEApi.instance().definitions().items().encodedPattern().maybeItem()
            .orElse(null);

    public Map<ICraftingPatternDetails, FluidStack> patternFluids = new HashMap<>();

    public TileEntityFluidFiller() {
        this.gridBlock = new ECFluidGridBlock(this);
    }

    @MENetworkEventSubscribe
    public void cellUpdate(final MENetworkCellArrayUpdate event) {
        final IStorageGrid storage = this.getStorageGrid();
        if (storage != null) {
            this.postChange(storage.getInventory(StorageChannels.FLUID), null, null);
        }
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
        return AECableType.DENSE_SMART;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public IGridNode getGridNode(final AEPartLocation location) {
        if (FMLCommonHandler.instance().getSide().isClient() && (this.world == null || this.world.isRemote)) {
            return null;
        }
        if (this.isFirstGetGridNode) {
            this.isFirstGetGridNode = false;
            this.getActionableNode().updateState();
            final IStorageGrid storage = this.getStorageGrid();
            storage.getInventory(StorageChannels.FLUID).addListener(this, null);
        }
        return this.node;
    }

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    private ItemStack getPattern(final ItemStack emptyContainer, final ItemStack filledContainer) {
        final NBTTagList in = new NBTTagList();
        final NBTTagList out = new NBTTagList();
        in.appendTag(emptyContainer.writeToNBT(new NBTTagCompound()));
        out.appendTag(filledContainer.writeToNBT(new NBTTagCompound()));
        final NBTTagCompound itemTag = new NBTTagCompound();
        itemTag.setTag("in", in);
        itemTag.setTag("out", out);
        itemTag.setBoolean("crafting", false);
        final ItemStack pattern = new ItemStack(this.encodedPattern);
        pattern.setTagCompound(itemTag);
        return pattern;
    }

    @Override
    public double getPowerUsage() {
        return 1.0;
    }

    private IStorageGrid getStorageGrid() {
        this.node = this.getGridNode(AEPartLocation.INTERNAL);
        if (this.node == null) {
            return null;
        }
        final IGrid grid = this.node.getGrid();
        if (grid == null) {
            return null;
        }
        return grid.getCache(IStorageGrid.class);
    }

    @Override
    public boolean isBusy() {
        return this.returnStack != null && !this.returnStack.isEmpty();
    }

    @Override
    public boolean isValid(final Object verificationToken) {
        return true;
    }

    @Override
    public void onListUpdate() {
    }

    @Override
    public void postChange(final IBaseMonitor<IAEFluidStack> monitor, final Iterable<IAEFluidStack> change,
            final IActionSource actionSource) {
        final List<Fluid> oldFluids = new ArrayList<>(this.fluids);
        boolean mustUpdate = false;
        this.fluids.clear();
        for (final IAEFluidStack fluid : ((IMEMonitor<IAEFluidStack>) monitor).getStorageList()) {
            if (!oldFluids.contains(fluid.getFluid())) {
                mustUpdate = true;
            } else {
                oldFluids.remove(fluid.getFluid());
            }
            this.fluids.add(fluid.getFluid());
        }
        if (!(oldFluids.isEmpty() && !mustUpdate)) {
            if (this.getGridNode(AEPartLocation.INTERNAL) != null
                    && this.getGridNode(AEPartLocation.INTERNAL).getGrid() != null) {
                this.getGridNode(AEPartLocation.INTERNAL).getGrid().postEvent(
                        new MENetworkCraftingPatternChange(this, this.getGridNode(AEPartLocation.INTERNAL)));
            }
        }
    }

    public void postUpdateEvent() {
        if (this.getGridNode(AEPartLocation.INTERNAL) != null
                && this.getGridNode(AEPartLocation.INTERNAL).getGrid() != null) {
            this.getGridNode(AEPartLocation.INTERNAL).getGrid()
                    .postEvent(new MENetworkCraftingPatternChange(this, this.getGridNode(AEPartLocation.INTERNAL)));
        }
    }

    @MENetworkEventSubscribe
    public void powerUpdate(final MENetworkPowerStatusChange event) {
        final IStorageGrid storage = this.getStorageGrid();
        if (storage != null) {
            this.postChange(storage.getInventory(StorageChannels.FLUID), null, null);
        }
    }

    @Override
    public void provideCrafting(final ICraftingProviderHelper craftingTracker) {
        this.patternFluids.clear();
        if (this.selectedFluid == null) {
            return;
        }
        final FluidStack fluidStack = new FluidStack(this.selectedFluid, 1);

        final Fluid fluid = fluidStack.getFluid();
        if (fluid == null) {
            return;
        }
        final int maxCapacity = FluidHelper.getCapacity(this.containerItem);
        if (maxCapacity == 0) {
            return;
        }
        final Pair<Integer, ItemStack> filled = FluidHelper.fillStack(this.containerItem.copy(),
                new FluidStack(fluid, maxCapacity));
        if (filled.getRight() == null) {
            return;
        }
        final ItemStack pattern = this.getPattern(this.containerItem, filled.getRight());
        final ICraftingPatternItem patter = (ICraftingPatternItem) pattern.getItem();
        final ICraftingPatternDetails details = patter.getPatternForItem(pattern, this.world);
        if (details == null) {
            return;
        }
        this.patternFluids.put(details, new FluidStack(fluid, filled.getLeft()));
        craftingTracker.addCraftingOption(this, details);
    }

    /** This is that if a player breaks the block, we finish crafting so it's not stuck in limbo. */
    public void finishCrafting() {
        this.beingBroken = true;
        if (this.returnStack != null && !this.returnStack.isEmpty()) {
            this.injectCraftedItems();
        }
    }

    @Override
    public boolean pushPattern(final ICraftingPatternDetails patternDetails, final InventoryCrafting table) {
        if ((this.returnStack != null && !this.returnStack.isEmpty()) || this.beingBroken) {
            return false;
        }
        final ItemStack filled = patternDetails.getCondensedOutputs()[0].getDefinition();
        if (!this.patternFluids.containsKey(patternDetails)) {
            return false;
        }
        final FluidStack fluid = this.patternFluids.get(patternDetails);
        final IStorageGrid storage = this.getStorageGrid();
        if (storage == null || fluid == null) {
            return false;
        }
        final IAEFluidStack fluidStack = AEUtils.createFluidStack(new FluidStack(fluid.getFluid(),
                FluidHelper.getCapacity(patternDetails.getCondensedInputs()[0].getDefinition())));
        final IAEFluidStack extracted = storage.getInventory(StorageChannels.FLUID).extractItems(fluidStack.copy(),
                Actionable.SIMULATE, new MachineSource(this));
        if (extracted == null || extracted.getStackSize() != fluidStack.getStackSize()) {
            return false;
        }
        storage.getInventory(StorageChannels.FLUID).extractItems(fluidStack, Actionable.MODULATE,
                new MachineSource(this));
        this.returnStack = filled;
        this.ticksToFinish = 40 - (this.speedState * 12);
        return true;
    }

    @Override
    public void readFromNBT(final NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        if (tagCompound.hasKey("container")) {
            this.containerItem = new ItemStack(tagCompound.getCompoundTag("container"));
        } else if (tagCompound.hasKey("isContainerEmpty") && tagCompound.getBoolean("isContainerEmpty")) {
            this.containerItem = null;
        }

        if (tagCompound.hasKey("selectedFluid")) {
            this.selectedFluid = FluidRegistry.getFluid(tagCompound.getString("selectedFluid"));
        }

        if (tagCompound.hasKey("return")) {
            this.returnStack = new ItemStack(tagCompound.getCompoundTag("return"));
        } else if (tagCompound.hasKey("isReturnEmpty") && tagCompound.getBoolean("isReturnEmpty")) {
            this.returnStack = null;
        }
        this.upgradeInventory.readFromNBT(tagCompound.getTagList("upgradeInventory", 10));
        this.onInventoryChanged();
        if (tagCompound.hasKey("time")) {
            this.ticksToFinish = tagCompound.getInteger("time");
        }
        if (this.hasWorld()) {
            final IGridNode node = this.getGridNode(AEPartLocation.INTERNAL);
            if (tagCompound.hasKey("nodes") && node != null) {
                node.loadFromNBT("node0", tagCompound.getCompoundTag("nodes"));
                node.updateState();
            }
        }
    }

    @Override
    public void registerListener() {
        final IStorageGrid storage = this.getStorageGrid();
        if (storage == null) {
            return;
        }
        final IMEMonitor<IAEFluidStack> fluidInventory = storage.getInventory(StorageChannels.FLUID);
        this.postChange(fluidInventory, null, null);
        fluidInventory.addListener(this, null);
    }

    @Override
    public void removeListener() {
        final IStorageGrid storage = this.getStorageGrid();
        if (storage == null) {
            return;
        }
        final IMEMonitor<IAEFluidStack> fluidInventory = storage.getInventory(StorageChannels.FLUID);
        fluidInventory.removeListener(this);
    }

    @Override
    public void securityBreak() {
        // TODO: Find out what func_147480_a is
    }

    @Override
    public void update() {
        if (!this.hasWorld()) {
            return;
        }
        if (this.ticksToFinish > 0) {
            this.ticksToFinish = this.ticksToFinish - 1;
        }
        if (this.ticksToFinish <= 0 && this.returnStack != null && !this.returnStack.isEmpty()) {
            this.injectCraftedItems();
        }
    }

    private void injectCraftedItems() {
        final IStorageGrid storage = this.getStorageGrid();
        if (storage == null) {
            return;
        }
        final IAEItemStack toInject = StorageChannels.ITEM.createStack(this.returnStack);
        if (storage.getInventory(StorageChannels.ITEM).canAccept(toInject.copy())) {
            final IAEItemStack nodAdded = storage.getInventory(StorageChannels.ITEM).injectItems(toInject.copy(),
                    Actionable.SIMULATE, new MachineSource(this));
            if (nodAdded == null) {
                storage.getInventory(StorageChannels.ITEM).injectItems(toInject, Actionable.MODULATE,
                        new MachineSource(this));
                this.returnStack = null;
            }
        }
    }

    @Override
    public void updateGrid(final IGrid oldGrid, final IGrid newGrid) {
        if (oldGrid != null) {
            final IStorageGrid storage = oldGrid.getCache(IStorageGrid.class);
            if (storage != null) {
                storage.getInventory(StorageChannels.FLUID).removeListener(this);
            }
        }
        if (newGrid != null) {
            final IStorageGrid storage = newGrid.getCache(IStorageGrid.class);
            if (storage != null) {
                storage.getInventory(StorageChannels.FLUID).addListener(this, null);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        if (this.containerItem != null) {
            tagCompound.setTag("container", this.containerItem.writeToNBT(new NBTTagCompound()));
        } else {
            tagCompound.setBoolean("isContainerEmpty", true);
        }

        if (this.selectedFluid != null) {
            tagCompound.setString("selectedFluid", this.selectedFluid.getName());
        } else {
            tagCompound.removeTag("selectedFluid");
        }

        tagCompound.setTag("upgradeInventory", this.upgradeInventory.writeToNBT());

        if (this.returnStack != null && !this.returnStack.isEmpty()) {
            tagCompound.setTag("return", this.returnStack.writeToNBT(new NBTTagCompound()));
        } else {
            tagCompound.setBoolean("isReturnEmpty", true);
        }
        tagCompound.setInteger("time", this.ticksToFinish);
        if (!this.hasWorld()) {
            return tagCompound;
        }
        final IGridNode node = this.getGridNode(AEPartLocation.INTERNAL);
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
        return new GuiFluidFiller(player, this);
    }

    @Override
    public Container getServerGuiElement(final EntityPlayer player, final Object... args) {
        return new ContainerFluidFiller(player.inventory, this);
    }

    @Override
    public void onInventoryChanged() {
        this.speedState = 0;
        for (int i = 0; i < this.upgradeInventory.getSizeInventory(); i++) {
            final ItemStack currentStack = this.upgradeInventory.getStackInSlot(i);
            if (currentStack != null) {
                if (AEApi.instance().definitions().materials().cardSpeed().isSameAs(currentStack)) {
                    this.speedState++;
                }
            }
        }

        this.saveData();
    }

    @Override
    public void setFluid(final int index, final Fluid fluid, final EntityPlayer player) {
        this.selectedFluid = fluid;

        if (this.hasWorld()) {
            this.updateBlock();
        }

        this.postUpdateEvent();

        NetworkUtil.sendToPlayer(new PacketFluidFillerSlotUpdate(this.selectedFluid), player);
        this.saveData();
    }

    @Override
    public IInventory getUpgradeInventory() {
        return this.upgradeInventory;
    }

    public void syncClientGui(final EntityPlayer player) {
        NetworkUtil.sendToPlayer(new PacketFluidFillerSlotUpdate(this.selectedFluid), player);
    }
}
