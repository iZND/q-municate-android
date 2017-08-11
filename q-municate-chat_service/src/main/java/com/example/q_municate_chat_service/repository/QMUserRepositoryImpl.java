package com.example.q_municate_chat_service.repository;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.q_municate_chat_service.dao.QMUserDao;
import com.example.q_municate_chat_service.entity.user.QMUser;
import com.example.q_municate_chat_service.util.RxUtils;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.core.server.Performer;
import com.quickblox.extensions.RxJavaPerformProcessor;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

public class QMUserRepositoryImpl extends BaseRepoImpl<QMUser> implements QMUserRepository {

    private static final String TAG = QMUserRepositoryImpl.class.getSimpleName();
    private QMUserDao userDao;
    private List<Integer> usersIds;

    public QMUserRepositoryImpl(QMUserDao userDao){
        this.userDao = userDao;
    }


    @Override
    public Completable create(QMUser event) {
        return null;
    }

    @Override
    public LiveData<List<QMUser>> load(int pageNumber, int count) {
        return null;
    }

    @Override
    public LiveData<QMUser> loadById(int id) {
        return null;
    }

    @Override
    public LiveData<List<QMUser>> loadUsersByIds(final List<Integer> usersIds, boolean forceload) {
        Log.i(TAG, "loadAll "+usersIds);
        this.usersIds = usersIds;
        final LiveData<List<QMUser>> dbSource = userDao.getUsersByIDs(usersIds);
        if (!forceload) {
            return dbSource;
        }
        result.addSource(dbSource,(users) -> {
                Log.i(TAG, "onChanged from db source");
                if (shouldFetch(users)) {
                    Log.i(TAG, "users shouldFetch");
                    result.removeSource(dbSource);
                    fetchFromNetwork(dbSource);
                } else {
                    Log.i(TAG, "return users from db");
                    result.setValue(users);
                }
            });
        return result;
    }

    @Override
    public LiveData<List<QMUser>> loadByIds(final List<Integer> usersIds, boolean forceload) {
        if (!forceload) {
            return userDao.getUsersByIDs(usersIds);
        } else {
            return null;/*RxUtils.makeObservable(QBUsers.getUsersByIDs(usersIds, null))
                    .switchMap((qbUsers) -> {
                            List<QMUser> users = QMUser.convertList(qbUsers);
                            return Observable.just(users);
                        })*/

        }
    }

    private void fetchFromNetwork(LiveData<List<QMUser>> dbSource) {
        Log.i(TAG, "fetchFromNetwork");
        final LiveData<List<QMUser>> apiSource = createApiData();
        result.addSource(apiSource, (users) -> {
                Log.i(TAG, "onChanged from api request");
                if (!CollectionsUtil.isEmpty(users)) {
                    dbExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(TAG, "chatDialogDao.insertAll");

                            userDao.insertAll(users);
                        }
                    });
                }
                result.setValue(users);
            });
    }

    @NonNull
    @Override
    protected LiveData<List<QMUser>> createApiData() {
        return new LiveData<List<QMUser>>() {
            @Override
            protected void onActive() {
                QBUsers.getUsersByIDs(usersIds, null).
                        performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
                            @Override
                            public void onSuccess(ArrayList<QBUser> users, Bundle bundle) {
                                List<QMUser> qmUsers = QMUser.convertList(users);
                                setValue(qmUsers);
                            }

                            @Override
                            public void onError(QBResponseException e) {
                                setValue(null);
                            }
                        });
            }
        };
    }

    @Override
    public Completable update(QMUser event) {
        return null;
    }

    @Override
    public Completable save(QMUser user) {
        userDao.create(user);
        return null;
    }

    @Override
    public void clear() {
        Log.i(TAG, "clear");
        dbExecutor.execute( () -> userDao.clear());
    }

    @Override
    protected void performApiReuqest() {

    }
}
