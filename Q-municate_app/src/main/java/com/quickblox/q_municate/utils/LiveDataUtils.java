package com.quickblox.q_municate.utils;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.util.Log;

public class LiveDataUtils {


    private static final String TAG = LiveDataUtils.class.getSimpleName();

    public static <T> void observeValue(LiveData<T> liveData, Observer<T> observer){
        final Observer<T> innerObserver = new Observer<T>(){

            @Override
            public void onChanged(@Nullable T o) {
                Log.i(TAG, "onChanged value , observer="+observer);
                if (observer != null) {
                    observer.onChanged(o);
                }
                liveData.removeObserver(this);
            }
        };

        liveData.observeForever(innerObserver);
    }

}
