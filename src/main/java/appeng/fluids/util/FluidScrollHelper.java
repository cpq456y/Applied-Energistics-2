package appeng.fluids.util;


import appeng.api.storage.data.IAEFluidStack;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fluids.Fluid;

public class FluidScrollHelper {
    
    public static long calculateNewAmount(final long currentAmount, final int wheel, final long maxAmount) {
        final long minAmount = Fluid.BUCKET_VOLUME;
        
        long newAmount;
        
        if (GuiScreen.isShiftKeyDown()) {
            if (GuiScreen.isCtrlKeyDown()) {
                if (wheel > 0) {
                    newAmount = currentAmount * 2;
                } else {
                    newAmount = currentAmount / 2;
                }
            } else {
                if (wheel > 0) {
                    newAmount = currentAmount + Fluid.BUCKET_VOLUME;
                } else {
                    newAmount = currentAmount - Fluid.BUCKET_VOLUME;
                }
            }
        } else {
            return currentAmount;
        }
        
        newAmount = Math.max(minAmount, Math.min(maxAmount, newAmount));
        
        return newAmount;
    }
    
    public static boolean applyScrollAdjustment(final IAEFluidTank fluidConfig, final int slotIndex, 
                                                   final int wheel, final long maxAmount) {
        final IAEFluidStack currentFluid = fluidConfig.getFluidInSlot(slotIndex);
        if (currentFluid == null) {
            return false;
        }
        
        final long currentAmount = currentFluid.getStackSize();
        final long newAmount = calculateNewAmount(currentAmount, wheel, maxAmount);
        
        if (newAmount != currentAmount) {
            final IAEFluidStack newFluid = currentFluid.copy();
            newFluid.setStackSize(newAmount);
            fluidConfig.setFluidInSlot(slotIndex, newFluid);
            return true;
        }
        
        return false;
    }
}
