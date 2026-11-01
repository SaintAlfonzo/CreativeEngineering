package net.satisfy.vinery.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.satisfy.vinery.api.VineryPlugin;
import net.satisfy.vinery.core.Vinery;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

/** Port: Fabric's "vinery" entrypoint as classes annotated with {@link VineryPlugin.Entry}, found in the mod scan. */
public final class VineryPlugins {
    private static final Type ENTRY = Type.getType(VineryPlugin.Entry.class);

    private VineryPlugins() {
    }

    public static List<VineryPlugin> find() {
        List<VineryPlugin> plugins = new ArrayList<>();
        for (ModFileScanData scan : ModList.get().getAllScanData()) {
            for (ModFileScanData.AnnotationData data : scan.getAnnotations()) {
                if (!ENTRY.equals(data.annotationType())) {
                    continue;
                }
                try {
                    Class<?> type = Class.forName(data.clazz().getClassName());
                    plugins.add((VineryPlugin) type.getDeclaredConstructor().newInstance());
                } catch (ReflectiveOperationException | ClassCastException e) {
                    Vinery.LOGGER.error("Could not load Vinery plugin {}", data.clazz().getClassName(), e);
                }
            }
        }
        return plugins;
    }
}
