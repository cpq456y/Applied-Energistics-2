package appeng.ext.wut;

import appeng.ext.wut.network.WUTNetworkHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

/**
 * WUT插件主类
 * 负责注册物品、网络消息和配方
 */
public class WUTPlugin {

    public static final String NET_CHANNEL_ID = "ae2wut_el";
    public static final SimpleNetworkWrapper NET_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(NET_CHANNEL_ID);
    public static WUTPlugin INSTANCE;

    public WUTPlugin() {
        INSTANCE = this;
    }

    public static void init() {
        // 注册网络消息
        NET_CHANNEL.registerMessage(
            WUTNetworkHandler.CycleTerminalMessage.Handler.class,
            WUTNetworkHandler.CycleTerminalMessage.class,
            0,
            Side.SERVER
        );
    }
}
