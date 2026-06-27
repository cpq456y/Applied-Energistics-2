package appeng.fluids.util;


import appeng.api.storage.data.IAEFluidStack;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketFluidSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.Collections;

/**
 * 流体槽位标记工具类
 * 提供右键标记流体的通用逻辑，可被多个槽位类复用
 */
public class FluidSlotMarker {
    
    /**
     * 处理右键点击，提取流体容器的流体并设置到流体配置槽
     * 
     * @param player 玩家
     * @param fluidConfig 流体配置槽
     * @param slotIndex 槽位索引
     * @return 始终返回true，阻止右键传播到物品标记逻辑
     */
    public static boolean handleRightClick(final EntityPlayer player, final IAEFluidTank fluidConfig, final int slotIndex) {
        final ItemStack mouseStack = player.inventory.getItemStack();
        
        if (mouseStack.isEmpty()) {
            // 右键空手，清除流体配置
            fluidConfig.setFluidInSlot(slotIndex, null);
            NetworkHandler.instance().sendToServer(new PacketFluidSlot(Collections.singletonMap(slotIndex, null)));
        } else {
            // 尝试从物品中提取流体
            final FluidStack fluid = FluidUtil.getFluidContained(mouseStack);
            if (fluid != null) {
                final IAEFluidStack aeFluid = AEFluidStack.fromFluidStack(fluid);
                fluidConfig.setFluidInSlot(slotIndex, aeFluid);
                NetworkHandler.instance().sendToServer(new PacketFluidSlot(Collections.singletonMap(slotIndex, aeFluid)));
            }
        }
        
        // 始终返回true，阻止右键传播到物品标记逻辑
        return true;
    }
}
