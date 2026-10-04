package appeng.ext.aeadditions.network.packet;

import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.ext.aeadditions.tileentity.TileEntityFluidCrafter;

/** Ported from PacketCrafterDroppedItem.kt. */
public class PacketCrafterDroppedItem extends Packet {

    public final TileEntityFluidCrafter tileEntityFluidCrafter;
    public final int slot;

    public PacketCrafterDroppedItem(final TileEntityFluidCrafter tileEntityFluidCrafter, final int slot) {
        this.tileEntityFluidCrafter = tileEntityFluidCrafter;
        this.slot = slot;
    }

    @Override
    public PacketId getPacketId() {
        return PacketId.FLUID_CRAFTER_DROPPED_ITEM;
    }

    @Override
    public void writeData(final PacketBufferEC data) {
        data.writeTile(this.tileEntityFluidCrafter);
        data.writeInt(this.slot);
    }

    /** Mirrors the Kotlin {@code companion object} nesting. */
    public static class Companion {

        @SideOnly(Side.CLIENT)
        public static class HandlerClient implements IPacketHandlerClient {

            @Override
            public void onPacketData(final PacketBufferEC data, final EntityPlayer player) throws IOException {
                final TileEntityFluidCrafter tile = (TileEntityFluidCrafter) data.readTile(player.world);
                final int slot = data.readInt();

                tile.removeSlot(slot);
            }
        }
    }
}
