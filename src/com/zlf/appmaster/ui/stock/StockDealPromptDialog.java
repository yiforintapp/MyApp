package com.zlf.appmaster.ui.stock;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zlf.appmaster.R;


public class StockDealPromptDialog extends Dialog {
	private final static String TAG ="StockDealPromptDialog";

	public StockDealPromptDialog(Context context) {
		super(context, R.style.qdialog);
		// TODO Auto-generated constructor stub
	}
	
	public StockDealPromptDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}
	
//	public void setOnDismissListener(onDismissListener listener){
//		mDismissListener = listener;
//	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_stock_deal_prompt);
		Button surebButton = (Button) findViewById(R.id.btn_dialog_sure);
		surebButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				StockDealPromptDialog.this.cancel();
			}
		});
	}

	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
		//Log.d(TAG,"StockDealPromptDialog dimiss");
	
	}
	
}
