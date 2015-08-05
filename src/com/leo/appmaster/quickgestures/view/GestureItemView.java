
package com.leo.appmaster.quickgestures.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.model.BusinessItemInfo;
import com.leo.appmaster.quickgestures.QuickGestureManager;
import com.leo.appmaster.quickgestures.model.GestureEmptyItemInfo;
import com.leo.appmaster.quickgestures.view.AppleWatchContainer.GType;
import com.leo.appmaster.utils.LeoLog;

@SuppressLint("CutPasteId")
public class GestureItemView extends LinearLayout {

    private static final String TAG = "GestureTextView";
    private AppleWatchLayout mHolderLayout;
    private DecorateAction mDecorateAction;
    private boolean mEditing;
    private Drawable mCrossDrawable;
    private boolean mIsShowReadTip;
    private TextView mTextView;
    private ImageView mImageView;

    public GestureItemView(Context context) {
        super(context);
    }

    public GestureItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public boolean isEmptyIcon() {
        return getTag() instanceof GestureEmptyItemInfo;
    }

    @Override
    protected void onFinishInflate() {
        mTextView = (TextView) findViewById(R.id.tv_app_name);
        mImageView = (ImageView) findViewById(R.id.iv_app_icon);
        super.onFinishInflate();
    }

    private void init() {
        mCrossDrawable = getContext().getResources().getDrawable(R.drawable.app_stop_btn);
        mCrossDrawable.setBounds(0, 0, mCrossDrawable.getIntrinsicWidth(),
                mCrossDrawable.getIntrinsicWidth());
    }

    public void setItemName(String name) {
        mTextView.setText(name);
    }

    public String getItemName() {
        return mTextView.getText().toString();
    }

    public void setItemIcon(Drawable icon, boolean needColor) {
        if (needColor) {
            QuickGestureManager qgm = QuickGestureManager.getInstance(getContext());
            Drawable target = qgm.getMatchedColor(icon);
            mImageView.setBackgroundDrawable(target);
            mImageView.setImageDrawable(icon);
        } else {
            mImageView.setBackgroundDrawable(icon);
            mImageView.setImageDrawable(null);
        }
    }

    public void setDecorateAction(DecorateAction action) {
        mDecorateAction = action;
        invalidate();
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
        mHolderLayout = (AppleWatchLayout) getParent();

        setPivotX(getMeasuredWidth() / 2);
        setPivotY(getMeasuredHeight() / 2);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (isBusinessItem()) {
            if (mDecorateAction == null) {
                mDecorateAction = new EventAction(getContext(), 0);
                mIsShowReadTip = true;
            }
        }

        if (mEditing && !(getTag() instanceof GestureEmptyItemInfo)) {
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
        Object tag = getTag();
        if (tag instanceof GestureEmptyItemInfo && !isDynamicItem()) {
            mImageView.setImageResource(R.drawable.switch_color_add);
        }
        invalidate();
    }

    public void leaveEditMode() {
        mEditing = false;
        Object tag = getTag();
        if (tag instanceof GestureEmptyItemInfo && !isDynamicItem()) {

            mImageView.setImageDrawable(null);
        }
        invalidate();
    }

    private boolean isBusinessItem() {
        Object tag = getTag();
        return tag instanceof BusinessItemInfo;
    }

    private boolean isDynamicItem() {
        if(mHolderLayout != null) {
            AppleWatchContainer container = mHolderLayout.getContainer();
            if(container != null) {
                GType type = container.getCurrentGestureType();
                if (type == GType.DymicLayout) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public void showReadTip() {
        mIsShowReadTip = true;
        invalidate();
    }

    public boolean getIsShowReadTip() {
        return mIsShowReadTip;
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
                    AppleWatchContainer qgc = (AppleWatchContainer) mHolderLayout
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
