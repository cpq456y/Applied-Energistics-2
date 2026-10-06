package appeng.ext.aeadditions.util;

import appeng.api.config.SecurityPermissions;
import appeng.api.util.AEPartLocation;
import appeng.ext.aeadditions.api.IECTileEntity;
import appeng.ext.aeadditions.container.ITickContainer;
import net.minecraft.inventory.Container;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class AEAdditionsEventHandler {

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        IECTileEntity tileEntity = TileUtil.getTile(event.getWorld(), event.getPos(), IECTileEntity.class);
        if (tileEntity != null) {
            if (!PermissionUtil.hasPermission(event.getPlayer(), SecurityPermissions.BUILD, tileEntity.getGridNode(AEPartLocation.INTERNAL))) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.side == Side.SERVER && event.player != null) {
            if (event.player.openContainer != null) {
                Container con = event.player.openContainer;
                if (con instanceof ITickContainer)
                    ((ITickContainer) con).onTick();
            }
        }
    }
}
