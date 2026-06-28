package appeng.ext.wut;

import appeng.api.config.Actionable;
import appeng.api.util.IConfigManager;
import appeng.core.sync.GuiBridge;
import appeng.ext.wut.network.WUTNetworkHandler;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.util.ConfigManager;
import appeng.util.Platform;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Wireless Universal Terminal - can hold multiple wireless terminal modes and switch between them
 */
public class ItemWirelessUniversalTerminal extends ToolWirelessTerminal {

    public static final String NAME = "wireless_universal_terminal";

    public static final byte MODE_ITEM = 0;
    public static final byte MODE_CRAFTING = 1;
    public static final byte MODE_FLUID = 2;
    public static final byte MODE_PATTERN = 3;
    public static final byte MODE_INTERFACE = 4;

    public ItemWirelessUniversalTerminal() {
        super();
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (world.isRemote) {
            return new ActionResult<>(EnumActionResult.FAIL, player.getHeldItem(hand));
        }

        ItemStack item = player.getHeldItem(hand);
        if (!item.hasTagCompound()) {
            return new ActionResult<>(EnumActionResult.SUCCESS, item);
        }

        final NBTTagCompound tag = item.getTagCompound();
        int slot = hand == EnumHand.MAIN_HAND ? player.inventory.currentItem : 40;

        if (!tag.hasKey("modes", 11)) {
            player.sendMessage(new net.minecraft.util.text.TextComponentString(
                net.minecraft.util.text.TextFormatting.RED + "No terminal modes available"));
            return new ActionResult<>(EnumActionResult.SUCCESS, item);
        }

        int[] modes = tag.getIntArray("modes");
        if (modes.length == 0) {
            player.sendMessage(new net.minecraft.util.text.TextComponentString(
                net.minecraft.util.text.TextFormatting.RED + "No terminal modes available"));
            return new ActionResult<>(EnumActionResult.SUCCESS, item);
        }

        byte mode = tag.getByte("mode");
        
        if (!hasMode(item, mode)) {
            mode = (byte) modes[0];
            tag.setByte("mode", mode);
        }

        WUTNetworkHandler.openTerminalGui(item, player, mode, slot);

        return new ActionResult<>(EnumActionResult.SUCCESS, item);
    }

    @Override
    public boolean canHandle(ItemStack is) {
        return is.getItem() instanceof ItemWirelessUniversalTerminal;
    }

    @Override
    public boolean usePower(EntityPlayer player, double amount, ItemStack is) {
        return this.extractAEPower(is, amount, Actionable.MODULATE) >= amount - 0.5;
    }

    @Override
    public boolean hasPower(EntityPlayer player, double amt, ItemStack is) {
        return this.getAECurrentPower(is) >= amt;
    }

    @Override
    public IConfigManager getConfigManager(ItemStack target) {
        final ConfigManager out = new ConfigManager((manager, settingName, newValue) -> {
            final NBTTagCompound data = Platform.openNbtData(target);
            manager.writeToNBT(data);
        });

        out.readFromNBT(Platform.openNbtData(target).copy());
        return out;
    }

    @Override
    public String getEncryptionKey(ItemStack item) {
        final NBTTagCompound tag = Platform.openNbtData(item);
        return tag.getString("encryptionKey");
    }

    @Override
    public void setEncryptionKey(ItemStack item, String encKey, String name) {
        final NBTTagCompound tag = Platform.openNbtData(item);
        tag.setString("encryptionKey", encKey);
        tag.setString("name", name);
    }

    @Override
    public IGuiHandler getGuiHandler(ItemStack is) {
        if (is.hasTagCompound()) {
            byte mode = is.getTagCompound().getByte("mode");
            return getGuiHandlerForMode(mode);
        }
        return null;
    }

    public static IGuiHandler getGuiHandlerForMode(byte mode) {
        switch (mode) {
            case MODE_CRAFTING:
                return GuiBridge.GUI_WIRELESS_CRAFTING_TERMINAL;
            case MODE_FLUID:
                return GuiBridge.GUI_WIRELESS_FLUID_TERMINAL;
            case MODE_PATTERN:
                return GuiBridge.GUI_WIRELESS_PATTERN_TERMINAL;
            case MODE_INTERFACE:
                return GuiBridge.GUI_WIRELESS_INTERFACE_TERMINAL;
            default:
                return GuiBridge.GUI_WIRELESS_TERM;
        }
    }

    public boolean hasMode(ItemStack stack, byte mode) {
        if (!stack.hasTagCompound()) {
            return false;
        }
        NBTTagCompound tag = stack.getTagCompound();
        if (!tag.hasKey("modes", 11)) {
            return false;
        }
        int[] modes = tag.getIntArray("modes");
        for (int m : modes) {
            if (m == mode) {
                return true;
            }
        }
        return false;
    }

    public byte getCurrentMode(ItemStack stack) {
        if (stack.hasTagCompound()) {
            return stack.getTagCompound().getByte("mode");
        }
        return MODE_ITEM;
    }

    public void setCurrentMode(ItemStack stack, byte mode) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setByte("mode", mode);
    }

    public void cycleToNextMode(ItemStack stack) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("modes", 11)) {
            return;
        }

        int[] modes = stack.getTagCompound().getIntArray("modes");
        if (modes.length == 0) {
            return;
        }

        byte currentMode = getCurrentMode(stack);
        int currentIndex = -1;

        // 找到当前模式的索引
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == currentMode) {
                currentIndex = i;
                break;
            }
        }

        // 切换到下一个模式
        int nextIndex = (currentIndex + 1) % modes.length;
        setCurrentMode(stack, (byte) modes[nextIndex]);
    }

    /**
     * 切换到上一个模式
     */
    public void cycleToPreviousMode(ItemStack stack) {
        if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey("modes", 11)) {
            return;
        }

        int[] modes = stack.getTagCompound().getIntArray("modes");
        if (modes.length == 0) {
            return;
        }

        byte currentMode = getCurrentMode(stack);
        int currentIndex = -1;

        // 找到当前模式的索引
        for (int i = 0; i < modes.length; i++) {
            if (modes[i] == currentMode) {
                currentIndex = i;
                break;
            }
        }

        // 切换到上一个模式
        int prevIndex = (currentIndex - 1 + modes.length) % modes.length;
        setCurrentMode(stack, (byte) modes[prevIndex]);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addCheckedInformation(ItemStack stack, World world, List<String> lines, ITooltipFlag advancedTooltips) {
        super.addCheckedInformation(stack, world, lines, advancedTooltips);
        
        if (stack.hasTagCompound()) {
            final NBTTagCompound tag = Platform.openNbtData(stack);
            
            if (tag.hasKey("modes", 11)) {
                int[] modes = tag.getIntArray("modes");
                if (modes.length > 0) {
                    byte mode = tag.getByte("mode");
                    lines.add(TextFormatting.AQUA + "Current mode: " + getModeName(mode));
                    
                    lines.add(TextFormatting.GRAY + "Installed terminals: " + modes.length);
                    for (int m : modes) {
                        lines.add(TextFormatting.WHITE + "  - " + getModeName((byte) m));
                    }
                } else {
                    lines.add(TextFormatting.RED + "No terminal modes available");
                }
            } else {
                lines.add(TextFormatting.RED + "No terminal modes available");
            }
        }

        lines.add(TextFormatting.YELLOW + "Click button in GUI to switch terminals");
    }

    public static String getModeName(byte mode) {
        switch (mode) {
            case MODE_ITEM:
                return "物品终端";
            case MODE_CRAFTING:
                return "合成终端";
            case MODE_FLUID:
                return "流体终端";
            case MODE_PATTERN:
                return "样板终端";
            case MODE_INTERFACE:
                return "接口终端";
            default:
                return "未知终端";
        }
    }

    @Override
    protected void getCheckedSubItems(CreativeTabs creativeTab, NonNullList<ItemStack> itemStacks) {
        ItemStack uncharged = new ItemStack(this, 1);
        NBTTagCompound unchargedTag = Platform.openNbtData(uncharged);
        unchargedTag.setDouble("internalCurrentPower", 0);
        unchargedTag.setDouble("internalMaxPower", this.getAEMaxPower(uncharged));
        itemStacks.add(uncharged);

        ItemStack charged = new ItemStack(this, 1);
        NBTTagCompound chargedTag = Platform.openNbtData(charged);
        chargedTag.setDouble("internalCurrentPower", this.getAEMaxPower(charged));
        chargedTag.setDouble("internalMaxPower", this.getAEMaxPower(charged));
        itemStacks.add(charged);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
}
