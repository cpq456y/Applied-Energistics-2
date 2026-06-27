/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.core.sync.packets;


import appeng.api.storage.data.IAEItemStack;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.container.IItemSyncContainer;
import appeng.util.item.AEItemStack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.HashMap;
import java.util.Map;


public class PacketItemConfigSlot extends AppEngPacket {
    private final Map<Integer, IAEItemStack> list;

    public PacketItemConfigSlot(final ByteBuf stream) {
        this.list = new HashMap<>();
        NBTTagCompound tag = ByteBufUtils.readTag(stream);

        for (final String key : tag.getKeySet()) {
            NBTTagCompound itemTag = tag.getCompoundTag(key);
            if (!itemTag.isEmpty()) {
                this.list.put(Integer.parseInt(key), AEItemStack.fromNBT(itemTag));
            } else {
                this.list.put(Integer.parseInt(key), null);
            }
        }
    }

    // api
    public PacketItemConfigSlot(final Map<Integer, IAEItemStack> list) {
        this.list = list;
        final NBTTagCompound sendTag = new NBTTagCompound();
        for (Map.Entry<Integer, IAEItemStack> is : list.entrySet()) {
            final NBTTagCompound tag = new NBTTagCompound();
            if (is.getValue() != null) {
                is.getValue().writeToNBT(tag);
            }
            sendTag.setTag(is.getKey().toString(), tag);
        }

        final ByteBuf data = Unpooled.buffer();
        data.writeInt(this.getPacketID());
        ByteBufUtils.writeTag(data, sendTag);
        this.configureWrite(data);
    }

    @Override
    public void clientPacketData(final INetworkInfo manager, final AppEngPacket packet, final EntityPlayer player) {
        final Container c = player.openContainer;
        if (c instanceof IItemSyncContainer) {
            ((IItemSyncContainer) c).receiveItemSlots(this.list);
        }
    }

    @Override
    public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player) {
        final Container c = player.openContainer;
        if (c instanceof IItemSyncContainer) {
            ((IItemSyncContainer) c).receiveItemSlots(this.list);
        }
    }
}
