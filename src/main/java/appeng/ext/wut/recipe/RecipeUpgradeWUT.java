package appeng.ext.wut.recipe;

import appeng.api.AEApi;
import appeng.ext.wut.ItemWirelessUniversalTerminal;
import appeng.ext.wut.WirelessUniversalTerminalHandler;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

/**
 * 合成配方：向已有的无线通用终端添加新的终端类型（包括接口终端）
 */
public class RecipeUpgradeWUT extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

    @Override
    public boolean matches(InventoryCrafting inv, World worldIn) {
        return !getCraftingResult(inv).isEmpty();
    }

    @Override
    public ItemStack getCraftingResult(InventoryCrafting inv) {
        ItemStack wut = ItemStack.EMPTY;
        ItemStack terminal = ItemStack.EMPTY;

        for (int i = 0; i < inv.getSizeInventory(); i++) {
            ItemStack stack = inv.getStackInSlot(i);
            if (stack.isEmpty()) continue;

            // 检查是否是已组合的WUT
            if (stack.getItem() instanceof ItemWirelessUniversalTerminal) {
                if (wut.isEmpty()) {
                    wut = stack;
                } else {
                    return ItemStack.EMPTY; // 只能有一个WUT
                }
            }
            // 检查是否是无线终端（包括接口终端）
            else if (isWirelessTerminal(stack)) {
                if (terminal.isEmpty()) {
                    terminal = stack;
                } else {
                    return ItemStack.EMPTY; // 只能有一个终端
                }
            }
            else {
                return ItemStack.EMPTY; // 不允许其他物品
            }
        }

        // 必须有WUT和终端
        if (wut.isEmpty() || terminal.isEmpty()) {
            return ItemStack.EMPTY;
        }

        // 获取终端类型
        byte terminalMode = getTerminalMode(terminal);
        if (terminalMode < 0) {
            return ItemStack.EMPTY;
        }

        // 检查WUT是否已经包含该终端类型
        if (wut.hasTagCompound() && wut.getTagCompound().hasKey("modes", 11)) {
            int[] modes = wut.getTagCompound().getIntArray("modes");
            for (int m : modes) {
                if (m == terminalMode) {
                    return ItemStack.EMPTY; // 已经包含该模式
                }
            }
        }

        // 复制WUT并添加新终端
        // wut.copy() 会深拷贝NBT（包括升级卡等所有数据）
        ItemStack output = wut.copy();
        output.setCount(1);
        
        // 在副本上直接修改NBT，保留所有原有数据
        net.minecraft.nbt.NBTTagCompound tag = output.getTagCompound();
        if (tag == null) {
            tag = new net.minecraft.nbt.NBTTagCompound();
            output.setTagCompound(tag);
        }
        
        // 添加新模式到数组
        java.util.List<Integer> modesList = new java.util.ArrayList<>();
        if (tag.hasKey("modes", 11)) {
            for (int m : tag.getIntArray("modes")) {
                modesList.add(m);
            }
        }
        modesList.add((int) terminalMode);
        
        int[] newModes = new int[modesList.size()];
        for (int i = 0; i < modesList.size(); i++) {
            newModes[i] = modesList.get(i);
        }
        tag.setIntArray("modes", newModes);

        return output;
    }

    @Override
    public boolean canFit(int width, int height) {
        return width >= 2 && height >= 2;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
        return ForgeHooks.defaultRecipeGetRemainingItems(inv);
    }

    /**
     * 检查物品是否是无线终端（包括接口终端）
     */
    private boolean isWirelessTerminal(ItemStack stack) {
        return AEApi.instance().definitions().items().wirelessTerminal().isSameAs(stack) ||
               AEApi.instance().definitions().items().wirelessCraftingTerminal().isSameAs(stack) ||
               AEApi.instance().definitions().items().wirelessFluidTerminal().isSameAs(stack) ||
               AEApi.instance().definitions().items().wirelessPatternTerminal().isSameAs(stack) ||
               AEApi.instance().definitions().items().wirelessInterfaceTerminal().isSameAs(stack);
    }

    /**
     * 获取终端类型名称
     */
    private String getTerminalType(ItemStack stack) {
        if (AEApi.instance().definitions().items().wirelessTerminal().isSameAs(stack)) {
            return "item";
        } else if (AEApi.instance().definitions().items().wirelessCraftingTerminal().isSameAs(stack)) {
            return "crafting";
        } else if (AEApi.instance().definitions().items().wirelessFluidTerminal().isSameAs(stack)) {
            return "fluid";
        } else if (AEApi.instance().definitions().items().wirelessPatternTerminal().isSameAs(stack)) {
            return "pattern";
        } else if (AEApi.instance().definitions().items().wirelessInterfaceTerminal().isSameAs(stack)) {
            return "interface";
        }
        return null;
    }

    /**
     * 获取终端模式常量
     */
    private byte getTerminalMode(ItemStack stack) {
        if (AEApi.instance().definitions().items().wirelessTerminal().isSameAs(stack)) {
            return ItemWirelessUniversalTerminal.MODE_ITEM;
        } else if (AEApi.instance().definitions().items().wirelessCraftingTerminal().isSameAs(stack)) {
            return ItemWirelessUniversalTerminal.MODE_CRAFTING;
        } else if (AEApi.instance().definitions().items().wirelessFluidTerminal().isSameAs(stack)) {
            return ItemWirelessUniversalTerminal.MODE_FLUID;
        } else if (AEApi.instance().definitions().items().wirelessPatternTerminal().isSameAs(stack)) {
            return ItemWirelessUniversalTerminal.MODE_PATTERN;
        } else if (AEApi.instance().definitions().items().wirelessInterfaceTerminal().isSameAs(stack)) {
            return ItemWirelessUniversalTerminal.MODE_INTERFACE;
        }
        return -1;
    }
}
