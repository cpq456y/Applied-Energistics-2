package appeng.ext.aeadditions.integration.mekanism;

import java.lang.reflect.Field;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IItemList;
import appeng.ext.aeadditions.api.IExternalGasStorageHandler;
import appeng.ext.aeadditions.api.gas.IAEGasStack;
import appeng.ext.aeadditions.util.GasUtil;
import appeng.ext.aeadditions.util.StorageChannels;

/** Ported from HandlerMekanismGasTank.kt (Kotlin {@code object}, kept as a singleton). */
public final class HandlerMekanismGasTank implements IExternalGasStorageHandler {

    public static final HandlerMekanismGasTank INSTANCE = new HandlerMekanismGasTank();

    public static final Class<?> clazz = lookupClass();

    private static Class<?> lookupClass() {
        try {
            return Class.forName("mekanism.common.tile.TileEntityGasTank");
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private HandlerMekanismGasTank() {
    }

    @Override
    public boolean canHandle(final TileEntity tile, final EnumFacing d, final IActionSource mySrc) {
        return tile != null && tile.getClass() == clazz;
    }

    @Override
    public IMEInventory<IAEGasStack> getInventory(final TileEntity tile, final EnumFacing d,
            final IActionSource src) {
        if (tile == null) {
            return null;
        }

        final GasTank tank = this.getGasTank(tile);
        if (tank == null) {
            return null;
        }

        return new Inventory(tank);
    }

    public GasTank getGasTank(final TileEntity tile) {
        try {
            final Field tank = clazz.getField("gasTank");

            if (tank != null) {
                return (GasTank) tank.get(tile);
            }
        } catch (final Throwable e) {
            // Do nothing
        }

        return null;
    }

    /** Nested inventory view over a Mekanism gas tank. */
    public static class Inventory implements IMEInventory<IAEGasStack> {

        public final GasTank tank;

        public Inventory(final GasTank tank) {
            this.tank = tank;
        }

        @Override
        public IAEGasStack injectItems(final IAEGasStack stackType, final Actionable actionable,
                final IActionSource actionSource) {
            final Object rawStack = stackType == null ? null : stackType.getGasStack();
            if (!(rawStack instanceof GasStack)) {
                return null;
            }
            final GasStack gasStack = (GasStack) rawStack;

            if (this.tank.canReceive(gasStack.getGas())) {
                final int accepted = this.tank.receive(gasStack, actionable == Actionable.MODULATE);

                if (accepted == stackType.getStackSize()) {
                    return null;
                }

                final IAEGasStack returnStack = stackType.copy();

                returnStack.setStackSize(stackType.getStackSize() - accepted);

                return returnStack;
            }

            return stackType;
        }

        @Override
        public IAEGasStack extractItems(final IAEGasStack stackType, final Actionable actionable,
                final IActionSource actionSource) {
            final Object rawStack = stackType == null ? null : stackType.getGasStack();
            if (!(rawStack instanceof GasStack)) {
                return null;
            }
            final GasStack gasStack = (GasStack) rawStack;

            if (this.tank.canDraw(gasStack.getGas())) {
                final GasStack drawn = this.tank.draw(gasStack.amount, actionable == Actionable.MODULATE);

                return StorageChannels.GAS.createStack(drawn);
            }

            return null;
        }

        @Override
        public IItemList<IAEGasStack> getAvailableItems(final IItemList<IAEGasStack> itemList) {
            final GasStack gas = this.tank.getGas();

            if (gas != null && itemList != null) {
                itemList.add(GasUtil.createAEGasStack(gas));
            }

            return itemList;
        }

        @Override
        public IStorageChannel<IAEGasStack> getChannel() {
            return StorageChannels.GAS;
        }
    }
}
