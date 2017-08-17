package com.quickblox.q_municate.business;


import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.q_municate_chat_service.entity.PagedResult;
import com.example.q_municate_chat_service.entity.QBMessage;
import com.example.q_municate_chat_service.entity.user.QMUser;
import com.example.q_municate_chat_service.repository.QBChatDilogRepositoryImpl;
import com.example.q_municate_chat_service.repository.QBMessageRepo;
import com.example.q_municate_chat_service.repository.QMUserRepository;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.q_municate.chat.ChatConnectionProvider;
import com.quickblox.q_municate.utils.LiveDataUtils;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.helpers.QBChatHelper;
import com.quickblox.q_municate_core.utils.FinderUnknownUsers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class ChatDialogsManager{

    private static final String TAG = ChatDialogsManager.class.getSimpleName();
    private QBChatDilogRepositoryImpl chatDialogRepo;

    private QMUserRepository userRepository;

    private QBMessageRepo messageRepo;
    private Executor ioExecuotr = Executors.newSingleThreadExecutor();
    private int pageNumber = 0;

    private Handler handler = new Handler(Looper.getMainLooper());

    public ChatDialogsManager(QBChatDilogRepositoryImpl chatDialogRepo, QMUserRepository userRepository
                            ,QBMessageRepo messageRepo){
        this.chatDialogRepo = chatDialogRepo;
        this.userRepository = userRepository;
        this.messageRepo = messageRepo;
    }

    public LiveData<List<QBChatDialog>> loadDialogs(boolean forceLoad) {
        LiveData<List<QBChatDialog>> listLiveData = chatDialogRepo.loadAll(forceLoad);
        if (!forceLoad){
            return listLiveData;
        }
        LiveDataUtils.observeValue(listLiveData, (qbChatDialogs) -> {
            Log.i(TAG, "observeValue dialogs=" + qbChatDialogs);
            ioExecuotr.execute(() -> {
                Log.i(TAG, "findunknown users");
                FinderUnknownUsers finderUnknownUsers =
                        new FinderUnknownUsers(AppSession.getSession().getUser(), qbChatDialogs);
                Collection<Integer> integers = finderUnknownUsers.find();
                List<Integer> userIds = new ArrayList<>(integers);

                handler.post(() -> LiveDataUtils.observeValue(userRepository.loadUsersByIds(userIds, true), null));
            });

        });
        return listLiveData;
    }

    public LiveData<List<QMUser>> loadDialogData(String dlgId){
        return Transformations.switchMap(chatDialogRepo.loadById(dlgId, false),
                (dialog) -> {
                Log.i(TAG, "udapted dialog =" +dialog);
                return userRepository.loadUsersByIds(dialog.getOccupants(), false);
            }
        );

    }

    public LiveData<QBChatDialog> loadDialog(String dlgId) {
        return null;
    }

    public LiveData<List<QMUser>> loadUsersInDialog(QBChatDialog dialog){
        return null;
    }

    public void delete(QBChatDialog dialog) {

    }

    public LiveData<List<QBMessage>> loadMessages(String dlgId, boolean forceLoad) {
        return messageRepo.loadAll(dlgId, forceLoad);
    }

    public void clearData() {
        chatDialogRepo.clear();
        userRepository.clear();
    }

    public void saveMessage(QBMessage message) {
        messageRepo.create(message);
    }

    public void saveDialog(QBChatDialog chatDialog) {
        chatDialogRepo.create(chatDialog);
    }

    public void updateDialog(QBChatDialog chatDialog) {
        chatDialogRepo.update(chatDialog);
    }
}
