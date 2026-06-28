package appeng.ext.wut.network;

import appeng.api.AEApi;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.features.IWirelessTermRegistry;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.GuiBridge;
import appeng.ext.wut.ItemWirelessUniversalTerminal;
import appeng.util.Platform;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * WUT网络处理器
 * 处理终端切换的网络消息
 */
public class WUTNetworkHandler {

    /**
     * 打开终端GUI
     */
    public static void openTerminalGui(ItemStack terminal, EntityPlayer player, byte mode, int slot) {
        if (player instanceof EntityPlayerMP) {
            IWirelessTermRegistry registry = AEApi.instance().registries().wireless();
            if (!registry.isWirelessTerminal(terminal)) {
                player.sendMessage(PlayerMessages.DeviceNotWirelessTerminal.get());
                return;
            }

            IWirelessTermHandler handler = registry.getWirelessTerminalHandler(terminal);
            String unparsedKey = handler.getEncryptionKey(terminal);
            if (unparsedKey.isEmpty()) {
                player.sendMessage(PlayerMessages.DeviceNotLinked.get());
                return;
            }

            long parsedKey = Long.parseLong(unparsedKey);
            if (AEApi.instance().registries().locatable().getLocatableBy(parsedKey) == null) {
                player.sendMessage(PlayerMessages.StationCanNotBeLocated.get());
                return;
            }

            if (handler.hasPower(player, 0.5F, terminal)) {
                // 设置当前模式
                if (terminal.getItem() instanceof ItemWirelessUniversalTerminal) {
                    ItemWirelessUniversalTerminal wut = (ItemWirelessUniversalTerminal) terminal.getItem();
                    wut.setCurrentMode(terminal, mode);
                }

                // 获取对应模式的GUI处理器
                GuiBridge guiBridge = (GuiBridge) ItemWirelessUniversalTerminal.getGuiHandlerForMode(mode);

                // 使用Platform.openGUI正确打开GUI
                Platform.openGUI(player, slot, guiBridge, false);
            } else {
                player.sendMessage(PlayerMessages.DeviceNotPowered.get());
            }
        }
    }

    /**
     * 切换终端消息
     */
    public static class CycleTerminalMessage implements IMessage {
        private byte direction; // 1 = next, -1 = previous

        public CycleTerminalMessage() {
        }

        public CycleTerminalMessage(byte direction) {
            this.direction = direction;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            direction = buf.readByte();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeByte(direction);
        }

        public static class Handler implements IMessageHandler<CycleTerminalMessage, IMessage> {
            @Override
            public IMessage onMessage(CycleTerminalMessage message, MessageContext ctx) {
                EntityPlayerMP player = ctx.getServerHandler().player;
                player.getServer().addScheduledTask(() -> {
                    ItemStack heldItem = player.getHeldItemMainhand();
                    if (heldItem.getItem() instanceof ItemWirelessUniversalTerminal) {
                        ItemWirelessUniversalTerminal wut = (ItemWirelessUniversalTerminal) heldItem.getItem();
                        if (message.direction > 0) {
                            wut.cycleToNextMode(heldItem);
                        } else {
                            wut.cycleToPreviousMode(heldItem);
                        }

                        // 重新打开GUI
                        byte newMode = wut.getCurrentMode(heldItem);
                        openTerminalGui(heldItem, player, newMode, player.inventory.currentItem);
                    }
                });
                return null;
            }
        }
    }
}
