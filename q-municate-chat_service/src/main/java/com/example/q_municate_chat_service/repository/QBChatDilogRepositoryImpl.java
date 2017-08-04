package com.example.q_municate_chat_service.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.q_municate_chat_service.dao.QBChatDialogDao;
import com.example.q_municate_chat_service.util.RxUtils;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.extensions.RxJavaPerformProcessor;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import rx.Completable;
import rx.Observable;
import rx.functions.Func0;

public class QBChatDilogRepositoryImpl extends BaseRepoImpl<QBChatDialog> implements QBChatDialogRepository{

    public static final String TAG = QBChatDilogRepositoryImpl.class.getSimpleName();

    QBChatDialogDao chatDialogDao;

    public QBChatDilogRepositoryImpl(QBChatDialogDao chatDialogDao) {
        this.chatDialogDao = chatDialogDao;
    }


    @Override
    public Completable create(QBChatDialog dialog) {
        return null;
    }

    @Override
    public LiveData<List<QBChatDialog>> load(int pageNumber, int count) {
        Log.i(TAG, "loadAll");
        final LiveData<List<QBChatDialog>> dbSource = chatDialogDao.getAll();
        result.addSource(dbSource, new Observer<List<QBChatDialog>>() {
            @Override
            public void onChanged(@Nullable List<QBChatDialog> data) {
                Log.i(TAG, "onChanged from db source");
                if (shouldFetch(data)) {
                    result.removeSource(dbSource);
                    fetchFromNetwork(dbSource);
                } else {
                    result.setValue(data);
                }
            }
        });
        return result;
    }

    private void fetchFromNetwork(LiveData<List<QBChatDialog>> dbSource) {
        Log.i(TAG, "fetchFromNetwork");
        final LiveData<List<QBChatDialog>> apiSource = createApiData();
        result.addSource(apiSource, new Observer<List<QBChatDialog>>() {
            @Override
            public void onChanged(final @Nullable List<QBChatDialog> qbChatDialogs) {
                Log.i(TAG, "onChanged from api request");
                if (!CollectionsUtil.isEmpty(qbChatDialogs)) {
                    dbExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "chatDialogDao.insertAll");

                            chatDialogDao.insertAll(qbChatDialogs);
                        }
                    });
                }
                result.setValue(qbChatDialogs);
            }
        });
    }

    @Override
    public Observable<QBChatDialog> loadById(String id, boolean forceLoad) {
        if (!forceLoad) {
            return Observable.defer( () ->Observable.just(chatDialogDao.getById(id)));
        } else {
            return RxUtils.makeObservable(QBRestChatService.getChatDialogById(id));

        }
    }

    @Override
    public Completable delete(QBChatDialog dialog) {
        chatDialogDao.delete(dialog);
        return null;
    }

    @Override
    public Completable update(QBChatDialog event) {
        return null;
    }

    @Override
    protected boolean shouldFetch(@Nullable List<QBChatDialog> data) {
        return CollectionsUtil.isEmpty(data);
    }

    @NonNull
    @Override
    protected LiveData<List<QBChatDialog>> createApiData() {
        return new LiveData<List<QBChatDialog>>() {
            @Override
            protected void onActive() {
                QBRestChatService.getChatDialogs(null, null).
                        performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
                            @Override
                            public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                                setValue(qbChatDialogs);
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                setValue(null);
                            }
                        });
            }
        };
    }

    protected void performApiReuqest() {

    }
}
