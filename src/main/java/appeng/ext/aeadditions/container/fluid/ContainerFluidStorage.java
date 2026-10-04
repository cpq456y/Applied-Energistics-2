package appeng.ext.aeadditions.container.fluid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.ext.aeadditions.api.IPortableFluidStorageCell;
import appeng.ext.aeadditions.api.IWirelessFluidTermHandler;
import appeng.ext.aeadditions.container.ContainerStorage;
import appeng.ext.aeadditions.container.StorageType;
import appeng.ext.aeadditions.util.AEUtils;
import appeng.ext.aeadditions.util.FluidHelper;
import appeng.ext.aeadditions.util.PlayerSource;
import org.apache.commons.lang3.tuple.Pair;

/** Ported from ContainerFluidStorage.kt. */
public class ContainerFluidStorage extends ContainerStorage {

    public ContainerFluidStorage(final EntityPlayer player, final EnumHand hand) {
        super(StorageType.FLUID, player, hand);
    }

    public ContainerFluidStorage(final IMEMonitor<IAEFluidStack> monitor, final EntityPlayer player,
            final IPortableFluidStorageCell storageCell, final EnumHand hand) {
        super(StorageType.FLUID, monitor, player, storageCell, hand);
    }

    public ContainerFluidStorage(final IMEMonitor<IAEFluidStack> monitor, final EntityPlayer player,
            final IWirelessFluidTermHandler handler, final EnumHand hand) {
        super(StorageType.FLUID, monitor, player, handler, hand);
    }

    public ContainerFluidStorage(final IMEMonitor<IAEFluidStack> monitor, final EntityPlayer player,
            final EnumHand hand) {
        super(StorageType.FLUID, monitor, player, hand);
    }

    @Override
    public void doWork() {
        final net.minecraft.item.ItemStack secondSlot = this.inventory.getStackInSlot(1);
        if (secondSlot != null && !secondSlot.isEmpty()
                && secondSlot.getCount() >= secondSlot.getMaxStackSize()) {
            return;
        }
        net.minecraft.item.ItemStack container = this.inventory.getStackInSlot(0);
        if (!FluidHelper.isFluidContainer(container)) {
            return;
        }
        if (this.monitor == null) {
            return;
        }
        container = container.copy();
        container.setCount(1);
        if (FluidHelper.isDrainableFilledContainer(container)) {
            final FluidStack containerFluid = FluidHelper.getFluidFromContainer(container);

            // Tries to inject fluid to network.
            final IAEFluidStack notInjected = this.monitor.injectItems(AEUtils.createFluidStack(containerFluid),
                    Actionable.SIMULATE, new PlayerSource(this.player, null));
            if (notInjected != null) {
                return;
            }
            final net.minecraft.item.ItemStack handItem = this.player.getHeldItem(this.hand);
            if (this.handler != null) {
                if (!this.handler.hasPower(this.player, 20.0, handItem)) {
                    return;
                }
                this.handler.usePower(this.player, 20.0, handItem);
            } else if (this.storageCell != null) {
                if (!this.storageCell.hasPower(this.player, 20.0, handItem)) {
                    return;
                }
                this.storageCell.usePower(this.player, 20.0, handItem);
            }
            final Pair<Integer, net.minecraft.item.ItemStack> drainedContainer = FluidHelper
                    .drainStack(container, containerFluid);
            if (this.fillSecondSlot(drainedContainer.getRight())) {
                this.monitor.injectItems(AEUtils.createFluidStack(containerFluid), Actionable.MODULATE,
                        new PlayerSource(this.player, null));
                this.decreaseFirstSlot();
            }
        } else if (FluidHelper.isFillableContainerWithRoom(container)) {
            if (this.selectedFluid == null) {
                return;
            }
            final int capacity = FluidHelper.getCapacity(container, this.selectedFluid);
            // Tries to simulate the extraction of fluid from storage.
            final IAEFluidStack result = this.monitor.extractItems(
                    AEUtils.createFluidStack(this.selectedFluid, capacity), Actionable.SIMULATE,
                    new PlayerSource(this.player, null));

            // Calculates the amount of fluid to fill container with.
            final int proposedAmount = result == null ? 0
                    : (int) Math.min((long) capacity, result.getStackSize());
            if (proposedAmount == 0) {
                return;
            }

            // Tries to fill the container with fluid.
            final Pair<Integer, net.minecraft.item.ItemStack> filledContainer = FluidHelper.fillStack(container,
                    new FluidStack(this.selectedFluid, proposedAmount));

            // Moves it to second slot and commits extraction to grid.
            if (this.fillSecondSlot(filledContainer.getRight())) {
                this.monitor.extractItems(
                        AEUtils.createFluidStack(this.selectedFluid, filledContainer.getLeft().longValue()),
                        Actionable.MODULATE, new PlayerSource(this.player, null));
                this.decreaseFirstSlot();

                // The Kotlin original used the kotlin.collections Iterable.contains extension; IItemList has
                // no such member, so the same equals-based scan is written out explicitly.
                boolean stillStored = false;
                final IAEFluidStack fullStack = AEUtils.createFluidStack(this.selectedFluid, capacity);
                for (final IAEFluidStack stored : this.monitor.getStorageList()) {
                    if (stored.equals(fullStack)) {
                        stillStored = true;
                        break;
                    }
                }
                if (!stillStored) {
                    this.selectedFluid = null;
                }
            }
        }
    }
}
