package appeng.helper;


import appeng.api.storage.data.IAEItemStack;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketItemConfigSlot;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.item.AEItemStack;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ItemConfigSyncHelper {
    private final AppEngInternalAEInventory inv;
    private final IAEItemStack[] cache;
    private final int idOffset;

    public ItemConfigSyncHelper(final AppEngInternalAEInventory inv, final int idOffset) {
        this.inv = inv;
        this.cache = new IAEItemStack[inv.getSlots()];
        this.idOffset = idOffset;
    }

    public void sendFull(final Iterable<IContainerListener> listeners) {
        this.sendDiffMap(this.createDiffMap(true), listeners);
    }

    public void sendDiff(final Iterable<IContainerListener> listeners) {
        this.sendDiffMap(this.createDiffMap(false), listeners);
    }

    public void readPacket(final Map<Integer, IAEItemStack> data) {
        for (int i = 0; i < this.inv.getSlots(); ++i) {
            if (data.containsKey(i + this.idOffset)) {
                IAEItemStack stack = data.get(i + this.idOffset);
                if (stack != null) {
                    this.inv.setStackInSlot(i, stack.getDefinition());
                } else {
                    this.inv.setStackInSlot(i, ItemStack.EMPTY);
                }
            }
        }
    }

    private void sendDiffMap(final Map<Integer, IAEItemStack> data, final Iterable<IContainerListener> listeners) {
        if (data.isEmpty()) {
            return;
        }

        for (final IContainerListener l : listeners) {
            if (l instanceof EntityPlayerMP) {
                NetworkHandler.instance().sendTo(new PacketItemConfigSlot(data), (EntityPlayerMP) l);
            }
        }
    }

    private final Map<Integer, IAEItemStack> createDiffMap(final boolean full) {
        final Map<Integer, IAEItemStack> ret = new HashMap<>();
        for (int i = 0; i < this.inv.getSlots(); ++i) {
            ItemStack currentStack = this.inv.getStackInSlot(i);
            IAEItemStack currentAE = currentStack.isEmpty() ? null : AEItemStack.fromItemStack(currentStack);
            
            if (full || !this.equalsSlot(i, currentAE)) {
                ret.put(i + this.idOffset, currentAE);
            }
            if (!full) {
                this.cache[i] = currentAE;
            }
        }
        return ret;
    }

    private final boolean equalsSlot(int slot, IAEItemStack stackA) {
        final IAEItemStack stackB = this.cache[slot];

        if (stackA == null && stackB == null) {
            return true;
        }
        if (stackA == null || stackB == null) {
            return false;
        }

        // Compare item type and count
        return stackA.getStackSize() == stackB.getStackSize() &&
               net.minecraft.item.ItemStack.areItemStacksEqual(stackA.getDefinition(), stackB.getDefinition());
    }
}
