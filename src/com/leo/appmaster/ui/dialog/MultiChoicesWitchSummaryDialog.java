
package com.leo.appmaster.ui.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.leo.appmaster.R;
import com.leo.appmaster.model.DayTrafficInfo;
import com.leo.appmaster.ui.RippleView;
import com.leo.appmaster.ui.dialog.DayTrafficSetting.TrafficHolder;

public class MultiChoicesWitchSummaryDialog extends LEOBaseDialog {
    public static final String TAG = "XLAlarmDialog";

    private Context mContext;

    private TextView mTitle;
    private TextView mSummary;
    private LinearLayout mLlSummary;
    private TextView mLeftBtn;
    private TextView mRightBtn;
    private RippleView mRvRight;
    private RippleView mRvLeft;
    private ListView mLvMain;
    private MyAdapter mAdapter;

    private OnDiaogClickListener mListener;

    private int nowItemPosition = 0;

    public void setNowItemPosition(int position) {
        nowItemPosition = position;
        mAdapter.notifyDataSetChanged();
    }
    
    public int getNowItemPosition() {
        return nowItemPosition;
    }

    public interface OnDiaogClickListener {
        public void onClick(int which);
    }

    public MultiChoicesWitchSummaryDialog(Context context) {
        super(context, R.style.bt_dialog);
        mContext = context.getApplicationContext();
        initUI();
    }

    public void setTitle(String titleStr) {
        if (titleStr != null) {
            mTitle.setText(titleStr);
        } else {
            mTitle.setText(R.string.tips);
        }
    }

    public void setContent(String titleStr) {
        if (titleStr != null) {
            mSummary.setText(titleStr);
        }
    }

    public void setContentVisible(boolean visible) {
        if (mLlSummary != null) {
            if (visible) {
                mLlSummary.setVisibility(View.VISIBLE);
            } else {
                mLlSummary.setVisibility(View.GONE);
            }
        }
    }


    public void setContentLineSpacing(int lineSpace) {
        mSummary.setLineSpacing(lineSpace, 1);
    }

    public void setContent(SpannableString text) {
        if (text != null) {
            mSummary.setText(text);
        }
    }

    public void setSpanContent(SpannableString titleStr) {
        if (titleStr != null) {
            mSummary.setText(titleStr);
        }
    }

    public void setLeftBtnStr(String lStr) {
        if (lStr != null) {
            mLeftBtn.setText(lStr);
        }
    }

    public void setRightBtnStr(String rStr) {
        if (rStr != null) {
            mRightBtn.setText(rStr);
        }
    }

    public void setRightBtnBackground(Drawable drawable) {
        if (drawable != null) {
            mRightBtn.setBackgroundDrawable(drawable);
        }
    }

    public void setRightBtnBackground(int resid) {
        mRightBtn.setBackgroundResource(resid);
    }

    public void setRightBtnTextColor(int color) {
        mRightBtn.setTextColor(color);
    }

    public void setLeftBtnListener(DialogInterface.OnClickListener lListener) {
        mRvLeft.setTag(lListener);
        mRvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvLeft
                        .getTag();
                try {
                    lListener.onClick(MultiChoicesWitchSummaryDialog.this, 0);
                } catch (Exception e) {
                }
            }
        });
//        mRvLeft.setOnRippleCompleteListener(new OnRippleCompleteListener() {
//
//            @Override
//            public void onRippleComplete(RippleView arg0) {
//                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvLeft
//                        .getTag();
//                try {
//                    lListener.onClick(LEOAlarmDialog.this, 0);
//                } catch (Exception e) {
//                }
//            }
//        });
    }

    public void setRightBtnListener(DialogInterface.OnClickListener rListener) {
        mRvRight.setTag(rListener);
        mRvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener lListener = (DialogInterface.OnClickListener) mRvRight
                        .getTag();
                try {
                    lListener.onClick(MultiChoicesWitchSummaryDialog.this, nowItemPosition);
                } catch (Exception e) {
                }
            }
        });
    }

    private class MyAdapter extends BaseAdapter {
        private Context mContext;

        public MyAdapter(Context ctx) {
            this.mContext = ctx;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.traffic_day_list, parent, false);
            }
            TextView tvContent = (TextView) convertView.findViewById(R.id.tv_showday);
            ImageView ivCheckBox = (ImageView) convertView.findViewById(R.id.iv_showday);
            tvContent.setText(strings[position]);

            if (nowItemPosition == position) {
                ivCheckBox.setImageResource(R.drawable.dialog_check_on);
            } else {
                ivCheckBox.setImageResource(R.drawable.dialog_check_off);
            }
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public int getCount() {
            return strings.length;
        }

    }

    private String[] strings;

    public void fillData(String[] itemContent) {
        strings = itemContent;
        mLvMain.setAdapter(mAdapter);
    }

    public ListView getListView() {
        return mLvMain;
    }

    private void initUI() {
        View dlgView = LayoutInflater.from(mContext).inflate(
                R.layout.dialog_multichoices_with_summary, null);
        mLlSummary = (LinearLayout) dlgView.findViewById(R.id.ll_summary);
        mTitle = (TextView) dlgView.findViewById(R.id.dlg_title);
        mSummary = (TextView) dlgView.findViewById(R.id.tv_summary);
        mLvMain = (ListView) dlgView.findViewById(R.id.lv_main);
        mAdapter = new MyAdapter(mContext);
        mRvRight = (RippleView) dlgView.findViewById(R.id.rv_dialog_blue_button);
        mRvLeft = (RippleView) dlgView.findViewById(R.id.rv_dialog_whitle_button);
        mLeftBtn = (TextView) dlgView.findViewById(R.id.dlg_left_btn);
        mRightBtn = (TextView) dlgView.findViewById(R.id.dlg_right_btn);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                if (mListener != null) {
                    mListener.onClick(arg1);
                }
                dialog.dismiss();
            }
        };
        setLeftBtnListener(listener);
        setRightBtnListener(listener);
        setContentView(dlgView);
        setCanceledOnTouchOutside(true);
    }


    public void setSummaryGravity(int gravity) {
        mSummary.setGravity(gravity);
    }


    public void setSureButtonText(String mText) {
        if (mText != null) {
            mRightBtn.setText(mText);
        }
    }


    public void setOnClickListener(OnDiaogClickListener listener) {
        mListener = listener;
    }

    public void setLeftBtnVisibility(boolean flag) {
        if (!flag) {
            mLeftBtn.setVisibility(View.GONE);
        }
    }

    public void setRightBtnParam(float width, float height) {
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) mRvRight
                .getLayoutParams();

        linearParams.height = (int) height;
        linearParams.width = (int) width;
        mRvRight.setLayoutParams(linearParams);
    }

    public void setRightBtnParam(float width, float height, boolean isLeftGone) {
        LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) mRvRight
                .getLayoutParams();
        linearParams.gravity = Gravity.CENTER_HORIZONTAL;
        linearParams.height = (int) height;
        linearParams.width = (int) width;
        mRvRight.setLayoutParams(linearParams);
    }
}
