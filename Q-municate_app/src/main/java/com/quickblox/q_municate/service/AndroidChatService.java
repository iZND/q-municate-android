package com.quickblox.q_municate.service;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class AndroidChatService extends Service{

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
