package appeng.ext.aeadditions.item.storage;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.util.AEPartLocation;
import appeng.core.sync.GuiBridge;
import appeng.ext.aeadditions.me.storage.PortableCellGuiObject;
import appeng.api.storage.ICellInventoryHandler;
import appeng.ext.aeadditions.api.gas.IAEGasStack;
import appeng.api.storage.IStorageChannel;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.ext.aeadditions.util.StorageChannels;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.Platform;
import appeng.ext.aeadditions.api.AEAApi;
import appeng.ext.aeadditions.api.IPortableGasStorageCell;
import appeng.ext.aeadditions.inventory.ECGasFilterInventory;
import net.minecraftforge.items.wrapper.InvWrapper;
import appeng.items.contents.CellUpgrades;

/**
 * Portable gas cell, mirroring ToolPortableCell (same capacity, upgrades and battery) but bound to the gas channel. The GUI is the one shipped with the merged AE-Additions code.
 */
public class BasicGasPortableCell extends AEBasePoweredItem implements IPortableGasStorageCell, IItemGroup, IGuiItem {

    public BasicGasPortableCell() {
        super(AEConfig.instance().getPortableCellBattery());
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World w, final EntityPlayer player, @Nonnull final EnumHand hand) {
        Platform.openGUI(player, null, AEPartLocation.INTERNAL, GuiBridge.GUI_PORTABLE_GAS_CELL);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public IGuiItemObject getGuiObject(final ItemStack is, final World w, final BlockPos pos) {
        return new PortableCellGuiObject(is);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(final ItemStack stack, final World world, final List<String> lines,
            final ITooltipFlag advancedTooltips) {
        super.addCheckedInformation(stack, world, lines, advancedTooltips);

        final ICellInventoryHandler<IAEGasStack> cdi = AEApi.instance()
                .registries()
                .cell()
                .getCellInventory(stack, null, StorageChannels.GAS);

        AEApi.instance().client().addCellInformation(cdi, lines);
    }

    @Override
    public int getBytes(final ItemStack cellItem) {
        return 512;
    }

    @Override
    public int getBytesPerType(final ItemStack cellItem) {
        return 8;
    }

    @Override
    public int getTotalTypes(final ItemStack cellItem) {
        return 27;
    }

    @Override
    public boolean isBlackListed(final ItemStack cellItem, final IAEGasStack requestedAddition) {
        return false;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(final ItemStack i) {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return 0.5;
    }

    @Override
    public IStorageChannel<IAEGasStack> getChannel() {
        return StorageChannels.GAS;
    }

    @Override
    public String getUnlocalizedGroupName(final Set<ItemStack> others, final ItemStack is) {
        return GuiText.StorageCells.getUnlocalized();
    }

    @Override
    public boolean isEditable(final ItemStack is) {
        return true;
    }

    @Override
    public IItemHandler getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public IItemHandler getConfigInventory(final ItemStack is) {
        return new InvWrapper(new ECGasFilterInventory("configFluidCell", 63, is));
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        final String fz = Platform.openNbtData(is).getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (final Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
        Platform.openNbtData(is).setString("FuzzyMode", fzMode.name());
    }

    @Override
    public boolean shouldCauseReequipAnimation(final ItemStack oldStack, final ItemStack newStack, final boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public boolean hasPower(final EntityPlayer player, final double amount, final ItemStack is) {
        return this.getAECurrentPower(is) >= amount;
    }

    @Override
    public boolean usePower(final EntityPlayer player, final double amount, final ItemStack is) {
        this.extractAEPower(is, amount, Actionable.MODULATE);
        return true;
    }
}