package org.zzy.crashcaught;

import android.app.Application;
import android.util.Log;

import org.zzy.lib.crashcaught.CrashCaught;
import org.zzy.lib.crashcaught.ICrashListener;

import java.sql.Ref;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2021/3/5 9:17
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashCaught.init(this, new ICrashListener() {
            @Override
            public void onMainThreadCrashHappened(Thread thread, Throwable throwable) {
                Log.e("Crash","onMainThreadCrashHappened");
                throwable.printStackTrace();
            }

            @Override
            public void onWorkerThreadCrashHappened(Thread thread, Throwable throwable) {
                Log.e("Crash","onWorkerThreadCrashHappened");
                throwable.printStackTrace();
            }

            @Override
            public void onBlackScreen(Throwable throwable) {
                Log.e("Crash","onBlackScreen");
                throwable.printStackTrace();
            }

            @Override
            public void onEnterSafeMode() {
                Log.e("Crash","onEnterSafeMode");
            }

            @Override
            public void onSafeModeCrashHappened(Throwable throwable) {
                Log.e("Crash","onSafeModeCrashHappened");
                throwable.printStackTrace();
            }
        });
    }
}
