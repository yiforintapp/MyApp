package com.zlf.appmaster.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.sql.Timestamp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.Log;

import com.zlf.appmaster.AppMasterApplication;
import com.zlf.appmaster.AppMasterConfig;

/**
 * Custom Log class, add catch exception and crash support
 */

public class LeoLog {
	public static final String TAG = "LEO AppMaster";

	public static final String LOG_DIR = "appmaster";
	public static final String LOG_NAME = "log.txt";

	public static final String CRASH_TAG = "Crash";

	/**
	 * Define constants of log level
	 */
	public static final int VERBOSE = android.util.Log.VERBOSE;
	public static final int DEBUG = android.util.Log.DEBUG;
	public static final int INFO = android.util.Log.INFO;
	public static final int WARN = android.util.Log.WARN;
	public static final int ERROR = android.util.Log.ERROR;

	/**
	 * Save log to file flag.
	 */
//	public static boolean SAVE_TO_FILE = false;

	/**
	 * Default log level,the log above default will be logged.
	 */
	public static int DEFAULT_LOG_LEVEL = VERBOSE;

	/**
	 * Is the {@link UncaughtExceptionHandler} enabled ?
	 */
	public static boolean EXCEPTION_HANDLER_ENABLE = true;

	/**
	 * set batch size of write LogEntry to file.
	 */
//	public static final int BATCH_SIZE = 20;


//	private static BlockingQueue<LogEntry> sLogEntryQueue = new LinkedBlockingQueue<LogEntry>();

	/**
	 * Flag of whether LogEntryWriterThread should exit.
	 */
//	private static boolean sWriterThreadExit = false;
//
//	private static boolean sFlushNow = false;

	private static File sLogFile;

	/**
	 * Use static StringBuilder instance to avoid allocating object frequently
	 */
//	private static final StringBuilder sStringBuilder = new StringBuilder();

	/**
	 * Use static Timestamp instance to avoid allocating object frequently
	 */
	private static final Timestamp sTimestamp = new Timestamp(
			System.currentTimeMillis());

	static {
//		if (AppMasterConfig.LOGGABLE && SAVE_TO_FILE) {
//			new LogEntryWriterThread().start();
//		}
		if (AppMasterConfig.LOGGABLE && EXCEPTION_HANDLER_ENABLE) {
			collectApplicationCrash();
		}
	}

	public static void init(Context aContext) {

	}

	public static void v(String aTag, String aMsg) {
		log(VERBOSE, aTag, aMsg);
	}

	public static void v(String aTag, String aMsg, Throwable aThrowable) {
		log(VERBOSE, aTag, aMsg, aThrowable);
	}

	public static void d(String aTag, String aMsg) {
		log(DEBUG, aTag, aMsg);
	}

	public static void d(String aTag, String aMsg, Throwable aThrowable) {
		log(DEBUG, aTag, aMsg, aThrowable);
	}

	public static void i(String aTag, String aMsg) {
		log(INFO, aTag, aMsg);
	}

	public static void i(String aTag, String aMsg, Throwable aThrowable) {
		log(INFO, aTag, aMsg, aThrowable);
	}

	public static void w(String aTag, String aMsg) {
		log(WARN, aTag, aMsg);
	}

	public static void w(String aTag, String aMsg, Throwable aThrowable) {
		log(WARN, aTag, aMsg, aThrowable);
	}

	public static void e(String aTag, String aMsg) {
		log(ERROR, aTag, aMsg);
	}

	public static void e(String aTag, String aMsg, Throwable aThrowable) {
		log(ERROR, aTag, aMsg, aThrowable);
	}

	public static void log(int aLogLevel, String aTag, String aMessage) {
		log(aLogLevel, aTag, aMessage, null);
	}

	/**
	 * log Send a logLevel log message and log the exception, then collect the
	 * log entry.
	 * 
	 * @param logLevel
	 *            Used to identify log level/
	 * @param tag
	 *            Used to identify the source of a log message. It usually
	 *            identifies the class or activity where the log call occurs.
	 * @param msg
	 *            The message you would like logged.
	 * @param tr
	 *            An exception to log
	 */
	public static void log(int aLogLevel, String aTag, String aMessage,
			Throwable aThrowable) throws ArrayIndexOutOfBoundsException {
		if (isLoggable(aLogLevel)) {
			switch (aLogLevel) {
			case VERBOSE:
				Log.v(TAG, aTag + ": " + aMessage, aThrowable);
				break;
			case DEBUG:
				Log.d(TAG, aTag + ": " + aMessage, aThrowable);
				break;
			case INFO:
				Log.i(TAG, aTag + ": " + aMessage, aThrowable);
				break;
			case WARN:
				Log.w(TAG, aTag + ": " + aMessage, aThrowable);
				break;
			case ERROR:
				Log.e(TAG, aTag + ": " + aMessage, aThrowable);
				break;
			default:
				Log.v(TAG, aTag + ": " + aMessage, aThrowable);
			}

//			sStringBuilder.setLength(0);
//			if (aMessage != null) {
//				sStringBuilder.append(aMessage);
//			}
//
//			if (aThrowable != null) {
//				sStringBuilder.append("\t").append(
//						Log.getStackTraceString(aThrowable));
//			}

			/*
			 * if (SAVE_TO_FILE) { LogEntry entry = new LogEntry();
			 * entry.logLevel = aLogLevel; entry.tag = aTag; entry.msg =
			 * sStringBuilder.toString(); collectLogEntry(entry); }
			 */
		}
	}

	public static class LogEntry {
		public int logLevel;
		public String tag;
		public String msg;
	}

	/**
	 * call when enter the method body that you want to debug with only one line
	 */
	public static void method() {
		StackTraceElement stack[] = (new Throwable()).getStackTrace();
		if (null == stack || 2 > stack.length) {
			return;
		}

		StackTraceElement s = stack[1];
		if (null != s) {
			String className = s.getClassName();
			String methodName = s.getMethodName();
			d(className, "+++++" + methodName);
		}
	}

	/**
	 * call when enter the method body that you want to debug.
	 */
	public static void enter() {
		StackTraceElement stack[] = (new Throwable()).getStackTrace();
		if (null == stack || 2 > stack.length) {
			return;
		}

		StackTraceElement s = stack[1];
		if (null != s) {
			String className = s.getClassName();
			String methodName = s.getMethodName();
			d(className, "====>" + methodName);
		}
	}

	/**
	 * call when leave the method body that you want to debug.
	 */
	public static void leave() {
		StackTraceElement stack[] = (new Throwable()).getStackTrace();
		if (null == stack || 2 > stack.length) {
			return;
		}

		StackTraceElement s = stack[1];
		if (null != s) {
			String className = s.getClassName();
			String methodName = s.getMethodName();
			d(className, "<====" + methodName);
		}
	}

//	private static void collectLogEntry(LogEntry entry) {
//		try {
//			sLogEntryQueue.put(entry);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//	}

//	static class LogEntryWriterThread extends Thread {
//		@Override
//		public void run() {
//			while (!sWriterThreadExit) {
//				if (sLogEntryQueue.size() >= BATCH_SIZE) {
//					writeLogEntryToFileByBatch(BATCH_SIZE);
//				} else if (sFlushNow && !sLogEntryQueue.isEmpty()) {
//					writeLogEntryToFileByBatch(sLogEntryQueue.size());
//				}
//			}
//			if (!sLogEntryQueue.isEmpty()) {
//				writeLogEntryToFileByBatch(sLogEntryQueue.size());
//			}
//		}
//
//	}

	public static String getTimestamp() {
		sTimestamp.setTime(System.currentTimeMillis());
		return sTimestamp.toString();
	}

	/**
	 * in order to improve performance, write LogEntry to file by batch
	 */
	private static void writeToFile(String message) {
		if (!isSDCardAvaible())
			return;

		try {
			if (sLogFile == null) {
				File dir = new File(Environment.getExternalStorageDirectory()
						.getAbsolutePath() + File.separator + LOG_DIR);
				if (!dir.exists()) {
					dir.mkdir();
				}

				sLogFile = new File(dir.getAbsolutePath() + File.separator
						+ LOG_NAME);
			}

			if (!sLogFile.exists()) {
				sLogFile.createNewFile();
			}

		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		BufferedWriter buf = null;
		try {
			// use BufferedWriter for performance, true to set append to file
			// flag
			buf = new BufferedWriter(new FileWriter(sLogFile, true));
			buf.append(message);
		} catch (IOException e) {
		} finally {
			try {
				buf.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Checks to see whether or not a log for the specified tag is loggable at
	 * the specified level. The default level of any tag is set as specified in
	 * {@link #setDefaultLogLevel(int)}. This means that any level above and
	 * including that level will be logged regardless of the value of
	 * DEBUG_ENABLE.
	 * 
	 * @param aLevel
	 *            The level to check.
	 * @return Whether or not that this is allowed to be logged.
	 */
	public static boolean isLoggable(int aLevel) {

		if (ERROR == aLevel) {
			return true;
		}

		if (AppMasterConfig.LOGGABLE) {
			if (aLevel >= DEFAULT_LOG_LEVEL) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public static boolean isSDCardAvaible() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState());
	}

	/**
	 * Return the string that each log level stand for.
	 * 
	 * @param aLogLevel
	 * @return the string of logLevel
	 */
	public static String getLogLevel(int aLogLevel) {
		switch (aLogLevel) {
		case VERBOSE:
			return "V";
		case DEBUG:
			return "D";
		case INFO:
			return "I";
		case WARN:
			return "W";
		case ERROR:
			return "E";
		default:
			return "V";
		}
	}

	public static String getAppInfo() {
		Context ct = AppMasterApplication.getInstance();;
		if (ct != null) {
			PackageManager pm = ct.getPackageManager();
			PackageInfo info;
			try {
				info = pm.getPackageInfo(ct.getPackageName(), 0);
				StringBuilder builder = new StringBuilder();
				builder.append(info.packageName)
						.append("\t" + "(versionName:" + info.versionName + ")")
						.append("\t" + "(versionCode:" + info.versionCode + ")");

				return builder.toString();
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			return null;
		} else {
			return null;
		}
	}

	/**
	 * Define an default error handler, collect the error if happens.
	 */
	public static void collectApplicationCrash() {
		final UncaughtExceptionHandler originalHandler = Thread
				.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
			    String appInfo = getAppInfo();
				log(ERROR, CRASH_TAG, appInfo, ex);
				StringBuilder sStringBuilder = new StringBuilder();
                  if (appInfo != null) {
                      sStringBuilder.append(appInfo);
                  }
                  if (ex != null) {
                      sStringBuilder.append("\t").append(
                              Log.getStackTraceString(ex));
                  }
                 writeToFile(sStringBuilder.toString());

				if (originalHandler != null) {
					originalHandler.uncaughtException(thread, ex);
				}
			}
		});
	}

	/**
	 * set sFlushNow tag to true, ensure the LogEntry Queue will be saved to
	 * file completely right now.
	 */
//	public synchronized static void flush() {
//		sFlushNow = true;
//	}

	/**
	 * when the application is destroyed, call this method to ensure the child
	 * thread will exit and the LogEntryQueue will be saved to file completely.
	 * Note that:this method should only be called when you really don't want to
	 * use LogEx to log any more.
	 */
	public static void clear() {
//		sWriterThreadExit = true;
//		sLogEntryQueue.clear();
//		sStringBuilder.setLength(0);
	}
}
