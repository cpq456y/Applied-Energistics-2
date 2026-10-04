package appeng.ext.aeadditions.integration.opencomputers;

import li.cil.oc.api.Manual;
import li.cil.oc.api.manual.PathProvider;
import li.cil.oc.api.prefab.ItemStackTabIconRenderer;
import li.cil.oc.api.prefab.ResourceContentProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import appeng.ext.aeadditions.Constants;
import appeng.ext.aeadditions.registries.ItemEnum;

/** Ported from AEAdditionsPathProvider.kt. */
public class AEAdditionsPathProvider implements PathProvider {

    public static final AEAdditionsPathProvider INSTANCE = new AEAdditionsPathProvider();

    private AEAdditionsPathProvider() {
    }

    public static void init() {
        Manual.addProvider(INSTANCE);
        Manual.addProvider(new ResourceContentProvider(Constants.MOD_ID, "docs/"));
        Manual.addTab(new ItemStackTabIconRenderer(new ItemStack(ItemEnum.FLUIDPATTERN.getItem())),
                "itemGroup.AE_Additions", Constants.MOD_ID + "/%LANGUAGE%/index.md");
    }

    @Override
    public String pathFor(final ItemStack itemStack) {
        if (itemStack != null && itemStack.getItem() == ItemEnum.OCUPGRADE.getItem()) {
            return Constants.MOD_ID + "/%LANGUAGE%/me_upgrade.md";
        }

        return null;
    }

    @Override
    public String pathFor(final World world, final BlockPos pos) {
        return null;
    }
}
