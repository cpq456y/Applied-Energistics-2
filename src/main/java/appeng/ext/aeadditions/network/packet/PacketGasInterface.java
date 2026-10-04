package appeng.ext.aeadditions.network.packet;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.ext.aeadditions.container.gas.ContainerGasInterface;
import appeng.ext.aeadditions.gui.gas.GuiGasInterface;
import appeng.ext.aeadditions.integration.mekanism.gas.MekanismGas;
import appeng.ext.aeadditions.util.GuiUtil;

/** Ported from PacketGasInterface.kt. */
public class PacketGasInterface extends Packet {

    public final List<GasTank> gasTanks;
    public final List<Gas> gasConfig;

    public PacketGasInterface(final List<GasTank> gasTanks, final List<Gas> gasConfig) {
        this.gasTanks = gasTanks;
        this.gasConfig = gasConfig;
    }

    @Override
    public PacketId getPacketId() {
        return PacketId.GAS_INTERFACE;
    }

    @Override
    public void writeData(final PacketBufferEC data) {
        final NBTTagCompound tag = new NBTTagCompound();
        for (int index = 0; index < this.gasTanks.size(); index++) {
            tag.setTag("tank#" + index, this.gasTanks.get(index).write(new NBTTagCompound()));
        }

        for (int index = 0; index < this.gasConfig.size(); index++) {
            final Gas gas = this.gasConfig.get(index);
            if (gas != null) {
                tag.setString("gasConfig#" + index, gas.getName());
            }
        }

        data.writeCompoundTag(tag);
    }

    @SideOnly(Side.CLIENT)
    public static class Handler implements IPacketHandlerClient {

        @Override
        public void onPacketData(final PacketBufferEC data, final EntityPlayer player) throws IOException {
            final NBTTagCompound tag = data.readCompoundTag();
            final List<GasTank> tanks = new ArrayList<>();
            final List<Gas> gasConfig = new ArrayList<>();

            for (int i = 0; i < 6; i++) {
                tanks.add(i, GasTank.readFromNBT(tag.getCompoundTag("tank#" + i)));

                if (tag.hasKey("gasConfig#" + i)) {
                    gasConfig.add(i, GasRegistry.getGas(tag.getString("gasConfig#" + i)));
                } else {
                    gasConfig.add(i, null);
                }
            }

            final GuiGasInterface gui = GuiUtil.getGui(GuiGasInterface.class);
            final ContainerGasInterface container = GuiUtil.getContainer(gui, ContainerGasInterface.class);
            if (container == null) {
                return;
            }

            for (int i = 0; i < 6; i++) {
                final GasTank tank = tanks.get(i);
                final Gas gas = gasConfig.get(i);

                final GasTank existingTank = container.gasInterface.getGasTanks().get(i);

                existingTank.setGas(tank.getGas());
                gui.getFilter()[i].setFluid(gas == null ? null : MekanismGas.fluidGas.get(gas));
            }
        }
    }
}
