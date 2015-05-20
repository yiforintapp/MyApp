
package com.leo.appmaster.quickgestures.view;

import com.leo.appmaster.R;
import com.leo.appmaster.utils.LeoLog;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GestureItemView extends TextView {

    private static final String TAG = "GestureTextView";
    private AppleWatchLayout mHolderLayout;
    private DecorateAction mDecorateAction;
    private boolean mEditing;
    private Drawable mCrossDrawable;
    private boolean mIsShowReadTip;
    private boolean mAddFlag = false;

    public GestureItemView(Context context) {
        super(context);
        init();
    }

    public GestureItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    private void init() {
        mCrossDrawable = getContext().getResources().getDrawable(R.drawable.gesture_item_delete);
        mCrossDrawable.setBounds(0, 0, mCrossDrawable.getIntrinsicWidth(),
                mCrossDrawable.getIntrinsicWidth());
    }

    public void setDecorateAction(DecorateAction action) {
        mDecorateAction = action;
    }

    public DecorateAction getDecorateAction() {
        return mDecorateAction;
    }

    public boolean hasAddFlag() {
        return mAddFlag;
    }

    public void setAddFlag(boolean add) {
        mAddFlag = add;
    }

    public Rect getCrossRect() {
        Rect rect = new Rect(0, 0, mCrossDrawable.getIntrinsicWidth() * 2,
                mCrossDrawable.getIntrinsicHeight() * 2);
        return rect;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mHolderLayout = (AppleWatchLayout) getParent();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mEditing && !mAddFlag) {
            drawCross(canvas);
        } else {
            if (mDecorateAction != null) {
                if (mIsShowReadTip) {
                    mDecorateAction.draw(canvas, this);
                }
            }
        }
    }

    private void drawCross(Canvas canvas) {
        mCrossDrawable.draw(canvas);
    }

    public void enterEditMode() {
        mEditing = true;
        invalidate();
    }

    public void leaveEditMode() {
        mEditing = false;
        invalidate();
    }

    public void showReadTip() {
        mIsShowReadTip = true;
        invalidate();
    }

    public void cancelShowReadTip() {
        mIsShowReadTip = false;
        invalidate();
    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED: {
                LeoLog.i(TAG, "ACTION_DRAG_STARTED");
                enterEditMode();
                if (event.getLocalState() == this) {
                    SectorQuickGestureContainer qgc = (SectorQuickGestureContainer) mHolderLayout
                            .getParent();
                    qgc.setEditing(true);
                    setVisibility(View.INVISIBLE);
                }
                break;
            }
            case DragEvent.ACTION_DRAG_ENDED: {
                LeoLog.i(TAG, "ACTION_DRAG_ENDED");
                if (event.getLocalState() == this) {
                    setVisibility(View.VISIBLE);
                    mHolderLayout.onEnterEditMode();
                }
                break;
            }
            case DragEvent.ACTION_DRAG_LOCATION: {
                LeoLog.i(TAG, "ACTION_DRAG_LOCATION: x = " + event.getX() + "  y = " + event.getY());
                if ((GestureItemView) event.getLocalState() != this
                        && !mHolderLayout.isReordering()) {
                    mHolderLayout.squeezeItems((GestureItemView) event.getLocalState(), this);
                }
                break;
            }
            case DragEvent.ACTION_DROP: {
                LeoLog.i(TAG, "ACTION_DROP");
                mHolderLayout.requestLayout();
                break;
            }
            case DragEvent.ACTION_DRAG_ENTERED: {
                LeoLog.i(TAG, "ACTION_DRAG_ENTERED ");
                if ((GestureItemView) event.getLocalState() != this
                        && !mHolderLayout.isReordering()) {
                    mHolderLayout.squeezeItems((GestureItemView) event.getLocalState(), this);
                }
                break;
            }

            case DragEvent.ACTION_DRAG_EXITED: {
                break;
            }
            default:
                LeoLog.i(TAG, "other drag event: " + event);
                break;
        }

        return true;
    }
}
