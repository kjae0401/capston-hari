package com.capston.hari;

import android.app.Application;
import android.content.Intent;

public class BaseApplication extends Application {
    static Intent forgroundservices;
    @Override
    public void onCreate() {
        super.onCreate();
        Foreground.init(this);
    }
}