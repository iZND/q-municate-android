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
import java.util.Collection;
import java.util.List;

import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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
    public Observable<List<QMUser>> loadByIds(final Collection<Integer> usersIds) {
            return userDao.getUsersByIDs(usersIds).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).
                    flatMap( (List<QMUser> users) -> {
                        Log.i(TAG, "flatMap" + users);
                        Performer<ArrayList<QBUser>> performer = QBUsers.getUsersByIDs(usersIds, null);
                        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);
                        return observable.map(qbUsers -> {
                            Log.i(TAG, "map" + qbUsers);
                            List<QMUser> qmUsers = QMUser.convertList(qbUsers);
                            userDao.insertAll(qmUsers);
                            return qmUsers;
                        });
                    });
    };

    @Override
    public Completable update(QMUser event) {
        return null;
    }

    @Override
    public Completable save(QMUser user) {
           return userDao.create(user);
    }

    @Override
    protected void performApiReuqest() {

    }
}
