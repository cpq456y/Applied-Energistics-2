package appeng.ext.aeadditions.integration.opencomputers;

import li.cil.oc.common.item.data.DroneData;
import li.cil.oc.common.item.data.RobotData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.IGuiHandler;

import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;
import appeng.ext.aeadditions.registries.ItemEnum;

/** Ported from WirelessHandlerUpgradeAE.kt (Kotlin {@code object}, kept as a singleton). */
public final class WirelessHandlerUpgradeAE implements IWirelessTermHandler {

    public static final WirelessHandlerUpgradeAE INSTANCE = new WirelessHandlerUpgradeAE();

    private WirelessHandlerUpgradeAE() {
    }

    @Override
    public String getEncryptionKey(final ItemStack itemStack) {
        if (itemStack == null) {
            return "";
        }

        if (OCUtils.isRobot(itemStack)) {
            return this.getEncryptionKeyForRobot(itemStack);
        }
        if (OCUtils.isDrone(itemStack)) {
            return this.getEncryptionKeyForDrone(itemStack);
        }

        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        return itemStack.getTagCompound().getString("key");
    }

    private String getEncryptionKeyForRobot(final ItemStack itemStack) {
        final RobotData robotData = new RobotData(itemStack);

        final ItemStack component = OCUtils.getComponent(robotData, ItemEnum.OCUPGRADE.getItem());
        if (component == null) {
            return "";
        }

        return this.getEncryptionKey(component);
    }

    private String getEncryptionKeyForDrone(final ItemStack itemStack) {
        final DroneData droneData = new DroneData(itemStack);

        final ItemStack component = OCUtils.getComponent(droneData, ItemEnum.OCUPGRADE.getItem());
        if (component == null) {
            return "";
        }

        return this.getEncryptionKey(component);
    }

    @Override
    public void setEncryptionKey(final ItemStack itemStack, final String encryptionKey, final String name) {
        if (itemStack == null) {
            return;
        }
        if (OCUtils.isRobot(itemStack)) {
            this.setEncryptionKeyForRobot(itemStack, encryptionKey, name);
            return;
        }
        if (OCUtils.isDrone(itemStack)) {
            this.setEncryptionKeyForDrone(itemStack, encryptionKey, name);

            return;
        }

        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }

        itemStack.getTagCompound().setString("key", encryptionKey);
    }

    private void setEncryptionKeyForRobot(final ItemStack itemStack, final String encryptionKey, final String name) {
        final RobotData robot = new RobotData(itemStack);
        final ItemStack component = OCUtils.getComponent(robot, ItemEnum.OCUPGRADE.getItem());

        if (component != null) {
            this.setEncryptionKey(itemStack, encryptionKey, name);
        }

        robot.save(itemStack);
    }

    private void setEncryptionKeyForDrone(final ItemStack itemStack, final String encryptionKey, final String name) {
        final DroneData drone = new DroneData(itemStack);
        final ItemStack component = OCUtils.getComponent(drone, ItemEnum.OCUPGRADE.getItem());

        if (component != null) {
            this.setEncryptionKey(itemStack, encryptionKey, name);
        }

        drone.save(itemStack);
    }

    @Override
    public boolean canHandle(final ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        final net.minecraft.item.Item item = itemStack.getItem();

        if (item == ItemEnum.OCUPGRADE.getItem()) {
            return true;
        }

        final boolean robotCheck = OCUtils.isRobot(itemStack)
                && OCUtils.getComponent(new RobotData(itemStack), ItemEnum.OCUPGRADE.getItem()) != null;
        final boolean droneCheck = OCUtils.isDrone(itemStack)
                && OCUtils.getComponent(new DroneData(itemStack), ItemEnum.OCUPGRADE.getItem()) != null;

        return robotCheck || droneCheck;
    }

    @Override
    public boolean usePower(final EntityPlayer entityPlayer, final double v, final ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean hasPower(final EntityPlayer entityPlayer, final double v, final ItemStack itemStack) {
        return true;
    }

    @Override
    public IConfigManager getConfigManager(final ItemStack itemStack) {
        return null;
    }

    @Override
    public IGuiHandler getGuiHandler(final ItemStack itemStack) {
        return null;
    }
}
