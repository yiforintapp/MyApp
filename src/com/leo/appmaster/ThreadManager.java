package com.leo.appmaster;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.leo.appmaster.utils.LeoLog;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.widget.Toast;

/**
 * 线程池统一管理类，包含如下
 *  异步线程4个
 *  io操作线程1个
 *  网络线程2个
 *  公共Timer
 *  线性线程池
 *
 * @author Jasper
 *
 */
public class ThreadManager {
    private static final String TAG = "ThreadManager";

    private static final int TIME_UI_ALARM = 300;
    private static final int TIME_ASYNC_ALARM = 5 * 1000;

    /**
     * 网络线程池个数
     * 一些不适用于Volley类的接口请求
     */
    private static final int NETWORK_CORE_SIZE = 2;
    /**
     * 异步线程池核心线程个数
     */
    private static final int ASYNCTASK_CORE_SIZE = 4;

    private static final int MAX_ASYNC_SIZE = 20;
    private static final int MAX_NETWORK_SIZE = 4;

    /**
     * AsyncTask的默认Executor，负责长时间网络请求，2个线程
     */
    private static ScheduledThreadPoolExecutor sNetworkExecutor;

    /**
     * 异步线程池，4个线程，执行一些耗时不能在UI线程执行的操作
     */
    private static ScheduledThreadPoolExecutor sAsyncExecutor;

    /**
     * 文件相关操作线程
     * 如文件读写、图片解码等等
     */
    private static Handler sFileThreadHandler;
    private static HandlerThread sFileThread;

    private static Handler sSubThreadHandler;
    private static HandlerThread sSubThread;

    private static Handler sUiHandler;

    private static Timer sTimer;

    private static Thread sUiThread;

    private static final ThreadFactory sNetworkFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "Network #" + mCount.getAndIncrement());
        }

    };

    private static final ThreadFactory sAsyncFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
        }

    };

    static {
        sNetworkExecutor = initThreadExecutor(NETWORK_CORE_SIZE, MAX_NETWORK_SIZE, sNetworkFactory);
        sAsyncExecutor = initThreadExecutor(ASYNCTASK_CORE_SIZE, MAX_ASYNC_SIZE, sAsyncFactory);

        sUiThread = Thread.currentThread();
    }

    private static ScheduledThreadPoolExecutor initThreadExecutor(int coreSize, int maxSize, ThreadFactory factory) {
        ScheduledThreadPoolExecutor result = new ScheduledThreadPoolExecutor(coreSize, factory);
        result.setMaximumPoolSize(maxSize);
        result.setKeepAliveTime(1L, TimeUnit.SECONDS);
        result.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                String clazz = null;
                try {
                    Field field = r.getClass().getDeclaredField("this$0");
                    field.setAccessible(true);
                    clazz = field.getClass().getName();
                } catch (NoSuchFieldException e) {
                    clazz = r.getClass().getName();
                }

                throw new RejectedExecutionException("Task " + clazz +
                        " rejected from " +
                        executor.toString());
            }
        });

        return result;
    }

    public static Handler getFileThreadHandler() {
        if (sFileThreadHandler == null) {
            sFileThread = new HandlerThread("file_io");
            sFileThread.start();

            sFileThreadHandler = new Handler(sFileThread.getLooper());
        }
        return sFileThreadHandler;
    }

    public static Handler getSubThreadHandler() {
        if (sSubThreadHandler == null) {
            sSubThread = new HandlerThread("sub_thread");
            sSubThread.start();

            sSubThreadHandler = new Handler(sSubThread.getLooper());
        }

        return sSubThreadHandler;
    }

    public static Handler getUiThreadHandler() {
        if (sUiHandler == null) {
            sUiHandler = new Handler(Looper.getMainLooper());
        }
        return sUiHandler;
    }

    public static ScheduledExecutorService getAsyncExecutor() {
        return sAsyncExecutor;
    }

    public static ScheduledExecutorService getNetworkExecutor() {
        return sNetworkExecutor;
    }

    /**
     *  一些比较耗时且不能在ui线程中执行的操作, 不占用线程池资源
     * @param runnable
     */
    public static void executeOnSubThread(Runnable runnable) {
        if (AppMasterConfig.LOGGABLE) {
            getSubThreadHandler().post(new WrapRunnable(runnable));
        } else {
            getSubThreadHandler().post(runnable);
        }
    }

    /**
     * 执行文件相关操作<br>
     *
     * @param runnable
     */
    public static void executeOnFileThread(Runnable runnable) {
        if (AppMasterConfig.LOGGABLE) {
            getFileThreadHandler().post(new WrapRunnable(runnable));
        } else {
            getFileThreadHandler().post(runnable);
        }
    }

    /**
     * 在异步线程中执行<br>
     * 一些比较耗时且不能在ui线程中执行的操作 <br>
     * <b>禁止执行网络请求，执行网络请求请使用executeOnNetworkThread</b>
     * @param runnable
     */
    public static void executeOnAsyncThread(Runnable runnable) {
        if (AppMasterConfig.LOGGABLE) {
            sAsyncExecutor.execute(new WrapRunnable(runnable));
        } else {
            sAsyncExecutor.execute(runnable);
        }
    }

    /**
     * 在异步线程中执行<br>
     * 一些比较耗时且不能在ui线程中执行的操作 <br>
     * <b>禁止执行网络请求，执行网络请求请使用executeOnNetworkThread</b>
     * @param runnable
     * @param delayMs
     */
    public static void executeOnAsyncThreadDelay(Runnable runnable, long delayMs) {
        if (AppMasterConfig.LOGGABLE) {
            runnable = new WrapRunnable(runnable);
        }
        sAsyncExecutor.schedule(runnable, delayMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 执行网络相关
     * @param runnable
     */
    public static void executeOnNetworkThread(Runnable runnable) {
        if (AppMasterConfig.LOGGABLE) {
            sNetworkExecutor.execute(new WrapRunnable(runnable));
        } else {
            sNetworkExecutor.execute(runnable);
        }
    }

    /**
     * 在ui线程中执行
     * @param runnable
     */
    public static void executeOnUiThread(Runnable runnable) {
        if (AppMasterConfig.LOGGABLE) {
            getUiThreadHandler().post(new WrapRunnable(runnable));
        } else {
            getUiThreadHandler().post(runnable);
        }
    }

    /**
     * 返回线性Executor，不会新建线程池，会受到现有线程池线程个数限制
     * @return
     */
    public static Executor newSerialExecutor() {
        return new SerialExecutor();
    }

    public static boolean isUiThread() {
        return Thread.currentThread() == sUiThread;
    }

    /**
     * 获取全局公用timer
     * @return
     */
    public static Timer getTimer() {
        if (sTimer == null) {
            sTimer = new Timer() {

                @Override
                public void schedule(TimerTask task, Date when) {
                    try {
                        super.schedule(task, when);
                    } catch (Exception e) {
                        LeoLog.e(TAG, "schedule date ex.", e);
                    }
                }

                @Override
                public void schedule(TimerTask task, long delay) {
                    try {
                        super.schedule(task, delay);
                    } catch (Exception e) {
                        LeoLog.e(TAG, "schedule delay ex.", e);
                    }
                }

                @Override
                public void schedule(TimerTask task, long delay, long period) {
                    try {
                        super.schedule(task, delay, period);
                    } catch (Exception e) {
                        LeoLog.e(TAG, "schedule delay and period ex.", e);
                    }
                }

                @Override
                public void schedule(TimerTask task, Date when, long period) {
                    try {
                        super.schedule(task, when, period);
                    } catch (Exception e) {
                        LeoLog.e(TAG, "schedule date and period ex.", e);
                    }
                }

                @Override
                public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
                    try {
                        super.scheduleAtFixedRate(task, delay, period);
                    } catch (Exception e) {
                        LeoLog.e(TAG, "scheduleAtFixedRate delay and period ex.", e);
                    }
                }

                @Override
                public void scheduleAtFixedRate(TimerTask task, Date when, long period) {
                    try {
                        super.scheduleAtFixedRate(task, when, period);
                    } catch (Exception e) {
                        LeoLog.e(TAG, "scheduleAtFixedRate date and period ex.", e);
                    }
                }

                @Override
                public void cancel() {
                    if (AppMasterConfig.LOGGABLE) {
                        throw new RuntimeException("Global timer cannot be cancel.");
                    }
                    // 全局timer不能cancel
                }

            };
        }

        return sTimer;
    }

    private static class SerialExecutor implements Executor {
        final Queue<Runnable> mTasks = new LinkedList<Runnable>();
        Runnable mActive;

        @Override
        public synchronized void execute(final Runnable r) {
            mTasks.offer(new Runnable() {
                public void run() {
                    try {
                        r.run();
                    } finally {
                        scheduleNext();
                    }
                }
            });
            if (mActive == null) {
                scheduleNext();
            }
        }

        protected synchronized void scheduleNext() {
            if ((mActive = mTasks.poll()) != null) {
                sAsyncExecutor.execute(mActive);
            }
        }

    }

    private static class WrapRunnable implements Runnable {
        private Runnable mTarget;

        public WrapRunnable(Runnable runnable) {
            mTarget = runnable;
        }

        @Override
        public void run() {
            if (mTarget != null) {
                long start = SystemClock.elapsedRealtime();
                mTarget.run();

                long cost = SystemClock.elapsedRealtime() - start;
                LeoLog.i(TAG, getClassTag() + " cost: " + cost);
                if (isUiThread()) {
                    if (cost > TIME_UI_ALARM) {
                        String msg = getClassTag() + "耗时超过 " + TIME_UI_ALARM + " ms, 需要优化";
                        toast(msg);
                    }
                } else {
                    if (cost > TIME_ASYNC_ALARM) {
                        String msg = getClassTag() + "耗时超过 " + TIME_ASYNC_ALARM + " ms, 请检查代码逻辑";
                        toast(msg);
                    }
                }
            }
        }

        private String getClassTag() {
            String clazz = null;
            try {
                Field field = mTarget.getClass().getDeclaredField("this$0");
                field.setAccessible(true);
                clazz = field.get(mTarget).getClass().getName();
            } catch (Exception e) {
                clazz = mTarget.getClass().getName();
            }

            return clazz;
        }

        private void toast(final String msg) {
            executeOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Context context = AppMasterApplication.getInstance();
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            });
        }

    }

}
