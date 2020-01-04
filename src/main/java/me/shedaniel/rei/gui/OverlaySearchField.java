/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.annotations.Internal;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import me.shedaniel.rei.impl.ScreenHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;

import java.util.List;

public class OverlaySearchField extends TextFieldWidget {
    
    public static boolean isSearching = false;
    public long keybindFocusTime = -1;
    public int keybindFocusKey = -1;
    protected long lastClickedTime = -1;
    private List<String> history = Lists.newArrayListWithCapacity(100);
    
    OverlaySearchField(int x, int y, int width, int height) {
        super(x, y, width, height);
        setMaxLength(10000);
    }
    
    @Override
    public void setFocused(boolean boolean_1) {
        if (isFocused() != boolean_1)
            addToHistory(getText());
        super.setFocused(boolean_1);
    }
    
    @Deprecated
    @Internal
    public void addToHistory(String text) {
        if (!text.isEmpty()) {
            history.removeIf(str -> str.equalsIgnoreCase(text));
            history.add(text);
            if (history.size() > 100)
                history.remove(0);
        }
    }
    
    @SuppressWarnings("deprecation")
    public void laterRender(int int_1, int int_2, float float_1) {
        RenderSystem.disableDepthTest();
        setEditableColor(ContainerScreenOverlay.getEntryListWidget().getAllStacks().isEmpty() && !getText().isEmpty() ? 16733525 : isSearching ? -852212 : (containsMouse(PointHelper.fromMouse()) || isFocused()) ? (ScreenHelper.isDarkModeEnabled() ? -17587 : -1) : -6250336);
        setSuggestion(!isFocused() && getText().isEmpty() ? I18n.translate("text.rei.search.field.suggestion") : null);
        super.render(int_1, int_2, float_1);
        RenderSystem.enableDepthTest();
    }
    
    @Override
    protected void renderSuggestion(int x, int y) {
        if (containsMouse(PointHelper.fromMouse()) || isFocused())
            this.font.drawWithShadow(this.font.trimToWidth(this.getSuggestion(), this.getWidth()), x, y, ScreenHelper.isDarkModeEnabled() ? 0xccddaa3d : 0xddeaeaea);
        else
            this.font.drawWithShadow(this.font.trimToWidth(this.getSuggestion(), this.getWidth()), x, y, -6250336);
    }
    
    @Override
    public void renderBorder() {
        if (!isSearching)
            super.renderBorder();
        else if (this.hasBorder()) {
            fill(this.getBounds().x - 1, this.getBounds().y - 1, this.getBounds().x + this.getBounds().width + 1, this.getBounds().y + this.getBounds().height + 1, -852212);
            fill(this.getBounds().x, this.getBounds().y, this.getBounds().x + this.getBounds().width, this.getBounds().y + this.getBounds().height, -16777216);
        }
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        boolean contains = containsMouse(double_1, double_2);
        if (isVisible() && contains && int_1 == 1)
            setText("");
        if (contains && int_1 == 0)
            if (lastClickedTime == -1)
                lastClickedTime = System.currentTimeMillis();
            else if (System.currentTimeMillis() - lastClickedTime > 700)
                lastClickedTime = -1;
            else {
                lastClickedTime = -1;
                isSearching = !isSearching;
                minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
        return super.mouseClicked(double_1, double_2, int_1);
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (this.isVisible() && this.isFocused())
            if (int_1 == 257 || int_1 == 335) {
                addToHistory(getText());
                setFocused(false);
                return true;
            } else if (int_1 == 265) {
                int i = history.indexOf(getText()) - 1;
                if (i < -1 && getText().isEmpty())
                    i = history.size() - 1;
                else if (i < -1) {
                    addToHistory(getText());
                    i = history.size() - 2;
                }
                if (i >= 0) {
                    setText(history.get(i));
                    return true;
                }
            } else if (int_1 == 264) {
                int i = history.indexOf(getText()) + 1;
                if (i > 0) {
                    setText(i < history.size() ? history.get(i) : "");
                    return true;
                }
            }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (System.currentTimeMillis() - keybindFocusTime < 1000 && InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), keybindFocusKey)) {
            keybindFocusTime = -1;
            keybindFocusKey = -1;
            return true;
        }
        return super.charTyped(char_1, int_1);
    }
    
    @Override
    public boolean containsMouse(double mouseX, double mouseY) {
        return ScreenHelper.getLastOverlay().isNotInExclusionZones(mouseX, mouseY) && super.containsMouse(mouseX, mouseY);
    }
    
    @Override
    public void render(int int_1, int int_2, float float_1) {
    }
    
}
