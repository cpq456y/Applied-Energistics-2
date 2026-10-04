package appeng.ext.aeadditions.gui.widget.fluid;

import appeng.api.storage.data.IAEFluidStack;
import appeng.ext.aeadditions.container.IFluidSelectorContainer;

public interface IFluidSelectorGui extends IFluidWidgetGui {

	IFluidSelectorContainer getContainer();

	IAEFluidStack getCurrentFluid();
}
