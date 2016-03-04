package com.leo.appmaster.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.leo.appmaster.R;

public class ResponseKeyView extends FrameLayout {
	private OnBackPressListener mBackPressListener;
	private OnCloseClickListener mCloseListener;
	private ImageView mIvClose;
	public ResponseKeyView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		LayoutInflater.from(context).inflate(R.layout.toast_permission_guide, this);
		setUI();
	}

	public ResponseKeyView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.toast_permission_guide, this);  
		setUI();
	}

	private void setUI() {
		mIvClose = (ImageView) findViewById(R.id.iv_permission_guide_close);
		mIvClose.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCloseListener != null) {
					mCloseListener.onClosePressed();
				}
			}
		});
	}

	public ResponseKeyView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.toast_permission_guide, this);  
		setUI();
	}

	public void setOnBackPressedListener(OnBackPressListener listener) {
		mBackPressListener = listener;
	}

	public void setOnCloseClickedListener(OnCloseClickListener listener) {
		mCloseListener = listener;
	}
	
//	LayoutInflater.from(context).inflate(R.layout.toast_permission_guide, this);  
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK) ) {
			Log.i("sss", "back");
			if (mBackPressListener != null) {
				mBackPressListener.onBackPressed();
			}
          return super.dispatchKeyEvent(event);
      } else {
          return super.dispatchKeyEvent(event);
      }
	}
	
	public interface OnBackPressListener {
		public abstract void onBackPressed();
	}
	
	public interface OnCloseClickListener {
		public abstract void onClosePressed();
	}
}
