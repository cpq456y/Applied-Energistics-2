package appeng.ext.aeadditions.integration.mekanism.gas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.ext.aeadditions.api.gas.IAEGasStack;
import appeng.ext.aeadditions.util.GasUtil;
import appeng.ext.aeadditions.util.StorageChannels;

/** Ported from MEMonitorFluidGasWrapper.kt. */
public class MEMonitorFluidGasWrapper
        implements IMEMonitor<IAEFluidStack>, IMEMonitorHandlerReceiver<IAEGasStack> {

    private final IMEMonitor<IAEGasStack> gasMonitor;

    private final Map<IMEMonitorHandlerReceiver<IAEFluidStack>, Object> listeners = new HashMap<>();

    public MEMonitorFluidGasWrapper(final IMEMonitor<IAEGasStack> gasMonitor) {
        this.gasMonitor = gasMonitor;
    }

    public IMEMonitor<IAEGasStack> getGasMonitor() {
        return this.gasMonitor;
    }

    @Override
    public IAEFluidStack injectItems(final IAEFluidStack fluidStack, final Actionable actionable,
            final IActionSource actionSource) {
        return GasUtil.createAEFluidStack(this.gasMonitor
                .injectItems(GasUtil.createAEGasStack(fluidStack), actionable, actionSource));
    }

    @Override
    public IAEFluidStack extractItems(final IAEFluidStack fluidStack, final Actionable actionable,
            final IActionSource actionSource) {
        return GasUtil.createAEFluidStack(this.gasMonitor
                .extractItems(GasUtil.createAEGasStack(fluidStack), actionable, actionSource));
    }

    @Override
    public IItemList<IAEFluidStack> getAvailableItems(final IItemList<IAEFluidStack> itemList) {
        return GasUtil.createAEFluidItemList(
                this.gasMonitor.getAvailableItems(GasUtil.createAEGasItemList(itemList)));
    }

    @Override
    public IStorageChannel<IAEFluidStack> getChannel() {
        return StorageChannels.FLUID;
    }

    @Override
    public AccessRestriction getAccess() {
        return this.gasMonitor.getAccess();
    }

    @Override
    public boolean isPrioritized(final IAEFluidStack fluidStack) {
        return this.gasMonitor.isPrioritized(GasUtil.createAEGasStack(fluidStack));
    }

    @Override
    public boolean canAccept(final IAEFluidStack fluidStack) {
        return this.gasMonitor.canAccept(GasUtil.createAEGasStack(fluidStack));
    }

    @Override
    public int getPriority() {
        return this.gasMonitor.getPriority();
    }

    @Override
    public int getSlot() {
        return this.gasMonitor.getSlot();
    }

    @Override
    public boolean validForPass(final int i) {
        return this.gasMonitor.validForPass(i);
    }

    @Override
    public void addListener(final IMEMonitorHandlerReceiver<IAEFluidStack> listener,
            final Object verificationToken) {
        if (this.listeners.isEmpty()) {
            this.gasMonitor.addListener(this, null);
        }
        this.listeners.put(listener, verificationToken);
    }

    @Override
    public void removeListener(final IMEMonitorHandlerReceiver<IAEFluidStack> listener) {
        this.listeners.remove(listener);
        if (this.listeners.isEmpty()) {
            this.gasMonitor.removeListener(this);
        }
    }

    @Override
    public IItemList<IAEFluidStack> getStorageList() {
        return GasUtil.createAEFluidItemList(this.gasMonitor.getStorageList());
    }

    @Override
    public boolean isValid(final Object p0) {
        return true;
    }

    @Override
    public void postChange(final IBaseMonitor<IAEGasStack> baseMonitor, final Iterable<IAEGasStack> iterable,
            final IActionSource actionSource) {
        final List<IAEFluidStack> changes = new ArrayList<>();

        for (final IAEGasStack stack : iterable) {
            changes.add(GasUtil.createAEFluidStack(stack));
        }

        for (final IMEMonitorHandlerReceiver<IAEFluidStack> k : this.listeners.keySet()) {
            k.postChange(this, changes, actionSource);
        }
    }

    @Override
    public void onListUpdate() {
        for (final IMEMonitorHandlerReceiver<IAEFluidStack> k : this.listeners.keySet()) {
            k.onListUpdate();
        }
    }
}
