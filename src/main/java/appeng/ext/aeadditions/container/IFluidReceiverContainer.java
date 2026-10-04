package appeng.ext.aeadditions.container;

import net.minecraftforge.fluids.Fluid;

public interface IFluidReceiverContainer {
	void receiveSelectedFluid(Fluid selectedFluid);
}
