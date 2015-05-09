
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
    private QuickGestureLayout mHolderLayout;
    private DecorateAction mDecorateAction;
    private boolean mEditing;
    private Drawable mCrossDrawable;

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
        mCrossDrawable = getContext().getResources().getDrawable(R.drawable.app_uninstall_btn);
        mCrossDrawable.setBounds(0, 0, mCrossDrawable.getIntrinsicWidth(),
                mCrossDrawable.getIntrinsicWidth());
    }

    public void setDecorateAction(DecorateAction action) {
        mDecorateAction = action;
    }

    public DecorateAction getDecorateAction() {
        return mDecorateAction;
    }

    public Rect getCrossRect() {
        Rect rect = new Rect(0, 0, mCrossDrawable.getIntrinsicWidth(),
                mCrossDrawable.getIntrinsicHeight());
        return rect;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mHolderLayout = (QuickGestureLayout) getParent();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mEditing) {
            drawCross(canvas);
        } else {
            if (mDecorateAction != null) {
                mDecorateAction.draw(canvas, this);
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

    @Override
    public boolean onDragEvent(DragEvent event) {
        ClipData data = event.getClipData();
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED: {
                LeoLog.i(TAG, "ACTION_DRAG_STARTED");
                enterEditMode();
                if (event.getLocalState() == this) {
                     setVisibility(View.INVISIBLE);
                }
                break;
            }
            case DragEvent.ACTION_DRAG_ENDED: {
                LeoLog.i(TAG, "ACTION_DRAG_ENDED");
                if (event.getLocalState() == this) {
                    setVisibility(View.VISIBLE);
                }
                // leaveEditMode();
                break;
            }
            case DragEvent.ACTION_DRAG_LOCATION: {
                LeoLog.i(TAG, "ACTION_DRAG_LOCATION: x = " + event.getX() + "  y = " + event.getY());
                break;
            }
            case DragEvent.ACTION_DROP: {
                LeoLog.i(TAG, "ACTION_DROP");
                mHolderLayout.requestLayout();
                break;
            }
            case DragEvent.ACTION_DRAG_ENTERED: {
                LeoLog.i(TAG, "ACTION_DRAG_ENTERED ");
                mHolderLayout.squeezeItems((GestureItemView) event.getLocalState(), this);
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
