package com.example.q_municate_chat_service.repository;


import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.example.q_municate_chat_service.dao.QMUserDao;
import com.example.q_municate_chat_service.entity.user.QMUser;
import com.quickblox.core.server.Performer;
import com.quickblox.extensions.RxJavaPerformProcessor;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

public class QMUserRepositoryImpl extends BaseRepoImpl<QMUser> implements QMUserRepository {

    private static final String TAG = QMUserRepositoryImpl.class.getSimpleName();
    private QMUserDao userDao;

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
    public Observable<List<QMUser>> loadByIds(final List<Integer> usersIds) {

        return Observable.defer( () -> {
                List<QMUser> cacheUsers = userDao.getUsersByIDs(usersIds);
                if (cacheUsers.size() == 0) {
                    Performer<ArrayList<QBUser>> performer = QBUsers.getUsersByIDs(usersIds, null);
                    final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

                    return observable.map( (qbUsers) -> {
                            Log.i(TAG, "map" + qbUsers);
                            List<QMUser> qmUsers = QMUser.convertList(qbUsers);
                            userDao.insertAll(qmUsers);
                            return qmUsers;

                    });
                } else {
                    return Observable.just(cacheUsers);
                }
            });
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
    protected void performApiReuqest() {

    }
}
