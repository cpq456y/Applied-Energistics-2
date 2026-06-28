/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.container.slot;


import appeng.api.storage.data.IAEFluidStack;
import appeng.fluids.util.FluidSlotMarker;
import appeng.fluids.util.IAEFluidTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;


public class SlotFakeFluidConfig extends SlotFake {

    private final IAEFluidTank fluidConfig;
    private final int slotIndex;

    public SlotFakeFluidConfig(final IItemHandler inv, final int idx, final int x, final int y,
                               final IAEFluidTank fluidConfig, final int slotIndex) {
        super(inv, idx, x, y);
        this.fluidConfig = fluidConfig;
        this.slotIndex = slotIndex;
    }

    public boolean handleRightClick(EntityPlayer player) {
        boolean result = FluidSlotMarker.handleRightClick(player, this.fluidConfig, this.slotIndex);
        this.putStack(ItemStack.EMPTY);
        return result;
    }

    public IAEFluidTank getFluidConfig() {
        return this.fluidConfig;
    }

    public int getFluidSlotIndex() {
        return this.slotIndex;
    }
}
