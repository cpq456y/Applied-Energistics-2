package appeng.ext.aeadditions.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AEPartLocation;
import appeng.ext.aeadditions.AEAdditionsIntegration;
import appeng.ext.aeadditions.api.IPortableFluidStorageCell;
import appeng.ext.aeadditions.api.IPortableGasStorageCell;
import appeng.ext.aeadditions.api.IWirelessGasTermHandler;
import appeng.ext.aeadditions.api.gas.IAEGasStack;
import appeng.ext.aeadditions.block.IGuiBlock;
import appeng.ext.aeadditions.container.fluid.ContainerFluidStorage;
import appeng.ext.aeadditions.container.gas.ContainerGasStorage;
import appeng.ext.aeadditions.gui.GuiStorage;
import appeng.ext.aeadditions.integration.mekanism.gas.MEMonitorFluidGasWrapper;
import appeng.ext.aeadditions.part.PartECBase;

/** Ported from GuiHandler.kt (Kotlin {@code object}, kept as a singleton). */
public class GuiHandler implements IGuiHandler {

    public static final GuiHandler INSTANCE = new GuiHandler();

    public EnumHand hand = null;

    public Object[] temp = new Object[0];

    public Object getContainer(final int id, final EntityPlayer player, final Object[] args) {
        switch (id) {
            case 3: {
                final IMEMonitor<IAEFluidStack> fluidInventory = (IMEMonitor<IAEFluidStack>) args[0];
                final IPortableFluidStorageCell storageCell = (IPortableFluidStorageCell) args[1];

                return new ContainerFluidStorage(fluidInventory, player, storageCell, this.hand);
            }
            case 4: {
                final MEMonitorFluidGasWrapper gasInventory = new MEMonitorFluidGasWrapper(
                        (IMEMonitor<IAEGasStack>) args[0]);
                return new ContainerGasStorage(gasInventory, player, this.hand);
            }
            case 5: {
                final MEMonitorFluidGasWrapper gasInventory = new MEMonitorFluidGasWrapper(
                        (IMEMonitor<IAEGasStack>) args[0]);
                final IWirelessGasTermHandler handler = (IWirelessGasTermHandler) args[1];
                return new ContainerGasStorage(gasInventory, player, handler, this.hand);
            }
            case 6: {
                final MEMonitorFluidGasWrapper gasInventory = new MEMonitorFluidGasWrapper(
                        (IMEMonitor<IAEGasStack>) args[0]);
                final IPortableGasStorageCell storageCell = (IPortableGasStorageCell) args[1];
                return new ContainerGasStorage(gasInventory, player, storageCell, this.hand);
            }
            default: {
                return null;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public Object getGui(final int id, final EntityPlayer player) {
        switch (id) {
            case 3:
                return new GuiStorage(new ContainerFluidStorage(player, this.hand),
                        "appeng.ext.aeadditions.item.storage.fluid.portable.name");
            case 4:
                return new GuiStorage(new ContainerGasStorage(player, this.hand),
                        "appeng.ext.aeadditions.part.gas.terminal.name");
            case 5:
                return new GuiStorage(new ContainerGasStorage(player, this.hand),
                        "appeng.ext.aeadditions.part.gas.terminal.name");
            case 6:
                return new GuiStorage(new ContainerGasStorage(player, this.hand),
                        "appeng.ext.aeadditions.item.storage.gas.portable.name");
            default:
                return null;
        }
    }

    public static int getGuiId(final int guiId) {
        return guiId + 6;
    }

    public static int getGuiId(final PartECBase part) {
        return part.getFacing().ordinal();
    }

    public static Object getPartContainer(final EnumFacing side, final EntityPlayer player, final World world,
            final int x, final int y, final int z) {
        return ((PartECBase) ((IPartHost) world.getTileEntity(new BlockPos(x, y, z))).getPart(side))
                .getServerGuiElement(player);
    }

    public static Object getPartGui(final EnumFacing side, final EntityPlayer player, final World world, final int x,
            final int y, final int z) {
        return ((PartECBase) ((IPartHost) world.getTileEntity(new BlockPos(x, y, z))).getPart(side))
                .getClientGuiElement(player);
    }

    public static void launchGui(final int id, final EntityPlayer player, final EnumHand hand, final Object[] args) {
        INSTANCE.temp = args;
        INSTANCE.hand = hand;
        player.openGui(AEAdditionsIntegration.MOD_INSTANCE, id, player.world, (int) player.posX, (int) player.posY,
                (int) player.posZ);
    }

    public static Object launchGui(final int id, final EntityPlayer player, final World world, final int x, final int y,
            final int z) {
        // EntityPlayer#openGui returns void in this Forge version, so there is no container to hand back.
        player.openGui(AEAdditionsIntegration.MOD_INSTANCE, id, world, x, y, z);
        return null;
    }

    public static boolean hasPermissions(final BlockPos pos, final AEPartLocation side, final EntityPlayer player) {
        final World world = player.world;
        final TileEntity tileEntity = world.getTileEntity(pos);

        if (tileEntity == null) {
            return true;
        } else if (tileEntity instanceof IGuiProvider) {
            return INSTANCE.securityCheck(tileEntity, player);
        } else if (tileEntity instanceof IPartHost) {
            final IPart part = ((IPartHost) tileEntity).getPart(side);

            if (part != null) {
                return INSTANCE.securityCheck(part, player);
            }
        }
        return false;
    }

    private boolean securityCheck(final Object tileEntity, final EntityPlayer player) {
        if (tileEntity instanceof IActionHost) {
            final IGridNode actionableNode = ((IActionHost) tileEntity).getActionableNode();

            if (actionableNode != null) {
                final IGrid grid = actionableNode.getGrid();

                final boolean requirePower = false;
                if (requirePower) {
                    final IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
                    if (!energyGrid.isNetworkPowered()) {
                        return false;
                    }
                }

                final ISecurityGrid securityGrid = grid.getCache(ISecurityGrid.class);

                if (securityGrid.hasPermission(player, SecurityPermissions.BUILD)) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }

    @Override
    public Object getServerGuiElement(final int id, final EntityPlayer player, final World world, final int x,
            final int y, final int z) {
        if (player == null) {
            return null;
        }

        final Object container = this.getContainerBlockElement(player, world, x, y, z);

        if (container != null) {
            return container;
        }

        EnumFacing side = null;

        if (id <= 5) {
            side = EnumFacing.VALUES[id];
        }

        final BlockPos pos = new BlockPos(x, y, z);

        final TileEntity tileEntity = world == null ? null : world.getTileEntity(pos);

        if (tileEntity == null) {
            if (id >= 6) {
                return this.getContainer(id - 6, player, this.temp);
            }

            return null;
        } else if (tileEntity instanceof IGuiProvider) {
            return ((IGuiProvider) tileEntity).getServerGuiElement(player);
        } else if (tileEntity instanceof IPartHost) {
            if (world != null && side != null) {
                return getPartContainer(side, player, world, x, y, z);
            }

            if (id >= 6) {
                return this.getContainer(id - 6, player, this.temp);
            }
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(final int id, final EntityPlayer player, final World world, final int x,
            final int y, final int z) {
        if (player == null) {
            return null;
        }

        final Object gui = this.getGuiBlockElement(player, world, x, y, z);

        if (gui != null) {
            return gui;
        }

        EnumFacing side = null;

        if (id <= 5) {
            side = EnumFacing.VALUES[id];
        }

        final BlockPos pos = new BlockPos(x, y, z);

        final TileEntity tileEntity = world == null ? null : world.getTileEntity(pos);

        if (tileEntity == null) {
            if (id >= 6) {
                return this.getGui(id - 6, player);
            }

            return null;
        } else if (tileEntity instanceof IGuiProvider) {
            return ((IGuiProvider) tileEntity).getClientGuiElement(player);
        } else if (tileEntity instanceof IPartHost) {
            if (world != null && side != null) {
                return getPartGui(side, player, world, x, y, z);
            }

            if (id >= 6) {
                return this.getGui(id - 6, player);
            }
        }

        return null;
    }

    public Object getGuiBlockElement(final EntityPlayer player, final World world, final int x, final int y,
            final int z) {
        if (world == null || player == null) {
            return null;
        }
        final BlockPos pos = new BlockPos(x, y, z);
        final net.minecraft.block.Block block = world.getBlockState(pos).getBlock();

        if (block instanceof IGuiBlock) {
            return ((IGuiBlock) block).getClientGuiElement(player, world, pos);
        }
        return null;
    }

    public Object getContainerBlockElement(final EntityPlayer player, final World world, final int x, final int y,
            final int z) {
        if (world == null || player == null) {
            return null;
        }
        final BlockPos pos = new BlockPos(x, y, z);
        final net.minecraft.block.Block block = world.getBlockState(pos).getBlock();

        if (block instanceof IGuiBlock) {
            return ((IGuiBlock) block).getServerGuiElement(player, world, pos);
        }
        return null;
    }
}
