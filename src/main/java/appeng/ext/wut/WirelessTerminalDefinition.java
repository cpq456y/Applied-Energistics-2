package appeng.ext.wut;

import appeng.api.features.IWirelessTermHandler;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.network.IGuiHandler;

/**
 * 无线终端定义
 * 用于描述一个无线终端的元数据
 */
public class WirelessTerminalDefinition {
    private final String name;
    private final Item terminalItem;
    private final IWirelessTermHandler handler;
    private final IGuiHandler guiHandler;
    private final String displayName;

    public WirelessTerminalDefinition(String name, Item terminalItem, IWirelessTermHandler handler, 
                                     IGuiHandler guiHandler, String displayName) {
        this.name = name;
        this.terminalItem = terminalItem;
        this.handler = handler;
        this.guiHandler = guiHandler;
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public Item getTerminalItem() {
        return terminalItem;
    }

    public IWirelessTermHandler getHandler() {
        return handler;
    }

    public IGuiHandler getGuiHandler() {
        return guiHandler;
    }

    public String getDisplayName() {
        return displayName;
    }
}
