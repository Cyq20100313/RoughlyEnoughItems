/*
 * Roughly Enough Items by Danielshe.
 * Licensed under the MIT License.
 */

package me.shedaniel.rei.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.api.Point;
import me.shedaniel.math.api.Rectangle;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.*;
import me.shedaniel.rei.gui.widget.*;
import me.shedaniel.rei.impl.ScreenHelper;
import me.shedaniel.rei.utils.CollectionUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.Window;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

public class RecipeViewingScreen extends Screen {
    
    public static final Identifier CHEST_GUI_TEXTURE = new Identifier("roughlyenoughitems", "textures/gui/recipecontainer.png");
    private static final int TABS_PER_PAGE = 5;
    private final List<Widget> preWidgets;
    private final List<Widget> widgets;
    private final List<TabWidget> tabs;
    private final Map<RecipeCategory<?>, List<RecipeDisplay>> categoriesMap;
    private final List<RecipeCategory<?>> categories;
    public int guiWidth;
    public int guiHeight;
    public int page, categoryPages;
    public int largestWidth, largestHeight;
    public boolean choosePageActivated;
    public RecipeChoosePageWidget recipeChoosePageWidget;
    private Rectangle bounds;
    @Nullable private CategoryBaseWidget workingStationsBaseWidget;
    private RecipeCategory<RecipeDisplay> selectedCategory;
    private ButtonWidget recipeBack, recipeNext, categoryBack, categoryNext;
    
    public RecipeViewingScreen(Map<RecipeCategory<?>, List<RecipeDisplay>> categoriesMap) {
        super(new LiteralText(""));
        this.categoryPages = 0;
        this.preWidgets = Lists.newArrayList();
        this.widgets = Lists.newArrayList();
        Window window = MinecraftClient.getInstance().getWindow();
        this.bounds = new Rectangle(window.getScaledWidth() / 2 - guiWidth / 2, window.getScaledHeight() / 2 - guiHeight / 2, 176, 186);
        this.categoriesMap = categoriesMap;
        this.categories = Lists.newArrayList();
        for (RecipeCategory<?> category : RecipeHelper.getInstance().getAllCategories()) {
            if (categoriesMap.containsKey(category))
                categories.add(category);
        }
        this.selectedCategory = (RecipeCategory<RecipeDisplay>) categories.get(0);
        this.tabs = new ArrayList<>();
        this.choosePageActivated = false;
    }
    
    @Nullable
    public CategoryBaseWidget getWorkingStationsBaseWidget() {
        return workingStationsBaseWidget;
    }
    
    @Override
    public boolean keyPressed(int int_1, int int_2, int int_3) {
        if (int_1 == 256 && choosePageActivated) {
            choosePageActivated = false;
            init();
            return true;
        }
        if ((int_1 == 256 || this.minecraft.options.keyInventory.matchesKey(int_1, int_2)) && this.shouldCloseOnEsc()) {
            MinecraftClient.getInstance().openScreen(ScreenHelper.getLastContainerScreen());
            ScreenHelper.getLastOverlay().init();
            return true;
        }
        if (int_1 == 258) {
            boolean boolean_1 = !hasShiftDown();
            if (!this.changeFocus(boolean_1))
                this.changeFocus(boolean_1);
            return true;
        }
        if (choosePageActivated)
            return recipeChoosePageWidget.keyPressed(int_1, int_2, int_3);
        else if (ConfigObject.getInstance().getNextPageKeybind().matchesKey(int_1, int_2)) {
            if (recipeNext.enabled)
                recipeNext.onPressed();
            return recipeNext.enabled;
        } else if (ConfigObject.getInstance().getPreviousPageKeybind().matchesKey(int_1, int_2)) {
            if (recipeBack.enabled)
                recipeBack.onPressed();
            return recipeBack.enabled;
        }
        for (Element element : children())
            if (element.keyPressed(int_1, int_2, int_3))
                return true;
        if (int_1 == 259) {
            if (ScreenHelper.hasLastRecipeScreen())
                minecraft.openScreen(ScreenHelper.getLastRecipeScreen());
            else
                minecraft.openScreen(ScreenHelper.getLastContainerScreen());
            return true;
        }
        return super.keyPressed(int_1, int_2, int_3);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void init() {
        super.init();
        this.children.clear();
        this.tabs.clear();
        this.preWidgets.clear();
        this.widgets.clear();
        this.largestWidth = width - 100;
        this.largestHeight = height - 40;
        int maxWidthDisplay = CollectionUtils.mapAndMax(getCurrentDisplayed(), display -> selectedCategory.getDisplayWidth(display), (Comparator<Integer>) Comparator.naturalOrder()).orElse(150);
        this.guiWidth = MathHelper.clamp(maxWidthDisplay + 30, 0, largestWidth);
        this.guiHeight = MathHelper.floor(MathHelper.clamp((selectedCategory.getDisplayHeight() + 7d) * (getRecipesPerPage() + 1d) + 40d, 186d, largestHeight));
        this.bounds = new Rectangle(width / 2 - guiWidth / 2, height / 2 - guiHeight / 2, guiWidth, guiHeight);
        this.page = MathHelper.clamp(page, 0, getTotalPages(selectedCategory) - 1);
        
        ButtonWidget w, w2;
        this.widgets.add(w = new ButtonWidget(new Rectangle(bounds.x + 2, bounds.y - 16, 10, 10), I18n.translate("text.rei.left_arrow")) {
            @Override
            public void onPressed() {
                categoryPages--;
                if (categoryPages < 0)
                    categoryPages = MathHelper.ceil(categories.size() / (float) TABS_PER_PAGE) - 1;
                RecipeViewingScreen.this.init();
            }
        });
        this.widgets.add(w2 = new ButtonWidget(new Rectangle(bounds.x + bounds.width - 12, bounds.y - 16, 10, 10), I18n.translate("text.rei.right_arrow")) {
            @Override
            public void onPressed() {
                categoryPages++;
                if (categoryPages > MathHelper.ceil(categories.size() / (float) TABS_PER_PAGE) - 1)
                    categoryPages = 0;
                RecipeViewingScreen.this.init();
            }
        });
        w.enabled = w2.enabled = categories.size() > TABS_PER_PAGE;
        widgets.add(categoryBack = new ButtonWidget(new Rectangle(bounds.getX() + 5, bounds.getY() + 5, 12, 12), I18n.translate("text.rei.left_arrow")) {
            @Override
            public void onPressed() {
                int currentCategoryIndex = categories.indexOf(selectedCategory);
                currentCategoryIndex--;
                if (currentCategoryIndex < 0)
                    currentCategoryIndex = categories.size() - 1;
                selectedCategory = (RecipeCategory<RecipeDisplay>) categories.get(currentCategoryIndex);
                categoryPages = MathHelper.floor(currentCategoryIndex / (double) TABS_PER_PAGE);
                page = 0;
                RecipeViewingScreen.this.init();
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.previous_category"));
            }
        });
        widgets.add(new ClickableLabelWidget(new Point(bounds.getCenterX(), bounds.getY() + 7), "") {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                setText(selectedCategory.getCategoryName());
                super.render(mouseX, mouseY, delta);
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.view_all_categories"));
            }
            
            @Override
            public void onLabelClicked() {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                ClientHelper.getInstance().executeViewAllRecipesKeyBind();
            }
        });
        widgets.add(categoryNext = new ButtonWidget(new Rectangle(bounds.getMaxX() - 17, bounds.getY() + 5, 12, 12), I18n.translate("text.rei.right_arrow")) {
            @Override
            public void onPressed() {
                int currentCategoryIndex = categories.indexOf(selectedCategory);
                currentCategoryIndex++;
                if (currentCategoryIndex >= categories.size())
                    currentCategoryIndex = 0;
                selectedCategory = (RecipeCategory<RecipeDisplay>) categories.get(currentCategoryIndex);
                categoryPages = MathHelper.floor(currentCategoryIndex / (double) TABS_PER_PAGE);
                page = 0;
                RecipeViewingScreen.this.init();
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.next_category"));
            }
        });
        categoryBack.enabled = categories.size() > 1;
        categoryNext.enabled = categories.size() > 1;
        
        widgets.add(recipeBack = new ButtonWidget(new Rectangle(bounds.getX() + 5, bounds.getY() + 21, 12, 12), I18n.translate("text.rei.left_arrow")) {
            @Override
            public void onPressed() {
                page--;
                if (page < 0)
                    page = getTotalPages(selectedCategory) - 1;
                RecipeViewingScreen.this.init();
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.previous_page"));
            }
        });
        widgets.add(new ClickableLabelWidget(new Point(bounds.getCenterX(), bounds.getY() + 23), "") {
            @Override
            public void render(int mouseX, int mouseY, float delta) {
                setText(String.format("%d/%d", page + 1, getTotalPages(selectedCategory)));
                super.render(mouseX, mouseY, delta);
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.choose_page"));
            }
            
            @Override
            public void onLabelClicked() {
                MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                RecipeViewingScreen.this.choosePageActivated = true;
                RecipeViewingScreen.this.init();
            }
        }.clickable(categoriesMap.get(selectedCategory).size() > getRecipesPerPageByHeight()));
        widgets.add(recipeNext = new ButtonWidget(new Rectangle(bounds.getMaxX() - 17, bounds.getY() + 21, 12, 12), I18n.translate("text.rei.right_arrow")) {
            @Override
            public void onPressed() {
                page++;
                if (page >= getTotalPages(selectedCategory))
                    page = 0;
                RecipeViewingScreen.this.init();
            }
            
            @Override
            public Optional<String> getTooltips() {
                return Optional.ofNullable(I18n.translate("text.rei.next_page"));
            }
        });
        recipeBack.enabled = recipeNext.enabled = categoriesMap.get(selectedCategory).size() > getRecipesPerPageByHeight();
        for (int i = 0; i < TABS_PER_PAGE; i++) {
            int j = i + categoryPages * TABS_PER_PAGE;
            if (categories.size() > j) {
                TabWidget tab;
                tabs.add(tab = new TabWidget(i, new Rectangle(bounds.x + bounds.width / 2 - Math.min(categories.size() - categoryPages * TABS_PER_PAGE, TABS_PER_PAGE) * 14 + i * 28, bounds.y - 28, 28, 28)) {
                    @Override
                    public boolean mouseClicked(double mouseX, double mouseY, int button) {
                        if (getBounds().contains(mouseX, mouseY)) {
                            MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                            if (getId() + categoryPages * TABS_PER_PAGE == categories.indexOf(selectedCategory))
                                return false;
                            selectedCategory = (RecipeCategory<RecipeDisplay>) categories.get(getId() + categoryPages * TABS_PER_PAGE);
                            page = 0;
                            RecipeViewingScreen.this.init();
                            return true;
                        }
                        return false;
                    }
                });
                tab.setRenderer(categories.get(j), categories.get(j).getLogo(), categories.get(j).getCategoryName(), tab.getId() + categoryPages * TABS_PER_PAGE == categories.indexOf(selectedCategory));
            }
        }
        Optional<ButtonAreaSupplier> supplier = RecipeHelper.getInstance().getAutoCraftButtonArea(selectedCategory);
        int recipeHeight = selectedCategory.getDisplayHeight();
        List<RecipeDisplay> currentDisplayed = getCurrentDisplayed();
        for (int i = 0; i < currentDisplayed.size(); i++) {
            int finalI = i;
            final Supplier<RecipeDisplay> displaySupplier = () -> currentDisplayed.get(finalI);
            int displayWidth = selectedCategory.getDisplayWidth(displaySupplier.get());
            final Rectangle displayBounds = new Rectangle(getBounds().getCenterX() - displayWidth / 2, getBounds().y + 40 + recipeHeight * i + 7 * i, displayWidth, recipeHeight);
            List<Widget> setupDisplay = selectedCategory.setupDisplay(displaySupplier, displayBounds);
            this.widgets.addAll(setupDisplay);
            if (supplier.isPresent() && supplier.get().get(displayBounds) != null)
                this.widgets.add(new AutoCraftingButtonWidget(displayBounds, supplier.get().get(displayBounds), supplier.get().getButtonText(), displaySupplier, setupDisplay, selectedCategory));
        }
        if (choosePageActivated)
            recipeChoosePageWidget = new RecipeChoosePageWidget(this, page, getTotalPages(selectedCategory));
        else
            recipeChoosePageWidget = null;
        
        workingStationsBaseWidget = null;
        List<List<EntryStack>> workingStations = RecipeHelper.getInstance().getWorkingStations(selectedCategory.getIdentifier());
        if (!workingStations.isEmpty()) {
            int hh = MathHelper.floor((bounds.height - 16) / 18f);
            int actualHeight = Math.min(hh, workingStations.size());
            int innerWidth = MathHelper.ceil(workingStations.size() / ((float) hh));
            int xx = bounds.x - (10 + innerWidth * 18) + 6;
            int yy = bounds.y + 16;
            preWidgets.add(workingStationsBaseWidget = new CategoryBaseWidget(new Rectangle(xx - 6, yy - 6, 15 + innerWidth * 18, 11 + actualHeight * 18)));
            int index = 0;
            List<String> list = Collections.singletonList(Formatting.YELLOW.toString() + I18n.translate("text.rei.working_station"));
            xx += (innerWidth - 1) * 18;
            for (List<EntryStack> workingStation : workingStations) {
                preWidgets.add(EntryWidget.create(xx, yy).entries(CollectionUtils.map(workingStation, stack -> stack.copy().setting(EntryStack.Settings.TOOLTIP_APPEND_EXTRA, s -> list))));
                index++;
                yy += 18;
                if (index >= hh) {
                    index = 0;
                    yy = bounds.y + 16;
                    xx -= 18;
                }
            }
        }
        
        children.addAll(tabs);
        children.add(ScreenHelper.getLastOverlay(true, false));
        children.addAll(widgets);
        children.addAll(preWidgets);
    }
    
    public List<Widget> getWidgets() {
        return widgets;
    }
    
    public List<RecipeDisplay> getCurrentDisplayed() {
        List<RecipeDisplay> list = Lists.newArrayList();
        int recipesPerPage = getRecipesPerPage();
        for (int i = 0; i <= recipesPerPage; i++)
            if (page * (recipesPerPage + 1) + i < categoriesMap.get(selectedCategory).size())
                list.add(categoriesMap.get(selectedCategory).get(page * (recipesPerPage + 1) + i));
        return list;
    }
    
    public RecipeCategory<RecipeDisplay> getSelectedCategory() {
        return selectedCategory;
    }
    
    public int getPage() {
        return page;
    }
    
    public int getCategoryPage() {
        return categoryPages;
    }
    
    @SuppressWarnings("deprecation")
    private int getRecipesPerPage() {
        if (selectedCategory.getFixedRecipesPerPage() > 0)
            return selectedCategory.getFixedRecipesPerPage() - 1;
        int height = selectedCategory.getDisplayHeight();
        return MathHelper.clamp(MathHelper.floor(((double) largestHeight - 40d) / ((double) height + 7d)) - 1, 0, Math.min(ConfigObject.getInstance().getMaxRecipePerPage() - 1, selectedCategory.getMaximumRecipePerPage() - 1));
    }
    
    private int getRecipesPerPageByHeight() {
        int height = selectedCategory.getDisplayHeight();
        return MathHelper.clamp(MathHelper.floor(((double) guiHeight - 40d) / ((double) height + 7d)), 0, Math.min(ConfigObject.getInstance().getMaxRecipePerPage() - 1, selectedCategory.getMaximumRecipePerPage() - 1));
    }
    
    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        for (Widget widget : preWidgets) {
            widget.render(mouseX, mouseY, delta);
        }
        if (selectedCategory != null)
            selectedCategory.drawCategoryBackground(bounds, mouseX, mouseY, delta);
        else {
            new CategoryBaseWidget(bounds).render();
            if (ScreenHelper.isDarkModeEnabled()) {
                fill(bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, 0xFF404040);
                fill(bounds.x + 17, bounds.y + 21, bounds.x + bounds.width - 17, bounds.y + 33, 0xFF404040);
            } else {
                fill(bounds.x + 17, bounds.y + 5, bounds.x + bounds.width - 17, bounds.y + 17, 0xFF9E9E9E);
                fill(bounds.x + 17, bounds.y + 21, bounds.x + bounds.width - 17, bounds.y + 33, 0xFF9E9E9E);
            }
        }
        for (TabWidget tab : tabs) {
            if (!tab.isSelected())
                tab.render(mouseX, mouseY, delta);
        }
        super.render(mouseX, mouseY, delta);
        for (Widget widget : widgets) {
            widget.render(mouseX, mouseY, delta);
        }
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        for (TabWidget tab : tabs) {
            if (tab.isSelected())
                tab.render(mouseX, mouseY, delta);
        }
        ScreenHelper.getLastOverlay().render(mouseX, mouseY, delta);
        ScreenHelper.getLastOverlay().lateRender(mouseX, mouseY, delta);
        if (choosePageActivated) {
            setBlitOffset(500);
            this.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
            setBlitOffset(0);
            recipeChoosePageWidget.render(mouseX, mouseY, delta);
        }
    }
    
    public int getTotalPages(RecipeCategory<RecipeDisplay> category) {
        return MathHelper.ceil(categoriesMap.get(category).size() / (double) (getRecipesPerPage() + 1));
    }
    
    public Rectangle getBounds() {
        return bounds;
    }
    
    @Override
    public boolean charTyped(char char_1, int int_1) {
        if (choosePageActivated) {
            return recipeChoosePageWidget.charTyped(char_1, int_1);
        }
        for (Element listener : children())
            if (listener.charTyped(char_1, int_1))
                return true;
        return super.charTyped(char_1, int_1);
    }
    
    @Override
    public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
        if (choosePageActivated) {
            return recipeChoosePageWidget.mouseDragged(double_1, double_2, int_1, double_3, double_4);
        }
        return super.mouseDragged(double_1, double_2, int_1, double_3, double_4);
    }
    
    @Override
    public boolean mouseReleased(double double_1, double double_2, int int_1) {
        if (choosePageActivated) {
            return recipeChoosePageWidget.mouseReleased(double_1, double_2, int_1);
        }
        return super.mouseReleased(double_1, double_2, int_1);
    }
    
    @Override
    public boolean mouseScrolled(double i, double j, double amount) {
        for (Element listener : children())
            if (listener.mouseScrolled(i, j, amount))
                return true;
        if (getBounds().contains(PointHelper.fromMouse())) {
            if (amount > 0 && recipeBack.enabled)
                recipeBack.onPressed();
            else if (amount < 0 && recipeNext.enabled)
                recipeNext.onPressed();
        }
        if ((new Rectangle(bounds.x, bounds.y - 28, bounds.width, 28)).contains(PointHelper.fromMouse())) {
            if (amount > 0 && categoryBack.enabled)
                categoryBack.onPressed();
            else if (amount < 0 && categoryNext.enabled)
                categoryNext.onPressed();
        }
        return super.mouseScrolled(i, j, amount);
    }
    
    @Override
    public boolean mouseClicked(double double_1, double double_2, int int_1) {
        if (choosePageActivated)
            if (recipeChoosePageWidget.containsMouse(double_1, double_2)) {
                return recipeChoosePageWidget.mouseClicked(double_1, double_2, int_1);
            } else {
                choosePageActivated = false;
                init();
                return false;
            }
        for (Element entry : children())
            if (entry.mouseClicked(double_1, double_2, int_1)) {
                setFocused(entry);
                if (int_1 == 0)
                    setDragging(true);
                return true;
            }
        return false;
    }
    
    @Override
    public Element getFocused() {
        if (choosePageActivated)
            return recipeChoosePageWidget;
        return super.getFocused();
    }
    
}
