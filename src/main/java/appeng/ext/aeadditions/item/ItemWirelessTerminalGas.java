package appeng.ext.aeadditions.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.ext.aeadditions.api.AEAApi;
import appeng.ext.aeadditions.api.IWirelessGasTermHandler;
import appeng.ext.aeadditions.models.ModelManager;

/** Ported from ItemWirelessTerminalGas.kt (Kotlin {@code object}, kept as a singleton). */
public final class ItemWirelessTerminalGas extends WirelessTermBase implements IWirelessGasTermHandler {

    public static final ItemWirelessTerminalGas INSTANCE = new ItemWirelessTerminalGas();

    private ItemWirelessTerminalGas() {
        AEAApi.instance().registerWirelessTermHandler(this);
    }

    @Override
    public String getTranslationKey(final ItemStack stack) {
        return super.getTranslationKey(stack).replace("item.appeng.ext.aeadditions", "appeng.ext.aeadditions.item");
    }

    @Override
    public boolean isItemNormalWirelessTermToo(final ItemStack is) {
        return false;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        return new ActionResult<>(EnumActionResult.SUCCESS,
                AEAApi.instance().openWirelessGasTerminal(player, hand, world));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModel(final Item item, final ModelManager manager) {
        if (manager != null) {
            manager.registerItemModel(item, 0, "terminals/fluid_wireless");
        }
    }
}
