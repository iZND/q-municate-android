package com.quickblox.q_municate.business;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.q_municate_chat_service.entity.QBMessage;
import com.example.q_municate_chat_service.entity.user.QMUser;
import com.example.q_municate_chat_service.repository.QBChatDilogRepositoryImpl;
import com.example.q_municate_chat_service.repository.QBMessageRepo;
import com.example.q_municate_chat_service.repository.QMUserRepository;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.utils.FinderUnknownUsers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import rx.Observable;

public class ChatDialogsManager {

    private static final String TAG = ChatDialogsManager.class.getSimpleName();
    private QBChatDilogRepositoryImpl chatDialogRepo;

    private QMUserRepository userRepository;

    private QBMessageRepo messageRepo;
    private Executor ioExecuotr = Executors.newSingleThreadExecutor();
    private int pageNumber = 0;

    public ChatDialogsManager(QBChatDilogRepositoryImpl chatDialogRepo, QMUserRepository userRepository
                            ,QBMessageRepo messageRepo){
        this.chatDialogRepo = chatDialogRepo;
        this.userRepository = userRepository;
        this.messageRepo = messageRepo;
    }

    public LiveData<List<QBChatDialog>> loadDialogs(int pageNumber, boolean forceLoad) {
        LiveData<List<QBChatDialog>> listLiveData = chatDialogRepo.load(pageNumber, 100);
        if (!forceLoad){
            return listLiveData;
        }
        listLiveData.observeForever(qbChatDialogs -> {
            ioExecuotr.execute(() -> {
                Log.i(TAG, "findunknown users");
                FinderUnknownUsers finderUnknownUsers =
                        new FinderUnknownUsers(AppSession.getSession().getUser(), qbChatDialogs);
                Collection<Integer> integers = finderUnknownUsers.find();
                List<Integer> userIds = new ArrayList<>(integers);

                LiveData<List<QMUser>> usersLiveData = userRepository.loadUsersByIds(userIds, true);
                final Observer<List<QMUser>> observer = new Observer<List<QMUser>>() {
                    @Override
                    public void onChanged(@Nullable List<QMUser> users) {
                        usersLiveData.removeObserver(this);
                    }
                };
                usersLiveData.observeForever(observer);
            });


        });
        return listLiveData;
    }

    public LiveData<List<QBChatDialog>> loadNextDialogs(boolean forceLoad) {
        LiveData<List<QBChatDialog>> listLiveData = chatDialogRepo.load(1, 100);
        if (!forceLoad){
            return listLiveData;
        }
        listLiveData.observeForever(qbChatDialogs -> {
            ioExecuotr.execute(() -> {
                Log.i(TAG, "findunknown users");
                FinderUnknownUsers finderUnknownUsers =
                        new FinderUnknownUsers(AppSession.getSession().getUser(), qbChatDialogs);
                Collection<Integer> integers = finderUnknownUsers.find();
                List<Integer> userIds = new ArrayList<>(integers);

                LiveData<List<QMUser>> usersLiveData = userRepository.loadUsersByIds(userIds, true);
                final Observer<List<QMUser>> observer = new Observer<List<QMUser>>() {
                    @Override
                    public void onChanged(@Nullable List<QMUser> users) {
                        usersLiveData.removeObserver(this);
                    }
                };
                usersLiveData.observeForever(observer);
            });


        });
        return listLiveData;
    }

    public LiveData<List<QMUser>> loadDialogData(String dlgId){
        return Transformations.switchMap(chatDialogRepo.loadById(dlgId, false),
                 (dialog) -> {
                return userRepository.loadUsersByIds(dialog.getOccupants(), false);
            }
        );
        /*}) chatDialogRepo.loadById(dlgId, false).flatMap(dialog -> {
            return userRepository.loadByIds(dialog.getOccupants(), false);
        }, (dialog, users) -> {
            Pair<QBChatDialog, List<QMUser>> dialogListPair = new Pair<>(dialog, users);
            return dialogListPair;
        });*/

    }

    public LiveData<List<QMUser>> loadUsersInDialog(QBChatDialog dialog){
        return null;
    }

    public void delete(QBChatDialog dialog) {

    }

    public LiveData<List<QBMessage>> loadMessages(String dlgId, boolean forceLoad) {
        return messageRepo.loadAll(dlgId, forceLoad);
    }
}
