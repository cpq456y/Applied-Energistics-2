package appeng.ext.mekeng.container.handler;

import appeng.api.storage.ITerminalHost;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.ext.mekeng.client.gui.GuiGasIO;
import appeng.ext.mekeng.client.gui.GuiGasInterface;
import appeng.ext.mekeng.client.gui.GuiGasInterfaceConfigurationTerminal;
import appeng.ext.mekeng.client.gui.GuiGasLevelEmitter;
import appeng.ext.mekeng.client.gui.GuiGasStorageBus;
import appeng.ext.mekeng.client.gui.GuiGasTerminal;
import appeng.ext.mekeng.client.gui.GuiMEPortableGasCell;
import appeng.ext.mekeng.client.gui.GuiWirelessGasTerminal;
import appeng.ext.mekeng.container.ContainerGasIO;
import appeng.ext.mekeng.container.ContainerGasInterface;
import appeng.ext.mekeng.container.ContainerGasInterfaceConfigurationTerminal;
import appeng.ext.mekeng.container.ContainerGasLevelEmitter;
import appeng.ext.mekeng.container.ContainerGasStorageBus;
import appeng.ext.mekeng.container.ContainerGasTerminal;
import appeng.ext.mekeng.container.ContainerMEPortableGasCell;
import appeng.ext.mekeng.container.ContainerWirelessGasTerminal;
import appeng.ext.mekeng.common.me.duality.IGasInterfaceHost;
import appeng.ext.mekeng.common.me.storage.IPortableGasCell;
import appeng.ext.mekeng.common.part.PartGasInterfaceConfigurationTerminal;
import appeng.ext.mekeng.common.part.PartGasLevelEmitter;
import appeng.ext.mekeng.common.part.PartGasStorageBus;
import appeng.ext.mekeng.common.part.PartSharedGasBus;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;

public class MkEGuis {

    /**
     * Base offset added to every encoded MkE GUI id. AE2 encodes its own ids as {@code ordinal << 4 | side}
     * (max ~759) and the AE-Additions layer uses ids below 16, so starting at 1024 keeps the three encodings
     * apart and lets {@code appeng.core.sync.GuiBridge} route each request to the right handler.
     */
    public static final int GUI_ID_BASE = 1024;

    private static final Int2ObjectMap<GuiFactory<?>> GUIS = new Int2ObjectOpenHashMap<>();
    private static int nextID = 1;

    public static GuiFactory<ITerminalHost> GAS_TERMINAL = new GuiFactory<ITerminalHost>(ITerminalHost.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerGasTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiGasTerminal(player.inventory, inv);
        }
    };

    public static GuiFactory<PartSharedGasBus> GAS_IO_BUS = new GuiFactory<PartSharedGasBus>(PartSharedGasBus.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, PartSharedGasBus inv) {
            return new ContainerGasIO(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, PartSharedGasBus inv) {
            return new GuiGasIO(player.inventory, inv);
        }
    };

    public static GuiFactory<IGasInterfaceHost> GAS_INTERFACE = new GuiFactory<IGasInterfaceHost>(IGasInterfaceHost.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, IGasInterfaceHost inv) {
            return new ContainerGasInterface(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IGasInterfaceHost inv) {
            return new GuiGasInterface(player.inventory, inv);
        }
    };

    public static GuiFactory<PartGasStorageBus> GAS_STORAGE_BUS = new GuiFactory<PartGasStorageBus>(PartGasStorageBus.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, PartGasStorageBus inv) {
            return new ContainerGasStorageBus(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, PartGasStorageBus inv) {
            return new GuiGasStorageBus(player.inventory, inv);
        }
    };

    public static GuiFactory<PartGasLevelEmitter> GAS_LEVEL_EMITTER = new GuiFactory<PartGasLevelEmitter>(PartGasLevelEmitter.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, PartGasLevelEmitter inv) {
            return new ContainerGasLevelEmitter(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, PartGasLevelEmitter inv) {
            return new GuiGasLevelEmitter(player.inventory, inv);
        }
    };

    public static GuiFactory<PartGasInterfaceConfigurationTerminal> GAS_INTERFACE_TERMINAL = new GuiFactory<PartGasInterfaceConfigurationTerminal>(PartGasInterfaceConfigurationTerminal.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, PartGasInterfaceConfigurationTerminal inv) {
            return new ContainerGasInterfaceConfigurationTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, PartGasInterfaceConfigurationTerminal inv) {
            return new GuiGasInterfaceConfigurationTerminal(player.inventory, inv);
        }
    };

    public static GuiFactory<IPortableGasCell> PORTABLE_GAS_CELL = new GuiFactory<IPortableGasCell>(IPortableGasCell.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, IPortableGasCell inv) {
            return new ContainerMEPortableGasCell(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IPortableGasCell inv) {
            return new GuiMEPortableGasCell(player.inventory, inv);
        }
    };

    public static GuiFactory<WirelessTerminalGuiObject> WIRELESS_GAS_TERM = new GuiFactory<WirelessTerminalGuiObject>(WirelessTerminalGuiObject.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, WirelessTerminalGuiObject inv) {
            return new ContainerWirelessGasTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, WirelessTerminalGuiObject inv) {
            return new GuiWirelessGasTerminal(player.inventory, inv);
        }
    };

    protected static synchronized int registerFactory(GuiFactory<?> factory) {
        GUIS.put(nextID, factory);
        int id = nextID;
        nextID ++;
        return id;
    }

    public static GuiFactory<?> getFactory(int id) {
        return GUIS.get(id);
    }

}
