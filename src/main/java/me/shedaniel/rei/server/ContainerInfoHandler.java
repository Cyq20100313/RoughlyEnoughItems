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

package me.shedaniel.rei.server;

import com.google.common.collect.Maps;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;

public class ContainerInfoHandler {
    private static final Map<String, Map<Class<? extends ScreenHandler>, ContainerInfo<? extends ScreenHandler>>> containerInfoMap = Maps.newLinkedHashMap();
    
    @Deprecated
    @ApiStatus.ScheduledForRemoval
    public static void registerContainerInfo(Identifier category, ContainerInfo<? extends ScreenHandler> containerInfo) {
        registerScreenWithHandlerInfo(category, containerInfo);
    }
    
    public static void registerScreenWithHandlerInfo(Identifier category, ContainerInfo<? extends ScreenHandler> containerInfo) {
        if (!containerInfoMap.containsKey(category.toString()))
            containerInfoMap.put(category.toString(), Maps.newLinkedHashMap());
        containerInfoMap.get(category.toString()).put(containerInfo.getContainerClass(), containerInfo);
    }
    
    public static boolean isCategoryHandled(Identifier category) {
        return containerInfoMap.containsKey(category.toString()) && !containerInfoMap.get(category.toString()).isEmpty();
    }
    
    public static ContainerInfo<? extends ScreenHandler> getContainerInfo(Identifier category, Class<?> containerClass) {
        if (!isCategoryHandled(category))
            return null;
        Map<Class<? extends ScreenHandler>, ContainerInfo<? extends ScreenHandler>> infoMap = containerInfoMap.get(category.toString());
        if (infoMap.containsKey(containerClass))
            return infoMap.get(containerClass);
        for (Map.Entry<Class<? extends ScreenHandler>, ContainerInfo<? extends ScreenHandler>> entry : infoMap.entrySet())
            if (entry.getKey().isAssignableFrom(containerClass))
                return entry.getValue();
        return null;
    }
}
