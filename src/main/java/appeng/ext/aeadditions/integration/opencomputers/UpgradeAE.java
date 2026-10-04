package appeng.ext.aeadditions.integration.opencomputers;

import li.cil.oc.api.Network;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Visibility;
import net.minecraft.tileentity.TileEntity;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridBlock;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import appeng.tile.misc.TileSecurityStation;

/** Ported from UpgradeAE.kt. */
public class UpgradeAE extends NetworkControl<TileSecurityStation> {

    private final EnvironmentHost envHost;

    public UpgradeAE(final EnvironmentHost envHost) {
        this.envHost = envHost;

        this.setNode(Network.newNode(this, Visibility.Network).withConnector()
                .withComponent("upgrade_me", Visibility.Network).create());
    }

    public EnvironmentHost getEnvHost() {
        return this.envHost;
    }

    @Override
    public TileSecurityStation tile() {
        final IGridHost security = this.getSecurity();
        if (security == null) {
            throw new SecurityException("No Security Station");
        }

        final IGridNode node = security.getGridNode(AEPartLocation.INTERNAL);
        if (node == null) {
            throw new SecurityException("No Security Station");
        }

        final IGridBlock gridBlock = node.getGridBlock();
        if (gridBlock == null) {
            throw new SecurityException("No Security Station");
        }

        final DimensionalCoord location = gridBlock.getLocation();
        if (location == null) {
            throw new SecurityException("No Security Station");
        }

        final TileEntity tileSecurity = location.getWorld().getTileEntity(location.getPos());
        if (!(tileSecurity instanceof TileSecurityStation)) {
            throw new SecurityException("No Security Station");
        }

        return (TileSecurityStation) tileSecurity;
    }

    @Override
    public EnvironmentHost host() {
        return this.envHost;
    }
}
