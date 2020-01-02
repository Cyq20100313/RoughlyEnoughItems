/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.api;

import com.google.common.collect.Lists;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.rei.RoughlyEnoughItemsCore;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface BaseBoundsHandler extends DisplayHelper.DisplayBoundsHandler<Screen> {
    /**
     * Gets the exclusion zones by the screen class
     *
     * @param currentScreenClass the current screen class
     * @param isOnRightSide      whether the user has set the overlay to the right
     * @return the list of exclusion zones
     */
    @Deprecated
    default List<Rectangle> getCurrentExclusionZones(Class<?> currentScreenClass, boolean isOnRightSide) {
        return getExclusionZones(currentScreenClass, false);
    }
    
    @Deprecated
    default List<Rectangle> getCurrentExclusionZones(Class<?> currentScreenClass, boolean isOnRightSide, boolean sort) {
        return getExclusionZones(currentScreenClass, sort);
    }
    
    List<Rectangle> getExclusionZones(Class<?> currentScreenClass, boolean sort);
    
    int supplierSize();
    
    /**
     * Register an exclusion zone
     *
     * @param screenClass the screen
     * @param supplier    the exclusion zone supplier, isOnRightSide -> the list of exclusion zones
     */
    @Deprecated
    default void registerExclusionZones(Class<?> screenClass, Function<Boolean, List<Rectangle>> supplier) {
        RoughlyEnoughItemsCore.LOGGER.warn("[REI] Someone is registering exclusion zones with the deprecated method: " + supplier.getClass().getName());
        registerExclusionZones(screenClass, () -> {
            List<Rectangle> zones = Lists.newArrayList(supplier.apply(false));
            zones.addAll(supplier.apply(true));
            return zones;
        });
    }
    
    void registerExclusionZones(Class<?> screenClass, Supplier<List<Rectangle>> supplier);
    
}
