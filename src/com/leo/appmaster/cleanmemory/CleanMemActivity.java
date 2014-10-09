package com.leo.appmaster.cleanmemory;

import com.leo.appmaster.R;
import com.leo.appmaster.ui.CommonTitleBar;
import com.leo.appmaster.ui.RocketDock;
import com.leo.appmaster.utils.ProcessUtils;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class CleanMemActivity extends Activity implements OnClickListener,
		OnTouchListener {
	private CommonTitleBar mTtileBar;
	private ImageView mRocket;
	private RocketDock mRocketDock;

	private TextView TvMemory;

	private long mLastAvailableMem;
	private long mTotalMem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clean_mem);
		initUI();
	}

	private void initUI() {
		mTtileBar = (CommonTitleBar) findViewById(R.id.layout_title_bar);
		mTtileBar.openBackView();
		mRocket = (ImageView) findViewById(R.id.rocket_icon);
		mRocket.setOnClickListener(this);
		mRocket.setOnTouchListener(this);
		mRocketDock = (RocketDock) findViewById(R.id.rocket_dock);
		TvMemory = (TextView) findViewById(R.id.tv_memory);

		mLastAvailableMem = ProcessUtils.getAvailableMem(this);
		mTotalMem = ProcessUtils.getTotalMem();

		TvMemory.setText(mLastAvailableMem + "/" + mTotalMem + "");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rocket_icon:
			launchRocket();
			break;
		default:
			break;
		}

	}

	public void launchRocket() {
		Animation ta = createRocketFly();
		mRocket.setImageResource(R.drawable.rocket_fly);
		mRocket.startAnimation(ta);
	}

	private Animation createRocketFly() {
		TranslateAnimation ta = new TranslateAnimation(0, 0, 0, -2000);
		ta.setDuration(1000);
		ta.setFillEnabled(true);
		ta.setFillBefore(true);
		ta.setInterpolator(new AccelerateInterpolator());
		return ta;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		return false;
	}

}
