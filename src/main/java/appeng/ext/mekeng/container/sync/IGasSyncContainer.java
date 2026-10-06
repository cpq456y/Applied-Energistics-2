package appeng.ext.mekeng.container.sync;

import appeng.ext.mekeng.common.me.data.IAEGasStack;

import java.util.Map;

public interface IGasSyncContainer {

    void receiveGasSlots(final Map<Integer, IAEGasStack> gases);

}
