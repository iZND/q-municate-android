package com.example.q_municate_chat_service.repository;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.quickblox.core.helper.CollectionsUtil;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class BaseRepoImpl<T, Id> implements BaseRepo<T, Id>{

    protected final MediatorLiveData<List<T>> result = new MediatorLiveData<>();

    protected Executor dbExecutor = Executors.newSingleThreadExecutor();

    @MainThread
    protected boolean shouldFetch(@Nullable List<T> data){
        return CollectionsUtil.isEmpty(data);
    }

    @NonNull
    @MainThread
    protected LiveData<List<T>> createApiData() {
        return new LiveData<List<T>>() {
            @Override
            protected void onActive() {
                performApiReuqest();
            };
        };
    };

    protected abstract void performApiReuqest();
}
