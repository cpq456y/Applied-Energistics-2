package appeng.ext.mekeng.common.me.storage;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.ext.mekeng.common.me.data.IAEGasStack;

public interface IPortableGasCell extends ITerminalHost, IMEMonitor<IAEGasStack>, IEnergySource, IGuiItemObject {

}
