/*
 * This file is licensed under the MIT License, part of Roughly Enough Items.
 * Copyright (c) 2018, 2019, 2020 shedaniel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.shedaniel.rei.impl.registry;

import me.shedaniel.rei.RoughlyEnoughItemsCore;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.display.LiveDisplayGenerator;
import me.shedaniel.rei.api.client.registry.display.visibility.DisplayVisibilityPredicate;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.crafting.Recipe;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class DisplayRegistryImpl extends RecipeManagerContextImpl<REIClientPlugin> implements DisplayRegistry {
    private final Map<CategoryIdentifier<?>, List<Display>> displays = new ConcurrentHashMap<>();
    private final Map<CategoryIdentifier<?>, List<LiveDisplayGenerator<?>>> displayGenerators = new ConcurrentHashMap<>();
    private final List<LiveDisplayGenerator<?>> globalDisplayGenerators = new ArrayList<>();
    private final List<DisplayVisibilityPredicate> visibilityPredicates = new ArrayList<>();
    private final List<DisplayFiller<?, ?>> fillers = new ArrayList<>();
    private final MutableInt displayCount = new MutableInt(0);
    
    @Override
    public void acceptPlugin(REIClientPlugin plugin) {
        plugin.registerDisplays(this);
    }
    
    @Override
    public int getDisplayCount() {
        return displayCount.getValue();
    }
    
    @Override
    public void registerDisplay(Display display) {
        displays.computeIfAbsent(display.getCategoryIdentifier(), location -> new ArrayList<>())
                .add(display);
        displayCount.increment();
    }
    
    public void registerDisplay(int index, Display display) {
        displays.computeIfAbsent(display.getCategoryIdentifier(), location -> new ArrayList<>())
                .add(index, display);
        displayCount.increment();
    }
    
    @Override
    public Map<CategoryIdentifier<?>, List<Display>> getAllDisplays() {
        return Collections.unmodifiableMap(displays);
    }
    
    @Override
    public <A extends Display> void registerGlobalDisplayGenerator(LiveDisplayGenerator<A> generator) {
        globalDisplayGenerators.add(generator);
    }
    
    @Override
    public <A extends Display> void registerDisplayGenerator(CategoryIdentifier<A> categoryId, LiveDisplayGenerator<A> generator) {
        displayGenerators.computeIfAbsent(categoryId, location -> new ArrayList<>())
                .add(generator);
    }
    
    @Override
    public Map<CategoryIdentifier<?>, List<LiveDisplayGenerator<?>>> getCategoryDisplayGenerators() {
        return Collections.unmodifiableMap(displayGenerators);
    }
    
    @Override
    public List<LiveDisplayGenerator<?>> getGlobalDisplayGenerators() {
        return Collections.unmodifiableList(globalDisplayGenerators);
    }
    
    @Override
    public void registerVisibilityPredicate(DisplayVisibilityPredicate predicate) {
        visibilityPredicates.add(predicate);
        visibilityPredicates.sort(Comparator.reverseOrder());
    }
    
    @Override
    public boolean isDisplayVisible(Display display) {
        DisplayCategory<Display> category = (DisplayCategory<Display>) CategoryRegistry.getInstance().get(display.getCategoryIdentifier()).getCategory();
        for (DisplayVisibilityPredicate predicate : visibilityPredicates) {
            try {
                InteractionResult result = predicate.handleDisplay(category, display);
                if (result != InteractionResult.PASS) {
                    return result == InteractionResult.SUCCESS;
                }
            } catch (Throwable throwable) {
                RoughlyEnoughItemsCore.LOGGER.error("Failed to check if the recipe is visible!", throwable);
            }
        }
        
        return true;
    }
    
    @Override
    public List<DisplayVisibilityPredicate> getVisibilityPredicates() {
        return Collections.unmodifiableList(visibilityPredicates);
    }
    
    @Override
    public <T, D extends Display> void registerFiller(Class<T> typeClass, Predicate<? extends T> predicate, Function<T, D> mappingFunction) {
        fillers.add(new DisplayFiller<>(typeClass, (Predicate<T>) predicate, mappingFunction));
    }
    
    @Override
    public void startReload() {
        super.startReload();
        this.displays.clear();
        this.displayGenerators.clear();
        this.visibilityPredicates.clear();
        this.fillers.clear();
        this.displayCount.setValue(0);
    }
    
    @Override
    public void endReload() {
        if (!fillers.isEmpty()) {
            List<Recipe<?>> allSortedRecipes = getAllSortedRecipes();
            for (int i = allSortedRecipes.size() - 1; i >= 0; i--) {
                Recipe<?> recipe = allSortedRecipes.get(i);
                Display display = tryFillDisplay(recipe);
                if (display != null) {
                    registerDisplay(0, display);
                }
            }
        }
    }
    
    @Override
    @Nullable
    public <T> Display tryFillDisplay(T value) {
        if (value instanceof Display) return (Display) value;
        for (DisplayFiller<?, ?> filler : fillers) {
            Display display = tryFillDisplayGenerics(filler, value);
            if (display != null) return display;
        }
        return null;
    }
    
    private <T, D extends Display> D tryFillDisplayGenerics(DisplayFiller<T, D> filler, Object value) {
        try {
            if (filler.typeClass.isInstance(value) && filler.predicate.test((T) value)) {
                return filler.mappingFunction.apply((T) value);
            }
        } catch (Throwable e) {
            RoughlyEnoughItemsCore.LOGGER.error("Failed to fill displays!", e);
        }
        
        return null;
    }
    
    private static class DisplayFiller<T, D extends Display> {
        private final Class<T> typeClass;
        private final Predicate<T> predicate;
        
        private final Function<T, D> mappingFunction;
        
        public DisplayFiller(Class<T> typeClass, Predicate<T> predicate, Function<T, D> mappingFunction) {
            this.typeClass = typeClass;
            this.predicate = predicate;
            this.mappingFunction = mappingFunction;
        }
    }
}
