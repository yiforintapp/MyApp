package com.leo.appmaster.airsig.airsigsdk;

import com.airsig.airsigengmulti.ASEngine.ASEngineParameters;

/**
 * 
 * Configuration class used to set AirSig parameters.
 *
 */
public class ASSetting {
	
	/**
	 * Settings for ASEngine.
	 * See {@link ASEngineParameters} for the detailed configuration.
	 */
	public ASEngineParameters engineParameters = ASEngineParameters.Default;
	
	// Custom Settings for verify UI
	
	/**
	 * Turn off the backup solution button in the verify activity.
	 * Default is false.
	 */
	public boolean disableBackupSolutionButtonInVerification = false;
	
	/**
	 * Custom the title of the backup solution button.
	 * Set to null or empty string will use default title.
	 */
	public String customBackupSolutionButtonTitle = null;
}
