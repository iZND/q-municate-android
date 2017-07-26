package com.quickblox.q_municate.business;


import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.example.q_municate_chat_service.repository.QBChatDilogRepositoryImpl;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.utils.FinderUnknownUsers;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class RepositoryManager {

    private static final String TAG = RepositoryManager.class.getSimpleName();
    QBChatDilogRepositoryImpl chatDialogRepo;

    private Executor ioExecuotr = Executors.newSingleThreadExecutor();

    public RepositoryManager(QBChatDilogRepositoryImpl chatDialogRepo){
        this.chatDialogRepo = chatDialogRepo;
    }

    public LiveData<List<QBChatDialog>> loadDialogs() {
        LiveData<List<QBChatDialog>> listLiveData = chatDialogRepo.load(1, 100);
        listLiveData.observeForever(qbChatDialogs -> {
            ioExecuotr.execute(() -> {
                Log.i(TAG, "findunknown users");
                FinderUnknownUsers finderUnknownUsers =
                        new FinderUnknownUsers(AppSession.getSession().getUser(), qbChatDialogs);
                finderUnknownUsers.find();
            });


        });
        return listLiveData;
    }

    public void delete(QBChatDialog dialog) {

    }
}
