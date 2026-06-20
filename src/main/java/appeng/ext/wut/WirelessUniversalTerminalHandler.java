package appeng.ext.wut;

import appeng.api.AEApi;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;
import appeng.core.sync.GuiBridge;
import appeng.items.tools.powered.ToolWirelessTerminal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.IGuiHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 无线通用终端处理器
 * 管理所有可用的无线终端定义，处理终端切换逻辑
 */
public class WirelessUniversalTerminalHandler {
    private static final Map<String, WirelessTerminalDefinition> terminalDefinitions = new LinkedHashMap<>();
    private static final List<String> terminalOrder = new ArrayList<>();

    /**
     * 初始化默认的终端定义
     */
    public static void init() {
        // 注册基础无线终端
        registerTerminal("item", 
            AEApi.instance().definitions().items().wirelessTerminal().maybeItem().orElse(null),
            GuiBridge.GUI_WIRELESS_TERM,
            "gui.appliedenergistics2.wireless_terminal");

        // 注册无线合成终端
        registerTerminal("crafting",
            AEApi.instance().definitions().items().wirelessCraftingTerminal().maybeItem().orElse(null),
            GuiBridge.GUI_WIRELESS_CRAFTING_TERMINAL,
            "gui.appliedenergistics2.wireless_crafting_terminal");

        // 注册无线流体终端
        registerTerminal("fluid",
            AEApi.instance().definitions().items().wirelessFluidTerminal().maybeItem().orElse(null),
            GuiBridge.GUI_WIRELESS_FLUID_TERMINAL,
            "gui.appliedenergistics2.wireless_fluid_terminal");

        // 注册无线模式终端
        registerTerminal("pattern",
            AEApi.instance().definitions().items().wirelessPatternTerminal().maybeItem().orElse(null),
            GuiBridge.GUI_WIRELESS_PATTERN_TERMINAL,
            "gui.appliedenergistics2.wireless_pattern_terminal");

        // 注册无线接口终端
        registerTerminal("interface",
            AEApi.instance().definitions().items().wirelessInterfaceTerminal().maybeItem().orElse(null),
            GuiBridge.GUI_WIRELESS_INTERFACE_TERMINAL,
            "gui.appliedenergistics2.wireless_interface_terminal");
    }

    /**
     * 注册一个新的终端类型
     */
    public static void registerTerminal(String name, Item terminalItem, IGuiHandler guiHandler, String displayName) {
        if (terminalItem == null || terminalOrder.contains(name)) {
            return;
        }

        // 创建一个简单的处理器
        IWirelessTermHandler handler = new IWirelessTermHandler() {
            @Override
            public boolean canHandle(ItemStack is) {
                return !is.isEmpty() && is.getItem() == terminalItem;
            }

            @Override
            public boolean usePower(EntityPlayer player, double amount, ItemStack is) {
                return false;
            }

            @Override
            public boolean hasPower(EntityPlayer player, double amount, ItemStack is) {
                return true;
            }

            @Override
            public IConfigManager getConfigManager(ItemStack is) {
                return null;
            }

            @Override
            public IGuiHandler getGuiHandler(ItemStack is) {
                return guiHandler;
            }

            @Override
            public String getEncryptionKey(ItemStack item) {
                return "";
            }

            @Override
            public void setEncryptionKey(ItemStack item, String encKey, String name) {
                // No-op for default handler
            }
        };

        WirelessTerminalDefinition definition = new WirelessTerminalDefinition(
            name, terminalItem, handler, guiHandler, displayName
        );

        terminalDefinitions.put(name, definition);
        terminalOrder.add(name);
    }

    /**
     * 获取当前激活的终端名称
     */
    public static String getCurrentTerminal(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) {
            return terminalOrder.isEmpty() ? "" : terminalOrder.get(0);
        }

        NBTTagCompound tag = stack.getTagCompound();
        String currentTerminal = tag.getString("currentTerminal");

        if (currentTerminal.isEmpty() || !terminalDefinitions.containsKey(currentTerminal)) {
            return terminalOrder.isEmpty() ? "" : terminalOrder.get(0);
        }

        return currentTerminal;
    }

    /**
     * 设置当前激活的终端
     */
    public static void setCurrentTerminal(ItemStack stack, String terminal) {
        if (stack.isEmpty() || !terminalDefinitions.containsKey(terminal)) {
            return;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        tag.setString("currentTerminal", terminal);
    }

    /**
     * 检查 WUT 是否包含特定终端
     */
    public static boolean hasTerminal(ItemStack stack, String terminalName) {
        if (stack.isEmpty() || !terminalOrder.contains(terminalName)) {
            return false;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            return false;
        }

        return tag.getBoolean("has_" + terminalName);
    }

    /**
     * 添加终端到 WUT
     */
    public static void addTerminal(ItemStack stack, String terminalName) {
        if (stack.isEmpty() || !terminalOrder.contains(terminalName)) {
            return;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }

        tag.setBoolean("has_" + terminalName, true);

        // 如果是第一个终端，设置为当前终端
        if (!tag.hasKey("currentTerminal")) {
            tag.setString("currentTerminal", terminalName);
        }
    }

    /**
     * 移除终端从 WUT
     */
    public static void removeTerminal(ItemStack stack, String terminalName) {
        if (stack.isEmpty() || !terminalOrder.contains(terminalName)) {
            return;
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            return;
        }

        tag.setBoolean("has_" + terminalName, false);

        // 如果移除的是当前终端，切换到第一个可用的终端
        String currentTerminal = tag.getString("currentTerminal");
        if (currentTerminal.equals(terminalName)) {
            for (String terminal : terminalOrder) {
                if (tag.getBoolean("has_" + terminal)) {
                    tag.setString("currentTerminal", terminal);
                    return;
                }
            }
            tag.removeTag("currentTerminal");
        }
    }

    /**
     * 切换到下一个终端
     */
    public static void cycleTerminalNext(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        String currentTerminal = getCurrentTerminal(stack);
        int currentIndex = terminalOrder.indexOf(currentTerminal);

        // 找到下一个已安装的终端
        for (int i = 1; i <= terminalOrder.size(); i++) {
            int nextIndex = (currentIndex + i) % terminalOrder.size();
            String nextTerminal = terminalOrder.get(nextIndex);
            if (hasTerminal(stack, nextTerminal)) {
                setCurrentTerminal(stack, nextTerminal);
                return;
            }
        }
    }

    /**
     * 切换到上一个终端
     */
    public static void cycleTerminalPrevious(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        String currentTerminal = getCurrentTerminal(stack);
        int currentIndex = terminalOrder.indexOf(currentTerminal);

        // 找到上一个已安装的终端
        for (int i = 1; i <= terminalOrder.size(); i++) {
            int prevIndex = (currentIndex - i + terminalOrder.size()) % terminalOrder.size();
            String prevTerminal = terminalOrder.get(prevIndex);
            if (hasTerminal(stack, prevTerminal)) {
                setCurrentTerminal(stack, prevTerminal);
                return;
            }
        }
    }

    /**
     * 获取已安装的终端数量
     */
    public static int getInstalledTerminalCount(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (String terminal : terminalOrder) {
            if (hasTerminal(stack, terminal)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取当前终端的 GUI 处理器
     */
    public static IGuiHandler getCurrentGuiHandler(ItemStack stack) {
        String currentTerminal = getCurrentTerminal(stack);
        WirelessTerminalDefinition definition = terminalDefinitions.get(currentTerminal);
        return definition != null ? definition.getGuiHandler() : null;
    }

    /**
     * 获取所有终端定义
     */
    public static Map<String, WirelessTerminalDefinition> getTerminalDefinitions() {
        return terminalDefinitions;
    }

    /**
     * 获取终端顺序
     */
    public static List<String> getTerminalOrder() {
        return terminalOrder;
    }
}
