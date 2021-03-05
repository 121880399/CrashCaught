package org.zzy.lib.crashcaught;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import org.zzy.lib.crashcaught.compat.ActivityKillerV15_V20;
import org.zzy.lib.crashcaught.compat.ActivityKillerV21_V23;
import org.zzy.lib.crashcaught.compat.ActivityKillerV24_V25;
import org.zzy.lib.crashcaught.compat.ActivityKillerV26;
import org.zzy.lib.crashcaught.compat.ActivityKillerV28;
import org.zzy.lib.crashcaught.compat.IActivityKiller;

import java.lang.reflect.Field;

import me.weishu.reflection.Reflection;


/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2021/3/4 9:08
 * 描    述：Crash捕获
 * 修订历史：
 * ================================================
 */
public class CrashCaught {
    private static final String TAG = CrashCaught.class.getSimpleName();

    /**
     * 是否已经初始化过了
     */
    private static boolean isInited = false;
    /**
     * 是否进入安全模式
     */
    private static boolean isEnterSafeMode = false;

    /**
     * 用来杀死Activity
     */
    private static IActivityKiller mActivityKiller;

    private static ICrashListener mCrashListener;


    public static void init(Context context, ICrashListener listener) {
        if (isInited) {
            return;
        }
        try {
            //解除 android P 反射限制
            Reflection.unseal(context);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        isInited = true;
        mCrashListener = listener;
        initKiller();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                if (mCrashListener == null) {
                    return;
                }
                //判断是否在主线程
                if (t == Looper.getMainLooper().getThread()) {
                    Log.d(TAG, "main thread crash");
                    mCrashListener.onMainThreadCrashHappened(t, e);
                    isChoreographerException(e);
                    //进入安全模式
                    enterSafeMode();
                } else {
                    mCrashListener.onWorkerThreadCrashHappened(t, e);
                }
            }
        });
    }

    private static void enterSafeMode() {
        Log.d(TAG, "enterSafeMode");
        isEnterSafeMode = true;
        if (mCrashListener != null) {
            mCrashListener.onEnterSafeMode();
        }
        while (true) {
            try {
                Log.d(TAG, "start loop");
                Looper.loop();
            } catch (Throwable e) {
                Log.d(TAG, "safe mode crash");
                isChoreographerException(e);
                if (mCrashListener != null) {
                    mCrashListener.onSafeModeCrashHappened(e);
                }
            }
        }
    }

    private static void isChoreographerException(Throwable e) {
        Log.d(TAG, "isChoreographerException(e);");
        if (e == null || mCrashListener == null) {
            return;
        }
        StackTraceElement[] elements = e.getStackTrace();
        if (elements == null) {
            return;
        }

        for (int i = elements.length - 1; i > -1; i--) {
            if (elements.length - i > 20) {
                return;
            }
            StackTraceElement element = elements[i];
            if ("android.view.Choreographer".equals(element.getClassName())
                    && "Choreographer.java".equals(element.getFileName())
                    && "doFrame".equals(element.getMethodName())) {
                mCrashListener.onBlackScreen(e);
                return;
            }
        }
    }


    //默认的CODE码
    public static final int LAUNCH_ACTIVITY = 100;
    public static final int PAUSE_ACTIVITY = 101;
    public static final int PAUSE_ACTIVITY_FINISHING = 102;
    public static final int STOP_ACTIVITY_HIDE = 104;
    public static final int RESUME_ACTIVITY = 107;
    public static final int DESTROY_ACTIVITY = 109;
    //android 28以后生命周期的改变使用这个code
    public static final int EXECUTE_TRANSACTION = 159;


    private static void hookmH() throws Exception {
        //考虑到启动速度问题，不进行反射了
//        if (Build.VERSION.SDK_INT >= 28) {
//            Class<?> HClass = Class.forName("android.app.ActivityThread$H");
//            Field executeTransactionField = HClass.getDeclaredField("EXECUTE_TRANSACTION");
//            executeTransactionField.setAccessible(true);
//            EXECUTE_TRANSACTION = executeTransactionField.getInt(HClass);
//        } else {
//            //通过反射二次确认CODE码，避免出现某些安卓版本CODE不一致的情况
//            //LAUNCH_ACTIVITY
//            Class<?> HClass = Class.forName("android.app.ActivityThread$H");
//            Field launchActivityField = HClass.getDeclaredField("LAUNCH_ACTIVITY");
//            launchActivityField.setAccessible(true);
//            LAUNCH_ACTIVITY = launchActivityField.getInt(HClass);
//
//            //PAUSE_ACTIVITY
//            Field pauseActivityField = HClass.getDeclaredField("PAUSE_ACTIVITY");
//            pauseActivityField.setAccessible(true);
//            PAUSE_ACTIVITY = pauseActivityField.getInt(HClass);
//
//            //PAUSE_ACTIVITY_FINISHING
//            Field pauseActivityFinishingField = HClass.getDeclaredField("PAUSE_ACTIVITY_FINISHING");
//            pauseActivityFinishingField.setAccessible(true);
//            PAUSE_ACTIVITY_FINISHING = pauseActivityFinishingField.getInt(HClass);
//
//            //STOP_ACTIVITY_HIDE
//            Field stopActivityHideField = HClass.getDeclaredField("STOP_ACTIVITY_HIDE");
//            stopActivityHideField.setAccessible(true);
//            STOP_ACTIVITY_HIDE = stopActivityHideField.getInt(HClass);
//
//            //RESUME_ACTIVITY
//            Field resumeActivityField = HClass.getDeclaredField("RESUME_ACTIVITY");
//            resumeActivityField.setAccessible(true);
//            RESUME_ACTIVITY = resumeActivityField.getInt(HClass);
//
//            //DESTROY_ACTIVITY
//            Field destroyActivityField = HClass.getDeclaredField("DESTROY_ACTIVITY");
//            destroyActivityField.setAccessible(true);
//            DESTROY_ACTIVITY = destroyActivityField.getInt(HClass);
//        }
        //得到ActivityThread的实例
        Class<?> activityThreadClazz = Class.forName("android.app.ActivityThread");
        Field sCurrentActivityThread = activityThreadClazz.getDeclaredField("sCurrentActivityThread");
        sCurrentActivityThread.setAccessible(true);
        Object activityThread = sCurrentActivityThread.get(activityThreadClazz);

        //得到mH
        Field mHField = activityThreadClazz.getDeclaredField("mH");
        mHField.setAccessible(true);
        Handler mHHandler = (Handler) mHField.get(activityThread);
        //得到mCallback
        Field callbackField = Handler.class.getDeclaredField("mCallback");
        callbackField.setAccessible(true);
        //给mCallback赋值
        callbackField.set(mHHandler, new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                //肯定在主线程执行
                if (Build.VERSION.SDK_INT >= 28) {
                    if (msg.what == EXECUTE_TRANSACTION) {
                        try {
                            mHHandler.handleMessage(msg);
                        } catch (Throwable throwable) {
                            mActivityKiller.finishLaunchActivity(msg);
                            notifyException(throwable);
                        }
                        return true;
                    }
                    return false;
                }
                switch (msg.what) {
                    case LAUNCH_ACTIVITY:// startActivity--> activity.attach  activity.onCreate  r.activity!=null  activity
                        // .onStart  activity.onResume
                        try {
                            mHHandler.handleMessage(msg);
                        } catch (Throwable throwable) {
                            mActivityKiller.finishLaunchActivity(msg);
                            notifyException(throwable);
                        }
                        return true;
                    case RESUME_ACTIVITY://回到activity onRestart onStart onResume
                        try {
                            mHHandler.handleMessage(msg);
                        } catch (Throwable throwable) {
                            mActivityKiller.finishResumeActivity(msg);
                            notifyException(throwable);
                        }
                        return true;
                    case PAUSE_ACTIVITY_FINISHING://按返回键 onPause
                    case PAUSE_ACTIVITY://开启新页面时，旧页面执行 activity.onPause
                        try {
                            mHHandler.handleMessage(msg);
                        } catch (Throwable throwable) {
                            mActivityKiller.finishPauseActivity(msg);
                            notifyException(throwable);
                        }
                        return true;
                    case STOP_ACTIVITY_HIDE://开启新页面时，旧页面执行 activity.onStop
                        try {
                            mHHandler.handleMessage(msg);
                        } catch (Throwable throwable) {
                            mActivityKiller.finishStopActivity(msg);
                            notifyException(throwable);
                        }
                        return true;
                    case DESTROY_ACTIVITY:// 关闭activity onStop  onDestroy
                        try {
                            mHHandler.handleMessage(msg);
                        } catch (Throwable throwable) {
                            notifyException(throwable);
                        }
                        return true;
                }
                return false;
            }
        });
    }


    private static void initKiller() {
        //各版本android的ActivityManager获取方式，finishActivity的参数，token(binder对象)的获取不一样
        if (Build.VERSION.SDK_INT >= 28) {
            mActivityKiller = new ActivityKillerV28();
        } else if (Build.VERSION.SDK_INT >= 26) {
            mActivityKiller = new ActivityKillerV26();
        } else if (Build.VERSION.SDK_INT == 25 || Build.VERSION.SDK_INT == 24) {
            mActivityKiller = new ActivityKillerV24_V25();
        } else if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT <= 23) {
            mActivityKiller = new ActivityKillerV21_V23();
        } else if (Build.VERSION.SDK_INT >= 15 && Build.VERSION.SDK_INT <= 20) {
            mActivityKiller = new ActivityKillerV15_V20();
        } else if (Build.VERSION.SDK_INT < 15) {
            mActivityKiller = new ActivityKillerV15_V20();
        }

        try {
            hookmH();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    /**
     * 用于hook时出错回调，一定在主线程执行
     * 作者: ZhouZhengyi
     * 创建时间: 2021/3/4 14:35
     */
    private static void notifyException(Throwable e) {
        if (mCrashListener == null) {
            return;
        }
        if (isEnterSafeMode) {
            mCrashListener.onSafeModeCrashHappened(e);
        } else {
            mCrashListener.onMainThreadCrashHappened(Looper.getMainLooper().getThread(), e);
            enterSafeMode();
        }
    }
}
