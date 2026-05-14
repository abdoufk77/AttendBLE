package com.example.attendble;

import android.app.Application;

import com.example.attendble.data.ServiceLocator;

/** Application : initialise le {@link ServiceLocator} avec le {@code Context} (requis pour SQLite). */
public class AttendBleApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ServiceLocator.init(this);
    }
}
