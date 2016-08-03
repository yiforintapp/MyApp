package com.zlf.appmaster.utils;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.zlf.appmaster.R;

import java.lang.reflect.Method;
import java.util.List;

public class KeyboardUtil {
	private static final int KEYCODE_CLEAR = 1999;
    private static final int KEYCODE_SEARCH = 1998;
	private static final int KEYCODE_SYSTEM_DEFAULT = 1001;
	

	private KeyboardView mKeyboardView;
	private Keyboard mCharKeyboard;     // 字母键盘
	private Keyboard mNumKeyboard;      // 数字键盘
	public boolean isNum = true;       // 是否数据键盘
	public boolean isUpper = false;     // 是否大写
	private boolean isSystemKeyboardShow = false;	// 系统键盘是否弹出
    private Activity mActivity;
	
	private EditText mEditText;

	public KeyboardUtil(Activity activity, EditText edit) {
        mActivity = activity;

		this.mEditText = edit;
		mCharKeyboard = new Keyboard(mActivity, R.xml.qwerty);
		mNumKeyboard = new Keyboard(mActivity, R.xml.symbols);

        // 以下 键的默认颜色不同
        setDefaultFocusKey(mCharKeyboard.getKeys());
        setDefaultFocusKey(mNumKeyboard.getKeys());

        mKeyboardView = (KeyboardView) activity.findViewById(R.id.keyboard_view);
		mKeyboardView.setKeyboard(mNumKeyboard);
		mKeyboardView.setEnabled(true);
		mKeyboardView.setPreviewEnabled(false);
		mKeyboardView.setOnKeyboardActionListener(listener);
	}

	private OnKeyboardActionListener listener = new OnKeyboardActionListener() {

		@Override
		public void swipeUp() {
		}

		@Override
		public void swipeRight() {
		}

		@Override
		public void swipeLeft() {
		}

		@Override
		public void swipeDown() {
		}

		@Override
		public void onText(CharSequence text) {		// 插入数字串
			mEditText.getText().insert(mEditText.getSelectionStart(), text);
		}

		@Override
		public void onRelease(int primaryCode) {
		}

		@Override
		public void onPress(int primaryCode) {
		}

		@Override
		public void onKey(int primaryCode, int[] keyCodes) {
			Editable editable = mEditText.getText();
			int start = mEditText.getSelectionStart();
			
			switch(primaryCode){
			case Keyboard.KEYCODE_CANCEL:// 完成
				hideKeyboard();
				break;
			case Keyboard.KEYCODE_DELETE:// 回退
				if (editable != null && editable.length() > 0) {
					if (start > 0) {
						editable.delete(start - 1, start);
					}
				}
				break;
			case Keyboard.KEYCODE_SHIFT:// 大小写切换
				changeKey();
				mKeyboardView.setKeyboard(mCharKeyboard);
				break;
			case Keyboard.KEYCODE_MODE_CHANGE:// 数字键盘切换
				if (isNum) {
					isNum = false;
					mKeyboardView.setKeyboard(mCharKeyboard);
				} else {
					isNum = true;
					mKeyboardView.setKeyboard(mNumKeyboard);
				}
				break;
            case KEYCODE_SEARCH:
                    break;
			case KEYCODE_SYSTEM_DEFAULT:
				
				isSystemKeyboardShow = true;
				
				hideKeyboard();
				
				//mEditText.requestFocus();
				InputMethodManager imm = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
				
				break;
			case KEYCODE_CLEAR:
				editable.clear();
				break;
			default:
				editable.insert(start, Character.toString((char) primaryCode));
				break;
			}
		}
	};
	
	/**
	 * 键盘大小写切换
	 */
	private void changeKey() {
		List<Key> keylist = mCharKeyboard.getKeys();
		if (isUpper) {//大写切换小写
			isUpper = false;
			for(Key key:keylist){
				if (key.label!=null && isword(key.label.toString())) {
					key.label = key.label.toString().toLowerCase();
					key.codes[0] = key.codes[0]+32;
				}
			}
		} else {//小写切换大写
			isUpper = true;
			for(Key key:keylist){
				if (key.label!=null && isword(key.label.toString())) {
					key.label = key.label.toString().toUpperCase();
					key.codes[0] = key.codes[0]-32;
				}
			}
		}
	}



    public void showKeyboard() {
        int visibility = mKeyboardView.getVisibility();
        if (visibility == View.GONE || visibility == View.INVISIBLE) {
            mKeyboardView.setVisibility(View.VISIBLE);
        }
    }
    
    public void hideKeyboard() {
        int visibility = mKeyboardView.getVisibility();
        if (visibility == View.VISIBLE) {
            mKeyboardView.setVisibility(View.INVISIBLE);
        }
    }
    
    private boolean isword(String str){
    	String wordstr = "abcdefghijklmnopqrstuvwxyz";
    	if (wordstr.indexOf(str.toLowerCase())>-1) {
			return true;
		}
    	return false;
    }

    public void hideSystemKeyBoard(){

        // 显示光标 4.0后有区别
        if (android.os.Build.VERSION.SDK_INT <= 10) {//4.0以下
            mEditText.setInputType(InputType.TYPE_NULL);
        } else {
            mActivity.getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            try {
                Class<EditText> cls = EditText.class;
                Method setShowSoftInputOnFocus;
                setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus",
                        boolean.class);
                setShowSoftInputOnFocus.setAccessible(true);
                setShowSoftInputOnFocus.invoke(mEditText, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        InputMethodManager imm = (InputMethodManager) mEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }

	public boolean isSystemKeyboardShow() {
		return isSystemKeyboardShow;
	}


    /**
     * 设置特殊键为focus状态
     * @param keys
     */
    private void setDefaultFocusKey(List<Key> keys){
        for (Key key : keys) {

            if (key.codes[0] == KEYCODE_CLEAR
                    || key.codes[0] == KEYCODE_SEARCH
                    || key.codes[0] == KEYCODE_SYSTEM_DEFAULT
                    || key.codes[0] == Keyboard.KEYCODE_CANCEL
                    || key.codes[0] == Keyboard.KEYCODE_DELETE
                    || key.codes[0] == Keyboard.KEYCODE_MODE_CHANGE
                    || key.codes[0] == Keyboard.KEYCODE_SHIFT) {

                key.onPressed();
            }

        }
    }
}
