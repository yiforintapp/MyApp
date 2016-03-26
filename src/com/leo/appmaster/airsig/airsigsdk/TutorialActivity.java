package com.leo.appmaster.airsig.airsigsdk;

import java.util.ArrayList;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.airsig.airsigengmulti.ASEngine;
import com.airsig.airsigengmulti.ASEngine.ASError;
import com.leo.appmaster.R;
import com.leo.appmaster.airsig.airsigui.AnimatedGifImageView;
import com.leo.appmaster.airsig.airsigutils.EventLogger;
import com.leo.appmaster.airsig.airsigutils.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.TextUtils.TruncateAt;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TutorialActivity extends Activity {
	
	private enum Step {
		ChooseWordPractice(null),
		WristPractice(ChooseWordPractice),
		WritePractice(WristPractice),
		HowToWrite(WritePractice);
		
		final Step next;
		
		Step(Step next) {
			this.next = next;
		}
	}
	
	// private data
	private Step mCurrentStep = null;
	private int mChooseWordPracticeTimes = 0;
	
	// UI components for input signature
	private RelativeLayout mTouchBox;
	private LinearLayout mToucharea;
	
	// UI components for messages
	private RelativeLayout mMessageBox;
	private TextView mTouchareaMessage;
	private MessageBoxFragment mMessageBoxFragment;
	
	// UI components steps
	private AutoFontSizeTextView mWritePracticeTextView;
	private AnimatedGifImageView mWritePracticeImageView;
	
	// other UI components
	private ProgressBar mProgressBar;
	private ToneGenerator mToneGenerator = null;
	private ToneGenerator mToneGenerator2 = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.airsig_activity_tutorial);

		// customize action bar: 
		// 1. no App icon 
		// 2. back button
		if (null != getActionBar()) {
			getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));  
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setDisplayShowTitleEnabled(false);
			getActionBar().setDisplayShowCustomEnabled(true);
			TextView title = new TextView(getApplicationContext());
			ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			title.setLayoutParams(lp);
			title.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
			title.setEllipsize(TruncateAt.END);
			title.setTextColor(getResources().getColor(R.color.airsig_actionbar_title));
			title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.airsig_actionbar_title_textsize));
			title.setText(getResources().getString(R.string.airsig_tutorial_title));
			getActionBar().setCustomView(title);
		}
		
		// initialize UI components:
		mTouchBox = (RelativeLayout) findViewById(R.id.viewTouchBox);
		mTouchBox.post(new Runnable() {
			@Override
			public void run() {
//				mTouchBox.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
//		    	mTouchBox.getLayoutParams().height = (int) (mTouchBox.getMeasuredHeight() + (mStatusBarHeight == 0 ? Utils.getStatusBarHeight(getApplicationContext()) : 0) - Utils.dp2px(getResources(), 4));
//		    	mTouchBox.requestLayout();
			}
		});
		mToucharea = (LinearLayout) findViewById(R.id.viewTouchArea);
		mToucharea.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return onTouchPressArea(v, event);
			}
		});
		
		// UI components for messages
		mMessageBox = (RelativeLayout) findViewById(R.id.viewMessageBox);
		mTouchareaMessage = (TextView) findViewById(R.id.textTouchAreaMessage);
		
		// other components
		mProgressBar = (ProgressBar) findViewById(R.id.progressBarWaiting);
		
		// display initial view
		new Handler().postDelayed(new Runnable() {
    	    public void run() {
    	    	gotoStep(Step.HowToWrite);
    	    }
    	}, 300);
		
		// Log event
		EventLogger.startAction(EventLogger.EVENT_NAME_TUTORIAL, null, true);
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        if (null != ASEngine.getSharedInstance()) {
        	ASEngine.getSharedInstance().startSensors();
        }
    }
    
    @Override
    protected void onPause() {
    	if (null != ASEngine.getSharedInstance()) {
    		ASEngine.getSharedInstance().stopSensors();
        }
    	if (null != mToneGenerator) {
			mToneGenerator.release();
			mToneGenerator = null;
		}
		if (null != mToneGenerator2) {
			mToneGenerator2.release();
			mToneGenerator2 = null;
		}
		super.onPause();
    }
    
    @Override
	public void onBackPressed() {
    	showDialog(getResources().getString(R.string.airsig_tutorial_dialog_confirm_exit_title),
			getResources().getString(R.string.airsig_tutorial_dialog_confirm_exit_detail),
			R.drawable.airsig_ic_dialog,
			getResources().getString(R.string.airsig_tutorial_dialog_confirm_exit_postive_button),
			new OnClickListener() {
				@Override
				public void onClick(View v) {
					finishActivity();
				}
			},
			getResources().getString(R.string.airsig_tutorial_dialog_confirm_exit_negative_button), null
		);
	}
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		if (android.R.id.home == item.getItemId()) {
			showDialog(getResources().getString(R.string.airsig_tutorial_dialog_confirm_exit_title),
				getResources().getString(R.string.airsig_tutorial_dialog_confirm_exit_detail),
				R.drawable.airsig_ic_dialog,
				getResources().getString(R.string.airsig_tutorial_dialog_confirm_exit_postive_button),
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						finishActivity();
					}
				},
				getResources().getString(R.string.airsig_tutorial_dialog_confirm_exit_negative_button), null
			);
		}

		return true;
	}
    
    private void gotoStep(final Step step) {
    	this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mCurrentStep = step;
				switch(step) {
				case HowToWrite: {
					showImageMessage(getResources().getString(R.string.airsig_tutorial_step_how_to_write_detail), 
						R.raw.airsig_ani_tutorial_total,
						getResources().getString(R.string.airsig_tutorial_step_how_to_write_next),
						new OnClickListener() {
							@Override
							public void onClick(View v) {
								gotoStep(step.next);
							}
						},
						null, null
					);
					break;
				}
				case WritePractice: {
					final View contentView = getLayoutInflater().inflate(R.layout.airsig_fragment_tutorial_img_msg_small, null);
					mWritePracticeImageView = (AnimatedGifImageView) contentView.findViewById(R.id.gifImageView);
					mWritePracticeImageView.setAnimatedGif(R.raw.airsig_ani_tutorial_total, AnimatedGifImageView.TYPE.FIT_CENTER);
					mWritePracticeImageView.setAnimatedGifPlayTime(0,  1200, 1000);
					mWritePracticeTextView = (AutoFontSizeTextView) contentView.findViewById(R.id.textView);
					mWritePracticeTextView.setText(getResources().getString(R.string.airsig_tutorial_step_write_practice1));
					collapseMessageBox(contentView);
					break;
				}
				case WristPractice: {
					final View contentView = getLayoutInflater().inflate(R.layout.airsig_fragment_tutorial_img_msg_small, null);
					AnimatedGifImageView imageView = (AnimatedGifImageView) contentView.findViewById(R.id.gifImageView);
					imageView.setAnimatedGif(R.raw.airsig_ani_tutorial_total, AnimatedGifImageView.TYPE.FIT_CENTER);
					imageView.setAnimatedGifPlayTime(900,  2200, 0);
					AutoFontSizeTextView textView = (AutoFontSizeTextView) contentView.findViewById(R.id.textView);
					textView.setText(getResources().getString(R.string.airsig_tutorial_step_wrist_practice));
					collapseMessageBox(contentView);
					break;
				}
				case ChooseWordPractice: {
					final View contentView = getLayoutInflater().inflate(R.layout.airsig_fragment_tutorial_img_msg_small, null);
					AnimatedGifImageView imageView = (AnimatedGifImageView) contentView.findViewById(R.id.gifImageView);
					imageView.setAnimatedGif(R.raw.airsig_ani_tutorial_total, AnimatedGifImageView.TYPE.FIT_CENTER);
					AutoFontSizeTextView textView = (AutoFontSizeTextView) contentView.findViewById(R.id.textView);
					textView.setText(getResources().getString(R.string.airsig_tutorial_step_choose_word_practice));
					collapseMessageBox(contentView);
					break;
				}
				default:
					break;
				}
			}
    	});
    }
    
    private boolean onTouchPressArea(final View v, final MotionEvent event) {
    	if (v != mToucharea) {
    		return false;
    	}
    	
    	if (isWaiting()) {
    		return false;
    	}
    	
    	switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			pressTouchArea(true);
			if (mCurrentStep == Step.WritePractice && mWritePracticeTextView != null) {
				mWritePracticeTextView.setText(getResources().getString(R.string.airsig_tutorial_step_write_practice2));
				mWritePracticeImageView.setAnimatedGif(R.raw.airsig_ani_tutorial_holding_posture, AnimatedGifImageView.TYPE.FIT_CENTER);
				mWritePracticeImageView.setAnimatedGifPlayTime(0,  3900, 0);
				ASEngine.getSharedInstance().startRecordingSensor(new ASEngine.OnSensorEventListener() {
					@Override
					public void startWriting() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (mWritePracticeTextView != null) {
									mWritePracticeTextView.setText(getResources().getString(R.string.airsig_tutorial_step_write_practice3));
									mWritePracticeImageView.setAnimatedGif(R.raw.airsig_ani_tutorial_total, AnimatedGifImageView.TYPE.FIT_CENTER);
									mWritePracticeImageView.setAnimatedGifPlayTime(2000,  mWritePracticeImageView.getAnimatedGifLength(), 0);
								}
							}
				    	});
					}
				});
			} else {
				ASEngine.getSharedInstance().startRecordingSensor(null);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			pressTouchArea(false);
			ArrayList<float[]> signData = ASEngine.getSharedInstance().stopRecordingSensor();
			ArrayList<ASError> methods = new ArrayList<ASError>();
			
			if (mCurrentStep == Step.WritePractice) {
				mWritePracticeTextView = null;
				mWritePracticeImageView = null;
				methods.add(ASError.TRAINING_INVALID_HOLDING_POSTURE);
			} else if (mCurrentStep == Step.WristPractice) {
				methods.add(ASError.TRAINING_INVALID_HOLDING_POSTURE);
				methods.add(ASError.TRAINING_INVALID_FEW_WRIST);
			} else if (mCurrentStep == Step.ChooseWordPractice) {
				methods.add(ASError.TRAINING_INVALID_HOLDING_POSTURE);
				methods.add(ASError.TRAINING_INVALID_FEW_WRIST);
				methods.add(ASError.TRAINING_INVALID_FEW_WORDS);
			}
			
			ASEngine.getSharedInstance().validateSignature(signData, methods, new ASEngine.OnValidateSignatureResultListener() {
				@Override
				public void onResult(ASError error, Map<String, Object> extraData) {
					if (error != null) {
						switch(error) {
						case TRAINING_INVALID_SHORT_SIGNATURE:
							gotoStep(mCurrentStep);
							break;
						case TRAINING_INVALID_HOLDING_POSTURE:
							showImageMessage(getResources().getString(R.string.airsig_tutorial_err_posture),
								R.raw.airsig_ani_tutorial_holding_posture,
								null, new OnClickListener() {
									@Override
									public void onClick(View v) {
										gotoStep(mCurrentStep);
									}
								},
								null, null
							);
							break;
						case TRAINING_INVALID_FEW_WRIST:
							showImageMessage(getResources().getString(R.string.airsig_tutorial_err_few_wrist),
								R.raw.airsig_ani_tutorial_holding_posture,
								null, new OnClickListener() {
									@Override
									public void onClick(View v) {
										gotoStep(mCurrentStep);
									}
								},
								null, null
							);
							break;
						case TRAINING_INVALID_FEW_WORDS:
							final ASEngine.ASSignatureSecurityLevel level = (ASEngine.ASSignatureSecurityLevel) error.userData.get(ASError.KEY_TRAINING_SECURITY_LEVEL);
							if (level != null) {
								displaySecurityLevel(level.level, false);
							}
							break;
						default:
							gotoStep(mCurrentStep);
							break;
						}
						
					} else {
						if (mCurrentStep == Step.WritePractice) {
							showTextMessage(getResources().getString(R.string.airsig_tutorial_step_write_practice_success_title),
								getResources().getString(R.string.airsig_tutorial_step_write_practice_success_detail),
								getResources().getString(R.string.airsig_tutorial_step_write_practice_success_next),
								new OnClickListener() {
									@Override
									public void onClick(View v) {
										if (mCurrentStep != null) {
											gotoStep(mCurrentStep.next);
										}
									}
								},
								null, null
							);
						} else if (mCurrentStep == Step.WristPractice) {
							showTextMessage(getResources().getString(R.string.airsig_tutorial_step_wrist_practice_success_title),
								getResources().getString(R.string.airsig_tutorial_step_wrist_practice_success_detail),
								getResources().getString(R.string.airsig_tutorial_step_wrist_practice_success_next),
								new OnClickListener() {
									@Override
									public void onClick(View v) {
										if (mCurrentStep != null) {
											gotoStep(mCurrentStep.next);
										}
									}
								},
								null, null
							);
						} else if (mCurrentStep == Step.ChooseWordPractice && extraData != null && extraData.size() > 0) {
							ASEngine.ASSignatureSecurityLevel sl = (ASEngine.ASSignatureSecurityLevel) extraData.get(ASError.KEY_TRAINING_SECURITY_LEVEL);
							if (sl != null) {
								displaySecurityLevel(sl.level, true);
							}
						}
					}
				}
			});
			
			break;
    	}
    	
    	return true;
    }
    
    private void enableTouchArea(final boolean enable) {
    	this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mTouchBox.setEnabled(enable);
				mToucharea.setEnabled(enable);
				if (enable) {
					mTouchareaMessage.setVisibility(View.VISIBLE);
				} else {
					mTouchareaMessage.setVisibility(View.GONE);
				}
			}
    	});
    }
    
    private void pressTouchArea(final boolean pressed) {
    	this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (pressed) {
					// Touch Area
					mToucharea.setBackground(getResources().getDrawable(R.drawable.airsig_training_toucharea_pressed));
					ViewGroup.MarginLayoutParams mpp = (ViewGroup.MarginLayoutParams) mToucharea.getLayoutParams();
					mpp.setMargins((int) Utils.dp2px(getResources(), 9), (int)Utils.dp2px(getResources(), 9), (int)Utils.dp2px(getResources(), 9), (int)Utils.dp2px(getResources(), 11));
					mToucharea.setLayoutParams(mpp);
					mTouchareaMessage.setVisibility(View.GONE);
				} else {
					mToucharea.setBackground(getResources().getDrawable(R.drawable.airsig_training_toucharea));
					ViewGroup.MarginLayoutParams mp = (ViewGroup.MarginLayoutParams) mToucharea.getLayoutParams();
					mp.setMargins((int)Utils.dp2px(getResources(), 3), (int)Utils.dp2px(getResources(), 5), (int)Utils.dp2px(getResources(), 3), (int)Utils.dp2px(getResources(), 0));
					mToucharea.setLayoutParams(mp);
				}
			}
    	});
    }
    
    private void displaySecurityLevel(final int level, final boolean pass) {
    	if (level < 0 || level > 5) {
    		return;
    	}
    	mChooseWordPracticeTimes++;
    	
    	this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				LayoutInflater inflater = getLayoutInflater();
				ViewGroup contentView = (ViewGroup) inflater.inflate(R.layout.airsig_fragment_tutorial_security_level, null);
				AutoFontSizeTextView titleView = (AutoFontSizeTextView) contentView.findViewById(R.id.textTitle);
				ImageView stars = (ImageView) contentView.findViewById(R.id.imageStars);
				
				switch(level) {
				case 0:
					titleView.setText(R.string.airsig_tutorial_security_level_result_none);
					stars.setImageResource(R.drawable.airsig_training_stars_0);
					break;
				case 1:
					titleView.setText(R.string.airsig_tutorial_security_level_result_insufficient);
					stars.setImageResource(R.drawable.airsig_training_stars_1);
					break;
				case 2:
					titleView.setText(R.string.airsig_tutorial_security_level_result_insufficient);
					stars.setImageResource(R.drawable.airsig_training_stars_2);
					break;
				case 3:
					titleView.setText(R.string.airsig_tutorial_security_level_result_insufficient);
					stars.setImageResource(R.drawable.airsig_training_stars_3);
					break;
				case 4:
					titleView.setText(R.string.airsig_tutorial_security_level_result_insufficient);
					stars.setImageResource(R.drawable.airsig_training_stars_4);
					break;
				case 5:
					titleView.setText(R.string.airsig_tutorial_security_level_result_insufficient);
					stars.setImageResource(R.drawable.airsig_training_stars_5);
					break;
				}
				
				if (pass) {
					titleView.setText(R.string.airsig_tutorial_security_level_result_sufficient);
				}
				
				// put into message box and display
				expandMessageBox(contentView,
					getResources().getString(R.string.airsig_tutorial_security_level_result_positive_button), 
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							gotoStep(mCurrentStep);
						}
					}, 
					getResources().getString(R.string.airsig_tutorial_security_level_result_negative_button), 
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							finishActivity();
						}
					}
				);
			}
		});
    }
    
    private void expandMessageBox(final View contentView,
    	final String positiveButtonText, final OnClickListener positiveButtonClickListener,
    	final String negativeButtonText, final OnClickListener negativeButtonClickListener) {
    	this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				enableTouchArea(false);
				
				// Set content of message box
				if (mMessageBoxFragment == null) {
					mMessageBox.removeAllViews();
				}
		    	final MessageBoxFragment newFragment = new MessageBoxFragment();
		    	mMessageBoxFragment = newFragment;
		    	if (positiveButtonText != null && positiveButtonText.length() > 0) {
		    		newFragment.setPositiveButton(positiveButtonText, positiveButtonClickListener);
		    	}
		    	if (negativeButtonText != null && negativeButtonText.length() > 0) {
		    		newFragment.setNegativeButton(negativeButtonText, negativeButtonClickListener);
		    	}
		    	if (contentView != null) {
		    		newFragment.setContentView(contentView);
		    	}
		    	FragmentTransaction transaction = getFragmentManager().beginTransaction();
		    	transaction.replace(R.id.viewMessageBox, newFragment);
		    	transaction.addToBackStack(null);
		    	transaction.commit();
				
		    	// Animation
		    	int duration = getResources().getInteger(R.integer.airsig_message_box_animation_duration);
		    	
		    	mMessageBox.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
				if (mMessageBox.getMeasuredHeight() <= getResources().getDimensionPixelSize(R.dimen.airsig_small_message_box_height)) {
					// Animation for touch area
			    	AlphaAnimation aa = new AlphaAnimation(1f, 0f);
			    	aa.setFillAfter(true);
			    	aa.setInterpolator(new FastOutSlowInInterpolator());
			    	aa.setDuration(duration);
			    	mTouchBox.startAnimation(aa);
			    	
			    	// Animation for message box
					newFragment.onExpandStart();
			    	Utils.animationChangeSize(mMessageBox, -1, getResources().getDimensionPixelSize(R.dimen.airsig_full_message_box_height), duration);
			    	new Handler().postDelayed(new Runnable() {
			    	    public void run() {
			    	    	newFragment.onExpandEnd();
			    	    }
			    	}, duration);
				}
			}
    	});
    }
    
    private void collapseMessageBox(final View contentView) {
    	this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Animation
				int duration = getResources().getInteger(R.integer.airsig_message_box_animation_duration);
		    	
		    	if (mMessageBox.getMeasuredHeight() <= getResources().getDimensionPixelSize(R.dimen.airsig_small_message_box_height)) {
		    		enableTouchArea(true);
		    		mMessageBox.removeAllViews();
			    	if (contentView != null) {
			    		mMessageBox.addView(contentView);
			    	}
		    	} else {
		    		// Animation for touch area
		    		AlphaAnimation aa = new AlphaAnimation(0f, 1f);
			    	aa.setFillAfter(false);
			    	aa.setInterpolator(new FastOutSlowInInterpolator());
			    	aa.setDuration(duration);
			    	new Handler().postDelayed(new Runnable() {
			    	    public void run() {
			    	    	enableTouchArea(true);
			    	    }
			    	}, aa.getDuration());
			    	mTouchBox.startAnimation(aa);
			    	
			    	// Animation for message box
			    	Utils.animationChangeSize(mMessageBox, -1, getResources().getDimensionPixelSize(R.dimen.airsig_small_message_box_height), duration);
			    	
			    	if (mMessageBox.getChildCount() > 0 && mMessageBoxFragment != null) {
			    		mMessageBoxFragment.onCollapseStart();
			    		AlphaAnimation aa2 = new AlphaAnimation(1f, 0f);
				    	aa2.setFillAfter(false);
				    	aa2.setInterpolator(new FastOutSlowInInterpolator());
				    	aa2.setDuration(duration);
				    	mMessageBoxFragment.getView().startAnimation(aa2);
				    	
			    		new Handler().postDelayed(new Runnable() {
				    	    public void run() {
				    	    	mMessageBoxFragment.onCollapseEnd();
				    	    	mMessageBoxFragment = null;
				    	    	mMessageBox.removeAllViews();
						    	if (contentView != null ) {
						    		mMessageBox.addView(contentView);
						    	}
				    	    }
				    	}, duration);
			    	} else {
			    		mMessageBox.removeAllViews();
				    	if (contentView != null ) {
				    		mMessageBox.addView(contentView);
				    	}
			    	}
		    	}
			}
    	});
    }
    
    private void showImageMessage(final CharSequence message, final Integer gifResourceId,
    		final String customButtonString, final OnClickListener positiveButtonClickListener,
    		final String negativeButton, final OnClickListener negativeButtonClickListener) {
    		this.runOnUiThread(new Runnable() {
    			@Override
    			public void run() {
    				View contentView = (View) getLayoutInflater().inflate(R.layout.airsig_fragment_training_image_message, null);
    				if (gifResourceId != null) {
    					AnimatedGifImageView imageView =  (AnimatedGifImageView) contentView.findViewById(R.id.gifImageView);
    					imageView.setVisibility(View.VISIBLE);
    					if ("drawable".compareToIgnoreCase(getResources().getResourceTypeName(gifResourceId)) == 0) {
    						imageView.setImageResource(gifResourceId);
    					} else {
    						imageView.setAnimatedGif(gifResourceId, AnimatedGifImageView.TYPE.FIT_CENTER);
    					}
    				}
    				if (message != null) {
    					AutoFontSizeTextView textView = (AutoFontSizeTextView) contentView.findViewById(R.id.textView);
    					textView.setVisibility(View.VISIBLE);
    					textView.setText(message);
    				}
    				
    				// put into message box and display
    				expandMessageBox(contentView,
    					(customButtonString == null ? getResources().getString(R.string.airsig_tutorial_err_default_button) : customButtonString), positiveButtonClickListener,
    					negativeButton, negativeButtonClickListener
    				);
    			}
    		});
    	}
    	
    	private void showTextMessage(final String title, final String detail,
    		final String customButtonString, final OnClickListener positiveButtonClickListener,
    		final String negativeButton, final OnClickListener negativeButtonClickListener) {
    		this.runOnUiThread(new Runnable() {
    			@Override
    			public void run() {
    				View contentView = (View) getLayoutInflater().inflate(R.layout.airsig_fragment_training_text_message, null);
    				if (detail != null) {
    					TextView titleView = (TextView) contentView.findViewById(R.id.textTitle);
    					titleView.setVisibility(View.VISIBLE);
    					titleView.setText(title);
    				}
    				if (detail != null) {
    					TextView detailView = (TextView) contentView.findViewById(R.id.textContent);
    					detailView.setVisibility(View.VISIBLE);
    					detailView.setText(detail);
    				}
    				
    				// put into message box and display
    				expandMessageBox(contentView,
    					(customButtonString == null ? getResources().getString(R.string.airsig_tutorial_err_default_button) : customButtonString), positiveButtonClickListener,
    					negativeButton, negativeButtonClickListener
    				);
    			}
    		});
    	}
    
    private void showDialog(final String title, final String detail, final Integer image,
		final String postiveButton, final OnClickListener positiveButtonClickListener,
		final String negativeButton, final OnClickListener negativeButtonClickListener) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertBuilder alert = new AlertBuilder(TutorialActivity.this);
				alert.setTitle(title);
				alert.setDetailedMessage(detail);
				if (image != null) {
					if ("drawable".compareToIgnoreCase(getResources().getResourceTypeName(image)) == 0) {
						alert.setImageResource(image);
					} else {
						alert.setGifResource(image);
					}
				}
				alert.setPositiveButton(postiveButton, positiveButtonClickListener);
				alert.setNegativeButton(negativeButton, negativeButtonClickListener);
				alert.setCancelable(false);
				alert.show();
			}
		});
	}
    
    private void finishActivity() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// Log event
				JSONObject eventProperties = new JSONObject();
				try {
					eventProperties.put(EventLogger.EVENT_PROP_TUTORIAL_STEP, mCurrentStep.name());
					eventProperties.put(EventLogger.EVENT_PROP_TUTORIAL_CHOOSE_WORD_PRACTICE_TIMES, mChooseWordPracticeTimes);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				EventLogger.endAction(EventLogger.EVENT_NAME_TUTORIAL, eventProperties);
				
				finish();
			}
		});
	}
    
    private boolean isWaiting() {
    	if (mProgressBar.getVisibility() == View.VISIBLE) {
    		return true;
    	} else {
    		return false;
    	}
    }

	private static class MessageBoxFragment extends Fragment {
		
		private FrameLayout mContentContainer;
		private TextView mPositiveButton, mNegativeButton;
		
		private View mContentView;
		private String mPositiveButtonText = null, mNegativeButtonText = null;
		private OnClickListener mPositiveButtonClickListener = null, mNegativeButtonClickListener = null;
		private int mButtonVisibility = View.VISIBLE;
		
	    @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    	View rootView = inflater.inflate(R.layout.airsig_fragment_training_message_box, container, false);
	    	
	    	mContentContainer = (FrameLayout) rootView.findViewById(R.id.viewContent);
	    	setContentView(mContentView);
	    	mPositiveButton = (TextView) rootView.findViewById(R.id.button_positive);
	    	privateSetPositiveButton(mPositiveButtonText, mPositiveButtonClickListener);
	    	mNegativeButton = (TextView) rootView.findViewById(R.id.button_negative);
	    	privateSetNegativeButton(mNegativeButtonText, mNegativeButtonClickListener);
	    	
	    	return rootView;
	    }
	    
	    public void onExpandStart() {
	    	if (mContentView != null) {
	    		mContentView.setVisibility(View.INVISIBLE);
	    	}
	    	mButtonVisibility = View.INVISIBLE;
	    	privateSetPositiveButton(mPositiveButtonText, mPositiveButtonClickListener);
	    	privateSetNegativeButton(mNegativeButtonText, mNegativeButtonClickListener);
	    }
	    
	    public void onExpandEnd() {
	    	if (mContentView != null) {
	    		mContentView.setVisibility(View.VISIBLE);
	    	}
	    	mButtonVisibility = View.VISIBLE;
	    	privateSetPositiveButton(mPositiveButtonText, mPositiveButtonClickListener);
	    	privateSetNegativeButton(mNegativeButtonText, mNegativeButtonClickListener);
	    }
	    
	    public void onCollapseStart() {
	    	if (mContentView != null) {
	    		mContentView.setVisibility(View.INVISIBLE);
	    	}
	    	mButtonVisibility = View.INVISIBLE;
	    	privateSetPositiveButton(mPositiveButtonText, mPositiveButtonClickListener);
	    	privateSetNegativeButton(mNegativeButtonText, mNegativeButtonClickListener);
	    }
	    
	    public void onCollapseEnd() {
	    	// do nothing
	    }
	    
	    public void setContentView(View view) {
	    	mContentView = view;
	    	if (mContentContainer != null) {
	    		mContentContainer.removeAllViews();
	    		if (mContentView != null) {
	    			mContentContainer.addView(view);
	    		}
	    	}
	    }
	    
	    public void setPositiveButton(String title, final OnClickListener clickListener) {
	    	mPositiveButtonText = title;
    		mPositiveButtonClickListener = clickListener;
    		privateSetPositiveButton(title, clickListener);
	    }
	    
	    private void privateSetPositiveButton(String title, final OnClickListener clickListener) {
	    	if (mPositiveButton == null) {
	    		return;
	    	}
	    	
	    	if (title == null || title.length() == 0) {
	    		mPositiveButton.setVisibility(View.GONE);
	    		return;
	    	}
			mPositiveButton.setText(title);
			mPositiveButton.setVisibility(mButtonVisibility);
			mPositiveButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (null != clickListener) {
						clickListener.onClick(mPositiveButton);
					}
				}
			});
		}
	    
	    public void setNegativeButton(String title, final OnClickListener clickListener) {
	    	mNegativeButtonText = title;
    		mNegativeButtonClickListener = clickListener;
    		privateSetNegativeButton(title, clickListener);
	    }
		
	    private void privateSetNegativeButton(String title, final OnClickListener clickListener) {
	    	if (mNegativeButton == null) {
	    		return;
	    	}
	    	
	    	if (title == null || title.length() == 0) {
	    		mNegativeButton.setVisibility(View.GONE);
	    		return;
	    	}
			mNegativeButton.setText(title);
			mNegativeButton.setVisibility(mButtonVisibility);
			mNegativeButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (null != clickListener) {
						clickListener.onClick(mNegativeButton);
					}
				}
			});
		}
	}
}
