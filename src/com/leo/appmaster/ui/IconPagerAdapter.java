
package com.leo.appmaster.ui;

public interface IconPagerAdapter {
    /**
     * Get icon representing the page at {@code index} in the adapter.
     */
    int getIconResId(int index);

    boolean getRedTip(int index);

    // From PagerAdapter
    int getCount();
}
