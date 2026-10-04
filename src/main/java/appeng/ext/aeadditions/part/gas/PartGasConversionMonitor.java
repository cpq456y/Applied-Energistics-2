package appeng.ext.aeadditions.part.gas;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Optional;

import org.apache.commons.lang3.tuple.MutablePair;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.ext.aeadditions.api.IWrenchHandler;
import appeng.ext.aeadditions.api.gas.IAEGasStack;
import appeng.ext.aeadditions.models.PartModels;
import appeng.ext.aeadditions.util.AEUtils;
import appeng.ext.aeadditions.util.GasUtil;
import appeng.ext.aeadditions.util.MachineSource;
import appeng.ext.aeadditions.util.StorageChannels;
import appeng.ext.aeadditions.util.WrenchUtil;

/** Ported from PartGasConversionMonitor.kt. */
public class PartGasConversionMonitor extends PartGasStorageMonitor {

    @Override
    public boolean onActivate(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (this.isMekanismEnabled()) {
            return this.onActivateGas(player, hand, pos);
        }

        return false;
    }

    @Optional.Method(modid = "mekanism")
    public boolean wasActivated(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (player == null || player.world == null) {
            return true;
        }
        if (player.world.isRemote) {
            return true;
        }
        final ItemStack s = player.getHeldItem(hand);
        if (s == null) {
            if (this.locked) {
                return false;
            }
            if (this.fluid == null) {
                return true;
            }
            if (this.watcher != null) {
                this.watcher.remove(StorageChannels.GAS.createStack(this.fluid));
            }
            this.fluid = null;
            this.amount = 0L;
            if (this.getHost() != null) {
                this.getHost().markForUpdate();
            }
            return true;
        }
        final RayTraceResult rayTraceResult = new RayTraceResult(pos, this.getFacing(), this.getLocation().getPos());
        final IWrenchHandler wrenchHandler = WrenchUtil.getHandler(s, player, rayTraceResult, hand);
        if (wrenchHandler != null) {
            this.locked = !this.locked;
            wrenchHandler.wrenchUsed(s, player, rayTraceResult, hand);
            if (this.getHost() != null) {
                this.getHost().markForUpdate();
            }
            if (this.locked) {
                player.sendMessage(new TextComponentTranslation("chat.appliedenergistics2.isNowLocked"));
            } else {
                player.sendMessage(new TextComponentTranslation("chat.appliedenergistics2.isNowUnlocked"));
            }
            return true;
        }
        if (this.locked) {
            return false;
        }
        if (GasUtil.isFilled(s)) {
            if (this.fluid != null && this.watcher != null) {
                this.watcher.remove(StorageChannels.GAS.createStack(this.fluid));
            }
            final GasStack gas = GasUtil.getGasFromContainer(s);
            final net.minecraftforge.fluids.FluidStack fluidStack = GasUtil.getFluidStack(gas);
            this.fluid = fluidStack == null ? null : fluidStack.getFluid();
            if (this.watcher != null) {
                this.watcher.add(StorageChannels.GAS.createStack(this.fluid));
            }
            if (this.getHost() != null) {
                this.getHost().markForUpdate();
            }
            this.onStackChange();
            return true;
        }
        return false;
    }

    @Optional.Method(modid = "mekanism")
    @Override
    public boolean onActivateGas(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        final boolean b = this.wasActivated(player, hand, pos);
        if (b) {
            return b;
        }
        if (player == null || player.world == null || hand == null || pos == null) {
            return true;
        }
        if (player.world.isRemote) {
            return true;
        }
        final ItemStack s = player.getHeldItem(hand);
        final IMEMonitor<IAEGasStack> mon = this.getGasStorage();
        if (this.locked && s != null && !s.isEmpty() && mon != null) {
            final ItemStack s2 = s.copy();
            s2.setCount(1);
            if (GasUtil.isFilled(s2)) {
                final GasStack g = GasUtil.getGasFromContainer(s2);
                if (g == null) {
                    return true;
                }
                final IAEGasStack g1 = StorageChannels.GAS.createStack(g);
                final IAEGasStack not = mon.injectItems(g1.copy(), Actionable.SIMULATE, new MachineSource(this));
                if (mon.canAccept(g1) && (not == null || not.getStackSize() == 0L)) {
                    mon.injectItems(g1, Actionable.MODULATE, new MachineSource(this));
                    final MutablePair<Integer, ItemStack> empty1 = GasUtil.drainStack(s2, g);
                    final ItemStack empty = empty1.getRight();
                    if (empty != null && !empty.isEmpty()) {
                        this.dropItems(this.getHost().getTile().getWorld(),
                                this.getHost().getTile().getPos().offset(this.getFacing()), empty);
                    }
                    final ItemStack s3 = s.copy();
                    s3.setCount(s3.getCount() - 1);
                    if (s3.getCount() == 0) {
                        player.setHeldItem(hand, ItemStack.EMPTY);
                    } else {
                        player.setHeldItem(hand, s3);
                    }
                }
                return true;
            } else if (GasUtil.isEmpty(s2)) {
                if (this.fluid == null) {
                    return true;
                }
                IAEGasStack extract = null;
                final net.minecraft.item.Item item = s2.getItem();
                if (item instanceof IGasItem) {
                    extract = mon.extractItems(
                            StorageChannels.GAS.createStack(
                                    new GasStack(GasUtil.getGas(this.fluid), ((IGasItem) item).getMaxGas(s2))),
                            Actionable.SIMULATE, new MachineSource(this));
                } else {
                    return true;
                }
                if (extract != null) {
                    mon.extractItems(
                            StorageChannels.GAS.createStack(new GasStack(GasUtil.getGas(this.fluid),
                                    (int) extract.getStackSize())),
                            Actionable.MODULATE, new MachineSource(this));
                    final MutablePair<Integer, ItemStack> empty1 = GasUtil.fillStack(s2,
                            (GasStack) extract.getGasStack());
                    if (empty1.getLeft() == 0) {
                        mon.injectItems(
                                StorageChannels.GAS.createStack(new GasStack(GasUtil.getGas(this.fluid),
                                        (int) extract.getStackSize())),
                                Actionable.MODULATE, new MachineSource(this));
                        return true;
                    }
                    final ItemStack empty = empty1.getRight();
                    if (empty != null && !empty.isEmpty()) {
                        this.dropItems(this.getHost().getTile().getWorld(),
                                this.getHost().getTile().getPos().offset(this.getFacing()), empty);
                    }
                    final ItemStack s3 = s.copy();
                    s3.setCount(s3.getCount() - 1);
                    if (s3.getCount() == 0) {
                        player.setHeldItem(hand, ItemStack.EMPTY);
                    } else {
                        player.setHeldItem(hand, s3);
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Optional.Method(modid = "mekanism")
    public boolean storageMonitor(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (player == null || player.world == null) {
            return true;
        }
        if (player.world.isRemote) {
            return true;
        }
        final ItemStack s = player.getHeldItem(hand);
        if (s == null) {
            if (this.locked) {
                return false;
            }
            if (this.fluid == null) {
                return true;
            }
            if (this.watcher != null) {
                this.watcher.remove(AEUtils.createFluidStack(this.fluid));
            }
            this.fluid = null;
            this.amount = 0L;
            if (this.getHost() != null) {
                this.getHost().markForUpdate();
            }
            return true;
        }
        final RayTraceResult rayTraceResult = new RayTraceResult(pos, this.getFacing(), this.getLocation().getPos());
        final IWrenchHandler wrenchHandler = WrenchUtil.getHandler(s, player, rayTraceResult, hand);
        if (wrenchHandler != null) {
            this.locked = !this.locked;
            wrenchHandler.wrenchUsed(s, player, rayTraceResult, hand);
            if (this.getHost() != null) {
                this.getHost().markForUpdate();
            }
            if (this.locked) {
                player.sendMessage(new TextComponentTranslation("chat.appliedenergistics2.isNowLocked"));
            } else {
                player.sendMessage(new TextComponentTranslation("chat.appliedenergistics2.isNowUnlocked"));
            }
            return true;
        }
        if (this.locked) {
            return false;
        }
        if (GasUtil.isFilled(s)) {
            if (this.fluid != null && this.watcher != null) {
                this.watcher.remove(AEUtils.createFluidStack(this.fluid));
            }
            final GasStack gas = GasUtil.getGasFromContainer(s);
            final net.minecraftforge.fluids.FluidStack fluidStack = GasUtil.getFluidStack(gas);
            this.fluid = fluidStack == null ? null : fluidStack.getFluid();
            if (this.watcher != null) {
                this.watcher.add(AEUtils.createFluidStack(this.fluid));
            }
            if (this.getHost() != null) {
                this.getHost().markForUpdate();
            }
            return true;
        }
        return false;
    }

    @Optional.Method(modid = "mekanism")
    @Override
    public void onStackChange() {
        if (this.fluid != null) {
            final IGridNode node = this.getGridNode();
            if (node == null) {
                return;
            }

            final IGrid grid = node.getGrid();
            if (grid == null) {
                return;
            }

            final IStorageGrid storage = grid.getCache(IStorageGrid.class);
            if (storage == null) {
                return;
            }

            final IMEMonitor<IAEGasStack> fluids = this.getGasStorage();
            if (fluids == null) {
                return;
            }

            final Object gas = GasUtil.getGas(this.fluid);

            for (final IAEGasStack s : fluids.getStorageList()) {
                if (s.getGas() == gas) {
                    this.amount = s.getStackSize();
                    if (this.getHost() != null) {
                        this.getHost().markForUpdate();
                    }

                    return;
                }

            }

            this.amount = 0L;
            if (this.getHost() != null) {
                this.getHost().markForUpdate();
            }
        }
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return PartModels.CONVERSION_MONITOR_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return PartModels.CONVERSION_MONITOR_ON;
        } else {
            return PartModels.CONVERSION_MONITOR_OFF;
        }
    }

    @Override
    public void updateWatcher(final IStackWatcher w) {
        this.watcher = w;

        if (this.fluid != null) {
            w.add(StorageChannels.GAS.createStack(this.fluid));
        }

        this.onStackChange(null, null, null, null, null);
    }
}
