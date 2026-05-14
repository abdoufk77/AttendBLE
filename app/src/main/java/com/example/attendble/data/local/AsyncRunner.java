package com.example.attendble.data.local;

import android.os.Handler;
import android.os.Looper;

import com.example.attendble.domain.Callback;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Exécute une tâche SQLite sur un thread de fond et délivre le résultat sur le main thread. */
public final class AsyncRunner {

    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private AsyncRunner() {}

    public static <T> void run(Callable<T> task, Callback<T> callback) {
        IO.execute(() -> {
            try {
                T result = task.call();
                MAIN.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                MAIN.post(() -> callback.onError(e));
            }
        });
    }
}
