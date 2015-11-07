
package com.leo.appmaster.ui;

/**
 * A PageIndicator is responsible to show an visual indicator on the total views
 * number and the current visible view.
 */
public interface LeoAppViewPagerPageIndicator extends LeoAppViewPager.OnPageChangeListener {
    /**
     * Bind the indicator to a ViewPager.
     * 
     * @param view
     */
    void setViewPager(LeoAppViewPager view);

    /**
     * Bind the indicator to a ViewPager.
     * 
     * @param view
     * @param initialPosition
     */
    void setViewPager(LeoAppViewPager view, int initialPosition);

    /**
     * <p>
     * Set the current page of both the ViewPager and indicator.
     * </p>
     * <p>
     * This <strong>must</strong> be used if you need to set the page before the
     * views are drawn on screen (e.g., default start page).
     * </p>
     * 
     * @param item
     */
    void setCurrentItem(int item);

    /**
     * Set a page change listener which will receive forwarded events.
     * 
     * @param listener
     */
    void setOnPageChangeListener(LeoAppViewPager.OnPageChangeListener listener);

    /**
     * Notify the indicator that the fragment list has changed.
     */
    void notifyDataSetChanged();
}
