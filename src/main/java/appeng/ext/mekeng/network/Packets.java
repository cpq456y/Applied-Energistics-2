package appeng.ext.mekeng.network;

import appeng.ext.mekeng.MekEng;
import appeng.ext.mekeng.network.packet.CGasSlotSync;
import appeng.ext.mekeng.network.packet.CGenericPacket;
import appeng.ext.mekeng.network.packet.CSwitchGuis;
import appeng.ext.mekeng.network.packet.MkEMessage;
import appeng.ext.mekeng.network.packet.SGasSlotSync;
import appeng.ext.mekeng.network.packet.SGenericPacket;
import appeng.ext.mekeng.network.packet.SMEGasInventoryUpdate;
import net.minecraftforge.fml.relauncher.Side;

public class Packets {

    private static int nextID = 1;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register(MkEMessage packet) {
        MekEng.proxy.netHandler.registerMessage(packet.getHandler(), packet.getClass(), nextID++, packet.isClient() ? Side.CLIENT : Side.SERVER);
    }

    public static void init() {
        register(new CGenericPacket());
        register(new SGenericPacket());
        register(new CSwitchGuis());
        register(new SMEGasInventoryUpdate());
        register(new CGasSlotSync());
        register(new SGasSlotSync());
    }

}
