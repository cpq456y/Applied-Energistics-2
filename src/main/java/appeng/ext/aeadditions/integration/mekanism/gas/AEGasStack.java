package appeng.ext.aeadditions.integration.mekanism.gas;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import io.netty.buffer.ByteBuf;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;
import appeng.ext.aeadditions.api.gas.IAEGasStack;
import appeng.ext.aeadditions.item.ItemGas;
import appeng.ext.aeadditions.registries.ItemEnum;
import appeng.ext.aeadditions.util.StorageChannels;

/** Ported from AEGasStack.kt. */
public class AEGasStack implements IAEGasStack {

    private boolean canCraft = false;
    private long _stackSize = 0L;
    private long _countRequestable = 0L;
    private Gas gas = null;

    public AEGasStack(final AEGasStack oldStack) {
        this.gas = oldStack.gas;
        this.setStackSize(oldStack.getStackSize());
        this.setCraftable(oldStack.isCraftable());
        this.setCountRequestable(oldStack.getCountRequestable());
    }

    public AEGasStack(final GasStack gasStack) {
        if (gasStack == null || gasStack.getGas() == null) {
            throw new IllegalArgumentException("Gas is null");
        }

        this.gas = gasStack.getGas();
        this.setStackSize(gasStack.amount);
        this.setCraftable(false);
        this.setCountRequestable(0);
    }

    public AEGasStack(final NBTTagCompound nbt) {
        this.gas = Gas.readFromNBT(nbt);
        this.setStackSize(nbt.getLong("amount"));
        this.setCraftable(nbt.getBoolean("isCraftable"));
        this.setCountRequestable(nbt.getLong("countRequestable"));
    }

    public AEGasStack(final ByteBuf data) {
        try {
            final int length = data.readInt();

            final byte[] bytes = new byte[length];
            data.readBytes(bytes);

            final DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(bytes));

            final NBTTagCompound nbt = CompressedStreamTools.readCompressed(inputStream);

            this.gas = Gas.readFromNBT(nbt);
            this.setStackSize(nbt.getLong("amount"));
            this.setCraftable(nbt.getBoolean("isCraftable"));
            this.setCountRequestable(nbt.getLong("countRequestable"));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void add(final IAEGasStack stack) {
        if (stack == null) {
            return;
        }

        this.incStackSize(stack.getStackSize());
        this.setCountRequestable(this.getCountRequestable() + stack.getCountRequestable());
        this.setCraftable(this.isCraftable() || stack.isCraftable());
    }

    @Override
    public long getStackSize() {
        return this._stackSize;
    }

    @Override
    public IAEGasStack setStackSize(final long stackSize) {
        this._stackSize = stackSize;

        return this;
    }

    @Override
    public long getCountRequestable() {
        return this._countRequestable;
    }

    @Override
    public IAEGasStack setCountRequestable(final long amount) {
        this._countRequestable = amount;

        return this;
    }

    @Override
    public boolean isCraftable() {
        return this.canCraft;
    }

    @Override
    public IAEGasStack setCraftable(final boolean isCraftable) {
        this.canCraft = isCraftable;

        return this;
    }

    @Override
    public IAEGasStack reset() {
        this.setStackSize(0);
        this._countRequestable = 0;
        this.canCraft = false;

        return this;
    }

    @Override
    public boolean isMeaningful() {
        return (int) this.getStackSize() != 0 || this.getCountRequestable() > 0 || this.isCraftable();
    }

    @Override
    public void incStackSize(final long amount) {
        this._stackSize += amount;
    }

    @Override
    public void decStackSize(final long amount) {
        this._stackSize -= amount;
    }

    @Override
    public void incCountRequestable(final long amount) {
        this._countRequestable += amount;
    }

    @Override
    public void decCountRequestable(final long amount) {
        this._countRequestable -= amount;
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        if (this.gas == null || nbt == null) {
            return;
        }

        this.gas.write(nbt);

        nbt.setLong("amount", this.getStackSize());
        nbt.setBoolean("isCraftable", this.isCraftable());
        nbt.setLong("countRequestable", this.getCountRequestable());
    }

    @Override
    public boolean fuzzyComparison(final IAEGasStack gasStack, final FuzzyMode fuzzyMode) {
        if (gasStack instanceof Gas) {
            return ((Gas) gasStack) == this.gas;
        } else if (gasStack instanceof IAEGasStack) {
            return gasStack.getGas() == this.gas;
        } else if (gasStack instanceof AEGasStack) {
            return gasStack.getGas() == this.gas;
        } else {
            return false;
        }
    }

    @Override
    public void writeToPacket(final ByteBuf byteBuffer) {
        if (byteBuffer == null) {
            return;
        }

        try {
            final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            final DataOutputStream outputStream = new DataOutputStream(byteOutputStream);
            final NBTTagCompound nbt = new NBTTagCompound();

            this.writeToNBT(nbt);

            CompressedStreamTools.writeCompressed(nbt, outputStream);

            final byte[] bytes = byteOutputStream.toByteArray();
            final int length = bytes.length;

            byteBuffer.writeInt(length);
            byteBuffer.writeBytes(bytes);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IAEGasStack copy() {
        return new AEGasStack(this);
    }

    @Override
    public IAEGasStack empty() {
        final IAEGasStack newStack = this.copy();
        newStack.reset();
        return newStack;
    }

    @Override
    public boolean isItem() {
        return false;
    }

    @Override
    public boolean isFluid() {
        return false;
    }

    @Override
    public IStorageChannel<IAEGasStack> getChannel() {
        return StorageChannels.GAS;
    }

    @Override
    public ItemStack asItemStackRepresentation() {
        final ItemStack stack = ItemEnum.GASITEM.getSizedStack(1);

        ItemGas.setGasName(stack, this.gas.getName());

        return stack;
    }

    @Override
    public GasStack getGasStack() {
        return new GasStack(this.gas, Math.min(Integer.MAX_VALUE, (int) this.getStackSize()));
    }

    @Override
    public Object getGas() {
        return this.gas;
    }

    @Override
    public int hashCode() {
        return this.gas.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }

        final AEGasStack o = (AEGasStack) other;

        if (this.canCraft != o.canCraft) {
            return false;
        }
        if (this._countRequestable != o._countRequestable) {
            return false;
        }

        return this.gas == o.gas;
    }
}
