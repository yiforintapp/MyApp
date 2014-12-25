package com.leo.appmaster.sdk.push.ui;

import java.util.List;

import com.leo.appmaster.R;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ViewpagerView extends LinearLayout {

	private Context ctx;
	private int position;
	private TextView textView1;
	private TextView textView2;
	private TextView tv_tishi_hint;
	private EditText editText;
	private List<String> msgList;
	private final static int CUSTOM_WISH_CHAR_LIMITED = 79;

	public ViewpagerView(Context context) {
		super(context);
		this.ctx = context;
		initView();
	}

	public ViewpagerView(Context context, int position, List<String> msg_string) {
		super(context);
		this.ctx = context;
		this.position = position;
		this.msgList = msg_string;
		initView();
	}

	private void initView() {
		LayoutInflater mInflater = LayoutInflater.from(ctx);
		View myView = mInflater.inflate(R.layout.msg_content_view, null);
		textView1 = (TextView) myView.findViewById(R.id.tv_msg_tishi);
		textView2 = (TextView) myView.findViewById(R.id.tv_msg);
		tv_tishi_hint = (TextView) myView.findViewById(R.id.tv_tishi_hint);
		editText = (EditText) myView.findViewById(R.id.ed_msg);
		textView1.setText(position + 1 + "/" + msgList.size());
		textView2.setText(msgList.get(position));

		if (position == 10) {
			textView2.setVisibility(View.GONE);
			editText.setVisibility(View.VISIBLE);
			editText.setHint(ctx.getString(R.string.wish_custom));
		}

		editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
				CUSTOM_WISH_CHAR_LIMITED) });

		editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}
			@Override
			public void afterTextChanged(Editable arg0) {
				handleWishMsgLimit(arg0.length());
			}
		});

		if (myView != null) {
			addView(myView);
		}
	}

	private void handleWishMsgLimit(int length) {
		if (CUSTOM_WISH_CHAR_LIMITED - length < 10) {
			String hint = ctx.getString(R.string.wish_char_hint,
					CUSTOM_WISH_CHAR_LIMITED - length);
			textView1.setVisibility(View.GONE);
			tv_tishi_hint.setText(hint);
			tv_tishi_hint.setVisibility(View.VISIBLE);
		} else {
			textView1.setVisibility(View.VISIBLE);
			tv_tishi_hint.setVisibility(View.GONE);
		}
	}
	
	public String getMsgContent(int currentPosition){
		if(currentPosition +1 < msgList.size()){
			return msgList.get(currentPosition);
		}else{
			return editText.getText().toString();
		}
	}

}
