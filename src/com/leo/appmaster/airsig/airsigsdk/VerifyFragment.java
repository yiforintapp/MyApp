package com.leo.appmaster.airsig.airsigsdk;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.airsig.airsigengmulti.ASEngine;
import com.airsig.airsigengmulti.ASEngine.ASAction;
import com.airsig.airsigengmulti.ASEngine.ASError;
import com.airsig.airsigengmulti.ASEngine.OnGetActionResultListener;
import com.leo.appmaster.R;
import com.leo.appmaster.airsig.airsigutils.EventLogger;
import com.leo.appmaster.airsig.airsigutils.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class VerifyFragment extends Fragment {
	
	/**
	 * The result listener interface for verification result.
	 * Must implement this interface when use this VerifyFragment in your activity
	 */
	public interface OnVerificationResultListener {
		/**
		 * The result call back, called after verified user's input
		 * @param result Pass or Failed the verification
		 * @param foreverBlocked The verification is blocked because user failed too many times. He needs to use backup solution to unlock
		 */
        public void onVerified(boolean result, boolean foreverBlocked);
    }
	
	// Settings
	private int [] mTargetActionIndexes = null;
	private OnVerificationResultListener mListener;
	
	// Private data
	private int mVerifyTimes = 0;
	private boolean mVerifyResult = false;
	
	// UI components:
	
	// Thumb
	private ImageView mImageViewThumb;
	private float mThumbPositionX;
	private float mThumbPositionY;
	
	// Verification result
	private View mToucharea;
	private TextView mTouchareaMsg;
	private TextView mResultMsg;
		
	// Others
	private ProgressBar mProgressBar;
	private ToneGenerator mToneGen = null;
	private ToneGenerator mToneGen2 = null; 

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View rootView = inflater.inflate(R.layout.airsig_fragment_verify, container, true);
    	
    	// Initialize UI components:
		// Others
		mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBarWaiting);
				
		// Thumb
		mImageViewThumb = (ImageView) rootView.findViewById(R.id.imageThumb);
		mImageViewThumb.setOnTouchListener(new ImageButton.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return onTouchThumb(v, event);
			}
		});
		setTargetActionIndexes(null);
		
		// Verification result
		mTouchareaMsg = (TextView) rootView.findViewById(R.id.textTouchAreaMessage);
		mResultMsg = (TextView) rootView.findViewById(R.id.textResultMessage);
		mToucharea = rootView.findViewById(R.id.viewTouchArea);
		
    	return rootView;
    }
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnVerificationResultListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " + OnVerificationResultListener.class.getName());
        }
    }
	
	@Override
	public void onResume() {
		super.onResume();
		if (null != ASEngine.getSharedInstance()) {
			ASEngine.getSharedInstance().startSensors();
		}
		
		// set rootview's location & size
		setRootviewSize();
		
		// Log event
		EventLogger.startAction(EventLogger.EVENT_NAME_VERIFY, null, true);
	}
	
	@Override
	public void onPause() {
		if (null != ASEngine.getSharedInstance()) {
			ASEngine.getSharedInstance().stopSensors();
		}
		
		// Log event
		JSONObject eventProperties = new JSONObject();
		try {
			eventProperties.put(EventLogger.EVENT_PROP_VERIFY_SUCCESS, mVerifyResult);
			eventProperties.put(EventLogger.EVENT_PROP_VERIFY_TIMES, mVerifyTimes);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		EventLogger.endAction(EventLogger.EVENT_NAME_VERIFY, eventProperties);
		
		if (null != mToneGen) {
			mToneGen.release();
			mToneGen = null;
		}
		if (null != mToneGen2) {
			mToneGen2.release();
			mToneGen2 = null;
		}
		super.onPause();
	}
	
	private void setRootviewSize() {
		View rootView = getView();
		
		Point size = new Point();
		getActivity().getWindowManager().getDefaultDisplay().getSize(size);
		int y = (int) (getResources().getDimensionPixelSize(R.dimen.airsig_vertical_margin) 
				+ getResources().getDimensionPixelSize(R.dimen.airsig_small_message_box_height) 
				+ (getResources().getDimensionPixelSize(R.dimen.airsig_actionbar_height) - Utils.getActionBarHeight(getActivity()))
				);
		
		rootView.getLayoutParams().height = (int) (size.y - y - Utils.getStatusBarHeight(getActivity().getApplicationContext()) - Utils.getActionBarHeight(getActivity()));
		rootView.setY(y);
		rootView.requestLayout();
	}
	
	public void setTargetActionIndexes(int [] indexes) {
		mTargetActionIndexes = indexes;
		
		// Thumb position
		showThumb(false);
		if (null == mTargetActionIndexes || mTargetActionIndexes.length == 0) {
			showWaiting(true);
			ASEngine.getSharedInstance().getAllActions(new ASEngine.OnGetAllActionsResultListener() {
				@Override
				public void onResult(ArrayList<ASAction> actions, ASError error) {
					showWaiting(false);
					ASAction action = null;
					if (null != actions) {
						for (int i = 0; i < actions.size(); i++) {
							action = actions.get(i);
							if (action.strength != ASEngine.ASStrength.ASStrengthNoData)
								break;
							else
								action = null;
						}
						
						if (null != action) {
							mThumbPositionX = (float) action.thumbPositionX;
							mThumbPositionY = (float) action.thumbPositionY;
							showThumb(true);
							resetResult();
						} else {
							showTrainingFirstMessage();
						}
						
					} else if (null != error) {
						showTrainingFirstMessage();
					}
				}
			});
		} else {
			showWaiting(true);
			ASEngine.getSharedInstance().getAction(mTargetActionIndexes[0], new OnGetActionResultListener() {
				@Override
				public void onResult(ASAction action, ASError error) {
					showWaiting(false);
					if (null != action && action.strength != ASEngine.ASStrength.ASStrengthNoData) {
						mThumbPositionX = (float) action.thumbPositionX;
						mThumbPositionY = (float) action.thumbPositionY;
						showThumb(true);
						resetResult();
					} else {
						showTrainingFirstMessage();
					}
				}
			});
		}
	}
	
	private boolean onTouchThumb(final View v, final MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			pressThumb(true);
			ASEngine.getSharedInstance().startRecordingSensor(null);
			return true;
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			showWaiting(true);
			ASEngine.getSharedInstance().completeRecordSensorToIdentifyAction(mTargetActionIndexes, new ASEngine.OnIdentifySignatureResultListener() {
				@Override
				public void onResult(ASAction action, ASError error) {
					showWaiting(false);
					pressThumb(false);
					mVerifyTimes++;
					mVerifyResult = false;

					if (null != action) {
						mVerifyResult = true;
						showMatch(true, null);
					} else if (null != error) {
						switch(error) {
						case NOT_FOUND:
							if (error.userData.containsKey(ASError.KEY_VERIFICATION_TIMES_LEFT)) {
								int timesLeft = ((Integer)error.userData.get(ASError.KEY_VERIFICATION_TIMES_LEFT)).intValue();
								if (timesLeft == 0) {
									if (error.userData.containsKey(ASError.KEY_VERIFY_BLOCKED_SECONDS)) {
										showMatch(false, String.format(getString(R.string.airsig_verify_too_many_fails_wait), error.userData.get(ASError.KEY_VERIFY_BLOCKED_SECONDS)));
									} else {
										alertTooManyFails();
									}
								} else {
									showMatch(false, String.format(getString(R.string.airsig_verify_not_match_times_left), timesLeft));
								}
							} else {
								showMatch(false, null);
							}
							break;
						case VERIFY_TOO_MANY_FAILED_TRIALS:
							if (error.userData.containsKey(ASError.KEY_VERIFY_BLOCKED_SECONDS)) {
								showMatch(false, String.format(getString(R.string.airsig_verify_too_many_fails_wait), error.userData.get(ASError.KEY_VERIFY_BLOCKED_SECONDS)));
							} else {
								alertTooManyFails();
							}
							break;
						default:
							showMatch(false, null);
							break;
						}
					}
					toneGenerator(mVerifyResult);
				}
			});
			showThumb(false);
			return true;
		}
		return false;
	}
	
	private void pressThumb(final boolean pressed) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (pressed) {
					// Touch Area
					mToucharea.setBackground(getResources().getDrawable(R.drawable.airsig_verify_toucharea_pressed));
					mTouchareaMsg.setVisibility(View.INVISIBLE);
				} else {
					mToucharea.setBackground(getResources().getDrawable(R.drawable.airsig_verify_toucharea));
					mTouchareaMsg.setVisibility(View.VISIBLE);
				}
			}
    	});
    }

	private void showTrainingFirstMessage() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertBuilder alert = new AlertBuilder(getActivity());
				alert.setImageResource(R.drawable.airsig_ic_dialog);
				alert.setTitle(getResources().getString(R.string.airsig_verify_dialog_title));
				alert.setDetailedMessage(getResources().getString(R.string.airsig_verify_dialog_hint));
				alert.setPositiveButton(getResources().getString(android.R.string.ok), null);
				alert.setCancelable(false);
				alert.show();
			}
		});
	}
	
	private void alertTooManyFails() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertBuilder alert = new AlertBuilder(getActivity());
				alert.setImageResource(R.drawable.airsig_ic_dialog);
				alert.setTitle(getResources().getString(R.string.airsig_verify_too_many_fails_alert_title));
				alert.setPositiveButton(getResources().getString(android.R.string.ok), new Button.OnClickListener() {
					@Override
					public void onClick(View v) {
						// call result listener
						mListener.onVerified(false, true);
					}
				});
				alert.setCancelable(false);
				alert.show();
			}
		});
	}
	
	private void toneGenerator(final boolean pass) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (pass) {
					makeABeepSound();
				} else {
					makeABeepSound();
					makeABeepSound();
				}
			}
		});
	}

	private void makeABeepSound() {
    	// a sound generator
    	if (null == mToneGen) {
    		mToneGen = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MAX_VOLUME);
    	}
    	// a no sound generator - for fixing bad first sound in some phone models
		if (null == mToneGen2) { 
			mToneGen2 = new ToneGenerator(AudioManager.STREAM_SYSTEM, ToneGenerator.MIN_VOLUME);
		}
		
		mToneGen2.startTone(ToneGenerator.TONE_PROP_BEEP);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mToneGen.startTone(ToneGenerator.TONE_PROP_BEEP);
    }
	
	private void resetResult() {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showThumb(true);
				mResultMsg.setVisibility(View.INVISIBLE);
			}
		});
	}

	private void showThumb(final boolean show) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (show) {
					if (-1 != mThumbPositionX && -1 != mThumbPositionY) {
						mImageViewThumb.post(new Runnable() {
							@Override
							public void run() {
								mImageViewThumb.setVisibility(View.VISIBLE);
								mImageViewThumb.setX((int) mThumbPositionX - getView().getX() - mImageViewThumb.getWidth()/2);
								mImageViewThumb.setY((int) mThumbPositionY - getView().getY() - mImageViewThumb.getHeight()/2
										- getResources().getDimensionPixelSize(R.dimen.airsig_actionbar_height) - Utils.getStatusBarHeight(getActivity()));
							}
						});
					}
				} else {
					mImageViewThumb.setVisibility(View.INVISIBLE);
				}
			}
		});
	}

	private void showMatch(final boolean match, final String message) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				// show result
				if (null != message && message.length() > 0) {
					mResultMsg.setText(message);
				} else {
					mResultMsg.setText(match ? R.string.airsig_verify_match : R.string.airsig_verify_not_match);
				}
				mResultMsg.setTextColor(getResources().getColor(match ? R.color.airsig_text_bright_blue : R.color.airsig_text_bright_red));
				mResultMsg.setVisibility(View.VISIBLE);
				
				// Log event
				if (match) {
					EventLogger.incrementUserProperty(EventLogger.USER_PROP_AIRSIG_VERIFY_PASS_TIMES, 1);
				}
				
				JSONObject eventProperties = new JSONObject();
				try {
					eventProperties.put(EventLogger.EVENT_PROP_VERIFY_SUCCESS, match);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				EventLogger.logEvent(EventLogger.EVENT_NAME_VERIFY_VERIFIED, eventProperties);
				
				// Callback
				if (match) {
					mListener.onVerified(true, false);
				} else {
					mListener.onVerified(false, false);
				}
				
				// reset the result
				int messageLength = 10 + ((message == null) ? 0 : message.length());
				final double messageShowTime = Math.min(5.0, messageLength * 0.06 + 1.0); // adjust the show time interval according to message length
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep((long)(1000 * messageShowTime));
						} catch (InterruptedException e) {
							e.printStackTrace();
						} finally {
							if (this != null && getActivity() != null) {
								resetResult();
							}
						}
					}
				}).start();
			}
		});
	}

	private void showWaiting(final boolean show) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (show) {
					mProgressBar.setVisibility(View.VISIBLE);
				} else {
					mProgressBar.setVisibility(View.INVISIBLE);
				}
			}
		});
	}
}
