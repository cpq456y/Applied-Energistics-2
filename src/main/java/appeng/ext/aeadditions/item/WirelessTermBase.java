package appeng.ext.aeadditions.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;

/** Ported from WirelessTermBase.kt. */
public abstract class WirelessTermBase extends PowerItem {

    private final double maxPower;

    protected WirelessTermBase() {
        this(1600000.0);
    }

    protected WirelessTermBase(final double maxPower) {
        this.maxPower = maxPower;
    }

    @Override
    public double getMaxPower() {
        return this.maxPower;
    }

    @Override
    public AccessRestriction getPowerFlow(final ItemStack itemStack) {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public double getDurabilityForDisplay(final ItemStack stack) {
        return 1 - this.getAECurrentPower(stack) / this.getMaxPower();
    }

    // Deliberately not @Override: this class does not implement the wireless-terminal handler
    // interfaces itself, but the inherited methods satisfy them for the concrete subclasses.

    public boolean canHandle(final ItemStack itemStack) {
        return itemStack != null && itemStack.getItem() == this;
    }

    public String getEncryptionKey(final ItemStack itemStack) {
        return this.ensureTagCompound(itemStack).getString("key");
    }

    public void setEncryptionKey(final ItemStack itemStack, final String encKey, final String name) {
        this.ensureTagCompound(itemStack).setString("key", encKey);
    }

    public boolean hasPower(final EntityPlayer player, final double amount, final ItemStack itemStack) {
        return this.getAECurrentPower(itemStack) >= amount;
    }

    public boolean usePower(final EntityPlayer player, final double amount, final ItemStack itemStack) {
        this.extractAEPower(itemStack, amount, Actionable.MODULATE);

        return true;
    }

    @Override
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }

        items.add(new ItemStack(this));
        final ItemStack itemStack = new ItemStack(this);
        this.injectAEPower(itemStack, this.getMaxPower(), Actionable.MODULATE);
        items.add(itemStack);
    }

    @Override
    public boolean showDurabilityBar(final ItemStack stack) {
        return true;
    }

    @Override
    public void addInformation(final ItemStack stack, final World worldIn, final List<String> tooltip,
            final ITooltipFlag flagIn) {
        final String encryptionKey = this.getEncryptionKey(stack);
        final double aeCurrentPower = this.getAECurrentPower(stack);
        tooltip.add(I18n.translateToLocal("gui.appliedenergistics2.StoredEnergy") + ": " + aeCurrentPower + " AE - "
                + Math.floor(aeCurrentPower / this.getMaxPower() * 1e4) / 1e2 + "%");
        tooltip.add(I18n.translateToLocal(
                !encryptionKey.isEmpty() ? "gui.appliedenergistics2.Linked" : "gui.appliedenergistics2.Unlinked"));
    }
}
