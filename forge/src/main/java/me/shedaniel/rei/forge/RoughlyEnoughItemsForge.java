package me.shedaniel.rei.forge;

import com.google.common.collect.Lists;
import me.shedaniel.architectury.platform.forge.EventBuses;
import me.shedaniel.rei.RoughlyEnoughItemsInitializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.function.Consumer;

@Mod("roughlyenoughitems")
@ApiStatus.Internal
public class RoughlyEnoughItemsForge {
    public static final Logger LOGGER = LogManager.getFormatterLogger("REI");
    
    public RoughlyEnoughItemsForge() {
        EventBuses.registerModEventBus("roughlyenoughitems", FMLJavaModLoadingContext.get().getModEventBus());
        RoughlyEnoughItemsInitializer.onInitialize();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> RoughlyEnoughItemsInitializer::onInitializeClient);
    }
    
    public static <T> void scanAnnotation(Class<T> clazz, Consumer<T> consumer) {
        scanAnnotation(Type.getType(clazz), consumer);
    }
    
    public static <T> void scanAnnotation(Type annotationType, Consumer<T> consumer) {
        List<T> instances = Lists.newArrayList();
        for (ModFileScanData data : ModList.get().getAllScanData()) {
            for (ModFileScanData.AnnotationData annotation : data.getAnnotations()) {
                if (annotationType.equals(annotation.getAnnotationType())) {
                    try {
                        T instance = (T) Class.forName(annotation.getMemberName()).getDeclaredConstructor().newInstance();
                        instances.add(instance);
                    } catch (Throwable throwable) {
                        LOGGER.error("Failed to load plugin: " + annotation.getMemberName(), throwable);
                    }
                }
            }
        }
        
        for (T instance : instances) {
            consumer.accept(instance);
        }
    }
}
