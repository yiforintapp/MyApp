package com.leo.appmaster.home;
import java.util.Locale;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;

import com.leo.appmaster.BaseActivity;
import com.leo.appmaster.R;
import com.leo.appmaster.ui.CommonTitleBar;

public class ProtocolActivity extends BaseActivity implements OnClickListener{
    private CommonTitleBar mTtileBar;
    private WebView webView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_protocol);
		   mTtileBar = (CommonTitleBar) findViewById(R.id.appwallTB);
	        mTtileBar.setTitle(R.string.protocolBar);
	        mTtileBar.setOptionImageVisibility(View.GONE);
	        mTtileBar.openBackView();
	        webView=(WebView) findViewById(R.id.webView);
	       
	        String area = Locale.getDefault().getLanguage();
		    if(area.equalsIgnoreCase("zh")){
		    	 webView.loadUrl("file:///android_asset/protocol_zh.html");
		    }else if(area.equalsIgnoreCase("hi")){
		    	 webView.loadUrl("file:///android_asset/protocol_hi.html");
		    }else{
		    	 webView.loadUrl("file:///android_asset/protocol_en.html");
		    }
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}

	
}
