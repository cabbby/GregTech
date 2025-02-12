package gregtech.common.covers.filter;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.ServerWidgetGroup;
import gregtech.api.gui.widgets.ToggleButtonWidget;
import gregtech.api.util.IDirtyNotifiable;
import gregtech.api.util.ItemStackKey;
import net.minecraft.item.ItemStack;

import java.util.Set;
import java.util.function.Consumer;

public class ItemFilterWrapper {

    private static final Object MATCH_RESULT_TRUE = new Object();
    private final IDirtyNotifiable dirtyNotifiable;
    private boolean isBlacklistFilter = false;
    private int maxStackSize = 1;
    private ItemFilter currentItemFilter;

    public ItemFilterWrapper(IDirtyNotifiable dirtyNotifiable) {
        this.dirtyNotifiable = dirtyNotifiable;
    }

    public void initUI(int y, Consumer<Widget> widgetGroup) {
        ServerWidgetGroup blacklistButton = new ServerWidgetGroup(() -> getItemFilter() != null);
        blacklistButton.addWidget(new ToggleButtonWidget(146, y, 20, 20, GuiTextures.BUTTON_BLACKLIST,
            this::isBlacklistFilter, this::setBlacklistFilter).setTooltipText("cover.filter.blacklist"));
        widgetGroup.accept(blacklistButton);
        widgetGroup.accept(new WidgetGroupItemFilter(y, this::getItemFilter));
    }

    public void setItemFilter(ItemFilter itemFilter) {
        this.currentItemFilter = itemFilter;
        if(currentItemFilter != null) {
            currentItemFilter.setDirtyNotifiable(dirtyNotifiable);
        }
    }

    public ItemFilter getItemFilter() {
        return currentItemFilter;
    }

    public void onFilterInstanceChange() {
        if (currentItemFilter != null) {
            currentItemFilter.setMaxStackSize(maxStackSize);
        }
        dirtyNotifiable.markAsDirty();
    }

    public void setMaxStackSize(int maxStackSize) {
        this.maxStackSize = maxStackSize;
        onFilterInstanceChange();
        dirtyNotifiable.markAsDirty();
    }

    public void setBlacklistFilter(boolean blacklistFilter) {
        isBlacklistFilter = blacklistFilter;
        dirtyNotifiable.markAsDirty();
    }

    public boolean isBlacklistFilter() {
        return isBlacklistFilter;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public boolean showGlobalTransferLimitSlider() {
        return isBlacklistFilter() || currentItemFilter == null || currentItemFilter.showGlobalTransferLimitSlider();
    }

    public int getSlotTransferLimit(Object matchSlot, Set<ItemStackKey> matchedStacks, int globalTransferLimit) {
        if (isBlacklistFilter() || currentItemFilter == null) {
            return globalTransferLimit;
        }
        return currentItemFilter.getSlotTransferLimit(matchSlot, matchedStacks, globalTransferLimit);
    }

    public Object matchItemStack(ItemStack itemStack) {
        Object originalResult;
        if (currentItemFilter == null) {
            originalResult = MATCH_RESULT_TRUE;
        } else {
            originalResult = currentItemFilter.matchItemStack(itemStack);
        }
        if (isBlacklistFilter()) {
            originalResult = originalResult == null ? MATCH_RESULT_TRUE : null;
        }
        return originalResult;
    }

    public boolean testItemStack(ItemStack itemStack) {
        return matchItemStack(itemStack) != null;
    }
}
