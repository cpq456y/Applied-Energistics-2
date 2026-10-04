package appeng.ext.aeadditions.integration.opencomputers;

import li.cil.oc.api.API;
import li.cil.oc.common.item.data.DroneData;
import li.cil.oc.common.item.data.RobotData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;

/** Ported from OCUtils.kt. */
public final class OCUtils {

    private OCUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <P extends IPart, C extends P> P getPart(final World world, final BlockPos pos,
            final AEPartLocation location, final Class<C> clazz) {
        if (world == null || pos == null) {
            return null;
        }

        final TileEntity tile = world.getTileEntity(pos);

        if (!(tile instanceof IPartHost)) {
            return null;
        }

        final IPartHost host = (IPartHost) tile;

        if (location == null || location == AEPartLocation.INTERNAL) {
            for (final AEPartLocation side : AEPartLocation.SIDE_LOCATIONS) {
                final IPart part = host.getPart(side);

                if (part != null && clazz == part.getClass()) {
                    return (P) part;
                }
            }

            return null;
        }

        final IPart part = host.getPart(location);

        if (part == null || clazz != part.getClass()) {
            return null;
        }

        return (P) part;
    }

    public static boolean isRobot(final ItemStack itemStack) {
        final li.cil.oc.api.detail.ItemInfo item = API.items.get(itemStack);
        if (item == null) {
            return false;
        }

        return "robot".equals(item.name());
    }

    public static boolean isDrone(final ItemStack itemStack) {
        final li.cil.oc.api.detail.ItemInfo item = API.items.get(itemStack);
        if (item == null) {
            return false;
        }

        return "drone".equals(item.name());
    }

    public static ItemStack getComponent(final RobotData robot, final Item item, final int meta) {
        for (final ItemStack component : robot.components()) {
            if (component != null && component.getItem() == item) {
                return component;
            }
        }

        return null;
    }

    public static ItemStack getComponent(final RobotData robot, final Item item) {
        return getComponent(robot, item, 0);
    }

    public static ItemStack getComponent(final DroneData drone, final Item item, final int meta) {
        for (final ItemStack component : drone.components()) {
            if (component != null && component.getItem() == item) {
                return component;
            }
        }

        return null;
    }

    public static ItemStack getComponent(final DroneData drone, final Item item) {
        return getComponent(drone, item, 0);
    }
}
