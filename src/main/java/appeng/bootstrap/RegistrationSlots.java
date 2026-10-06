/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.bootstrap;


import appeng.bootstrap.components.IItemRegistrationComponent;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.ArrayList;
import java.util.List;


/**
 * Insertion points inside AE2's own item registration order.
 *
 * <p>Forge hands out item ids in the order in which the items are registered, and both JEI and the
 * creative tabs list items by walking the item registry in exactly that id order. Content that was merged
 * into this fork as an extension layer (see {@code appeng.ext.mekeng}) therefore cannot register its items
 * from its own registry event handler: they would all end up behind every single AE2 item. Instead
 * {@code ApiItems} / {@code ApiBlocks} drop one of these slots at the position where the items belong, and
 * the extension fills the slot during pre-init, when its item instances and material stacks exist.
 */
public final class RegistrationSlots {

    /** Sits directly behind the fluid storage cells. */
    public static final ItemSlot AFTER_FLUID_CELLS = new ItemSlot();

    /** Sits directly behind the fluid interface. */
    public static final ItemSlot AFTER_FLUID_INTERFACE = new ItemSlot();

    /** Sits directly behind the portable fluid cell. */
    public static final ItemSlot AFTER_PORTABLE_CELLS = new ItemSlot();

    private RegistrationSlots() {
    }

    /**
     * A gap in AE2's item registration order. Everything added to it is registered where the slot itself
     * sits, in the order in which it was added.
     */
    public static final class ItemSlot implements IItemRegistrationComponent {

        private final List<IItemRegistrationComponent> components = new ArrayList<>();

        public void add(final IItemRegistrationComponent component) {
            this.components.add(component);
        }

        @Override
        public void itemRegistration(final Side side, final IForgeRegistry<Item> registry) {
            for (final IItemRegistrationComponent component : this.components) {
                component.itemRegistration(side, registry);
            }
        }
    }
}
