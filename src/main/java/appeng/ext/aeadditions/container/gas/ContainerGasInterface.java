package appeng.ext.aeadditions.container.gas;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.ext.aeadditions.container.ContainerBase;
import appeng.ext.aeadditions.container.IContainerListener;
import appeng.ext.aeadditions.gui.gas.GuiGasInterface;
import appeng.ext.aeadditions.network.packet.PacketGasInterface;
import appeng.ext.aeadditions.tileentity.TileEntityGasInterface;
import appeng.ext.aeadditions.util.NetworkUtil;

/** Ported from ContainerGasInterface.kt. */
public class ContainerGasInterface extends ContainerBase implements IContainerListener {

    private final EntityPlayer player;

    public TileEntityGasInterface gasInterface;

    @SideOnly(Side.CLIENT)
    public GuiGasInterface gui = null;

    public ContainerGasInterface(final EntityPlayer player, final TileEntityGasInterface gasInterface) {
        this.player = player;
        this.gasInterface = gasInterface;

        this.bindPlayerInventory(player.inventory, 8, 149);

        gasInterface.registerListener(this);
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }

    @Override
    public boolean canInteractWith(final EntityPlayer playerIn) {
        return true;
    }

    @Override
    public void updateContainer() {
        NetworkUtil.sendToPlayer(
                new PacketGasInterface(this.gasInterface.getGasTanks(), this.gasInterface.getGasConfig()),
                this.player);
    }

    @Override
    public void onContainerClosed(final EntityPlayer playerIn) {
        this.gasInterface.removeListener(this);
    }
}
