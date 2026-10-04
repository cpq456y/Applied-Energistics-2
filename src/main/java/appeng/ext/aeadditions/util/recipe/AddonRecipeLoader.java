package appeng.ext.aeadditions.util.recipe;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import appeng.ext.aeadditions.Constants;

/**
 * Replays Forge's JSON recipe loading for the merged AE-Additions content.
 *
 * <p>Forge loads recipes strictly per mod container:
 * {@code CraftingHelper.loadRecipes(true)} iterates {@code Loader.getActiveModList()} and reads
 * {@code assets/<modId>/recipes} from each container's own source. The merged code deliberately has no
 * {@code aeadditions} container any more, so {@code assets/aeadditions/recipes/**} is never scanned and every
 * recipe silently disappears (no parse error is logged, because the files are never opened). Its
 * {@code _factories.json} is skipped for the same reason, which also leaves the {@code mod_integration}
 * condition type unregistered.
 *
 * <p>This loader is invoked from {@code AEAdditionsIntegration#init()}, which runs right after
 * {@code CraftingHelper.loadRecipes(false)}: at that point every factory is registered (vanilla, forge and
 * AE2's own {@code appliedenergistics2:part}) while the recipe registry is still open for registration.
 *
 * <p>The recipes reference shared ingredients as {@code "#aeadditions:iron"}, i.e. through Forge's ingredient
 * constants, so {@code _constants.json} has to be loaded as well. Forge's own
 * {@code JsonContext#loadConstants} is package private, therefore the constants are loaded into a small
 * subclass that overrides the public {@code getConstant} lookup.
 */
public final class AddonRecipeLoader {

    private static final String BASE = "assets/" + Constants.MOD_ID + "/recipes";

    private AddonRecipeLoader() {
    }

    public static void loadRecipes() {
        CraftingHelper.register(new ResourceLocation(Constants.MOD_ID, "mod_integration"),
                new ConditionModIntegration());

        final AddonJsonContext ctx = new AddonJsonContext(Constants.MOD_ID);

        try (BufferedReader reader = openReader(BASE + "/_constants.json")) {
            if (reader != null) {
                ctx.load(CraftingHelper.GSON.fromJson(reader, JsonObject[].class));
            }
        } catch (final Throwable t) {
            FMLLog.log.error("[aeadditions] Failed to load " + BASE + "/_constants.json", t);
        }

        int loaded = 0;
        int skipped = 0;

        for (final String path : findRecipeFiles()) {
            final String relative = path.substring(BASE.length() + 1);

            if (!relative.endsWith(".json") || relative.startsWith("_")) {
                continue;
            }

            final String name = relative.substring(0, relative.length() - ".json".length());

            try (BufferedReader reader = openReader(path)) {
                if (reader == null) {
                    continue;
                }

                final JsonObject json = JsonUtils.fromJson(CraftingHelper.GSON, reader, JsonObject.class);

                if (!CraftingHelper.processConditions(json, "conditions", ctx)) {
                    skipped++;
                    continue;
                }

                final IRecipe recipe = CraftingHelper.getRecipe(json, ctx);
                if (recipe == null) {
                    skipped++;
                    continue;
                }

                ForgeRegistries.RECIPES
                        .register(recipe.setRegistryName(new ResourceLocation(Constants.MOD_ID, name)));
                loaded++;
            } catch (final Throwable t) {
                FMLLog.log.error("[aeadditions] Failed to load recipe " + path, t);
            }
        }

        FMLLog.log.info("[aeadditions] Loaded " + loaded + " addon recipes (" + skipped + " skipped)");
    }

    /** Lists every entry below {@value #BASE}, working both from a directory (dev) and from a jar (release). */
    private static List<String> findRecipeFiles() {
        final List<String> out = new ArrayList<>();

        try {
            final URL url = AddonRecipeLoader.class.getClassLoader().getResource(BASE);
            if (url == null) {
                FMLLog.log.error("[aeadditions] Recipe folder " + BASE + " not found on the classpath");
                return out;
            }

            if ("jar".equals(url.getProtocol())) {
                final JarURLConnection connection = (JarURLConnection) url.openConnection();
                try (JarFile jar = connection.getJarFile()) {
                    final Enumeration<JarEntry> entries = jar.entries();
                    while (entries.hasMoreElements()) {
                        final String entry = entries.nextElement().getName();
                        if (entry.startsWith(BASE + "/") && entry.endsWith(".json")) {
                            out.add(entry);
                        }
                    }
                }
            } else if ("file".equals(url.getProtocol())) {
                final Path root = Paths.get(url.toURI());
                try (Stream<Path> walk = Files.walk(root)) {
                    walk.filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".json"))
                            .forEach(p -> out.add(BASE + "/"
                                    + root.relativize(p).toString().replace(File.separatorChar, '/')));
                }
            }
        } catch (final Exception e) {
            FMLLog.log.error("[aeadditions] Unable to enumerate " + BASE, e);
        }

        return out;
    }

    private static BufferedReader openReader(final String path) throws IOException {
        final InputStream in = AddonRecipeLoader.class.getClassLoader().getResourceAsStream(path);
        return in == null ? null : new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    /**
     * {@link JsonContext} whose constant table can be filled from outside; required because Forge's own
     * {@code loadConstants} is package private.
     */
    private static final class AddonJsonContext extends JsonContext {

        private final Map<String, Ingredient> constants = new HashMap<>();

        AddonJsonContext(final String modId) {
            super(modId);
        }

        void load(final JsonObject[] jsons) {
            for (final JsonObject json : jsons) {
                final String name = JsonUtils.getString(json, "name");
                this.constants.put(name,
                        CraftingHelper.getIngredient(JsonUtils.getJsonArray(json, "ingredient"), this));
            }
        }

        @Override
        public Ingredient getConstant(final String name) {
            return this.constants.get(name);
        }
    }
}
