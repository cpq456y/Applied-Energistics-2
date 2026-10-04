package appeng.ext.aeadditions.item;

import cofh.redstoneflux.api.IEnergyContainerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Optional;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;

/** Ported from PowerItem.kt. */
@Optional.Interface(iface = "cofh.redstoneflux.api.IEnergyContainerItem", modid = "redstoneflux", striprefs = true)
public abstract class PowerItem extends ItemECBase implements IAEItemPowerStorage, IEnergyContainerItem {

    /** Replaces the Kotlin {@code abstract val MAX_POWER}. */
    public abstract double getMaxPower();

    @Override
    public double injectAEPower(final ItemStack itemStack, final double amt, final Actionable actionable) {
        if (itemStack == null) {
            return 0.0;
        }
        final NBTTagCompound tagCompound = this.ensureTagCompound(itemStack);
        final double currentPower = tagCompound.getDouble("power");
        final double toInject = Math.min(amt, this.getMaxPower() - currentPower);

        if (actionable == Actionable.MODULATE) {
            tagCompound.setDouble("power", currentPower + toInject);
        }

        return toInject;
    }

    @Override
    public double extractAEPower(final ItemStack itemStack, final double amt, final Actionable actionable) {
        if (itemStack == null) {
            return 0.0;
        }
        final NBTTagCompound tagCompound = this.ensureTagCompound(itemStack);
        final double currentPower = tagCompound.getDouble("power");
        final double toExtract = Math.min(amt, currentPower);

        if (actionable == Actionable.MODULATE) {
            tagCompound.setDouble("power", currentPower - toExtract);
        }

        return toExtract;
    }

    @Override
    public double getAEMaxPower(final ItemStack stack) {
        return this.getMaxPower();
    }

    @Override
    public double getAECurrentPower(final ItemStack itemStack) {
        if (itemStack == null) {
            return 0.0;
        }

        final NBTTagCompound tagCompound = this.ensureTagCompound(itemStack);
        return tagCompound.getDouble("power");
    }

    @Optional.Method(modid = "redstoneflux")
    @Override
    public int receiveEnergy(final ItemStack container, final int maxReceive, final boolean simulate) {
        if (container == null) {
            return 0;
        }
        if (simulate) {
            final double current = PowerUnits.AE.convertTo(PowerUnits.RF, this.getAECurrentPower(container));
            final double max = PowerUnits.AE.convertTo(PowerUnits.RF, this.getAEMaxPower(container));

            if (max - current >= maxReceive) {
                return maxReceive;
            } else {
                return (int) (max - current);
            }
        }

        final double currentAEPower = this.getAECurrentPower(container);

        if (currentAEPower < this.getAEMaxPower(container)) {
            return (int) PowerUnits.AE.convertTo(PowerUnits.RF,
                    this.injectAEPower(container, PowerUnits.RF.convertTo(PowerUnits.AE, (double) maxReceive),
                            Actionable.MODULATE));
        }

        return 0;
    }

    @Optional.Method(modid = "redstoneflux")
    @Override
    public int extractEnergy(final ItemStack container, final int maxExtract, final boolean simulate) {
        if (container == null) {
            return 0;
        }

        if (simulate) {
            return this.getEnergyStored(container) >= maxExtract ? maxExtract : this.getEnergyStored(container);
        }

        return (int) PowerUnits.AE.convertTo(PowerUnits.RF, this.extractAEPower(container,
                PowerUnits.RF.convertTo(PowerUnits.AE, (double) maxExtract), Actionable.MODULATE));
    }

    @Optional.Method(modid = "redstoneflux")
    @Override
    public int getEnergyStored(final ItemStack container) {
        return (int) PowerUnits.AE.convertTo(PowerUnits.RF, this.getAECurrentPower(container));
    }

    @Optional.Method(modid = "redstoneflux")
    @Override
    public int getMaxEnergyStored(final ItemStack container) {
        return (int) PowerUnits.AE.convertTo(PowerUnits.RF, this.getAEMaxPower(container));
    }

    public NBTTagCompound ensureTagCompound(final ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        return itemStack.getTagCompound();
    }
}
