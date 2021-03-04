package org.zzy.lib.crashcaught;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2021/3/4 9:26
 * 描    述：
 * 修订历史：
 * ================================================
 */
public interface ICrashListener {

    /**
     * 当异常发生时回调这个方法,可能只调用一次，因为调用一次以后会进入到安全模式
     * @param thread Crash发生在哪个线程
     * @param throwable 异常情况
     */
     void onMainThreadCrashHappened(Thread thread, Throwable throwable);

    /**
     * 当子线程异常发生时回调这个方法，每次都会调用，因为子线程的异常不会进入安全模式
     * 作者: ZhouZhengyi
     * 创建时间: 2021/3/4 10:05
     */
    void onWorkerThreadCrashHappened(Thread thread, Throwable throwable);

    /**
    * 当有可能出现黑屏的情况下回调该方法
    * 作者: ZhouZhengyi
    * 创建时间: 2021/3/4 9:29
    */
     void onBlackScreen(Throwable throwable);

     /**
     * 当进入安全模式回调该方法
     * 作者: ZhouZhengyi
     * 创建时间: 2021/3/4 10:49
     */
     void onEnterSafeMode();

     /**
     * 安全模式下发生的奔溃
     * 作者: ZhouZhengyi
     * 创建时间: 2021/3/4 10:54
     */
     void onSafeModeCrashHappened(Throwable throwable);

}
