package com.webank.wecrosssdk.rpc.methods;

import com.webank.wecrosssdk.exception.ErrorCode;
import com.webank.wecrosssdk.exception.WeCrossSDKException;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Callback<T> {
    private static Timer timer = new HashedWheelTimer();
    private static final long CALLBACK_TIMEOUT = 30000; // ms
    private Timeout timeoutWorker;
    private AtomicBoolean isFinish = new AtomicBoolean(false);

    public Callback() {
        timeoutWorker =
                timer.newTimeout(
                        timeout -> {
                            if (!isFinish.getAndSet(true)) {
                                timeoutWorker.cancel();
                                onFailed(
                                        new WeCrossSDKException(
                                                ErrorCode.REMOTECALL_ERROR, "Timeout"));
                            }
                        },
                        CALLBACK_TIMEOUT,
                        TimeUnit.MILLISECONDS);
    }

    public abstract void onSuccess(T response);

    public abstract void onFailed(WeCrossSDKException e);

    public void callOnSuccess(T response) {
        if (!isFinish.getAndSet(true)) {

            timeoutWorker.cancel();
            onSuccess(response);
        }
    }

    public void callOnFailed(WeCrossSDKException e) {
        if (!isFinish.getAndSet(true)) {

            timeoutWorker.cancel();
            onFailed(e);
        }
    }
}
