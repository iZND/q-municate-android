package com.example.q_municate_chat_service;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.q_municate_chat_service.dao.QBChatDialogDao;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.CollectionsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class QBChatDilogRepositoryImpl implements QBChatDilogRepository {

    public static final String TAG = QBChatDilogRepositoryImpl.class.getSimpleName();

    QBChatDialogDao chatDialogDao;

    private final MediatorLiveData<List<QBChatDialog>> result = new MediatorLiveData<>();

    Executor dbExecutor;

    public QBChatDilogRepositoryImpl(QBChatDialogDao chatDialogDao){
        this.chatDialogDao = chatDialogDao;
        dbExecutor = Executors.newSingleThreadExecutor();
    }


    @Override
    public void create(QBChatDialog dialog) {
        //
    }

    @Override
    public LiveData<List<QBChatDialog>> loadAll() {
        Log.i(TAG, "loadAll");
        final LiveData<List<QBChatDialog>> dbSource = chatDialogDao.getAll();
        result.addSource(dbSource, new Observer<List<QBChatDialog>>() {
            @Override
            public void onChanged(@Nullable List<QBChatDialog> data) {
                Log.i(TAG, "onChanged from db source");


                if (shouldFetch(data)) {
                    result.removeSource(dbSource);
                    Log.i(TAG, "onChanged from db source :shouldFetch");
                    fetchFromNetwork(dbSource);
                } else {
                    result.setValue(data);
                }
            }
        });
       return result;
    }

    private void fetchFromNetwork(LiveData<List<QBChatDialog>> dbSource) {

        final LiveData<List<QBChatDialog>> apiSource = createApiData();
        result.addSource(apiSource, new Observer<List<QBChatDialog>>() {
            @Override
            public void onChanged(final @Nullable List<QBChatDialog> qbChatDialogs) {
                Log.i(TAG, "onChanged from api source");
                if (!CollectionsUtil.isEmpty(qbChatDialogs)) {
                    dbExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            chatDialogDao.insertAll(qbChatDialogs);
                        }
                    });
                }
                result.setValue(qbChatDialogs);
            }
        });
    }

    @Override
    public LiveData<QBChatDialog> loaByd(String id) {
        return null;
    }

    @Override
    public void delete(QBChatDialog dialog) {
        chatDialogDao.delete(dialog);
    }

    @MainThread
    protected boolean shouldFetch(@Nullable List<QBChatDialog> data){
        return CollectionsUtil.isEmpty(data);
    }

    @NonNull
    @MainThread
    protected LiveData<List<QBChatDialog>> createApiData() {
        return new LiveData<List<QBChatDialog>>() {
            @Override
            protected void onActive() {
                QBRestChatService.getChatDialogs(null, null).
                        performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
                            @Override
                            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                                postValue(qbChatDialogs);
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                postValue(null);
                            }
                        });
            }
        };
    }
}
