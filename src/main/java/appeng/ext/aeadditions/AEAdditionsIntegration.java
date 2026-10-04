package appeng.ext.aeadditions;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;

import appeng.api.AEApi;
import appeng.ext.aeadditions.integration.Integration;
import appeng.ext.aeadditions.network.GuiHandler;
import appeng.ext.aeadditions.network.PacketHandler;
import appeng.ext.aeadditions.proxy.ClientProxy;
import appeng.ext.aeadditions.proxy.CommonProxy;
import appeng.ext.aeadditions.util.AEAConfigHandler;
import appeng.ext.aeadditions.util.AEAdditionsEventHandler;
import appeng.ext.aeadditions.util.EventHandler;
import appeng.ext.aeadditions.wireless.AEWirelessTermHandler;

/**
 * Integration bootstrap for the AE-Additions (ExtraCells 2 fork) content merged into
 * {@code appeng.ext.aeadditions}.
 * <p>
 * The original standalone {@code @Mod} entry point (and its Forgelin Kotlin language adapter) has
 * been removed: the merged content now lives inside the AE2 mod container and is driven from
 * {@code appeng.core.Registration}. Everything here is additive - no upstream AE2 class or API
 * signature is modified.
 */
public final class AEAdditionsIntegration {

    /**
     * Forge GUI-handler key. FML resolves GUI handlers (and {@code EntityPlayer#openGui}) through
     * {@code FMLCommonHandler.findContainerFor}, which accepts either a mod id or the mod object,
     * so the AE2 mod id is used here and resolves to the AE2 ModContainer.
     */
    public static final Object MOD_INSTANCE = appeng.core.AppEng.MOD_ID;

    /** Common/client proxy, created during preInit. */
    public static CommonProxy proxy;

    /** Feature/integration registry. */
    public static Integration integration;

    /** Network packet handler. */
    private static PacketHandler packetHandler;

    private static File configFolder;

    public static PacketHandler getPacketHandler() {
        return packetHandler;
    }

    public static void preInit(final FMLPreInitializationEvent event) {
        // The original @Mod object had these fields initialised before preInit ran, and
        // AEAConfigHandler.reload() forwards to integration.loadConfig(...), so create them first.
        integration = new Integration();
        proxy = FMLCommonHandler.instance().getSide() == Side.CLIENT ? new ClientProxy() : new CommonProxy();
        packetHandler = new PacketHandler();

        NetworkRegistry.INSTANCE.registerGuiHandler(MOD_INSTANCE, GuiHandler.INSTANCE);

        configFolder = event.getModConfigurationDirectory();

        final Configuration config = new Configuration(new File(configFolder, "aeadditions.cfg"));
        final AEAConfigHandler javaConfigHandler = new AEAConfigHandler(config);
        javaConfigHandler.reload();

        MinecraftForge.EVENT_BUS.register(javaConfigHandler);

        integration.preInit();

        proxy.registerItems();
        proxy.registerBlocks();
        proxy.registerModels();

    }

    public static void init() {
        AEApi.instance().registries().wireless().registerWirelessHandler(new AEWirelessTermHandler());

        MinecraftForge.EVENT_BUS.register(new AEAdditionsEventHandler());
        MinecraftForge.EVENT_BUS.register(EventHandler.INSTANCE);

        proxy.registerMovables();
        proxy.registerRenderers();
        proxy.registerTileEntities();
        proxy.registerFluidBurnTimes();
        proxy.addRecipes(configFolder);
        // Forge only loads recipes per mod container; the merged code has no container of its own, so its
        // JSON recipes have to be replayed manually (see the class comment for the full explanation).
        appeng.ext.aeadditions.util.recipe.AddonRecipeLoader.loadRecipes();
        proxy.registerPackets();

        integration.init();

        appeng.ext.aeadditions.util.datafix.AEDataFixers.register();
    }

    public static void postInit() {
        integration.postInit();
    }

    private AEAdditionsIntegration() {
    }
}
