package appeng.ext.aeadditions.util.datafix;

import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;

import appeng.ext.aeadditions.Constants;

/** Ported from AEDataFixers.kt. */
public final class AEDataFixers {

    private AEDataFixers() {
    }

    public static void register() {
        final ModFixs fixes = FMLCommonHandler.instance().getDataFixer().init(Constants.MOD_ID, 4);
        fixes.registerFix(FixTypes.ITEM_INSTANCE, new BasicCellDataFixer());
    }
}
