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

package appeng.core;


import appeng.api.AEApi;
import appeng.api.definitions.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Optional;


public final class CreativeTab extends CreativeTabs {
    public static CreativeTab instance = null;

    public CreativeTab() {
        super("appliedenergistics2");
    }

    static void init() {
        instance = new CreativeTab();
    }

    /**
     * Vanilla iterates {@code Item.REGISTRY}, which is backed by a plain {@link java.util.HashMap}, so the tab
     * would list the entries in hash order - effectively scrambled. Forge's registry iterator walks the
     * numerical ids instead, i.e. the registration order, which is also the order JEI uses. Keeping both in the
     * same order makes the storage components/cells read as item -> fluid -> gas and 1k -> 4k -> ... -> 16384k.
     */
    @Override
    public void displayAllRelevantItems(final NonNullList<ItemStack> items) {
        for (final Item item : ForgeRegistries.ITEMS) {
            item.getSubItems(this, items);
        }
    }

    @Override
    public ItemStack getIcon() {
        return this.createIcon();
    }

    @Override
    public ItemStack createIcon() {
        final IDefinitions definitions = AEApi.instance().definitions();
        final IBlocks blocks = definitions.blocks();
        final IItems items = definitions.items();
        final IMaterials materials = definitions.materials();

        return this.findFirst(blocks.controller(), blocks.chest(), blocks.cellWorkbench(), blocks.fluixBlock(), items.cell1k(), items.networkTool(),
                materials.fluixCrystal(), materials.certusQuartzCrystal(), materials.skyDust());
    }

    private ItemStack findFirst(final IItemDefinition... choices) {
        for (final IItemDefinition definition : choices) {
            Optional<ItemStack> maybeIs = definition.maybeStack(1);
            if (maybeIs.isPresent()) {
                return maybeIs.get();
            }
        }

        return new ItemStack(Blocks.CHEST);
    }
}