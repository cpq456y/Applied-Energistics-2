package appeng.ext.wut;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
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
 * 无线通用终端物品
 * 可以包含多个无线终端并在它们之间切换
 */
public class ItemWirelessUniversalTerminal extends ToolWirelessTerminal {

    public static final String NAME = "wireless_universal_terminal";

    // 终端模式常量
    public static final byte MODE_ITEM = 0;
    public static final byte MODE_CRAFTING = 1;
    public static final byte MODE_FLUID = 2;
    public static final byte MODE_PATTERN = 3;
    public static final byte MODE_INTERFACE = 4; // 接口终端模式

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

        // 检查是否有 modes 数组
        if (!tag.hasKey("modes", 11)) {
            player.sendMessage(new net.minecraft.util.text.TextComponentString(
                net.minecraft.util.text.TextFormatting.RED + "没有任何可用终端"));
            return new ActionResult<>(EnumActionResult.SUCCESS, item);
        }

        int[] modes = tag.getIntArray("modes");
        if (modes.length == 0) {
            player.sendMessage(new net.minecraft.util.text.TextComponentString(
                net.minecraft.util.text.TextFormatting.RED + "没有任何可用终端"));
            return new ActionResult<>(EnumActionResult.SUCCESS, item);
        }

        byte mode = tag.getByte("mode");
        
        // 如果当前模式不在 modes 数组中，使用第一个可用的模式
        if (!hasMode(item, mode)) {
            mode = (byte) modes[0];
            tag.setByte("mode", mode);
        }

        // 打开对应模式的GUI
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

    /**
     * 根据模式获取对应的GUI处理器
     */
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

    /**
     * 检查物品是否包含指定模式
     */
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

    /**
     * 获取当前模式
     */
    public byte getCurrentMode(ItemStack stack) {
        if (stack.hasTagCompound()) {
            return stack.getTagCompound().getByte("mode");
        }
        return MODE_ITEM;
    }

    /**
     * 设置当前模式
     */
    public void setCurrentMode(ItemStack stack, byte mode) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setByte("mode", mode);
    }

    /**
     * 切换到下一个模式
     */
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
        // 调用父类显示电量信息（但不显示链接状态，因为父类会显示）
        super.addCheckedInformation(stack, world, lines, advancedTooltips);
        
        // 注意：父类 ToolWirelessTerminal.addCheckedInformation 已经显示了链接状态
        // 所以我们只需要显示模式信息
        
        // 显示已安装的模式列表
        if (stack.hasTagCompound()) {
            final NBTTagCompound tag = Platform.openNbtData(stack);
            
            if (tag.hasKey("modes", 11)) {
                int[] modes = tag.getIntArray("modes");
                if (modes.length > 0) {
                    // 显示当前模式
                    byte mode = tag.getByte("mode");
                    lines.add(TextFormatting.AQUA + "当前模式: " + getModeName(mode));
                    
                    lines.add(TextFormatting.GRAY + "已安装终端: " + modes.length);
                    for (int m : modes) {
                        lines.add(TextFormatting.WHITE + "  - " + getModeName((byte) m));
                    }
                } else {
                    lines.add(TextFormatting.RED + "没有任何可用终端");
                }
            } else {
                lines.add(TextFormatting.RED + "没有任何可用终端");
            }
        }

        // 显示切换提示
        lines.add(TextFormatting.YELLOW + "在GUI中点击按钮切换终端");
    }

    /**
     * 获取模式名称
     */
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
        // 注册无电版（先）
        ItemStack uncharged = new ItemStack(this, 1);
        NBTTagCompound unchargedTag = Platform.openNbtData(uncharged);
        unchargedTag.setDouble("internalCurrentPower", 0);
        unchargedTag.setDouble("internalMaxPower", this.getAEMaxPower(uncharged));
        // 不设置 modes 数组，保持空白
        itemStacks.add(uncharged);

        // 注册空白版（有电无模式，后）
        ItemStack charged = new ItemStack(this, 1);
        NBTTagCompound chargedTag = Platform.openNbtData(charged);
        chargedTag.setDouble("internalCurrentPower", this.getAEMaxPower(charged));
        chargedTag.setDouble("internalMaxPower", this.getAEMaxPower(charged));
        // 不设置 modes 数组，保持空白
        itemStacks.add(charged);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
}
