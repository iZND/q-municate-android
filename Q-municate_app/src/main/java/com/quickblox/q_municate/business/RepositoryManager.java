package com.quickblox.q_municate.business;


import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.example.q_municate_chat_service.repository.QBChatDilogRepositoryImpl;
import com.example.q_municate_chat_service.repository.QMUserRepository;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.utils.FinderUnknownUsers;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RepositoryManager {

    private static final String TAG = RepositoryManager.class.getSimpleName();
    private QBChatDilogRepositoryImpl chatDialogRepo;

    private QMUserRepository userRepository;
    private Executor ioExecuotr = Executors.newSingleThreadExecutor();

    public RepositoryManager(QBChatDilogRepositoryImpl chatDialogRepo, QMUserRepository userRepository){
        this.chatDialogRepo = chatDialogRepo;
        this.userRepository = userRepository;
    }

    public LiveData<List<QBChatDialog>> loadDialogs() {
        LiveData<List<QBChatDialog>> listLiveData = chatDialogRepo.load(1, 100);
        listLiveData.observeForever(qbChatDialogs -> {
            ioExecuotr.execute(() -> {
                Log.i(TAG, "findunknown users");
                FinderUnknownUsers finderUnknownUsers =
                        new FinderUnknownUsers(AppSession.getSession().getUser(), qbChatDialogs);
                Collection<Integer> integers = finderUnknownUsers.find();
                userRepository.loadByIds(integers).subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).subscribe();
            });


        });
        return listLiveData;
    }

    public void delete(QBChatDialog dialog) {

    }
}
