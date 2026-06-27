package appeng.fluids.util;


import appeng.api.storage.data.IAEFluidStack;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fluids.Fluid;

/**
 * 流体滚轮调整工具类
 * 提供滚轮调整流体量的通用逻辑，可被多个GUI复用
 */
public class FluidScrollHelper {
    
    /**
     * 根据滚轮方向和按键状态计算新的流体量
     * 
     * @param currentAmount 当前流体量（单位：mB）
     * @param wheel 滚轮方向，正数向上，负数向下
     * @param maxAmount 最大流体量上限（单位：mB）
     * @return 调整后的流体量（单位：mB）
     */
    public static long calculateNewAmount(final long currentAmount, final int wheel, final long maxAmount) {
        final long minAmount = Fluid.BUCKET_VOLUME; // 1B = 1000mB
        
        long newAmount;
        
        if (GuiScreen.isShiftKeyDown()) {
            if (GuiScreen.isCtrlKeyDown()) {
                // shift+ctrl+滚轮：当前数量*2 或 /2
                if (wheel > 0) {
                    newAmount = currentAmount * 2;
                } else {
                    newAmount = currentAmount / 2;
                }
            } else {
                // shift+滚轮：n+1B 或 n-1B
                if (wheel > 0) {
                    newAmount = currentAmount + Fluid.BUCKET_VOLUME;
                } else {
                    newAmount = currentAmount - Fluid.BUCKET_VOLUME;
                }
            }
        } else {
            // 普通滚轮：不处理
            return currentAmount;
        }
        
        // 限制范围
        newAmount = Math.max(minAmount, Math.min(maxAmount, newAmount));
        
        return newAmount;
    }
    
    /**
     * 应用滚轮调整到流体配置
     * 
     * @param fluidConfig 流体配置槽
     * @param slotIndex 槽位索引
     * @param wheel 滚轮方向
     * @param maxAmount 最大流体量上限
     * @return 是否成功调整
     */
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
