package com.leo.appmaster.home;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.leo.appmaster.BaseActivity;
import com.leo.appmaster.R;
import com.leo.appmaster.ui.CommonTitleBar;

public class ProtocolActivity extends BaseActivity implements OnClickListener{
    private CommonTitleBar mTtileBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_protocol);
		   mTtileBar = (CommonTitleBar) findViewById(R.id.appwallTB);
	        mTtileBar.setTitle(R.string.protocolBar);
	        mTtileBar.setOptionImageVisibility(View.GONE);
	        mTtileBar.openBackView();
	
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}

	
}
