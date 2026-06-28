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

package appeng.container.implementations;


import appeng.api.config.*;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.IConfigManager;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.*;
import appeng.fluids.helper.FluidSyncHelper;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;

import java.util.Collections;
import java.util.Map;


public class ContainerInterface extends ContainerUpgradeable implements IOptionalSlotHost, appeng.fluids.container.IFluidSyncContainer, appeng.container.IItemSyncContainer {

    private final DualityInterface myDuality;
    private final FluidSyncHelper tankSync;
    private final FluidSyncHelper configSync;
    private final appeng.helper.ItemConfigSyncHelper itemConfigSync;

    @GuiSync(3)
    public YesNo bMode = YesNo.NO;

    @GuiSync(4)
    public LockCraftingMode lMode = LockCraftingMode.NONE;

    @GuiSync(7)
    public int patternExpansions = 0;

    @GuiSync(8)
    public YesNo iTermMode = YesNo.YES;

    @GuiSync(9)
    public LockCraftingMode lockReason = LockCraftingMode.NONE;

    public ContainerInterface(final InventoryPlayer ip, final IInterfaceHost te) {
        super(ip, te.getInterfaceDuality().getHost());

        this.myDuality = te.getInterfaceDuality();
        this.tankSync = new FluidSyncHelper(this.myDuality.getFluidStorage(), DualityInterface.NUMBER_OF_STORAGE_SLOTS);
        this.configSync = new FluidSyncHelper(this.myDuality.getFluidConfig(), 0);
        this.itemConfigSync = new appeng.helper.ItemConfigSyncHelper(
            (appeng.tile.inventory.AppEngInternalAEInventory) this.myDuality.getConfig(),
            0
        );

        for (int row = 0; row < 4; ++row) {
            for (int x = 0; x < 9; x++) {
                this.addSlotToContainer(new OptionalSlotRestrictedInput(SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN, this.myDuality
                        .getPatterns(), this, x + row * 9, 8 + 18 * x, 97 + (18 * row), row, this.getInventoryPlayer()).setStackLimit(1));
            }
        }

        for (int x = 0; x < DualityInterface.NUMBER_OF_CONFIG_SLOTS; x++) {
            this.addSlotToContainer(new SlotFakeFluidConfig(this.myDuality.getConfig(), x, 8 + 18 * x, 35,
                    this.myDuality.getFluidConfig(), x));
        }

        for (int x = 0; x < DualityInterface.NUMBER_OF_STORAGE_SLOTS; x++) {
            this.addSlotToContainer(new SlotOversized(this.myDuality.getStorage(), x, 8 + 18 * x, 53));
        }
    }

    @Override
    protected int getHeight() {
        return 256;
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();
    }

    @Override
    public int availableUpgrades() {
        return 4;
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        return myDuality.getInstalledUpgrades(Upgrades.PATTERN_EXPANSION) >= idx;
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (patternExpansions != getPatternUpgrades()) {
            patternExpansions = getPatternUpgrades();
            this.myDuality.dropExcessPatterns();
        }

        if (Platform.isServer()){
            lockReason = myDuality.getCraftingLockedReason();
            this.tankSync.sendDiff(this.listeners);
            this.configSync.sendDiff(this.listeners);
            this.itemConfigSync.sendDiff(this.listeners);
        }
        super.detectAndSendChanges();
    }

    @Override
    public void addListener(final IContainerListener listener) {
        super.addListener(listener);
        this.tankSync.sendFull(Collections.singleton(listener));
        this.configSync.sendFull(Collections.singleton(listener));
        this.itemConfigSync.sendFull(Collections.singleton(listener));
    }

    public void receiveFluidSlots(final Map<Integer, IAEFluidStack> fluids) {
        if (appeng.util.Platform.isServer()) {
            boolean configChanged = false;
            for (Map.Entry<Integer, IAEFluidStack> entry : fluids.entrySet()) {
                int key = entry.getKey();
                IAEFluidStack fluid = entry.getValue();
                if (key >= 0 && key < DualityInterface.NUMBER_OF_CONFIG_SLOTS) {
                    final IAEFluidStack currentFluid = this.myDuality.getFluidConfig().getFluidInSlot(key);
                    final boolean fluidChanged = (currentFluid == null && fluid != null) ||
                                                 (currentFluid != null && !currentFluid.equals(fluid)) ||
                                                 (currentFluid != null && fluid != null && currentFluid.getStackSize() != fluid.getStackSize());
                    
                    this.myDuality.getFluidConfig().setFluidInSlot(key, fluid);
                    
                    if (fluidChanged) {
                        configChanged = true;
                    }
                    
                    if (fluid != null && !this.myDuality.getConfig().getStackInSlot(key).isEmpty()) {
                        ((appeng.tile.inventory.AppEngInternalAEInventory) this.myDuality.getConfig()).setStackInSlot(key, net.minecraft.item.ItemStack.EMPTY);
                        configChanged = true;
                    }
                }
            }
            if (configChanged) {
                this.myDuality.readFluidConfig();
                this.myDuality.readConfig();
                this.myDuality.saveChanges();
            }
        } else {
            for (Map.Entry<Integer, IAEFluidStack> entry : fluids.entrySet()) {
                int key = entry.getKey();
                if (key >= 0 && key < DualityInterface.NUMBER_OF_CONFIG_SLOTS) {
                    this.myDuality.getFluidConfig().setFluidInSlot(key, entry.getValue());
                } else if (key >= DualityInterface.NUMBER_OF_STORAGE_SLOTS && 
                           key < DualityInterface.NUMBER_OF_STORAGE_SLOTS + DualityInterface.NUMBER_OF_STORAGE_SLOTS) {
                    int slotIndex = key - DualityInterface.NUMBER_OF_STORAGE_SLOTS;
                    this.myDuality.getFluidStorage().setFluidInSlot(slotIndex, entry.getValue());
                }
            }
        }
    }

    @Override
    public void receiveItemSlots(final Map<Integer, appeng.api.storage.data.IAEItemStack> items) {
        if (appeng.util.Platform.isServer()) {
            boolean configChanged = false;
            for (Map.Entry<Integer, appeng.api.storage.data.IAEItemStack> entry : items.entrySet()) {
                int key = entry.getKey();
                appeng.api.storage.data.IAEItemStack item = entry.getValue();
                if (key >= 0 && key < DualityInterface.NUMBER_OF_CONFIG_SLOTS) {
                    // Check if the item actually changed
                    final net.minecraft.item.ItemStack currentItem = this.myDuality.getConfig().getStackInSlot(key);
                    final boolean itemChanged = (currentItem.isEmpty() && item != null) ||
                                                (!currentItem.isEmpty() && item == null) ||
                                                (!currentItem.isEmpty() && item != null && !net.minecraft.item.ItemStack.areItemStacksEqual(currentItem, item.getDefinition()));
                    
                    if (item != null) {
                        net.minecraft.item.ItemStack stack = item.getDefinition().copy();
                        stack.setCount((int) item.getStackSize());
                        ((appeng.tile.inventory.AppEngInternalAEInventory) this.myDuality.getConfig()).setStackInSlot(key, stack);
                    } else {
                        ((appeng.tile.inventory.AppEngInternalAEInventory) this.myDuality.getConfig()).setStackInSlot(key, net.minecraft.item.ItemStack.EMPTY);
                    }
                    
                    if (itemChanged) {
                        configChanged = true;
                    }
                    
                    // Clear fluid config if item is set
                    if (this.myDuality.getFluidConfig().getFluidInSlot(key) != null) {
                        this.myDuality.getFluidConfig().setFluidInSlot(key, null);
                        configChanged = true;
                    }
                }
            }
            if (configChanged) {
                this.myDuality.readConfig();
                this.myDuality.readFluidConfig();
                this.myDuality.saveChanges();
            }
        } else {
            // Client side: update config
            for (Map.Entry<Integer, appeng.api.storage.data.IAEItemStack> entry : items.entrySet()) {
                int key = entry.getKey();
                if (key >= 0 && key < DualityInterface.NUMBER_OF_CONFIG_SLOTS) {
                    if (entry.getValue() != null) {
                        net.minecraft.item.ItemStack stack = entry.getValue().getDefinition().copy();
                        stack.setCount((int) entry.getValue().getStackSize());
                        ((appeng.tile.inventory.AppEngInternalAEInventory) this.myDuality.getConfig()).setStackInSlot(key, stack);
                    } else {
                        ((appeng.tile.inventory.AppEngInternalAEInventory) this.myDuality.getConfig()).setStackInSlot(key, net.minecraft.item.ItemStack.EMPTY);
                    }
                }
            }
        }
    }

    public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slot, final long id) {
        if (action != InventoryAction.FILL_ITEM && action != InventoryAction.EMPTY_ITEM) {
            super.doAction(player, action, slot, id);
            return;
        }

        if (slot < 0 || slot >= DualityInterface.NUMBER_OF_STORAGE_SLOTS) {
            return;
        }

        final appeng.api.storage.data.IAEFluidStack fluidStack = this.myDuality.getFluidStorage().getFluidInSlot(slot);
        if (fluidStack == null) {
            return;
        }

        final net.minecraft.item.ItemStack mouseItem = player.inventory.getItemStack();
        if (mouseItem.isEmpty()) {
            return;
        }

        if (action == InventoryAction.FILL_ITEM) {
            final net.minecraftforge.fluids.capability.IFluidHandler fluidHandler = net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY
                    .cast(mouseItem.getCapability(net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null));
            if (fluidHandler == null) {
                return;
            }

            final net.minecraftforge.fluids.FluidStack fluidToFill = fluidStack.getFluidStack().copy();
            fluidToFill.amount = Math.min(fluidToFill.amount, (int) fluidStack.getStackSize());

            final int filled = fluidHandler.fill(fluidToFill, true);
            if (filled > 0) {
                final appeng.fluids.util.AENetworkFluidInventory fluidStorage = (appeng.fluids.util.AENetworkFluidInventory) this.myDuality.getFluidStorage();
                fluidStorage.drain(slot, new net.minecraftforge.fluids.FluidStack(fluidStack.getFluid(), filled), true);
                player.inventory.setItemStack(mouseItem);
            }
        } else if (action == InventoryAction.EMPTY_ITEM) {
            final net.minecraftforge.fluids.capability.IFluidHandler fluidHandler = net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY
                    .cast(mouseItem.getCapability(net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null));
            if (fluidHandler == null) {
                return;
            }

            final net.minecraftforge.fluids.FluidStack drained = fluidHandler.drain((int) (DualityInterface.TANK_CAPACITY - fluidStack.getStackSize()), true);
            if (drained != null && drained.amount > 0) {
                final appeng.fluids.util.AENetworkFluidInventory fluidStorage = (appeng.fluids.util.AENetworkFluidInventory) this.myDuality.getFluidStorage();
                fluidStorage.fill(slot, drained, true);
                player.inventory.setItemStack(mouseItem);
            }
        }
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        super.onUpdate(field, oldValue, newValue);
        if (Platform.isClient() && field.equals("patternExpansions"))
            this.myDuality.dropExcessPatterns();
    }

    @Override
    protected void loadSettingsFromHost(final IConfigManager cm) {
        this.setBlockingMode((YesNo) cm.getSetting(Settings.BLOCK));
        this.setUnlockMode((LockCraftingMode) cm.getSetting(Settings.UNLOCK));
        this.setInterfaceTerminalMode((YesNo) cm.getSetting(Settings.INTERFACE_TERMINAL));
    }

    public LockCraftingMode getUnlockMode() {return this.lMode;}

    public void setUnlockMode(final LockCraftingMode mode) {this.lMode = mode;}

    public YesNo getBlockingMode() {
        return this.bMode;
    }

    private void setBlockingMode(final YesNo bMode) {
        this.bMode = bMode;
    }

    public YesNo getInterfaceTerminalMode() {
        return this.iTermMode;
    }

    private void setInterfaceTerminalMode(final YesNo iTermMode) {
        this.iTermMode = iTermMode;
    }

    public int getPatternUpgrades() {
        return this.myDuality.getInstalledUpgrades(Upgrades.PATTERN_EXPANSION);
    }

    public LockCraftingMode getCraftingLockedReason() {
        return lockReason;
    }

    public DualityInterface getMyDuality() {
        return this.myDuality;
    }
}
