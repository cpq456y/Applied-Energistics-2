package appeng.ext.aeadditions.integration.opencomputers;

import li.cil.oc.api.CreativeTab;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.item.HostAware;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.internal.Drone;
import li.cil.oc.api.internal.Robot;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Optional;

import appeng.ext.aeadditions.integration.Integration;
import appeng.ext.aeadditions.item.ItemECBase;

/** Ported from UpgradeItemAEBase.kt. */
@Optional.InterfaceList({
        @Optional.Interface(iface = "li.cil.oc.api.driver.item.HostAware", modid = "opencomputers", striprefs = true),
        @Optional.Interface(iface = "li.cil.oc.api.driver.EnvironmentProvider", modid = "opencomputers", striprefs = true) })
public abstract class UpgradeItemAEBase extends ItemECBase implements HostAware, EnvironmentProvider {

    @Optional.Method(modid = "opencomputers")
    @Override
    public boolean worksWith(final ItemStack itemStack, final Class<? extends EnvironmentHost> host) {
        return this.worksWith(itemStack) && host != null
                && (Robot.class.isAssignableFrom(host) || Drone.class.isAssignableFrom(host));
    }

    @Optional.Method(modid = "opencomputers")
    @Override
    public boolean worksWith(final ItemStack itemStack) {
        return itemStack != null && itemStack.getItem() == this;
    }

    @Optional.Method(modid = "opencomputers")
    @Override
    public ManagedEnvironment createEnvironment(final ItemStack itemStack, final EnvironmentHost host) {
        if (itemStack != null && itemStack.getItem() == this && this.worksWith(itemStack, host.getClass())) {
            try {
                return CompleteHelper.getCompleteUpgradeAE(host);
            } catch (final Throwable e) {
                return new UpgradeAE(host);
            }
        }

        return null;
    }

    @Override
    public EnumRarity getRarity(final ItemStack stack) {
        switch (stack.getItemDamage()) {
            case 0:
                return EnumRarity.RARE;
            case 1:
                return EnumRarity.UNCOMMON;
            default:
                return super.getRarity(stack);
        }
    }

    @Optional.Method(modid = "opencomputers")
    public CreativeTabs getOCCreativeTab() {
        return CreativeTab.instance;
    }

    @Override
    public CreativeTabs[] getCreativeTabs() {
        if (Integration.Mods.OPENCOMPUTERS.isEnabled()) {
            return new CreativeTabs[] { this.getCreativeTab(), CreativeTab.instance };
        }

        return super.getCreativeTabs();
    }

    @Optional.Method(modid = "opencomputers")
    @Override
    public String slot(final ItemStack itemStack) {
        return Slot.Upgrade;
    }

    @Optional.Method(modid = "opencomputers")
    @Override
    public int tier(final ItemStack itemStack) {
        switch (itemStack.getItemDamage()) {
            case 0:
                return 2;
            case 1:
                return 1;
            default:
                return 0;
        }
    }

    @Optional.Method(modid = "opencomputers")
    @Override
    public NBTTagCompound dataTag(final ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        final NBTTagCompound nbt = itemStack.getTagCompound();

        if (!nbt.hasKey("oc:data")) {
            nbt.setTag("oc:data", new NBTTagCompound());
        }

        return nbt.getCompoundTag("oc:data");
    }

    @Optional.Method(modid = "opencomputers")
    @Override
    public Class<?> getEnvironment(final ItemStack itemStack) {
        if (itemStack != null && itemStack.getItem() == this) {
            try {
                return CompleteHelper.getCompleteUpgradeAEClass();
            } catch (final Throwable e) {
                return UpgradeAE.class;
            }
        }

        return null;
    }
}
