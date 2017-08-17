package com.example.q_municate_chat_service.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.q_municate_chat_service.dao.QBChatDialogDao;
import com.example.q_municate_chat_service.entity.PagedResult;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.CollectionsUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Completable;

public class QBChatDilogRepositoryImpl extends BaseRepoImpl<QBChatDialog> implements QBChatDialogRepository{

    public static final String TAG = QBChatDilogRepositoryImpl.class.getSimpleName();

    QBChatDialogDao chatDialogDao;

    public QBChatDilogRepositoryImpl(QBChatDialogDao chatDialogDao) {
        this.chatDialogDao = chatDialogDao;
    }


    @Override
    public Completable create(QBChatDialog dialog) {
        dbExecutor.execute( () -> chatDialogDao.insert(dialog));
        return null;
    }

    @Override
    public void load( int count, int pageNumber, boolean forceLoad,
                                           QBEntityCallback<PagedResult<QBChatDialog>> callback) {
        Log.i(TAG, "loadAll");

    }

    @Override
    public LiveData<List<QBChatDialog>> loadAll(boolean forceLoad) {
        Log.i(TAG, "loadAll");
         final LiveData<List<QBChatDialog>> dbSource = chatDialogDao.getAll();
        result.addSource(dbSource, (dialogs) -> {
                    if (shouldFetch(dialogs)) {
                        result.removeSource(dbSource);
                        loadFromNet();
                    } else {
                        result.setValue(dialogs);
                    }
                });
        return result;
    }


    private void loadFromNet() {
        RepoPageLoader pageLoader = new RepoPageLoader();
        dbExecutor.execute(pageLoader);
        result.addSource(pageLoader.asLiveData(), (dialogs -> {
            Log.i(TAG, "onChanged from api request");
            if (!CollectionsUtil.isEmpty(dialogs)) {
                dbExecutor.execute( () ->  chatDialogDao.insertAll(dialogs));
            }
            result.setValue(dialogs);
        }));

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
    public LiveData<QBChatDialog> loadById(String id, boolean forceLoad) {
        if (!forceLoad) {
            return chatDialogDao.getById(id);
        } else {
            return new LiveData<QBChatDialog>() {
                @Override
                protected void onActive() {
                    QBRestChatService.getChatDialogById(id).
                            performAsync(new QBEntityCallback<QBChatDialog>() {
                                @Override
                                public void onSuccess(QBChatDialog dialog, Bundle bundle) {
                                    setValue(dialog);
                                }

                                @Override
                                public void onError(QBResponseException e) {
                                    setValue(null);
                                }
                            });
                }
            };
        }


      /*  if (!forceLoad) {
            return Observable.defer( () ->Observable.just(chatDialogDao.getById(id)));
        } else {
            return RxUtils.makeObservable(QBRestChatService.getChatDialogById(id));

        }*/
    }

    @Override
    public Completable delete(QBChatDialog dialog) {
        chatDialogDao.delete(dialog);
        return null;
    }

    @Override
    public Completable update(QBChatDialog dialog) {
        Log.i(TAG, "updated dlg="+dialog);
        dbExecutor.execute( () -> chatDialogDao.update(dialog));
        return null;
    }

    @Override
    public void clear() {
        Log.i(TAG, "clear");
        dbExecutor.execute( () -> chatDialogDao.clear());
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
