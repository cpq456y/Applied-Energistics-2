package appeng.fluids.util;


import appeng.api.storage.data.IAEFluidStack;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketFluidSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.Collections;

public class FluidSlotMarker {
    
    public static boolean handleRightClick(final EntityPlayer player, final IAEFluidTank fluidConfig, final int slotIndex) {
        final ItemStack mouseStack = player.inventory.getItemStack();
        
        if (mouseStack.isEmpty()) {
            fluidConfig.setFluidInSlot(slotIndex, null);
            NetworkHandler.instance().sendToServer(new PacketFluidSlot(Collections.singletonMap(slotIndex, null)));
        } else {
            final FluidStack fluid = FluidUtil.getFluidContained(mouseStack);
            if (fluid != null) {
                final IAEFluidStack aeFluid = AEFluidStack.fromFluidStack(fluid);
                fluidConfig.setFluidInSlot(slotIndex, aeFluid);
                NetworkHandler.instance().sendToServer(new PacketFluidSlot(Collections.singletonMap(slotIndex, aeFluid)));
            }
        }
        
        return true;
    }
}
