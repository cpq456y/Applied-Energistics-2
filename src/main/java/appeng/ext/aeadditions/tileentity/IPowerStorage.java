package appeng.ext.aeadditions.tileentity;

import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IAEPowerStorage;

/** Ported from IPowerStorage.kt. */
public interface IPowerStorage extends IAEPowerStorage {

    PowerInformation powerInformation = new PowerInformation();

    @Override
    default double getAECurrentPower() {
        return powerInformation.currentPower;
    }

    @Override
    default AccessRestriction getPowerFlow() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    default double getAEMaxPower() {
        return powerInformation.maxPower;
    }

    default void setMaxPower(final double power) {
        powerInformation.maxPower = power;
    }

    @Override
    default double injectAEPower(final double amt, final Actionable mode) {
        final double maxStore = powerInformation.maxPower - powerInformation.currentPower;

        final double notStored = maxStore - amt >= 0 ? 0.00 : amt - maxStore;

        if (mode == Actionable.MODULATE) {
            powerInformation.currentPower += amt - notStored;
        }

        return notStored;
    }

    @Override
    default boolean isAEPublicPowerStorage() {
        return true;
    }

    @Override
    default double extractAEPower(final double amount, final Actionable mode,
            final PowerMultiplier usePowerMultiplier) {
        final double toExtract = Math.min(amount, powerInformation.currentPower);
        if (mode == Actionable.MODULATE) {
            powerInformation.currentPower -= toExtract;
        }

        return toExtract;
    }

    default void readPowerFromNBT(final NBTTagCompound tag) {
        if (tag.hasKey("currentPowerBattery")) {
            powerInformation.currentPower = tag.getDouble("currentPowerBattery");
        }
    }

    default void writePowerToNBT(final NBTTagCompound tag) {
        tag.setDouble("currentPowerBattery", powerInformation.currentPower);
    }

    class PowerInformation {
        public double currentPower = 0.00;
        public double maxPower = 500.00;
    }
}
