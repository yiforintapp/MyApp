package com.leo.appmaster.airsig.airsigsdk;

import java.util.ArrayList;
import java.util.Map;

import com.airsig.airsigengmulti.ASEngine;
import com.airsig.airsigengmulti.ASEngine.ASAction;
import com.airsig.airsigengmulti.ASEngine.ASError;
import com.airsig.airsigengmulti.ASEngine.OnGetActionResultListener;
import com.leo.appmaster.airsig.airsigutils.ActionIndexConverter;
import com.leo.appmaster.airsig.airsigutils.EventLogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

/**
 * 
 * Graphical user interface of AirSig. Client applications can use ASGui to
 * launch AirSig functions directly.
 *
 */
public class ASGui {
	
	/**
	 * Interface definition for a callback to be invoked when Training Activity closed
	 */
	public interface OnTrainingResultListener {
		/**
		 * Called when the training result returned
		 * 
		 * @param isRetrain true if this time is re-train
		 * @param success true if success trained
		 * @param action the action instance after training
		 */
		public void onResult(boolean isRetrain, boolean success, ASAction action);
	}
	
	/**
	 * Interface definition for a callback to be invoked when Verify Activity closed
	 */
	public interface OnVerifyResultListener {
		/**
		 * Called when the verification result returned
		 * 
		 * @param pass true if the user pass the verification
		 * @param tooManyFails true if the user failed the verification too many times
		 * @param goToBackupSolution true if the user clicked the backup solution button
		 */
		public void onResult(boolean pass, boolean tooManyFails, boolean goToBackupSolution);
	}
	
	final static String LICENSE_KEY = "2s8cdnvtsk28blas2lx0upwpzpgjwxd5tmnto9x6fx202q9oywx9umpaetarbtfjwj2awiw4l10553iy75a439uuj9dr8c9j2vat3t0w9b4zxde9mzm9tfwr3dow45xvhqq8etmvtmlojeyd84w95yprml66787won2jv4oq9e8zd0f6z8z60mpr54hdtufwjixzfqvdpmqxrntym9wxr9snbhjqcyoaqklkz7zgfvnikly7728rzhj1js822cu0otd8mji05e5kd63bfyxav1dhmus4kvq6f9teytqlsa7bbap27b28tk1c2mwttib3ca6redhxm5vtmwcxg8j8x8yz35kpmba9ergdzcodjxb8g21w0f6e0fyxbvg3cjyuqr6ctbxi5zzxz";
	
	private Context mContext;
	private ASSetting mSetting;
	private ActionIndexConverter mActionIndexConverter = null;
	private BroadcastReceiver mTrainingReceiver;
	private BroadcastReceiver mVerifyReceiver;
	
	private static ASGui sharedInstance;
	
	/**
	 * Initialize the singleton ASGui instance.
	 * Must call this method before starting use ASGui.
	 * 
	 * @param context The application context.
	 * @param dbDir The database directory of AirSig engine, if null, the database path will be save to default directory (/data/data/...)
	 * @param setting The configurations for UI and ASEngine 
	 * @param eventLoggerDelegate
	 * 			  The delegate for logging event, please implement the interface to support event logging. 
	 * 			  Set null if you don't want to support event logging.
	 * 
	 * @return The initialized singleton ASGui instance
	 */
	public static synchronized ASGui getSharedInstance(Context context, String dbDir, ASSetting setting, EventLogger.EventLoggerDelegte eventLoggerDelegate) {
		if (null == sharedInstance) {
			sharedInstance = new ASGui(context, dbDir, setting, eventLoggerDelegate);
		}
	    return sharedInstance;
	}
	
	/**
	 * Re-initialize the singleton ASGui instance, the old one will be deleted.
	 * 
	 * @param context The application context.
	 * @param dbDir The database directory of AirSig engine, if null, the database path will be save to default directory (/data/data/...)
	 * @param setting The configurations for UI and ASEngine 
	 * @param eventLoggerDelegate
	 * 			  The delegate for logging event, please implement the interface to support event logging. 
	 * 			  Set null if you don't want to support event logging.
	 * 
	 * @return The re-initialized singleton ASGui instance
	 */
	public static synchronized ASGui updateSharedInstance(Context context, String dbDir, ASSetting setting, EventLogger.EventLoggerDelegte eventLoggerDelegate) {
		sharedInstance = new ASGui(context, dbDir, setting, eventLoggerDelegate);
	    return sharedInstance;
	}
	
	/**
	 * Get the singleton ASGui instance.
	 * Must initialize it first. see {@link ASGui#getSharedInstance(Context, String)} for more information.
	 * 
	 * @return The singleton ASGui instance
	 */
	public static synchronized ASGui getSharedInstance() {
		return sharedInstance;
	}
	
	/**
	 * Constructor of ASGui
	 * 
	 * @param context
	 *            the application context.
	 * @param dbDir
	 *            the database directory of AirSig engine. If null, the database
	 *            path will be save to default directory (/data/data/...)
	 * @param eventLoggerDelegate
	 * 			  The delegate for logging event, please implement the interface to support event logging. 
	 * 			  Set null if you don't want to support event logging.
	 */
	private ASGui(Context context, String dbDir, ASSetting setting, EventLogger.EventLoggerDelegte eventLoggerDelegate) {
		mContext = context;
		mActionIndexConverter = new ActionIndexConverter(mContext);
		if (null == setting) {
			mSetting = new ASSetting();
		} else {
			mSetting = setting;
		}
		
		ASEngine.initSharedInstance(mContext, ASGui.LICENSE_KEY, dbDir, mSetting.engineParameters);
		EventLogger.initialize(mContext, eventLoggerDelegate);
	}
	
	/**
	 * Get the object contains settings
	 * @return The setting object
	 */
	public ASSetting getSetting() {
		return mSetting;
	}

	/**
	 * Check the required sensor (gyroscope, linear accelerometer and gravity sensor) status of current
	 * device
	 * 
	 * @return The required sensors were present or not
	 */
	public boolean isSensorAvailable() {
		return ASEngine.getSharedInstance().isSensorAvailable();
	}

	/**
	 * Check the required air signature is exist or not.
	 * 
	 * @param account
	 *            used to identify different account, it shall be defined by
	 *            client application. if client application has no multiple
	 *            account requirement, just set it as null.
	 * 
	 * @return True, if the air signature is ready for verifying.
	 */
	public boolean isSignatureReady(Map<String, String> account) {
		int actionIndex = mActionIndexConverter.getActionIndex(account);
		return isSignatureReady(actionIndex);
	}
	
	/**
	 * Check the required air signature is exist or not.
	 * 
	 * @param actionIndex The index of the signature
	 * @return True, if the air signature is ready for verifying.
	 */
	public boolean isSignatureReady(int actionIndex) {
		ArrayList<ASAction> actions = ASEngine.getSharedInstance().getAllActions();
		if (null != actions) {
			for (int i = 0; i < actions.size(); i++) {
				if (actions.get(i).actionIndex == actionIndex) {
					if (actions.get(i).strength != ASEngine.ASStrength.ASStrengthNoData) {
						return true;
					} else {
						return false;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Delete current signature.
	 * 
	 * @param account
	 *            used to identify different account, it shall be defined by
	 *            client application. if client application has no multiple
	 *            account requirement, just set it as null.
	 */
	public void deleteSignature(Map<String, String> account) {
		int actionIndex = mActionIndexConverter.getActionIndex(account);
		if (actionIndex >= 0) {
			mActionIndexConverter.deleteQuery(account);
			ASEngine.getSharedInstance().deleteAction(actionIndex, null);
		}
	}
	
	/**
	 * Delete current signature.
	 * 
	 * @param actionIndex The index of the signature
	 */
	public void deleteSignature(int actionIndex) {
		if (actionIndex >= 0) {
			mActionIndexConverter.deleteQuery(actionIndex);
			ASEngine.getSharedInstance().deleteAction(actionIndex, null);
		}
	}

	public void deleteAllSignatures() {
		ASEngine.getSharedInstance().deleteAllActions(null);
	}
	
	/**
	 * Show training activity. Tutorial activity will be prompted at the first
	 * time.
	 * 
	 * @param account
	 *            used to identify different account, it shall be defined by
	 *            client application. if client application has no multiple
	 *            account requirement, just set it as null.
	 * @param listener The listener to receive training result
	 * 
	 */
	public void showTrainingActivity(Map<String, String> account, OnTrainingResultListener listener) {
		int actionIndex = mActionIndexConverter.getActionIndex(account);
		showTrainingActivity(actionIndex, listener);
	}
	
	/**
	 * Show training activity. Tutorial activity will be prompted at the first
	 * time.
	 * 
	 * @param actionIndex The index of the action to be train
	 * @param listener The listener to receive training result
	 */
	public void showTrainingActivity(final int actionIndex, final OnTrainingResultListener listener) {
		if (null != listener) {
			mTrainingReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, final Intent intent) {
					LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mTrainingReceiver);
					mTrainingReceiver = null;
					
					ASEngine.getSharedInstance().getAction(actionIndex, new OnGetActionResultListener() {
						@Override
						public void onResult(ASAction action, ASError error) {
						listener.onResult(intent.getBooleanExtra(TrainingActivity.kIsRetrain, false), 
								intent.getBooleanExtra(TrainingActivity.kTrainingResult, false), 
								action);
						}
					});
				}
			};
			LocalBroadcastManager.getInstance(mContext).registerReceiver(mTrainingReceiver, new IntentFilter(TrainingActivity.DEFAULT_RESULT_EVENT_NAME));
		}
		
		Intent intent = new Intent();
		intent.setClass(mContext, TrainingActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Bundle bundle = new Bundle();
		bundle.putInt(TrainingActivity.kActionIndex, actionIndex);
		intent.putExtras(bundle);

		mContext.startActivity(intent);
	}
	
	/**
	 * Show the verify activity. 
	 * 
	 * @param account 
	 * 				used to identify different account, it shall be defined by
	 *            	client application. if client application has no multiple
	 *            	account requirement, just set it as null.
	 * @param disableBackButton Set to true will make the user MUST pass the verification to continue
	 * @param listener The listener to receive verification result
	 */
	public void showVerifyActivity(Map<String, String> account, boolean disableBackButton, OnVerifyResultListener listener) {
		int actionIndex = mActionIndexConverter.getActionIndex(account);
		showVerifyActivity(actionIndex, disableBackButton, listener);
	}
	
	/**
	 * Show the verify activity. 
	 * 
	 * @param actionIndex The index of the action to verify
	 * @param disableBackButton Set to true will make the user MUST pass the verification to continue
	 * @param listener The listener to receive verification result
	 */

	public void showVerifyActivity(int actionIndex, boolean disableBackButton, final OnVerifyResultListener listener) {
		if (null != listener) {
			mVerifyReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mVerifyReceiver);
					mVerifyReceiver = null;
					listener.onResult(intent.getBooleanExtra(VerifyActivity.kVerifyResult, false), 
							intent.getBooleanExtra(VerifyActivity.kVerifyTooManyFails, false), 
							intent.getBooleanExtra(VerifyActivity.kGoToBackSolution, false));
				} 
			};
			LocalBroadcastManager.getInstance(mContext).registerReceiver(mVerifyReceiver, new IntentFilter(VerifyActivity.DEFAULT_RESULT_EVENT_NAME));
		}
		
		Intent intent = new Intent();
		intent.setClass(mContext, VerifyActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Bundle bundle = new Bundle();
		int [] targetIndexes = new int[1];
		targetIndexes[0] = actionIndex;
		bundle.putIntArray(VerifyActivity.kTargetActionIndexes, targetIndexes);
		bundle.putBoolean(VerifyActivity.kDisableBackupSolutionButton, mSetting.disableBackupSolutionButtonInVerification);
		if (null != mSetting.customBackupSolutionButtonTitle) {
			bundle.putString(VerifyActivity.kBackupSolutionTitle, mSetting.customBackupSolutionButtonTitle);
		}
		bundle.putBoolean(VerifyActivity.kDisableBackButton, disableBackButton);
		intent.putExtras(bundle);
		
		mContext.startActivity(intent);
	}
}
