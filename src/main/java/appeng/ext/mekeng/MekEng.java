package appeng.ext.mekeng;

import appeng.api.config.TunnelType;
import appeng.ext.mekeng.proxy.ClientProxy;
import appeng.ext.mekeng.proxy.CommonProxy;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;

/**
 * Mekanism Energistics (MkE) bootstrap, merged into the AE2 mod container as {@code appeng.ext.mekeng}.
 *
 * <p>The original standalone {@code @Mod} entry point has been removed: the content now lives inside the
 * {@code appliedenergistics2} container and is driven from {@code appeng.core.Registration}. What is kept is
 * {@link #INSTANCE} (used as the network/GUI handler key) and the {@link #GAS} P2P tunnel type.
 *
 * <p>The coremod part of MkE is NOT merged: see {@code appeng.ext.mekeng.core.MkECore}, which stays a separate
 * {@code FMLCorePlugin} because its ASM transformers patch AE2 classes before they are loaded and therefore
 * cannot live inside the mod itself.
 */
public final class MekEng {

    /** P2P tunnel type added for gases. */
    public static final TunnelType GAS;

    static {
        // add P2P type
        GAS = EnumHelper.addEnum(TunnelType.class, "GAS", new Class[0]);
    }

    /**
     * GUI-handler key used by {@code EntityPlayer#openGui}. FML resolves the key through
     * {@code FMLCommonHandler.findContainerFor}, which accepts a mod id string or a mod object, so the AE2 mod
     * id is used here and resolves to the AE2 ModContainer (the merged content has no container of its own).
     */
    public static final Object INSTANCE = appeng.core.AppEng.MOD_ID;

    public static CommonProxy proxy;

    public static Logger log;

    private MekEng() {
    }

    public static void preInit(final FMLPreInitializationEvent event) {
        log = event.getModLog();
        proxy = FMLCommonHandler.instance().getSide() == Side.CLIENT ? new ClientProxy() : new CommonProxy();
        proxy.preInit(event);
    }

    public static void init(final FMLInitializationEvent event) {
        proxy.init(event);
    }

    public static void postInit(final FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    /** Item/block/GUI id inside the merged namespace. */
    public static ResourceLocation id(final String path) {
        return new ResourceLocation(appeng.core.AppEng.MOD_ID, path);
    }
}
