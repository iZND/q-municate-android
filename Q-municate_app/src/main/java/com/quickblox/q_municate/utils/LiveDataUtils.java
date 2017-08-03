package com.quickblox.q_municate.utils;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

public class LiveDataUtils {

    public static <T> void observeValue(LiveData<T> liveData, Observer<T> observer){
        final Observer<T> innerObserver = new Observer<T>(){

            @Override
            public void onChanged(@Nullable T o) {
                observer.onChanged(o);
                liveData.removeObserver(this);
            }
        };

        liveData.observeForever(innerObserver);
    }

}
