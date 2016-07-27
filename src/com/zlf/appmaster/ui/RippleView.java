package com.zlf.appmaster.ui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

import com.zlf.appmaster.R;
import com.zlf.appmaster.utils.LeoLog;
import com.zlf.tools.animator.Animator;
import com.zlf.tools.animator.AnimatorListenerAdapter;
import com.zlf.tools.animator.AnimatorSet;
import com.zlf.tools.animator.ObjectAnimator;
import com.zlf.tools.animator.Property;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class RippleView extends RelativeLayout {

    private static final int DEFAULT_DURATION = 350;
    private static final int DEFAULT_FADE_DURATION = 75;
    private static final float DEFAULT_DIAMETER_DP = 35;
    private static final float DEFAULT_ALPHA = 0.5f;
    private static final int DEFAULT_COLOR = Color.BLACK;
    private static final int DEFAULT_BACKGROUND = Color.TRANSPARENT;
    private static final boolean DEFAULT_HOVER = true;
    private static final boolean DEFAULT_DELAY_CLICK = true;
    private static final boolean DEFAULT_PERSISTENT = false;
    private static final boolean DEFAULT_SEARCH_ADAPTER = false;
    private static final boolean DEFAULT_RIPPLE_OVERLAY = false;

    private static final int FADE_EXTRA_DELAY = 50;
    private static final long HOVER_DURATION = 2500;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect bounds = new Rect();

    private int rippleColor;
    private boolean rippleOverlay;
    private boolean rippleHover;
    private int rippleDiameter;
    private int rippleDuration;
    private int rippleAlpha;
    private boolean rippleDelayClick;
    private int rippleFadeDuration;
    private boolean ripplePersistent;
    private Drawable rippleBackground;
    private boolean rippleInAdapter;

    private float radius;

    private AdapterView parentAdapter;
    private View childView;
    private AnimatorSet rippleAnimator;
    private ObjectAnimator hoverAnimator;

    private Point currentCoords = new Point();
    private Point previousCoords = new Point();

    private boolean eventCancelled;
    private boolean prepressed;
    private int positionInAdapter;

    private GestureDetector gestureDetector;
    private PerformClickEvent pendingClickEvent;
    private PressedEvent pendingPressEvent;


    public static RippleBuilder on(View view) {
        return new RippleBuilder(view);
    }

    public RippleView(Context context) {
        this(context, null, 0);
        childView = this;
    }

    public RippleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        childView = this;
    }

    CheckBox mCheckBox;

    public void setCheckBox(CheckBox checkBox) {
        mCheckBox = checkBox;
    }

    public CheckBox getCheckBox() {
        return mCheckBox;
    }

    public RippleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        childView = this;

        setWillNotDraw(false);
        gestureDetector = new GestureDetector(context, longClickListener);

//        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RippleView);
//        rippleColor = typedArray.getColor(R.styleable.RippleView_rv_color, DEFAULT_COLOR);
//        rippleDuration = typedArray.getInteger(R.styleable.RippleView_rv_rippleDuration, DEFAULT_DURATION);
//        rippleAlpha = (int) (255 * typedArray.getFloat(R.styleable.RippleView_rv_alpha, DEFAULT_ALPHA));
//        rippleBackground = new ColorDrawable(DEFAULT_BACKGROUND);
//        ripplePersistent = DEFAULT_PERSISTENT;
//        rippleInAdapter = DEFAULT_SEARCH_ADAPTER;
//        rippleDelayClick = DEFAULT_DELAY_CLICK;
//        rippleFadeDuration = DEFAULT_FADE_DURATION;
//        rippleOverlay = DEFAULT_RIPPLE_OVERLAY;
//        rippleHover = DEFAULT_HOVER;
//        rippleDiameter = (int) dpToPx(getResources(), DEFAULT_DIAMETER_DP);
//        typedArray.recycle();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MaterialRippleLayout);
        rippleColor = a.getColor(R.styleable.MaterialRippleLayout_rippleColor, DEFAULT_COLOR);
        rippleDiameter = a.getDimensionPixelSize(
                R.styleable.MaterialRippleLayout_rippleDimension,
                (int) dpToPx(getResources(), DEFAULT_DIAMETER_DP)
        );

        rippleOverlay = a.getBoolean(R.styleable.MaterialRippleLayout_rippleOverlay, DEFAULT_RIPPLE_OVERLAY);
        rippleHover = a.getBoolean(R.styleable.MaterialRippleLayout_rippleHover, DEFAULT_HOVER);
        rippleDuration = a.getInt(R.styleable.MaterialRippleLayout_rippleDuration, DEFAULT_DURATION);
        rippleAlpha = (int) (255 * a.getFloat(R.styleable.MaterialRippleLayout_rippleAlpha, DEFAULT_ALPHA));
        rippleDelayClick = a.getBoolean(R.styleable.MaterialRippleLayout_rippleDelayClick, DEFAULT_DELAY_CLICK);
        rippleFadeDuration = a.getInteger(R.styleable.MaterialRippleLayout_rippleFadeDuration, DEFAULT_FADE_DURATION);
        rippleBackground = new ColorDrawable(a.getColor(R.styleable.MaterialRippleLayout_rippleBackground, DEFAULT_BACKGROUND));
        ripplePersistent = a.getBoolean(R.styleable.MaterialRippleLayout_ripplePersistent, DEFAULT_PERSISTENT);
        rippleInAdapter = a.getBoolean(R.styleable.MaterialRippleLayout_rippleInAdapter, DEFAULT_SEARCH_ADAPTER);
        a.recycle();

        paint.setColor(rippleColor);
        paint.setAlpha(rippleAlpha);
    }


//    @SuppressWarnings("unchecked")
//    public <T extends View> T getChildView() {
//        return (T) childView;
//    }

    @Override
    public final void addView(View child, int index, ViewGroup.LayoutParams params) {
//        if (getChildCount() > 0) {
//            throw new IllegalStateException("MaterialRippleLayout can host only one child");
//        }
//        //noinspection unchecked
        childView = child;
        super.addView(child, index, params);
    }

    OnClickListener listener;

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
//        if (childView == null) {
//            throw new IllegalStateException("MaterialRippleLayout must have a child view to handle clicks");
//        }
//        childView.setOnClickListener(onClickListener);
//        super.setOnClickListener(onClickListener);
        listener = onClickListener;
    }


    private boolean isClickAlready = false;
    private boolean isCanClick = false;
    private long clickTime = 0;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean superOnTouchEvent = super.onTouchEvent(event);

        if (!isEnabled() || !childView.isEnabled()) return superOnTouchEvent;

        boolean isEventInBounds = bounds.contains((int) event.getX(), (int) event.getY());

        if (isEventInBounds) {
            previousCoords.set(currentCoords.x, currentCoords.y);
            currentCoords.set((int) event.getX(), (int) event.getY());
        }

        boolean gestureResult = gestureDetector.onTouchEvent(event);
        if (gestureResult) {
            return true;
        } else {
            int action = event.getActionMasked();
            switch (action) {
                case MotionEvent.ACTION_UP:

                    if (isCanClick) {
                        if (longClickDone && needLongClick) {

                            startRipple(null);
                            cancelPressedEvent();

                            isClickAlready = false;
                            isCanClick = false;
                            longClickDone = false;

                        } else {
                            pendingClickEvent = new PerformClickEvent();

                            if (prepressed) {
                                childView.setPressed(true);
                                postDelayed(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                childView.setPressed(false);
                                            }
                                        }, ViewConfiguration.getPressedStateDuration());
                            }

                            if (isEventInBounds) {
                                startRipple(pendingClickEvent);
                            } else if (!rippleHover) {
                                setRadius(0);
                            }
                            if (!rippleDelayClick && isEventInBounds) {
                                LeoLog.d("testRippleClick", "is click right now");
                                pendingClickEvent.run();
                            }
                            cancelPressedEvent();
                            isCanClick = false;
                        }
                    }

                    clickTime = System.currentTimeMillis();

                    break;
                case MotionEvent.ACTION_DOWN:

                    long otherClick = System.currentTimeMillis();
                    boolean timeOut = otherClick - clickTime > 300 ? true : false;

                    if (!isClickAlready && timeOut) {
                        isClickAlready = true;
                        isCanClick = true;

                        setPositionInAdapter();
                        eventCancelled = false;
                        if (isInScrollingContainer()) {
                            cancelPressedEvent();
                            prepressed = true;
                            pendingPressEvent = new PressedEvent(event);
                            postDelayed(pendingPressEvent, ViewConfiguration.getTapTimeout());
                        } else {
                            childView.onTouchEvent(event);
                            childView.setPressed(true);
                            if (rippleHover) {
                                startHover();
                            }
                        }

                    } else {
                        isCanClick = false;
                    }


                    break;
                case MotionEvent.ACTION_CANCEL:
                    LeoLog.d("testRippleClick", "ACTION_CANCEL");
                    if (rippleInAdapter) {
                        // dont use current coords in adapter since they tend to jump drastically on scroll
                        currentCoords.set(previousCoords.x, previousCoords.y);
                        previousCoords = new Point();
                    }
                    childView.onTouchEvent(event);
                    if (rippleHover) {
                        if (!prepressed) {
                            startRipple(null);
                        }
                    } else {
                        childView.setPressed(false);
                    }
                    cancelPressedEvent();
                    isClickAlready = false;
                    isCanClick = false;
                    longClickDone = false;

                    clickTime = System.currentTimeMillis();

                    break;
                case MotionEvent.ACTION_MOVE:
                    if (rippleHover) {
                        if (isEventInBounds && !eventCancelled) {
                            invalidate();
                        } else if (!isEventInBounds) {
                            startRipple(null);
                        }
                    }

                    if (!isEventInBounds) {
                        cancelPressedEvent();
                        if (hoverAnimator != null) {
                            hoverAnimator.cancel();
                        }
                        childView.onTouchEvent(event);
                        eventCancelled = true;

                        isClickAlready = false;
                        isCanClick = false;
                        longClickDone = false;
                    }
                    break;
            }
            return true;
        }
    }

    private void cancelPressedEvent() {
        if (pendingPressEvent != null) {
            removeCallbacks(pendingPressEvent);
            prepressed = false;
        }
    }

    private boolean longClickDone = false;
    private SimpleOnGestureListener longClickListener = new SimpleOnGestureListener() {
        public void onLongPress(MotionEvent e) {
            childView.performLongClick();
            longClickDone = true;
        }
    };

    private void startHover() {
        if (eventCancelled) return;

        if (hoverAnimator != null) {
            hoverAnimator.cancel();
        }
        final float radius = (float) (Math.sqrt(Math.pow(getWidth(), 2) + Math.pow(getHeight(), 2)) * 1.2f);
        hoverAnimator = ObjectAnimator.ofFloat(this, radiusProperty, rippleDiameter, radius)
                .setDuration(HOVER_DURATION);
        hoverAnimator.setInterpolator(new LinearInterpolator());
        hoverAnimator.start();
    }

    private void startRipple(final Runnable animationEndRunnable) {
        if (eventCancelled) return;

        float endRadius = getEndRadius();

        cancelAnimations();

        rippleAnimator = new AnimatorSet();
        rippleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!ripplePersistent) {
                    setRadius(0);
                    setRippleAlpha(rippleAlpha);
                }
                if (animationEndRunnable != null && rippleDelayClick) {
                    LeoLog.d("testclick", "startRipple click");
                    animationEndRunnable.run();
                }
                childView.setPressed(false);
            }
        });

        ObjectAnimator ripple = ObjectAnimator.ofFloat(this, radiusProperty, radius, endRadius);
        ripple.setDuration(rippleDuration);
        ripple.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator fade = ObjectAnimator.ofInt(this, circleAlphaProperty, rippleAlpha, 0);
        fade.setDuration(rippleFadeDuration);
        fade.setInterpolator(new AccelerateInterpolator());
        fade.setStartDelay(rippleDuration - rippleFadeDuration - FADE_EXTRA_DELAY);

        if (ripplePersistent) {
            rippleAnimator.play(ripple);
        } else if (getRadius() > endRadius) {
            fade.setStartDelay(0);
            rippleAnimator.play(fade);
        } else {
            rippleAnimator.playTogether(ripple, fade);
        }
        rippleAnimator.start();
    }

    private void cancelAnimations() {
        if (rippleAnimator != null) {
            rippleAnimator.cancel();
            rippleAnimator.removeAllListeners();
        }

        if (hoverAnimator != null) {
            hoverAnimator.cancel();
        }
    }

    private float getEndRadius() {
        final int width = getWidth();
        final int height = getHeight();

        final int halfWidth = width / 2;
        final int halfHeight = height / 2;

        final float radiusX = halfWidth > currentCoords.x ? width - currentCoords.x : currentCoords.x;
        final float radiusY = halfHeight > currentCoords.y ? height - currentCoords.y : currentCoords.y;

        return (float) Math.sqrt(Math.pow(radiusX, 2) + Math.pow(radiusY, 2)) * 1.2f;
    }

    public boolean isInScrollingContainer() {
        ViewParent p = getParent();
        while (p != null && p instanceof ViewGroup) {
            if (((ViewGroup) p).shouldDelayChildPressedState()) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    private AdapterView findParentAdapterView() {
        if (parentAdapter != null) {
            return parentAdapter;
        }
        ViewParent current = getParent();
        while (true) {
            if (current instanceof AdapterView) {
                parentAdapter = (AdapterView) current;
                return parentAdapter;
            } else {
                try {
                    current = current.getParent();
                } catch (NullPointerException npe) {
                    throw new RuntimeException("Could not find a parent AdapterView");
                }
            }
        }
    }

    private void setPositionInAdapter() {
        if (rippleInAdapter) {
            positionInAdapter = findParentAdapterView().getPositionForView(RippleView.this);
        }
    }

    private boolean adapterPositionChanged() {
        if (rippleInAdapter) {
            int newPosition = findParentAdapterView().getPositionForView(RippleView.this);
            final boolean changed = newPosition != positionInAdapter;
            positionInAdapter = newPosition;
            if (changed) {
                cancelPressedEvent();
                cancelAnimations();
                childView.setPressed(false);
                setRadius(0);
            }
            return changed;
        }
        return false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bounds.set(0, 0, w, h);
        rippleBackground.setBounds(bounds);
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    /*
     * Drawing
     */
    @Override
    public void draw(Canvas canvas) {
        final boolean positionChanged = adapterPositionChanged();
        if (rippleOverlay) {
            if (!positionChanged) {
                rippleBackground.draw(canvas);
            }
            super.draw(canvas);
            if (!positionChanged) {
                canvas.drawCircle(currentCoords.x, currentCoords.y, radius, paint);
            }
        } else {
            if (!positionChanged) {
                rippleBackground.draw(canvas);
                canvas.drawCircle(currentCoords.x, currentCoords.y, radius, paint);
            }
            super.draw(canvas);
        }
    }

    /*
     * Animations
     */
    private Property<RippleView, Float> radiusProperty
            = new Property<RippleView, Float>(Float.class, "radius") {
        @Override
        public Float get(RippleView object) {
            return object.getRadius();
        }

        @Override
        public void set(RippleView object, Float value) {
            object.setRadius(value);
        }
    };

    private float getRadius() {
        return radius;
    }


    public void setRadius(float radius) {
        this.radius = radius;
        invalidate();
    }

    private Property<RippleView, Integer> circleAlphaProperty
            = new Property<RippleView, Integer>(Integer.class, "rippleAlpha") {
        @Override
        public Integer get(RippleView object) {
            return object.getRippleAlpha();
        }

        @Override
        public void set(RippleView object, Integer value) {
            object.setRippleAlpha(value);
        }
    };

    public int getRippleAlpha() {
        return paint.getAlpha();
    }

    public void setRippleAlpha(Integer rippleAlpha) {
        paint.setAlpha(rippleAlpha);
        invalidate();
    }

    /*
    * Accessor
     */
    public void setRippleColor(int rippleColor) {
        this.rippleColor = rippleColor;
        paint.setColor(rippleColor);
        paint.setAlpha(rippleAlpha);
        invalidate();
    }

    public void setRippleOverlay(boolean rippleOverlay) {
        this.rippleOverlay = rippleOverlay;
    }

    public void setRippleDiameter(int rippleDiameter) {
        this.rippleDiameter = rippleDiameter;
    }

    public void setRippleDuration(int rippleDuration) {
        this.rippleDuration = rippleDuration;
    }

    public void setRippleBackground(int color) {
        rippleBackground = new ColorDrawable(color);
        rippleBackground.setBounds(bounds);
        invalidate();
    }

    public void setRippleHover(boolean rippleHover) {
        this.rippleHover = rippleHover;
    }

    public void setRippleDelayClick(boolean rippleDelayClick) {
        this.rippleDelayClick = rippleDelayClick;
    }

    public void setRippleFadeDuration(int rippleFadeDuration) {
        this.rippleFadeDuration = rippleFadeDuration;
    }

    public void setRipplePersistent(boolean ripplePersistent) {
        this.ripplePersistent = ripplePersistent;
    }

    public void setRippleInAdapter(boolean rippleInAdapter) {
        this.rippleInAdapter = rippleInAdapter;
    }

    public void setDefaultRippleAlpha(int alpha) {
        this.rippleAlpha = alpha;
        paint.setAlpha(alpha);
        invalidate();
    }

    private boolean needLongClick = false;

    public void setNeedLongClick(boolean isNeedLongClick) {
        needLongClick = isNeedLongClick;
    }

    /*
     * Helper
     */
    private class PerformClickEvent implements Runnable {

        @Override
        public void run() {
            // if parent is an AdapterView, try to call its ItemClickListener
            if (getParent() instanceof AdapterView) {
                clickAdapterView((AdapterView) getParent());
            } else if (rippleInAdapter) {
                // find adapter view
                clickAdapterView(findParentAdapterView());
            } else {
                // otherwise, just perform click on child
//                childView.performClick();
                LeoLog.d("testClickEvent", "PerformClickEvent");
                if (listener != null) {
                    LeoLog.d("testClickEvent", "click!");
                    listener.onClick(RippleView.this);
                }
            }
            isClickAlready = false;
            LeoLog.d("testRippleClick", "click finish");
        }

        private void clickAdapterView(AdapterView parent) {
            try {
                final int position = parent.getPositionForView(RippleView.this);
                final long itemId = parent.getAdapter() != null
                        ? parent.getAdapter().getItemId(position)
                        : 0;
                if (position != AdapterView.INVALID_POSITION) {
                    parent.performItemClick(RippleView.this, position, itemId);
                }
            } catch (Exception e) {
            }
        }
    }

    private final class PressedEvent implements Runnable {

        private final MotionEvent event;

        public PressedEvent(MotionEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            prepressed = false;
            childView.onTouchEvent(event);
            childView.setPressed(true);
            if (rippleHover) {
                startHover();
            }
        }
    }

    static float dpToPx(Resources resources, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    /*
     * Builder
     */

    public static class RippleBuilder {

        private final Context context;
        private final View child;

        private int rippleColor = DEFAULT_COLOR;
        private boolean rippleOverlay = DEFAULT_RIPPLE_OVERLAY;
        private boolean rippleHover = DEFAULT_HOVER;
        private float rippleDiameter = DEFAULT_DIAMETER_DP;
        private int rippleDuration = DEFAULT_DURATION;
        private float rippleAlpha = DEFAULT_ALPHA;
        private boolean rippleDelayClick = DEFAULT_DELAY_CLICK;
        private int rippleFadeDuration = DEFAULT_FADE_DURATION;
        private boolean ripplePersistent = DEFAULT_PERSISTENT;
        private int rippleBackground = DEFAULT_BACKGROUND;
        private boolean rippleSearchAdapter = DEFAULT_SEARCH_ADAPTER;

        public RippleBuilder(View child) {
            this.child = child;
            this.context = child.getContext();
        }

        public RippleBuilder rippleColor(int color) {
            this.rippleColor = color;
            return this;
        }

        public RippleBuilder rippleOverlay(boolean overlay) {
            this.rippleOverlay = overlay;
            return this;
        }

        public RippleBuilder rippleHover(boolean hover) {
            this.rippleHover = hover;
            return this;
        }

        public RippleBuilder rippleDiameterDp(int diameterDp) {
            this.rippleDiameter = diameterDp;
            return this;
        }

        public RippleBuilder rippleDuration(int duration) {
            this.rippleDuration = duration;
            return this;
        }

        public RippleBuilder rippleAlpha(float alpha) {
            this.rippleAlpha = 255 * alpha;
            return this;
        }

        public RippleBuilder rippleDelayClick(boolean delayClick) {
            this.rippleDelayClick = delayClick;
            return this;
        }

        public RippleBuilder rippleFadeDuration(int fadeDuration) {
            this.rippleFadeDuration = fadeDuration;
            return this;
        }

        public RippleBuilder ripplePersistent(boolean persistent) {
            this.ripplePersistent = persistent;
            return this;
        }

        public RippleBuilder rippleBackground(int color) {
            this.rippleBackground = color;
            return this;
        }

        public RippleBuilder rippleInAdapter(boolean inAdapter) {
            this.rippleInAdapter(inAdapter);
            return this;
        }

        public RippleView create() {
            RippleView layout = new RippleView(context);
            layout.setRippleColor(rippleColor);
            layout.setDefaultRippleAlpha((int) rippleAlpha);
            layout.setRippleDelayClick(rippleDelayClick);
            layout.setRippleDiameter((int) dpToPx(context.getResources(), rippleDiameter));
            layout.setRippleDuration(rippleDuration);
            layout.setRippleFadeDuration(rippleFadeDuration);
            layout.setRippleHover(rippleHover);
            layout.setRipplePersistent(ripplePersistent);
            layout.setRippleOverlay(rippleOverlay);
            layout.setRippleBackground(rippleBackground);
            layout.setRippleInAdapter(rippleSearchAdapter);

            ViewGroup.LayoutParams params = child.getLayoutParams();
            ViewGroup parent = (ViewGroup) child.getParent();
            int index = 0;

            if (parent != null && parent instanceof RippleView) {
                throw new IllegalStateException("MaterialRippleLayout could not be created: parent of the view already is a MaterialRippleLayout");
            }

            if (parent != null) {
                index = parent.indexOfChild(child);
                parent.removeView(child);
            }

            layout.addView(child, new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));

            if (parent != null) {
                parent.addView(layout, index, params);
            }

            return layout;
        }
    }
}
