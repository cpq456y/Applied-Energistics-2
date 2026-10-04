package appeng.ext.aeadditions.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;

/** Ported from MEMonitorHandler.kt. */
public class MEMonitorHandler<T extends IAEStack<T>> implements IMEMonitor<T> {

    public final IMEInventoryHandler<T> internalHandler;
    public final IStorageChannel<T> chan;

    private final IItemList<T> cachedList;
    private final Map<IMEMonitorHandlerReceiver<T>, Object> listeners = new HashMap<>();
    protected boolean hasChanged = true;

    public MEMonitorHandler(final IMEInventoryHandler<T> internalHandler, final IStorageChannel<T> chan) {
        this.internalHandler = internalHandler;
        this.chan = chan;
        this.cachedList = chan.createList();
    }

    @Override
    public T injectItems(final T input, final Actionable mode, final IActionSource src) {
        if (mode == Actionable.SIMULATE) {
            return this.internalHandler.injectItems(input, mode, src);
        }

        return this.monitorDifference(input.copy(), this.internalHandler.injectItems(input, mode, src), false, src);
    }

    private T monitorDifference(final T original, final T leftOvers, final boolean extraction,
            final IActionSource src) {
        final T diff = original.copy();
        if (extraction) {
            diff.setStackSize(leftOvers == null ? 0 : -leftOvers.getStackSize());
        } else if (leftOvers != null) {
            diff.decStackSize(leftOvers.getStackSize());
        }

        if (diff.getStackSize() != 0L) {
            this.postChangesToListeners(Collections.singletonList(diff), src);
        }

        return leftOvers;
    }

    protected void postChangesToListeners(final Iterable<T> changes, final IActionSource src) {
        this.notifyListenersOfChange(changes, src);
    }

    protected void notifyListenersOfChange(final Iterable<T> diff, final IActionSource src) {
        this.hasChanged = true;
        final Iterator<Map.Entry<IMEMonitorHandlerReceiver<T>, Object>> i = this.listeners.entrySet().iterator();

        while (i.hasNext()) {
            final Map.Entry<IMEMonitorHandlerReceiver<T>, Object> o = i.next();
            final IMEMonitorHandlerReceiver<T> receiver = o.getKey();

            if (receiver.isValid(o.getValue())) {
                receiver.postChange(this, diff, src);
            } else {
                i.remove();
            }
        }
    }

    @Override
    public T extractItems(final T request, final Actionable mode, final IActionSource src) {
        if (mode == Actionable.SIMULATE) {
            return this.internalHandler.extractItems(request, mode, src);
        }

        return this.monitorDifference(request.copy(), this.internalHandler.extractItems(request, mode, src), true,
                src);
    }

    @Override
    public IItemList<T> getAvailableItems(final IItemList<T> out) {
        return this.internalHandler.getAvailableItems(out);
    }

    @Override
    public IStorageChannel<T> getChannel() {
        return this.internalHandler.getChannel();
    }

    @Override
    public AccessRestriction getAccess() {
        return this.internalHandler.getAccess();
    }

    @Override
    public boolean isPrioritized(final T input) {
        return this.internalHandler.isPrioritized(input);
    }

    @Override
    public boolean canAccept(final T input) {
        return this.internalHandler.canAccept(input);
    }

    @Override
    public int getPriority() {
        return this.internalHandler.getPriority();
    }

    @Override
    public int getSlot() {
        return this.internalHandler.getSlot();
    }

    @Override
    public boolean validForPass(final int i) {
        return this.internalHandler.validForPass(i);
    }

    @Override
    public void addListener(final IMEMonitorHandlerReceiver<T> listener, final Object verificationToken) {
        if (listener != null && verificationToken != null) {
            this.listeners.put(listener, verificationToken);
        }
    }

    @Override
    public void removeListener(final IMEMonitorHandlerReceiver<T> listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    @Override
    public IItemList<T> getStorageList() {
        if (this.hasChanged) {
            this.hasChanged = false;
            this.cachedList.resetStatus();
            return this.getAvailableItems(this.cachedList);
        }

        return this.cachedList;
    }
}
